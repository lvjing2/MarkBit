package com.liwn.zzl.markbit.tools.implementation;

import android.app.Activity;

import com.liwn.zzl.markbit.R;
import com.liwn.zzl.markbit.tools.ToolType;
import com.liwn.zzl.markbit.ui.DrawTopBar.ToolButtonIDs;

public class ImportTool extends StampTool {

	public ImportTool(Activity activity, ToolType toolType) {
		super(activity, toolType);
		mStampActive = true;
		mAttributeButton2.setEnabled(false);
	}

	@Override
	public int getAttributeButtonResource(ToolButtonIDs buttonNumber) {
		switch (buttonNumber) {
		case BUTTON_ID_PARAMETER_BOTTOM_1:
			return R.drawable.icon_menu_stamp_paste;
		case BUTTON_ID_PARAMETER_BOTTOM_2:
			return NO_BUTTON_RESOURCE;
		default:
			return super.getAttributeButtonResource(buttonNumber);
		}
	}
}
