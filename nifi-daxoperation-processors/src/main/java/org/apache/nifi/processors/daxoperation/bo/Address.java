package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

public class Address {
    public ObjectId id;
    public String tag;          // This denotes the address type like primary, postal, official, etc
    public String doorNumber;
    public String buildingName;
    public String streetName_1;
    public String streetName_2;
    public String cityName;
    public String stateName;
    public String countryName;
    public String pinCode;
}
