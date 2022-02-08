package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.List;

public class UserContactInfo {
    public ObjectId id;
    public String addressLine1;
    public String addressLine2;
    public String city;
    public String state;
    public String country;
    public int pincode;
    public String phone1;
    public String phone2;
    public String phone3;
    public String email1;
    public String email2;
    public List<EmergencyContact> emergencyContactList;
    public List<PrimaryCarePhysician> primaryCareContactList;
    public String mobile;
}
