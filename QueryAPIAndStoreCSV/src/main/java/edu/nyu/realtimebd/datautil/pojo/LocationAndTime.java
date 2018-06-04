package edu.nyu.realtimebd.datautil.pojo;


import java.time.LocalDateTime;
import java.util.Date;

/**
 * Created by Ramandeep Singh on 18-04-2016.
 */
public class LocationAndTime {
    private float startLatitude;
    private float startLongitude;
    private float endLatitude;
    private float endLongitude;
    private LocalDateTime journeyStartTime;

    public LocalDateTime getJourneyStartTime() {
        return journeyStartTime;
    }

    public void setJourneyStartTime(LocalDateTime journeyStartTime) {
        this.journeyStartTime = journeyStartTime;
    }

    public float getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(float startLatitude) {
        this.startLatitude = startLatitude;
    }

    public float getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(float startLongitude) {
        this.startLongitude = startLongitude;
    }

    public float getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(float endLatitude) {
        this.endLatitude = endLatitude;
    }

    public float getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(float endLongitude) {
        this.endLongitude = endLongitude;
    }
}
