package org.chirauki.CSAndroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SimpleAdapter;

public class Instances_Tab extends ListActivity {
	private static final int MREFRESH_ID = Menu.FIRST;
	private static final int MCREATE_ID = MREFRESH_ID + 1;
	
	private static final int CTXSTART_ID = Menu.FIRST;
	private static final int CTXSTOP_ID = CTXSTART_ID + 1;
	private static final int CTXDESTROY_ID = CTXSTOP_ID + 1;
	private static final int CTXATTACH_ID = CTXDESTROY_ID + 1;
	private static final int CTXDETACH_ID = CTXATTACH_ID + 1;
	private static final int CTXRESETPWD_ID = CTXDETACH_ID + 1;
	private static final int CTXCHOFFERING_ID = CTXRESETPWD_ID + 1;
	
	CSAPIexecutor cs;
	ProgressDialog progDialog;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.instances_list);
		
		progDialog = new ProgressDialog(this);
		progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		
		Bundle extras = getIntent().getExtras();
		String jsonClient = extras.getString("csclient");
    	
		Gson gson = new Gson();
    	
    	this.cs = gson.fromJson(jsonClient, CSAPIexecutor.class);
    	cs.setContext(getApplicationContext());
		
		fillList();
		
		registerForContextMenu(this.getListView());	
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu);
		menu.add(0, MREFRESH_ID, 0, R.string.refresh);
		menu.add(0, MCREATE_ID, 0, R.string.instances_add_new);
        return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch(item.getItemId()) {
        case MREFRESH_ID:
            fillList();
            return true;
        case MCREATE_ID:
        	Intent in = new Intent(this, AddInstance.class);
        	Gson gson = new Gson();
        	in.putExtra("csclient", gson.toJson(cs));
    		startActivity(in);
        	return true;
	    }
	    return super.onOptionsItemSelected(item);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		SimpleAdapter a = (SimpleAdapter) this.getListView().getAdapter();
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		HashMap<String, String> mapa = (HashMap<String, String>) a.getItem(info.position);
		String instanceID = mapa.get("id").toString();
		switch (getSelectedInstanceStatus(instanceID)) {
		case 0: //Running
			menu.add(0, CTXSTOP_ID, 0, R.string.instances_stop);
			break;
		case 1: //Stopped
			menu.add(0, CTXSTART_ID, 0, R.string.instances_start);
			menu.add(0, CTXRESETPWD_ID, 0, R.string.instances_resetpwd);
			menu.add(0, CTXCHOFFERING_ID, 0, R.string.instances_choffering);
			break;
		}
		switch(hasISOAttached(instanceID)) {
		case 0: //NO ISO
			menu.add(0, CTXATTACH_ID, 0, R.string.instances_destroy);
			break;
		case 1: //HAS ISO
			menu.add(0, CTXDETACH_ID, 0, R.string.instances_detach);
			break;
		}
		
		menu.add(0, CTXDESTROY_ID, 0, R.string.instances_destroy);
	}

	private void fillList() {
		List<HashMap<String, String>> inst = getInstancesList(); 
        String[] from = new String[] {"instance_name", "instance_disp_name", "os_type", "offering_name", "state"};
    	int[] to = new int[] { R.id.txt_instance_name, R.id.txt_instance_name_id, R.id.txt_os_name, R.id.txt_offering_name, R.id.txt_state };
        SimpleAdapter adapter = new SimpleAdapter(this, inst, R.layout.instance_row, from, to);
        setListAdapter(adapter);
        setContentView(R.layout.instances_list);
        registerForContextMenu(this.getListView());	
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		super.onContextItemSelected(item);
		switch (item.getItemId()) {
			case CTXSTART_ID:
				AdapterContextMenuInfo infostart = (AdapterContextMenuInfo) item.getMenuInfo();
				startInstance(infostart.position);
				fillList();
				return true;
			case CTXSTOP_ID:
				AdapterContextMenuInfo infostop = (AdapterContextMenuInfo) item.getMenuInfo();
				stopInstance(infostop.position);
				fillList();
				return true;
			case CTXDESTROY_ID:
				AdapterContextMenuInfo infodest = (AdapterContextMenuInfo) item.getMenuInfo();
				return true;
		}
		return true;
	}
	
	private int getSelectedInstanceStatus(String id) {
		int status = -1;
		/*SimpleAdapter a = (SimpleAdapter) this.getListView().getAdapter();
		HashMap<String, String> mapa = (HashMap<String, String>) a.getItem(pos);
		int instanceID = Integer.parseInt(mapa.get("id").toString());*/
		JSONObject vminstance = cs.listVirtualMachines(id);
		try {
			String state = vminstance.getString("state");
			if (state.equals("Running")) {
				status = 0;
			} else if (state.equals("Stopped")) {
				status = 1;
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return status;
	}
	
	private int hasISOAttached(String id) {
		int status = -1;
		/*SimpleAdapter a = (SimpleAdapter) this.getListView().getAdapter();
		HashMap<String, String> mapa = (HashMap<String, String>) a.getItem(pos);
		int instanceID = Integer.parseInt(mapa.get("id").toString());*/
		JSONObject vminstance = cs.listVirtualMachines(id);
		
		if (vminstance.has("isoid")) {
			return 1;
		} else {
			return 0;
		}
	}
	
	private void stopInstance(int pos) {
		SimpleAdapter a = (SimpleAdapter) this.getListView().getAdapter();
		HashMap<String, String> mapa = (HashMap<String, String>) a.getItem(pos);
		String instanceID = mapa.get("id").toString();
		new stopTask().execute(instanceID);
	}
	
	private void startInstance(int pos) {
		SimpleAdapter a = (SimpleAdapter) this.getListView().getAdapter();
		HashMap<String, String> mapa = (HashMap<String, String>) a.getItem(pos);
		String instanceID = mapa.get("id").toString();
		new startTask().execute(instanceID);
	}
	
	private List<HashMap<String, String>> getInstancesList() {
    	//List of hashmaps with needed info
    	List<HashMap<String, String>> instances = new ArrayList<HashMap<String, String>>();
    	try {
    		//get instances
    		JSONArray vms = cs.listVirtualMachines();
    		for (int i = 0; i < vms.length(); i++) {
    			JSONObject tmpvm = (JSONObject) vms.getJSONObject(i);
    			JSONObject ostype = (JSONObject) cs.listOsTypes(tmpvm.getString("guestosid"));

    			HashMap<String, String> map = new HashMap<String, String>();
    			map.put("instance_name", tmpvm.getString("name"));
    			map.put("instance_disp_name", tmpvm.getString("displayname"));
    			map.put("os_type", ostype.getString("description"));
    			map.put("offering_name", tmpvm.getString("serviceofferingname"));
    			map.put("state", tmpvm.getString("state"));
    			map.put("id", tmpvm.getString("id"));

    			instances.add(map);
    		}
		} catch (JSONException e) {
    		e.printStackTrace();
    	}

    	return instances;
	}
	
	private class startTask extends AsyncTask<String, Void, Integer> {
		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub
			String instance = params[0];
			cs.startVirtualMachine(instance);
			return -1;
		}

		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progDialog.dismiss();
			fillList();
		}
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progDialog.setMessage(getString(R.string.instance_starting));
			progDialog.show();
		}
	 }
	
	private class stopTask extends AsyncTask<String, Void, Integer> {
		@Override
		protected Integer doInBackground(String... params) {
			// TODO Auto-generated method stub
			String instance = params[0];
			cs.stopVirtualMachine(instance);
			return -1;
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			progDialog.dismiss();
			fillList();
		}
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progDialog.setMessage(getString(R.string.instance_stopping));
			progDialog.show();
		}
	}
	
	private class refresh extends AsyncTask<Void, Void, JSONArray> {
		@Override
		protected JSONArray doInBackground(Void... params) {
			JSONArray vms;
			vms = cs.listVirtualMachines();
			return vms;
		}
	}
}
