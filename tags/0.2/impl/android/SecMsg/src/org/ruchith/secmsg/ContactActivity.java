package org.ruchith.secmsg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

public class ContactActivity extends Activity {

	private static final int DIRECT_MSG_DIALOG = 1;
	private DBAdapter db;
	private String contact;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		
		Long id = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DBAdapter.KEY_ROWID);
		if (id == null) {
			Bundle extras = getIntent().getExtras();
			id = extras != null ? extras.getLong(DBAdapter.KEY_ROWID)
									: null;
		}
		
		db = new DBAdapter(this);
		db.open();
		
		this.contact = db.getContactById(id);
		this.setTitle(this.contact);
		
		populate();
	}

	private void populate() {
		String msg = db.getMessage(this.contact);
		
		TextView tv = new TextView(this);
		tv.setTextSize(40);
		if(msg != null) {
			tv.setText(msg);
		} else {
			tv.setText(R.string.contact_no_message);
		}
		setContentView(tv);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contact, menu);
		return true;
	}
	
	@Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.add_direct_message:
    		showDialog(DIRECT_MSG_DIALOG);
    		return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIRECT_MSG_DIALOG:

			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Enter Direct Message");
			alert.setMessage("Message");

			// Set an EditText view to get user input
			final EditText input = new EditText(this);
			alert.setView(input);

			alert.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							String value = input.getText().toString();
							db.setMessage(contact, value);
							populate();
						}
					});

			alert.setNegativeButton("Cancel",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// Nothing to do
						}
					});

			return alert.create();
		}
		return null;
	}
	
	@Override
	public void finish() {
		db.close();
		super.finish();
	}
}
