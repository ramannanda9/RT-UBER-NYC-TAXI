package edu.nyu.realtimebd.datautil.util;


import edu.nyu.realtimebd.datautil.csv.CSVUtil;
import edu.nyu.realtimebd.datautil.pojo.LocationAndTime;
import edu.nyu.realtimebd.lyftclient.pojo.CostEstimate;
import edu.nyu.realtimebd.lyftclient.pojo.OAuthRequest;
import edu.nyu.realtimebd.lyftclient.utils.LyftClientUtil;
import edu.nyu.realtimebd.uberclient.pojo.Price;
import edu.nyu.realtimebd.uberclient.pojo.PricesResult;
import edu.nyu.realtimebd.uberclient.utils.UberClientUtil;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Ramandeep Singh on 18-04-2016. Main workhorse this class does the orchestration of
 * reading the records, Invoking the API's and storing the CSV file
 */
public class Invoker {

  public static final Logger logger = LoggerFactory.getLogger(Invoker.class);
  public static File file;
  public static String outputDirectory;
  private static boolean onlyUber = false;
  private static boolean onlyLyft = false;
  private static CSVUtil csvUtilUber;
  private static CSVUtil csvUtilLyft;

  public static void main(String[] args) throws InterruptedException {
    Options options = addOptions();
    CommandLineParser parser = new DefaultParser();
    if (args.length <= 2) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(" java -jar datautil.jar ", options);
      return;
    }
    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
      file = new File(cmd.getOptionValue("f"));
      outputDirectory = cmd.getOptionValue("d");
      if (cmd.getOptionValue("u") != null && !cmd.getOptionValue("u").isEmpty()) {
        int value = Integer.parseInt(cmd.getOptionValue("u"));
        if (value == 1) {
          onlyUber = true;
        }
      }
      if (cmd.getOptionValue("l") != null && !cmd.getOptionValue("l").isEmpty()) {
        int value = Integer.parseInt(cmd.getOptionValue("l"));
        if (value == 1) {
          onlyLyft = true;
        }
      }

    } catch (ParseException e) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp(" java -jar datautil.jar ", options);
      return;
    }
    final CountDownLatch latch = new CountDownLatch(1);
    int date = LocalDate.now().getDayOfMonth();
    File uberFile;
    File lyftFile;
    uberFile = new File(outputDirectory + File.separator + "UBER_API_OUTPUT_" + date + "_t.csv");
    lyftFile = new File(outputDirectory + File.separator + "LYFT_API_OUTPUT_" + date + "_t.csv");
    csvUtilUber = new CSVUtil(uberFile);
    csvUtilLyft = new CSVUtil(lyftFile);
    final boolean single = onlyLyft || onlyUber;
    OAuthRequest request = new OAuthRequest("client_credentials", "public");
    final UberClientUtil uberClient = new UberClientUtil();
    final LyftClientUtil lyftClient = new LyftClientUtil(request);
    Flowable<LocationAndTime> records = CSVUtil.readRecordsFromFileAsync(file.toPath());
    Flowable<LocationAndTime> timedEvents = records
        .doOnNext(lt -> logger
            .info("Got location and time {}", getSecondsFromNow(lt)
            ))
        .filter(
            locationAndTime -> getSecondsFromNow(locationAndTime) > 0
        ).flatMap(locationAndTime -> Single
                .timer(
                    getSecondsFromNow(locationAndTime),
                    TimeUnit.SECONDS)
                .zipWith(Single.just(locationAndTime), (t, lt) -> lt).toFlowable()
            , 2);
    timedEvents.subscribe(new Subscriber<LocationAndTime>() {
      Subscription subscription = null;

      @Override
      public void onSubscribe(Subscription s) {
        logger.info("Subscribing");
        subscription = s;
        s.request(1);
      }

      @Override
      public void onNext(LocationAndTime locationAndTime) {
        if (single) {
          if (onlyLyft) {
            Completable
                .fromAction(() -> Invoker.queryLyftTaskAndStore(locationAndTime, lyftClient))
                .subscribeOn(Schedulers.io()).subscribe();
          }
          if (onlyUber) {
            Completable.
                fromAction(() -> Invoker.queryUberAndStore(locationAndTime, uberClient))
                .subscribeOn(Schedulers.io()).subscribe();
          }
        } else {
          Completable uberCall = Completable.
              fromAction(() -> Invoker.queryUberAndStore(locationAndTime, uberClient));
          Completable lyftCall = Completable
              .fromAction(() -> Invoker.queryLyftTaskAndStore(locationAndTime, lyftClient));
          uberCall.mergeWith(lyftCall).subscribeOn(Schedulers.io()).subscribe();
        }
        subscription.request(1);
        logger.debug("Requesting one more");
      }

      @Override
      public void onError(Throwable t) {
        logger.error("Error occurred", t);
      }

      @Override
      public void onComplete() {
        logger.info("Unsubscribing");
        try {
          csvUtilLyft.cleanUp();
        } catch (IOException e) {
          logger.error("Error occurred while clean up", e);
        }
        try {
          csvUtilUber.cleanUp();
        } catch (IOException e) {
          logger.error("Error occurred while clean up", e);
        }
        latch.countDown();
      }
    });
    latch.await();

  }


  private static int getSecondsFromDate(LocationAndTime lt) {
    return lt.getJourneyStartTime().toLocalTime().toSecondOfDay();
  }

  private static int getSecondsFromNow(LocationAndTime lt) {
    return getSecondsFromDate(lt) - (LocalDateTime.now().toLocalTime().toSecondOfDay());
  }

  /**
   * Helper method to invoke the Uber API and then store the record in CSV
   *
   * @param locationAndTime input parameters for the API
   * @param uberClient the instance of uber client
   */
  public static void queryUberAndStore(LocationAndTime locationAndTime, UberClientUtil uberClient) {
    Map<String, Float> requestMap = new HashMap<>();
    requestMap.put("startLatitude", locationAndTime.getStartLatitude());
    requestMap.put("startLongitude", locationAndTime.getStartLongitude());
    requestMap.put("endLatitude", locationAndTime.getEndLatitude());
    requestMap.put("endLongitude", locationAndTime.getEndLongitude());
    LocalDateTime origTime = locationAndTime.getJourneyStartTime();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime toDateTime = LocalDateTime
        .of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), origTime.getHour(),
            origTime.getMinute(), origTime.getSecond());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    uberClient.getPriceRequest(requestMap).subscribe((pr) -> {
      List<Price> prices = pr.getPrices();
      prices.forEach(price -> {
        price.setCurrentDate(formatter.format(toDateTime));
        logger.debug(price.toString());
        csvUtilUber.writeRecordToFile(price);
      });
    }, throwable -> logger.error("Error occurred", throwable));
  }

  /**
   * Helper method to invoke the lyft API and then store the record in CSV file
   *
   * @param locationAndTime input parameters for the API
   * @param lyftClient the instance of uber client
   */
  public static void queryLyftTaskAndStore(LocationAndTime locationAndTime,
      LyftClientUtil lyftClient) {
    Map<String, Float> requestMap = new HashMap<>();
    requestMap.put("startLatitude", locationAndTime.getStartLatitude());
    requestMap.put("startLongitude", locationAndTime.getStartLongitude());
    requestMap.put("endLatitude", locationAndTime.getEndLatitude());
    requestMap.put("endLongitude", locationAndTime.getEndLongitude());
    LocalDateTime origTime = locationAndTime.getJourneyStartTime();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime toDateTime = LocalDateTime
        .of(now.getYear(), now.getMonthValue(), now.getDayOfMonth(), origTime.getHour(),
            origTime.getMinute(), origTime.getSecond());
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    lyftClient.getCostEstimate(requestMap).subscribe(costEstimates -> {
          List<CostEstimate> costEstimateList = costEstimates.getCostEstimates();
          costEstimateList.forEach(costEstimate -> {
            costEstimate.setCurrentDate(formatter.format(toDateTime));
            logger.debug(costEstimate.toString());
            csvUtilLyft.writeRecordToFile(costEstimate);
          });
        }, throwable -> logger.error("Error occurred ", throwable)
    );
  }


  /**
   * Prints help on how to use the program from command line
   */
  public static Options addOptions() {
    Options options = new Options();
    options.addOption("f", true, "The input file ");
    options.addOption("d", true, "The output directory");
    options.addOption("u", true, "Only uber retrieval value is (1,0) default 0");
    options.addOption("l", true, "Only Lyft Retrieval value is (1,0) default 0");
    return options;
  }
}
