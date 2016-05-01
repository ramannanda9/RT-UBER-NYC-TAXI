package edu.nyu.realtimebd.datautil.util;


import edu.nyu.realtimebd.datautil.csv.CSVUtil;
import edu.nyu.realtimebd.datautil.pojo.LocationAndTime;
import edu.nyu.realtimebd.lyftclient.pojo.CostEstimate;
import edu.nyu.realtimebd.lyftclient.pojo.OAuthRequest;
import edu.nyu.realtimebd.lyftclient.utils.LyftClientUtil;
import edu.nyu.realtimebd.uberclient.pojo.Price;
import edu.nyu.realtimebd.uberclient.utils.UberClientUtil;
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Ramandeep Singh on 18-04-2016.
 * Main workhorse this class does the orchestration of reading the records,
 * Invoking the API's and storing the CSV file
 */
public class Invoker {
    public static final Logger logger= LoggerFactory.getLogger(Invoker.class);
    public static File file;
    public static String outputDirectory;
    private static boolean onlyUber=false;
    private static boolean onlyLyft=false;
    private static CSVUtil csvUtilUber;
    private static CSVUtil csvUtilLyft;
    private static ExecutorService executor=null;

    public static void main(String[] args) {
        Options options=addOptions();
        List<Future> futures=new ArrayList<>();
        CommandLineParser parser=new DefaultParser();
        if(args.length<=2){
            HelpFormatter formatter=new HelpFormatter();
            formatter.printHelp(" java -jar datautil.jar ",options);
            return;
        }
        CommandLine cmd= null;
        try {
             cmd = parser.parse(options, args);
             file = new File(cmd.getOptionValue("f"));
             outputDirectory=cmd.getOptionValue("d");
             if(cmd.getOptionValue("u")!=null&&!cmd.getOptionValue("u").isEmpty()){
               int value=Integer.parseInt(cmd.getOptionValue("u"));
                 if(value==1){
                     onlyUber=true;
                 }
             }
            if(cmd.getOptionValue("l")!=null&&!cmd.getOptionValue("l").isEmpty()){
                int value=Integer.parseInt(cmd.getOptionValue("l"));
                if(value==1){
                    onlyLyft=true;
                }
            }

         }catch (ParseException e){
             HelpFormatter formatter=new HelpFormatter();
             formatter.printHelp(" java -jar datautil.jar ",options);
             return;
         }

        int date=LocalDate.now().getDayOfMonth();
        File uberFile;
        File lyftFile;
        uberFile=new File(outputDirectory+ File.separator+"UBER_API_OUTPUT_"+date+"_t.csv");
        lyftFile=new File(outputDirectory+ File.separator+"LYFT_API_OUTPUT_"+date+"_t.csv");
        csvUtilUber=new CSVUtil(uberFile);
        csvUtilLyft=new CSVUtil(lyftFile);

        boolean single=false;
        if(!onlyLyft&&!onlyUber){
            executor= daemonTwoThreadService();
        }
        else{
            single=true;
        }
        OAuthRequest request=new OAuthRequest("client_credentials","public");
        final UberClientUtil uberClient=new UberClientUtil();
        final LyftClientUtil lyftClient= new LyftClientUtil(request);
        List<LocationAndTime> recordList=CSVUtil.readRecordsFromFile(file);

        int i=0;
        try {
            while (true) {
                if (i >= recordList.size()) {
                    break;
                }
                LocationAndTime locationAndTime = recordList.get(i);
                ZonedDateTime zonedDateTime = ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault());
                LocalTime localTime = zonedDateTime.toLocalTime();
                LocalDateTime localDateTime = Instant.ofEpochMilli(locationAndTime.getJourneyStartTime().getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalTime origTime = localDateTime.toLocalTime();
                if (origTime.isBefore(localTime) || origTime.equals(localTime)) {
                    i = i + 1;
                    if (!single) {
                        Runnable uberTask = () -> {
                            Invoker.queryUberAndStore(locationAndTime, uberClient);
                        };
                            futures.add(executor.submit(uberTask));

                            Runnable lyftTask = () -> {
                                Invoker.queryLyftTaskAndStore(locationAndTime, lyftClient);
                            };
                            futures.add(executor.submit(lyftTask));

                    } else if (onlyUber) {
                        Invoker.queryUberAndStore(locationAndTime, uberClient);
                    } else {
                        Invoker.queryLyftTaskAndStore(locationAndTime, lyftClient);
                    }
                }
            }


       //Wait for submitted tasks to complete
        if(!single){
           for(Future future:futures){
               try {
                   if(future!=null) {
                       future.get();
                   }
               } catch (InterruptedException e) {
                   e.printStackTrace();
               } catch (ExecutionException e) {
                   e.printStackTrace();
               }
           }
            executor.shutdownNow();
        }
            csvUtilLyft.cleanUp();
            csvUtilUber.cleanUp();
        }
        catch (IOException e){

        }


    }

    /**
     * Helper method to invoke the Uber API and then store the record in CSV
     * @param locationAndTime input parameters for the API
     * @param uberClient the instance of uber client
     */
    public static void queryUberAndStore(LocationAndTime locationAndTime,UberClientUtil uberClient){
        Map requestMap=new HashMap<>();
        requestMap.put("startLatitude",locationAndTime.getStartLatitude());
        requestMap.put("startLongitude",locationAndTime.getStartLongitude());
        requestMap.put("endLatitude",locationAndTime.getEndLatitude());
        requestMap.put("endLongitude",locationAndTime.getEndLongitude());
        List<Price> prices=uberClient.getPriceRequest(requestMap).getPrices();
        LocalDateTime localDateTime=Instant.ofEpochMilli(locationAndTime.getJourneyStartTime().getTime()).atZone( ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime now=LocalDateTime.now();
        LocalDateTime toDateTime=LocalDateTime.of( now.getYear(),now.getMonthValue(),now.getDayOfMonth() , localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if(prices!=null){
            for(Price price:prices){
                price.setCurrentDate(formatter.format(toDateTime.atZone(ZoneId.systemDefault()).toInstant()));
                csvUtilUber.writeRecordToFile(price);
            }
        }




    }

    /**
     * Helper method to invoke the lyft API and then store the record in CSV file
     * @param locationAndTime input parameters for the API
     * @param lyftClient the instance of uber client
     */
    public static void queryLyftTaskAndStore(LocationAndTime locationAndTime,LyftClientUtil lyftClient){
        Map requestMap=new HashMap<>();
        requestMap.put("startLatitude",locationAndTime.getStartLatitude());
        requestMap.put("startLongitude",locationAndTime.getStartLongitude());
        requestMap.put("endLatitude",locationAndTime.getEndLatitude());
        requestMap.put("endLongitude",locationAndTime.getEndLongitude());
        List<CostEstimate> costEstimates=lyftClient.getCostEstimate(requestMap).getCostEstimates();
        LocalDateTime localDateTime=Instant.ofEpochMilli(locationAndTime.getJourneyStartTime().getTime()).atZone( ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime now=LocalDateTime.now();
        LocalDateTime toDateTime=LocalDateTime.of( now.getYear(),now.getMonthValue(),now.getDayOfMonth() , localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if(costEstimates!=null){
            for(CostEstimate estimate:costEstimates){
                estimate.setCurrentDate(formatter.format(toDateTime.atZone(ZoneId.systemDefault()).toInstant()));
                csvUtilLyft.writeRecordToFile(estimate);
            }
        }


    }


    /**
     * Creates an executor service instance
     * which spawns daemon threads.
     * These thread shut down when the program exits
     * To prevent memory and resource leak.
     * @return
     */
    public static  ExecutorService daemonTwoThreadService(){
        return Executors.newFixedThreadPool(2,new ThreadFactory(){

            @Override
            public Thread newThread(Runnable r) {
                Thread t= Executors.defaultThreadFactory().newThread(r);
                t.setDaemon(true);
                return t;
            }
        });
    }

    /**
     * Prints help on how to use the program from command line
     * @return
     */
    public static Options addOptions() {
        Options options = new Options();
        options.addOption("f", true, "The input file ");
        options.addOption("d", true, "The output directory");
        options.addOption("u", true, "Only uber retrieval value is (1,0) default 0");
        options.addOption("l",true,"Only Lyft Retrieval value is (1,0) default 0");
        return options;
    }
}
