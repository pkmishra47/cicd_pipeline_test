package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.List;

public class Corporate {
    private ObjectId id;
    private String name;
    private String displayName;
    private String contactPerson;
    private String mobileNumber;
    private List<LabPackage> exclusivePackages;
    private List<LabPackage> userAlsoPackages;

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getContactPerson() {
        return this.contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getMobileNummber() {
        return this.mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public List<LabPackage> getExclusivePages() {
        return this.exclusivePackages;
    }

    public void setExclusivePackages(List<LabPackage> exclusivePackages) {
        this.exclusivePackages = exclusivePackages;
    }

    public List<LabPackage> getUserAlsoPackages() {
        return this.userAlsoPackages;
    }

    public void setUserAlsoPackages(List<LabPackage> userAlsoPackages) {
        this.userAlsoPackages  = userAlsoPackages;
    }
}
