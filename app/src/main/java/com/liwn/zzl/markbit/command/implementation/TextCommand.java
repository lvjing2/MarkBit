package com.liwn.zzl.markbit.command.implementation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.liwn.zzl.markbit.MarkBitApplication;
import com.liwn.zzl.markbit.mark.font.Str2FixMat;

/**
 * Created by liwn on 16-5-8.
 */
public class TextCommand extends BaseCommand {

    private String text;
    private Paint mPaint;

    public TextCommand(String text, Paint currentPaint) {
        super(currentPaint);
        this.text = text;
        mPaint = currentPaint;
    }

    @Override
    public void run(Canvas canvas, Bitmap bitmap) {

        notifyStatus(NOTIFY_STATES.COMMAND_STARTED);

        string2Bitmap(text, bitmap);

        notifyStatus(NOTIFY_STATES.COMMAND_DONE);
    }

    private void string2Bitmap(String s, Bitmap bitmap) {
        Str2FixMat str2FixMat = new Str2FixMat(s, MarkBitApplication.BIT_LCD_WIDTH, MarkBitApplication.BIT_LCD_HEIGHT, MarkBitApplication.applicationContext);
        boolean[][] strMat = str2FixMat.getMat();

        int color = mPaint.getColor();
        for (int i = 0; i < strMat.length; i++) {
            for (int j = 0; j < strMat[0].length; j++) {
                if (strMat[i][j]) {
                    bitmap.setPixel(j, i, color);
                }
            }
        }
    }

}
