package com.ninjarific.wirelessmapper.database.orm.models;

import com.j256.ormlite.field.DatabaseField;

// TODO: Fix the generic type
// BaseModel was meant to have a generic ID type
// But the ORMLite has trouble creating the id field in that situation
// For now, we changed id to be of type Long instead of ID, as that's how we're using it
// To fix this, we need to change the type back to ID, but provide a custom persister for it
public abstract class BaseModel<ID> {
	@DatabaseField(generatedId=true)
	private Long id;
	
	protected BaseModel() {/* for ORMLite use */}

	public Long getId() {
		return id;
	}
}
