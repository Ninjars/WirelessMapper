package com.ninjarific.wirelessmapper.database;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ninjarific.wirelessmapper.wifidata.WifiData;
import com.ninjarific.wirelessmapper.wifidata.WifiScanResult;


public class DatabaseHelper extends OrmLiteSqliteOpenHelper  {
	private static final String TAG = "DatabaseHelper";
	
	private static final String DATABASE_NAME = "WirelessDatabase.db";
	
	private static final int DATABASE_VERSION = 1;
	
//	private Dao<WifiData, Integer> mWifiData = null;
//	private Dao<WifiScanResult, Integer> mWifiScanResult = null;
	private RuntimeExceptionDao<WifiData, Integer> mWifiDataRuntimeExceptionDao = null;
	private RuntimeExceptionDao<WifiScanResult, Integer> mWifiScanResultRuntimeExceptionDao = null;
	
	public DatabaseHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	// Method is called during creation of the database
	@Override
	public void onCreate(SQLiteDatabase db, ConnectionSource connectionSource) {
		// Create every table.
		for (Class<?> modelClass : DatabaseCatalogue.modelClasses) {
			try {
				TableUtils.createTable(connectionSource, modelClass);
			} catch (SQLException e) {
				Log.i(TAG, "Exception while creating table", e);
			}
		}

//		// here we try inserting data in the on-create as a test
//		RuntimeExceptionDao<WifiData, Integer> dao = getWifiDataDao();
//		long millis = System.currentTimeMillis();
//		// create some entries in the onCreate
//		WifiData simple = new WifiData(millis);
//		dao.create(simple);
//		simple = new WifiData(millis + 1);
//		dao.create(simple);
//		Log.i(DatabaseHelper.class.getName(), "created new entries in onCreate: " + millis);
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			TableUtils.dropTable(connectionSource, WifiData.class, true);
			TableUtils.dropTable(connectionSource, WifiScanResult.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}
	
//	public Dao<WifiData, Integer> getDao() throws SQLException {
//		if (mWifiData == null) {
//			mWifiData = getDao(WifiData.class);
//		}
//		return mWifiData;
//	}
//	
//	public Dao<WifiScanResult, Integer> getDao() throws SQLException {
//		if (mWifiScanResult == null) {
//			mWifiScanResult = getDao(WifiScanResult.class);
//		}
//		return mWifiScanResult;
//	}
	
	/**
	 * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our WifiData class. It will
	 * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
	 */
	public RuntimeExceptionDao<WifiData, Integer> getWifiDataDao() {
		if (mWifiDataRuntimeExceptionDao == null) {
			mWifiDataRuntimeExceptionDao = getRuntimeExceptionDao(WifiData.class);
		}
		return mWifiDataRuntimeExceptionDao;
	}
	
	/**
	 * Returns the RuntimeExceptionDao (Database Access Object) version of a Dao for our WifiScanResult class. It will
	 * create it or just give the cached value. RuntimeExceptionDao only through RuntimeExceptions.
	 */
	public RuntimeExceptionDao<WifiScanResult, Integer> getWifiScanResultDao() {
		if (mWifiScanResultRuntimeExceptionDao == null) {
			mWifiScanResultRuntimeExceptionDao = getRuntimeExceptionDao(WifiScanResult.class);
		}
		return mWifiScanResultRuntimeExceptionDao;
	}
	
	private static String getDatabasePath(Context context) {
		// return context.getFilesDir() + "/databases/" + DATABASE_NAME;
		return context.getDatabasePath(DATABASE_NAME).getPath();
	}
}
