package org.chirauki.CSAndroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class Network_Tab extends Activity {
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("A ver si sacamos las IPs y eso");
        setContentView(textview);
    }
}
