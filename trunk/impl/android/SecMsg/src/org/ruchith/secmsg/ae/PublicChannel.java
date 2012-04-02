package org.ruchith.secmsg.ae;

import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.bouncycastle.util.encoders.Base64;
import org.ruchith.secmsg.Constants;

import android.util.Log;

public class PublicChannel {

	private static final String TAG = "PublicChannel";

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
			req.setURI(new URI(Constants.PUBCHANNEL_NEW_ENTRY_URL
					+ new String(Base64.encode(message.getBytes()))));
			HttpResponse resp = client.execute(req);
			return resp.getEntity().getContentLength() > 0;
		} catch (Exception e) {
			Log.i(TAG, e.getMessage());
			return false;
		} 
	
	}
}
