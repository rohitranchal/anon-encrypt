package org.ruchith.secmsg;

import java.net.URLDecoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class PrivateDataActivity extends Activity {

	private String decoded;
	private DBAdapter db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		db = new DBAdapter(this);
		db.open();
		Uri data = getIntent().getData();

		String dataStr = data.toString();
		int i = dataStr.lastIndexOf('/');
		String content = dataStr.substring(i + 1);
		decoded = URLDecoder.decode(content);

		showDialog(1);

	}

	@Override
	protected Dialog onCreateDialog(int id) {

		LayoutInflater factory = LayoutInflater.from(this);
		final View dialogView = factory
				.inflate(R.layout.privdata_dialog, null);
		TextView tv = (TextView) dialogView.findViewById(R.id.privdata_val);
		tv.setText(decoded);

		Cursor c = db.fetchAllContacts();
		startManagingCursor(c);

		String[] from = new String[] { DBAdapter.KEY_CONTACT_ID };

		int[] to = new int[] { R.id.privdata_contact_text};

		SimpleCursorAdapter contacts = new SimpleCursorAdapter(this,
				R.layout.privdata_dialog_contact_row, c, from, to);

		final Spinner contactList = (Spinner) dialogView
				.findViewById(R.id.privdata_contacts);
		contactList.setAdapter(contacts);

		return new AlertDialog.Builder(PrivateDataActivity.this)
				.setTitle(R.string.priva_data_dialog_title)
				.setView(dialogView)
				.setCancelable(false)
				.setPositiveButton(R.string.priva_data_button_save,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								TextView tv = (TextView) contactList.getSelectedView();
								String selectedContactName = tv.getText().toString();
								db.addPrivdata(selectedContactName, decoded);
								finish();
							}
						})
				.setNegativeButton(R.string.alert_msg_button_cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								finish();
							}
						}).create();

	}
	
	@Override
	public void finish() {
		db.close();
		super.finish();
	}
}
