package edu.nyu.realtimebd.lyftclient.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Ramandeep Singh on 14-04-2016.
 */
public class CostEstimates {
    @SerializedName("cost_estimates")
    private List<CostEstimate> costEstimates;

    public List<CostEstimate> getCostEstimates() {
        return costEstimates;
    }

    public void setCostEstimates(List<CostEstimate> costEstimates) {
        this.costEstimates = costEstimates;
    }
}
