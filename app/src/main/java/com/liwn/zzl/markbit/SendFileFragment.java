package com.liwn.zzl.markbit;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.liwn.zzl.markbit.bins.BinsDownloadUrl;
import com.liwn.zzl.markbit.bins.IRPairs;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SendFileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SendFileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SendFileFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    public static final String ARG_COLUMN_COUNT = "column-count";
    private static final String ISLOGIN = "isLogin";

    private View mView;

    private OnFragmentInteractionListener mListener;

    private Context activityContext = null;
    private static final String TAG = SendFileFragment.class.getSimpleName();
    private ProgressDialog dialog;
    private ProgressDialog mDownloadProgress;

    private Button btFileSend;
    private Button btDownload;
    private Button btUpdateSetting;
    private Button btLogout;
    private boolean isBTConnected;
    private DownloadTask downloadTask;
    private int userTypeCnt = 100;
    private String[] userTypes = new String[userTypeCnt + 1];

    public void enableBT() {
        isBTConnected = true;
    }

    public void disableBT() {
        isBTConnected = false;
    }

    public SendFileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param columnCount Parameter 1.
     * @return A new instance of fragment SendFileFragment.
     */
    public static SendFileFragment newInstance(int columnCount) {
        SendFileFragment fragment = new SendFileFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private boolean compareDeviceName(String s1, String s2) {
        s1 = s1.replaceAll("-", "");
        s1 = s1.replaceAll("_", "");
        s2 = s2.replaceAll("-", "");
        s2 = s2.replaceAll("_", "");
        return s1.equals(s2);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_sendfile, container, false);

        setUserTypes();
        btFileSend = (Button) mView.findViewById(R.id.bt_file_send);
        btDownload = (Button) mView.findViewById(R.id.bt_file_download);
        btUpdateSetting = (Button) mView.findViewById(R.id.bt_setting_send);
        btLogout = (Button) mView.findViewById(R.id.bt_logout);

        btFileSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBTConnected) {
                    // get bluetooth name
                    File file;
                    if (compareDeviceName(MarkBitApplication.connectedDeviceName, getString(R.string.I_device_name))) {
                        file = FileIO.getIconFile();
                    } else if (compareDeviceName(MarkBitApplication.connectedDeviceName, getString(R.string.R_device_name))) {
                        file = FileIO.getRconFile();
                    } else {
                        Toast.makeText(activityContext, getString(R.string.allowed_ble_device_name) + " " +
                                getString(R.string.I_device_name) + " " + getString(R.string.or) + " " + getString(R.string.R_device_name),
                                Toast.LENGTH_SHORT).show();
                        file = null;
                        return;
                    }

                    if (file != null) {
                        Uri updateUri = Uri.fromFile(file);
                        mListener.sendFileFromUriByBT(updateUri, MarkBitApplication.UPDATE_TYPE_LIBRARY);
                    } else {
                        Log.e(TAG, "file is null");
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                }
            }
        });
        btUpdateSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBTConnected) {
                    // TODO: send which file acording to the device name
                    File file;
                    if (compareDeviceName(MarkBitApplication.connectedDeviceName, getString(R.string.I_device_name))) {
                        file = FileIO.getIconFile();
                    } else if (compareDeviceName(MarkBitApplication.connectedDeviceName, getString(R.string.R_device_name))) {
                        file = FileIO.getRconFile();
                    } else {
                        Toast.makeText(activityContext, getString(R.string.allowed_ble_device_name) + " " +
                                        getString(R.string.I_device_name) + " " + getString(R.string.or) + " " + getString(R.string.R_device_name),
                                Toast.LENGTH_SHORT).show();
                        file = null;
                        return;
                    }
                    if (file != null) {
                        Uri setUri = Uri.fromFile(file);
                        mListener.sendFileFromUriByBT(setUri, MarkBitApplication.UPDATE_TYPE_SETTING);
                    } else {
                        Toast.makeText(activityContext, R.string.lib_not_found, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
                }
            }
        });
        btLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                        PreferenceManager.getDefaultSharedPreferences(getContext()).edit().putBoolean(ISLOGIN, false).apply();
                        Intent loginIntent = new Intent(getContext(), LoginActivity.class);
                        startActivity(loginIntent);
                        getActivity().finish();
            }
        });

        return mView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        activityContext = getContext();
    }

    @Override
    public void onStart() {
        super.onStart();
        downloadTask = new DownloadTask(activityContext, 0);
        mDownloadProgress = new ProgressDialog(activityContext);
        mDownloadProgress.setMessage(getString(R.string.downloading));
        mDownloadProgress.setIndeterminate(true);
        mDownloadProgress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mDownloadProgress.setCancelable(false);
        mDownloadProgress.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                downloadTask.cancel(true);
            }
        });

        btDownload.setOnClickListener(new AlertClickListener());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void initProgressBar(int max) {
        dialog = new ProgressDialog(activityContext);
        dialog.setTitle(R.string.file_sending_title);
//        dialog.setMessage(getString(R.string.file_sending_msg));
        dialog.setProgress(0);
        dialog.setMax(max);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                mListener.cancleFileSend();
            }
        });
        dialog.show();
    }

    public void setProgressBarNum(int num) {
        if (dialog.getProgress() < dialog.getMax()) {
            if (num  > dialog.getMax()) {
                dialog.setProgress(dialog.getMax());
            } else {
                dialog.setProgress(num);
            }
        }
    }

    public void destroyProgressBar() {
        dialog.setProgress(dialog.getMax());
        dialog.dismiss();
    }

    private void setUserTypes() {
        userTypes[0] = getString(R.string.zhtk);
        userTypes[1] = getString(R.string.gaxf);
        userTypes[2] = getString(R.string.gajj);
        userTypes[3] = getString(R.string.jtyz);

        for (int i = 4; i <= userTypeCnt; ++i) {
            userTypes[i] = String.valueOf(i);
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void sendFileFromUriByBT(Uri uri, String type);
        void cancleFileSend();
    }

    private class AlertClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            new AlertDialog.Builder(activityContext).setTitle(R.string.library_type).setItems(userTypes, new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                    if (isOnline()) {
                        final DownloadTask downloadTask = new DownloadTask(activityContext, which);
                        downloadTask.execute();
                    } else {
                        Toast.makeText(activityContext, R.string.net_hit, Toast.LENGTH_LONG).show();
                    }
                }
            }).show();
        }
    }

    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private int userType;
        private PowerManager.WakeLock mWakeLock;
        private String flagI = "icon100.bin";
        private String flagR = "rcon100.bin";
        private boolean isGetI = false;
        private boolean isGetR = false;
        private Map<String, String> urls = new HashMap<>();

        public DownloadTask(Context context, int userType) {
            this.context = context;
            this.userType = userType;
        }

        private String download(int index, String http_url) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(http_url);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                String[] http_url_infos = http_url.split("/");
                String filename = http_url_infos[http_url_infos.length-1];
                output = new FileOutputStream(FileIO.getMediaFolderName() + "/" + filename);

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (index == 0 && fileLength > 0) {
                        publishProgress((int) (total * 50 / fileLength));
                    } else if (index == 1 && fileLength > 0) {
                        publishProgress((int) (50 + total * 100 / fileLength));
                    }

                    output.write(data, 0, count);
                }
                return "true";
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mDownloadProgress.show();
            mDownloadProgress.setProgress(0);
        }

        @Override
        protected String doInBackground(String... sUrl) {
            // 0,1,2,3 : 4
            if (this.userType < BinsDownloadUrl.downloadUrls.size()) {
                IRPairs pairs = BinsDownloadUrl.downloadUrls.get(this.userType);
                String res1 = download(0, pairs.getiUrl());
                String res2 = download(1, pairs.getrUrl());
                Log.e(TAG, String.format("Download result: %s", res1));
                Log.e(TAG, String.format("Download result: %s", res2));
                if ("true".equals(res1) && "true".equals(res2)) {
                    return "true";
                }
                return null;
            } else {
                Log.e(TAG, "Download failed cause the bin type is undefined.");
                return "false";
            }
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mDownloadProgress.setIndeterminate(false);
            mDownloadProgress.setMax(100);
            mDownloadProgress.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mDownloadProgress.dismiss();
            if ("true".equals(result))
                Toast.makeText(context, R.string.downlaod_success, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context, R.string.download_failed, Toast.LENGTH_LONG).show();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) activityContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
