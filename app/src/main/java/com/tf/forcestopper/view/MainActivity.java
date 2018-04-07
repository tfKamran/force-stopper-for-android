package com.tf.forcestopper.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.tf.forcestopper.R;
import com.tf.forcestopper.model.ApplicationItem;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadSplashFragment();
    }

    private void loadSplashFragment() {
        final SplashFragment fragment = new SplashFragment();
        fragment.setOnAppsLoadedListener(new SplashFragment.OnAppsLoadedListener() {
            @Override
            public void onAppsLoaded(List<ApplicationItem> installedApplications) {
                loadMainFragment(installedApplications);
            }
        });

        loadFragment(fragment);
    }

    private void loadMainFragment(List<ApplicationItem> installedApplications) {
        final MainFragment fragment = new MainFragment();
        fragment.setInstalledApplications(installedApplications);

        loadFragment(fragment);
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_holder, fragment)
                .commit();
    }
}
