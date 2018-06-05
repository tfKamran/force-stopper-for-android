package com.tf.forcestopper.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.tf.forcestopper.R;
import com.tf.forcestopper.model.ApplicationItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ApplicationsHelper {

    private final Context mContext;
    private final PackageManager mPackageManager;

    public ApplicationsHelper(Context context) {
        mContext = context;
        mPackageManager = context.getPackageManager();
    }

    public List<ApplicationItem> getInstalledApplicationItems() {
        final List<PackageInfo> installedPackages = mPackageManager.getInstalledPackages(PackageManager.GET_META_DATA);
        final List<ApplicationItem> mInstalledApplications = new ArrayList<>();

        for (PackageInfo installedPackage : installedPackages) {
            if ((installedPackage.applicationInfo.flags
                    & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0) {
                mInstalledApplications.add(new ApplicationItem(installedPackage.packageName,
                        getApplicationIcon(installedPackage.packageName),
                        getApplicationLabel(installedPackage.packageName)));
            }
        }

        mInstalledApplications.remove(new ApplicationItem(mContext.getPackageName()));

        Collections.sort(mInstalledApplications, new Comparator<ApplicationItem>() {
            @Override
            public int compare(ApplicationItem o1, ApplicationItem o2) {
                return o1.label.compareTo(o2.label);
            }
        });

        return mInstalledApplications;
    }

    public Drawable getApplicationIcon(String packageName) {
        Drawable applicationIcon;

        try {
            applicationIcon = mPackageManager.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

            applicationIcon = mContext.getResources().getDrawable(R.drawable.ic_launcher_foreground);
        }

        return applicationIcon;
    }

    public String getApplicationLabel(String packageName) {
        String applicationLabel = "Error";

        try {
            applicationLabel = getApplicationLabel(mPackageManager.getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return applicationLabel;
    }

    public String getApplicationLabel(ApplicationInfo applicationInfo) {
        return mPackageManager.getApplicationLabel(applicationInfo).toString();
    }
}
