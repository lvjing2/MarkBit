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

package com.liwn.zzl.markbit.tools.implementation;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.liwn.zzl.markbit.MarkBitApplication;
import com.liwn.zzl.markbit.R;
import com.liwn.zzl.markbit.command.Command;
import com.liwn.zzl.markbit.command.implementation.BaseCommand;
import com.liwn.zzl.markbit.command.implementation.ResizeCommand;
import com.liwn.zzl.markbit.dialog.IndeterminateProgressDialog;
import com.liwn.zzl.markbit.tools.ToolType;
import com.liwn.zzl.markbit.ui.DrawTopBar.ToolButtonIDs;

import java.util.Observable;

public class ResizeTool extends BaseToolWithRectangleShape {

	private static final float START_ZOOM_FACTOR = 0.95f;
	private static final boolean ROTATION_ENABLED = false;
	private static final boolean RESPECT_IMAGE_BORDERS = false;
	private static final boolean RESIZE_POINTS_VISIBLE = false;
	private static final boolean RESPECT_MAXIMUM_BORDER_RATIO = false;
	private static final boolean RESPECT_MAXIMUM_BOX_RESOLUTION = true;
	private static final float MAXIMUM_BITMAP_SIZE_FACTOR = 4.0f;

	private float mResizeBoundWidthXLeft;
	private float mResizeBoundWidthXRight = 0;
	private float mResizeBoundHeightYTop;
	private float mResizeBoundHeightYBottom = 0;
	private int mIntermediateResizeBoundWidthXLeft;
	private int mIntermediateResizeBoundWidthXRight;
	private int mIntermediateResizeBoundHeightYTop;
	private int mIntermediateResizeBoundHeightYBottom;
	private boolean mBitmapIsEmpty;

	private boolean mCropRunFinished = false;
	private static FindCroppingCoordinatesAsyncTask mFindCroppingCoordinates = null;
	private boolean mResizeInformationAlreadyShown = false;
	private boolean mMaxImageResolutionInformationAlreadyShown = false;

	public ResizeTool(Context context, ToolType toolType) {
		super(context, toolType);

		setRotationEnabled(ROTATION_ENABLED);
		setRespectImageBounds(RESPECT_IMAGE_BORDERS);
		setResizePointsVisible(RESIZE_POINTS_VISIBLE);
		setRespectMaximumBorderRatio(RESPECT_MAXIMUM_BORDER_RATIO);

		mBoxHeight = MarkBitApplication.drawingSurface.getBitmapHeight();
		mBoxWidth = MarkBitApplication.drawingSurface.getBitmapWidth();
		mToolPosition.x = mBoxWidth / 2f;
		mToolPosition.y = mBoxHeight / 2f;

		resetScaleAndTranslation();

		mCropRunFinished = true;

		DisplayResizeInformationAsyncTask displayResizeInformation = new DisplayResizeInformationAsyncTask();
		displayResizeInformation.execute();

		Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
		Point displaySize = new Point();
		display.getSize(displaySize);
		int displayWidth = displaySize.x;
		int displayHeight = displaySize.y;
		setMaximumBoxResolution(displayWidth * displayHeight * MAXIMUM_BITMAP_SIZE_FACTOR);
		setRespectMaximumBoxResolution(RESPECT_MAXIMUM_BOX_RESOLUTION);
	}

	@Override
	public void resetInternalState() {
		resetScaleAndTranslation();
	}

	@Override
	protected void drawToolSpecifics(Canvas canvas) {
		if (mCropRunFinished) {
			mLinePaint.setColor(mPrimaryShapeColor);
			mLinePaint.setStrokeWidth(mToolStrokeWidth * 2);

			PointF rightTopPoint = new PointF(-mBoxWidth / 2, -mBoxHeight / 2);
			float tempWidth = mBoxWidth;

			for (int lines = 0; lines < 4; lines++) {
				float resizeLineLengthHeight = mBoxHeight / 10;
				float resizeLineLengthWidth = mBoxWidth / 10;

				canvas.drawLine(rightTopPoint.x - mToolStrokeWidth / 2,
						rightTopPoint.y, rightTopPoint.x + resizeLineLengthWidth,
						rightTopPoint.y, mLinePaint);

				canvas.drawLine(rightTopPoint.x, rightTopPoint.y
								- mToolStrokeWidth / 2, rightTopPoint.x,
						rightTopPoint.y + resizeLineLengthHeight, mLinePaint);

				canvas.drawLine(rightTopPoint.x + mBoxWidth / 2
								- resizeLineLengthWidth, rightTopPoint.y, rightTopPoint.x
								+ mBoxWidth / 2 + resizeLineLengthWidth, rightTopPoint.y,
						mLinePaint);
				canvas.rotate(90);
				float tempX = rightTopPoint.x;
				rightTopPoint.x = rightTopPoint.y;
				rightTopPoint.y = tempX;
				float tempHeight = mBoxHeight;
				mBoxHeight = mBoxWidth;
				mBoxWidth = tempHeight;
			}
			mBoxWidth = tempWidth;
		}
	}

	@Override
	public int getAttributeButtonColor(ToolButtonIDs buttonNumber) {
		switch (buttonNumber) {
			case BUTTON_ID_PARAMETER_TOP:
				return Color.TRANSPARENT;
			default:
				return super.getAttributeButtonColor(buttonNumber);
		}
	}

	@Override
	public void attributeButtonClick(ToolButtonIDs buttonNumber) {
		switch (buttonNumber) {
			case BUTTON_ID_PARAMETER_BOTTOM_1:
				if (mFindCroppingCoordinates == null
						|| mFindCroppingCoordinates.getStatus() != AsyncTask.Status.RUNNING) {
					mFindCroppingCoordinates = new FindCroppingCoordinatesAsyncTask();
					mFindCroppingCoordinates.execute();
				}
				break;
			case BUTTON_ID_PARAMETER_BOTTOM_2:
				executeResizeCommand();
				break;
			default:
				super.attributeButtonClick(buttonNumber);
		}
	}

	@Override
	public int getAttributeButtonResource(ToolButtonIDs buttonNumber) {
		switch (buttonNumber) {
			case BUTTON_ID_PARAMETER_TOP:
				return NO_BUTTON_RESOURCE;
			case BUTTON_ID_PARAMETER_BOTTOM_1:
				return R.drawable.icon_menu_resize_adjust;
			case BUTTON_ID_PARAMETER_BOTTOM_2:
				return R.drawable.icon_menu_resize_cut;
			default:
				return super.getAttributeButtonResource(buttonNumber);
		}
	}

	private void resetScaleAndTranslation() {
		MarkBitApplication.perspective.resetScaleAndTranslation();
		float zoomFactor = MarkBitApplication.perspective
				.getScaleForCenterBitmap() * START_ZOOM_FACTOR;
		MarkBitApplication.perspective.setScale(zoomFactor);
	}

	private void initialiseResizingState() {
		mCropRunFinished = false;
		mResizeBoundWidthXRight = 0;
		mResizeBoundHeightYBottom = 0;
		mResizeBoundWidthXLeft = MarkBitApplication.drawingSurface
				.getBitmapWidth();
		mResizeBoundHeightYTop = MarkBitApplication.drawingSurface
				.getBitmapHeight();
		mIntermediateResizeBoundWidthXLeft = 0;
		mIntermediateResizeBoundWidthXRight = MarkBitApplication.drawingSurface
				.getBitmapWidth();
		mIntermediateResizeBoundHeightYTop = 0;
		mIntermediateResizeBoundHeightYBottom = MarkBitApplication.drawingSurface
				.getBitmapHeight();
		resetScaleAndTranslation();
	}

	protected void displayToastInformation(int stringID) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout layout = (LinearLayout) inflater.inflate(
				R.layout.image_toast_layout, (ViewGroup) ((Activity) mContext)
						.findViewById(R.id.image_toast_layout_root));

		if (stringID != R.string.resize_to_resize_tap_text) {
			ImageView toastImage = (ImageView) layout.findViewById(R.id.toast_image);
			toastImage.setVisibility(View.GONE);

			TextView text = (TextView) layout.findViewById(R.id.toast_text);
			text.setText(mContext.getText(stringID));
		}

		Toast toast = new Toast(mContext);
		toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(layout);
		toast.show();
	}

	protected void executeResizeCommand() {
		if (mCropRunFinished == true) {
			mCropRunFinished = false;
			initResizeBounds();
			if (areResizeBordersValid()) {
				IndeterminateProgressDialog.getInstance().show();
				Command command = new ResizeCommand((int) Math.floor(mResizeBoundWidthXLeft),
						(int) Math.floor(mResizeBoundHeightYTop),
						(int) Math.floor(mResizeBoundWidthXRight),
						(int) Math.floor(mResizeBoundHeightYBottom),
						(int) mMaximumBoxResolution);

				((ResizeCommand) command).addObserver(this);
				MarkBitApplication.commandManager.commitCommand(command);
			} else {
				mCropRunFinished = true;
				displayToastInformation(R.string.resize_nothing_to_resize);
			}
		}
	}

	private boolean areResizeBordersValid() {
		if (mResizeBoundWidthXRight < mResizeBoundWidthXLeft
				|| mResizeBoundHeightYTop > mResizeBoundHeightYBottom) {
			return false;
		}
		if (mResizeBoundWidthXLeft >= MarkBitApplication.drawingSurface.getBitmapWidth() ||
				mResizeBoundWidthXRight < 0 || mResizeBoundHeightYBottom < 0 ||
				mResizeBoundHeightYTop >= MarkBitApplication.drawingSurface.getBitmapHeight()) {
			return false;
		}
		if (mResizeBoundWidthXLeft == 0 && mResizeBoundHeightYTop == 0 &&
				mResizeBoundWidthXRight == MarkBitApplication.drawingSurface.getBitmapWidth() - 1 &&
				mResizeBoundHeightYBottom == MarkBitApplication.drawingSurface.getBitmapHeight() - 1) {
			return false;
		}
		if ((mResizeBoundWidthXRight + 1 - mResizeBoundWidthXLeft)
				* (mResizeBoundHeightYBottom + 1 - mResizeBoundHeightYTop) > mMaximumBoxResolution) {
			return false;
		}

		return true;

	}

	@Override
	public void update(Observable observable, Object data) {
		super.update(observable, data);
		if (data instanceof BaseCommand.NOTIFY_STATES) {
			if (BaseCommand.NOTIFY_STATES.COMMAND_DONE == data
					|| BaseCommand.NOTIFY_STATES.COMMAND_FAILED == data) {
				initialiseResizingState();
				mResizeBoundWidthXRight = Float
						.valueOf(MarkBitApplication.drawingSurface
								.getBitmapWidth() - 1);
				mResizeBoundHeightYBottom = Float
						.valueOf(MarkBitApplication.drawingSurface
								.getBitmapHeight() - 1);
				mResizeBoundWidthXLeft = 0f;
				mResizeBoundHeightYTop = 0f;
				setRectangle(new RectF(mResizeBoundWidthXLeft,
						mResizeBoundHeightYTop, mResizeBoundWidthXRight,
						mResizeBoundHeightYBottom));
				mCropRunFinished = true;
			}
		}
	}

	protected class DisplayResizeInformationAsyncTask extends
			AsyncTask<Void, Integer, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			return null;
		}

		@Override
		protected void onPostExecute(Void nothing) {
			if (!mResizeInformationAlreadyShown) {
				displayToastInformation(R.string.resize_to_resize_tap_text);
				mResizeInformationAlreadyShown = true;
			}
		}
	}

	protected class FindCroppingCoordinatesAsyncTask extends
			AsyncTask<Void, Integer, Void> {

		private int mBitmapWidth = -1;
		private int mBitmapHeight = -1;
		private final int TRANSPARENT = Color.TRANSPARENT;

		FindCroppingCoordinatesAsyncTask() {
			initialiseResizingState();
			mBitmapWidth = MarkBitApplication.drawingSurface.getBitmapWidth();
			mBitmapHeight = MarkBitApplication.drawingSurface
					.getBitmapHeight();
			mLinePaint = new Paint();
			mLinePaint.setDither(true);
			mLinePaint.setStyle(Paint.Style.STROKE);
			mLinePaint.setStrokeJoin(Paint.Join.ROUND);

		}

		@Override
		protected Void doInBackground(Void... arg0) {
			if (MarkBitApplication.drawingSurface.isDrawingSurfaceBitmapValid()) {
				croppingAlgorithmSnail();
			}
			return null;
		}

		private void croppingAlgorithmSnail() {
			try {
				if (MarkBitApplication.drawingSurface
						.isDrawingSurfaceBitmapValid()) {
					mBitmapIsEmpty = true;
					searchTopToBottom();
					if (mBitmapIsEmpty) {
						setRectangle(new RectF(0, 0, mBitmapWidth - 1, mBitmapHeight - 1));
					} else {
						searchLeftToRight();
						searchBottomToTop();
						searchRightToLeft();
					}
					initResizeBounds();

				}
			} catch (Exception ex) {
				Log.e(MarkBitApplication.TAG,
						"ERROR: Cropping->" + ex.getMessage());
			}
		}

		private void getBitmapPixelsLineWidth(int[] bitmapPixelsArray,
											  int heightStartYLine) {
			MarkBitApplication.drawingSurface.getPixels(bitmapPixelsArray, 0,
					mBitmapWidth, 0, heightStartYLine, mBitmapWidth, 1);
		}

		private void getBitmapPixelsLineHeight(int[] bitmapPixelsArray,
											   int widthXStartLine) {
			MarkBitApplication.drawingSurface.getPixels(bitmapPixelsArray, 0,
					1, widthXStartLine, 0, 1, mBitmapHeight);
		}

		private void searchTopToBottom() {
			int[] localBitmapPixelArray = new int[mBitmapWidth];
			for (mIntermediateResizeBoundHeightYTop = 0;
				 mIntermediateResizeBoundHeightYTop < mBitmapHeight; mIntermediateResizeBoundHeightYTop++) {
				getBitmapPixelsLineWidth(localBitmapPixelArray, mIntermediateResizeBoundHeightYTop);
				setRectangle(new RectF(mIntermediateResizeBoundWidthXLeft,
						mIntermediateResizeBoundHeightYTop,
						mIntermediateResizeBoundWidthXRight,
						mIntermediateResizeBoundHeightYBottom));

				for (int indexWidth = 0; indexWidth < mBitmapWidth; indexWidth++) {
					if (localBitmapPixelArray[indexWidth] != TRANSPARENT) {
						updateResizeBounds(indexWidth, mIntermediateResizeBoundHeightYTop);
						mBitmapIsEmpty = false;
						return;
					}
				}
			}
		}

		private void searchLeftToRight() {
			int[] localBitmapPixelArray = new int[mBitmapHeight];
			for (mIntermediateResizeBoundWidthXLeft = 0;
				 mIntermediateResizeBoundWidthXLeft < mBitmapWidth; mIntermediateResizeBoundWidthXLeft++) {
				getBitmapPixelsLineHeight(localBitmapPixelArray, mIntermediateResizeBoundWidthXLeft);

				setRectangle(new RectF(mIntermediateResizeBoundWidthXLeft,
						mIntermediateResizeBoundHeightYTop,
						mIntermediateResizeBoundWidthXRight,
						mIntermediateResizeBoundHeightYBottom));

				for (int indexHeight = mIntermediateResizeBoundHeightYTop; indexHeight < mBitmapHeight; indexHeight++) {
					if (localBitmapPixelArray[indexHeight] != TRANSPARENT) {
						updateResizeBounds(mIntermediateResizeBoundWidthXLeft, indexHeight);
						return;
					}
				}

			}
		}

		private void searchBottomToTop() {
			int[] localBitmapPixelArray = new int[mBitmapWidth];
			for (mIntermediateResizeBoundHeightYBottom = mBitmapHeight - 1;
				 mIntermediateResizeBoundHeightYBottom >= 0; mIntermediateResizeBoundHeightYBottom--) {
				getBitmapPixelsLineWidth(localBitmapPixelArray, mIntermediateResizeBoundHeightYBottom);

				setRectangle(new RectF(mIntermediateResizeBoundWidthXLeft,
						mIntermediateResizeBoundHeightYTop,
						mIntermediateResizeBoundWidthXRight,
						mIntermediateResizeBoundHeightYBottom));

				for (int indexWidth = mIntermediateResizeBoundWidthXLeft; indexWidth < mBitmapWidth; indexWidth++) {
					if (localBitmapPixelArray[indexWidth] != TRANSPARENT) {
						updateResizeBounds(indexWidth, mIntermediateResizeBoundHeightYBottom);
						return;
					}
				}
			}
		}

		private void searchRightToLeft() {
			int[] localBitmapPixelArray = new int[mBitmapHeight];
			for (mIntermediateResizeBoundWidthXRight = mBitmapWidth - 1;
				 mIntermediateResizeBoundWidthXRight >= 0; mIntermediateResizeBoundWidthXRight--) {
				getBitmapPixelsLineHeight(localBitmapPixelArray, mIntermediateResizeBoundWidthXRight);

				setRectangle(new RectF(mIntermediateResizeBoundWidthXLeft,
						mIntermediateResizeBoundHeightYTop,
						mIntermediateResizeBoundWidthXRight,
						mIntermediateResizeBoundHeightYBottom));

				for (int indexHeightTop = mIntermediateResizeBoundHeightYTop; indexHeightTop <= mIntermediateResizeBoundHeightYBottom; indexHeightTop++) {
					if (localBitmapPixelArray[indexHeightTop] != TRANSPARENT) {
						updateResizeBounds(mIntermediateResizeBoundWidthXRight, indexHeightTop);
						return;
					}
				}

			}
		}

		@Override
		protected void onPostExecute(Void nothing) {
			mCropRunFinished = true;
		}

	}

	private void updateResizeBounds(int resizeWidthXPosition,
									int resizeHeightYPosition) {
		mResizeBoundWidthXLeft = Math.min(resizeWidthXPosition,
				mResizeBoundWidthXLeft);
		mResizeBoundWidthXRight = Math.max(resizeWidthXPosition,
				mResizeBoundWidthXRight);

		mResizeBoundHeightYTop = Math.min(resizeHeightYPosition,
				mResizeBoundHeightYTop);
		mResizeBoundHeightYBottom = Math.max(resizeHeightYPosition,
				mResizeBoundHeightYBottom);

		setRectangle(new RectF(mResizeBoundWidthXLeft, mResizeBoundHeightYTop,
				mResizeBoundWidthXRight, mResizeBoundHeightYBottom));

	}

	private void setRectangle(RectF rectangle) {
		mBoxWidth = rectangle.right - rectangle.left + 1f;
		mBoxHeight = rectangle.bottom - rectangle.top + 1f;
		mToolPosition.x = rectangle.left + mBoxWidth / 2f;
		mToolPosition.y = rectangle.top + mBoxHeight / 2f;
	}

	private void initResizeBounds() {
		mResizeBoundWidthXLeft = mToolPosition.x - mBoxWidth / 2f;
		mResizeBoundWidthXRight = mToolPosition.x + mBoxWidth / 2f - 1f;
		mResizeBoundHeightYTop = mToolPosition.y - mBoxHeight / 2f;
		mResizeBoundHeightYBottom = mToolPosition.y + mBoxHeight / 2f - 1f;
	}

	@Override
	protected void onClickInBox() {
		executeResizeCommand();
	}

	@Override
	protected void preventThatBoxGetsTooLarge(float oldWidth, float oldHeight,
											  float oldPosX, float oldPosY) {
		super.preventThatBoxGetsTooLarge(oldWidth, oldHeight, oldPosX, oldPosY);
		if (!mMaxImageResolutionInformationAlreadyShown) {
			displayToastInformation(R.string.resize_max_image_resolution_reached);
			mMaxImageResolutionInformationAlreadyShown = true;
		}
	}

}
