package com.liwn.zzl.markbit;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A login screen that offers login via username/password.
 */
public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = RegisterActivity.class.getSimpleName();
    private static final int REQUEST_CODE_TWO_PERMISSION = 3;
    private static final String TIMESTAMP = "timestamp";

    // UI references.
    private EditText mOldPassView;
    private EditText mNewPassView;
    private EditText mReNewPassView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.
        mOldPassView = (EditText) findViewById(R.id.oldpassword);
        mNewPassView = (EditText) findViewById(R.id.newpassword);
        mReNewPassView = (EditText) findViewById(R.id.renewpassword);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        Button mChangePassButton = (Button) findViewById(R.id.changePass_button);
        mChangePassButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                changePass();
            }
        });


    }

    private void changePass() {
        byte[] validate = new byte[FileIO.PASSWORD_LEN];
        FileIO.getBytes(MarkBitApplication.i_file, validate, FileIO.PASSWORD_ADDR, FileIO.PASSWORD_LEN);
        String password = FileIO.bytesToHexString(validate);
        password = password.replaceAll(" ", "");

        String oldPass = mOldPassView.getText().toString().trim();
        String newPass = mNewPassView.getText().toString().trim();
        String reNewPass = mReNewPassView.getText().toString().trim();

        Log.e(TAG, oldPass + " === " + newPass);
        boolean isValid = true;
        if (!oldPass.equals(password)) {
            mOldPassView.setError(getString(R.string.error_incorrect_password));
            mOldPassView.requestFocus();
            isValid = false;
        }

        if (!isValid(newPass)) {
            mNewPassView.setError(getString(R.string.error_invalid_new_password));
            mNewPassView.requestFocus();
            isValid = false;
        }

        if (!newPass.equals(reNewPass)) {
            mReNewPassView.setError(getString(R.string.error_invalid_renew_password));
            mReNewPassView.requestFocus();
            isValid = false;
        }
        if (!isValid) {
            return;
        }

        byte[] byteNewPass = FileIO.hexStringToByteArray(newPass);
        FileIO.setBytes(MarkBitApplication.i_file, FileIO.PASSWORD_ADDR, FileIO.PASSWORD_LEN, byteNewPass);
        Toast.makeText(this, "change password successed!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private boolean isValid (@NonNull String str) {
        int len = str.length();
        if (len != 8) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            Character c = str.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("isFirstRun", true)) {
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean("isFirstRun", false).apply();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);

    }
}

