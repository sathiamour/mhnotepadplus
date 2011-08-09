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
	            //���array.xml�е�������ԴgetStringArray���ص���һ��String����  
	            String text = AppContext.getResources().getStringArray(Items)[position];  
	            textView.setText(text);  
	            //���������С  
	            textView.setTextSize(24);  
	            AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(  
	                    LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);  
	            textView.setLayoutParams(layoutParams);  
	            //����ˮƽ�����Ͼ���  
	            textView.setGravity(android.view.Gravity.CENTER_VERTICAL);  
	            textView.setMinHeight(65);  
	            //����������ɫ  
	            textView.setTextColor(Color.BLACK);    
	            //����ͼ�������ֵ����  
	            textView.setCompoundDrawablesWithIntrinsicBounds(ImgIds[position], 0, 0, 0);  
	            //����textView���������µ�padding��С  
	            textView.setPadding(15, 0, 15, 0);  
	            //�������ֺ�ͼ��֮���padding��С  
	            textView.setCompoundDrawablePadding(15); 
	            return textView;  
	      }	          
	}  