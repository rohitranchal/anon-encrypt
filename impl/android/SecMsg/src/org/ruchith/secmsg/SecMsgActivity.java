package org.ruchith.secmsg;

import it.unisa.dia.gas.jpbc.Element;

import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.RootKeyGen;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class SecMsgActivity extends ListActivity {
	
	private static final String TAG = "SecMsgActivity";
	
	private DBAdapter mDbHelper;
    private Cursor mContactsCursor;
    private AEManager aeManager;
    

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mDbHelper = new DBAdapter(this);
        this.mDbHelper.open();
        this.aeManager = AEManager.getInstance(this.mDbHelper);
        setContentView(R.layout.main);
        this.fillData();
    }
    
    
    private void fillData() {
        mContactsCursor = mDbHelper.fetchAllContacts();
        startManagingCursor(mContactsCursor);

        String[] from = new String[]{DBAdapter.KEY_CONTACT_ID};

        int[] to = new int[]{R.id.text1};

        SimpleCursorAdapter notes = 
            new SimpleCursorAdapter(this, R.layout.contact_row, mContactsCursor, from, to);
        setListAdapter(notes);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
    	return true;
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.create_contact:
    		AlertDialog.Builder alert = new AlertDialog.Builder(this);
    		alert.setTitle(R.string.alert_title_create_contact);
    		alert.setMessage(R.string.alert_msg_create_contact);
    		
    		final EditText input = new EditText(this);
    		alert.setView(input);
    		
    		alert.setPositiveButton(R.string.alert_msg_button_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					String val = input.getText().toString();
					
					
					RootKeyGen rkg = new RootKeyGen();
					AEParameters params = aeManager.getParameters();
					rkg.init(params);
					
					Element id1 = params.getPairing().getZr().newRandomElement();
					Element r = params.getPairing().getZr().newRandomElement();
					AEPrivateKey contactPriv = rkg.genKey(id1, aeManager.getMasterKey(), r);
					String contactPrivData = contactPriv.serializeJSON().toString();
					
					mDbHelper.addContact(val, id1.toString(), r.toString());
					fillData();
					
					
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"recipient@example.com"});
					i.putExtra(Intent.EXTRA_SUBJECT, "your data");
					i.putExtra(Intent.EXTRA_TEXT   , contactPrivData);
					try {
					    startActivity(Intent.createChooser(i, "Send mail..."));
					} catch (android.content.ActivityNotFoundException ex) {
					    Toast.makeText(SecMsgActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
					}
					Log.i(TAG, val);
				}
			});

    		alert.setNegativeButton(R.string.alert_msg_button_cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			});
    		
    		alert.show();
    		
    		return true;
    	case R.id.remote_data:
    		
    		return true;
    	case R.id.params:
    		Intent i = new Intent(this, ConfigurationActivity.class);
    		
    		startActivity(i);
    		return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
}