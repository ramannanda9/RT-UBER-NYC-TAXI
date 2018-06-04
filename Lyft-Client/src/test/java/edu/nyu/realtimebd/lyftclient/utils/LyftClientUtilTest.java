package edu.nyu.realtimebd.lyftclient.utils;

import edu.nyu.realtimebd.lyftclient.pojo.CostEstimate;
import edu.nyu.realtimebd.lyftclient.pojo.CostEstimates;
import edu.nyu.realtimebd.lyftclient.pojo.OAuthRequest;
import io.reactivex.Flowable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.Schedulers;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;

public class LyftClientUtilTest {

  private Logger logger = LoggerFactory.getLogger(LyftClientUtilTest.class);

  @Test
  public void testGetCostEstimates() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);
    OAuthRequest request = new OAuthRequest("client_credentials", "public");
    LyftClientUtil clientUtil = new LyftClientUtil(request);
    List<Map<String, Float>> costRequestList = new ArrayList<>();
    Map<String, Float> costRequestMap = new HashMap<>();
    costRequestMap.put("startLatitude", 37.7772f);
    costRequestMap.put("startLongitude", -122.4233f);
    costRequestMap.put("endLatitude", 37.7972f);
    costRequestMap.put("endLongitude", -122.4533f);
    costRequestList.add(costRequestMap);
    costRequestList.add(costRequestMap);
    Flowable<CostEstimates> pricesResults = clientUtil
        .getCostEstimates(Flowable.fromIterable(costRequestList), true);
    pricesResults.map(CostEstimates::getCostEstimates).
        subscribe(new Subscriber<List<CostEstimate>>() {
          @Override
          public void onSubscribe(Subscription s) {
            s.request(Long.MAX_VALUE - 1);
          }

          @Override
          public void onNext(List<CostEstimate> costEstimates) {
            costEstimates.forEach(l -> logger.debug(l.toString()));
          }

          @Override
          public void onError(Throwable t) {
            logger.error("Error occurred", t);
          }

          @Override
          public void onComplete() {
            logger.info("Done");
            latch.countDown();
          }
        });
    latch.await();
  }
}