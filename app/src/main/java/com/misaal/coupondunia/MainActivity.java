package com.misaal.coupondunia;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Displays a list of restaurants ordered by proximity to the user's current mLocation.
 *
 * NOTES : -
 *
 * I am storing the restaurant details in the SQLite database because i think the task needs me to
 * (emphasis on efficient storage).
 * However i believe for such an application there is no need to save the data to the database because
 * it changes frequently (according to device location) and is more efficient making calls to the server and
 * storing the data in Java objects for as long as it is required.
 *
 *
 *
 */
public class MainActivity extends ActionBarActivity {

    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String NO_INTERNET = "Couldn't find restaurants. Please check your internet connection";

    private TextView mUserLocationTV;
    private LinearLayout mListLoadingLayout;
    private TextView mEmptyListTV;
    private Location mLocation;
    private RestaurantAdapter mAdapter;
    private ListView restaurantsLV;
    private List<Restaurant> mRestaurants;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // text view at the top of the activity that shows the users current location (area)
        mUserLocationTV = (TextView)findViewById(R.id.user_location);
        // the initial empty view of the listview which shows a progress bar and a textview telling
        // the user that the application is loading the data
        mListLoadingLayout = (LinearLayout)findViewById(R.id.list_loading_layout);
        mListLoadingLayout.setVisibility(View.VISIBLE);
        // a textview that gets set as the empty view if there are no restaurants around or if there's
        // no internet
        mEmptyListTV = (TextView) findViewById(R.id.list_empty_view);
        // the listview that displays the restaurant data
        restaurantsLV = (ListView) findViewById(R.id.restaurant_list);
        restaurantsLV.setEmptyView(mListLoadingLayout);
        // fetch the devices current location
        mLocation = fetchLocation();

        try {
            // fetch the data to be displayed in the list
            mRestaurants = fetchData();
        } catch (Exception e) {}

        if(mRestaurants == null){
            mRestaurants = new ArrayList<>();
        }

        if(mRestaurants != null && !mRestaurants.isEmpty()){
            // initialize the adapter, set it to the listview and sort by distance
            mAdapter = new RestaurantAdapter(this, mRestaurants, mLocation);
            restaurantsLV.setAdapter(mAdapter);
            if(mLocation != null){
                sortAdapter();
            }else{
                Toast.makeText(this, "Couldn't retrieve location!", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        // stop the loading progress bar and display text saying that there is no internet
        if(!isNetworkConnected()){
            mListLoadingLayout.setVisibility(View.GONE);
            mEmptyListTV.setText(getResources().getString(R.string.no_internet_text));
            restaurantsLV.setEmptyView(mEmptyListTV);
            return;
        }else{
            mEmptyListTV.setText(getResources().getString(R.string.list_empty_view_text));
        }


        // tries to find location if it hasn't already been found. Sets up the adapter again and sorts
        // by distance
        if(mLocation == null){
            mLocation = fetchLocation();
            if(mLocation != null){
                sortAdapter();
            }else{
                Toast.makeText(this, "Couldn't get your Location", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Initializes adapter, sets it as adapter to listview and sorts the adapter by distance of the
     * restaurants from the device's current location
     */
    private void sortAdapter() {
        if(mAdapter != null && mAdapter.getCount() > 1){
            mAdapter.sort(new Comparator<Restaurant>() {
                @Override
                public int compare(Restaurant restaurant, Restaurant restaurant2) {
                    double d1 = mAdapter.calculateDistance(restaurant.getLatitude(), restaurant.getLongitude());
                    double d2 = mAdapter.calculateDistance(restaurant2.getLatitude(), restaurant2.getLongitude());
                    return Double.compare(d1, d2);
                }
            });
        }
    }


    /** Store the data in the SQLite database
     *
     * @param restaurants
     */
    private void storeData(List<Restaurant> restaurants) {
        StoreDBDataTask task = new StoreDBDataTask(this, restaurants);
        task.execute();
    }

    /** Fetch the current location of the device
     *
     * @return
     */
    private Location fetchLocation() {
        LocationService service = new LocationService(this);
        Location location = null;
        if(service.canGetLocation()){ // check if location providers are  enabled
            location = service.getLocation();
            setUserLocationText(service, location);
        }else{
            service.showSettingsAlert();
            Log.v(LOG_TAG, "Location services are disabled");
        }

        return location;
    }


    /** Fetches an address from the input Location objects coordinates (lat, lon) and populates the
     * mUserLocationTV with the locality of the address object
     *
     * @param service LocationService
     * @param location : current location of the user
     */
    private void setUserLocationText(LocationService service, Location location) {
        if(location != null && isNetworkConnected()){
            Address address = null;
            try {
                address = service.getAddress(this, location.getLatitude(), location.getLongitude());
            } catch (IOException e) {
                return;
            }
            if(address != null){
                String area = address.getSubLocality();
                mUserLocationTV.setText(area);
            }
        }
    }


    /**
     * Fetches the data of the restaurant by retrieving the data from the database or by downloading
     * the JSON from the URL if the database is empty
     * @return : List<Restaurant> data
     */
    private List<Restaurant> fetchData() throws ExecutionException, InterruptedException {
        List<Restaurant> restaurants = null;
        // try to fetch the data from the database
        FetchDBDataTask dbTask = new FetchDBDataTask(this);
        dbTask.execute();
        restaurants = dbTask.get();
        // if the database has no data fetch from the URL
        if(restaurants.isEmpty()){
            FetchNetDataTask task = new FetchNetDataTask(this);
            task.execute();
            restaurants = task.get();
            // store it in the database
            storeData(restaurants);
        }

        // change from progress bar to empty view that displays that nothing was found
        if(restaurants == null || restaurants.size() < 1){
            mListLoadingLayout.setVisibility(View.GONE);
            mEmptyListTV.setVisibility(View.VISIBLE);
            restaurantsLV.setEmptyView(mEmptyListTV);
        }
        return restaurants;
    }

    /**
     * Checks if the phone has any active network connection and returns true or false
     * @return
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            return false;// There are no active networks.
        } else
            return true;
    }


}
