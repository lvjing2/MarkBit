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

package com.liwn.zzl.markbit.command.implementation;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;

import com.liwn.zzl.markbit.FileIO;
import com.liwn.zzl.markbit.MarkBitApplication;

public class BitmapCommand extends BaseCommand {

	private boolean mResetScaleAndTranslation = true;

	public BitmapCommand(Bitmap bitmap) {
		if (bitmap != null) {
			mBitmap = Bitmap.createBitmap(bitmap);
		}
	}

	public BitmapCommand(Bitmap bitmap, boolean resetScaleAndTranslation) {
		this(bitmap);
		mResetScaleAndTranslation = resetScaleAndTranslation;
	}

	@Override
	public void run(Canvas canvas, Bitmap bitmap) {
		if (mBitmap == null && mFileToStoredBitmap != null) {
			mBitmap = FileIO.getBitmapFromFile(mFileToStoredBitmap);
		}
		if (mBitmap != null) {
			if (bitmap != null) {
				bitmap.eraseColor(Color.TRANSPARENT);
			}
			MarkBitApplication.drawingSurface.setBitmap(mBitmap.copy(
					Config.ARGB_8888, true));

			if (mResetScaleAndTranslation
					&& MarkBitApplication.perspective != null) {
				MarkBitApplication.perspective.resetScaleAndTranslation();
			}

			if (mFileToStoredBitmap == null) {
				storeBitmap();
			}
		}
	}
}
