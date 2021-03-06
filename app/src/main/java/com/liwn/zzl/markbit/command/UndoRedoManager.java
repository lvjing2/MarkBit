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

package com.liwn.zzl.markbit.command;

import com.liwn.zzl.markbit.R;
import com.liwn.zzl.markbit.ui.DrawTopBar;

public final class UndoRedoManager {

	private static UndoRedoManager mInstance;
	private DrawTopBar mDrawTopBar;

	public static enum StatusMode {
		ENABLE_UNDO, DISABLE_UNDO, ENABLE_REDO, DISABLE_REDO
	};

	private UndoRedoManager() {

	}

	public static UndoRedoManager getInstance() {
		if (mInstance == null) {
			mInstance = new UndoRedoManager();
		}
		return mInstance;
	}

	public void setStatusbar(DrawTopBar drawTopBar) {
		mDrawTopBar = drawTopBar;
	}

	public void update(StatusMode status) {
		switch (status) {
		case ENABLE_UNDO:
			mDrawTopBar.toggleUndo(R.drawable.icon_menu_undo);
			mDrawTopBar.enableUndo();

			break;
		case DISABLE_UNDO:
			mDrawTopBar.toggleUndo(R.drawable.icon_menu_undo_disabled);
			mDrawTopBar.disableUndo();
			break;
		case ENABLE_REDO:
			mDrawTopBar.toggleRedo(R.drawable.icon_menu_redo);
			mDrawTopBar.enableRedo();
			break;
		case DISABLE_REDO:
			mDrawTopBar.toggleRedo(R.drawable.icon_menu_redo_disabled);
			mDrawTopBar.disableRedo();
			break;

		default:
			break;
		}
	}

}
