package com.misaal.coupondunia;

import android.location.Address;
import android.location.Location;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Displays a list of restaurants ordered by proximity to the user's current mLocation.
 *
 * NOTES : -
 * Not using Picasso for downloading images from a url on purpose.
 *
 * I am storing the restaurant details in the SQLite database because i think the task needs me to.
 * However i believe for such an application there is no need to save the data to the database because
 * it changes frequently (according to mLocation) and is more efficient making calls to the server and
 * storing the data in Java objects as long as it is required.
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

        mUserLocationTV = (TextView)findViewById(R.id.user_location);


        mListLoadingLayout = (LinearLayout)findViewById(R.id.list_loading_layout);
        mListLoadingLayout.setVisibility(View.VISIBLE);

        mEmptyListTV = (TextView)findViewById(R.id.list_empty_view);


        restaurantsLV = (ListView)findViewById(R.id.restaurant_list);
        restaurantsLV.setEmptyView(mListLoadingLayout);

        mLocation = fetchLocation();

        try {
            mRestaurants = fetchData();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(mLocation != null){
            setUpAdapter();
        }

    }


    /**
     * Initializes adapter, sets it as adapter to listview and sorts the adapter by distance of the
     * restaurants from the device's current location
     */
    private void setUpAdapter() {
        mAdapter = new RestaurantAdapter(this, mRestaurants, mLocation);
        restaurantsLV.setAdapter(mAdapter);
        mAdapter.sort(new Comparator<Restaurant>() {
            @Override
            public int compare(Restaurant restaurant, Restaurant restaurant2) {
                double d1 = mAdapter.calculateDistance(restaurant.getLatitude(), restaurant.getLongitude());
                double d2 = mAdapter.calculateDistance(restaurant2.getLatitude(), restaurant2.getLongitude());
                return Double.compare(d1, d2);
            }
        });
    }


    /** Store the data in the SQLite database
     *
     * @param restaurants
     */
    private void storeData(List<Restaurant> restaurants) {
        StoreDBDataTask task = new StoreDBDataTask(this, restaurants);
        task.execute();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(mLocation == null){
            mLocation = fetchLocation();
            setUpAdapter();
        }
    }

    /** Fetch the current mLocation of the user
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
        if(location != null){
            Address address = null;
            try {
                address = service.getAddress(this, location.getLatitude(), location.getLongitude());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(address != null){
                String area = address.getSubLocality();
                mUserLocationTV.setText(area);
            }
        }
    }


    /**
     * Fetches the data of the restaurant by downloading the JSON from the URL
     * @return
     */
    private List<Restaurant> fetchData() throws ExecutionException, InterruptedException {
        List<Restaurant> restaurants = null;

        FetchDBData dbTask = new FetchDBData(this);
        dbTask.execute();
        restaurants = dbTask.get();

        if(restaurants.isEmpty()){
            FetchNetworkDataTask task = new FetchNetworkDataTask(this);
            task.execute();
            restaurants = task.get();
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

}
