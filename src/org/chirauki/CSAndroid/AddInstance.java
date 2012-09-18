package org.chirauki.CSAndroid;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class AddInstance extends Activity {
	private CSAPIexecutor client;
	private String clUrl = "";
	private String clApik = "";
	private String clSeck = "";
	
	private ArrayList<Integer> tmplids = new ArrayList<Integer>();
	private ArrayList<Integer> isoids = new ArrayList<Integer>();
	private ArrayList<Integer> svcids = new ArrayList<Integer>();
	private ArrayList<Integer> dskids = new ArrayList<Integer>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		Bundle extras = getIntent().getExtras();
		clUrl = extras.getString("clurl");
		clApik = extras.getString("clapik");
		clSeck = extras.getString("clseck");
		client = new CSAPIexecutor(clUrl, clApik, clSeck, getApplicationContext());
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.add_instance);
		
		Spinner spSource = (Spinner) findViewById(R.id.spinner_source);
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, 
				R.array.instances_sources_array, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spSource.setAdapter(adapter);
	    spSource.setContentDescription("Source");
	    spSource.setOnItemSelectedListener(new spinnerSourceListener());
	    
	    Spinner spSvc = (Spinner) findViewById(R.id.spinner_svc_offering);
		ArrayAdapter<String> adapter_svc = getSvcAdapter();
		adapter_svc.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spSvc.setAdapter(adapter_svc);
	}
	
	private ArrayAdapter<String> getTemplatesAdapter(Integer type) {
		//TODO rellenar el spinner de templates, en funcion de si son ISos o templates
		ArrayList<String> templs = new ArrayList<String>();
		JSONArray templates = null;
		switch (type) {
		case 0: //TEMPLATES
			templates = client.listTemplates();
			break;
		case 1:
			templates = client.listIsos();
			break;
		}
		try {
			for (int i = 0; i < templates.length(); i++) {
				//HashMap n = new HashMap<Integer, String>();
				JSONObject tmpl = templates.getJSONObject(i);
				switch (type) {
				case 0: //TEMPLATES
					tmplids.add(tmpl.getInt("id"));
					break;
				case 1:
					isoids.add(tmpl.getInt("id"));
					break;
				}
				templs.add(tmpl.getString("name"));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ArrayAdapter<String> adap = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, templs);
		adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
		return adap;
	}
	
	private ArrayAdapter<String> getSvcAdapter() {
		//TODO rellenar el spinner de templates, en funcion de si son ISos o templates
		ArrayList<String> offerings = new ArrayList<String>();
		JSONArray offers = client.listServiceOfferings();
		
		try {
			for (int i = 0; i < offers.length(); i++) {
				//HashMap n = new HashMap<Integer, String>();
				JSONObject svc = offers.getJSONObject(i);
				String name = svc.getString("name") + 
						" (" + svc.getInt("cpunumber") + "x" + (float) svc.getInt("cpuspeed") / 1000 + 
						"GHz," + (float) svc.getInt("memory") / 1024 + "GB";
				if (svc.getBoolean("offerha")) {
					name = name + ",HA)";
				} else {
					name = name +  ")"; 
				}
				svcids.add(svc.getInt("id"));
				offerings.add(name);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ArrayAdapter<String> adap = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, offerings);
		adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
		return adap;
	}
	
	private ArrayAdapter<String> getDiskAdapter(Integer type) {
		//TODO rellenar el spinner de templates, en funcion de si son ISos o templates
		ArrayList<String> offerings = new ArrayList<String>();
		JSONArray offers = client.listDiskOfferings();
		switch(type) {
		case 0: //Templates
			offerings.add("None");
			break;
		}
		try {
			for (int i = 0; i < offers.length(); i++) {
				//HashMap n = new HashMap<Integer, String>();
				JSONObject svc = offers.getJSONObject(i);
				String name = svc.getString("name") + " (" + svc.getInt("disksize") + "GB)";
				dskids.add(svc.getInt("id"));
				offerings.add(name);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ArrayAdapter<String> adap = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, offerings);
		adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
		return adap;
	}
	
	private ArrayAdapter<String> getNetAdapter() {
		//TODO rellenar el spinner de templates, en funcion de si son ISos o templates
		ArrayList<String> nets = new ArrayList<String>();
		JSONArray networks = client.listNetworks();
		
		try {
			for (int i = 0; i < networks.length(); i++) {
				//HashMap n = new HashMap<Integer, String>();
				JSONObject net = networks.getJSONObject(i);
				if (net.getBoolean("isdefault")) {
					nets.add("Default");
				} else {
					nets.add(net.getString("name"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		ArrayAdapter<String> adap = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_multiple_choice, nets);
		
		return adap;
	}
	
	private void nextStep() {
		Spinner spTmpl = (Spinner) findViewById(R.id.spinner_source_tmpl);
		
		int tmpl_id = tmplids.get(spTmpl.getSelectedItemPosition()); 
	}
	
	private class spinnerSourceListener implements OnItemSelectedListener {
		
        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        	Spinner spTmpl = (Spinner) findViewById(R.id.spinner_source_tmpl);
    		ArrayAdapter<String> adap = getTemplatesAdapter(pos);
	    	adap.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spTmpl.setAdapter(adap);
            
            Spinner disk = (Spinner) findViewById(R.id.spinner_data_disk);
            ArrayAdapter<String> adapdisk = getDiskAdapter(pos);
            adapdisk.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            disk.setAdapter(adapdisk);
	    }

	    public void onNothingSelected(AdapterView parent) {
	      // Do nothing.
	    }
	}
}
