package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Map;

public class ResourceMaster {
    public ObjectId id;
    public Resources resourceType;
    public String resourceName;
    public Map<String, String> resourceDetail;
    public String notes;


    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public Map<String, String> getResourceDetail() {
        return resourceDetail;
    }

    public void setResourceDetail(Map<String, String> resourceDetail) {
        this.resourceDetail = resourceDetail;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Resources getResourceType() {
        return resourceType;
    }

    public void setResourceType(Resources resourceType) {
        this.resourceType = resourceType;
    }
}