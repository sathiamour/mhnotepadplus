package com.android.hinotes;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddItemActivity  extends Activity {
	
	public static final int MaxLength = 64;
	public static final String Action = "action";
	public static final String NextAction = "next";
	public static final String StopAction = "stop";
	private EditText Content;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_checklist_item);

		Button Confirm = (Button)findViewById(android.R.id.button1);
		Button Cancel = (Button)findViewById(android.R.id.button2);
		Button Next = (Button)findViewById(android.R.id.button3);
		Content = (EditText)findViewById(R.id.item_content);
		Confirm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				   String Item = Content.getText().toString().trim();
				   // Length check
				   if( Item.length() > MaxLength )
				   {
					   showDialog(ProjectConst.Item_TooLong_Prompt_Dlg);
					   return;
				   }
				   // Empty check
				   if( Item.length() == 0 )
				   {
					   showDialog(ProjectConst.Item_Empty_Prompt_Dlg);
					   return;
				   }
                   // Return
				   Intent Back = new Intent();
				   Back.putExtra(Action, StopAction);
				   Back.putExtra(OneNote.KEY_BODY, Item);
				   setResult(RESULT_OK, Back);
				   finish();
			}
		});

		Cancel.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});

		Next.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				   String Item = Content.getText().toString().trim();
				   // Length check
				   if( Item.length() > MaxLength )
				   {
					   showDialog(ProjectConst.Item_TooLong_Prompt_Dlg);
					   return;
				   }
				   // Empty check
				   if( Item.length() == 0 )
				   {
					   showDialog(ProjectConst.Item_Empty_Prompt_Dlg);
					   return;
				   }
				   // Return
				   Intent Back = new Intent();
				   Back.putExtra(Action, NextAction);
				   Back.putExtra(OneNote.KEY_BODY, Item);
				   setResult(RESULT_OK, Back);
				   finish();
			}
		});
		
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case ProjectConst.Item_TooLong_Prompt_Dlg:
		     return HelperFunctions.BuildAltertDialog(this, R.string.prompt_title, R.string.item_toolong_prompt);
		case ProjectConst.Item_Empty_Prompt_Dlg:
			return HelperFunctions.BuildAltertDialog(this, R.string.prompt_title, R.string.item_empty_prompt);
		}
		return null;
	}
	
	@Override 
	public void onBackPressed(){
		setResult(RESULT_CANCELED);
	    finish();
	}
}
