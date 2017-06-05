package com.liwn.zzl.markbit;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
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

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
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

    private Context mContext;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */

    private Button bluetoothStatus = null;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothLeService mBluetoothLeService = null;

    private Timer dataTimer;
    private TimerTask dataTimerTask;

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

    private String mConnectedDeviceName = null;
    private String mDeviceAddress;
    private boolean isPackageSendSuccessed;
    private boolean isBluetoothConnected;
    private int address;
    private File file;
    private RandomAccessFile randomAccessFile;
    private final int diff = 32;
    private byte[] sendBytes;
    private boolean isFileStartSend = false;
    private boolean isFileFinished = false;
    private boolean isFileCancled = false;
    // refresh setting or update mark libs, isUpdateType[0] is the newest type.
    private String[] isUpdateType = new String[2];
    private boolean isClickedDisConnected = false;

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
                bluetoothStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_bluetooth_connected_white_36dp, 0);
                bluetoothStatus.setText(mConnectedDeviceName);
                mSendFileFragment.enableBT();

                MarkBitApplication.connectedDeviceName = mConnectedDeviceName;
                Log.d(TAG, "broadcastReceiver connected " + mConnectedDeviceName);
                Toast.makeText(getApplicationContext(), getString(R.string.connected_to) + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isBluetoothConnected = false;
                bluetoothStatus.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.mipmap.ic_bluetooth_disabled_white_36dp, 0);
                bluetoothStatus.setText(R.string.display_disconnected);
                // if connected, then break the for loop
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothLeService.connect(mConnectedDeviceName, mDeviceAddress);
                    }
                }, 3000);
                // TODO: 1.reconnection speed up 2. add new web page 3. fixed the reconnection loss
//                if (!isClickedDisConnected) {
//                    mBluetoothLeService.connect(mConnectedDeviceName, mDeviceAddress);
//                } else {
//                    isClickedDisConnected = false;
//                }
                mSendFileFragment.disableBT();

                isPackageSendSuccessed = false;

                Log.d(TAG, "broadcastReceiver disconnected");
                Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTING.equals(action)) {
                Log.d(TAG, "connecting");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

//                breakAndCheck();
//                if (sendBytes != null) {
//                    sendMessage(sendBytes);
//                }
                Log.d(TAG, "in action services discovered");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String recString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d(TAG, "receive bytes: " + recString);
                final byte[] recBytes = FileIO.hexStringToByteArray(recString);

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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(MarkBitApplication.I_SYNCED, MarkBitApplication.i_synced).apply();
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(MarkBitApplication.R_SYNCED, MarkBitApplication.r_synced).apply();
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
                    Log.e(TAG, "data timer is running");
                    if (mBluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED) {
                        sendMessage(sendBytes);
                    }
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
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(MarkBitApplication.I_SYNCED, MarkBitApplication.i_synced).apply();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(MarkBitApplication.R_SYNCED, MarkBitApplication.r_synced).apply();
            super.onBackPressed();
            return;
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

        mContext = this;
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        bluetoothStatus = (Button) findViewById(R.id.icon_bluetooth_status);
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
                Log.e(TAG, "is connected: " + (mBluetoothLeService.getConnectionState() == BluetoothLeService.STATE_CONNECTED));
//                if (isBluetoothConnected) {
//                    mBluetoothLeService.disconnect();
//                    isClickedDisConnected = true;
//                } else {
                    Intent serverIntent = new Intent(mContext, DeviceListActivity.class);
                    startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
//                }
            }
        });

        updateSelfNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
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
        isUpdateType[1] = isUpdateType[0];
        isUpdateType[0] = type;

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

                    if (MarkBitApplication.UPDATE_TYPE_SETTING.equals(type)) {
                        mSendFileFragment.initProgressBar(MarkBitApplication.MARK_SETTING_SIZE);
                    } else if (MarkBitApplication.UPDATE_TYPE_LIBRARY.equals(type)) {
                        mSendFileFragment.initProgressBar(allBytes);
                    } else {
                        Log.e(TAG, "update type undefined!");
                        mSendFileFragment.initProgressBar(MarkBitApplication.MARK_SETTING_SIZE);
                    }
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
                    if (MarkBitApplication.UPDATE_TYPE_SETTING.equals(type)) {
                        mSendFileFragment.initProgressBar(MarkBitApplication.MARK_SETTING_SIZE);
                    } else if (MarkBitApplication.UPDATE_TYPE_LIBRARY.equals(type)) {
                        mSendFileFragment.initProgressBar(allBytes);
                    } else {
                        Log.e(TAG, "update type undefined!");
                        mSendFileFragment.initProgressBar(MarkBitApplication.MARK_SETTING_SIZE);
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
            Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
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
                        Log.d(TAG, "choose: : " + mConnectedDeviceName + ": " + mDeviceAddress);
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
                        int old_control_id = data.getExtras().getInt(MarkItemFragment.OLD_CTL_ID);
                        int new_position_id = data.getExtras().getInt(MarkItemFragment.NEW_POS_ID);
                        int new_control_id = data.getExtras().getInt(MarkItemFragment.NEW_CTL_ID);
                        Log.e(TAG, "A");
                        mMarkItemFragment.replaceMark(true, old_position_id, old_control_id, new_position_id, new_control_id);

                        MarkBitApplication.i_synced = false;
                        MarkBitApplication.r_synced = false;
                        updateSelfNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
                    }
                }
                break;
            case MarkItemFragment.REQUEST_CHOOSE_NEW_MARK_B:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        int old_position_id = data.getExtras().getInt(MarkItemFragment.OLD_POS_ID);
                        int old_control_id = data.getExtras().getInt(MarkItemFragment.OLD_CTL_ID);
                        int new_position_id = data.getExtras().getInt(MarkItemFragment.NEW_POS_ID);
                        int new_control_id = data.getExtras().getInt(MarkItemFragment.NEW_CTL_ID);
                        Log.e(TAG, "B");
                        mMarkItemFragment.replaceMark(false, old_position_id, old_control_id, new_position_id, new_control_id);

                        MarkBitApplication.i_synced = false;
                        MarkBitApplication.r_synced = false;
                        updateSelfNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
                    }
                }
                break;
            case MarkItemFragment.REQUEST_CHOOSE_MODIFY_MARK_A:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        int old_position_id = data.getExtras().getInt(MarkItemFragment.OLD_POS_ID);
                        int old_control_id = data.getExtras().getInt(MarkItemFragment.OLD_CTL_ID);
                        int new_position_id = data.getExtras().getInt(MarkItemFragment.NEW_POS_ID);
                        int new_control_id = data.getExtras().getInt(MarkItemFragment.NEW_CTL_ID);
                        mMarkItemFragment.replaceMarkContent(true, old_position_id, old_control_id, new_position_id, new_control_id);

                        MarkBitApplication.i_synced = false;
                        MarkBitApplication.r_synced = false;
                        updateSelfNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
                        Log.e(TAG, "modify mark return: true");
                    }
                }
                break;
            case MarkItemFragment.REQUEST_CHOOSE_MODIFY_MARK_B:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        int old_position_id = data.getExtras().getInt(MarkItemFragment.OLD_POS_ID);
                        int old_control_id = data.getExtras().getInt(MarkItemFragment.OLD_CTL_ID);
                        int new_position_id = data.getExtras().getInt(MarkItemFragment.NEW_POS_ID);
                        int new_control_id = data.getExtras().getInt(MarkItemFragment.NEW_CTL_ID);
                        mMarkItemFragment.replaceMarkContent(false, old_position_id, old_control_id, new_position_id, new_control_id);

                        MarkBitApplication.i_synced = false;
                        MarkBitApplication.r_synced = false;
                        updateSelfNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);
                        Log.e(TAG, "modify mark return: true");
                    }
                }
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

            if (isUpdateType[0].equals(MarkBitApplication.UPDATE_TYPE_LIBRARY)) {

            } else if (isUpdateType[0].equals(MarkBitApplication.UPDATE_TYPE_SETTING)) {
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

            mSendFileFragment.destroyProgressBar();

            if (isUpdateType[0].equals(MarkBitApplication.UPDATE_TYPE_SETTING)) {
                Toast.makeText(this, getString(R.string.update_setting_result_succeed), Toast.LENGTH_SHORT).show();
            } else if (isUpdateType[0].equals(MarkBitApplication.UPDATE_TYPE_LIBRARY)) {
                Toast.makeText(this, getString(R.string.send_file_result_succeed), Toast.LENGTH_SHORT).show();
            }
            Log.i(TAG, "filename: " + file.getName());
//            if (getString(R.string.I_name).equals(file.getName())) {
//                MarkBitApplication.i_synced = true;
//            } else if (getString(R.string.R_name).equals(file.getName())) {
//                MarkBitApplication.r_synced = true;
//            }

            if (file.getName().startsWith(getString(R.string.I_name).split(".")[0]) && file.getName().endsWith(getString(R.string.I_name).split(".")[1])) {
                MarkBitApplication.i_synced = true;
            } else if (file.getName().startsWith(getString(R.string.R_name).split(".")[0]) && file.getName().endsWith(getString(R.string.R_name).split(".")[1])) {
                MarkBitApplication.r_synced = true;
            } else {
                Log.e(TAG, "filename: " + file.getName() + "is not valid.");
            }

            updateSelfNotification(MarkBitApplication.i_synced, MarkBitApplication.r_synced);

            isFileStartSend = false;
            isFileFinished = true;
            isPackageSendSuccessed = false;
            isFileCancled = false;

            recoveryScreenStatus();
            Log.e(TAG, "file send succeed, and cancel data timer");
            dataTimer.cancel();
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
        if(isFileStartSend && !isFileFinished && isFileCancled &&
                (isUpdateType[0] != null && isUpdateType[0].equals(isUpdateType[1]))) {
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
            Log.d(TAG, "sended message: " + FileIO.bytesToHexString(message));

            int perLen = 20;
            int num = (message.length - 1) / perLen + 1;

            for (int i = 0; i < num; ++i) {
                byte[] subMes = Arrays.copyOfRange(message, i * perLen, (i+1) * perLen);
                mBluetoothLeService.WriteValue(subMes);
                try {
                    Thread.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        return true;
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
        if (i_synced && r_synced) {
            synced_notification.setVisibility(View.GONE);
        } else if (i_synced && !r_synced) {
            String notification = String.format(getString(R.string.not_synced), getString(R.string.R_show_name));
            synced_notification.setText(notification);
            synced_notification.setVisibility(View.VISIBLE);
        } else if (!i_synced && r_synced) {
            String notification = String.format(getString(R.string.not_synced), getString(R.string.I_show_name));
            synced_notification.setText(notification);
            synced_notification.setVisibility(View.VISIBLE);
        } else if (!i_synced && !r_synced) {
            String notification = String.format(getString(R.string.i_r_not_synced), getString(R.string.I_show_name), getString(R.string.R_show_name));
            synced_notification.setText(notification);
            synced_notification.setVisibility(View.VISIBLE);
        }

//        if (!i_synced || !r_synced) {
//            synced_notification.setVisibility(View.VISIBLE);
//            synced_notification.setText(getString(R.string.please_synced));
//        } else {
//            synced_notification.setVisibility(View.GONE);
//        }
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
            Toast.makeText(this, getString(R.string.factory_name_title) + str_factory_name + "\n"
                    + getString(R.string.device_name_title) + device_name , Toast.LENGTH_LONG).show();
            String tmp = getString(R.string.factory_name);
            if (str_factory_name.equals(getString(R.string.factory_name))) {
                return true;
            } else {
                Toast.makeText(this, R.string.file_invalid, Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Toast.makeText(this, R.string.file_invalid, Toast.LENGTH_LONG).show();
            return false;
        }
    }
}

