package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.apache.nifi.processors.daxoperation.utils.NVPair;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;



@Entity(value = "ResMetaMaster", noClassnameStored = true)
public class DBResourceMaster {
	@Id private ObjectId id;
	//@Property private DBResources dbResourceType;
	@Property private String dbResourceType;
	@Property private String dbResourceName; 
	@Embedded("resDetail") 
	List<NVPair> resDetail = new ArrayList<NVPair>();
	
	//@Embedded ("resourceMasterDetail") 
	//List<ObjPair<String,List<ObjPair<String,String>>>> resourceMasterDetail = new ArrayList<ObjPair<String,List<ObjPair<String,String>>>>();
	//private Map<String, Map<String,String>> resourceMasterDetail = new HashMap<String, Map<String, String>>();
	@Property private String notes;
	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}
	public void setDbResourceType(String dbResourceType) {
		this.dbResourceType = dbResourceType;
	}
	public String getDbResourceType() {
		return dbResourceType;
	}
	public void setDbResourceName(String dbResourceName) {
		this.dbResourceName = dbResourceName;
	}
	public String getDbResourceName() {
		return dbResourceName;
	}
	public void setResourceMasterDetail(List<NVPair> resDet) {
		this.resDetail = resDet;
	}
	public List<NVPair>  getResourceMasterDetail() {
		return this.resDetail;
	}
	public void addResourceDetail(String resourceName) {
		//this.resourceMasterDetail.add(resourceName);
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getNotes() {
		return notes;
	}
}
