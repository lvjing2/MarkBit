package com.liwn.zzl.markbit;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by zzl on 16/7/4.
 */
public class SettingFragment extends Fragment {

    private static final String TAG = SettingFragment.class.getSimpleName();
    private TextView tv_version;
    private TextView tv_all_samples_num_show;
    private TextView tv_A_samples_num_show;
    private TextView tv_B_samples_num_show;
    private SeekBar sb_all_samples_num;
    private SeekBar sb_A_samples_num;
    private SeekBar sb_B_samples_num;
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
        sb_B_samples_num = (SeekBar) view.findViewById(R.id.B_samples_num);

        tv_all_samples_num_show = (TextView) view.findViewById(R.id.all_samples_num_show);
        tv_A_samples_num_show = (TextView) view.findViewById(R.id.A_samples_num_show);
        tv_B_samples_num_show = (TextView) view.findViewById(R.id.B_samples_num_show);


        int version_len = 4;
        byte[] version = new byte[version_len];
        FileIO.getBytes(MarkBitApplication.i_file, version, 0, version_len);
        tv_version.setText(bytesToHexString(version));

        int all_samples_num = FileIO.getByte(MarkBitApplication.i_file, 0x10) & 0xff;
        int A_samples_num = FileIO.getByte(MarkBitApplication.i_file, 0x11) & 0xff;
        int B_samples_num = FileIO.getByte(MarkBitApplication.i_file, 0x12) & 0xff;
        sb_all_samples_num.setProgress(all_samples_num);
        sb_A_samples_num.setProgress(A_samples_num);
        sb_B_samples_num.setProgress(B_samples_num);
        tv_all_samples_num_show.setText(String.valueOf(all_samples_num));
        tv_A_samples_num_show.setText(String.valueOf(A_samples_num));
        tv_B_samples_num_show.setText(String.valueOf(B_samples_num));
        Log.d(TAG, "" + all_samples_num);
        Log.d(TAG, "" + A_samples_num);
        Log.d(TAG, "" + B_samples_num);



        sb_brightness = (SeekBar) view.findViewById(R.id.brightness_adjust);
        sb_A_show_time= (SeekBar) view.findViewById(R.id.A_show_time);
        sb_B_show_time = (SeekBar) view.findViewById(R.id.B_show_time);
        sp_battery_types = (Spinner) view.findViewById(R.id.battery_type);
        tv_brightness_show = (TextView) view.findViewById(R.id.brightness_num_show);
        tv_A_show_time_num_show = (TextView) view.findViewById(R.id.A_show_time_num_show);
        tv_B_show_time_num_show = (TextView) view.findViewById(R.id.B_show_time_num_show);

        int brightness = FileIO.getByte(MarkBitApplication.i_file, 0x13) & 0xff;
        int A_show_time = FileIO.getByte(MarkBitApplication.i_file, 0x14) & 0xff;
        int B_show_time = FileIO.getByte(MarkBitApplication.i_file, 0x16) & 0xff;
        int battery_types = FileIO.getByte(MarkBitApplication.i_file, 0x18) & 0xff;
        sb_brightness.setProgress(brightness);
        sb_A_show_time.setProgress(A_show_time);
        sb_B_show_time.setProgress(B_show_time);
        sp_battery_types.setSelection(battery_types);
        tv_brightness_show.setText(String.valueOf(brightness));
        tv_A_show_time_num_show.setText(String.valueOf(A_show_time));
        tv_B_show_time_num_show.setText(String.valueOf(B_show_time));

        Log.d(TAG, "" + brightness);
        Log.d(TAG, "" + A_show_time);
        Log.d(TAG, "" + B_show_time);
        Log.d(TAG, "" + battery_types);


        st_magnetic = (Switch) view.findViewById(R.id.magnetic_switch);
        st_low_voltage = (Switch) view.findViewById(R.id.low_voltage_switch);
        st_dump = (Switch) view.findViewById(R.id.dump_switch);

        byte is_magnetic = FileIO.getByte(MarkBitApplication.i_file, 0x19);
        byte is_low_voltage = FileIO.getByte(MarkBitApplication.i_file, 0x1A);
        byte is_dump = FileIO.getByte(MarkBitApplication.i_file, 0x1B);
        st_magnetic.setChecked(is_magnetic == 0x01 ? true : false);
        st_low_voltage.setChecked(is_low_voltage == 0x01 ? true : false);
        st_dump.setChecked(is_dump == 0x01 ? true : false);
        Log.d(TAG, "" + is_magnetic);
        Log.d(TAG, "" + is_low_voltage);
        Log.d(TAG, "" + is_dump);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        initUI(view);

        sb_all_samples_num.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress < 100) {
                    seekBar.setProgress(100);
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
                FileIO.setBytes(MarkBitApplication.i_file, 0x10, read_byte_num, all_samples);
                FileIO.setBytes(MarkBitApplication.r_file, 0x10, read_byte_num, all_samples);
            }
        });

        sb_A_samples_num.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_A_samples_num_show.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int read_byte_num = 1;

                byte[] A_samples = new byte[read_byte_num];
                A_samples[0] = (byte) seekBar.getProgress();
                FileIO.setBytes(MarkBitApplication.i_file, 0x11, read_byte_num, A_samples);
                FileIO.setBytes(MarkBitApplication.r_file, 0x11, read_byte_num, A_samples);


            }
        });

        sb_B_samples_num.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_B_samples_num_show.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int read_byte_num = 1;

                byte[] B_samples = new byte[read_byte_num];
                B_samples[0] = (byte) seekBar.getProgress();
                FileIO.setBytes(MarkBitApplication.i_file, 0x12, read_byte_num, B_samples);
                FileIO.setBytes(MarkBitApplication.r_file, 0x12, read_byte_num, B_samples);
            }
        });



        sb_brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_brightness_show.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int read_byte_num = 1;

                byte[] new_brightness = new byte[read_byte_num];
                new_brightness[0] = (byte) seekBar.getProgress();
                FileIO.setBytes(MarkBitApplication.i_file, 0x13, read_byte_num, new_brightness);
                FileIO.setBytes(MarkBitApplication.r_file, 0x13, read_byte_num, new_brightness);
            }
        });

        sb_A_show_time.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_A_show_time_num_show.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int read_byte_num = 1;

                byte[] showTime = new byte[read_byte_num];
                showTime[0] = (byte) seekBar.getProgress();
                FileIO.setBytes(MarkBitApplication.i_file, 0x14, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.r_file, 0x14, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.i_file, 0x17, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.r_file, 0x17, read_byte_num, showTime);
            }
        });

        sb_B_show_time.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tv_B_show_time_num_show.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int read_byte_num = 1;

                byte[] showTime = new byte[read_byte_num];
                showTime[0] = (byte) seekBar.getProgress();
                FileIO.setBytes(MarkBitApplication.i_file, 0x16, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.r_file, 0x16, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.i_file, 0x15, read_byte_num, showTime);
                FileIO.setBytes(MarkBitApplication.r_file, 0x15, read_byte_num, showTime);
            }
        });



        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(view.getContext(), R.array.battery_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp_battery_types.setAdapter(adapter);
        byte battery_type = FileIO.getByte(MarkBitApplication.i_file, 0x18);
        sp_battery_types.setSelection(battery_type & 0xff, true);
        sp_battery_types.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                String battery_type = String.valueOf(parent.getItemAtPosition(position));

                byte battery_type = FileIO.getByte(MarkBitApplication.i_file, 0x18);

                Log.d(TAG, "position: " + position);
//                Toast.makeText(getContext(), position, Toast.LENGTH_SHORT).show();

                int read_byte_num = 1;
                byte[] type = new byte[read_byte_num];
                type[0] = (byte) position;
                FileIO.setBytes(MarkBitApplication.i_file, 0x18, read_byte_num, type);
                FileIO.setBytes(MarkBitApplication.r_file, 0x18, read_byte_num, type);
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
                FileIO.setBytes(MarkBitApplication.i_file, 0x19, read_byte_num, set);
                FileIO.setBytes(MarkBitApplication.r_file, 0x19, read_byte_num, set);
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
                FileIO.setBytes(MarkBitApplication.i_file, 0x1A, read_byte_num, set);
                FileIO.setBytes(MarkBitApplication.r_file, 0x1A, read_byte_num, set);
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
                FileIO.setBytes(MarkBitApplication.i_file, 0x1B, read_byte_num, set);
                FileIO.setBytes(MarkBitApplication.r_file, 0x1B, read_byte_num, set);
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
        // TODO: Update argument type and name
        void updateIndexMark();
    }
}
