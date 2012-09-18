package org.chirauki.CSAndroid;

import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Overview_Tab extends Activity {
	private static final int MREFRESH_ID = Menu.FIRST;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        updateFields();
    }
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateFields();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.add(0, MREFRESH_ID, 0, R.string.refresh);
        return true;
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case MREFRESH_ID:
                updateFields();
                return true;
        }

        return super.onMenuItemSelected(featureId, item);
	}
	
	private void updateFields() {
		Integer ram = 0;
        Integer cpu = 0;
        Long storage = 0l;
        Integer runningInstances = 0;
        Integer stoppedInstances = 0;
        Integer maximumInstances = 0;
        Integer usedVolumes = 0;
        Integer maxVolumes = 0;
        Integer usedSnap = 0;
        Integer maxSnap = 0;
        Integer usedTmpl = 0;
        Integer maxTmpl = 0;
        Integer usedIsos = 0;

        
        String clUrl = "";
        String clApik = "";
        String clSeck = "";
        
    	Bundle extras = getIntent().getExtras();
    	clUrl = extras.getString("clurl");
    	clApik = extras.getString("clapik");
    	clSeck = extras.getString("clseck");
    
    	setContentView(R.layout.cloud_overview);
    	
    	CSAPIexecutor client = new CSAPIexecutor(clUrl, clApik, clSeck, getApplicationContext());
        
    	JSONObject account;
    	try {
    		account = client.whoAmI();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return;
		} catch (ExecutionException e1) {
			e1.printStackTrace();
			return;
		}
    	//get instances
    	JSONArray vms = client.listVirtualMachines();
    	try {
    		for (int i = 0; i < vms.length(); i++) {
    			JSONObject tmpvm = (JSONObject) vms.getJSONObject(i);
    			// Update running / stopped count
    			if (tmpvm.getString("state").equals("Stopped")) {
    				stoppedInstances++;
    			} else if (tmpvm.getString("state").equals("Running")) {
    				runningInstances++;
				}
    			//Update CPU
    			cpu += tmpvm.getInt("cpunumber") * tmpvm.getInt("cpuspeed");
    			//Update RAM
    			ram += tmpvm.getInt("memory");
    		}
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	ram = ram / 1024;
    	//Used Volumes
    	JSONArray vols = client.listVolumes();
    	usedVolumes = vols.length();
    	try {
    		for (int i = 0; i < vols.length(); i++) {
    			JSONObject tmpvol = (JSONObject) vols.getJSONObject(i);
    			long tmp = (tmpvol.getLong("size"));
    			storage += (tmpvol.getLong("size"));
    		}
    		
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	//Used Snapshots
    	JSONArray snap = client.listSnapshots();
    	usedSnap = snap.length();
    	try {
    		for (int i = 0; i < snap.length(); i++) {
    			JSONObject tmpsnap = (JSONObject) snap.getJSONObject(i);
    			JSONObject tmpvol = (JSONObject) client.listVolumes(tmpsnap.getInt("volumeid"));
    			storage += (tmpvol.getLong("size"));
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	//Templates
    	JSONArray tmpl = client.listOwnTemplates();
    	usedTmpl = tmpl.length();
    	try {
    		for (int i = 0; i < tmpl.length(); i++) {
    			JSONObject tmptmpl = (JSONObject) tmpl.getJSONObject(i);
    			storage += (tmptmpl.getLong("size"));
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	//ISO
    	JSONArray isos = client.listOwnIsos();
    	usedIsos = isos.length();
    	try {
    		for (int i = 0; i < isos.length(); i++) {
    			JSONObject tmpiso = (JSONObject) isos.getJSONObject(i);
    			storage += (tmpiso.getLong("size"));
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	//Resource limits
    	JSONArray rlimits = client.listResourceLimits();
    	try {
    		for (int i = 0; i < rlimits.length(); i++) {
    			JSONObject tmplim = (JSONObject) rlimits.getJSONObject(i);
    			/*
    			 * Type of resource to update. Values are 0, 1, 2, 3, and 4. 
    			 * 0 - Instance. Number of instances a user can create. 
    			 * 1 - IP. Number of public IP addresses a user can own. 
    			 * 2 - Volume. Number of disk volumes a user can create.
    			 * 3 - Snapshot. Number of snapshots a user can create.
    			 * 4 - Template. Number of templates that a user can register/create.
    			 */
    			switch (tmplim.getInt("resourcetype")) {
    			case 0:
    				maximumInstances = tmplim.getInt("max");
    				break;
    			case 2:
    				maxVolumes = tmplim.getInt("max");
    				break;
    			case 3:
    				maxSnap = tmplim.getInt("max");
    				break;
    			case 4:
    				maxTmpl = tmplim.getInt("max");
    				break;
    			}
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    	
    	
    	storage = storage >> 30;
    	
    	// Update textviews
    	TextView tv = (TextView) findViewById(R.id.txt_cpu);
    	tv.setText("" + cpu.toString());
    	tv = (TextView) findViewById(R.id.txt_ram);
    	tv.setText("" + ram.toString());
    	tv = (TextView) findViewById(R.id.txt_running_instances);
    	tv.setText("" + runningInstances.toString());
    	tv = (TextView) findViewById(R.id.txt_stopped_instances);
    	tv.setText("" + stoppedInstances.toString());
    	tv = (TextView) findViewById(R.id.txt_used_vols);
    	tv.setText("" + usedVolumes.toString());
    	tv = (TextView) findViewById(R.id.txt_used_snap);
    	tv.setText("" + usedSnap.toString());
    	tv = (TextView) findViewById(R.id.txt_storage);
    	tv.setText("" + storage.toString());
    	tv = (TextView) findViewById(R.id.txt_used_templates);
    	tv.setText("" + usedTmpl.toString());
    	tv = (TextView) findViewById(R.id.txt_used_isos);
    	tv.setText("" + usedIsos.toString());
    	//TODO Arreglar para que ponga los maximos de aplicacion.
    	tv = (TextView) findViewById(R.id.txt_max_instances);
    	if (maximumInstances < 0 ) {
    		tv.setText("Unlimited");
    	} else {
    		tv.setText(maximumInstances.toString());
    	}
    	tv = (TextView) findViewById(R.id.txt_max_vols);
    	if (maxVolumes < 0 ) {
    		tv.setText("Unlimited");
    	} else {
    		tv.setText(maxVolumes.toString());
    	}
    	tv = (TextView) findViewById(R.id.txt_max_snap);
    	if (maxSnap < 0 ) {
    		tv.setText("Unlimited");
    	} else {
    		tv.setText(maxSnap.toString());
    	}
    	tv = (TextView) findViewById(R.id.txt_max_templates);
    	if (maxTmpl < 0 ) {
    		tv.setText("Unlimited");
    	} else {
    		tv.setText(maxTmpl.toString());
    	}
    	
	}
}

