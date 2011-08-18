package com.android.hinotes;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hinotes.ProjectConst;

public class CheckListItemAdapter extends BaseAdapter {

            private Context AppContext;
	        private ArrayList<String> Items = new ArrayList<String>();
	        //最好是list数据结构
	        public CheckListItemAdapter(Context c) {
	        	AppContext = c;
	        	Items = new ArrayList<String>();
	        }

	        public CheckListItemAdapter(Context c, ArrayList<String> I) {
	        	AppContext = c;
	        	Items = I;
	        }
	        
	        public int getCount() {
	            return Items.size();
	        }

	        public Object getItem(int position) {
	            return position;
	        }

	        public long getItemId(int position) {
	            return position;
	        }

	        public View getView(int position, View convertView, ViewGroup parent) {
	        	LinearLayout Item = new LinearLayout(AppContext);
	        	Item.setGravity(Gravity.CENTER_VERTICAL);
	        	ImageView Tag = new ImageView(AppContext);
	        	if( position == 0 || position == Items.size()-1 )
	        	    Tag.setImageResource(android.R.drawable.ic_menu_add);
	        	else 
	        		Tag.setImageResource(android.R.drawable.ic_menu_agenda);
	        	TextView Title = new TextView(AppContext);
	        	Title.setText(Items.get(position));
	        	Title.setSingleLine();
                Title.setEllipsize(TruncateAt.END);
	        	Title.setTextSize(18);
	        	Title.setTextColor(Color.BLACK);
	        	Item.addView(Tag);Item.addView(Title);
	            return Item;
	        }

	        public ArrayList<String> GetItems()
	        {
	        	return Items;
	        }
	        
	        public void SetItems(ArrayList<String> I)
	        {
	        	Items = null;
	        	Items = I;
	            notifyDataSetChanged();
	        }
	        
            public String GetItemAt(int Idx)
            {
            	return Items.get(Idx);
            }
            
            public void SetItemAt(int Idx, String NewContent)
            {
            	Items.set(Idx, NewContent);
	            notifyDataSetChanged();
            }
            
	        public void RemoveItemAt(int Idx) 
	        {
	            Items.remove(Idx);
	            notifyDataSetChanged();
	        }
	        
	        public void PopFrontItem() {
	            Items.remove(Items.size()-1);
	            notifyDataSetChanged();
	        }
	        
	        public void PopBackItem() {
	            Items.remove(ProjectConst.One);
	            notifyDataSetChanged();
	        }
	        
	        public void PushItem(String ItemString) {
	            Items.add(ItemString);
	            notifyDataSetChanged();
	        }
	        
	        public void PushBackItem(String ItemString) {
	            Items.add(Items.size()-1, ItemString);
	            notifyDataSetChanged();
	        }
	        
	        public void PushFontItem(String ItemString){
	        	Items.add(ProjectConst.One, ItemString);
	            notifyDataSetChanged();
	        }

}
