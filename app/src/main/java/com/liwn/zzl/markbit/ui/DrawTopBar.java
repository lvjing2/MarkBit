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

package com.liwn.zzl.markbit.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.Toast;

import com.liwn.zzl.markbit.DrawOptionsMenuActivity;
import com.liwn.zzl.markbit.DrawActivity;
import com.liwn.zzl.markbit.MarkBitApplication;
import com.liwn.zzl.markbit.R;
import com.liwn.zzl.markbit.command.UndoRedoManager;
import com.liwn.zzl.markbit.dialog.colorpicker.ColorPickerDialog;
import com.liwn.zzl.markbit.tools.Tool;
import com.liwn.zzl.markbit.tools.ToolFactory;
import com.liwn.zzl.markbit.tools.ToolType;
import com.liwn.zzl.markbit.tools.implementation.DrawTool;

import java.util.Observable;

public class DrawTopBar extends Observable implements OnTouchListener {

	public static enum ToolButtonIDs {
		BUTTON_ID_TOOL, BUTTON_ID_PARAMETER_TOP, BUTTON_ID_PARAMETER_BOTTOM_1, BUTTON_ID_PARAMETER_BOTTOM_2
	}

	private static final int SWITCH_TOOL_TOAST_Y_OFFSET = (int) DrawOptionsMenuActivity.ACTION_BAR_HEIGHT + 25;
	private static final int SWITCH_TOOL_BACKGROUND_ALPHA = 50;

	private ImageButton mUndoButton;
	private ImageButton mRedoButton;
	private ImageButton mColorButton;
	private ImageButton mToolButton;

	protected DrawingSurface drawingSurface;
	protected Tool mCurrentTool;
	private Tool mPreviousTool;
	protected DrawActivity drawActivity;

	private Toast mToolNameToast;
	private boolean mUndoDisabled;
	private boolean mRedoDisabled;

	public DrawTopBar(DrawActivity drawActivity) {
		this.drawActivity = drawActivity;
		mCurrentTool = new DrawTool(drawActivity, ToolType.BRUSH);
		MarkBitApplication.currentTool = mCurrentTool;

		mUndoButton = (ImageButton) drawActivity
				.findViewById(R.id.btn_top_undo);
		mUndoButton.setOnTouchListener(this);

		mRedoButton = (ImageButton) drawActivity
				.findViewById(R.id.btn_top_redo);
		mRedoButton.setOnTouchListener(this);

		mColorButton = (ImageButton) drawActivity
				.findViewById(R.id.btn_top_color);
		mColorButton.setOnTouchListener(this);

		mToolButton = (ImageButton) drawActivity
				.findViewById(R.id.btn_top_toolswitch);
		mToolButton.setOnTouchListener(this);

		setToolSwitchBackground(R.drawable.icon_menu_move);
		drawingSurface = (DrawingSurface) drawActivity
				.findViewById(R.id.drawingSurfaceView);

		UndoRedoManager.getInstance().setStatusbar(this);
	}

	public Tool getCurrentTool() {
		return this.mCurrentTool;
	}

	public void setTool(Tool tool) {

		// ignore to set the same tool again. except stamptool -> reselect =
		// reset selection.
		if ((tool.getToolType() == mCurrentTool.getToolType())
				&& (tool.getToolType() != ToolType.STAMP)) {
			return;
		}

		if (((tool.getToolType() == ToolType.MOVE) || (tool.getToolType() == ToolType.ZOOM))
				&& (!((mCurrentTool.getToolType() == ToolType.MOVE) || (mCurrentTool
						.getToolType() == ToolType.ZOOM)))) {
			mPreviousTool = mCurrentTool;
			setToolSwitchBackground(mPreviousTool
					.getAttributeButtonResource(ToolButtonIDs.BUTTON_ID_TOOL));

		} else if (((tool.getToolType() == ToolType.MOVE) && (mCurrentTool
				.getToolType() == ToolType.ZOOM))
				|| ((tool.getToolType() == ToolType.ZOOM) && (mCurrentTool
						.getToolType() == ToolType.MOVE))) {
			// everything stays the same
		} else {
			mPreviousTool = null;
			setToolSwitchBackground(R.drawable.icon_menu_move);
		}

		if ((mPreviousTool == null)
				&& ((tool.getToolType() == ToolType.MOVE) || (tool
						.getToolType() == ToolType.ZOOM))) {
			this.mCurrentTool = ToolFactory.createTool(drawActivity,
					ToolType.BRUSH);
		} else {
			this.mCurrentTool = tool;
		}

		Animation switchAnimation = AnimationUtils.loadAnimation(drawActivity,
				R.anim.fade_in);
		mToolButton.setAnimation(switchAnimation);
		mToolButton.setImageResource(mCurrentTool
				.getAttributeButtonResource(ToolButtonIDs.BUTTON_ID_TOOL));

		showToolChangeToast();

		super.setChanged();
		super.notifyObservers();
	}

	private void setToolSwitchBackground(int backgroundResource) {
		Bitmap bitmap = BitmapFactory.decodeResource(
				drawActivity.getResources(), backgroundResource);
		BitmapDrawable bitmapDrawable = new BitmapDrawable(
				drawActivity.getResources(), bitmap);
		bitmapDrawable.setAlpha(SWITCH_TOOL_BACKGROUND_ALPHA);
		mToolButton.setBackgroundDrawable(bitmapDrawable);
	}

	private void showToolChangeToast() {
		if (mToolNameToast != null) {
			mToolNameToast.cancel();
		}

		mToolNameToast = Toast.makeText(drawActivity, drawActivity
				.getString(mCurrentTool.getToolType().getNameResource()),
				Toast.LENGTH_SHORT);
		mToolNameToast.setGravity(Gravity.TOP | Gravity.RIGHT, 0,
				SWITCH_TOOL_TOAST_Y_OFFSET);
		mToolNameToast.show();
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		switch (view.getId()) {
		case R.id.btn_top_undo:
			onUndoTouch(event);
			return true;
		case R.id.btn_top_redo:
			onRedoTouch(event);
			return true;
		case R.id.btn_top_toolswitch:
			onToolSwitchTouch(event);
			return true;
		case R.id.btn_top_color:
			onColorTouch(event);
			return true;
		default:
			return false;
		}
	}

	private void onUndoTouch(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (!mUndoDisabled) {
				mUndoButton.setBackgroundResource(R.color.holo_blue_bright);
			}
			MarkBitApplication.commandManager.undo();
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			mUndoButton.setBackgroundResource(0);
		}
	}

	private void onRedoTouch(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (!mRedoDisabled) {
				mRedoButton.setBackgroundResource(R.color.holo_blue_bright);
			}
			MarkBitApplication.commandManager.redo();
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			mRedoButton.setBackgroundResource(0);
		}
	}

	private void onToolSwitchTouch(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (mPreviousTool != null) {
				drawActivity.switchTool(mPreviousTool);
			} else {
				drawActivity.switchTool(ToolType.MOVE);
			}
		}
	}

	private void onColorTouch(MotionEvent event) {
		if ((event.getAction() == MotionEvent.ACTION_DOWN)
				&& mCurrentTool.getToolType().isColorChangeAllowed()) {
			ColorPickerDialog.getInstance().show();
			ColorPickerDialog.getInstance().setInitialColor(
					mCurrentTool.getDrawPaint().getColor());
		}
	}

	public void toggleUndo(final int undoIcon) {
		drawActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mUndoButton.setImageResource(undoIcon);
			}
		});
	}

	public void toggleRedo(final int redoIcon) {
		drawActivity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mRedoButton.setImageResource(redoIcon);
			}
		});

	}

	public void enableUndo() {
		mUndoDisabled = false;
	}

	public void disableUndo() {
		mUndoDisabled = true;
	}

	public void enableRedo() {
		mRedoDisabled = false;
	}

	public void disableRedo() {
		mRedoDisabled = true;
	}
}
