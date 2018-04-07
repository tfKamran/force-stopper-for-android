package com.tf.forcestopper.model;


import android.graphics.drawable.Drawable;

public class ApplicationItem {

    public final String packageName;
    public final Drawable icon;
    public final String label;

    public ApplicationItem(String packageName, Drawable icon, String label) {
        this.packageName = packageName;
        this.icon = icon;
        this.label = label;
    }

    public ApplicationItem(String packageName) {
        this.packageName = packageName;
        icon = null;
        label = "";
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null
                && obj instanceof ApplicationItem
                && ((packageName != null && packageName.equals(((ApplicationItem) obj).packageName))
                || ((ApplicationItem) obj).packageName == null);
    }
}
