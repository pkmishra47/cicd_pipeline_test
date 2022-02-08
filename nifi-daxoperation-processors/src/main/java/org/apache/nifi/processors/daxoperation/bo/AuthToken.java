package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class AuthToken {
    public ObjectId id;
    public String mobileNumber;
    public Date startDate;
    public String authToken;
    public List<string> assocUhid;
    public String email;
    public String uhid;
}
