package org.chirauki.CSAndroid;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class cloud_tabs extends TabActivity {
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.cloud_view);

	    Resources res = getResources(); // Resource object to get Drawables
	    TabHost tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	    Intent intent;  // Reusable Intent for each tab

	    // Create an Intent to launch an Activity for the tab (to be reused)
	    intent = new Intent().setClass(this, Overview_Tab.class);

	    // Initialize a TabSpec for each tab and add it to the TabHost
	    spec = tabHost.newTabSpec("overview").setContent(intent);
	    tabHost.addTab(spec);

	    // Do the same for the other tabs
	    intent = new Intent().setClass(this, Instances_Tab.class);
	    spec = tabHost.newTabSpec("instances").setContent(intent);
	    tabHost.addTab(spec);

	    intent = new Intent().setClass(this, Volumes_Tab.class);
	    spec = tabHost.newTabSpec("volumes").setContent(intent);
	    tabHost.addTab(spec);
	    
	    intent = new Intent().setClass(this, Network_Tab.class);
	    spec = tabHost.newTabSpec("network").setContent(intent);
	    tabHost.addTab(spec);

	    tabHost.setCurrentTab(2);
	}
}
