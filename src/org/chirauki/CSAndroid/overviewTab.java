package org.chirauki.CSAndroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class overviewTab extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textview = new TextView(this);
        textview.setText("Overview");
        setContentView(textview);
    }
}
