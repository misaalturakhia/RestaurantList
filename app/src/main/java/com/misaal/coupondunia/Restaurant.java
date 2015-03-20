package com.misaal.coupondunia;

import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.List;

/** A class that holds the data about a restaurant that is to be displayed in a list
 * Created by Misaal on 18/03/2015.
 */
public class Restaurant {

    private String name;
    private String logoUrl;
    private int noOfCoupons;
    private List<String> categories;
    private double latitude;
    private double longitude;
    private String area;

    /**
     * Hiding default constructor
     */
    private Restaurant(){}

    /**
     * Constructor
     * @param name
     * @param url
     * @param offers
     * @param categoryList
     * @param lat
     * @param lon
     * @param area
     */
    public Restaurant(String name, String url, int offers, List<String> categoryList, double lat, double lon, String area){
        this.name = name;
        this.logoUrl = url;
        this.noOfCoupons = offers;
        this.categories = new ArrayList<>(categoryList);
        this.latitude = lat;
        this.longitude = lon;
        this.area = area;
    }

    // getters

    public String getName() {
        return name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public int getNoOfCoupons() {
        return noOfCoupons;
    }

    public List<String> getCategories() {
        return categories;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getArea() {
        return area;
    }

}
