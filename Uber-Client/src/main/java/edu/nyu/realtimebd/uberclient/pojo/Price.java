package edu.nyu.realtimebd.uberclient.pojo;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Created by Ramandeep Singh on 11-04-2016.
   This class contains the attributes that are returned by price endpoint of Uber API
   @link https://developer.uber.com/docs/v1-estimates-price
 */

public class Price {
    @SerializedName("localized_display_name")
    private String localizedDisplayName;
    @SerializedName("high_estimate")
    private Float highEstimate;
    @SerializedName("low_estimate")
    private Float lowEstimate;
    @SerializedName("minimum")
    private Float minimumFare;
    private int duration;
    @SerializedName("display_name")
    private String displayName;
    @SerializedName("product_id")
    private String productId;
    @SerializedName("surge_multiplier")
    private Float surgeMultiplier;
    @SerializedName("currency_code")
    private String currencyCode;
    private Float distance;
    private String currentDate;

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getLocalizedDisplayName() {
        return localizedDisplayName;
    }

    public void setLocalizedDisplayName(String localizedDisplayName) {
        this.localizedDisplayName = localizedDisplayName;
    }

    public Float getHighEstimate() {
        return highEstimate;
    }

    public void setHighEstimate(Float highEstimate) {
        this.highEstimate = highEstimate;
    }

    public Float getLowEstimate() {
        return lowEstimate;
    }

    public void setLowEstimate(Float lowEstimate) {
        this.lowEstimate = lowEstimate;
    }

    public Float getMinimumFare() {
        return minimumFare;
    }

    public void setMinimumFare(Float minimumFare) {
        this.minimumFare = minimumFare;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Float getSurgeMultiplier() {
        return surgeMultiplier;
    }

    public void setSurgeMultiplier(Float surgeMultiplier) {
        this.surgeMultiplier = surgeMultiplier;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public Float getDistance() {
        return distance;
    }

    public void setDistance(Float distance) {
        this.distance = distance;
    }

    @Override
    public String toString() {
        return "Price{" +
                "localizedDisplayName='" + localizedDisplayName + '\'' +
                ", highEstimate=" + highEstimate +
                ", lowEstimate=" + lowEstimate +
                ", minimumFare=" + minimumFare +
                ", duration=" + duration +
                ", displayName='" + displayName + '\'' +
                ", productId='" + productId + '\'' +
                ", surgeMultiplier=" + surgeMultiplier +
                ", currencyCode='" + currencyCode + '\'' +
                ", distance=" + distance +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Price)) return false;

        Price price = (Price) o;

        if (duration != price.duration) return false;
        if (!currencyCode.equals(price.currencyCode)) return false;
        if (!displayName.equals(price.displayName)) return false;
        if (!distance.equals(price.distance)) return false;
        if (!highEstimate.equals(price.highEstimate)) return false;
        if (!localizedDisplayName.equals(price.localizedDisplayName)) return false;
        if (!lowEstimate.equals(price.lowEstimate)) return false;
        if (!minimumFare.equals(price.minimumFare)) return false;
        if (!productId.equals(price.productId)) return false;
        if (!surgeMultiplier.equals(price.surgeMultiplier)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = localizedDisplayName.hashCode();
        result = 31 * result + highEstimate.hashCode();
        result = 31 * result + lowEstimate.hashCode();
        result = 31 * result + minimumFare.hashCode();
        result = 31 * result + duration;
        result = 31 * result + displayName.hashCode();
        result = 31 * result + productId.hashCode();
        result = 31 * result + surgeMultiplier.hashCode();
        result = 31 * result + currencyCode.hashCode();
        result = 31 * result + distance.hashCode();
        return result;
    }
}
