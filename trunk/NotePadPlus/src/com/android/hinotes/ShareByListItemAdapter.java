package com.android.hinotes;

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
		  ShareByListItemAdapter(Context AppCtxt, String Action, String MediaType){
				Intent IntentType = new Intent(Action);
				IntentType.setType(MediaType);  
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
	            
	            // Set application icon
	            ImageView Tag = new ImageView(AppContext);
	            Tag.setImageDrawable(info.activityInfo.applicationInfo.loadIcon(AppContext.getPackageManager()));
	            Tag.setScaleType(ImageView.ScaleType.FIT_CENTER);
	            Tag.setPadding(0, 5, 0, 0);
	            Tag.setAdjustViewBounds(true);
	            Tag.setMinimumHeight(60);
	            Tag.setMaxWidth(50);
	            //Tag.setMaxWidth(35);
                 
	            // Add icon & name
	            Item.addView(Tag);
	            Item.addView(textView);

	            return Item;  
	      }	          
	}  