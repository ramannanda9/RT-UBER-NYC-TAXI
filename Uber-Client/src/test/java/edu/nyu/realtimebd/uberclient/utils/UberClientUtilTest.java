package edu.nyu.realtimebd.uberclient.utils;

import edu.nyu.realtimebd.uberclient.pojo.Price;
import edu.nyu.realtimebd.uberclient.pojo.PricesResult;
import io.reactivex.Flowable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UberClientUtilTest {

  private Logger logger = LoggerFactory.getLogger(UberClientUtilTest.class);

  @Test
  public void testGetPrices() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    List<Map<String, Float>> list = new ArrayList<>();
    Map<String, Float> requestPrice = new HashMap<>();
    requestPrice.put("startLatitude", 40.72f);
    requestPrice.put("startLongitude", -74.03f);
    requestPrice.put("endLatitude", 41.71f);
    requestPrice.put("endLongitude", -74.08f);
    list.add(requestPrice);
    UberClientUtil clientUtil = new UberClientUtil();
    clientUtil
        .getPriceRequests(Flowable.fromIterable(list), true).
        map(PricesResult::getPrices).subscribe(new Subscriber<List<Price>>() {
      @Override
      public void onSubscribe(Subscription s) {
        logger.info("Subcribed ");
        s.request(Long.MAX_VALUE);
      }

      @Override
      public void onNext(List<Price> prices) {
        prices.forEach(price -> logger.info(price.toString()));
      }

      @Override
      public void onError(Throwable t) {
        logger.error("Error occurred ", t);
      }

      @Override
      public void onComplete() {
        logger.info("Unsubscribed ");
        latch.countDown();
      }
    });
    latch.await();
  }

}