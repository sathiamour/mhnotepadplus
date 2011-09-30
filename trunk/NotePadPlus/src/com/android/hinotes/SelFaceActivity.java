package com.android.hinotes;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class SelFaceActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selface);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
        // Grid view
		GridView FaceGrid = (GridView) findViewById(R.id.facegrid); 
		// Add Listeners
		FaceGrid.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				   
			}
		});
        // Fill faces
		ArrayList<HashMap<String, Object>> GridFaces = new ArrayList<HashMap<String, Object>>();
		int[] Faces = {R.drawable.face_01,R.drawable.face_02,R.drawable.face_03,R.drawable.face_04,R.drawable.face_05,R.drawable.face_06,R.drawable.face_07,R.drawable.face_08,R.drawable.face_09,R.drawable.face_10,R.drawable.face_11,R.drawable.face_12,R.drawable.face_13,R.drawable.face_14,R.drawable.face_15,R.drawable.face_16,R.drawable.face_17,R.drawable.face_18,R.drawable.face_19,R.drawable.face_20,R.drawable.face_21,R.drawable.face_22,R.drawable.face_23,R.drawable.face_24,R.drawable.face_25,R.drawable.face_26,R.drawable.face_27,R.drawable.face_28,R.drawable.face_29,R.drawable.face_30};
		for (int i = 0; i < 30; ++i) 
		{
			// Item
			HashMap<String, Object> Note = new HashMap<String, Object>();
			Note.put("FaceId", Faces[i]);
			GridFaces.add(Note);
	    }
		SimpleAdapter ImgAdapter = new SimpleAdapter(this, GridFaces, R.layout.faceitem, new String[] {"FaceId"}, new int[] {R.id.face});
		FaceGrid.setAdapter(ImgAdapter);
	}
}
