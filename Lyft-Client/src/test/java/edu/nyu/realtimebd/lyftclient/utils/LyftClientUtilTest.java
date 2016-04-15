package edu.nyu.realtimebd.lyftclient.utils;

import edu.nyu.realtimebd.lyftclient.pojo.CostEstimate;
import edu.nyu.realtimebd.lyftclient.pojo.CostEstimates;
import edu.nyu.realtimebd.lyftclient.pojo.OAuthRequest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class LyftClientUtilTest {

    @Test
    public void testGetCostEstimates() throws Exception {
           OAuthRequest request=new OAuthRequest("client_credentials","public");
            LyftClientUtil clientUtil=new LyftClientUtil(request);
            List<Map<String,Float>> costRequestList=new ArrayList<>();
            Map<String,Float> costRequestMap=new HashMap<>();
            costRequestMap.put("startLatitude",37.7772f);
            costRequestMap.put("startLongitude",-122.4233f);
            costRequestMap.put("endLatitude",37.7972f);
            costRequestMap.put("endLongitude",-122.4533f);
            costRequestList.add(costRequestMap);
            List<CostEstimates> pricesResults=clientUtil.getCostEstimates(costRequestList,true);
            pricesResults.stream().forEach((CostEstimates costEstimates)->{
                        costEstimates.getCostEstimates().stream().
                                forEach((CostEstimate costEstimate) -> System.out.println("Cost Estimates = " + costEstimate));
                    }
            );

    }
}