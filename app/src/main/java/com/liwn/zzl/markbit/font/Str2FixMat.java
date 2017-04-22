package com.liwn.zzl.markbit.font;

import android.content.Context;
import android.util.Log;

/**
 * Created by liwn on 16-3-2.
 */
public class Str2FixMat {
    private final static String TAG = "string to fixed mat";
    private String input;
    private int fixMat_height;
    private int fixMat_width;
    private Str2Mat sm;
    private boolean[][] screen;


    private boolean isValid = true;
    private boolean isDigit = true;
    private boolean isChinese = true;

    private Context context;

    public Str2FixMat(String input, int fixMat_width, int fixMat_height, Context context) {
        this.input = input;
        this.fixMat_height = fixMat_height;
        this.fixMat_width = fixMat_width;
        this.context = context;

        StringValidCheck validCheck = new StringValidCheck(input);
        isDigit = validCheck.isDigit();
        isChinese = validCheck.isChinese();
        isValid = validCheck.isValid();
    }

    public boolean[][] getMat() {
        int length = input.length();
        if (isValid) {
            if (isDigit && ! isChinese) {
                if (length == 2) {
                    sm = new Str2Mat(input, fixMat_height, fixMat_width / length, 1, 2, isDigit, isChinese, context);
                    screen = sm.getMat();
                } else {
                    Log.d(TAG, "the length of digit support only 2!");
                }
            } else if (isChinese && !isDigit) {
                if (length == 1) {
                    sm = new Str2Mat(input, fixMat_height, fixMat_width, 1, 1, isDigit, isChinese, context);
                    screen = sm.getMat();
                } else if (length == 4) {
                    sm = new Str2Mat(input, fixMat_height / 2, fixMat_width / 2, 2, 2, isDigit, isChinese, context);
                    screen = sm.getMat();
                } else {
                    Log.d(TAG, "the length of the chinese support only 1 or 4!");
                }
            } else {
                Log.d(TAG, "the input is neither a digit nor a chinese!");
            }

            return screen;
        } else {
            Log.d(TAG, "input is invalid! so the screen is null!");
            return null;
        }
    }

    public void printScreen() {
        if (screen == null) {
            Log.d(TAG, "the screen is null!");
            return;
        }

        for (int i = 0; i < fixMat_height; i++) {
            for (int j = 0; j < fixMat_width; j++) {
                System.out.print(screen[i][j] ? "o" : "");
            }
            System.out.println();
        }
    }
}
