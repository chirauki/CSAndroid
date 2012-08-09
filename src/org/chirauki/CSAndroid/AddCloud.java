package org.chirauki.CSAndroid;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class AddCloud extends Activity {

	private CSAndroidDbAdapter mDbHelper;
	private EditText txtName;
	private EditText txtUrl;
	private EditText txtApikey;
	private EditText txtSecretkey;
	private EditText txtUser;
	private EditText txtPass;
	
	private Long mRowId;
	
	Button btnSaveCloud;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mDbHelper = new CSAndroidDbAdapter(this);
		//mDbHelper.open();
			
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
		txtUser = (EditText) findViewById(R.id.clApiKey);
		txtPass = (EditText) findViewById(R.id.clSecKey);
		fillForm();
	}

	public void saveCloud(View view) {
		mDbHelper = new CSAndroidDbAdapter(this);
		mDbHelper.open();
		txtName = (EditText) findViewById(R.id.clName);
		txtUrl = (EditText) findViewById(R.id.clUrl);
		txtUser = (EditText) findViewById(R.id.clApiKey);
		txtPass = (EditText) findViewById(R.id.clSecKey);
		
		HashMap<String, String> data = new HashMap<String, String>(); 
 		data.put("user", txtUser.getText().toString());
 		data.put("pass", txtPass.getText().toString());
		data.put("url", txtUrl.getText().toString());
		
		try {
			data = new doLogin().execute(data).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//CSAPIexecutor cs = new CSAPIexecutor(txtUrl.getText().toString());
		//cs.executeLogin(txtUser.getText().toString(), txtPass.getText().toString(), null);
		
		
		if (mRowId == null) {
			mDbHelper.createCloud(txtName.getText().toString(),
					data.get("url"),"blah",	"blah",	data.get("apik"),data.get("secret"));
		} else {
			mDbHelper.updateCloud(mRowId, txtName.getText().toString(), txtUrl.getText().toString(), txtApikey.getText().toString(), txtSecretkey.getText().toString());
		}
		
		mDbHelper.close();
		
		setResult(RESULT_OK);
        finish();
	}

	@Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(CSAndroidDbAdapter.KEY_ROWID, mRowId);
        //mDbHelper.close();
    }
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		//exit();
	}
	
	private void fillForm() {
		if (mRowId != null) {
			mDbHelper.open();
			Cursor c = mDbHelper.fetchCloud(mRowId);
			startManagingCursor(c);
			txtName.setText(c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_NAME)));
			txtUrl.setText(c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_URL)));
			txtApikey.setText(c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_APIK)));
			txtSecretkey.setText(c.getString(c.getColumnIndexOrThrow(CSAndroidDbAdapter.KEY_SECK)));
			mDbHelper.close();
		}
	}
	
	private void exit() {
		mDbHelper = new CSAndroidDbAdapter(this);
		mDbHelper.open();
		txtName = (EditText) findViewById(R.id.clName);
		txtUrl = (EditText) findViewById(R.id.clUrl);
		txtApikey = (EditText) findViewById(R.id.clApiKey);
		txtSecretkey = (EditText) findViewById(R.id.clSecKey);
		if (mRowId == null) {
			mDbHelper.createCloud(txtName.getText().toString(), "blah", "blah", txtUrl.getText().toString(), txtApikey.getText().toString(), txtSecretkey.getText().toString());
		} else {
			mDbHelper.updateCloud(mRowId, txtName.getText().toString(), txtUrl.getText().toString(), txtApikey.getText().toString(), txtSecretkey.getText().toString());
		}
		
		mDbHelper.close();
	}
	
	//private class doLogin extends AsyncTask<Params, Progress, Result> {
	private class doLogin extends AsyncTask<HashMap<String, String>, Void, HashMap<String, String>> {
		
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected HashMap<String, String> doInBackground(HashMap<String, String>... params) {
			HashMap<String, String> data = params[0];
			String apiUrl = data.get("url");
			String user = data.get("user");
			String pass = data.get("pass");
			
			CSAPIexecutor cs = new CSAPIexecutor(apiUrl);
			cs.executeLogin(user, pass, null);
		
			data.put("apik", cs.getApiKey());
			data.put("secret", cs.getApiSKey());
			
			return data;
		}
		
		@Override
		protected void onPostExecute(HashMap<String, String> result) {
			super.onPostExecute(result);
		}
	}
}
