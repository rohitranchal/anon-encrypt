package org.ruchith.secmsg;

import org.ruchith.ae.base.AEParameters;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class ConfigurationActivity extends ListActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.config);
		populate();
	}

	private void populate() {
		AEManager manager = AEManager.getInstance();
		AEParameters params = manager.getParameters();
		
		String[] values = new String[8];
		values[0] = "Curve : \n" + params.getCurveParams().toString();
		values[1] = "G : " + params.getG().toString();
		values[2] = "G1 : " + params.getG1().toString();
		values[3] = "G2 : " + params.getG2().toString();
		values[4] = "G3 : " + params.getG3().toString();
		values[5] = "H1 : " + params.getH1().toString();
		values[6] = "H2 : " + params.getH2().toString();
		values[7] = "H3 : " + params.getH3().toString();
		
		setListAdapter(new ArrayAdapter<String>(this,R.layout.config_row, values));
		
		
	}

}
