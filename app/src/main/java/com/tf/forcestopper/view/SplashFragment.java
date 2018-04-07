package com.tf.forcestopper.view;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.tf.forcestopper.R;
import com.tf.forcestopper.model.ApplicationItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SplashFragment extends Fragment {

    private View layoutHeader;
    private View layoutProgress;

    private PackageManager mPackageManager;

    private OnAppsLoadedListener mOnAppsLoadedListener;
    private List<ApplicationItem> mInstalledApplications = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_splash, null, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        layoutHeader = view.findViewById(R.id.layout_header);
        layoutProgress = view.findViewById(R.id.layout_progress);

        animateHeader();

        mPackageManager = getActivity().getPackageManager();

        loadInstalledApplications();
    }

    private void animateHeader() {
        final Animation headerAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.splash_header_fade_in);
        headerAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutHeader.setVisibility(View.VISIBLE);

                animateProgress();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        layoutHeader.startAnimation(headerAnimation);
    }

    private void animateProgress() {
        final Animation progressAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.splash_progress_fade_in);
        progressAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                layoutProgress.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        layoutProgress.startAnimation(progressAnimation);
    }

    private void loadInstalledApplications() {
        new ApplicationListTask().execute();
    }

    private class ApplicationListTask extends AsyncTask<Void, Void, List<ApplicationItem>> {

        @Override
        protected List<ApplicationItem> doInBackground(Void... voids) {
            final List<PackageInfo> installedPackages = mPackageManager.getInstalledPackages(PackageManager.GET_META_DATA);
            for (PackageInfo installedPackage : installedPackages) {
                if ((installedPackage.applicationInfo.flags
                        & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0) {
                    mInstalledApplications.add(new ApplicationItem(installedPackage.packageName,
                            getApplicationIcon(installedPackage.packageName),
                            getApplicationLabel(installedPackage.packageName)));
                }
            }

            mInstalledApplications.remove(new ApplicationItem(getActivity().getPackageName()));

            Collections.sort(mInstalledApplications, new Comparator<ApplicationItem>() {
                @Override
                public int compare(ApplicationItem o1, ApplicationItem o2) {
                    return o1.label.compareTo(o2.label);
                }
            });

            return mInstalledApplications;
        }

        @Override
        protected void onPostExecute(List<ApplicationItem> applicationItems) {
            super.onPostExecute(applicationItems);

            if (mOnAppsLoadedListener != null) {
                mOnAppsLoadedListener.onAppsLoaded(mInstalledApplications);
            }
        }
    }

    private Drawable getApplicationIcon(String packageName) {
        Drawable applicationIcon;

        try {
            applicationIcon = mPackageManager.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

            applicationIcon = getResources().getDrawable(R.drawable.ic_launcher_foreground);
        }

        return applicationIcon;
    }

    private String getApplicationLabel(String packageName) {
        String applicationLabel = "Error";

        try {
            applicationLabel = getApplicationLabel(mPackageManager.getApplicationInfo(packageName,
                    PackageManager.GET_META_DATA));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return applicationLabel;
    }

    private String getApplicationLabel(ApplicationInfo applicationInfo) {
        return mPackageManager.getApplicationLabel(applicationInfo).toString();
    }

    public OnAppsLoadedListener getOnAppsLoadedListener() {
        return mOnAppsLoadedListener;
    }

    public void setOnAppsLoadedListener(OnAppsLoadedListener onAppsLoadedListener) {
        this.mOnAppsLoadedListener = onAppsLoadedListener;
    }

    public interface OnAppsLoadedListener {
        void onAppsLoaded(List<ApplicationItem> installedApplications);
    }
}
