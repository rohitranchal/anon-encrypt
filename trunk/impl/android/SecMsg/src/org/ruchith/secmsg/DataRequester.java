package org.ruchith.secmsg;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;

import java.net.URI;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.ae.base.AEParameters;
import org.ruchith.ae.base.AEPrivateKey;
import org.ruchith.ae.base.ContactKeyGen;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class DataRequester {

	private static final String TAG = "DataRequester";

	private DBAdapter db;
	private Context ctx;

	public DataRequester(DBAdapter dbAdapter, Context ctx) {
		this.db = dbAdapter;
		this.ctx = ctx;
	}

	public boolean request(String contact) {
		try {

			// Get private key of contact
			Cursor privDataC = db.getPrivData(contact);
			String privDataStr = null;
			String idStr = null;
			if (privDataC.moveToFirst()) {
				privDataStr = privDataC.getString(privDataC
						.getColumnIndex(DBAdapter.KEY_PRIV_DATA));
				idStr = privDataC.getString(privDataC
						.getColumnIndex(DBAdapter.KEY_ID));
			} else {
				Log.i(TAG, this.ctx.getString(R.string.ex_no_such_contact));
				return false;
			}

			AEManager aeManager = AEManager.getInstance();
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode on = (ObjectNode) mapper.readTree(privDataStr);
			AEParameters params = aeManager.getParameters();
			Pairing pairing = params.getPairing();
			AEPrivateKey privKey = new AEPrivateKey(on, pairing);
			
			// Generate temp key
			Element id = pairing.getZr().newElement();
			id.setFromBytes(idStr.getBytes());
			ContactKeyGen keyGen = new ContactKeyGen();
			keyGen.init(id, privKey, params);
			
			Element randId = keyGen.genRandomID();
			AEPrivateKey randPrivKey = keyGen.getTmpPrivKey(randId);
			
			// Store temp key
			String publish = randId.toString();
			db.addRequestInfo(publish, randPrivKey.serializeJSON().toString());
			
			// Send request

			HttpClient client = new DefaultHttpClient();
			HttpGet req = new HttpGet();
			req.setURI(new URI(Constants.PUBCHANNEL_NEW_ENTRY_URL
					+ URLEncoder.encode(publish)));
			HttpResponse resp = client.execute(req);
			Log.i(TAG, "" + resp.getEntity().getContentLength());
			return true;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
	}

}