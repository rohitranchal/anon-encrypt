package org.ruchith.secmsg;

import it.unisa.dia.gas.jpbc.Element;

import java.net.URLEncoder;

import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.RootKeyGen;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class SecMsgActivity extends ListActivity {

	private static final String TAG = "SecMsgActivity";

	private static final int REQ_UPDATE_DIALOG = 1;
	
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

		String[] from = new String[] { DBAdapter.KEY_CONTACT_ID };

		int[] to = new int[] { R.id.text1 };

		SimpleCursorAdapter notes = new SimpleCursorAdapter(this,
				R.layout.contact_row, mContactsCursor, from, to);
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
					String pivDataVal = contactPriv.serializeJSON().toString();
					
//					String contactPrivData = Base64.encodeToString(pivDataVal.getBytes(), Base64.URL_SAFE);
					
					mDbHelper.addContact(val, id1.toString(), r.toString());
					fillData();
					
					
					Intent i = new Intent(Intent.ACTION_SEND);
					i.setType("text/plain");
					i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"rfernand@purdue.edu"});
					i.putExtra(Intent.EXTRA_SUBJECT, "Your Private Data");
					i.putExtra(Intent.EXTRA_TEXT   , Html.fromHtml("<a href=\"secmsg://data/" + URLEncoder.encode(pivDataVal) + "\">Click here to install private data</a><h1>test heading</h1>"));
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
    	case R.id.request_updates:
    		
    		try {
    			
    			showDialog(REQ_UPDATE_DIALOG);
    			
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
    		return true;
    	case R.id.params:
    		Intent i = new Intent(this, ConfigurationActivity.class);
    		
    		startActivity(i);
    		return true;
    	}
    	return super.onMenuItemSelected(featureId, item);
    }
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case REQ_UPDATE_DIALOG:
			//Select the contact
			LayoutInflater factory = LayoutInflater.from(this);
			final View dialogView = factory
					.inflate(R.layout.req_update_dialog, null);

			Cursor c = mDbHelper.fetchAllContacts();
			startManagingCursor(c);

			String[] from = new String[] { DBAdapter.KEY_CONTACT_ID };

			int[] to = new int[] { R.id.req_update_contact_text};

			SimpleCursorAdapter contacts = new SimpleCursorAdapter(this,
					R.layout.req_update_dialog_row, c, from, to);

			final Spinner contactList = (Spinner) dialogView
					.findViewById(R.id.req_update_contacts);
			contactList.setAdapter(contacts);
			
			return new AlertDialog.Builder(SecMsgActivity.this)
			.setTitle(R.string.req_update_dialog_title)
			.setView(dialogView)
			.setCancelable(false)
			.setPositiveButton(R.string.req_update_button_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							
							TextView tv = (TextView) contactList.getSelectedView();
							String selectedContactName = tv.getText().toString();
							
							//Create a new DataRequester 
							DataRequester dataRequester = 
									new DataRequester(mDbHelper, SecMsgActivity.this);
							dataRequester.request(selectedContactName); //Make request
						}
					})
			.setNegativeButton(R.string.alert_msg_button_cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							//Nothing to do
						}
					}).create();
		default:
			return null;
		}
	}
}