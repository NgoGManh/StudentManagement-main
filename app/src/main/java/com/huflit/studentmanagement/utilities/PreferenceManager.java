package com.huflit.studentmanagement.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

public class PreferenceManager {
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_PREFERENCE_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void putBoolean(String key, Boolean value) {
        //Edit data object
        SharedPreferences.Editor editor = sharedPreferences.edit();
        //add value to SharedPreferences
        editor.putBoolean(key, value);
        //save changes
        editor.apply();
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }
    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public <T> void putObject(String key, T value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        String json = gson.toJson(value);
        editor.putString(key, json);
        editor.apply();
    }

    public <T> T getObject(String key, Class<T> classOfT) {
        String json = sharedPreferences.getString(key, null);
        return gson.fromJson(json, classOfT);
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }
    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
    public void remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(key);
        editor.apply();
    }

}
