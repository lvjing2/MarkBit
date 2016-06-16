package com.liwn.zzl.markbit.mark.font;

import android.util.Log;

/**
 * Created by liwn on 16-5-8.
 */
public class StringValidCheck {
    private String TAG = "text input valid check";
    private boolean isChinese = true;
    private boolean isDigit = true;
    private boolean isValid;
    private String input;


    public boolean isChinese() {
        return isChinese;
    }

    public void setChinese(boolean chinese) {
        isChinese = chinese;
    }

    public boolean isDigit() {
        return isDigit;
    }

    public void setDigit(boolean digit) {
        isDigit = digit;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public StringValidCheck(String input) {
        this.input = input;
        checkInput();
    }

    private static boolean isChineseChar(char c) {
        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
            return true;
        }
        return false;
    }

    public void checkInput() {
        int length = input.length();

        if (length > 0) {

            // check isDigit or isChinese
            for (int i = 0; i < length; ++i) {
                if (isDigit && Character.isDigit(input.charAt(i))) {
                    isDigit = true;
                } else {
                    isDigit = false;
                }

                if (isChinese && isChineseChar(input.charAt(i))) {
                    isChinese = true;
                } else {
                    isChinese = false;
                }
            }

            if (isDigit && !isChinese) {
                if (length == 2) {
                    isValid = true;
                } else {
                    isValid = false;
                    Log.d(TAG, "the length of digit support only 2!");
                }
            } else if (isChinese && !isDigit) {
                if (length == 1 || length == 4) {
                    isValid = true;
                } else {
                    isValid = false;
                    Log.d(TAG, "the length of the chinese support only 1 or 4!");
                }
            } else {
                isValid = false;
                Log.d(TAG, "the input is neither a digit nor a chinese!");
            }

        } else {
            isValid = false;
            Log.d(TAG, "please input the text.");
        }

    }
}
