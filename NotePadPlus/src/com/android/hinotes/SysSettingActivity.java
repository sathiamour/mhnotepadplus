package com.android.hinotes;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.WindowManager;

public class SysSettingActivity extends PreferenceActivity 
       implements OnPreferenceChangeListener, OnPreferenceClickListener
{
	private String FullScreenDispKey;
	private String FontSizeKey;
	private String OrderByKey;
	private String BgClrKey;
	private String ItemHeightKey;
	private CheckBoxPreference FullScreenDispPref;
	private ListPreference FontSizePref;
	private ListPreference OrderByPref;
	private ListPreference ItemHeightPref;
	private Preference BgClr;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferencei);
		// Get preference
		FullScreenDispKey = getString(R.string.pref_fullscreen_key);
		FontSizeKey = getString(R.string.pref_fontsize_key);
		OrderByKey = getString(R.string.pref_orderby_key);
		BgClrKey = getString(R.string.pref_bgclr_key);
		ItemHeightKey = getString(R.string.pref_itemheight_key);
		FullScreenDispPref = (CheckBoxPreference)findPreference(FullScreenDispKey);
		FontSizePref = (ListPreference)findPreference(FontSizeKey);
		OrderByPref = (ListPreference)findPreference(OrderByKey);
		ItemHeightPref = (ListPreference)findPreference(ItemHeightKey);
		BgClr = (Preference)findPreference(BgClrKey);

		// Register listener
		FullScreenDispPref.setOnPreferenceChangeListener(this);
		FullScreenDispPref.setOnPreferenceClickListener(this);
		FontSizePref.setOnPreferenceChangeListener(this);
		FontSizePref.setOnPreferenceClickListener(this);
		OrderByPref.setOnPreferenceChangeListener(this);
		OrderByPref.setOnPreferenceClickListener(this);
		ItemHeightPref.setOnPreferenceChangeListener(this);
		ItemHeightPref.setOnPreferenceClickListener(this);
		BgClr.setOnPreferenceChangeListener(this);
		BgClr.setOnPreferenceClickListener(this);
		
		// Initialize summary
		String[] FontSizeArray = getResources().getStringArray(R.array.fontsize);
		FontSizePref.setSummary(FontSizeArray[Integer.parseInt(NotePadPlus.SysSettings.FontSize)]);
		String[] OrderByArray = getResources().getStringArray(R.array.orderby);
		OrderByPref.setSummary(OrderByArray[Integer.parseInt(NotePadPlus.SysSettings.OrderBy)]);
		String[] ItemHeightArray = getResources().getStringArray(R.array.itemheight);
		ItemHeightPref.setSummary(ItemHeightArray[Integer.parseInt(NotePadPlus.SysSettings.ItemHeight)]);
	}

	@Override
	public boolean onPreferenceChange(Preference Pref, Object NewVal) {
		// TODO Auto-generated method stub
		if( Pref.getKey().equals(FullScreenDispKey) )
		{ 
		    WindowManager.LayoutParams attrs = getWindow().getAttributes();  
			if( NewVal.toString().equals(ProjectConst.True) ) {
 			    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;   
			    getWindow().setAttributes(attrs);   
			    getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS); 
			    NotePadPlus.SysSettings.IsFullScreen = true;
			} else {
				attrs.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);   
				getWindow().setAttributes(attrs);   
				getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
				NotePadPlus.SysSettings.IsFullScreen = false;
			}
		} else if( Pref.getKey().equals(FontSizeKey) ) {
			String[] FontSizeArray = getResources().getStringArray(R.array.fontsize);
		    Pref.setSummary(FontSizeArray[Integer.parseInt(NewVal.toString())]);
		    NotePadPlus.SysSettings.FontSize = NewVal.toString();
		} else if( Pref.getKey().equals(OrderByKey) ) {
			String[] OrderByArray = getResources().getStringArray(R.array.orderby);
		    Pref.setSummary(OrderByArray[Integer.parseInt(NewVal.toString())]);
		    NotePadPlus.SysSettings.OrderBy = NewVal.toString();
		}else if( Pref.getKey().equals(ItemHeightKey) ) {
			String[] ItemHeightArray = getResources().getStringArray(R.array.itemheight);
			ItemHeightPref.setSummary(ItemHeightArray[Integer.parseInt(NewVal.toString())]);
			NotePadPlus.SysSettings.ItemHeight = NewVal.toString();
		}else
			return false;
		return true;
	}
    
	@Override
	public boolean onPreferenceClick(Preference Pref) {
		// TODO Auto-generated method stub
		return true;
	}
	 @Override 
	 public void onBackPressed(){
		 setResult(RESULT_OK);
		 finish();
	 }
}
