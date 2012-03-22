package org.ruchith.secmsg;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.plaf.jpbc.pairing.CurveParams;
import it.unisa.dia.gas.plaf.jpbc.pairing.a1.TypeA1CurveGenerator;

import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameterGenerator;
import org.ruchith.ae.base.AEParameters;

import android.database.Cursor;
import android.util.Log;

public class AEManager {

	private static final String TAG = "AEManager";
	
	private DBAdapter mDbHelper;
	private AEParameters params;
	private Element masterKey;
	
	private static AEManager instance;

	private AEManager(DBAdapter db) {
		this.mDbHelper = db;
		//Load configuration
		this.loadConfiguration();
	}
	
	public static AEManager getInstance(DBAdapter db) {
		if(instance == null) {
			instance = new AEManager(db);
		}
		return instance;
	}
	
	public static AEManager getInstance() {
		return instance;
	}

	private void loadConfiguration() {
		//Check whether parameters are available 
		Cursor c = this.mDbHelper.fetchConfig();
		if(c.getCount() > 0) {
			c.moveToFirst();
			String paramVal = c.getString(c.getColumnIndexOrThrow(DBAdapter.KEY_PARAMS));
			String mkVal = c.getString(c.getColumnIndexOrThrow(DBAdapter.KEY_MASTER_KEY));
			
			try {
				ObjectMapper mapper = new ObjectMapper();
				ObjectNode on = (ObjectNode) mapper.readTree(paramVal);
				this.params = new AEParameters(on);
				
				Element tmp = this.params.getPairing().getG1().newElement();
				
				tmp.setFromBytes(Base64.decode(mkVal));
				this.masterKey = tmp.getImmutable();
				
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			
		} else {
			//install
			install();
		}
	}
	
	private void install() {
		CurveParams curveParams = (CurveParams) new TypeA1CurveGenerator(4, 32)
				.generate();

		AEParameterGenerator paramGen = new AEParameterGenerator();
		paramGen.init(curveParams);

		this.params = paramGen.generateParameters();
		this.masterKey = paramGen.getMasterKey();
		
		//Add to DB
		String paramVal = this.params.serializeJSON().toString();
		String mkVal = new String(Base64.encode(this.masterKey.toBytes()));
		
		this.mDbHelper.addConfig(paramVal, mkVal);
		
	}
	
	public AEParameters getParameters() {
		return this.params;
	}

}
