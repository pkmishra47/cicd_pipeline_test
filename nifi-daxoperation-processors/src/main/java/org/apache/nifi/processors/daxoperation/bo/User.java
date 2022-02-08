package org.apache.nifi.processors.daxoperation.bo;
import org.apache.nifi.processors.daxoperation.dbo.DBProtonDetails;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class User {
public ObjectId id;
public String salutation;
public String firstName;
public String middleName;
public String lastName;
public DBUser.UserStatus status;
public Date dateActivated;
public String mobileNumber;
public UserBasicInfo userBasicInfo;
public UserContactInfo userContactInfo;
public UserPreferenceInfo userPreferenceInfo;
public List<SugarInfo> sugarInfo;
public String aadharNumber;
public String license;
public String panCard;
public DBProtonDetails.ProtonStatus protonstatus;
public String email;
}
