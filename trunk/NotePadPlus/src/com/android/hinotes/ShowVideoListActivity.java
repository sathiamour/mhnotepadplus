package com.android.hinotes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ShowVideoListActivity extends Activity {
	// Media uri
	private Vector<Uri> MediaUri;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showmedialist);
		
		MediaUri = new Vector<Uri>();
		int Count = getIntent().getIntExtra(ProjectConst.Key_Count, ProjectConst.Zero);
		for( int i = 0; i < Count; ++i )
			 MediaUri.add(Uri.parse(getIntent().getStringExtra(ProjectConst.Key_Uri+Integer.toString(i))));

        // List view
		ListView AudioList = (ListView) findViewById(R.id.audiolist); 
		// Add Listeners
		AudioList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				   Intent intent = new Intent(Intent.ACTION_VIEW);   
				   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				   intent.setDataAndType(MediaUri.get(position), "video/*");   
				   startActivity(intent);  
			}
		});
		
		// Set adapter
		ArrayList<HashMap<String, Object>> Items = new ArrayList<HashMap<String, Object>>();
		for( int i = 0; i < Count; ++i ) 
		{
            // Create one item
			HashMap<String, Object> OneItem = new HashMap<String, Object>();
			// Set list item's data
			OneItem.put("Img", R.drawable.video_icon);
			OneItem.put("Title", " ”∆µ»’÷æ"+Integer.toString(i+1));
			Items.add(OneItem);  
		}
		
		SimpleAdapter ListItemAdapter = new SimpleAdapter(this, Items, R.layout.medialistitem, new String[] {"Img", "Title"}, new int[] {R.id.img, R.id.title});
		AudioList.setAdapter(ListItemAdapter);
    }
}
