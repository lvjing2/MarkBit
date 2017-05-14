package com.liwn.zzl.markbit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.liwn.zzl.markbit.font.Num2Mat;
import com.liwn.zzl.markbit.font.Str2FixMat;
import com.liwn.zzl.markbit.font.StringValidCheck;
import com.liwn.zzl.markbit.mark.GetBitmap;

public class DrawActivity extends AppCompatActivity {
    private int old_position_id;
    private int old_control_id;
    private ImageView markPreview;
    private EditText markText;
    private SwitchCompat colorSwitch;
    private Button preview;
    private Button submit;
    private String previousText;
    private boolean isSaved;
    private AlertDialog dialog;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        getSupportActionBar().setTitle(R.string.modify_mark_title);


        mContext = this;
        isSaved = true;

        markPreview = (ImageView) findViewById(R.id.markPreview);
        markText = (EditText) findViewById(R.id.mark_text);
        colorSwitch = (SwitchCompat) findViewById(R.id.color_switch);
        preview = (Button) findViewById(R.id.btn_preview);
        submit = (Button) findViewById(R.id.modify_mark_submit);
        markPreview.setImageBitmap(new GetBitmap().getBitmap(null, false));
        markText.requestFocus();

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            old_position_id = extras.getInt(MarkItemFragment.OLD_POS_ID);
            old_control_id = extras.getInt(MarkItemFragment.OLD_CTL_ID);
//            Log.e(TAG, "extras bundle is not null");
        }
        Log.e("old position id: ", "" + old_position_id);

        colorSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = markText.getText().toString();
                Str2FixMat str2FixMat = new Str2FixMat(text, MarkBitApplication.BIT_LCD_WIDTH, MarkBitApplication.BIT_LCD_HEIGHT, mContext);

                boolean[][] mat = str2FixMat.getMat();
                Bitmap bitmap = new GetBitmap().getBitmap(mat, colorSwitch.isChecked());
                markPreview.setImageBitmap(bitmap);
            }
        });

        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = markText.getText().toString();
                boolean isChecked = colorSwitch.isChecked();

                Str2FixMat str2FixMat = new Str2FixMat(text, MarkBitApplication.BIT_LCD_WIDTH, MarkBitApplication.BIT_LCD_HEIGHT, mContext);

                boolean[][] mat = str2FixMat.getMat();
                Bitmap bitmap = new GetBitmap().getBitmap(mat, isChecked);
                markPreview.setImageBitmap(bitmap);
            }
        });

        submit.setEnabled(false);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Str2FixMat str2FixMat = new Str2FixMat(markText.getText().toString(), MarkBitApplication.BIT_LCD_WIDTH, MarkBitApplication.BIT_LCD_HEIGHT, mContext);
                boolean[][] mat = str2FixMat.getMat();
                GetBitmap getBitmap = new GetBitmap();
                Bitmap bitmap = getBitmap.getBitmap(mat, colorSwitch.isChecked());
                markPreview.setImageBitmap(bitmap);
                GetBitmap.saveBitMatrix(mat, colorSwitch.isChecked() ? 0 : 1, old_control_id);

                Toast.makeText(v.getContext(), R.string.modify_save_successfully, Toast.LENGTH_LONG).show();
                isSaved = true;
                //TODO: save
                submit.setEnabled(false);
            }
        });

        markText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isSaved = false;
                submit.setEnabled(true);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        colorSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSaved = false;
                submit.setEnabled(true);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (isSaved) {
            Intent i = new Intent();
            int new_position_id = old_position_id;
            int new_control_id = old_control_id;
            i.putExtra(MarkItemFragment.OLD_POS_ID, old_position_id);
            i.putExtra(MarkItemFragment.OLD_CTL_ID, old_control_id);
            i.putExtra(MarkItemFragment.NEW_POS_ID, old_position_id);
            i.putExtra(MarkItemFragment.NEW_CTL_ID, new_control_id);
            this.setResult(Activity.RESULT_OK, i);
            finish();
        } else {
            Intent i = new Intent();
            this.setResult(Activity.RESULT_CANCELED, i);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.save_notification_title)
                    .setMessage(R.string.save_notification_message)
                    .setPositiveButton(R.string.drop_the_edit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.back_to_save, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
            dialog = builder.setCancelable(true).create();
            dialog.show();
        }
    }

    @Override
    protected void onDestroy() {
        if (dialog != null) {
            dialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // todo: goto back activity from here

                Intent i = new Intent();
                int new_position_id = old_position_id;
                int new_control_id = old_control_id;
                i.putExtra(MarkItemFragment.OLD_POS_ID, old_position_id);
                i.putExtra(MarkItemFragment.OLD_CTL_ID, old_control_id);
                i.putExtra(MarkItemFragment.NEW_POS_ID, old_position_id);
                i.putExtra(MarkItemFragment.NEW_CTL_ID, new_control_id);
                this.setResult(Activity.RESULT_OK, i);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
