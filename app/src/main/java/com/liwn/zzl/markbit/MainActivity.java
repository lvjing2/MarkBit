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
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.liwn.zzl.markbit.bluetooth.BluetoothLeService;
import com.liwn.zzl.markbit.bluetooth.DeviceListActivity;
import com.liwn.zzl.markbit.dummy.DummyContent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements MarkItemFragment.OnListFragmentInteractionListener, SendFileFragment.OnFragmentInteractionListener {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 2;
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

    private int clickedIndex;
    private int currentIndex;
    private SendFileFragment mSendFileFragment;
    private MarkItemFragment mMarkItemFragment;
    private Fragment[] mFragments;
    private Button[] mTabs;

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
    private RandomAccessFile file;
    private OutputStream outputStream;
    private final int diff = 32;
    private byte[] sendBytes;
    private boolean isFileStartSend = false;
    private boolean isFileFinished = false;

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mConnectedDeviceName, mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
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

                Log.d(TAG, "broadcastReceiver connected " + mConnectedDeviceName);
                Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isBluetoothConnected = false;
                bluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disconnected);
                mBluetoothLeService.connect(mConnectedDeviceName, mDeviceAddress);
                mSendFileFragment.disableBT();

                Log.d(TAG, "broadcastReceiver disconnected");
                Toast.makeText(getApplicationContext(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            } else if (BluetoothLeService.ACTION_GATT_CONNECTING.equals(action)) {
                Log.d(TAG, "broadcastReceiver connecting");
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.

                breakAndCheck();

                Log.d(TAG, "in action services discovered");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.d(TAG, "in action data available");
                String recString = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                Log.d(TAG, "receive bytes: " + recString);
                byte[] recBytes = hexStringToByteArray(recString);
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
            case R.id.btn_send_file:
                clickedIndex = 0;
                break;
            case R.id.btn_mark_management:
                clickedIndex = 1;
                break;
            case R.id.btn_draw_mark:
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
        mTabs[0] = (Button) findViewById(R.id.btn_send_file);
        mTabs[1] = (Button) findViewById(R.id.btn_mark_management);
        mTabs[2] = (Button) findViewById(R.id.btn_draw_mark);
        mTabs[0].setSelected(true);

        mSendFileFragment = new SendFileFragment();
        mMarkItemFragment = new MarkItemFragment();
        mFragments = new Fragment [] {mSendFileFragment, mMarkItemFragment, mMarkItemFragment};

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(mFragments[currentIndex]);
        if (!mFragments[clickedIndex].isAdded()) {
            ft.add(R.id.fragment_container, mFragments[clickedIndex]);
        }
        ft.show(mFragments[clickedIndex]).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        initView();

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

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else if (mBluetoothLeService == null) {
            // TODO init mBluetoothServer
//            setupChat();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_connect_device) {
            Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        } else if (id == R.id.menu_marks_management) {

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMarkItemFragmentInteraction(DummyContent.DummyItem item) {

    }

    @Override
    public void sendFileFromUriByBT(Uri uri) {

        if (uri == null || uri.toString().length() < 1) {
            Log.e(TAG, "BAD URI: cannot load file");
            return;
        }

        // start send file
        try {
            file = new RandomAccessFile(FileIO.getFile(this, uri), "r");
            if (file != null) {
                int allBytes = 0;
                try {
                    allBytes = (int)file.length();
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                ((SendFileFragment) mSmartFragmentStatePagerAdapter.getRegisteredFragment(0)).initProgressBar(allBytes);
                mSendFileFragment.initProgressBar(allBytes);

                isFileFinished = false;
                isFileStartSend = true;
                initSendBytes();

                byte[] init = {(byte)0xA5, (byte)0x07, (byte)0x0A, (byte)0xC0, (byte)0x0C, (byte)0x0D, (byte)0x5A};
                sendMessage(init);
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
                        Log.d(TAG, mConnectedDeviceName + " & " + mDeviceAddress);
                        mBluetoothLeService.connect(mConnectedDeviceName, mDeviceAddress);
                        connectDevice(data);
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
        }

    }

    private void connectDevice(Intent data) {
        Log.d(TAG, "connecting...");
        if (data != null) {
            mConnectedDeviceName = data.getExtras().getString(MarkBitApplication.DEVICE_NAME);
            mDeviceAddress = data.getExtras().getString(MarkBitApplication.DEVICE_ADDRESS);
            Log.d(TAG, mConnectedDeviceName + ": " + mDeviceAddress);
            // TODO to connect devices
            mBluetoothLeService.connect(mConnectedDeviceName, mDeviceAddress);
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
            address = (recBytes[3] & 0xff) * 255 + (recBytes[4] & 0xff);
        }

        byte[] tmp = new byte[diff];
        int sendLen = diff + 8;
        sendBytes[3] = recBytes[3];
        sendBytes[4] = recBytes[4];

        // file is not finished, so keep sending
        if (isFileStartSend && !isFileFinished) {

//            ((SendFileFragment) mSmartFragmentStatePagerAdapter.getRegisteredFragment(0)).setProgressBarNum((address + 1) * diff);
            mSendFileFragment.setProgressBarNum((address + 1) * diff);

            try {
                file.seek(diff * address);
                int res = file.read(tmp, 0, diff);
                if (res == -1) {
                    isFileFinished = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            sendBytes[sendLen - 3] = getCheckSum(tmp, 0, diff);
            for (int i = 0; i < diff; ++i) {
                sendBytes[i + 5] = tmp[i];
            }

            sendMessage(sendBytes);
        } else if (isFileStartSend && isFileFinished){

//            ((SendFileFragment) mSmartFragmentStatePagerAdapter.getRegisteredFragment(0)).destroyProgressBar();
            mSendFileFragment.destroyProgressBar();

            Toast.makeText(this, getString(R.string.send_file_result_succeed), Toast.LENGTH_SHORT).show();
            isFileStartSend = false;
            isFileFinished = false;
        }
    }

    private void readProcessRx(byte[] recBytes) {
        int length = recBytes[1] & 0xff;
        if (recBytes[0] == (byte) 0xA5 && recBytes[length - 2] == (byte) 0x0D
                && recBytes[length - 1] == (byte) 0x5A) {
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
                    address = (recBytes[3] & 0xff) * 255 + (recBytes[4] & 0xff);
                    address += diff;
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
                    // TODO RX received error package.

                }
            }

            sendMessage(feedbackInstruct);
        } else {
            // TODO RX received error package.
        }
    }

    private void readProcess(byte[] recBytes) {
        // A5 07 8A 00 00 0D 5A

        if (recBytes.length >= 7) {
            switch (recBytes[2]) {
                case (byte) 0x8A:
                    readProcessTx(recBytes);
                    break;
                case (byte) 0x0A:
                    readProcessRx(recBytes);
                    break;
            }
        } else {
            Log.e(TAG, "received recbytes invalid!");
        }
    }

    private void breakAndCheck() {
        if(isFileStartSend && !isFileFinished) {
            byte[] feedbackInstruct = {(byte) 0xA5, (byte) 0x07, (byte) 0x8A, sendBytes[3], sendBytes[4], (byte) 0x0D, (byte) 0x5A};
            readProcess(feedbackInstruct);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    protected void sendMessage(byte[] message) {
        // Check that we're actually connected before trying anything
        if (mBluetoothLeService.getConnectionState() != mBluetoothLeService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
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
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }



            // Reset out string buffer to zero and clear the edit text field
//            mOutStringBuffer.setLength(0);
//            mOutEditText.setText(mOutStringBuffer);
        }
    }

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
}
