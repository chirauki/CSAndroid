package org.chirauki.CSAndroid;

import java.util.HashMap;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class AddCloud extends Activity {

	private CSAndroidDbAdapter mDbHelper;
	private EditText txtName;
	private EditText txtUrl;
	private EditText txtUser;
	private EditText txtPass;
	private EditText txtDomain;
	
	private Long mRowId;
	
	Button btnSaveCloud;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mDbHelper = new CSAndroidDbAdapter(this);
			
		mRowId = null;
        
        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(CSAndroidDbAdapter.KEY_ROWID);
        if (mRowId == null) {
        	Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong("clid")
                                    : null;
        }
		
		setContentView(R.layout.add_cloud);
		txtName = (EditText) findViewById(R.id.clName);
		txtUrl = (EditText) findViewById(R.id.clUrl);
		txtUser = (EditText) findViewById(R.id.clUsername);
		txtPass = (EditText) findViewById(R.id.clPass);
		txtDomain = (EditText) findViewById(R.id.clDomain);
		fillForm();
	}

	public void saveCloud(View view) {
		mDbHelper = new CSAndroidDbAdapter(this);
		
		txtName = (EditText) findViewById(R.id.clName);
		txtUrl = (EditText) findViewById(R.id.clUrl);
		txtUser = (EditText) findViewById(R.id.clUsername);
		txtPass = (EditText) findViewById(R.id.clPass);
		txtDomain = (EditText) findViewById(R.id.clDomain);
		
		HashMap<String, String> data = new HashMap<String, String>(); 
 		data.put("user", txtUser.getText().toString());
 		data.put("pass", txtPass.getText().toString());
 		data.put("domain", txtDomain.getText().toString());
 		data.put("url", txtUrl.getText().toString());
		
 		CSAPIexecutor cli = new CSAPIexecutor(data.get("url"), data.get("user"),
				data.get("pass"), data.get("domain"), getApplicationContext());
 		if (cli.loginUser()) {
 			if (mRowId == null) {
 				mDbHelper.open();
 				mDbHelper.createCloud(txtName.getText().toString(),
 						data.get("url"), data.get("user"),data.get("pass"),data.get("domain"));
 				mDbHelper.close();
 			} else {
 				mDbHelper.open();
 				mDbHelper.updateCloud(mRowId, txtName.getText().toString(),
 						data.get("url"), data.get("user"),data.get("pass"),data.get("domain"));
 				mDbHelper.close();
 			} 			
 		} else {
 			Toast.makeText(this, "Error at login. Try again or try different credentials", Toast.LENGTH_LONG).show();
 			return;
 		}
		setResult(RESULT_OK);
        finish();
	}

	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CSAndroidDbAdapter.KEY_ROWID, mRowId);
    }
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	private void fillForm() {
		if (mRowId != null) {
			mDbHelper.open();
			Cursor c = mDbHelper.fetchCloud(mRowId);
			startManagingCursor(c);
			txtName.setText(c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_NAME)));
			txtUrl.setText(c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_URL)));
			txtUser.setText(c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_USERNAME)));
			txtPass.setText(c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_PASS)));
			mDbHelper.close();
		}
	}
	
	private void exit() {
		mDbHelper = new CSAndroidDbAdapter(this);
		
		txtName = (EditText) findViewById(R.id.clName);
		txtUrl = (EditText) findViewById(R.id.clUrl);
		txtUser = (EditText) findViewById(R.id.clUsername);
		txtPass = (EditText) findViewById(R.id.clPass);
		txtDomain = (EditText) findViewById(R.id.clDomain);
		
		HashMap<String, String> data = new HashMap<String, String>(); 
 		data.put("user", txtUser.getText().toString());
 		data.put("pass", txtPass.getText().toString());
 		data.put("domain", txtDomain.getText().toString());
 		data.put("url", txtUrl.getText().toString());
 		
		if (mRowId == null) {
			mDbHelper.open();
			mDbHelper.createCloud(txtName.getText().toString(),
					data.get("url"), data.get("user"),data.get("pass"),data.get("domain"));
			mDbHelper.close();
		} else {
			mDbHelper.open();
			mDbHelper.updateCloud(mRowId, txtName.getText().toString(),
					data.get("url"), data.get("user"),data.get("pass"),data.get("domain"));
			mDbHelper.close();
		}
	}
}
