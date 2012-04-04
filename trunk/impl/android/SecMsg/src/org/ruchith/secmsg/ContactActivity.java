package org.ruchith.secmsg;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.TextView;

public class ContactActivity extends Activity {

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
		
		DBAdapter db = new DBAdapter(this);
		db.open();
		
		Cursor c = db.getMessageById(id.longValue());
		
		TextView tv = new TextView(this);
		tv.setTextSize(40);
		c.moveToFirst();
		if(!c.isAfterLast()) {
			String msg = c.getString(c.getColumnIndex(DBAdapter.KEY_MESSAGE));
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
}
