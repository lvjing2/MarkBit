/**
 * Paintroid: An image manipulation application for Android.
 * Copyright (C) 2010-2015 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.liwn.zzl.markbit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.liwn.zzl.markbit.dialog.BrushPickerDialog;
import com.liwn.zzl.markbit.dialog.CustomAlertDialogBuilder;
import com.liwn.zzl.markbit.dialog.IndeterminateProgressDialog;
import com.liwn.zzl.markbit.dialog.InfoDialog;
import com.liwn.zzl.markbit.dialog.InfoDialog.DialogType;
import com.liwn.zzl.markbit.dialog.ToolsDialog;
import com.liwn.zzl.markbit.dialog.colorpicker.ColorPickerDialog;
import com.liwn.zzl.markbit.listener.DrawingSurfaceListener;
import com.liwn.zzl.markbit.tools.Tool;
import com.liwn.zzl.markbit.tools.ToolFactory;
import com.liwn.zzl.markbit.tools.ToolType;
import com.liwn.zzl.markbit.tools.implementation.ImportTool;
import com.liwn.zzl.markbit.ui.BottomBar;
import com.liwn.zzl.markbit.ui.DrawTopBar;
import com.liwn.zzl.markbit.ui.DrawingSurface;
import com.liwn.zzl.markbit.ui.Perspective;

public class DrawActivity extends DrawOptionsMenuActivity {

    public static final String EXTRA_INSTANCE_FROM_CATROBAT = "EXTRA_INSTANCE_FROM_CATROBAT";
    public static final String EXTRA_ACTION_BAR_HEIGHT = "EXTRA_ACTION_BAR_HEIGHT";
    protected DrawingSurfaceListener mDrawingSurfaceListener;
    protected DrawTopBar mDrawTopBar;
    protected BottomBar mBottomBar;

    protected boolean mToolbarIsVisible = true;
    private Menu mMenu = null;

    private String drawText = null;

    public String getDrawText() {
        return drawText;
    }

    public void setDrawText(String drawText) {
        this.drawText = drawText;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        ColorPickerDialog.init(this);
        BrushPickerDialog.init(this);
        ToolsDialog.init(this);
        IndeterminateProgressDialog.init(this);

        /**
         * EXCLUDED PREFERENCES FOR RELEASE /*SharedPreferences
         * sharedPreferences = PreferenceManager
         * .getDefaultSharedPreferences(this); String languageString =
         * sharedPreferences.getString(
         * getString(R.string.preferences_language_key), "nolang");
         *
         * if (languageString.equals("nolang")) {
         * Log.e(MarkBitApplication.TAG, "no language preference exists"); }
         * else { Log.i(MarkBitApplication.TAG, "load language: " +
         * languageString); Configuration config =
         * getBaseContext().getResources() .getConfiguration(); config.locale =
         * new Locale(languageString);
         * getBaseContext().getResources().updateConfiguration(config,
         * getBaseContext().getResources().getDisplayMetrics()); }
         */

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        // setDefaultPreferences();
        initActionBar();

        MarkBitApplication.drawingSurface = (DrawingSurface) findViewById(R.id.drawingSurfaceView);
        MarkBitApplication.previewImageView = (ImageView) findViewById(R.id.markPreviewImage);
        MarkBitApplication.perspective = new Perspective(
                ((SurfaceView) MarkBitApplication.drawingSurface).getHolder());
        mDrawingSurfaceListener = new DrawingSurfaceListener();
        mDrawTopBar = new DrawTopBar(this);
        mBottomBar = new BottomBar(this);

        MarkBitApplication.drawingSurface
                .setOnTouchListener(mDrawingSurfaceListener);

        // Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);

        int baseMarkID = -2;
        String baseMarkPath = null;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("baseMarkID")) {
                baseMarkID = extras.getInt("baseMarkID");
            }
            if (extras.containsKey("baseMarkPath")) {
                baseMarkPath = extras.getString("baseMarkPath");
            }
            if (baseMarkPath != null) {
                initialiseWithMarkPath(baseMarkPath);
            } else {
                initialiseNewBitmap();
            }
        } else {
            initialiseNewBitmap();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkIfLoadBitmapFailed();
    }

    public void checkIfLoadBitmapFailed() {
        if (loadBitmapFailed) {
            loadBitmapFailed = false;
            new InfoDialog(DialogType.WARNING,
                    R.string.dialog_loading_image_failed_title,
                    R.string.dialog_loading_image_failed_text).show(
                    getSupportFragmentManager(), "loadbitmapdialogerror");
        }
    }

    private void initActionBar() {

        getActionBar().setCustomView(R.layout.top_bar);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayShowCustomEnabled(true);
        if (Build.VERSION.SDK_INT < MarkBitApplication.ANDROID_VERSION_ICE_CREAM_SANDWICH) {
            Bitmap bitmapActionBarBackground = Bitmap.createBitmap(1, 1,
                    Config.ARGB_8888);
            bitmapActionBarBackground.eraseColor(getResources().getColor(
                    R.color.custom_background_color));
            Drawable drawable = new BitmapDrawable(getResources(),
                    bitmapActionBarBackground);
            getActionBar().setBackgroundDrawable(drawable);
            getActionBar().setSplitBackgroundDrawable(drawable);
        }
    }

    @Override
    public void onDetachedFromWindow() {
        IndeterminateProgressDialog.getInstance().dismiss();
        super.onDetachedFromWindow();
    }

    @Override
    protected void onDestroy() {

        MarkBitApplication.commandManager.resetAndClear();
        MarkBitApplication.drawingSurface.recycleBitmap();
//		ColorPickerDialog.getInstance().setInitialColor(
//				getResources().getColor(R.color.color_chooser_red));
        ColorPickerDialog.getInstance().setInitialColor(Color.RED);
        MarkBitApplication.currentTool.changePaintStrokeCap(Cap.ROUND);
        MarkBitApplication.currentTool.changePaintStrokeWidth(1);
        MarkBitApplication.isPlainImage = true;
        MarkBitApplication.savedPictureUri = null;
        MarkBitApplication.saveCopy = false;

        ToolsDialog.getInstance().dismiss();
        IndeterminateProgressDialog.getInstance().dismiss();
        ColorPickerDialog.getInstance().dismiss();
        // BrushPickerDialog.getInstance().dismiss(); // TODO: how can there
        // ever be a null pointer exception?
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        mMenu = menu;
        MarkBitApplication.drawMenu = mMenu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.draw_menu, menu);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_hide_menu:
                setFullScreen(mToolbarIsVisible);
                return true;
            case android.R.id.home:
                showSecurityQuestionBeforeExit();
                return true;
            /* EXCLUDE PREFERENCES FOR RELEASE */
            // case R.id.menu_item_preferences:
            // Intent intent = new Intent(this, SettingsActivity.class);
            // intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            // startActivity(intent);
            // return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mToolbarIsVisible == false) {
            setFullScreen(false);
            return true;
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (!mToolbarIsVisible) {
            setFullScreen(false);

        } else if (MarkBitApplication.currentTool.getToolType() == ToolType.BRUSH) {
            showSecurityQuestionBeforeExit();
        } else {
            switchTool(ToolType.BRUSH);
        }
    }

    private Bitmap overlay(Bitmap outPreview, Bitmap mark) {
        int bW = outPreview.getWidth();
        int bH = outPreview.getHeight();


        int scaleRatio = bW / MarkBitApplication.ALL_LCD_WIDTH;
        Bitmap scaledMark = Bitmap.createScaledBitmap(mark, scaleRatio * mark.getWidth(), scaleRatio * mark.getHeight(), false);

        int lW = scaledMark.getWidth();
        int lH = scaledMark.getHeight();

        int lX = (bW - lW) / 2;
        int lY = (bH - lH) / 2;
        Bitmap newBitmap = Bitmap.createBitmap(bW, bH, outPreview.getConfig());
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(outPreview, new Matrix(), null);
        canvas.drawBitmap(scaledMark, lX, lY, null);
        return newBitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                Bitmap allPreview = overlay(MarkBitApplication.outPreview, MarkBitApplication.drawingSurface.getmWorkingBitmap());
                MarkBitApplication.previewImageView.setImageBitmap(allPreview);
        }

        return super.onTouchEvent(event);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            Log.d(MarkBitApplication.TAG,
                    "onActivityResult: result not ok, most likely a dialog hast been canceled");
            return;
        }

        switch (requestCode) {
            case REQUEST_CODE_IMPORTPNG:
                Uri selectedGalleryImageUri = data.getData();
                Tool tool = ToolFactory.createTool(this, ToolType.IMPORTPNG);
                switchTool(tool);

                loadBitmapFromUriAndRun(selectedGalleryImageUri,
                        new RunnableWithBitmap() {
                            @Override
                            public void run(Bitmap bitmap) {
                                if (MarkBitApplication.currentTool instanceof ImportTool) {
                                    ((ImportTool) MarkBitApplication.currentTool)
                                            .setBitmapFromFile(bitmap);

                                } else {
                                    Log.e(MarkBitApplication.TAG,
                                            "importPngToFloatingBox: Current tool is no ImportTool as required");
                                }
                            }
                        });

                break;
            case REQUEST_CODE_FINISH:
                finish();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void importPng() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivityForResult(intent, REQUEST_CODE_IMPORTPNG);
    }

    public synchronized void switchTool(ToolType changeToToolType) {

        switch (changeToToolType) {
            case REDO:
                MarkBitApplication.commandManager.redo();
                break;
            case UNDO:
                MarkBitApplication.commandManager.undo();
                break;
            case IMPORTPNG:
                importPng();
                break;
            default:
                Tool tool = ToolFactory.createTool(this, changeToToolType);
                switchTool(tool);
                break;
        }

    }

    public synchronized void switchTool(Tool tool) {
        Paint tempPaint = new Paint(
                MarkBitApplication.currentTool.getDrawPaint());
        if (tool != null) {
            mDrawTopBar.setTool(tool);
            mBottomBar.setTool(tool);
            MarkBitApplication.currentTool = tool;
            MarkBitApplication.currentTool.setDrawPaint(tempPaint);
        }
    }

    private void showSecurityQuestionBeforeExit() {
        if (MarkBitApplication.isSaved
                || !MarkBitApplication.commandManager.hasCommands()
                && MarkBitApplication.isPlainImage) {
            finish();
            return;
        } else {
            AlertDialog.Builder builder = new CustomAlertDialogBuilder(this);
            builder.setTitle(R.string.closing_security_question_title);
            builder.setMessage(R.string.closing_security_question);
            builder.setPositiveButton(R.string.save_button_text,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            saveFileBeforeExit();
                            finish();
                        }
                    });
            builder.setNegativeButton(R.string.discard_button_text,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });

            builder.setCancelable(true);
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    private void saveFileBeforeExit() {
        saveFile();
    }

    private void setFullScreen(boolean isFullScreen) {
        MarkBitApplication.perspective.setFullscreen(isFullScreen);
        if (isFullScreen) {
            getActionBar().hide();
            LinearLayout bottomBarLayout = (LinearLayout) findViewById(R.id.main_bottom_bar);
            bottomBarLayout.setVisibility(View.GONE);
            mToolbarIsVisible = false;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            getActionBar().show();
            LinearLayout bottomBarLayout = (LinearLayout) findViewById(R.id.main_bottom_bar);
            bottomBarLayout.setVisibility(View.VISIBLE);
            mToolbarIsVisible = true;
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

	/* EXCLUDE PREFERENCES FOR RELEASE */
    // private void setDefaultPreferences() {
    // PreferenceManager
    // .setDefaultValues(this, R.xml.preferences_tools, false);
    // }

}
