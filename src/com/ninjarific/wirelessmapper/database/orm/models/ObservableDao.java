package com.ninjarific.wirelessmapper.database.orm.models;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import android.util.Log;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedUpdate;
import com.j256.ormlite.stmt.UpdateBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.ninjarific.wirelessmapper.database.DatabaseListener;
import com.ninjarific.wirelessmapper.database.DatabaseListener.TRANSACTION_TYPE;

public class ObservableDao<T, ID> extends BaseDaoImpl<T, ID> implements ObjectProvider<T, ID>{
	public static final String TAG = "ObservableDao";
	public static final boolean DLOG = false;
	
	private class ChangeSet extends HashMap<TRANSACTION_TYPE, HashSet<ID>> {
		
		private static final long serialVersionUID = -7085735431572627370L;
		
		private HashSet<ID> getOrCreate(TRANSACTION_TYPE type) {
			HashSet<ID> changedObjectIds = get(type);
			if(changedObjectIds == null){
				changedObjectIds = new HashSet<ID>();
				put(type, changedObjectIds);
			}
			return changedObjectIds;
		}
		
	}
	
	private class DatabaseListenerChangeSets extends HashMap<DatabaseListener<T, ID>, ChangeSet> {
		
		private static final long serialVersionUID = -4115125341100279452L;

		private ChangeSet getOrCreate(DatabaseListener<T, ID> listener) {
			ChangeSet changeSet = get(listener);
			if(changeSet == null){
				changeSet = new ChangeSet();
				put(listener, changeSet);
			}
			return changeSet;
		}
		
	}
	
	HashSet<DatabaseListener<T, ID>> mDatabaseListeners = new HashSet<DatabaseListener<T, ID>>();
	
	DatabaseListenerChangeSets mChanges = new DatabaseListenerChangeSets();
	
	public ObservableDao(Class<T> dataClass) throws SQLException {
		super(dataClass);
	}

	public ObservableDao(ConnectionSource connectionSource, Class<T> dataClass)
			throws SQLException {
		super(connectionSource, dataClass);
	}

	public ObservableDao(ConnectionSource connectionSource,
			DatabaseTableConfig<T> tableConfig) throws SQLException {
		super(connectionSource, tableConfig);
		// TODO Auto-generated constructor stub
	}
	
	//------------------------------
	public void addDatabaseListener(DatabaseListener<T, ID> listener){
		mDatabaseListeners.add(listener);
	}
	
	public void removeDatabaseListener(DatabaseListener<T, ID> listener){
		mDatabaseListeners.remove(listener);
	}
	
	private void notifyObservers(){
		for(DatabaseListener<T, ID> listener : mDatabaseListeners){
			ChangeSet registeredObserverChanges = mChanges.get(listener);
			for(TRANSACTION_TYPE type : TRANSACTION_TYPE.values()){
				HashSet<ID> changedObjectIds = registeredObserverChanges.get(type);
				listener.onChange(changedObjectIds, type, this);
			}
		}
		mChanges.clear();
	}
	
	private void registerChangeOfInterest(DatabaseListener<T, ID> listener, ID id, TRANSACTION_TYPE transactionType){
		if(DLOG)Log.d(TAG, "registerChangeOfInterest() " + transactionType.toString());
		ChangeSet registeredObserverChanges = mChanges.getOrCreate(listener);		
		HashSet<ID> changedObjectIds = registeredObserverChanges.getOrCreate(transactionType);
		changedObjectIds.add(id);
	}
	
	private void registerChange(Collection<ID> ids, TRANSACTION_TYPE transactionType){
		if(DLOG)Log.d(TAG, "registerChange() - Collection - " + transactionType.toString());
		for(ID id : ids){
			registerChange(id, transactionType);
		}
	}
	
	private void registerChange(ID id, TRANSACTION_TYPE transactionType){
		if(DLOG)Log.d(TAG, "registerChange() - ID - " + transactionType.toString());
		for(DatabaseListener<T, ID> listener : mDatabaseListeners){
			boolean isInterested = listener.isInterestedInChange(id, transactionType, this);
			if(isInterested){
				registerChangeOfInterest(listener, id, transactionType);
			}
		}
	}
	
	private void onChange(Collection<ID> ids, TRANSACTION_TYPE transactionType){
		registerChange(ids, transactionType);
		notifyObservers();
	}
	
	private void onChange(ID id, TRANSACTION_TYPE transactionType){
		HashSet<ID> ids = new HashSet<ID>();
		ids.add(id);
		onChange(ids, transactionType);
	}

	//------------------------------
	
	@Override
	public void commit(DatabaseConnection connection) throws SQLException {
		Log.d(getClass().getName(), "commit()");
		//TODO Well, this is not being called with auto commit false... oh well! We don't support observer notification for this.
		super.commit(connection);
	}

	@Override
	public int create(T arg0) throws SQLException {
		int i = super.create(arg0);
		ID id = extractId(arg0);
		onChange(id, TRANSACTION_TYPE.INSERT);
		return i;
	}
	
	//-------- DELETES ----------------------
	@Override
	public int delete(Collection<T> objects) throws SQLException {
		HashSet<ID> ids = new HashSet<ID>();
		for(T object : objects){
			ids.add(extractId(object));
		}
		registerChange(ids, TRANSACTION_TYPE.DELETE);
		int i = super.delete(objects);
		notifyObservers();
		return i;
	}

	@Override
	public int delete(PreparedDelete<T> preparedDelete) throws SQLException {
		//TODO 
		return super.delete(preparedDelete);
	}

	@Override
	public int delete(T object) throws SQLException {
		ID id = extractId(object);
		registerChange(id, TRANSACTION_TYPE.DELETE);
		int i = super.delete(object);
		notifyObservers();
		return i;
	}
	
	//--------OTHER DELETES ----------------------
	@Override
	public DeleteBuilder<T, ID> deleteBuilder() {
		//TODO
		return super.deleteBuilder();
	}

	
	@Override
	public int deleteById(ID id) throws SQLException {
		registerChange(id, TRANSACTION_TYPE.DELETE);
		int i = super.deleteById(id);
		notifyObservers();
		return i;
	}

	@Override
	public int deleteIds(Collection<ID> ids) throws SQLException {
		registerChange(ids, TRANSACTION_TYPE.DELETE);
		int i = super.deleteIds(ids);
		notifyObservers();
		return i;
	}
	
	@Override
	public void rollBack(DatabaseConnection connection) throws SQLException {
		//TODO
		super.rollBack(connection);
	}

	//--------UPDATE ----------------------
	@Override
	public int update(PreparedUpdate<T> preparedUpdate) throws SQLException {
		//TODO
		return super.update(preparedUpdate);
	}

	
	@Override
	public int update(T object) throws SQLException {
		int i = super.update(object);
		ID id = extractId(object);
		onChange(id, TRANSACTION_TYPE.UPDATE);
		return i;
	}

	
	@Override
	public UpdateBuilder<T, ID> updateBuilder() {
		//TODO
		return super.updateBuilder();
	}

	
	@Override
	public int updateId(T object, ID id) throws SQLException {
		int i = super.updateId(object, id);
		onChange(id, TRANSACTION_TYPE.UPDATE);
		return i;
	}
	
	
	@Override
	public int updateRaw(String arg0, String... arg1) throws SQLException {
		Log.wtf(getClass().getName(), "updateRaw()");
		//TODO make sure only we call this.
		return super.updateRaw(arg0, arg1);
	}

	@Override
	public int executeRaw(String arg0, String... arg1) throws SQLException {
		Log.wtf(getClass().getName(), "executeRaw()");
		//TODO
		return super.executeRaw(arg0, arg1);
	}

	@Override
	public int executeRawNoArgs(String arg0) throws SQLException {
		Log.wtf(getClass().getName(), "executeRawNoArgs()");
		//TODO
		return super.executeRawNoArgs(arg0);
	}
	
	//--------ObjectProvider interface ----------------------
	@Override
	public T getObject(ID id) {
		try {
			return queryForId(id);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
