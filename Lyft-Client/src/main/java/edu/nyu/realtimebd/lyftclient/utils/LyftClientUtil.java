package edu.nyu.realtimebd.lyftclient.utils;

import edu.nyu.realtimebd.lyftclient.pojo.CostEstimates;
import edu.nyu.realtimebd.lyftclient.pojo.OAuthRequest;
import edu.nyu.realtimebd.lyftclient.pojo.OAuthResponse;
import edu.nyu.realtimebd.lyftclient.service.LyftService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Created by Ramandeep Singh on 14-04-2016.
 */
public class LyftClientUtil {
    private static final String CLIENT_ID;
    private static final String API_ENDPOINT;
    private static final String CLIENT_SECRET;
    private static final Integer RATE_LIMIT;
    private Retrofit retrofit;
    private Retrofit retrofitAuthenticated;
    private String accessToken = "";
    private static boolean tokenExpired = true;
    private OAuthRequest oAuthRequest;
    private static Logger logger = LoggerFactory.getLogger(LyftClientUtil.class);

    // Static Initializer for few of the final constants
    static {
        ResourceBundle rb = ResourceBundle.getBundle("lyft");
        CLIENT_ID = rb.getString("CLIENT_ID");
        CLIENT_SECRET = rb.getString("CLIENT_SECRET");
        API_ENDPOINT = rb.getString("API_ENDPOINT");
        RATE_LIMIT = Integer.parseInt(rb.getString("RATE_LIMIT"));

    }

    public LyftClientUtil(final OAuthRequest oAuthRequest) {
        this.oAuthRequest = oAuthRequest;
        initializeRetrofitClients();

    }

    /**
     * This method initializes the retrofit clients
     * a) One for the initial authentication end point
     * b) Other for other service requests
     */
    private void initializeRetrofitClients() {
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        OkHttpClient clientNormal;
        OkHttpClient clientAuthenticated;
        builder.interceptors().add(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request.Builder builder = originalRequest.newBuilder().header("Authorization:Bearer ", accessToken).
                        method(originalRequest.method(), originalRequest.body());
                okhttp3.Response response = chain.proceed(builder.build());
                /*
                implies that the token has expired
                or was never initialized
                 */
                if (response.code() == 401) {
                    tokenExpired = true;
                    logger.info("Token Expired");
                    getAuthenticationToken();
                    builder = originalRequest.newBuilder().header("Authorization:Bearer ", accessToken).
                            method(originalRequest.method(), originalRequest.body());
                    response = chain.proceed(builder.build());
                }
                return response;
            }
        });

        clientAuthenticated = builder.build();
        retrofitAuthenticated = new Retrofit.Builder().client(clientAuthenticated)
                .baseUrl(API_ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        OkHttpClient.Builder builder1 = new OkHttpClient().newBuilder();
        builder1.authenticator(new Authenticator() {
            @Override
            public Request authenticate(Route route, okhttp3.Response response) throws IOException {
                String authentication = Credentials.basic(CLIENT_ID, CLIENT_SECRET);
                Request.Builder builder = response.request().newBuilder().addHeader("Authorization", authentication);
                return builder.build();
            }
        });
        clientNormal = builder1.build();
        retrofit = new Retrofit.Builder().client(clientNormal).
                baseUrl(API_ENDPOINT).
                addConverterFactory(GsonConverterFactory.create()).build();
    }

    /**
     * Generic method which can invoke any function without applying rate limit
     *
     * @param method    the function to invoke or apply the each map input to
     * @param inputList The list of Maps, each of which contains the key value pair of service parameters
     * @param <R>       Generic Return object type in the list
     * @param <K>       Type of Key in Map
     * @param <V>       Type of Value in Map
     * @return A list with object type <V>
     */
    private <R, K, V> List<R> invokeWithoutRateLimit(Function<Map, R> method, List<Map<K, V>> inputList) {
        List<R> returnList = new ArrayList<>();
        inputList.stream().forEach(m -> {
            returnList.add(method.apply(m));
        });
        return returnList;
    }

    /**
     * Generic method which can invoke any function with applying rate limit
     * It uses RxJava and Blocking invocation
     *
     * @param method    the function to invoke or apply the each map input to
     * @param inputList The list of Maps, each of which contains the key value pair of service parameters
     * @param <R>       Generic Return object type in the list
     * @param <K>       Type of Key in Map
     * @param <V>       Type of Value in Map
     * @return A list with object type <V>
     */
    private <R, K, V> List<R> invokeWithRateLimit(Function<Map, R> method, List<Map<K, V>> inputList) {
        List<R> returnList = new ArrayList<>();
        Observable.zip(Observable.from(inputList),
                Observable.interval(RATE_LIMIT, TimeUnit.SECONDS), (obs, timer) -> obs)
                .doOnNext(item -> {
                            R result = method.apply(item);
                            returnList.add(result);
                        }
                ).toList().toBlocking().first();
        return returnList;
    }

    /**
     * This method accepts a list of coordinates and returns the estimated
     * fare for different lyft rides
     *
     * @param costRequestList     The list of coordinates
     * @param invokeWithRateLimit Apply Rate limiting
     * @return A list of Prices per request
     */
    public List<CostEstimates> getCostEstimates(List<Map<String, Float>> costRequestList, boolean invokeWithRateLimit) {
        if (invokeWithRateLimit) {
            return invokeWithRateLimit(this::getCostEstimate, costRequestList);
        } else {
            return invokeWithoutRateLimit(this::getCostEstimate, costRequestList);
        }


    }

    /**
     * This is the actual method that fetches the price quotes from Lyft API
     * It invokes the get method on estimates/price endpoint of the API
     *
     * @param costRequestParameters A map of the GET request parameters
     * @return CostEstimates instance for a single trip
     * @see edu.nyu.realtimebd.lyftclient.pojo.CostEstimates
     */
    public CostEstimates getCostEstimate(Map<String, Float> costRequestParameters) {
        LyftService lyftService = retrofitAuthenticated.create(LyftService.class);
        Float startLatitude = costRequestParameters.get("startLatitude");
        Float startLongitude = costRequestParameters.get("startLongitude");
        Float endLatitude = costRequestParameters.get("endLatitude");
        Float endLongitude = costRequestParameters.get("endLongitude");
        //Synchronous blocking call.
        Call<CostEstimates> call = lyftService.getCostEstimates(startLatitude, startLongitude, endLatitude, endLongitude,null);
        Response<CostEstimates> resultResponse = null;
        try {
            resultResponse = call.execute();
        } catch (IOException e) {
            logger.error("Exception ", e);
        }
        if (resultResponse != null) {
            return resultResponse.body();
        }
        return null;


    }

    /**
     * Is invoked only when the access token is required
     * Or it expires
     */
    private void getAuthenticationToken() {
        LyftService lyftService = this.retrofit.create(LyftService.class);
        Call<OAuthResponse> authRequestCall = lyftService.getAccessToken(oAuthRequest);
        Response<OAuthResponse> response = null;
        try {
            response = authRequestCall.execute();
            if (response.isSuccessful()) {
                accessToken = response.body().getAccessToken();
            }
        } catch (IOException e) {
            logger.error("Exception occurred due to ", e);
        }

    }

}
