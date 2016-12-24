package com.liwn.zzl.markbit;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;


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
    private static final int REQUEST_CODE_CHOOSE_FILE = 11;
    private static final String ISLOGIN = "isLogin";

    private String mParam1;
    private View mView;

    private OnFragmentInteractionListener mListener;

    private Context activityContext = null;
    private static final String TAG = SendFileFragment.class.getSimpleName();
    private ProgressDialog dialog;

    private Button btFileSend;
    private Button btUpdateSetting;
    private Button btLogout;
    private boolean isBTConnected;

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
        if (getArguments() != null) {
            mParam1 = String.valueOf(getArguments().getInt(ARG_COLUMN_COUNT));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_sendfile, container, false);

        btFileSend = (Button) mView.findViewById(R.id.bt_file_send);
        btUpdateSetting = (Button) mView.findViewById(R.id.bt_setting_send);
        btLogout = (Button) mView.findViewById(R.id.bt_logout);

        btFileSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBTConnected) {
                    // TODO: need test
                    // get bluetooth name
                    File file;
                    if (MarkBitApplication.connectedDeviceName == getString(R.string.I_device_name)) {
                        file = FileIO.getIconFile();
                    } else if (MarkBitApplication.connectedDeviceName == getString(R.string.R_device_name)) {
                        file = FileIO.getRconFile();
                    } else {
                        Toast.makeText(activityContext, "Device name is not " +
                                getString(R.string.I_device_name) + " or " + getString(R.string.R_device_name), Toast.LENGTH_SHORT).show();
                        file = null;
                        return;
                    }

                    if (file != null) {
                        Uri updateUri = Uri.fromFile(file);
                        mListener.sendFileFromUriByBT(updateUri, MarkBitApplication.UPDATE_TYPE_LIBRARY);
                    } else {
                        Toast.makeText(activityContext, "Library files is not existed, please import bin files in MarkBit/", Toast.LENGTH_SHORT).show();
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
                    File file = FileIO.getIconFile();
                    if (file != null) {
                        Uri setUri = Uri.fromFile(file);
                        mListener.sendFileFromUriByBT(setUri, MarkBitApplication.UPDATE_TYPE_SETTING);
                    } else {
                        Toast.makeText(activityContext, "Library files is not existed.", Toast.LENGTH_SHORT).show();
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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CHOOSE_FILE:
                    Uri uri = data.getData();
                    File src = new File(uri.getPath());
                    String filename = src.getName();
                    // TODO: validate file name
                    // 1. is validation
                    // 2. is R file or I file
                    if (filename.equals(MarkBitApplication.i_name) || filename.equals(MarkBitApplication.r_name)) {
                        File dst = new File(FileIO.getMediaFolderName() + "/" + filename);

                        if (!FileIO.isSameFile(src, dst)) {

                            byte[] setting_backup = new byte[MarkBitApplication.MARK_SETTING_SIZE];
                            boolean isDstExisted = false;
                            if (dst.exists()) {
                                isDstExisted = true;
                                FileIO.getBytes(dst, setting_backup, 0, MarkBitApplication.MARK_SETTING_SIZE);
                                dst.delete();
                            }
                            try {
                                FileIO.copyFile(src, dst);
                                if (isDstExisted) {
                                    FileIO.setBytes(dst, 0, MarkBitApplication.MARK_SETTING_SIZE, setting_backup);
                                }
                                Uri sendUri = Uri.fromFile(dst);
                                mListener.sendFileFromUriByBT(sendUri, MarkBitApplication.UPDATE_TYPE_LIBRARY);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            mListener.sendFileFromUriByBT(uri, MarkBitApplication.UPDATE_TYPE_LIBRARY);
                        }
                    } else {
                        Toast.makeText(activityContext, "Please import " + MarkBitApplication.i_name + " or " + MarkBitApplication.r_name, Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
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


}
