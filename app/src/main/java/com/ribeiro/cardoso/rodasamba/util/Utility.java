package com.ribeiro.cardoso.rodasamba.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

import com.ribeiro.cardoso.rodasamba.R;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utility {

    private static final String SHARED_PREFERENCES_PREFIX = "com.ribeiro.cardoso.samba";
    private static final String SHARED_PREFERENCES_LASTSYNC = "last_sync";

    /**
     * Verifies if the app is launching for the first time, so the app can show the settings screen
     * @param context app/activity context
     * @return whether is the first launch or not
     */
    public static boolean isFirstLaunch(Context context){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String first_launch_key = context.getString(R.string.pref_first_launch_key);
        final boolean aBoolean = sharedPreferences.getBoolean(first_launch_key, true);

        if (aBoolean){
            sharedPreferences.edit().putBoolean(first_launch_key, false).apply();
        }

        return aBoolean;
    }

    public static void forceFirstLaunch(Context context, boolean boolValue){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String first_launch_key = context.getString(R.string.pref_first_launch_key);

        sharedPreferences.edit().putBoolean(first_launch_key, boolValue).apply();
    }

    /**
     * Verifies if the the user was created and saved in preferences
     * @param context app/activity context
     * @return whether is the first launch or not
     */
    public static boolean isUserCreated(Context context){

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String user_id_key = context.getString(R.string.pref_user_id_key);
        final String user_id_default = context.getString(R.string.pref_user_id_default);
        final String user_id = sharedPreferences.getString(user_id_key, user_id_default);

        if (user_id.equals(user_id_default)){
            return false;
        }

        return true;
    }

    public static void setUserId(Context context, String id){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String user_id_key = context.getString(R.string.pref_user_id_key);
        sharedPreferences.edit().putString(user_id_key, id).apply();
    }

    public static String getUserId(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        final String user_id_key = context.getString(R.string.pref_user_id_key);
        final String user_id_default = context.getString(R.string.pref_user_id_default);
        final String user_id = sharedPreferences.getString(user_id_key, user_id_default);

        return  user_id;
    }

    public static int getRegionUser(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String defKey = context.getString(R.string.pref_user_region_id_key);
        final int defValue = Integer.parseInt(context.getString(R.string.pref_user_region_id_default));
        return sharedPreferences.getInt(defKey, defValue);
    }

    public static void setRegionUser(Context context, int id){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String defKey = context.getString(R.string.pref_user_region_id_key);
        sharedPreferences.edit().putInt(defKey, id).apply();
    }

    public static int getAgeGroupUser(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String defKey = context.getString(R.string.pref_user_age_group_id_key);
        final int defValue = Integer.parseInt(context.getString(R.string.pref_user_age_group_id_default));
        return sharedPreferences.getInt(defKey, defValue);
    }

    public static void setAgeGroupUser(Context context, int id){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String defKey = context.getString(R.string.pref_user_age_group_id_key);
        sharedPreferences.edit().putInt(defKey, id).apply();
    }

    public static String getSexUser(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String defKey = context.getString(R.string.pref_user_sex_key);
        final String defValue = context.getString(R.string.pref_user_sex_default);
        return sharedPreferences.getString(defKey, defValue);
    }

    public static void setSexUser(Context context, String sex){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        final String defKey = context.getString(R.string.pref_user_sex_key);
        sharedPreferences.edit().putString(defKey, sex).apply();
    }

    /**
     * Get the Date object, from a given date string
     * @param dateString String that represents the date in format yyyy-MM-dd
     *
     * @return Date object representing the date string
     */
    public static Date getDateFromDateString(String dateString) {
        Date parsedDate = null;

        try {
            parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        return  parsedDate;
    }

    /**
     * Get the DateTime object, from a given date string
     * @param dateString String that represents the date in format yyyy-MM-dd
     * @param timeString String that represents the time in format HH:mm:ss
     *
     * @return DateTIME object representing the date string
     */
    public static DateTime getDateTimeFromDateString(String dateString, String timeString) {
        DateTime parsedDate = null;

        DateTimeFormatter formattter;

        if (timeString == null || timeString.isEmpty()) {
            formattter = DateTimeFormat.forPattern("yyyy-MM-dd");
        }
        else {
            formattter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
        }

        parsedDate = formattter.parseDateTime(dateString + " " + timeString);

        return  parsedDate;
    }

    /**
     * Checks if a DateTime object is occurring today
     * @param dateTime DateTime the DateTime object to check
     *
     * @return boolean indicating if the dateTime is occurring today
     */
    public static boolean isOccurringToday(DateTime dateTime) {
        DateTime now = new DateTime();
        LocalDate today = now.toLocalDate();
        LocalDate tomorrow = today.plusDays(1);

        DateTime startOfToday = today.toDateTimeAtStartOfDay(now.getZone());
        DateTime startOfTomorrow = tomorrow.toDateTimeAtStartOfDay(now.getZone());

        return startOfToday.isBefore(dateTime) && startOfTomorrow.isAfter(dateTime);
    }

    public static boolean isOccurringThisWeek(DateTime dateTime) {
        DateTime now = new DateTime();

        return dateTime.getWeekOfWeekyear() == now.getWeekOfWeekyear();
    }

    public static boolean isOccurringThisMonth(DateTime dateTime) {
        DateTime now = new DateTime();

        return dateTime.getMonthOfYear() == now.getMonthOfYear();
    }

    /**
     * Get the Month in format MMM (eg. JUL), from a given date
     * @param dateString String that represents the date in format YYYY-MM-DD
     *
     * @return String with the month of the date
     */
    public static String getMonthFromDateString(String dateString){
        if (dateString == null || dateString.isEmpty()) return null;

        DateFormat dateFormat = new SimpleDateFormat("MMM");
        return dateFormat.format(Utility.getDateFromDateString(dateString));
    }

    /**
     * Get the Month in format MMMM (eg. Julho), from a given date
     * @param dateString String that represents the date in format YYYY-MM-DD
     *
     * @return String with the month of the date
     */
    public static String getFullMonthFromDateString(String dateString){
        if (dateString == null || dateString.isEmpty()) return null;

        DateFormat dateFormat = new SimpleDateFormat("MMMM");
        return dateFormat.format(Utility.getDateFromDateString(dateString));
    }

    /**
     * Get the day from a given date
     * @param dateString String that represents the date in format YYYY-MM-DD
     *
     * @return String with the day of the date
     */
    public static String getDayFromDateString(String dateString){
        if (dateString == null || dateString.isEmpty()) return null;

        DateFormat dateFormat = new SimpleDateFormat("dd");
        return dateFormat.format(Utility.getDateFromDateString(dateString));
    }

    /**
     * Get the hour part from a given time
     * @param timeString String that represents the date in format hh:mm:ss
     *
     * @return String with the hour of the time in format hh
     */
    public static String getHourFromTimeString(String timeString) {
        if (timeString == null) return null;

        Date date = null;
        try {
            date = new SimpleDateFormat("HH:mm:ss").parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat dateFormat = new SimpleDateFormat("HH");
        return dateFormat.format(date);
    }

    /**
     * Get the minute part from a given time
     * @param timeString String that represents the date in format hh:mm:ss
     *
     * @return String with the minute of the time in format mm
     */
    public static String getMinuteFromTimeString(String timeString) {
        if (timeString == null) return null;

        Date date = null;
        try {
            date = new SimpleDateFormat("HH:mm:ss").parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat dateFormat = new SimpleDateFormat("mm");
        return dateFormat.format(date);
    }

    /**
     * Get the hour and minute part from a given time
     * @param timeString String that represents the date in format hh:mm:ss
     *
     * @return String with the hour and minute of the time in format hh:mm
     */
    public static String getHoursAndMinutesFromTimeString(String timeString){
        return Utility.getHourFromTimeString(timeString) + ":" + Utility.getMinuteFromTimeString(timeString);
    }


    /**
     * Checks if the user is connected to the internet
     * @param context Context to check
     *
     * @return boolean true if connected, false otherwise
     */
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();

        return (ni != null && ni.isConnected());
    }

    public static void updateLastSync(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Utility.SHARED_PREFERENCES_PREFIX, Context.MODE_PRIVATE);

        preferences.edit().putLong(SHARED_PREFERENCES_LASTSYNC, new Date().getTime()).apply();
    }

    public static DateTime getLastSync(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(Utility.SHARED_PREFERENCES_PREFIX, Context.MODE_PRIVATE);

        long lastSyncLong = preferences.getLong(SHARED_PREFERENCES_LASTSYNC, 0);

        if (lastSyncLong > 0) {
            return new DateTime(lastSyncLong);
        }

        return null;
    }

    public static String getDeviceName() {
        String manufacturer = android.os.Build.MANUFACTURER;
        String model = android.os.Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static class Pair<L,R> {

        private final L left;
        private final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        public L getLeft() { return left; }
        public R getRight() { return right; }

        @Override
        public int hashCode() { return left.hashCode() ^ right.hashCode(); }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (!(o instanceof Pair)) return false;
            Pair pairo = (Pair) o;
            return this.left.equals(pairo.getLeft()) &&
                    this.right.equals(pairo.getRight());
        }
    }

}
