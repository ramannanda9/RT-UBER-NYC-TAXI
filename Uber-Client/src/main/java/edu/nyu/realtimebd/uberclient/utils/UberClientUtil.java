package edu.nyu.realtimebd.uberclient.utils;

import edu.nyu.realtimebd.uberclient.pojo.PricesResult;
import edu.nyu.realtimebd.uberclient.service.UberService;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Ramandeep Singh on 11-04-2016.
 */
public class UberClientUtil {

  private static final String SERVER_KEY;
  private static final String BASE_URL;
  private static final Integer RATE_LIMIT;
  private Retrofit retrofit;
  private static Logger logger = LoggerFactory.getLogger(UberClientUtil.class);

  public UberClientUtil() {
    //Nothing for now but future integration may require authentication tokens
    retrofit = new Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build();
  }

  // Static Initializer for few of the final constants
  static {
    ResourceBundle rb = ResourceBundle.getBundle("uber");
    SERVER_KEY = rb.getString("server_token");
    BASE_URL = rb.getString("uber_endpoint");
    RATE_LIMIT = Integer.parseInt(rb.getString("rate_limit"));

  }

  /**
   * This method accepts a list of coordinates and returns the estimated fare for different uber
   * rides
   *
   * @param priceRequests The Flowable each of which is a Map of coordinates
   * @param invokeWithRateLimit Apply Rate limiting
   * @return A Flowable of Prices per request
   */
  public Flowable<PricesResult> getPriceRequests(Flowable<Map<String, Float>> priceRequests,
      boolean invokeWithRateLimit) {
    if (invokeWithRateLimit) {
      return invokeWithRateLimit(this::getPriceRequest, priceRequests);
    } else {
      return invokeWithoutRateLimit(this::getPriceRequest, priceRequests);
    }


  }

  /**
   * Generic method which can invoke any function without applying rate limit
   *
   * @param method the function to invoke or apply the each map input to
   * @param coordinates The Flowable of Maps, each of which contains the key value pair of service
   * parameters
   * @param <R> Generic Return object type
   * @param <K> Type of Key in Map
   * @param <V> Type of Value in Map
   * @return A list with object type <V>
   */
  private <R, K, V> Flowable<R> invokeWithoutRateLimit(Function<Map, Single<R>> method,
      Flowable<Map<K, V>> coordinates) {
    return coordinates.concatMapDelayError(m -> method.apply(m).toFlowable());
  }

  /**
   * Generic method which can invoke any function with applying rate limit It uses RxJava
   *
   * @param method the function to invoke or apply the each map input to
   * @param coordinates The Flowable of Maps, each of which contains the key value pair of service
   * parameters
   * @param <R> Generic Return object type
   * @param <K> Type of Key in Map
   * @param <V> Type of Value in Map
   * @return A {@code Flowable<R>} representing results for the query
   */
  private <R, K, V> Flowable<R> invokeWithRateLimit(Function<Map, Single<R>> method,
      Flowable<Map<K, V>> coordinates) {
    return Flowable.zip(coordinates,
        Flowable.interval(RATE_LIMIT, TimeUnit.SECONDS), (obs, timer) -> obs)
        .concatMapDelayError(item -> method.apply(item).toFlowable());

  }

  /**
   * This is the actual method that fetches the price quotes from UBER API It invokes the getPrices
   * method on estimates/price endpoint of the API
   *
   * @param priceRequestParameters A map of the GET request parameters
   * @return {@code Single<PricesResult>} a Single representing pricesResult from uber
   * @see PricesResult
   */
  public Single<PricesResult> getPriceRequest(Map<String, Float> priceRequestParameters) {
    UberService uberService = retrofit.create(UberService.class);
    Float startLatitude = priceRequestParameters.get("startLatitude");
    Float startLongitude = priceRequestParameters.get("startLongitude");
    Float endLatitude = priceRequestParameters.get("endLatitude");
    Float endLongitude = priceRequestParameters.get("endLongitude");
    return uberService
        .getPrices(startLatitude, startLongitude, endLatitude, endLongitude, SERVER_KEY)
        .retry(3).doOnError(e -> logger.error("Error occurred due to", e));
  }


}
