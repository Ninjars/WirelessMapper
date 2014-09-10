package com.ninjarific.wirelessmapper.database.orm.models;

import com.j256.ormlite.field.DatabaseField;

public abstract class BaseModel<ID> {
	// we use this field-name so we can query for users with a certain id
	public final static String ID_FIELD_NAME = "id";

	@DatabaseField(generatedId=true, columnName = ID_FIELD_NAME)
	private Long id;
	
	protected BaseModel() {/* for ORMLite use */}

	public Long getId() {
		return id;
	}
}
