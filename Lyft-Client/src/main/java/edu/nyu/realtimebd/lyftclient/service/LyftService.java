package edu.nyu.realtimebd.lyftclient.service;

import edu.nyu.realtimebd.lyftclient.pojo.CostEstimates;
import edu.nyu.realtimebd.lyftclient.pojo.OAuthRequest;
import edu.nyu.realtimebd.lyftclient.pojo.OAuthResponse;
import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Created by Ramandeep Singh on 14-04-2016. The LyftService Retrofit class maps to the Lyft REST
 * API
 */

public interface LyftService {

  /**
   * The method to obtain the oauth access token.
   *
   * @param request The request
   * @return {@code Single<OAuthResponse>} token, expiry, token_type and scope
   * @see edu.nyu.realtimebd.lyftclient.pojo.OAuthRequest
   * @see edu.nyu.realtimebd.lyftclient.pojo.OAuthResponse
   */
  @Headers("Content-Type:application/json")
  @POST("oauth/token")
  public Single<OAuthResponse> getAccessToken(@Body OAuthRequest request);

  @GET("v1/cost")
  public Single<CostEstimates> getCostEstimates(@Query("start_lat") Float startLatitude,
      @Query("start_lng") Float startLongitude,
      @Query("end_lat") Float endLatitude,
      @Query("end_lng") Float endLongitude,
      @Query("ride_type") String rideType);


}
