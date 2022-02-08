package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;

public class Education {
    public ObjectId id;
    public String graduationLevel;
    public String graduationName;
    public Date attendedFrom;
    public Date attendedEnd;
}
