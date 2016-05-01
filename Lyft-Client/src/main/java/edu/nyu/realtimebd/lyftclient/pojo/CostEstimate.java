package edu.nyu.realtimebd.lyftclient.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Ramandeep Singh on 14-04-2016.
 */
public class CostEstimate {
    @SerializedName("ride_type")
    private String rideType;
    @SerializedName("display_name")
    private String displayName;
    private String currency;
    @SerializedName("estimated_cost_cents_min")
    private Integer minCostCent;
    @SerializedName("estimated_cost_cents_max")
    private Integer maxCostCent;
    @SerializedName("estimated_duration_seconds")
    private Integer journeyDuration;
    @SerializedName("estimated_distance_miles")
    private Float distanceMiles;
    @SerializedName("primetime_confirmation_token")
    private String primeTimeConfirmationToken;
    @SerializedName("primetime_percentage")
    private String primeTimePercentage;
    private String currentDate;

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CostEstimate)) return false;

        CostEstimate that = (CostEstimate) o;

        if (!currency.equals(that.currency)) return false;
        if (!displayName.equals(that.displayName)) return false;
        if (!distanceMiles.equals(that.distanceMiles)) return false;
        if (!journeyDuration.equals(that.journeyDuration)) return false;
        if (maxCostCent != null ? !maxCostCent.equals(that.maxCostCent) : that.maxCostCent != null) return false;
        if (minCostCent != null ? !minCostCent.equals(that.minCostCent) : that.minCostCent != null) return false;
        if (primeTimeConfirmationToken != null ? !primeTimeConfirmationToken.equals(that.primeTimeConfirmationToken) : that.primeTimeConfirmationToken != null)
            return false;
        if (!primeTimePercentage.equals(that.primeTimePercentage)) return false;
        if (!rideType.equals(that.rideType)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = rideType.hashCode();
        result = 31 * result + displayName.hashCode();
        result = 31 * result + currency.hashCode();
        result = 31 * result + (minCostCent != null ? minCostCent.hashCode() : 0);
        result = 31 * result + (maxCostCent != null ? maxCostCent.hashCode() : 0);
        result = 31 * result + journeyDuration.hashCode();
        result = 31 * result + distanceMiles.hashCode();
        result = 31 * result + (primeTimeConfirmationToken != null ? primeTimeConfirmationToken.hashCode() : 0);
        result = 31 * result + primeTimePercentage.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "CostEstimate{" +
                "rideType='" + rideType + '\'' +
                ", displayName='" + displayName + '\'' +
                ", currency='" + currency + '\'' +
                ", minCostCent=" + minCostCent +
                ", maxCostCent=" + maxCostCent +
                ", journeyDuration=" + journeyDuration +
                ", distanceMiles=" + distanceMiles +
                ", primeTimeConfirmationToken='" + primeTimeConfirmationToken + '\'' +
                ", primeTimePercentage='" + primeTimePercentage + '\'' +
                '}';
    }

    public String getRideType() {
        return rideType;
    }

    public void setRideType(String rideType) {
        this.rideType = rideType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Integer getMinCostCent() {
        return minCostCent;
    }

    public void setMinCostCent(Integer minCostCent) {
        this.minCostCent = minCostCent;
    }

    public Integer getMaxCostCent() {
        return maxCostCent;
    }

    public void setMaxCostCent(Integer maxCostCent) {
        this.maxCostCent = maxCostCent;
    }

    public Integer getJourneyDuration() {
        return journeyDuration;
    }

    public void setJourneyDuration(Integer journeyDuration) {
        this.journeyDuration = journeyDuration;
    }

    public Float getDistanceMiles() {
        return distanceMiles;
    }

    public void setDistanceMiles(Float distanceMiles) {
        this.distanceMiles = distanceMiles;
    }

    public String getPrimeTimePercentage() {
        return primeTimePercentage;
    }

    public void setPrimeTimePercentage(String primeTimePercentage) {
        this.primeTimePercentage = primeTimePercentage;
    }

    public String getPrimeTimeConfirmationToken() {
        return primeTimeConfirmationToken;
    }

    public void setPrimeTimeConfirmationToken(String primeTimeConfirmationToken) {
        this.primeTimeConfirmationToken = primeTimeConfirmationToken;
    }
}
