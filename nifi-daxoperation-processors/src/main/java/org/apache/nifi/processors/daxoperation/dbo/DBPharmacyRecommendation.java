package org.apache.nifi.processors.daxoperation.dbo;

import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "PharmacyRecommendation", noClassnameStored = true)
public class DBPharmacyRecommendation {
    @Id
    private ObjectId id;

    @Property
    private String mobileNumber;
    
    @Property
    private List<String> pharmacyItems;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getMobileNumber() {
        return this.mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public List<String> getPharmacyItems() {
        return this.pharmacyItems;
    }

    public void setPharmacyItems(List<String> pharmacyItems) {
        this.pharmacyItems = pharmacyItems;
    }
}