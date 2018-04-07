package com.tf.forcestopper.view;

import android.app.ProgressDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.tf.forcestopper.model.ApplicationItem;
import com.tf.forcestopper.util.Preferences;
import com.tf.forcestopper.util.ShellScriptExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainFragment extends Fragment {

    private RecyclerView listIgnoredApplications;

    private PackageManager mPackageManager;
    private Preferences mPreferences;

    private List<ApplicationItem> mInstalledApplications = new ArrayList<>();
    private List<String> mIgnoredList = new ArrayList<>();
    private ApplicationAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return LayoutInflater.from(getActivity()).inflate(R.layout.fragment_main, null, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPackageManager = getActivity().getPackageManager();
        mPreferences = new Preferences(getActivity().getApplicationContext());

        View btnForceStopApps = view.findViewById(R.id.btn_force_stop_apps);
        btnForceStopApps.setOnClickListener(btnStopOnClickListener);

        listIgnoredApplications = view.findViewById(R.id.list_ignored_applications);
        listIgnoredApplications.setLayoutManager(new LinearLayoutManager(getActivity()));
        mAdapter = new ApplicationAdapter();
        listIgnoredApplications.setAdapter(mAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        mIgnoredList = mPreferences.getIgnoreList();
    }

    @Override
    public void onStop() {
        super.onStop();

        mPreferences.setIgnoredList(mIgnoredList);
    }

    private View.OnClickListener btnStopOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new ForceStopTask().execute();
        }
    };

    public void setInstalledApplications(List<ApplicationItem> installedApplications) {
        this.mInstalledApplications = installedApplications;

        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    private class ForceStopTask extends AsyncTask<Void, Void, Void> {

        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            mProgressDialog = ProgressDialog.show(getActivity(), "Stopping Apps",
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

    private class ApplicationAdapter extends RecyclerView.Adapter<ApplicationViewHolder> {
        @Override
        public ApplicationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ApplicationViewHolder(LayoutInflater.from(getActivity())
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
