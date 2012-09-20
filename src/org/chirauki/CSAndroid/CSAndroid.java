package org.chirauki.CSAndroid;

import org.json.JSONObject;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class CSAndroid extends ListActivity {
	private static final int MENUCLOUD_ID = Menu.FIRST;
	private static final int CTXEDIT_ID = MENUCLOUD_ID + 1;
	private static final int CTXDELETE_ID = CTXEDIT_ID + 1;
	
	private static final int REQ_ADD = 0;
	private static final int REQ_EDIT = 1;
	
	private static CSAndroidDbAdapter mDbHelper;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new CSAndroidDbAdapter(this);
        //mDbHelper.open();
        //mDbHelper.deleteDatabase();
        //mDbHelper.deleteAll();
        //mDbHelper.execSQL("DROP TABLE clouds");
        fillCloudList();
        setContentView(R.layout.cloud_list);
        registerForContextMenu(this.getListView());
        
    }
    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);

    	mDbHelper.open();
    	Cursor c = mDbHelper.fetchCloud(id);
    	startManagingCursor(c);
		
		Intent in = new Intent(this, Cloud.class);
		
		CSAPIexecutor cs = new CSAPIexecutor(c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_URL)).trim(), 
				c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_USERNAME)).trim(),
				c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_PASS)).trim(),
				c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_DOMAIN)).trim(),
				getApplicationContext());
		
		if (cs.loginUser()) {
			Gson gson = new Gson();
			
			String p = gson.toJson(cs);

			in.putExtra("csclient", p);
			
			mDbHelper.close();
			
			startActivity(in);
		} else {
			Toast.makeText(getApplicationContext(), "Could not login", Toast.LENGTH_SHORT).show();
			mDbHelper.close();
		}
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENUCLOUD_ID, 0, R.string.add_cloud);
        return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case MENUCLOUD_ID:
                newCloud();
                return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }
 
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		// TODO Auto-generated method stub
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CTXEDIT_ID, 0, R.string.cl_edit);
		menu.add(0, CTXDELETE_ID, 0, R.string.cl_delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		
		switch (item.getItemId()) {
			case CTXDELETE_ID:
				mDbHelper.open();
				AdapterContextMenuInfo infodel = (AdapterContextMenuInfo) item.getMenuInfo();
				mDbHelper.deleteCloud(infodel.id);
				mDbHelper.close();
				fillCloudList();
				return true;
			case CTXEDIT_ID:
				AdapterContextMenuInfo infoed = (AdapterContextMenuInfo) item.getMenuInfo();
				Intent in = new Intent(this, AddCloud.class);
				in.putExtra("clid", infoed.id);
				startActivityForResult(in, REQ_EDIT);		
				return true;
		}
		return true;
	}
    
    private void newCloud() {
    	Intent in = new Intent(this, AddCloud.class);
    	startActivityForResult(in, REQ_ADD);
    }
    
    private void fillCloudList() {
    	mDbHelper.open();
    	Cursor allclouds = mDbHelper.fetchAllClouds();
    	startManagingCursor(allclouds);
    	
    	// Create an array to specify the fields we want to display in the list (only TITLE)
        //String[] from = new String[]{CSAndroidDbAdapter.KEY_ROWID,CSAndroidDbAdapter.KEY_NAME};
    	String[] from = new String[]{CSAndroidDbAdapter.KEY_NAME, CSAndroidDbAdapter.KEY_USERNAME,
    			CSAndroidDbAdapter.KEY_DOMAIN, CSAndroidDbAdapter.KEY_USRTYPE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        //int[] to = new int[]{R.id.row_ID, R.id.row_name};
        int[] to = new int[]{R.id.cloud_name, R.id.txt_username, 
        		 R.id.txt_domain, R.id.txt_usertype};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter clouds = new SimpleCursorAdapter(this, R.layout.cloud_row, allclouds, from, to);
        setListAdapter(clouds);
        
        mDbHelper.close();
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		fillCloudList();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if(this.getListAdapter().isEmpty()) { fillCloudList();  }
	}
}
