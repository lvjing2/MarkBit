package com.liwn.zzl.markbit;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.liwn.zzl.markbit.bluetooth.BluetoothLeService;
import com.liwn.zzl.markbit.bluetooth.DeviceListActivity;
import com.liwn.zzl.markbit.mark.DummyContent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements MarkItemFragment.OnListFragmentInteractionListener, SendFileFragment.OnFragmentInteractionListener, SettingFragment.OnFragmentInteractionListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 2;
    private static final int REQUEST_CODE_TWO_PERMISSION = 3;
    private static final String PREFERENCE = "PREFERENCE";
    private static final String I_SYNCED = "I_SYNCED";
    private static final String R_SYNCED = "R_SYNCED";

    private Context mContext;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    private ImageButton bluetoothStatus = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeService mBluetoothLeService = null;

    private String mConnectedDeviceName = null;
    private String mDeviceAddress;
    private boolean isDataTimerStart = false;
    private Timer dataTimer;
    private TimerTask dataTimerTask;
    private boolean isPackageSendSuccessed;
//    private int isResendCount = 0;

    private int clickedIndex;
    private int currentIndex;
    private SendFileFragment mSendFileFragment;
    private MarkItemFragment mMarkItemFragment;
    private SettingFragment mSettingFragment;
    private Fragment[] mFragments;
    private Button[] mTabs;

    private TextView synced_notification;
    private boolean dbClickExit = false;

    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * The {@link ViewPager} that will host the section contents.
     */
//    private SmartFragmentStatePagerAdapter mSmartFragmentStatePagerAdapter;
//    private ViewPager mViewPager;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
//            mBluetoothLeService.connect(mConnectedDeviceName, mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private boolean isBluetoothConnected;
    private int address;
    private File file;
    private RandomAccessFile randomAccessFile;
    private OutputStream outputStream;
    private final int diff = 32;
    private byte[] sendBytes;
    private boolean isFileStartSend = false;
    private boolean isFileFinished = false;
    private boolean isFileCancled = false;
    private String marksFolderName = FileIO.default_prefix;
    private String isUpdateType;

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//        if (mBluetoothLeService != null) {
//            final boolean result = mBluetoothLeService.connect(mConnectedDeviceName, mDeviceAddress);
//            Log.d(TAG, "Connect request result=" + result);
//        }
    }

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                isBluetoothConnected = true;
                mConnectedDeviceName = intent.getStringExtra(MarkBitApplication.DEVICE_NAME);
                bluetoothStatus.setImageResource(R.drawable.ic_bluetooth_connected);
                mSendFileFragment.enableBT();

                MarkBitApplication.connectedDeviceName = mConnectedDeviceName;
                Log.d(TAG, "broadcastReceiver connected " + mConnectedDeviceName);
                Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isBluetoothConnected = false;
                bluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disconnected);
//                mBluetoothLeService.connect(mConnectedDeviceName, mDeviceAddress);
                mSendFileFragment.disableBT();

                isPackageSendSuccessed = false;

                Log.d(TAG, "broadcastReceiver disconnected");
                Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTING.equals(action)) {
                Log.d(TAG, "broadcastReceiver connecting");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

//                breakAndCheck();
                if (sendBytes != null) {
                    sendMessage(sendBytes);
                }
                Log.d(TAG, "in action services discovered");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "in action data available");
                String recString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d(TAG, "receive bytes: " + recString);
                final byte[] recBytes = hexStringToByteArray(recString);

                readProcess(recBytes);
            }
        }
    };

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    public void onTabClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_mark_management:
                clickedIndex = 0;
                break;
            case R.id.btn_draw_mark:
                clickedIndex = 1;
                break;
            case R.id.btn_send_file:
                clickedIndex = 2;
                break;
        }

        if (currentIndex != clickedIndex) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(mFragments[currentIndex]);
            if (!mFragments[clickedIndex].isAdded()) {
                ft.add(R.id.fragment_container, mFragments[clickedIndex]);
            }
            ft.show(mFragments[clickedIndex]).commit();
        }

        mTabs[currentIndex].setSelected(false);
        // 把当前tab设为选中状态
        mTabs[clickedIndex].setSelected(true);
        currentIndex = clickedIndex;
    }

    private void initView() {
        mTabs = new Button[3];
        mTabs[0] = (Button) findViewById(R.id.btn_mark_management);
        mTabs[1] = (Button) findViewById(R.id.btn_draw_mark);
        mTabs[2] = (Button) findViewById(R.id.btn_send_file);
        mTabs[0].setSelected(true);

        mSendFileFragment = new SendFileFragment();
        mMarkItemFragment = new MarkItemFragment();
        mSettingFragment = new SettingFragment();
        mFragments = new Fragment [] {mMarkItemFragment, mSettingFragment, mSendFileFragment};

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(mFragments[currentIndex]);
        if (!mFragments[clickedIndex].isAdded()) {
            ft.add(R.id.fragment_container, mFragments[clickedIndex]);
        }
        ft.show(mFragments[clickedIndex]).commit();

        synced_notification = (TextView) findViewById(R.id.synced_notification);
    }

    private void copyBinsFromAsset2External() {
        FileIO.deleteAllFiles();

        AssetManager assetManager = getAssets();
        InputStream is;
        try {
            String[] files = assetManager.list(MarkBitApplication.default_bins_dir_name);
            if (files != null) {
                for (String filename : files) {
                    is = assetManager.open(MarkBitApplication.default_bins_dir_name + "/" + filename);
                    FileIO.copyStream(this, is, filename);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // if first run

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isFirstRun", true)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isFirstRun", false).apply();
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(I_SYNCED, MarkBitApplication.i_synced).apply();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(R_SYNCED, MarkBitApplication.r_synced).apply();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void initDataTimer() {
        dataTimer = new Timer();
        dataTimerTask = new TimerTask() {
            @Override
            public void run() {
                if (isFileStartSend && !isFileFinished && !isPackageSendSuccessed) {
//                    if (!isTimerStart) {
//                        isResendCount = 0;
//                        isTimerStart = true;
//                        initTimer();
//                    }
                    sendMessage(sendBytes);
                }
            }
        };
        dataTimer.schedule(dataTimerTask, 2000, 2000);
    }

    @Override
    public void onBackPressed() {
        getSupportFragmentManager();
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else if (!dbClickExit) {
            dbClickExit = true;
            Toast.makeText(this, getString(R.string.dbClick_Exit), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dbClickExit = false;
                }
            }, 2000);
        } else {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(I_SYNCED, MarkBitApplication.i_synced).apply();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(R_SYNCED, MarkBitApplication.r_synced).apply();
            super.onBackPressed();
            return;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_TWO_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //授权成功，直接操作
                    Log.e(TAG, "SD and BLE permission granted succeed!");
                    copyBinsFromAsset2External();

                    MarkBitApplication.i_file = FileIO.getIconFile();
                    MarkBitApplication.r_file = FileIO.getRconFile();

                    MarkBitApplication.i_synced = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(I_SYNCED, true);
                    MarkBitApplication.r_synced = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(R_SYNCED, true);
                    updateSelfNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
                    if (!MarkBitApplication.i_file.exists() || !MarkBitApplication.r_file.exists()) {
                        Log.d(TAG, "NO BINS FILE!");
                        Toast.makeText(MarkBitApplication.applicationContext, R.string.bins_not_import, Toast.LENGTH_LONG).show();
//			return;
                    } else {
                        MarkBitApplication.dummyContent = new DummyContent();
                        mMarkItemFragment.recyclerView_A.getAdapter().notifyDataSetChanged();
                        mMarkItemFragment.recyclerView_B.getAdapter().notifyDataSetChanged();
                    }
                } else {
                    //禁止授权
                    Toast.makeText(MainActivity.this, "SD or BLE permission is denied.", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "SD or BLE permission granted failed!");
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        initView();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "no storage permission");

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_TWO_PERMISSION);
        }

        boolean isFirstRun = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isFirstRun", true);
        if (isFirstRun) {
            MarkBitApplication.i_synced = true;
            MarkBitApplication.r_synced = true;
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(I_SYNCED, MarkBitApplication.i_synced).apply();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(R_SYNCED, MarkBitApplication.r_synced).apply();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                copyBinsFromAsset2External();
            } else {
//                Toast.makeText(MainActivity.this, "external storage permission is denied.", Toast.LENGTH_SHORT).show();
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            MarkBitApplication.i_file = FileIO.getIconFile();
            MarkBitApplication.r_file = FileIO.getRconFile();

            MarkBitApplication.i_synced = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(I_SYNCED, true);
            MarkBitApplication.r_synced = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(R_SYNCED, true);
            updateSelfNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
            if (!MarkBitApplication.i_file.exists() || !MarkBitApplication.r_file.exists()) {
                Log.d(TAG, "NO BINS FILE!");
                Toast.makeText(MarkBitApplication.applicationContext, R.string.bins_not_import, Toast.LENGTH_LONG).show();
//			return;
            } else {
                MarkBitApplication.dummyContent = new DummyContent();
            }
        }

        mContext = this;
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        bluetoothStatus = (ImageButton) findViewById(R.id.icon_bluetooth_status);
        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.bluetooth_not_available, Toast.LENGTH_LONG).show();
            finish();
        }

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        bluetoothStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "is connected:" + isBluetoothConnected + ", " + mBluetoothLeService.getConnectionState());
                if (isBluetoothConnected) {
                    mBluetoothLeService.disconnect();
                } else {
                    Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                }
            }
        });

//        HorizontalScrollView horizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);
//        horizontalScrollView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                clickedIndex = 2;
//                if (currentIndex != clickedIndex) {
//                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//                    ft.hide(mFragments[currentIndex]);
//                    if (!mFragments[clickedIndex].isAdded()) {
//                        ft.add(R.id.fragment_container, mFragments[clickedIndex]);
//                    }
//                    ft.show(mFragments[clickedIndex]).commit();
//                }
//
//                mTabs[currentIndex].setSelected(false);
//                // 把当前tab设为选中状态
//                mTabs[clickedIndex].setSelected(true);
//                currentIndex = clickedIndex;
//            }
//        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else if (mBluetoothLeService == null) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        recoveryScreenStatus();
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.menu_connect_device) {
//            Intent serverIntent = new Intent(this, DeviceListActivity.class);
//            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @Override
    public void onMarkItemFragmentInteraction(DummyContent.DummyItem item) {

    }

    @Override
    public void cancleFileSend() {
        isFileStartSend = true;
        isFileFinished = false;

        isPackageSendSuccessed = false;
        isFileCancled = true;

        recoveryScreenStatus();

        dataTimer.cancel();
    }

    @Override
    public void sendFileFromUriByBT(Uri uri, String type) {
        isUpdateType = type;

        if (uri == null || uri.toString().length() < 1) {
            Log.e(TAG, "BAD URI: cannot load file");
            return;
        }

        // start send file
        try {
            File tmp_file = FileIO.getFile(this, uri);
            RandomAccessFile new_file = new RandomAccessFile(tmp_file, "r");

            // if not valid, then return and cancel the send procedure
            if (!checkFileValidate(tmp_file)) {
                Log.e(TAG, "file check invalided!");
                return;
            }
            if (file != null && tmp_file.getAbsolutePath().equals(file.getAbsolutePath()) && isFileCancled) {
                if (new_file != null) {
                    int allBytes = (int) new_file.length();

                    if (type == MarkBitApplication.UPDATE_TYPE_SETTING) {
                        mSendFileFragment.initProgressBar(MarkBitApplication.MARK_SETTING_SIZE);
                    } else if (type == MarkBitApplication.UPDATE_TYPE_LIBRARY) {
                        mSendFileFragment.initProgressBar(allBytes);
                    }
//                    mSendFileFragment.initProgressBar(allBytes);
                    isFileStartSend = true;

//                    initSendBytes();
                    breakAndCheck();
                    initDataTimer();
                    keepScreenON();

                    isPackageSendSuccessed = false;
                    isFileCancled = false;
                    isFileFinished = false;
                }
            } else {
                if (new_file != null) {
                    int allBytes = (int)new_file.length();
                    if (type == MarkBitApplication.UPDATE_TYPE_SETTING) {
                        mSendFileFragment.initProgressBar(MarkBitApplication.MARK_SETTING_SIZE);
                    } else if (type == MarkBitApplication.UPDATE_TYPE_LIBRARY) {
                        mSendFileFragment.initProgressBar(allBytes);
                    }
//                    mSendFileFragment.initProgressBar(allBytes);

                    isFileFinished = false;
                    isFileStartSend = true;
                    isPackageSendSuccessed = false;
                    isFileCancled = false;

                    initSendBytes();

                    byte[] send_init = {(byte) 0xA5, (byte) 0x07, (byte) 0x0A, (byte) 0xC0, (byte) 0x0C, (byte) 0x0D, (byte) 0x5A};

                    for (int i = 0; i < send_init.length; ++i) {
                        sendBytes[i] = send_init[i];
                    }

                    file = tmp_file;
                    randomAccessFile = new_file;

                    initDataTimer();
                    keepScreenON();
                }
            }

        } catch (IOException e) {
            Toast.makeText(this, "file not found.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        mConnectedDeviceName = data.getExtras().getString(MarkBitApplication.DEVICE_NAME);
                        mDeviceAddress = data.getExtras().getString(MarkBitApplication.DEVICE_ADDRESS);
                        Log.d(TAG, "connecting...: " + mConnectedDeviceName + ": " + mDeviceAddress);
                        mBluetoothLeService.connect(mConnectedDeviceName, mDeviceAddress);
                        MarkBitApplication.connectedDeviceName = mConnectedDeviceName;
                    }
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // enabled bt
//                    Log.d(TAG, "bluetooth enabled!");
//                    bluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disconnected);
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case MarkItemFragment.REQUEST_CHOOSE_NEW_MARK_A:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        int old_position_id = data.getExtras().getInt(MarkItemFragment.OLD_POS_ID);
                        int new_position_id = data.getExtras().getInt(MarkItemFragment.NEW_POS_ID);
                        Log.e(TAG, "A");
                        mMarkItemFragment.replaceMark(true, old_position_id, new_position_id);

                        MarkBitApplication.i_synced = false;
//                        MarkBitApplication.r_synced = false;
                        updateSelfNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
                    }
                }
                break;
            case MarkItemFragment.REQUEST_CHOOSE_NEW_MARK_B:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        int old_position_id = data.getExtras().getInt(MarkItemFragment.OLD_POS_ID);
                        int new_position_id = data.getExtras().getInt(MarkItemFragment.NEW_POS_ID);
                        Log.e(TAG, "B");
                        mMarkItemFragment.replaceMark(false, old_position_id, new_position_id);

                        MarkBitApplication.i_synced = false;
//                        MarkBitApplication.r_synced = false;
                        updateSelfNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
                    }
                }
                break;
        }

    }

    private byte getCheckSum(byte[] bytes, int offset, int length) {

        long sum = 0;
        for (int i = offset; i < offset + length; ++i) {
            sum += (long) (bytes[i] & 0xff);
        }

        return (byte)(sum & 0xff);
    }

    private void initSendBytes() {
        int sendLen = diff + 8;
        sendBytes = new byte[sendLen];
        sendBytes[0] = (byte) 0xA5;
        sendBytes[1] = (byte) (sendLen & 0xff);
        sendBytes[2] = (byte) 0x0A;
//        sendBytes[3] = recBytes[3];
//        sendBytes[4] = recBytes[4];
        sendBytes[sendLen-1] = (byte) 0x5A;
        sendBytes[sendLen-2] = (byte) 0x0D;
    }

    private void readProcessTx(byte[] recBytes) {
        int length = recBytes[1] & 0xff;
        if (recBytes[0] == (byte) 0xA5 && recBytes[1] == (byte) 0x07
                && recBytes[length - 2] == (byte) 0x0D && recBytes[length - 1] == (byte) 0x5A) {
            address = (recBytes[3] & 0xff) * 256 + (recBytes[4] & 0xff);

            if (address == 0) {
                initSendBytes();
            }
            // if file is not cancle send, then the package is send correctly.
            if (!isFileCancled) {
                isPackageSendSuccessed = true;
            }
        }

        byte[] tmp = new byte[diff];
        int sendLen = diff + 8;
        sendBytes[3] = recBytes[3];
        sendBytes[4] = recBytes[4];

        // file is not finished, so keep sending
        if (isFileStartSend && !isFileFinished && !isFileCancled) {

//            ((SendFileFragment) mSmartFragmentStatePagerAdapter.getRegisteredFragment(0)).setProgressBarNum((address + 1) * diff);
            mSendFileFragment.setProgressBarNum((address + 1) * diff);

            try {
                randomAccessFile.seek(diff * address);
                int res = randomAccessFile.read(tmp, 0, diff);
                if (res == -1) {
                    isFileFinished = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (isUpdateType == MarkBitApplication.UPDATE_TYPE_LIBRARY) {

            } else if (isUpdateType == MarkBitApplication.UPDATE_TYPE_SETTING) {
                if (address > MarkBitApplication.MARK_SETTING_SIZE / diff) {
                    isFileFinished = true;
                }
            }

            sendBytes[sendLen - 3] = getCheckSum(tmp, 0, diff);
            for (int i = 0; i < diff; ++i) {
                sendBytes[i + 5] = tmp[i];
            }

            if (sendMessage(sendBytes)) {
                isPackageSendSuccessed = false;
            } else {

            }
        } else if (isFileStartSend && isFileFinished){

//            ((SendFileFragment) mSmartFragmentStatePagerAdapter.getRegisteredFragment(0)).destroyProgressBar();
            mSendFileFragment.destroyProgressBar();

            Toast.makeText(this, getString(R.string.send_file_result_succeed), Toast.LENGTH_SHORT).show();
//            Log.e(TAG, "filename: " + file.getName());
            if (getString(R.string.I_name).equals(file.getName())) {
                MarkBitApplication.i_synced = true;
            } else if (getString(R.string.R_name).equals(file.getName())) {
                MarkBitApplication.r_synced = true;
            }

            updateSelfNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);

            isFileStartSend = false;
            isFileFinished = true;
            isPackageSendSuccessed = false;
            isFileCancled = false;

            recoveryScreenStatus();
            dataTimer.cancel();
        }
    }

    private void readProcessRx(byte[] recBytes) {
        int length = recBytes[1] & 0xff;
        if (length > recBytes.length) {
            Log.e(TAG, "received bytes: " + bytesToHexString(recBytes) + " invalid!");
            return;
        }
        if (recBytes[0] == (byte) 0xA5 && recBytes[length - 2] == (byte) 0x0D && recBytes[length - 1] == (byte) 0x5A) {
            // receive succeed.
            byte[] feedbackInstruct = {(byte) 0xA5, (byte) 0x07, (byte) 0x8A, recBytes[3], recBytes[4], (byte) 0x0D, (byte) 0x5A};
            if (recBytes[1] == (byte) 0x07) {
                // init, create receive file
                try {
                    File file = FileIO.createNewEmptyPictureFile(this, "received_file");
                    if (file != null) {
                        outputStream = new FileOutputStream(file);
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } else if (recBytes[1] == (byte) ((diff + 8) & 0xff)) {
                // add the address
                byte cc = getCheckSum(recBytes, 5, diff);
                if (cc == recBytes[length - 3]) {
                    address = (recBytes[3] & 0xff) * 256 + (recBytes[4] & 0xff);
                    address += 1;
                    feedbackInstruct[3] = (byte) ((address & 0xff00) >> 8);
                    feedbackInstruct[4] = (byte) (address & 0xff);

                    // storage
                    try {
                        if (outputStream != null) {
                            outputStream.write(recBytes, 5, diff);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                }
            }

            sendMessage(feedbackInstruct);
        } else {
        }
    }

    private void readProcess(byte[] recBytes) {
        // A5 07 8A 00 00 0D 5A

        if (recBytes.length >= 7) {
            switch (recBytes[2]) {
                case (byte) 0x8A:
                    readProcessTx(recBytes);
                    break;
//                case (byte) 0x0A:
//                    readProcessRx(recBytes);
//                    break;
                default:
                    Log.e(TAG, "received msg is invalid!");
                    Toast.makeText(this, "received msg is invalid!", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            Log.e(TAG, "received recbytes invalid!");
        }
    }

    private void keepScreenON() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
//        params.screenBrightness = 0;
        getWindow().setAttributes(params);
    }

    private void recoveryScreenStatus() {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        params.screenBrightness = -1;
        getWindow().setAttributes(params);
    }

    private void breakAndCheck() {
        if(isFileStartSend && !isFileFinished && isFileCancled) {
//            byte[] feedbackInstruct = {(byte) 0xA5, (byte) 0x07, (byte) 0x8A, sendBytes[3], sendBytes[4], (byte) 0x0D, (byte) 0x5A};
//            readProcess(feedbackInstruct);
//            if (!isTimerStart) {
//                isResendCount = 0;
//                isTimerStart = true;
//                initTimer();
//            }
            sendMessage(sendBytes);
        } else {
            if (sendBytes != null) {
                byte[] send_init = {(byte) 0xA5, (byte) 0x07, (byte) 0x0A, (byte) 0xC0, (byte) 0x0C, (byte) 0x0D, (byte) 0x5A};
                for (int i = 0; i < send_init.length; ++i) {
                    sendBytes[i] = send_init[i];
                }
            }
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    protected boolean sendMessage(byte[] message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothLeService.getConnectionState() != mBluetoothLeService.STATE_CONNECTED && mBluetoothLeService.getConnectionState() != mBluetoothLeService.STATE_CONNECTING) {
//            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            Log.e(TAG, getString(R.string.not_connected) + " data send failed!");
            return false;
        }

        // Check that there's actually something to send
        if (message.length > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
//            mBluetoothServer.write(message);
            Log.d(TAG, "sended message: " + bytesToHexString(message));

            int perLen = 20;
            int num = (message.length - 1) / perLen + 1;

            for (int i = 0; i < num; ++i) {
                byte[] subMes = Arrays.copyOfRange(message, i * perLen, (i+1) * perLen);
                mBluetoothLeService.WriteValue(subMes);
                try {
                    Thread.sleep(25);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        return true;
    }

    @NonNull
    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x ", b & 0xff));
        }
        return sb.toString().toUpperCase();
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    @Override
    public void updateIndexMark(int num) {
        mMarkItemFragment.updateMark(num);
    }

    @Override
    public void updateAllMark(int num) {
        mMarkItemFragment.updateAllMark(num);
    }

    private void updateSelfNotification(boolean i_synced, boolean r_synced) {
//        if (i_synced && r_synced) {
//            synced_notification.setVisibility(View.GONE);
//        } else if (i_synced && !r_synced) {
//            String notification = String.format(getString(R.string.not_synced), getString(R.string.R_name));
//            synced_notification.setText(notification);
//            synced_notification.setVisibility(View.VISIBLE);
//        } else if (!i_synced && r_synced) {
//            String notification = String.format(getString(R.string.not_synced), getString(R.string.I_name));
//            synced_notification.setText(notification);
//            synced_notification.setVisibility(View.VISIBLE);
//        } else if (!i_synced && !r_synced) {
//            String notification = String.format(getString(R.string.i_r_not_synced), getString(R.string.I_name), getString(R.string.R_name));
//            synced_notification.setText(notification);
//            synced_notification.setVisibility(View.VISIBLE);
//        }

        if (!i_synced || !r_synced) {
            synced_notification.setVisibility(View.VISIBLE);
            synced_notification.setText(getString(R.string.please_synced));
        } else {
            synced_notification.setVisibility(View.GONE);
        }
    }

    public void updateNotification(boolean i_synced, boolean r_synced) {
        updateSelfNotification(i_synced, r_synced);
    }

    private boolean checkFileValidate(File file) {
        byte device_type = FileIO.getByte(file, FileIO.DEVICE_NAME_ADDR);
        byte[] factory_name = new byte[FileIO.FACTORY_NAME_LEN];
        FileIO.getBytes(file, factory_name, FileIO.FACTORY_NAME_ADDR, FileIO.FACTORY_NAME_LEN);

        String device_name = "";
        if (device_type == 0x49) {
            device_name = getString(R.string.I_device_name);
        } else if (device_type == 0x52) {
            device_name = getString(R.string.R_device_name);
        }

        String str_factory_name = null;
        try {
            str_factory_name = new String(factory_name, "UTF-8");
            Toast.makeText(this, "factory name: " + str_factory_name + "; device_name: " + device_name , Toast.LENGTH_LONG).show();
            String tmp = getString(R.string.factory_name);
            if (str_factory_name.equals(getString(R.string.factory_name))) {
                return true;
            } else {
                Toast.makeText(this, "file is not valid", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(this, "file is not valid", Toast.LENGTH_LONG).show();
            return false;
        }
    }
}

