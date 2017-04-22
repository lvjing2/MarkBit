package com.liwn.zzl.markbit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;

import com.liwn.zzl.markbit.font.Num2Mat;
import com.liwn.zzl.markbit.font.Str2FixMat;
import com.liwn.zzl.markbit.font.StringValidCheck;
import com.liwn.zzl.markbit.mark.GetBitmap;

public class DrawActivity extends AppCompatActivity {
    private int old_position_id;
    private ImageView markPreview;
    private EditText markText;
    private Switch colorSwitch;
    private Button submit;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        mContext = this;

        markPreview = (ImageView) findViewById(R.id.markPreview);
        markText = (EditText) findViewById(R.id.mark_text);
        colorSwitch = (Switch) findViewById(R.id.color_switch);
        submit = (Button) findViewById(R.id.modify_mark_submit);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            old_position_id = extras.getInt(MarkItemFragment.OLD_POS_ID);
//            Log.e(TAG, "extras bundle is not null");
        }
        Log.e("old position id: ", "" + old_position_id);

        submit.setOnClickListener(new View.OnClickListener() {
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
    }




}
