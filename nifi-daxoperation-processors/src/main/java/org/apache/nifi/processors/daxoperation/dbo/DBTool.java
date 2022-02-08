package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.nifi.processors.daxoperation.bo.ToolData;
import org.apache.nifi.processors.daxoperation.dao.ToolsDao;
import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.apache.nifi.processors.daxoperation.utils.ComputeTools;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

//import com.healthhiway.businessobject.ToolData;
//import com.healthhiway.dao.ToolsDao;
//import com.healthhiwasy.util.Comparators;
//import com.healthhiway.util.ComputeTools;

@Entity(value = "Tools", noClassnameStored = true)
public class DBTool {
	@Id private ObjectId id;
	@Property private String toolName; // This is referenced from ToolMaster
	@Property private String uhid; 
	@Property private Date dateCreated;
	@Property boolean isActive;
	@Embedded("toolData")
	List<DBToolData> toolsData = new ArrayList<DBToolData>();
	@Property private String observations; 

	public void setUhid(String uhid) {
		this.uhid = uhid;
	}

	public String getUhid() {
		return uhid;
	}

	public void setToolName(String toolName) {
		this.toolName = toolName;
	}

	public String getToolName() {
		return toolName;
	}

	public void setCreationDate(Date testDate) {
		this.dateCreated = testDate;
	}

	public Date getCreationDate() {
		return dateCreated;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public ObjectId getId() {
		return id;
	}

	public void setToolsData(List<DBToolData> toolsDataList) {
		this.toolsData = toolsDataList;
	}

	public List<DBToolData> getToolsData() {
		return this.toolsData;
	}

	public void addToolData(DBToolData toolData) {
		this.toolsData.add(toolData);
	}
	
	public void setObservations(String observations) {
		this.observations = observations;
	}

	public String getObservations() {
		return observations;
	}
	
	public void activate(){
		this.isActive = true;
	}
	
	public void deactive() {
		this.isActive = false;
	}
	
	public boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(boolean activeState) {
		isActive = activeState;
	}
	
	public void deleteData(ObjectId objId) {
		if(objId == null)
			throw new NullPointerException("deleteData called with null objectid");
		
		DBToolData tempDbToolData = new DBToolData();
		tempDbToolData.setId(objId);

		Collections.sort(toolsData, Comparators.toolsDataComparator());
		int itemIndex = Collections.binarySearch(toolsData, tempDbToolData);
		if(itemIndex >= 0) {
			toolsData.remove(itemIndex);	
		}
	}
	
	public ToolData saveData(String trackerName, ToolData toolData) {
		if(toolData == null)
			throw new NullPointerException("editData called with null ToolData");

		ToolData newToolData = ComputeTools.calculateToolsResult(trackerName,toolData);
		DBToolData dbToolData = ToolsDao.newDBToolData(newToolData);
		ObjectId toolDataId = null;
		if(toolData.getId() != null) {
			toolDataId = new ObjectId(toolData.getId().toString());
			DBToolData tempDbToolData = new DBToolData();
			tempDbToolData.setId(toolDataId);

			Collections.sort(toolsData, Comparators.toolsDataComparator());
			int itemIndex = Collections.binarySearch(toolsData, tempDbToolData);
			if(itemIndex >= 0) {
				toolsData.remove(itemIndex);
				dbToolData.setId(toolDataId);
			}
		}

		toolsData.add(dbToolData);
		newToolData.setId(dbToolData.getId());
		return newToolData;
	}
}