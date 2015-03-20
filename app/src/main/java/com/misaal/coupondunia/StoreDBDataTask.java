package com.misaal.coupondunia;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

/**
 * Created by Misaal on 20/03/2015.
 */
public class StoreDBDataTask extends AsyncTask<Void, Void, Void>{

    private final Context mContext;
    private final List<Restaurant> restaurants;

    public StoreDBDataTask(Context context, List<Restaurant> restaurantList) {
        this.mContext = context;
        this.restaurants = restaurantList;
    }


    @Override
    protected Void doInBackground(Void... voids) {
        RestaurantDBHelper helper = new RestaurantDBHelper(mContext);
        helper.insertRestaurantList(restaurants);
        return null;
    }
}
