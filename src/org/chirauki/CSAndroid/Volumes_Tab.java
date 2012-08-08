package org.chirauki.CSAndroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Volumes_Tab extends Activity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("Esos disquitossss");
        setContentView(textview);
    }
}
