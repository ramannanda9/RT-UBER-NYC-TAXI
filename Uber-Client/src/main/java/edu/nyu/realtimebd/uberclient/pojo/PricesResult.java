package edu.nyu.realtimebd.uberclient.pojo;

import java.util.List;

/**
 * Created by Ramandeep Singh on 11-04-2016.
 */
public class PricesResult {


    private List<Price> prices;

    public PricesResult() {

    }
    public List<Price> getPrices() {
        return prices;
    }

    public void setPrices(List<Price> prices) {
        this.prices = prices;
    }
}
