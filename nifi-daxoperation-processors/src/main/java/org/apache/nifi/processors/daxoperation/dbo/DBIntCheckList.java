package org.apache.nifi.processors.daxoperation.dbo;


import java.util.ArrayList;
import java.util.List;

/**
 * Reference and embedding of other objects is still pending
 * Mani -- important to note on reference vs embedding has to be done
 */
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

//import com.healthhiway.businessobject.ProtonChecklist;

@Entity(value = "InternationalCheckList", noClassnameStored = true)
public class DBIntCheckList {
	@Id private ObjectId id;
	
	@Embedded("IntcheckList")
	List<DBProtonChecklist> protonChecklist = new ArrayList<DBProtonChecklist>();

	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public List<DBProtonChecklist> getProtonChecklist() {
		return protonChecklist;
	}
	public void setProtonChecklist(List<DBProtonChecklist> protonChecklist) {
		this.protonChecklist = protonChecklist;
	}
	
}
