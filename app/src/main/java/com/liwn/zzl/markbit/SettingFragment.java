package com.liwn.zzl.markbit;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by zzl on 16/7/4.
 */
public class SettingFragment extends Fragment {

    private static final String TAG = SettingFragment.class.getSimpleName();
    private TextView tv_version;
    private TextView tv_all_samples_num_show;
    private TextView tv_A_samples_num_show;
//    private TextView tv_B_samples_num_show;
    private SeekBar sb_all_samples_num;
    private SeekBar sb_A_samples_num;
//    private SeekBar sb_B_samples_num;
    private TextView tv_brightness_show;
    private TextView tv_A_show_time_num_show;
    private TextView tv_B_show_time_num_show;
    private SeekBar sb_brightness;
    private SeekBar sb_A_show_time;
    private SeekBar sb_B_show_time;
    private Spinner sp_battery_types;
    private Switch st_magnetic;
    private Switch st_low_voltage;
    private Switch st_dump;
    private final static int MIN_ALL_SAMPLE_NUM = 30;

    private OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    private static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString().toUpperCase();
    }

    private void initUI(View view) {
        tv_version = (TextView) view.findViewById(R.id.version_num);
        sb_all_samples_num = (SeekBar) view.findViewById(R.id.all_samples_num);
        sb_A_samples_num = (SeekBar) view.findViewById(R.id.A_samples_num);
//        sb_B_samples_num = (SeekBar) view.findViewById(R.id.B_samples_num);

        tv_all_samples_num_show = (TextView) view.findViewById(R.id.all_samples_num_show);
        tv_A_samples_num_show = (TextView) view.findViewById(R.id.A_samples_num_show);
//        tv_B_samples_num_show = (TextView) view.findViewById(R.id.B_samples_num_show);


        int version_len = 4;
        byte[] version = new byte[version_len];
        FileIO.getBytes(MarkBitApplication.i_file, version, FileIO.VERSION_ADDR, FileIO.VERSION_LEN);
        tv_version.setText(bytesToHexString(version));

        int all_samples_num = FileIO.getByte(MarkBitApplication.i_file, FileIO.ALL_SAMPLE_NUM_ADDR) & 0xff;
        int A_samples_num = FileIO.getByte(MarkBitApplication.i_file, FileIO.A_SAMPLE_NUM_ADDR) & 0xff;
//        int B_samples_num = FileIO.getByte(MarkBitApplication.i_file, FileIO.B_SAMPLE_NUM_ADDR) & 0xff;
        sb_all_samples_num.setProgress(all_samples_num);
        sb_A_samples_num.setProgress(A_samples_num);
//        sb_B_samples_num.setProgress(B_samples_num);
        tv_all_samples_num_show.setText(String.valueOf(all_samples_num));
        tv_A_samples_num_show.setText(String.valueOf(A_samples_num));
//        tv_B_samples_num_show.setText(String.valueOf(B_samples_num));

        sb_brightness = (SeekBar) view.findViewById(R.id.brightness_adjust);
        sb_A_show_time= (SeekBar) view.findViewById(R.id.A_show_time);
        sb_B_show_time = (SeekBar) view.findViewById(R.id.B_show_time);

        sp_battery_types = (Spinner) view.findViewById(R.id.battery_type);

        tv_brightness_show = (TextView) view.findViewById(R.id.brightness_num_show);
        tv_A_show_time_num_show = (TextView) view.findViewById(R.id.A_show_time_num_show);
        tv_B_show_time_num_show = (TextView) view.findViewById(R.id.B_show_time_num_show);

        int brightness = FileIO.getByte(MarkBitApplication.i_file, FileIO.BRIGHTNESS_ADDR) & 0xff;
        int A_show_time = FileIO.getByte(MarkBitApplication.i_file, FileIO.A_SHOW_TIME_ADDR) & 0xff;
        int B_show_time = FileIO.getByte(MarkBitApplication.i_file, FileIO.B_SHOW_TIME_ADDR) & 0xff;
        int battery_types = FileIO.getByte(MarkBitApplication.i_file, FileIO.BATTERY_TYPE_ADDR) & 0xff;
        sb_brightness.setProgress(brightness);
        sb_A_show_time.setProgress(A_show_time);
        sb_B_show_time.setProgress(B_show_time);
        sp_battery_types.setSelection(battery_types);
        tv_brightness_show.setText(String.valueOf(brightness));
        tv_A_show_time_num_show.setText(String.valueOf((float) A_show_time / 10) + getString(R.string.time_unit));
        tv_B_show_time_num_show.setText(String.valueOf((float) B_show_time / 10) + getString(R.string.time_unit));

        st_magnetic = (Switch) view.findViewById(R.id.magnetic_switch);
        st_low_voltage = (Switch) view.findViewById(R.id.low_voltage_switch);
        st_dump = (Switch) view.findViewById(R.id.dump_switch);

        byte is_magnetic = FileIO.getByte(MarkBitApplication.i_file, FileIO.IS_MAGNET_ADDR);
        byte is_low_voltage = FileIO.getByte(MarkBitApplication.i_file, FileIO.IS_LOW_VOLTAGE_ADDR);
        byte is_dump = FileIO.getByte(MarkBitApplication.i_file, FileIO.IS_DUMP_ADDR);
        st_magnetic.setChecked(is_magnetic == 0x01 ? true : false);
        st_low_voltage.setChecked(is_low_voltage == 0x01 ? true : false);
        st_dump.setChecked(is_dump == 0x01 ? true : false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        initUI(view);

        sb_all_samples_num.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < MIN_ALL_SAMPLE_NUM) {
                    seekBar.setProgress(MIN_ALL_SAMPLE_NUM);
                    tv_all_samples_num_show.setText(String.valueOf(MIN_ALL_SAMPLE_NUM));
                } else {
                    tv_all_samples_num_show.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int read_byte_num = 1;

                byte[] all_samples = new byte[read_byte_num];
                all_samples[0] = (byte) seekBar.getProgress();
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.ALL_SAMPLE_NUM_ADDR, read_byte_num, all_samples);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.ALL_SAMPLE_NUM_ADDR, read_byte_num, all_samples);

                MarkBitApplication.i_synced = false;
                MarkBitApplication.r_synced = false;
                mListener.updateNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
                mListener.updateAllMark(seekBar.getProgress());
            }
        });

        sb_A_samples_num.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) {
                    seekBar.setProgress(1);
                    tv_A_samples_num_show.setText(String.valueOf(1));
                } else {
                    tv_A_samples_num_show.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int read_byte_num = 1;

                byte[] A_samples = new byte[read_byte_num];
                A_samples[0] = (byte) seekBar.getProgress();
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.A_SAMPLE_NUM_ADDR, read_byte_num, A_samples);
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.B_SAMPLE_NUM_ADDR, read_byte_num, A_samples);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.A_SAMPLE_NUM_ADDR, read_byte_num, A_samples);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.B_SAMPLE_NUM_ADDR, read_byte_num, A_samples);

                MarkBitApplication.i_synced = false;
                MarkBitApplication.r_synced = false;
                mListener.updateNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
                mListener.updateIndexMark(seekBar.getProgress());
            }
        });

//        sb_B_samples_num.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                tv_B_samples_num_show.setText(String.valueOf(progress));
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                int read_byte_num = 1;
//
//                byte[] B_samples = new byte[read_byte_num];
//                B_samples[0] = (byte) seekBar.getProgress();
//                FileIO.setBytes(MarkBitApplication.i_file, FileIO.B_SAMPLE_NUM_ADDR, read_byte_num, B_samples);
//                FileIO.setBytes(MarkBitApplication.r_file, FileIO.B_SAMPLE_NUM_ADDR, read_byte_num, B_samples);
//                    MarkBitApplication.i_synced = false;
//                    MarkBitApplication.r_synced = false;
//                  mListener.updateNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
//            }
//        });



        sb_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                tv_brightness_show.setText(String.valueOf(progress));
                if (progress < 1) {
                    seekBar.setProgress(1);
                    tv_brightness_show.setText(String.valueOf(1));
                } else {
                    tv_brightness_show.setText(String.valueOf(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int read_byte_num = 1;

                byte[] new_brightness = new byte[read_byte_num];
                new_brightness[0] = (byte) seekBar.getProgress();
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.BRIGHTNESS_ADDR, read_byte_num, new_brightness);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.BRIGHTNESS_ADDR, read_byte_num, new_brightness);
                MarkBitApplication.i_synced = false;
                MarkBitApplication.r_synced = false;
                mListener.updateNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
            }
        });

        sb_A_show_time.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) {
                    seekBar.setProgress(1);
                    tv_A_show_time_num_show.setText(String.valueOf(0.1) + getString(R.string.time_unit));
                } else {
                    tv_A_show_time_num_show.setText(String.valueOf((float) progress / 10) + getString(R.string.time_unit));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int read_byte_num = 1;

                byte[] showTime = new byte[read_byte_num];
                showTime[0] = (byte) seekBar.getProgress();
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.A_SHOW_TIME_ADDR, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.B_HIDE_TIME_ADDR, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.A_SHOW_TIME_ADDR, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.B_HIDE_TIME_ADDR, read_byte_num, showTime);
                MarkBitApplication.i_synced = false;
                MarkBitApplication.r_synced = false;
                mListener.updateNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
            }
        });

        sb_B_show_time.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 1) {
                    seekBar.setProgress(1);
                    tv_B_show_time_num_show.setText(String.valueOf(0.1) + getString(R.string.time_unit));
                } else {
                    tv_B_show_time_num_show.setText(String.valueOf((float) progress / 10) + getString(R.string.time_unit));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int read_byte_num = 1;

                byte[] showTime = new byte[read_byte_num];
                showTime[0] = (byte) seekBar.getProgress();
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.A_HIDE_TIME_ADDR, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.B_SHOW_TIME_ADDR, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.A_HIDE_TIME_ADDR, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.B_SHOW_TIME_ADDR, read_byte_num, showTime);

                MarkBitApplication.i_synced = false;
                MarkBitApplication.r_synced = false;
                mListener.updateNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
            }
        });

        ArrayAdapter<String> a = new ArrayAdapter<String>(this.getContext(), R.layout.spinner_battery_dropdown_item, getResources().getStringArray(R.array.battery_types));
        a.setDropDownViewResource(R.layout.spinner_battery_dropdown_item);
        sp_battery_types.setAdapter(a);

        byte battery_type = FileIO.getByte(MarkBitApplication.i_file, FileIO.BATTERY_TYPE_ADDR);
        sp_battery_types.setSelection(battery_type & 0xff, true);
        sp_battery_types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int read_byte_num = 1;
                byte[] type = new byte[read_byte_num];
                type[0] = (byte) position;
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.BATTERY_TYPE_ADDR, read_byte_num, type);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.BATTERY_TYPE_ADDR, read_byte_num, type);
                MarkBitApplication.i_synced = false;
                MarkBitApplication.r_synced = false;
                mListener.updateNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        st_magnetic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int flag = 0;
                if (isChecked) {
                    flag = 1;
                } else {
                    flag = 0;
                }

                int read_byte_num = 1;
                byte[] set = new byte[read_byte_num];
                set[0] = (byte) flag;
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.IS_MAGNET_ADDR, read_byte_num, set);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.IS_MAGNET_ADDR, read_byte_num, set);
                MarkBitApplication.i_synced = false;
                MarkBitApplication.r_synced = false;
                mListener.updateNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
            }
        });

        st_low_voltage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int flag = 0;
                if (isChecked) {
                    flag = 1;
                } else {
                    flag = 0;
                }

                int read_byte_num = 1;
                byte[] set = new byte[read_byte_num];
                set[0] = (byte) flag;
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.IS_LOW_VOLTAGE_ADDR, read_byte_num, set);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.IS_LOW_VOLTAGE_ADDR, read_byte_num, set);
                MarkBitApplication.i_synced = false;
                MarkBitApplication.r_synced = false;
                mListener.updateNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
            }
        });

        st_dump.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                int flag = 0;
                if (isChecked) {
                    flag = 1;
                } else {
                    flag = 0;
                }

                int read_byte_num = 1;
                byte[] set = new byte[read_byte_num];
                set[0] = (byte) flag;
                FileIO.setBytes(MarkBitApplication.i_file, FileIO.IS_DUMP_ADDR, read_byte_num, set);
                FileIO.setBytes(MarkBitApplication.r_file, FileIO.IS_DUMP_ADDR, read_byte_num, set);
                MarkBitApplication.i_synced = false;
                MarkBitApplication.r_synced = false;
                mListener.updateNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
            }
        });

        return view;
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

    public interface OnFragmentInteractionListener {
        void updateIndexMark(int num);
        void updateAllMark(int num);
        void updateNotification(boolean i_synced, boolean r_synced);
    }
}
