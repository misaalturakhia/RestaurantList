package com.misaal.coupondunia;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Reads a JSON String and provides a method to extract relevant restaurant data into a list
 * Created by Misaal on 19/03/2015.
 */
public class JSONReader {

    private final String jsonStr;

    public JSONReader(String str){
        jsonStr = str;
    }


    /** Extracts data from the JSON String and stores it in a List<Restaurant>
     *
     * @return : List<Restaurant>
     * @throws JSONException
     */
    public List<Restaurant> extractRestaurantList() throws JSONException {
        JSONObject reader = new JSONObject(jsonStr);
        JSONObject data = reader.getJSONObject("data");
        Iterator keys = data.keys();
        List<Restaurant> restaurants = new ArrayList<>();
        while(keys.hasNext()){
            String key = (String) keys.next();
            JSONObject restaurantData = data.getJSONObject(key);

            // get attributes
            String name = restaurantData.getString("OutletName");
            String url = restaurantData.getString("LogoURL");
            int numOfCoupons = restaurantData.getInt("NumCoupons");
            double latitude = restaurantData.getDouble("Latitude");
            double longitude = restaurantData.getDouble("Longitude");
            String area = restaurantData.getString("NeighbourhoodName");

            // get categories array and put it into list
            JSONArray categories = restaurantData.getJSONArray("Categories");
            List<String> catList = new ArrayList<>();
            for(int j = 0 ; j < categories.length(); j++){
                String categoryName = categories.getJSONObject(j).getString("Name");
                String categoryType = categories.getJSONObject(j).getString("CategoryType");

                if(!catList.contains(categoryName)){ // make sure category names are not repeated
                    if(categoryType.equals("TypeOfRestaurant")){
                        catList.add(0, categoryName); // add type of restaurant at the start
                    }else if(categoryType.equals("Cuisine")){
                        catList.add(categoryName);
                    }
                }
            }

            Restaurant restaurant = new Restaurant(name, url, numOfCoupons, catList, latitude, longitude, area);
            restaurants.add(restaurant);
        }

        return restaurants;
    }

}
