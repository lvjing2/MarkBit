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
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.liwn.zzl.markbit.dialog.IndeterminateProgressDialog;
import com.liwn.zzl.markbit.dialog.InfoDialog;
import com.liwn.zzl.markbit.dialog.InfoDialog.DialogType;
import com.liwn.zzl.markbit.tools.Tool.StateChange;
import com.liwn.zzl.markbit.tools.implementation.ImportTool;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public abstract class DrawOptionsMenuActivity extends AppCompatActivity {

    protected static final int REQUEST_CODE_IMPORTPNG = 1;
    protected static final int REQUEST_CODE_LOAD_PICTURE = 2;
    protected static final int REQUEST_CODE_FINISH = 3;
    protected static final int REQUEST_CODE_TAKE_PICTURE = 4;
    protected static final int REQUEST_CODE_CROP = 5;

    public static final float ACTION_BAR_HEIGHT = 50.0f;

    protected boolean loadBitmapFailed = false;

    public static enum ACTION {
        SAVE, CANCEL
    }

    private static Uri mCameraImageUri;

    protected abstract class RunnableWithBitmap {
        public abstract void run(Bitmap bitmap);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_item_save_image:
                SaveTask saveTask = new SaveTask(this);
                saveTask.execute();
                break;
//		case R.id.menu_item_save_copy:
//			MarkBitApplication.saveCopy = true;
//			SaveTask saveCopyTask = new SaveTask(this);
//			saveCopyTask.execute();
//			break;
            case R.id.menu_item_new_image:
//			chooseNewImage();
                onNewImage();
                break;

//		case R.id.menu_item_load_image:
//			onLoadImage();
//			break;

            case R.id.menu_item_load_image_from_uri:
                onLoadImageFromFile();
                break;

            case R.id.menu_item_load_image_from_camera:
                onLoadImageFromCamera();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void onLoadImageFromFile() {

        if (!MarkBitApplication.commandManager.hasCommands()
                && MarkBitApplication.isPlainImage) {
            startLoadImageIntent();
        } else if (MarkBitApplication.isSaved) {
            startLoadImageIntent();
        } else {

            final SaveTask saveTask = new SaveTask(this);

            AlertDialog.Builder alertLoadDialogBuilder = new AlertDialog.Builder(
                    this);
            alertLoadDialogBuilder
                    .setTitle(R.string.menu_load_image)
                    .setMessage(R.string.dialog_warning_new_image)
                    .setCancelable(true)
                    .setPositiveButton(R.string.save_button_text,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    saveTask.execute();
                                    startLoadImageIntent();
                                }
                            })
                    .setNegativeButton(R.string.discard_button_text,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    startLoadImageIntent();
                                }
                            });
            AlertDialog alertLoadImage = alertLoadDialogBuilder.create();
            alertLoadImage.show();
        }
    }

    private void startLoadImageIntent() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivityForResult(intent, REQUEST_CODE_LOAD_PICTURE);
    }

    private void chooseNewImage() {
        AlertDialog.Builder alertChooseNewBuilder = new AlertDialog.Builder(
                this);
        alertChooseNewBuilder.setTitle(R.string.menu_new_image).setItems(
                R.array.new_image, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                onNewImage();
                                break;
                            case 1:
                                onLoadImageFromCamera();
                                break;
                        }
                    }
                });
        AlertDialog alertNew = alertChooseNewBuilder.create();
        alertNew.show();
        return;

    }

    private void onNewImage() {
        if (!MarkBitApplication.commandManager.hasCommands()
                && MarkBitApplication.isPlainImage) {
            initialiseNewBitmap();
        } else if (MarkBitApplication.isSaved) {
            initialiseNewBitmap();
        } else {

            final SaveTask saveTask = new SaveTask(this);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    this);
            alertDialogBuilder
                    .setTitle(R.string.menu_new_image)
                    .setMessage(R.string.dialog_warning_new_image)
                    .setCancelable(true)
                    .setPositiveButton(R.string.save_button_text,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    saveTask.execute();
                                    initialiseNewBitmap();

                                }
                            })
                    .setNegativeButton(R.string.discard_button_text,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    initialiseNewBitmap();
                                }
                            });
            AlertDialog alertNewImage = alertDialogBuilder.create();
            alertNewImage.show();
        }
    }

    private void onLoadImageFromCamera() {
        if (!MarkBitApplication.commandManager.hasCommands()
                && MarkBitApplication.isPlainImage) {
            takePhoto();
        } else if (MarkBitApplication.isSaved) {
            takePhoto();
        } else {

            final SaveTask saveTask = new SaveTask(this);

            AlertDialog.Builder newCameraImageAlertDialogBuilder = new AlertDialog.Builder(
                    this);
            newCameraImageAlertDialogBuilder
                    .setTitle(R.string.menu_new_image_from_camera)
                    .setMessage(R.string.dialog_warning_new_image)
                    .setCancelable(true)
                    .setPositiveButton(R.string.save_button_text,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    saveTask.execute();
                                    takePhoto();
                                }
                            })
                    .setNegativeButton(R.string.discard_button_text,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    takePhoto();
                                }
                            });
            AlertDialog alertNewCameraImage = newCameraImageAlertDialogBuilder
                    .create();
            alertNewCameraImage.show();
        }
    }

    // TODO fix camera rotation bugs in xiaomi smartphone
    private Uri rotateNormal(Uri uri) throws IOException {
        ExifInterface exif = new ExifInterface(uri.getPath());
        int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        int rotationInDegrees = exifToDegrees(rotation);
        Matrix matrix = new Matrix();
        if (rotation != 0) {
            matrix.preRotate(rotationInDegrees);
        }
        Bitmap srcBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        Bitmap adjustedBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, MarkBitApplication.BIT_LCD_WIDTH, MarkBitApplication.BIT_LCD_HEIGHT, matrix, true);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        adjustedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(this.getContentResolver(), adjustedBitmap, "Title", null);
        return Uri.parse(path);
    }

    private static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
            return 90;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
            return 180;
        } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
            return 270;
        }
        return 0;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_LOAD_PICTURE:
                    mCameraImageUri = data.getData();
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(mCameraImageUri));
                        // crop cannot resize images smaller than 50*50
                        if (bitmap.getWidth() > 50 && bitmap.getHeight() > 50) {
                            cropImage(mCameraImageUri);
                            MarkBitApplication.saveCopy = true;
                        } else {
                            loadBitmapFromUri(mCameraImageUri);
                            MarkBitApplication.isPlainImage = false;
                            MarkBitApplication.isSaved = false;
                            MarkBitApplication.savedPictureUri = null;
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                    break;
                case REQUEST_CODE_TAKE_PICTURE:
                    // fixed the rotate bugs for some android device
//                  mCameraImageUri = rotateNormal(mCameraImageUri);
                    cropImage(mCameraImageUri);
                    break;
                case REQUEST_CODE_CROP:
                    if (data != null) {
                        loadBitmapFromUri(data.getData());
                        MarkBitApplication.isPlainImage = false;
                        MarkBitApplication.isSaved = false;
                        MarkBitApplication.savedPictureUri = null;
                    }
                    break;
            }

        }
    }

    private void cropImage(Uri uri) {
        File file = FileIO.getFile(this, uri);
        Uri tmp = Uri.fromFile(file);

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(tmp, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", MarkBitApplication.BIT_LCD_WIDTH);
        intent.putExtra("outputY", MarkBitApplication.BIT_LCD_HEIGHT);
        intent.putExtra("scale", true);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        intent.putExtra("noFaceDetection", true);
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        startActivityForResult(intent, REQUEST_CODE_CROP);
    }

    protected void takePhoto() {
        File tempFile = FileIO.createNewEmptyPictureFile(DrawOptionsMenuActivity.this, FileIO.getDefaultFileName());
        if (tempFile != null) {
            mCameraImageUri = Uri.fromFile(tempFile);
        }
        if (mCameraImageUri == null) {
            new InfoDialog(DialogType.WARNING,
                    R.string.dialog_error_sdcard_text,
                    R.string.dialog_error_save_title).show(
                    getSupportFragmentManager(), "savedialogerror");
            return;
        }
//        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//        startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
    }

    protected void loadBitmapFromUriAndRun(final Uri uri,
                                           final RunnableWithBitmap runnable) {
        String loadMessge = getResources().getString(R.string.dialog_load);
        final ProgressDialog dialog = ProgressDialog.show(
                DrawOptionsMenuActivity.this, "", loadMessge, true);

        Thread thread = new Thread("loadBitmapFromUriAndRun") {
            @Override
            public void run() {
                Bitmap bitmap = null;
                try {
                    bitmap = FileIO.getBitmapFromUri(uri);
                } catch (Exception e) {
                    loadBitmapFailed = true;
                }

                if (bitmap != null) {
                    runnable.run(bitmap);
                } else {
                    loadBitmapFailed = true;
                }
                dialog.dismiss();
                MarkBitApplication.currentTool
                        .resetInternalState(StateChange.NEW_IMAGE_LOADED);
                if (loadBitmapFailed) {
                    loadBitmapFailed = false;
                    new InfoDialog(DialogType.WARNING,
                            R.string.dialog_loading_image_failed_title,
                            R.string.dialog_loading_image_failed_text).show(
                            getSupportFragmentManager(),
                            "loadbitmapdialogerror");
                } else {
                    if (!(MarkBitApplication.currentTool instanceof ImportTool)) {
                        MarkBitApplication.savedPictureUri = uri;
                    }
                }
            }
        };
        thread.start();
    }

    // if needed use Async Task
    public void saveFile() {

        Bitmap bitmap = MarkBitApplication.drawingSurface.getBitmapCopy();
        if (!FileIO.saveBitmap(this, bitmap)) {

            // save failed!
            new InfoDialog(DialogType.WARNING,
                    R.string.dialog_error_sdcard_text,
                    R.string.dialog_error_save_title).show(
                    getSupportFragmentManager(), "savedialogerror");
        }

        MarkBitApplication.isSaved = true;
    }

    protected void loadBitmapFromUri(Uri uri) {
        if (uri == null || uri.toString().length() < 1) {
            Log.e(MarkBitApplication.TAG, "BAD URI: cannot load image");
            return;
        }

        loadBitmapFromUriAndRun(uri, new RunnableWithBitmap() {
            @Override
            public void run(Bitmap bitmap) {
                MarkBitApplication.drawingSurface.resetBitmap(bitmap);
                MarkBitApplication.perspective.resetScaleAndTranslation();
            }
        });
    }

    protected void initialiseWithMarkPath(String markPath) {
//        File file = FileIO.getFileByPath(markPath);
        File file = new File(markPath);

        if (file != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(markPath);
            initialiseWithBitmap(bitmap.copy(Config.ARGB_8888, true));
        } else {
            Log.e("Initialise by Mark ID: ", "faild for file is null");
            initialiseNewBitmap();
        }
    }

    protected void initialiseWithBitmap(Bitmap bitmap) {
        MarkBitApplication.drawingSurface.resetBitmap(bitmap);
        MarkBitApplication.perspective.resetScaleAndTranslation();
        MarkBitApplication.currentTool
                .resetInternalState(StateChange.NEW_IMAGE_LOADED);
        MarkBitApplication.isPlainImage = true;
        MarkBitApplication.isSaved = false;
        MarkBitApplication.savedPictureUri = null;
    }

    protected void initialiseNewBitmap() {
        float width = MarkBitApplication.BIT_LCD_WIDTH;
        float height = MarkBitApplication.BIT_LCD_HEIGHT;
        Log.d("PAINTROID - MFA", "init new bitmap with: w: " + width + " h:"
                + height);
        Bitmap bitmap = Bitmap.createBitmap((int) width, (int) height,
                Config.ARGB_8888);
        bitmap.eraseColor(Color.TRANSPARENT);
        initialiseWithBitmap(bitmap);
    }

    protected class SaveTask extends AsyncTask<String, Void, Void> {

        private DrawOptionsMenuActivity context;

        public SaveTask(DrawOptionsMenuActivity context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            IndeterminateProgressDialog.getInstance().show();
            Log.d(MarkBitApplication.TAG, "async tast prgDialog isShowing"
                    + IndeterminateProgressDialog.getInstance().isShowing());
        }

        @Override
        protected Void doInBackground(String... arg0) {
            saveFile();
            return null;
        }

        @Override
        protected void onPostExecute(Void Result) {
            IndeterminateProgressDialog.getInstance().dismiss();
            if (!MarkBitApplication.saveCopy) {
                Toast.makeText(context, R.string.saved, Toast.LENGTH_LONG)
                        .show();
            } else {
                if (MarkBitApplication.drawMenu.findItem(R.id.menu_item_save_image) != null) {
                    MarkBitApplication.drawMenu.findItem(R.id.menu_item_save_image).setVisible(true);
                }
                Toast.makeText(context, R.string.copy, Toast.LENGTH_LONG)
                        .show();
                MarkBitApplication.saveCopy = false;
            }
        }
    }
}
