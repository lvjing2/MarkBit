package com.liwn.zzl.markbit.tools.implementation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.Log;
import android.widget.EditText;

import com.liwn.zzl.markbit.MarkBitApplication;
import com.liwn.zzl.markbit.R;
import com.liwn.zzl.markbit.command.Command;
import com.liwn.zzl.markbit.command.implementation.TextCommand;
import com.liwn.zzl.markbit.dialog.CustomAlertDialogBuilder;
import com.liwn.zzl.markbit.mark.font.StringValidCheck;
import com.liwn.zzl.markbit.tools.ToolType;

/**
 * Created by liwn on 16-4-26.
 */

// TODO create the text tool
public class TextTool extends BaseTool{
    private static final String TAG = "TextTool";
    private Context mContext;
    private String text;
    private Bitmap bitmap;

    public TextTool(Context context, ToolType toolType) {
        super(context, toolType);

        mContext = context;
//        drawBytes = boolean2Byte(string2Booleans("我"));
    }

    @Override
    public boolean handleDown(PointF coordinate) {
        return false;
    }

    @Override
    public boolean handleMove(PointF coordinate) {
        return false;
    }

    @Override
    public boolean handleUp(PointF coordinate) {
        int bitmapHeight = MarkBitApplication.drawingSurface
                .getBitmapHeight();
        int bitmapWidth = MarkBitApplication.drawingSurface.getBitmapWidth();

        if ((coordinate.x > bitmapWidth) || (coordinate.y > bitmapHeight)
                || (coordinate.x < 0) || (coordinate.y < 0)) {
            return false;
        }

        showDialog();
        return true;
    }

    private void showDialog() {
        AlertDialog.Builder builder = new CustomAlertDialogBuilder(mContext);
        builder.setTitle(R.string.text_input_title);
        final EditText editText = new EditText(mContext);
        editText.setHint(R.string.text_input_hint);
        builder.setView(editText);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                text = editText.getText().toString();
                StringValidCheck validCheck = new StringValidCheck(text);
                if (validCheck.isValid()) {

                    Command command = new TextCommand(text, mBitmapPaint);
                    MarkBitApplication.commandManager.commitCommand(command);

                } else {
                    Log.e(TAG, "text input is not valid!");
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);
        builder.create().show();
    }

    @Override
    public void draw(Canvas canvas) {
//        Log.d(TAG, "draw function");
    }

    @Override
    protected void resetInternalState() {

    }
}
