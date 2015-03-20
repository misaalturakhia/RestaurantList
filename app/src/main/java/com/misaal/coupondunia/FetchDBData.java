package com.misaal.coupondunia;

import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

/** An async task that retrieves restaurant data in the form of a List<Restaurant> from the SQLite
 * database on the phone
 * Created by Misaal on 20/03/2015.
 */
public class FetchDBData extends AsyncTask<Void, Void, List<Restaurant>> {

    private final Context mContext;

    public FetchDBData(Context context){
        this.mContext = context;
    }

    @Override
    protected List<Restaurant> doInBackground(Void... voids) {
        RestaurantDBHelper helper = new RestaurantDBHelper(mContext);
        return helper.getAllRestaurants();
    }
}
