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
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
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

    public static final byte VERSION_ADDR          = 0x00;
    public static final int  VERSION_LEN           = 4;
    public static final byte DEVICE_NAME_ADDR      = 0x07;
    public static final byte FACTORY_NAME_ADDR     = 0x08;
    public static final int  FACTORY_NAME_LEN      = 8;
    public static final byte ALL_SAMPLE_NUM_ADDR   = 0x10;
    public static final byte A_SAMPLE_NUM_ADDR     = 0x11;
    public static final byte B_SAMPLE_NUM_ADDR     = 0x12;
    public static final byte BRIGHTNESS_ADDR       = 0x13;
    public static final byte A_SHOW_TIME_ADDR      = 0x14;
    public static final byte A_HIDE_TIME_ADDR      = 0x15;
    public static final byte B_SHOW_TIME_ADDR      = 0x16;
    public static final byte B_HIDE_TIME_ADDR      = 0x17;
    public static final byte BATTERY_TYPE_ADDR     = 0x18;
    public static final byte IS_MAGNET_ADDR        = 0x19;
    public static final byte IS_LOW_VOLTAGE_ADDR   = 0x1A;
    public static final byte IS_DUMP_ADDR          = 0x1B;
    public static final byte PASSWORD_ADDR         = 0x1C;
    public static final int  PASSWORD_LEN          = 4;
    public static final byte A_INDEX_LIB_ADDR      = 0x20;
    public static final byte B_INDEX_LIB_ADDR      = 0x40;

    private FileIO() {
    }

    public static void deleteAllFiles() {
        File[] files = getFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    public static File getMediaFile() {
        if(MEDIA_FILE == null){
            if(initialisePaintroidMediaDirectory() == true) {
                return MEDIA_FILE;
            } else {
                return null;
            }
        }
        return MEDIA_FILE;
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

    public static File getIconFile() {
        File file = new File(getMediaFolderName() + "/" + MarkBitApplication.i_name);
        return file;
    }

    public static File getRconFile() {
        File file = new File(getMediaFolderName() + "/" + MarkBitApplication.r_name);
        return file;
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

    public static File createNewEmptyBinFile(Context context,
                                                 String filename) {
        if (initialisePaintroidMediaDirectory() == true) {
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
                    "/" + MarkBitApplication.applicationContext.getString(R.string.ext_storage_directory_name) + "/");
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

    public static void copyFile(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static void copyStream(Context context, InputStream inputStream) throws IOException {
        File outFile = createNewEmptyPictureFile(context, getDefaultFileName());
        OutputStream outputStream = new FileOutputStream(outFile);
        copyStream(inputStream, outputStream);
    }

    public static void copyStream(Context context, InputStream inputStream, String fileName) throws IOException {
        File outFile = createNewEmptyBinFile(context, fileName);
        OutputStream outputStream = new FileOutputStream(outFile);
        copyStream(inputStream, outputStream);
    }

    public static File getFile(Context context, Uri uri) {
        return new File(getPath(context, uri));
    }

    public static byte getByte(File file, int offset) {
        byte res = 0;

        try {
            RandomAccessFile ra_file = new RandomAccessFile(file, "r");


            byte[] tmp = new byte[1];
            if (ra_file != null) {
                ra_file.seek(offset);
                if (ra_file.read(tmp, 0, 1) == 1) {
                    res = tmp[0];
                }
            }

            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return res;
        }
    }

    public static int getBytes(File file, byte[] buffer, int offset, int len) {
        int res = -1;

        try {
            RandomAccessFile ra_file = new RandomAccessFile(file, "r");

            if (ra_file != null) {
                ra_file.seek(offset);
                res = ra_file.read(buffer, 0, len);
            }

            return res;
        } catch (IOException e) {
            e.printStackTrace();
            return res;
        }
    }

    public static int setBytes(File file, int offset, int length, byte[] value) {
        int res = -1;
        try {
            RandomAccessFile ra_file = new RandomAccessFile(file, "rw");
            if (ra_file != null) {
                ra_file.seek(offset);
                ra_file.write(value, 0, length);
                res = length;

            } else {
                Log.e(TAG, "file not found!");
            }

            return res;

        } catch (IOException e) {
            e.printStackTrace();
            return res;
        }
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                DatabaseUtils.dumpCursor(cursor);

                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public static String getPath(final Context context, final Uri uri) {

        Log.d(TAG + " File -",
                "Authority: " + uri.getAuthority() +
                        ", Fragment: " + uri.getFragment() +
                        ", Port: " + uri.getPort() +
                        ", Query: " + uri.getQuery() +
                        ", Scheme: " + uri.getScheme() +
                        ", Host: " + uri.getHost() +
                        ", Segments: " + uri.getPathSegments().toString()
        );

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
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

    public static boolean isSameFile(@NonNull File f1, @NonNull File f2) {
        try {
            String p1 = f1.getCanonicalPath();
            String p2 = f2.getCanonicalPath();
            p1 = p1.replaceFirst("/storage/emulated/legacy", "/storage/emulated/0");
            p2 = p2.replaceFirst("/storage/emulated/legacy", "/storage/emulated/0");
            p1 = p1.replaceFirst("/storage/sdcard0", "/storage/emulated/0");
            p2 = p2.replaceFirst("/storage/sdcard0", "/storage/emulated/0");
            if (p1.equals(p2)) {
                return true;
            } else {
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "isSameFile Exception");
            e.printStackTrace();
            return false;
        }
    }
}
