package com.ninjarific.wirelessmapper.database;

import java.sql.SQLException;
import java.util.List;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.ninjarific.wirelessmapper.database.orm.models.BaseModel;


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
	}

	/**
	 * This is called when your application is upgraded and it has a higher version number. This allows you to adjust
	 * the various data to match the new version number.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, ConnectionSource connectionSource, int oldVersion, int newVersion) {
		try {
			Log.i(DatabaseHelper.class.getName(), "onUpgrade");
			for (Class<?> modelClass : DatabaseCatalogue.modelClasses) {
				TableUtils.dropTable(connectionSource, modelClass, true);
			}
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
	
	public <ID, T extends BaseModel<ID>> List<BaseModel<ID>> query(Class<T> modelClass, PreparedQuery<BaseModel<ID>> query) {
		@SuppressWarnings("unchecked")
		RuntimeExceptionDao<BaseModel<ID>, ID> dao = (RuntimeExceptionDao<BaseModel<ID>, ID>) getRuntimeExceptionDaoForModelClass(modelClass);
		return dao.query(query);
	}
	
	/* DAOs */
	
	// A wrapper for getRuntimeExceptionDao() that makes sure we use the right ID class.
	// use this instead of getRuntimeExceptionDao()
	private <ID, T extends BaseModel<ID>> RuntimeExceptionDao<T, ID> getRuntimeExceptionDaoForModelClass(Class <T> c) {
		return getRuntimeExceptionDao(c);
	}
	
	// A wrapper for getDao() that makes sure we use the right ID class.
	// use this instead of getDao()
	public <ID, T extends BaseModel<ID>> Dao<T, ?> getDaoForModelClass(Class <T> c) {
		try {
			Dao<T, ?> dao = getDao(c);
			return dao;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private <ID, T extends BaseModel<ID>> RuntimeExceptionDao<T, ID> getRuntimeExceptionDaoForObject(BaseModel<ID> object) {
		return getRuntimeExceptionDaoForModelClass(object.getClass());
	}
}
