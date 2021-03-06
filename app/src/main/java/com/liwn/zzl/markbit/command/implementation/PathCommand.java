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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.liwn.zzl.markbit.MarkBitApplication;

public class PathCommand extends BaseCommand {
	protected Path mPath;

	public PathCommand(Paint paint, Path path) {
		super(paint);
		if (path != null) {
			mPath = new Path(path);
		}
	}

	@Override
	public void run(Canvas canvas, Bitmap bitmap) {
		if ((canvas == null) || mPath == null) {
			Log.w(MarkBitApplication.TAG,
					"Object must not be null in PathCommand.");
			return;
		}

		RectF bounds = new RectF();
		mPath.computeBounds(bounds, true);
		Rect boundsCanvas = canvas.getClipBounds();

		if (boundsCanvas == null) {

			notifyStatus(NOTIFY_STATES.COMMAND_FAILED);
			return;
		}

		if (pathInCanvas(bounds, boundsCanvas)) {
			canvas.drawPath(mPath, mPaint);
		} else {

			notifyStatus(NOTIFY_STATES.COMMAND_FAILED);
		}
	}

	private boolean pathInCanvas(RectF rectangleBoundsPath,
			Rect rectangleBoundsCanvas) {
		RectF rectangleCanvas = new RectF(rectangleBoundsCanvas);

		float strokeWidth = mPaint.getStrokeWidth();

		rectangleBoundsPath.bottom = rectangleBoundsPath.bottom
				+ (strokeWidth / 2);
		rectangleBoundsPath.left = rectangleBoundsPath.left - (strokeWidth / 2);
		rectangleBoundsPath.right = rectangleBoundsPath.right
				+ (strokeWidth / 2);
		rectangleBoundsPath.top = rectangleBoundsPath.top - (strokeWidth / 2);

		return (RectF.intersects(rectangleCanvas, rectangleBoundsPath));
	}
}
