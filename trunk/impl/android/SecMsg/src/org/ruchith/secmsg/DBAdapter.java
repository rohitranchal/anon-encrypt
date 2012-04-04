package org.ruchith.secmsg;

import org.ruchith.secmsg.ae.UpdateRequest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBAdapter {

	public static final String KEY_ROWID = "_id";
	public static final String KEY_CONTACT_ID = "contactId";
	public static final String KEY_ID = "id";
	public static final String KEY_RANDOM = "random";
	public static final String KEY_PRIV_DATA = "privDataFromContact";
	public static final String KEY_MY_CONTACT_ID = "myIdFromContact";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_MESSAGE_COUNT = "messageCount";
	public static final String KEY_PARAMS = "params";
	public static final String KEY_PUB_CHANNEL_INDEX = "pubChannelIndex";
	public static final String KEY_MASTER_KEY = "masterKey";

	public static final String KEY_REQUEST_ID = "reqId";
	public static final String KEY_SALT = "salt";
	public static final String KEY_TMP_KEY = "tmpKey";

	private static final String[] DB_CREATE = new String[] {
			"CREATE TABLE Contact ("
					+ "_id integer primary key autoincrement, "
					+ "contactId text not null, " 
					+ "id text not null, "
					+ "random text not null, " 
					+ "privDataFromContact text, "
					+ "myIdFromContact text)",
			"CREATE TABLE Message ("
					+ "_id integer primary key autoincrement, "
					+ "contactId text not null, "
					+ "messageCount integer not null, " 
					+ "message text not null)",
			"CREATE TABLE Config (" 
					+ "params text not null, "
					+ "pubChannelIndex integer default 0, "
					+ "masterKey text not null)",
			"CREATE TABLE RequestInfo ("
					+ "_id integer primary key autoincrement, "
					+ "contactId text not null, "
					+ "salt text not null, "
					+ "reqId text not null, " 
					+ "tmpKey text not null)" };

	private static final String DATABASE_NAME = "data";
	private static final String DATABASE_TABLE_CONTACT = "Contact";
	private static final String DATABASE_TABLE_MESSAGE = "Message";
	private static final String DATABASE_TABLE_CONFIG = "Config";
	private static final String DATABASE_TABLE_REQ_INFO = "RequestInfo";

	private static final int DATABASE_VERSION = 2;

	private static final String TAG = "NotesDbAdapter";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;

	private final Context mCtx;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			for (int i = 0; i < DB_CREATE.length; i++) {
				db.execSQL(DB_CREATE[i]);
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS notes");
			onCreate(db);
		}

	}

	public DBAdapter(Context ctx) {
		this.mCtx = ctx;
	}

	public DBAdapter open() {
		this.mDbHelper = new DatabaseHelper(this.mCtx);
		this.mDb = this.mDbHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		this.mDbHelper.close();
	}

	/**
	 * Return all contacts.
	 * 
	 * @return A {@link Cursor} with all {@link #KEY_ROWID} and
	 *         {@link #KEY_CONTACT_ID} values.
	 */
	public Cursor fetchAllContacts() {
		return mDb.query(DATABASE_TABLE_CONTACT, new String[] { KEY_ROWID,
				KEY_CONTACT_ID }, null, null, null, null, null);
	}

	/**
	 * Return the stored configuration.
	 * 
	 * @return A {@link Cursor} with the parameters and the master key.
	 */
	public Cursor fetchConfig() {
		return mDb.query(DATABASE_TABLE_CONFIG, new String[] { KEY_PARAMS,
				KEY_PUB_CHANNEL_INDEX, KEY_MASTER_KEY }, 
				null, null, null, null, null);
	}

	/**
	 * Store information about a new contact.
	 * 
	 * @param name
	 *            Name given to the contact.
	 * @param id
	 *            Generated id of the contact.
	 * @param random
	 *            Random value associated with the contact.
	 * @return
	 */
	public long addContact(String name, String id, String random) {
		ContentValues values = new ContentValues();
		values.put(KEY_CONTACT_ID, name);
		values.put(KEY_ID, id);
		values.put(KEY_RANDOM, random);

		return mDb.insert(DATABASE_TABLE_CONTACT, null, values);
	}

	/**
	 * Store my private data from the remote contact.
	 * 
	 * @param name
	 *            Name of the contact.
	 * @param privData
	 *            Serilized private data.
	 * @return
	 */
	public boolean addPrivdata(String name, String privData) {
		ContentValues values = new ContentValues();
		values.put(KEY_PRIV_DATA, privData);

		return mDb.update(DATABASE_TABLE_CONTACT, values, KEY_CONTACT_ID + "='"
				+ name + "'", null) > 0;
	}

	/**
	 * Obtain my private key of the contact.
	 * @param name {@link #KEY_CONTACT_ID} of the contact.
	 * @return {@link Cursor} with private data.
	 */
	public Cursor getPrivData(String name) {
		return mDb.query(DATABASE_TABLE_CONTACT,
				new String[] { KEY_ID, KEY_PRIV_DATA }, KEY_CONTACT_ID + "='" + name
						+ "'", null, null, null, null);
	}

	/**
	 * Store configuration.
	 * 
	 * @param params
	 *            Global parameters.
	 * @param mk
	 *            The master key.
	 */
	public void addConfig(String params, String mk) {
		ContentValues values = new ContentValues();
		values.put(KEY_PARAMS, params);
		values.put(KEY_MASTER_KEY, mk);

		mDb.insert(DATABASE_TABLE_CONFIG, null, values);
	}

	/**
	 * Store the temporary key used for a request.
	 * 
	 * @param reqId
	 *            Request id - public key value used
	 * @param contact
	 *            Name of the contact
	 * @param salt
	 *            Salt value used to hash contact name in {@link UpdateRequest}
	 * @param keyInfo
	 *            Serialized public key
	 * @return
	 */
	public long addRequestInfo(String reqId, String contact, String salt, String keyInfo) {
		ContentValues values = new ContentValues();
		values.put(KEY_REQUEST_ID, reqId);
		values.put(KEY_CONTACT_ID, contact);
		values.put(KEY_SALT, salt);
		values.put(KEY_TMP_KEY, keyInfo);

		return mDb.insert(DATABASE_TABLE_REQ_INFO, null, values);
	}
	
	public boolean updatePublicChannelIndex(int index) {

		ContentValues values = new ContentValues();
		values.put(KEY_PUB_CHANNEL_INDEX, new Integer(index));

		return mDb.update(DATABASE_TABLE_CONFIG, values, null, null) > 0;		
	}

	/**
	 * Get the last message 
	 * @param name
	 * @return
	 */
	public Cursor getMessage(String name) {
		return mDb.query(DATABASE_TABLE_MESSAGE,
				new String[] { KEY_MESSAGE }, KEY_CONTACT_ID + "='" + name
						+ "'", null, null, null, null);
	}
	
	public Cursor getMessageById(long id) {
		String sql = "SELECT " + KEY_CONTACT_ID + " FROM "
				+ DATABASE_TABLE_CONTACT + " WHERE " + KEY_ROWID + " = " + id;
		Cursor c = mDb.rawQuery(sql, null);
		c.moveToFirst();
		String name = c.getString(c.getColumnIndex(KEY_CONTACT_ID));
		c.close();
		
		return mDb.query(DATABASE_TABLE_MESSAGE,
				new String[] { KEY_MESSAGE }, KEY_CONTACT_ID + "='" + name
						+ "'", null, null, null, null);
	}
	
	/**
	 * Adding a message
	 * @param name
	 * @param message
	 * @return
	 */
	public long setMessage(String name, String message) {
		ContentValues values = new ContentValues();
		values.put(KEY_MESSAGE, message );
		values.put(KEY_CONTACT_ID, name);
		values.put(KEY_MESSAGE_COUNT, 1);
		
		return mDb.insert(DATABASE_TABLE_MESSAGE, null, values);
	}
}
