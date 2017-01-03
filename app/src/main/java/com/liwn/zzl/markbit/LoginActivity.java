package com.liwn.zzl.markbit;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.liwn.zzl.markbit.mark.DummyContent;

import java.io.IOException;
import java.io.InputStream;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int REQUEST_CODE_TWO_PERMISSION = 3;
    private static final String TIMESTAMP = "timestamp";
    private static final String ISFIRSTRUN = "isFirstRun";
    private static final String ISLOGIN = "isLogin";

    // UI references.
    private TextView mUsernameView;
    private EditText mPasswordView;
    private TextView mChangePassView;
    private Context mContext;
//    private TextInputLayout mTextInputLayout;

    private boolean dbClickExit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsernameView = (TextView) findViewById(R.id.username);
        mPasswordView = (EditText) findViewById(R.id.password);
        mChangePassView = (TextView) findViewById(R.id.changePass);
        mContext = this;


        Button mUsernameSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mUsernameSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mChangePassView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent changePassIntent = new Intent(mContext, RegisterActivity.class);
                startActivity(changePassIntent);
            }
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Log.e(TAG, "no storage permission");

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_CODE_TWO_PERMISSION);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            MarkBitApplication.i_file = FileIO.getIconFile();
            MarkBitApplication.r_file = FileIO.getRconFile();

            MarkBitApplication.i_synced = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(MarkBitApplication.I_SYNCED, true);
            MarkBitApplication.r_synced = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(MarkBitApplication.R_SYNCED, true);
            if (!MarkBitApplication.i_file.exists() || !MarkBitApplication.r_file.exists()) {
                Log.d(TAG, "NO BINS FILE!");
                Toast.makeText(MarkBitApplication.applicationContext, R.string.bins_not_import, Toast.LENGTH_LONG).show();
//			return;
            } else {
                MarkBitApplication.dummyContent = new DummyContent();
            }
        }

        boolean isFirstRun = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(ISFIRSTRUN, true);
        boolean isLogin = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(ISLOGIN, false);
        Long lastLoginTimestamp = PreferenceManager.getDefaultSharedPreferences(this).getLong(TIMESTAMP, 0L);
        if (isFirstRun) {
            MarkBitApplication.i_synced = true;
            MarkBitApplication.r_synced = true;
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(MarkBitApplication.I_SYNCED, MarkBitApplication.i_synced).apply();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(MarkBitApplication.R_SYNCED, MarkBitApplication.r_synced).apply();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                copyBinsFromAsset2External();
            } else {
//                Toast.makeText(MainActivity.this, "external storage permission is denied.", Toast.LENGTH_SHORT).show();
            }
        }

        // 如果不是第一次打开app,且24小时内登录过了,就不需要再登录
        if (!isFirstRun && isLogin) {
            if (System.currentTimeMillis() - lastLoginTimestamp < 24 * 3600 * 1000) {
                Intent mainIntent = new Intent(this, MainActivity.class);
                startActivity(mainIntent);
                finish();
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        byte[] validate = new byte[FileIO.PASSWORD_LEN];
        FileIO.getBytes(MarkBitApplication.i_file, validate, FileIO.PASSWORD_ADDR, FileIO.PASSWORD_LEN);
        String validatePass = FileIO.bytesToHexString(validate);
        validatePass = validatePass.replaceAll(" ", "");

        String username = mUsernameView.getText().toString().trim();
        String password = mPasswordView.getText().toString().trim();

        Log.e(TAG, validatePass + " === " + password);
        if (password.equals(validatePass)) {
            Long timestamp = System.currentTimeMillis();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putLong(TIMESTAMP, timestamp).apply();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(ISLOGIN, true).apply();
            Intent mainIntent = new Intent(mContext, MainActivity.class);
            startActivity(mainIntent);
            finish();
        } else {
            mPasswordView.setError(getString(R.string.error_incorrect_password));
            mPasswordView.requestFocus();
        }
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

                    MarkBitApplication.i_synced = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(MarkBitApplication.I_SYNCED, true);
                    MarkBitApplication.r_synced = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(MarkBitApplication.R_SYNCED, true);
                    if (!MarkBitApplication.i_file.exists() || !MarkBitApplication.r_file.exists()) {
                        Log.d(TAG, "NO BINS FILE!");
                        Toast.makeText(MarkBitApplication.applicationContext, R.string.bins_not_import, Toast.LENGTH_LONG).show();
//			return;
                    } else {
                        MarkBitApplication.dummyContent = new DummyContent();
                    }
                } else {
                    //禁止授权
                    Toast.makeText(this, R.string.premission_denied, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "SD or BLE permission granted failed!");
                }
                return;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(ISFIRSTRUN, true)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(ISFIRSTRUN, false).apply();
        }
    }
}

