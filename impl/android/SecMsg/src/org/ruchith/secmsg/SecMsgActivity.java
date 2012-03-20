package org.ruchith.secmsg;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;

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
        this.aeManager = new AEManager(this, this.mDbHelper);
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
    		return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
}