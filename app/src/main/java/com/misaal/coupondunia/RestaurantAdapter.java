package com.misaal.coupondunia;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/** An adapter that handles the data to be displayed in a list
 * Created by Misaal on 19/03/2015.
 */
public class RestaurantAdapter extends ArrayAdapter<Restaurant>{

    private static final String OFFERS = "Offers";

    private final Context mContext;
    private List<Restaurant> restaurantList;
    private Location mUserLocation;
    private TextView distanceTV;

    /**
     * Constructor
     * @param context
     * @param restaurants
     */
    public RestaurantAdapter(Context context, List<Restaurant> restaurants, Location userLocation){
        super(context, R.layout.restaurant_list_item, restaurants);
        this.mContext = context;
        this.restaurantList = restaurants;
        this.mUserLocation = userLocation;
    }


    @Override
    public int getCount() {
        return restaurantList.size();
    }

    @Override
    public Restaurant getItem(int position) {
        return restaurantList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = null;
        if(view == null){ // inflate restaurant_list_item layout resource
            inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.restaurant_list_item, null);
        }

        // get the Restaurant object that this list element represents
        Restaurant restaurant = getItem(position);

        final ImageView logoIV = (ImageView) view.findViewById(R.id.restaurant_logo);
        String imageUrl = restaurant.getLogoUrl();
        Picasso.with(mContext).load(imageUrl).fit().into(logoIV);

        // set name of restaurant
        final TextView nameTV = (TextView)view.findViewById(R.id.restaurant_name);
        nameTV.setText(restaurant.getName());

        // set the number of coupons/offers available for the restaurant
        final TextView offersTV = (TextView)view.findViewById(R.id.restaurant_offer_count);
        offersTV.setText(restaurant.getNoOfCoupons() + " " + OFFERS);


        final LinearLayout categoriesLayout = (LinearLayout) view.findViewById(R.id.restaurant_categories_layout);
        // clears any previously added views to this layout
        if(categoriesLayout.getChildCount() > 0){
            categoriesLayout.removeAllViews();
        }
        // programmatically adding layouts that display each category.
        List<String> categories = restaurant.getCategories();
        for(String category : categories){
            addCategoryTextView(category, categoriesLayout, inflater);
        }

        // calculate and set distance text
        distanceTV = (TextView)view.findViewById(R.id.restaurant_distance);
        if(mUserLocation != null){
            double distance = calculateDistance(restaurant.getLatitude(), restaurant.getLongitude());
            String distanceTxt = createDistanceText(distance);
            distanceTV.setText(distanceTxt);
        }else{
            distanceTV.setText("???");
        }

        // set area text
        final TextView areaTV = (TextView) view.findViewById(R.id.restaurant_area);
        areaTV.setText(restaurant.getArea());

        return view;
    }


    /**
     * Creates the text for the distance field. If the distance is less than 1 km, it creates a string
     * in meters and otherwise in kms to 1 decimal place
     * @param distance : distance to be converted to text
     * @return
     */
    private String createDistanceText(double distance) {
        String distanceTxt = null;
        if(distance < 1){
            double meterDistance = Math.rint(distance * 1000);
            distanceTxt = meterDistance + " m";
        }else{
            distance = Math.rint(distance * 10) / 10;
            distanceTxt = distance + " km";
        }
        return distanceTxt;
    }


    /**
     * Calculates the distance between the geographical point represented by the input coordinates and
     * the user's current mLocation
     * @param latitude : mLatitude of the restaurant
     * @param longitude : mLongitude of the restaurant
     * @return : distance
     */
    public double calculateDistance(double latitude, double longitude) {
        double userLat = mUserLocation.getLatitude();
        double userLon = mUserLocation.getLongitude();

        // get distance in meters
        double distance = kmDiffBetweenGeoPts(userLat, userLon, latitude, longitude);
        return distance;
    }


    /** Inflates the category_text.xml layout file and adds it to the 'categoriesLayout' of the restaurant
     * list item
     *
     * @param categoryName : the name of the category of the restaurant
     * @param categoriesLayout : the layout where the category view is to be placed
     * @param inflater : layout inflater
     */
    private void addCategoryTextView(String categoryName, LinearLayout categoriesLayout, LayoutInflater inflater) {
        if(inflater == null){
            inflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        }

        View view = inflater.inflate(R.layout.category_text, null);
        final TextView categoryTV = (TextView)view.findViewById(R.id.restaurant_category);
        categoryTV.setText(categoryName);
        categoriesLayout.addView(view);
    }


    /**
     * Checks if the phone has any active network connection and returns true or false
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;// There are no active networks.
        } else
            return true;
    }

    /** Returns the distance between 2 geo points in kilometers
     *
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public double kmDiffBetweenGeoPts(double lat1, double lon1, double lat2, double lon2){
        final int EARTH_RADIUS = 6371; // mean radius of the earth
        double latDiff = deg2radians(lat2 - lat1);  // deg2rad below
        double lonDiff = deg2radians(lon2-lon1);
        double a =       Math.sin(latDiff/2) * Math.sin(latDiff/2) +
                Math.cos(deg2radians(lat1)) * Math.cos(deg2radians(lat2)) *
                        Math.sin(lonDiff/2) * Math.sin(lonDiff/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = EARTH_RADIUS * c; // Distance in km
        return d;
    }


    /**
     * Returns the distance between 2 geographical points in metres
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return
     */
    public long mDiffBetweenGeoPts(double lat1, double lon1, double lat2, double lon2){
        final int metresPerKm = 1000;
        return Math.round(kmDiffBetweenGeoPts(lat1, lon1, lat2, lon2) * metresPerKm);
    }


    /**
     * Converts degrees to radians
     * @param degrees
     * @return
     */
    private double deg2radians(double degrees) {
        return degrees * (Math.PI/180);
    }


}
