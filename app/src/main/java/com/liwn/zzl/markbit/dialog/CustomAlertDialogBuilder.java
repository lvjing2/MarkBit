package com.liwn.zzl.markbit.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;

import com.liwn.zzl.markbit.R;

public class CustomAlertDialogBuilder extends AlertDialog.Builder {
	public CustomAlertDialogBuilder(Context context) {
		super(new ContextThemeWrapper(context, R.style.CustomPaintroidDialog));
	}
}
