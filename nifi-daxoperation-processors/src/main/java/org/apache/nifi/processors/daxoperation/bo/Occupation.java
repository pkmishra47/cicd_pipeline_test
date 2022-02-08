package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;

public class Occupation {
    public ObjectId id;
    public String companyName;
    public String occupationTitle;
    public Date startDate;
    public Date endDate;
}
