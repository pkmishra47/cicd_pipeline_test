package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class UserBasicInfo {
    public ObjectId id;
    public Date dateOfBirth;
    public String sex;
    public int age;
    public String bloodGroup;
    public String martialStatus;
    public List<Education> educationList;
    public List<Occupation> occupationList;
    public Attachment profileImage;
    public String diabetesRegimen;
    public String employeeId;
    public String employeeStatus;
    public String groupName;
    public String entityUserType;
}
