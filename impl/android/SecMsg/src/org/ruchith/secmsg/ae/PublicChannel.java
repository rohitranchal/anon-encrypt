package org.ruchith.secmsg.ae;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bouncycastle.util.encoders.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.ruchith.secmsg.AEManager;

import android.util.Log;

public class PublicChannel {

	private static final String TAG = "PublicChannel";


	private final static String PUBCHANNEL_URL = "http://ruchith.net";
	private final static String PUBCHANNEL_NEW_ENTRY_URL = PUBCHANNEL_URL + "/add/";
	private final static String PUBCHANNEL_PULL_URL = PUBCHANNEL_URL + "/pull/";
	
	private static PublicChannel publicChannel;
	
	private HttpClient client = new DefaultHttpClient();
	
	private PublicChannel() {}
	
	/**
	 * Create an instance of the channel.
	 * @return The singleton instance of the public channel.
	 */
	public static PublicChannel getInstance() {
		if(publicChannel == null) {
			publicChannel = new PublicChannel();
		}
		
		return publicChannel;
	}

	/**
	 * Publish the given message.
	 * Content will be base 64 encoded.
	 * @param message Content to be published
	 * @return Success or failure.
	 */
	public boolean publish(String message) {
		try {
			HttpGet req = new HttpGet();
			req.setURI(new URI(PUBCHANNEL_NEW_ENTRY_URL
					+ new String(Base64.encode(message.getBytes()))));
			HttpResponse resp = client.execute(req);
			return resp.getEntity().getContentLength() > 0;
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			return false;
		}
	}
	
	/**
	 * Return all new entries that are available.
	 * @return A JSON {@link ObjectNode} instance with the results.
	 */
	public ArrayNode pullAll() {
		try {
			int index = AEManager.getInstance().getPubChannelIndex();
			HttpGet req = new HttpGet();
			req.setURI(new URI(PUBCHANNEL_PULL_URL + index));
			HttpResponse resp = client.execute(req);
			InputStream content = resp.getEntity().getContent();
			
			ObjectMapper mapper = new ObjectMapper();
			return (ArrayNode) mapper.readTree(new InputStreamReader(content));
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			return null;
		}
	}
}
