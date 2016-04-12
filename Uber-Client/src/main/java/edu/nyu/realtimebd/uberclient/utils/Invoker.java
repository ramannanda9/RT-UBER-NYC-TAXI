package edu.nyu.realtimebd.uberclient.utils;

import edu.nyu.realtimebd.uberclient.pojo.Price;
import edu.nyu.realtimebd.uberclient.pojo.PricesResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ramandeep Singh on 12-04-2016.
 */
public class Invoker {
    public static void main(String[] args) {
        List<Map<String,String>> list=new ArrayList<Map<String,String>>();
        Map requestPrice=new HashMap<String,Float>();
        requestPrice.put("startLatitude",40.72f);
        requestPrice.put("startLongitude",-74.03f);
        requestPrice.put("endLatitude",41.71f);
        requestPrice.put("endLongitude",-74.08f);
        list.add(requestPrice);
        List<PricesResult> pricesResults=UberClientUtil.getPriceRequests(list, true);
        pricesResults.stream().forEach((PricesResult p)->{
            p.getPrices().stream().
                    forEach((Price price) -> System.out.println("price = " + price));
        }
        );
    }
}
