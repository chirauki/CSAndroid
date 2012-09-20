package org.chirauki.CSAndroid;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

import com.google.gson.Gson;

public class Cloud extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    String jsonClient = "";
        
        Bundle extras = getIntent().getExtras();
    	jsonClient = extras.getString("csclient");
    	
    	if (savedInstanceState != null) {
        	
        }
	    setContentView(R.layout.cloud_view);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, Overview_Tab.class);
	    intent.putExtra("csclient", jsonClient);
	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("overview").setContent(intent).setIndicator("Overview");
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, Instances_Tab.class);
	    intent.putExtra("csclient", jsonClient);
	    spec = tabHost.newTabSpec("instances").setContent(intent).setIndicator("Instances");
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, Volumes_Tab.class);
	    spec = tabHost.newTabSpec("volumes").setContent(intent).setIndicator("Volumes");
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Network_Tab.class);
	    spec = tabHost.newTabSpec("network").setContent(intent).setIndicator("Network");
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(1);
	}
}
