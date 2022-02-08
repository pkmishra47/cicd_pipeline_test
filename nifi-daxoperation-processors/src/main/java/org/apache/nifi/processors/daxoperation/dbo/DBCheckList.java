package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "CheckList", noClassnameStored = true)
public class DBCheckList {

	@Id private ObjectId id;
	@Property private String checkListName;
	@Property private List<String> checkListItems = new ArrayList<String>();
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getCheckListName() {
		return checkListName;
	}
	public void setCheckListName(String checkListName) {
		this.checkListName = checkListName;
	}
	public List<String> getCheckListItems() {
		return checkListItems;
	}
	public void setCheckListItems(List<String> checkListItems) {
		this.checkListItems = checkListItems;
	}
}


