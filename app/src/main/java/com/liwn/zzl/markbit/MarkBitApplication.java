/**
 *  Paintroid: An image manipulation application for Android.
 *  Copyright (C) 2010-2015 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.liwn.zzl.markbit;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.Toast;

import com.liwn.zzl.markbit.mark.DummyContent;

import java.io.File;

public class MarkBitApplication extends Application {
	public static final String TAG = "PAINTROID";
	public static final int BIT_LCD_WIDTH = 48;
	public static final int BIT_LCD_HEIGHT = 48;
	public static final int ALL_LCD_WIDTH = 87;
	public static final int ALL_LCD_HEIGHT = 87;
	public static final int DEFAULT_COLOR = Color.RED;
	public static final int ANDROID_VERSION_ICE_CREAM_SANDWICH = 14;

	// Message types sent from the BluetoothChatService Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final int MESSAGE_DEVICE_SELECTED = 6;

	public static final int MARK_STORAGE_NUM = 128;
	public static StringBuilder markStorageMask = new StringBuilder(MARK_STORAGE_NUM);
//	public static ArrayList<MarkItem> markItemList = new ArrayList<MarkItem>();
	public static DummyContent dummyContent;
	public static int markID1 = -2;
	public static int markID2 = -2;

	// Key names received from the BluetoothChatService Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String DEVICE_ADDRESS = "device_address";
	public static final String TOAST = "toast";

	public static String default_bins_dir_name = "bins";
	public static String i_name;
	public static String r_name;

	public static Context applicationContext;
//	public static DrawingSurface drawingSurface;
//	public static CommandManager commandManager;
//	public static Tool currentTool;
//	public static Perspective perspective;
	public static boolean openedFromCatroid = false;
	public static String catroidPicturePath;
	public static boolean isPlainImage = true;
	public static Menu drawMenu;
	public static Menu btMenu;
	public static BitmapFactory.Options opts = null;
	public static boolean isSaved = true;
	public static Uri savedPictureUri = null;
	public static boolean saveCopy = false;
	public static Bitmap outPreview;
	public static ImageView previewImageView;
	public static File i_file;
	public static File r_file;

	@Override
	public void onCreate() {
		super.onCreate();
		applicationContext = getApplicationContext();
		opts = getOpts();
		i_name = applicationContext.getString(R.string.I_name);
		r_name = applicationContext.getString(R.string.R_name);


//		commandManager = new CommandManagerImplementation();
//		outPreview = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.out_preview);
	}

	private BitmapFactory.Options getOpts() {
		BitmapFactory.Options opts = null;
		TypedValue value = new TypedValue();

		if (opts == null) {
			opts = new BitmapFactory.Options();
		}

		if (opts.inDensity == 0 && value != null) {
			final int density = value.density;
			if (density == TypedValue.DENSITY_DEFAULT) {
				opts.inDensity = DisplayMetrics.DENSITY_DEFAULT;
			} else if (density != TypedValue.DENSITY_NONE) {
				opts.inDensity = density;
			}
		}

		if (opts.inTargetDensity == 0) {
			opts.inTargetDensity = getResources().getDisplayMetrics().densityDpi;
		}

		return opts;
	}

	public static String getVersionName(Context context) {
		String versionName = "unknown";
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			versionName = packageInfo.versionName;
		} catch (NameNotFoundException nameNotFoundException) {
			Log.e(MarkBitApplication.TAG, "Name not found",
					nameNotFoundException);
		}
		return versionName;
	}
}
