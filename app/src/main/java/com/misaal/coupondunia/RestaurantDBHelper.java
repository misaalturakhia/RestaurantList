package com.misaal.coupondunia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.TableLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Misaal on 20/03/2015.
 */
public class RestaurantDBHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "RestaurantDB.db";
    public static final String RESTAURANT_TABLE_NAME = "restaurants";
    public static final String COLUMN_RESTAURANT_ID = "id";
    public static final String COLUMN_RESTAURANT_NAME = "name";
    public static final String COLUMN_RESTAURANT_URL = "url";
    public static final String COLUMN_RESTAURANT_LATITUDE = "mLatitude";
    public static final String COLUMN_RESTAURANT_LONGITUDE = "mLongitude";
    public static final String COLUMN_RESTAURANT_NO_OF_COUPONS = "coupons";
    public static final String COLUMN_RESTAURANT_CATEGORIES = "categories";
    public static final String COLUMN_RESTAURANT_AREA = "area";


    public RestaurantDBHelper(Context context){
        super(context, DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "create table " + RESTAURANT_TABLE_NAME + " " +
                        "(" + COLUMN_RESTAURANT_ID + " integer primary key autoincrement, "
                        + COLUMN_RESTAURANT_NAME + " text, "
                        + COLUMN_RESTAURANT_URL + " text, "
                        + COLUMN_RESTAURANT_LATITUDE + " real, "
                        + COLUMN_RESTAURANT_LONGITUDE + " real, "
                        + COLUMN_RESTAURANT_NO_OF_COUPONS + " integer,"
                        + COLUMN_RESTAURANT_AREA + " text,"
                        + COLUMN_RESTAURANT_CATEGORIES + " text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i2) {
        db.execSQL("DROP TABLE IF EXISTS "+RESTAURANT_TABLE_NAME);
        onCreate(db);
    }

    public void deleteTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS "+RESTAURANT_TABLE_NAME);
        db.close();
    }

    public void createTable(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("create table "+RESTAURANT_TABLE_NAME+" "+
                "("+COLUMN_RESTAURANT_ID+" integer primary key autoincrement, "
                +COLUMN_RESTAURANT_NAME+" text, "
                +COLUMN_RESTAURANT_URL+" text, "
                +COLUMN_RESTAURANT_LATITUDE+" real, "
                +COLUMN_RESTAURANT_LONGITUDE+" real, "
                +COLUMN_RESTAURANT_NO_OF_COUPONS+" integer,"
                +COLUMN_RESTAURANT_AREA+" text,"
                +COLUMN_RESTAURANT_CATEGORIES+" text)");
    }


    /**
     * Inserts all the data from the list of restaurant objects into the table 'restaurants'
     * @param restaurantList
     * @return
     */
    public boolean insertRestaurantList(List<Restaurant> restaurantList){
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try{
            for(Restaurant restaurant : restaurantList){
                ContentValues contentValues = createRowContent(restaurant.getName(), restaurant.getLogoUrl(),
                        restaurant.getLatitude(), restaurant.getLongitude(), restaurant.getNoOfCoupons(),
                        restaurant.getArea(), restaurant.getCategories());
                db.insert(RESTAURANT_TABLE_NAME, null, contentValues);
            }
            db.setTransactionSuccessful();
        }finally{
            db.endTransaction();
        }
        return true;
    }


    /**
     * Inserts data about one restaurant that corresponds to one row in the 'restaurants' table
     * @param name
     * @param url
     * @param lat
     * @param lon
     * @param coupons
     * @param area
     * @param categoryList
     * @return
     */
    public boolean insertRestaurant (String name, String url, double lat, double lon,int coupons,
                                      String area, List<String> categoryList){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = createRowContent(name, url, lat, lon, coupons, area, categoryList);
        db.insert(RESTAURANT_TABLE_NAME, null, contentValues);
        return true;
    }


    /**
     * Creates a set of row data
     * @param name
     * @param url
     * @param lat
     * @param lon
     * @param coupons
     * @param area
     * @param categoryList
     * @return : ContentValues object that holds the new row data
     */
    private ContentValues createRowContent(String name, String url, double lat, double lon,int coupons,
                                           String area, List<String> categoryList){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_RESTAURANT_NAME, name);
        contentValues.put(COLUMN_RESTAURANT_URL, url);
        contentValues.put(COLUMN_RESTAURANT_LATITUDE, lat);
        contentValues.put(COLUMN_RESTAURANT_LONGITUDE, lon);
        contentValues.put(COLUMN_RESTAURANT_NO_OF_COUPONS, coupons);
        contentValues.put(COLUMN_RESTAURANT_AREA, area);
        contentValues.put(COLUMN_RESTAURANT_CATEGORIES, createCategoryString(categoryList));
        return contentValues;
    }


    /**
     * Performs a read for all restaurant data and returns it in the form of List<Restaurant>
     * @return : List<Restaurant>
     */
    public List<Restaurant> getAllRestaurants() {
        List<Restaurant> restaurantList = new ArrayList<>();
        //hp = new HashMap();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from "+ RESTAURANT_TABLE_NAME, null);
        if(!res.moveToFirst()){ // if the query returns nothing
            return restaurantList;
        }
        while(res.isAfterLast() == false){
            String name = res.getString(res.getColumnIndex(COLUMN_RESTAURANT_NAME));
            String url = res.getString(res.getColumnIndex(COLUMN_RESTAURANT_URL));
            double latitude = res.getDouble(res.getColumnIndex(COLUMN_RESTAURANT_LATITUDE));
            double longitude = res.getDouble(res.getColumnIndex(COLUMN_RESTAURANT_LONGITUDE));
            int coupons = res.getInt(res.getColumnIndex(COLUMN_RESTAURANT_NO_OF_COUPONS));
            String area = res.getString(res.getColumnIndex(COLUMN_RESTAURANT_AREA));
            String categoriesStr = res.getString(res.getColumnIndex(COLUMN_RESTAURANT_CATEGORIES));
            List<String> categories = fetchCategories(categoriesStr);

            restaurantList.add(new Restaurant(name, url, coupons, categories, latitude, longitude, area));
            res.moveToNext();
        }
        return restaurantList;
    }


    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, RESTAURANT_TABLE_NAME);
        return numRows;
    }

    /**
     * Concatenates the categories from the list into a comma-separated string.
     * I realise that this is not an efficient method, but I am doing this because the alternative
     * would be to create a table of categories and a relational table that linked each category to
     * the restaurants. Considering in this task, we don't need to query by category at all, I'm using
     * this method. Also, I have taken into account that a restaurant can't have large amounts of
     * categories associated with it.
     *
     * @param categoryList
     * @return
     */
    private String createCategoryString(List<String> categoryList) {
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < categoryList.size(); i++){
            builder.append(categoryList.get(i));
            if(i != categoryList.size() - 1){
                builder.append(",");
            }
        }
        return builder.toString();
    }


    /**
     * Splits the previously concatenated 'comma-separated' string of categories
     * @param categoriesStr
     * @return
     */
    private List<String> fetchCategories(String categoriesStr) {
        String[] categoryArray = categoriesStr.split(",");
        return  Arrays.asList(categoryArray);
    }
}
