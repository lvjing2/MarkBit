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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("NewApi")
public abstract class FileIO {

    private static final String TAG = FileIO.class.getSimpleName();
    private static File MEDIA_FILE = null;
    private static final int BUFFER_SIZE = 1024;
    public static final String default_prefix = "mark";
    public static final String default_num = "-1";
    public static final String default_date = "date";
    public static final String default_tag = "tag";
    public static String default_file_name = default_prefix + "_" + default_num + "_" + default_date + "_" + default_tag;
    private static final String DEFAULT_FILENAME_TIME_FORMAT = "yyyy-MM-dd-hhmmss";
    private static final String ENDING = ".png";

    private FileIO() {
    }

    public static void deleteAllFiles() {
        for (File file : getFiles()) {
            file.delete();
        }
    }

    public static String getMediaFolderName() {
        if(MEDIA_FILE == null){
            if(initialisePaintroidMediaDirectory() == true) {
                return MEDIA_FILE.getAbsolutePath();
            } else {
                return null;
            }
        }
        return MEDIA_FILE.getAbsolutePath();
    }

    public static Uri getBaseUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public static boolean saveBitmap(Context context, Bitmap bitmap) {
        return saveBitmap(context, bitmap, null);
    }

    public static boolean saveBitmap(Context context, Bitmap bitmap, String path) {
        if (initialisePaintroidMediaDirectory() == false) {
            return false;
        }

        final int QUALITY = 100;
        final Bitmap.CompressFormat FORMAT = Bitmap.CompressFormat.PNG;
        OutputStream outputStream = null;
        File file = null;

        try {
            if (bitmap == null || bitmap.isRecycled()) {
                Log.e(MarkBitApplication.TAG, "ERROR saving bitmap. ");
                return false;
            } else if (path != null) {
                file = new File(path);
                outputStream = new FileOutputStream(file);
            } else if (MarkBitApplication.savedPictureUri != null
                    && !MarkBitApplication.saveCopy) {
                outputStream = context.getContentResolver().openOutputStream(
                        MarkBitApplication.savedPictureUri);
            } else {
                file = createNewEmptyPictureFile(context);
                outputStream = new FileOutputStream(file);
            }
        } catch (FileNotFoundException e) {
            Log.e(MarkBitApplication.TAG,
                    "ERROR writing image file. File not found. Path: " + path,
                    e);
            return false;
        }

        if (outputStream != null) {
            boolean isSaved = bitmap.compress(FORMAT, QUALITY, outputStream);
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (isSaved) {
                if (file != null) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DATA,
                            file.getAbsolutePath());

                    MarkBitApplication.savedPictureUri = context
                            .getContentResolver().insert(getBaseUri(),
                                    contentValues);
                }
            } else {
                Log.e(MarkBitApplication.TAG,
                        "ERROR writing image file. Bitmap compress didn't work. ");
                return false;
            }

        }
        return true;
    }

    public static String getDefaultFileNameByPrefix(String fileName) {
        String preFileName = fileName.split("\\.")[0];

        StringBuilder stringBuilder = new StringBuilder(preFileName);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_FILENAME_TIME_FORMAT);
        stringBuilder.append("_");
        stringBuilder.append(simpleDateFormat.format(new Date()));
        stringBuilder.append("_");
        stringBuilder.append(default_tag);

        return stringBuilder.toString() + ENDING;
    }

    public static String getDefaultFileName() {
        StringBuilder stringBuilder = new StringBuilder();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
                DEFAULT_FILENAME_TIME_FORMAT);
        String[] fileNameSplit = default_file_name.split("_");
        for (String nameSplit : fileNameSplit) {
            if (nameSplit.equals(default_date)) {
                stringBuilder.append(simpleDateFormat.format(new Date()));
            } else {
                stringBuilder.append(nameSplit);
            }
            stringBuilder.append("_");
        }
        if (stringBuilder.length() >= 1)
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString() + ENDING;
    }

    public static File createNewEmptyPictureFile(Context context,
                                                 String filename) {
        if (initialisePaintroidMediaDirectory() == true) {
            if (!filename.toLowerCase().endsWith(ENDING.toLowerCase())) {
                filename += ENDING;
            }
            return new File(MEDIA_FILE, filename);
        } else {
            return null;
        }
    }

    public static File createNewEmptyPictureFile(Context context) {
        return createNewEmptyPictureFile(context, getDefaultFileName());
    }

    public static String getRealPathFromURI(Context context, Uri imageUri) {
        String path = null;
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = context.getContentResolver().query(imageUri,
                filePathColumn, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            path = cursor.getString(columnIndex);
        } else {
            try {
                File file = new File(new java.net.URI(imageUri.toString()));
                path = file.getAbsolutePath();
            } catch (URISyntaxException e) {
                Log.e("PAINTROID", "URI ERROR ", e);
            }
        }

        return path;
    }

    private static boolean initialisePaintroidMediaDirectory() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            MEDIA_FILE = new File(
                    Environment.getExternalStorageDirectory(),
                    "/"
                            + MarkBitApplication.applicationContext
                            .getString(R.string.ext_storage_directory_name) + "/");
        } else {
            return false;
        }
        if (MEDIA_FILE != null) {
            if (MEDIA_FILE.isDirectory() == false) {

                return MEDIA_FILE.mkdirs();
            }
        } else {
            return false;
        }
        return true;
    }

    public static Bitmap getBitmapFromUri(Uri bitmapUri) {
        BitmapFactory.Options options = new BitmapFactory.Options();

//		TODO: special treatment necessary?
//		if (MarkBitApplication.openedFromCatroid) {
//			try {
//				InputStream inputStream = MarkBitApplication.applicationContext
//						.getContentResolver().openInputStream(bitmapUri);
//				Bitmap immutableBitmap = BitmapFactory
//						.decodeStream(inputStream);
//				inputStream.close();
//				return immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}

        options.inJustDecodeBounds = true;

        try {
            InputStream inputStream = MarkBitApplication.applicationContext
                    .getContentResolver().openInputStream(bitmapUri);
            BitmapFactory.decodeStream(inputStream, null, options);
            inputStream.close();
        } catch (Exception e) {
            return null;
        }

        int tmpWidth = options.outWidth;
        int tmpHeight = options.outHeight;
        int sampleSize = 1;

        if (!MarkBitApplication.openedFromCatroid) {
            DisplayMetrics metrics = new DisplayMetrics();
            Display display = ((WindowManager) MarkBitApplication.applicationContext
                    .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            display.getMetrics(metrics);
            int maxWidth = display.getWidth();
            int maxHeight = display.getHeight();

            while (tmpWidth > maxWidth || tmpHeight > maxHeight) {
                tmpWidth /= 2;
                tmpHeight /= 2;
                sampleSize *= 2;
            }
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;

        Bitmap immutableBitmap;
        try {
            InputStream inputStream = MarkBitApplication.applicationContext
                    .getContentResolver().openInputStream(bitmapUri);
            immutableBitmap = BitmapFactory.decodeStream(inputStream, null,
                    options);
            inputStream.close();
        } catch (Exception e) {
            return null;
        }

        tmpWidth = immutableBitmap.getWidth();
        tmpHeight = immutableBitmap.getHeight();
        int[] tmpPixels = new int[tmpWidth * tmpHeight];
        immutableBitmap.getPixels(tmpPixels, 0, tmpWidth, 0, 0, tmpWidth,
                tmpHeight);

        Bitmap mutableBitmap = Bitmap.createBitmap(tmpWidth, tmpHeight,
                Bitmap.Config.ARGB_8888);
        mutableBitmap.setPixels(tmpPixels, 0, tmpWidth, 0, 0, tmpWidth,
                tmpHeight);

        return mutableBitmap;
    }

    public static Bitmap getBitmapFromFile(File bitmapFile) {

        BitmapFactory.Options options = new BitmapFactory.Options();

        if (MarkBitApplication.openedFromCatroid) {
            options.inJustDecodeBounds = false;
            Bitmap immutableBitmap = BitmapFactory.decodeFile(
                    bitmapFile.getAbsolutePath(), options);
            return immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
        }

        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(bitmapFile.getAbsolutePath(), options);

        int tmpWidth = options.outWidth;
        int tmpHeight = options.outHeight;
        int sampleSize = 1;

        DisplayMetrics metrics = new DisplayMetrics();
        Display display = ((WindowManager) MarkBitApplication.applicationContext
                .getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        display.getMetrics(metrics);
        int maxWidth = display.getWidth();
        int maxHeight = display.getHeight();

        while (tmpWidth > maxWidth || tmpHeight > maxHeight) {
            tmpWidth /= 2;
            tmpHeight /= 2;
            sampleSize *= 2;
        }

        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;

        Bitmap immutableBitmap = BitmapFactory.decodeFile(
                bitmapFile.getAbsolutePath(), options);

        tmpWidth = immutableBitmap.getWidth();
        tmpHeight = immutableBitmap.getHeight();
        int[] tmpPixels = new int[tmpWidth * tmpHeight];
        immutableBitmap.getPixels(tmpPixels, 0, tmpWidth, 0, 0, tmpWidth,
                tmpHeight);

        Bitmap mutableBitmap = Bitmap.createBitmap(tmpWidth, tmpHeight,
                Bitmap.Config.ARGB_8888);
        mutableBitmap.setPixels(tmpPixels, 0, tmpWidth, 0, 0, tmpWidth,
                tmpHeight);

        return mutableBitmap;
    }

    public static String createFilePathFromUri(Activity activity, Uri uri) {
        // Problem here
        String filepath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity
                .managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            filepath = cursor.getString(columnIndex);
        }

        if (filepath == null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String id = uri.getLastPathSegment().split(":")[1];
            final String[] imageColumns = {MediaStore.Images.Media.DATA};
            final String imageOrderBy = null;

            String state = Environment.getExternalStorageState();
            if (!state.equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
                uri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
            }
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            cursor = activity.managedQuery(uri, imageColumns,
                    MediaStore.Images.Media._ID + "=" + id, null, imageOrderBy);

            if (cursor.moveToFirst()) {
                filepath = cursor.getString(cursor
                        .getColumnIndex(MediaStore.Images.Media.DATA));
            }

        } else if (filepath == null) {
            filepath = uri.getPath();
        }
        return filepath;
    }

    public static void copyStream(InputStream inputStream,
                                  OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }
    }

    public static void copyStream(Context context, InputStream inputStream) throws IOException {
        File outFile = createNewEmptyPictureFile(context, getDefaultFileName());
        OutputStream outputStream = new FileOutputStream(outFile);
        copyStream(inputStream, outputStream);
    }

    public static void copyStream(Context context, InputStream inputStream, String fileNamePrefix) throws IOException {
        File outFile = createNewEmptyPictureFile(context, getDefaultFileNameByPrefix(fileNamePrefix));
        OutputStream outputStream = new FileOutputStream(outFile);
        copyStream(inputStream, outputStream);
    }

    public static File getFile(Uri uri) {
        Log.d(TAG, "uri.getPath(): " + uri.getPath() + "; uri.getAuthority(): " + uri.getAuthority());
        int i = 0;
        i++;

        boolean isMediaFile = "media".equalsIgnoreCase(uri.getAuthority());

        return new File(uri.getPath());
    }

    public static File[] getFiles() {
        if (initialisePaintroidMediaDirectory() == true) {

            FilenameFilter filenameFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if (filename.startsWith("mark")) {
                        return true;
                    }
                    return false;
                }
            };
            return MEDIA_FILE.listFiles(filenameFilter);
        } else {
            return null;
        }

    }

    public static File getFileByID(int id) {
        for (File file : getFiles()) {
            String[] fileNameSplit = file.getName().split("_");
            if (fileNameSplit.length >= 4 && fileNameSplit[1].equals(String.valueOf(id))) {
                return file;
            }
        }
        return null;
    }

    public static boolean deleteFileByID(int id) {
        File file = getFileByID(id);
        if (file != null) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
