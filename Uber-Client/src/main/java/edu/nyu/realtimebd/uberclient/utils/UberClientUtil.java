package edu.nyu.realtimebd.uberclient.utils;

import edu.nyu.realtimebd.uberclient.pojo.PricesResult;
import edu.nyu.realtimebd.uberclient.service.UberService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


/**
 * Created by Ramandeep Singh on 11-04-2016.
 */
public class UberClientUtil {
    private static final String SERVER_KEY;
    private static final String BASE_URL;
    private static final Integer RATE_LIMIT;
    private static final Retrofit retrofit;
    private static Logger logger= LoggerFactory.getLogger(UberClientUtil.class);
    // Static Initializer for few of the final constants
    static {
        ResourceBundle rb=ResourceBundle.getBundle("uber");
        SERVER_KEY=rb.getString("server_token");
        BASE_URL=rb.getString("uber_endpoint");
        RATE_LIMIT=Integer.parseInt(rb.getString("rate_limit"));

        retrofit= new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * This method accepts a list of coordinates and returns the estimated
     * fare for different uber rides
     * @param priceRequestList The list of coordinates
     * @param invokeWithRateLimit Apply Rate limiting
     * @return A list of Prices per request
     */
    public static List<PricesResult> getPriceRequests(List<Map<String,String>> priceRequestList,boolean invokeWithRateLimit){
        if(invokeWithRateLimit){
        return invokeWithRateLimit(UberClientUtil::getPriceRequest,priceRequestList);
        }
        else{
           return invokeWithoutRateLimit(UberClientUtil::getPriceRequest,priceRequestList);
        }



    }

    /**
     * Generic method which can invoke any function without applying rate limit
     * @param method the function to invoke or apply the each map input to
     * @param inputList The list of Maps, each of which contains the key value pair of service parameters
     * @param <R> Generic Return object type in the list
     * @param <K> Type of Key in Map
     * @param <V> Type of Value in Map
     * @return A list with object type <V>
     */
    private static <R,K,V> List<R> invokeWithoutRateLimit(Function<Map,R> method, List<Map<K,V>> inputList) {
       List<R> returnList=new ArrayList<>();
       inputList.stream().forEach(m->{returnList.add(method.apply(m)); });
       return returnList;
    }

    /**
     * Generic method which can invoke any function with applying rate limit
     * It uses RxJava and Blocking invocation
     * @param method the function to invoke or apply the each map input to
     * @param inputList The list of Maps, each of which contains the key value pair of service parameters
     * @param <R> Generic Return object type in the list
     * @param <K> Type of Key in Map
     * @param <V> Type of Value in Map
     * @return A list with object type <V>
     */
    private static  <R,K,V> List<R> invokeWithRateLimit(Function<Map,R> method,List<Map<K,V>> inputList){
        List<R> returnList=new ArrayList<>();
        Observable.zip(Observable.from(inputList),
                Observable.interval(RATE_LIMIT, TimeUnit.SECONDS), (obs,timer)->obs)
                .doOnNext(item -> {
                           R result= method.apply(item);
                           returnList.add(result);
                        }
                ).toList().toBlocking().first();
        return returnList;
    }

    /**
     * This is the actual method that fetches the price quotes from UBER API
     * It invokes the getPrices method on estimates/price endpoint of the API
     * @param priceRequestParameters A map of the GET request parameters
     * @return
     */
    public static PricesResult getPriceRequest(Map<String,Float> priceRequestParameters ){
        UberService uberService= retrofit.create(UberService.class);
        Float startLatitude=priceRequestParameters.get("startLatitude");
        Float startLongitude=priceRequestParameters.get("startLongitude");
        Float endLatitude=priceRequestParameters.get("endLatitude");
        Float endLongitude=priceRequestParameters.get("endLongitude");
        //Synchronous blocking call.
        Call<PricesResult> call=uberService.getPrices(startLatitude,startLongitude,endLatitude,endLongitude,SERVER_KEY);
        Response<PricesResult> resultResponse=null;
        try {
            resultResponse= call.execute();
        } catch (IOException e) {
          logger.error("Exception ",e);
        }
        return resultResponse.body();


    }



}
