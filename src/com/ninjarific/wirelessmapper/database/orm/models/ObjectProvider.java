package com.ninjarific.wirelessmapper.database.orm.models;

public interface ObjectProvider<T, ID>{
	/**
	 * Given an ID, will return the associated Object.
	 * @param id
	 * @return Object - may be null for deleted Objects.
	 */
	public T getObject(ID id);
}
