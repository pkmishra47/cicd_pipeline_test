package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;

public class SugarInfo {
    public ObjectId id;
    public String pacakgeId;
    public String packageName;
    public Date billingDate;
    public String treatingPhysician;
    public String preferredLanguage;
    public String dcpFlag;
    public String siteType;
}
