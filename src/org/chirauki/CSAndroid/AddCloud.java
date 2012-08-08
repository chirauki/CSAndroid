package org.chirauki.CSAndroid;

import android.app.Activity;
import android.database.Cursor;
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
		
		CSAPIexecutor cs = new CSAPIexecutor(txtUrl.getText().toString());
		cs.executeLogin(txtUser.getText().toString(), txtPass.getText().toString(), null);
		
		
		if (mRowId == null) {
			mDbHelper.createCloud(txtName.getText().toString(), 
					txtUrl.getText().toString(), "blah", "blah", txtApikey.getText().toString(), txtSecretkey.getText().toString());
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
}
