package org.apache.nifi.processors.daxoperation.dbo;


/**
 * Reference and embedding of other objects is still pending
 * Mani -- important to note on reference vs embedding has to be done
 */
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;


@Entity(value = "ProtonChecklist", noClassnameStored = true)
public class DBProtonChecklist {
	@Id private ObjectId id;
	

	@Property private String name; 
	@Property private boolean checked; 

	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public boolean isChecked() {
		return checked;
	}
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
}
