package com.tf.forcestopper.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Preferences {

    private static final String EXCEPTION_LIST = "exceptionList";

    private SharedPreferences sharedPreferences;

    public Preferences(Context context) {
        sharedPreferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public void setIgnoredList(List<String> packages) {
        StringBuilder packagesCsv = new StringBuilder();

        for (String aPackage : packages) {
            packagesCsv.append(aPackage).append(",");
        }

        if (packagesCsv.length() > 1) {
            packagesCsv.deleteCharAt(packagesCsv.length() - 1);
        }

        sharedPreferences.edit().putString(EXCEPTION_LIST, packagesCsv.toString()).apply();
    }

    public List<String> getIgnoreList() {
        return new ArrayList<>(Arrays.asList(sharedPreferences.getString(EXCEPTION_LIST, "").split(",")));
    }
}
