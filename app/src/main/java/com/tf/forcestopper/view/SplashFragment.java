package com.tf.forcestopper.view;

import android.content.pm.PackageManager;
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
import com.tf.forcestopper.util.ApplicationsHelper;

import java.util.ArrayList;
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
            return new ApplicationsHelper(getActivity()).getInstalledApplicationItems();
        }

        @Override
        protected void onPostExecute(List<ApplicationItem> applicationItems) {
            super.onPostExecute(applicationItems);

            if (mOnAppsLoadedListener != null) {
                mOnAppsLoadedListener.onAppsLoaded(mInstalledApplications);
            }
        }
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
