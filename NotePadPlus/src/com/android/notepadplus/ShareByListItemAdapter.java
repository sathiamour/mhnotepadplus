package com.android.notepadplus;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

class ShareByListItemAdapter extends BaseAdapter {  
	      
		  public List<ResolveInfo> Apps;
		  Context AppContext;
		  ShareByListItemAdapter(Context AppCtxt){
				Intent IntentType = new Intent(Intent.ACTION_SEND);
				IntentType.setType("text/plain");  
				Apps = AppCtxt.getPackageManager().queryIntentActivities(IntentType, PackageManager.GET_ACTIVITIES);
				AppContext = AppCtxt;
		  }
		  
	      @Override  
	      public int getCount() {  
	             return Apps.size();  
	      }  
	  
	      @Override  
	      public Object getItem(int position) {  
	            return null;  
	      }  
	  
	      @Override  
	      public long getItemId(int position) {  
	            return position;  
	      }  
	  
	      @Override  
	      public View getView(int position, View contentView, ViewGroup parent) {  
	    	    LinearLayout Item = new LinearLayout(AppContext);
	            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);  
	    	    Item.setLayoutParams(layoutParams);
	            TextView textView = new TextView(AppContext);  

	            ResolveInfo info = Apps.get(position) ;
	            String text = info.loadLabel(AppContext.getPackageManager()).toString();
	            textView.setText(text);  

	            // Set font size  
	            textView.setTextSize(24);  
	            // Set layout
	            textView.setGravity(android.view.Gravity.CENTER_VERTICAL);  
	            textView.setMinHeight(60);  
	            textView.setPadding(5, 0, 0, 0);
	            // Set font color
	            textView.setTextColor(Color.BLACK);  
	            
	            // Set app icon
	            ImageView Tag = new ImageView(AppContext);
	            Tag.setImageDrawable(info.activityInfo.loadIcon(AppContext.getPackageManager()));
	            Tag.setScaleType(ImageView.ScaleType.FIT_CENTER);
	            Tag.setMinimumHeight(60);
                 
	            // Add icon & name
	            Item.addView(Tag);
	            Item.addView(textView);

	            return Item;  
	      }	          
	}  