package edu.nyu.realtimebd.uberclient.service;

import edu.nyu.realtimebd.uberclient.pojo.PricesResult;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Ramandeep Singh on 11-04-2016.
 */
public interface UberService {

  /**
   * @param startLatitude the start latitude of the ride
   * @param startLongitude the start longitude of the ride
   * @param endLatitude the end latitude of the ride
   * @param endLongitude the end longitude of the ride
   * @param authentication the server_token in this case
   * @return A Single of type PricesResult
   * @see edu.nyu.realtimebd.uberclient.pojo.PricesResult
   * @see edu.nyu.realtimebd.uberclient.pojo.Price
   */
  @GET("estimates/price")
  public Single<PricesResult> getPrices(@Query("start_latitude") Float startLatitude,
      @Query("start_longitude") Float startLongitude,
      @Query("end_latitude") Float endLatitude,
      @Query("end_longitude") Float endLongitude,
      @Query("server_token") String authentication
  );

}
