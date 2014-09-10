package com.ninjarific.wirelessmapper.database;

import java.util.Collection;

import com.ninjarific.wirelessmapper.database.orm.models.ObjectProvider;

public interface DatabaseListener<T, ID>{
	public static enum TRANSACTION_TYPE {
		INSERT,
		UPDATE,
		DELETE
	}
	public void onChange(Collection<ID> ids, TRANSACTION_TYPE transactionType, ObjectProvider<T, ID> objectProvider);
	public boolean isInterestedInChange(ID id, TRANSACTION_TYPE transactionType, ObjectProvider<T, ID> objectProvider);
}
