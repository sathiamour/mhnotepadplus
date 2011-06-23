package com.android.notepadplus;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;

/** Class Must extends with Dialog */
/** Implement onClickListener to dismiss dialog when OK Button is pressed */
public class NotifyDialog extends Dialog implements OnClickListener {
	Button DismissBtn;

	public NotifyDialog(Context context) {
		super(context);
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		LayoutInflater Inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout Dlg = (LinearLayout) Inflate.inflate(R.layout.note_alarm_notify, null);
		LinearLayout.LayoutParams Layout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		Dlg.setLayoutParams(Layout);
		setContentView(Dlg);
		/** Set callback */
		DismissBtn = (Button) findViewById(R.id.dismiss_notify);
		DismissBtn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		/** Dismiss the dialog */
		if( v == DismissBtn )
			dismiss();
	}

}
