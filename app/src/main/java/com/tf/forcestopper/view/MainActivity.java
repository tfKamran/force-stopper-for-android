package com.tf.forcestopper.view;

import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.tf.forcestopper.R;
import com.tf.forcestopper.util.Preferences;
import com.tf.forcestopper.util.ShellScriptExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView listIgnoredApplications;

    private PackageManager mPackageManager;
    private Preferences mPreferences;

    private List<ApplicationItem> mInstalledApplications = new ArrayList<>();
    private List<String> mIgnoredList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPackageManager = getPackageManager();
        mPreferences = new Preferences(getApplicationContext());

        View btnForceStopApps = findViewById(R.id.btn_force_stop_apps);
        btnForceStopApps.setOnClickListener(btnStopOnClickListener);

        listIgnoredApplications = findViewById(R.id.list_ignored_applications);
        listIgnoredApplications.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        loadIgnoreList();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mIgnoredList = mPreferences.getIgnoreList();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mPreferences.setIgnoredList(mIgnoredList);
    }

    private void loadIgnoreList() {
        new ApplicationListTask().execute();
    }

    private View.OnClickListener btnStopOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new ForceStopTask().execute();
        }
    };

    private class ApplicationListTask extends AsyncTask<Void, Void, List<ApplicationItem>> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = ProgressDialog.show(MainActivity.this, "Loading Apps",
                    "Please wait...", true, false);
        }

        @Override
        protected List<ApplicationItem> doInBackground(Void... voids) {
            final List<PackageInfo> installedPackages = mPackageManager.getInstalledPackages(PackageManager.GET_META_DATA);
            for (PackageInfo installedPackage : installedPackages) {
                if ((installedPackage.applicationInfo.flags
                        & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0) {
                    mInstalledApplications.add(new ApplicationItem(installedPackage));
                }
            }

            mInstalledApplications.remove(new ApplicationItem(getPackageName()));

            Collections.sort(mInstalledApplications, new Comparator<ApplicationItem>() {
                @Override
                public int compare(ApplicationItem o1, ApplicationItem o2) {
                    return o1.label.compareTo(o2.label);
                }
            });

            return mInstalledApplications;
        }

        @Override
        protected void onPostExecute(List<ApplicationItem> packageInfoList) {
            super.onPostExecute(packageInfoList);

            listIgnoredApplications.setAdapter(new ApplicationAdapter());

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }

    private class ForceStopTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = ProgressDialog.show(MainActivity.this, "Stopping Apps",
                    "Please wait...", true, false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            long startTime = System.currentTimeMillis();

            for (ApplicationItem applicationItem : mInstalledApplications) {
                try {
                    if (!mIgnoredList.contains(applicationItem.packageName)) {
                        ShellScriptExecutor.executeShell("am force-stop --user current " + applicationItem.packageName);
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

            Log.d("---", "force stopped " + mInstalledApplications.size() + " in " + (System.currentTimeMillis() - startTime));

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
            }
        }
    }

    private class ApplicationItem {

        public final String packageName;
        public final Drawable icon;
        public final String label;

        ApplicationItem(PackageInfo installedPackage) {
            packageName = installedPackage.packageName;
            icon = getApplicationIcon(packageName);
            label = getApplicationLabel(installedPackage.applicationInfo);
        }

        ApplicationItem(String packageName) {
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
    }

    private class ApplicationAdapter extends RecyclerView.Adapter<ApplicationViewHolder> {
        @Override
        public ApplicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ApplicationViewHolder(LayoutInflater.from(MainActivity.this)
                    .inflate(R.layout.layout_application_item, listIgnoredApplications, false));
        }

        @Override
        public void onBindViewHolder(ApplicationViewHolder holder, int position) {
            final ApplicationItem applicationItem = mInstalledApplications.get(position);

            holder.imgIcon.setImageDrawable(applicationItem.icon);
            holder.lblLine1.setText(applicationItem.label);
            holder.lblLine2.setText(applicationItem.packageName);

            holder.chkSelected.setChecked(mIgnoredList.contains(mInstalledApplications
                    .get(position).packageName));

        }

        @Override
        public int getItemCount() {
            return mInstalledApplications.size();
        }
    }

    private class ApplicationViewHolder extends RecyclerView.ViewHolder
            implements CompoundButton.OnCheckedChangeListener {

        private final CheckBox chkSelected;
        private final ImageView imgIcon;
        private final TextView lblLine1;
        private final TextView lblLine2;

        ApplicationViewHolder(View itemView) {
            super(itemView);

            chkSelected = itemView.findViewById(R.id.chk_selected);
            imgIcon = itemView.findViewById(R.id.img_icon);
            lblLine1 = itemView.findViewById(R.id.lbl_line_1);
            lblLine2 = itemView.findViewById(R.id.lbl_line_2);

            chkSelected.setOnCheckedChangeListener(this);
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                mIgnoredList.add(mInstalledApplications.get(getAdapterPosition()).packageName);
            } else {
                mIgnoredList.remove(mInstalledApplications.get(getAdapterPosition()).packageName);
            }
        }
    }
}
