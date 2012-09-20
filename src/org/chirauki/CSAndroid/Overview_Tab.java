package org.chirauki.CSAndroid;

import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.gson.Gson;

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

        
        Bundle extras = getIntent().getExtras();
        String jsonClient = extras.getString("csclient");
    	
    	Gson gson = new Gson();
    	
    	CSAPIexecutor cs = gson.fromJson(jsonClient, CSAPIexecutor.class);
    	cs.setContext(getApplicationContext());

    	setContentView(R.layout.cloud_overview);
    	
    	JSONObject account;
    	try {
    		account = cs.listAccounts();

    		if (account != null) {
    			TextView t = new TextView(getApplicationContext());
    			
    			runningInstances = account.getInt("vmrunning");
    			t = (TextView) findViewById(R.id.txt_running_instances);
    	    	t.setText("" + runningInstances.toString());
    	    	
    			stoppedInstances = account.getInt("vmstopped");
    			t = (TextView) findViewById(R.id.txt_stopped_instances);
    	    	t.setText("" + stoppedInstances.toString());
    	    	
    	    	Object tmp = account.get("vmstopped");
    	    	try {
    	    		maximumInstances = Integer.parseInt(tmp.toString());
    	    		t = (TextView) findViewById(R.id.txt_max_instances);
        	    	t.setText("" + maximumInstances.toString());
    	    	} catch (NumberFormatException e) {
    	    		t = (TextView) findViewById(R.id.txt_max_instances);
        	    	t.setText("" + tmp.toString());
    	    	}
    	    	
    	    	usedVolumes = account.getInt("volumetotal");
    	    	t = (TextView) findViewById(R.id.txt_used_vols);
    	    	t.setText("" + usedVolumes.toString());
    	    	
    	    	tmp = account.get("volumeavailable");
    	    	try {
    	    		maxVolumes = Integer.parseInt(tmp.toString());
    	    		t = (TextView) findViewById(R.id.txt_max_vols);
        	    	t.setText("" + maxVolumes.toString());
    	    	} catch (NumberFormatException e) {
    	    		t = (TextView) findViewById(R.id.txt_max_vols);
        	    	t.setText("" + tmp.toString());
    	    	}
    	    	
    	    	usedSnap = account.getInt("snapshottotal");
    	    	t = (TextView) findViewById(R.id.txt_used_snap);
    	    	t.setText("" + usedSnap.toString());
    	    	
    	    	tmp = account.get("snapshotavailable");
    	    	try {
    	    		maxSnap = Integer.parseInt(tmp.toString());
    	    		t = (TextView) findViewById(R.id.txt_max_snap);
        	    	t.setText("" + maxSnap.toString());
    	    	} catch (NumberFormatException e) {
    	    		t = (TextView) findViewById(R.id.txt_max_snap);
        	    	t.setText("" + tmp.toString());
    	    	}
    	    	
    	    	tmp = account.get("templatelimit");
    	    	try {
    	    		maxTmpl = Integer.parseInt(tmp.toString());
    	    		t = (TextView) findViewById(R.id.txt_max_templates);
        	    	t.setText("" + maxTmpl.toString());
    	    	} catch (NumberFormatException e) {
    	    		t = (TextView) findViewById(R.id.txt_max_templates);
        	    	t.setText("" + tmp.toString());
    	    	}
    		
    	    	//get instances
    	    	JSONArray vms = cs.listVirtualMachines();
    	    	try {
    	    		for (int i = 0; i < vms.length(); i++) {
    	    			JSONObject tmpvm = (JSONObject) vms.getJSONObject(i);
    	    			//Update CPU
    	    			cpu += tmpvm.getInt("cpunumber") * tmpvm.getInt("cpuspeed");
    	    			//Update RAM
    	    			ram += tmpvm.getInt("memory");
    	    		}
    	    	} catch (Exception e) {
    	    		e.printStackTrace();
    	    	}
    	    	ram = ram / 1024;
    	    	cpu = cpu / 1000;

    	    	t = (TextView) findViewById(R.id.txt_cpu);
    	    	t.setText("" + cpu.toString());
    	    	t = (TextView) findViewById(R.id.txt_ram);
    	    	t.setText("" + ram.toString());

    	    	//Used Volumes
    	    	JSONArray vols = cs.listVolumes();
    	    	usedVolumes = vols.length();
    	    	t = (TextView) findViewById(R.id.txt_used_vols);
    	    	t.setText("" + usedVolumes.toString());
    	    	try {
    	    		for (int i = 0; i < vols.length(); i++) {
    	    			JSONObject tmpvol = (JSONObject) vols.getJSONObject(i);
    	    			//long tmp2 = (tmpvol.getLong("size"));
    	    			storage += (tmpvol.getLong("size"));
    	    		}
    	    		
    	    	} catch (Exception e) {
    	    		e.printStackTrace();
    	    	}
    	    	
    	    	//Used Snapshots
    	    	JSONArray snap = cs.listSnapshots();
    	    	usedSnap = snap.length();
    	    	t = (TextView) findViewById(R.id.txt_used_snap);
    	    	t.setText("" + usedSnap.toString());
    	    	try {
    	    		for (int i = 0; i < snap.length(); i++) {
    	    			JSONObject tmpsnap = (JSONObject) snap.getJSONObject(i);
    	    			JSONObject tmpvol = (JSONObject) cs.listVolumes(tmpsnap.getInt("volumeid"));
    	    			storage += (tmpvol.getLong("size"));
    	    		}
    	    	} catch (Exception e) {
    	    		e.printStackTrace();
    	    	}
    	    	
    	    	//Templates
    	    	JSONArray tmpl = cs.listOwnTemplates();
    	    	usedTmpl = tmpl.length();
    	    	t = (TextView) findViewById(R.id.txt_used_templates);
    	    	t.setText("" + usedTmpl.toString());
    	    	try {
    	    		for (int i = 0; i < tmpl.length(); i++) {
    	    			JSONObject tmptmpl = (JSONObject) tmpl.getJSONObject(i);
    	    			storage += (tmptmpl.getLong("size"));
    	    		}
    	    	} catch (Exception e) {
    	    		e.printStackTrace();
    	    	}
    	    	//ISO
    	    	JSONArray isos = cs.listOwnIsos();
    	    	usedIsos = isos.length();
    	    	t = (TextView) findViewById(R.id.txt_used_isos);
    	    	t.setText("" + usedIsos.toString());
    	    	try {
    	    		for (int i = 0; i < isos.length(); i++) {
    	    			JSONObject tmpiso = (JSONObject) isos.getJSONObject(i);
    	    			storage += (tmpiso.getLong("size"));
    	    		}
    	    	} catch (Exception e) {
    	    		e.printStackTrace();
    	    	}
    	    	
    	    	storage = storage >> 30;
    	    	t = (TextView) findViewById(R.id.txt_storage);
    	    	t.setText("" + storage.toString());
    		}
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
	}
}

