package com.android.hinotes;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AbsListView.LayoutParams;

public class CommonListItemAdapter extends BaseAdapter {  
	      
		  private int[] ImgIds;
		  private int Items;
		  private Context AppContext;
		  CommonListItemAdapter(int[] Img, int ItemRes, Context AppCtxt){
			     ImgIds = Img;
			     Items = ItemRes;
			     AppContext = AppCtxt;
		  }
	      @Override  
	      public int getCount() {  
	             return ImgIds.length;  
	      }  
	  
	      @Override  
	      public Object getItem(int position) {  
	            return null;  
	      }  
	  
	      @Override  
	      public long getItemId(int position) {  
	            return 0;  
	      }  
	  
	      @Override  
	      public View getView(int position, View contentView, ViewGroup parent) {  
	            TextView textView = new TextView(AppContext);  
	            //获得array.xml中的数组资源getStringArray返回的是一个String数组  
	            String text = AppContext.getResources().getStringArray(Items)[position];  
	            textView.setText(text);  
	            //设置字体大小  
	            textView.setTextSize(24);  
	            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(  
	                    LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);  
	            textView.setLayoutParams(layoutParams);  
	            //设置水平方向上居中  
	            textView.setGravity(android.view.Gravity.CENTER_VERTICAL);  
	            textView.setMinHeight(65);  
	            //设置文字颜色  
	            textView.setTextColor(Color.BLACK);    
	            //设置图标在文字的左边  
	            textView.setCompoundDrawablesWithIntrinsicBounds(ImgIds[position], 0, 0, 0);  
	            //设置textView的左上右下的padding大小  
	            textView.setPadding(15, 0, 15, 0);  
	            //设置文字和图标之间的padding大小  
	            textView.setCompoundDrawablePadding(15); 
	            return textView;  
	      }	          
	}  