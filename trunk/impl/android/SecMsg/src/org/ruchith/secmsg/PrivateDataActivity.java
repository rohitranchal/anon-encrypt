package org.ruchith.secmsg;

import java.net.URLDecoder;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.widget.TextView;

public class PrivateDataActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		Uri data = getIntent().getData();

		String dataStr = data.toString();
		int i = dataStr.lastIndexOf('/');
		String content = dataStr.substring(i + 1);
		String decoded = URLDecoder.decode(content);
		tv.setText(decoded);
		
		setContentView(tv);
	}
}
