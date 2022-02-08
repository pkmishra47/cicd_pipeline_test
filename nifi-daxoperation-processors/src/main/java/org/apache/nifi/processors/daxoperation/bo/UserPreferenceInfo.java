package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.List;

public class UserPreferenceInfo {
    public ObjectId id;
    public String smsAlert;
    public String emailAlert;
    public List<string> dashboardToolList;
    public List<Widget> widgetList;
}
