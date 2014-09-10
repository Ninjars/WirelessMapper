package com.ninjarific.wirelessmapper.database;

import java.sql.SQLException;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ninjarific.wirelessmapper.database.orm.models.BaseModel;
import com.ninjarific.wirelessmapper.database.orm.models.ObservableDao;
import com.ninjarific.wirelessmapper.database.orm.models.WifiData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScanPoint;


public class DatabaseHelper extends OrmLiteSqliteOpenHelper  {
	private static final String TAG = "DatabaseHelper";
	
	private static final String DATABASE_NAME = "WirelessDatabase.db";
	
	private static final int DATABASE_VERSION = 1;
	
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
			TableUtils.dropTable(connectionSource, WifiScanPoint.class, true);
			// after we drop the old databases, we create the new ones
			onCreate(db, connectionSource);
		} catch (SQLException e) {
			Log.e(DatabaseHelper.class.getName(), "Can't drop databases", e);
			throw new RuntimeException(e);
		}
	}
	
/* Fetch */
	
	public <ID, T extends BaseModel<ID>> T fetchObject(Class<T> objectClass, ID id) {
		RuntimeExceptionDao<T, ID> dao = getRuntimeExceptionDaoForModelClass(objectClass);
		return dao.queryForId(id);
	}
	
	/* Update */
	
	public <ID, T extends BaseModel<ID>> void insert(BaseModel<ID> object) {
		RuntimeExceptionDao<BaseModel<ID>, ID> dao = getRuntimeExceptionDaoForObject(object);
		dao.create(object);
		dao.refresh(object);
	}
	
	public <ID, T extends BaseModel<ID>> void update(BaseModel<ID> object) {
		RuntimeExceptionDao<BaseModel<ID>, ID> dao = getRuntimeExceptionDaoForObject(object);
		dao.update(object);
	}
	
	public <ID, T extends BaseModel<ID>> void delete(BaseModel<ID> object) {
		RuntimeExceptionDao<BaseModel<ID>, ID> dao = getRuntimeExceptionDaoForObject(object);
		dao.update(object);
	}
	
	/* Observing */
	
	public <ID, T extends BaseModel<ID>> void addChangeObserver(Class<T> modelClass, DatabaseListener<T,ID> listener) {
		ObservableDao<T, ID> dao = getDaoForModelClass(modelClass);
		if (dao != null) {
			dao.addDatabaseListener(listener);
		}
	}
	
	public <ID, T extends BaseModel<ID>> void removeChangeObserver(Class<T> modelClass, DatabaseListener<T,ID> listener){
		ObservableDao<T, ID> dao = getDaoForModelClass(modelClass);
		if (dao != null) {
			dao.removeDatabaseListener(listener);
		}
	}
	
	/* DAOs */
	
	// A wrapper for getRuntimeExceptionDao() that makes sure we use the right ID class.
	// use this instead of getRuntimeExceptionDao()
	private <ID, T extends BaseModel<ID>> RuntimeExceptionDao<T, ID> getRuntimeExceptionDaoForModelClass(Class <T> c) {
		return getRuntimeExceptionDao(c);
	}
	
	// A wrapper for getDao() that makes sure we use the right ID class.
	// use this instead of getDao()
	@SuppressWarnings("unchecked")
	private <ID, T extends BaseModel<ID>> ObservableDao<T, ID> getDaoForModelClass(Class <T> c) {
		try {
			return (ObservableDao<T, ID>) getDao(c);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private <ID, T extends BaseModel<ID>> RuntimeExceptionDao<T, ID> getRuntimeExceptionDaoForObject(BaseModel<ID> object) {
		return getRuntimeExceptionDaoForModelClass(object.getClass());
	}
}
