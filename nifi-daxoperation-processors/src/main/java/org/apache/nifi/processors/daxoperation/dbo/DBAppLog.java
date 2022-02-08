
package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "AppLog", noClassnameStored = true)
public class DBAppLog implements Comparable<DBAppLog>{
    @Id private ObjectId id;
    @Property private Date timeStamp;
    @Property private String systemName;
    @Property private int systemID;
    @Property private String eventCategorName;
    @Property private int eventCategoryID;
    @Property private String eventName;
    @Property private int eventID;
    @Property private String logData;
    @Property private Map<String,String> logDataMap = new HashMap<String,String>(); 
    
	public String getEventCategorName() {
        return eventCategorName;
    }

    public void setEventCategorName(String eventCategorName) {
        this.eventCategorName = eventCategorName;
    }

    public int getEventCategoryID() {
        return eventCategoryID;
    }

    public void setEventCategoryID(int eventCategoryID) {
        this.eventCategoryID = eventCategoryID;
    }

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getLogData() {
        return logData;
    }

    public void setLogData(String logData) {
        this.logData = logData;
    }

    public int getSystemID() {
        return systemID;
    }

    public void setSystemID(int systemID) {
        this.systemID = systemID;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

         
    @Override
    public int compareTo(DBAppLog o) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	public void setLogDataMap(Map<String,String> logDataMap) {
		this.logDataMap = logDataMap;
	}

	public Map<String,String> getLogDataMap() {
		return logDataMap;
	}

	public void setTimeStamp(Date timeStamp) {
		this.timeStamp = timeStamp;
	}

	public Date getTimeStamp() {
		return timeStamp;
	}
    
    
}

