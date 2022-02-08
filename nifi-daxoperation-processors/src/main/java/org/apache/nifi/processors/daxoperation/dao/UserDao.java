package org.apache.nifi.processors.daxoperation.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.*;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.healthhiway.businessobject.Attachment;
//import com.healthhiway.businessobject.DataImportCount;
//import com.healthhiway.businessobject.Education;
//import com.healthhiway.businessobject.EmergencyContact;
//import com.healthhiway.businessobject.Occupation;
//import com.healthhiway.businessobject.PrimaryCarePhysician;
//import com.healthhiway.businessobject.SugarInfo;
//import com.healthhiway.businessobject.User;
//import com.healthhiway.businessobject.UserBasicInfo;
//import com.healthhiway.businessobject.UserContactInfo;
//import com.healthhiway.businessobject.UserIdentityData;
//import com.healthhiway.businessobject.UserPreferenceInfo;


public class UserDao extends BasicDAO<DBUser, ObjectId> {
    private static Logger log = LoggerFactory.getLogger(UserDao.class);
    IdentityDao identityDao;

    public UserDao(MongoClient mongoClient) {
        super(DBUser.class, new MongoDBUtil(mongoClient).getDb());
        ensureIndexes();
    }

    public List<DBUser> getUsers() {
        return find().asList();
    }

    //This method should be used only during import logic, very unsafe.
    public DBUser getUserByFirstName(String firstName) {
        DBUser dbUser = null;
        if (firstName == null)
            return dbUser;

        dbUser = createQuery().filter("firstName", firstName).get();

        return dbUser;
    }

    public List<DBUser> getUsers(String firstName, String secondName) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();
        if (firstName == null || secondName == null)
            return dbUserList;

        Query<DBUser> q = createQuery();
        q.or(
                q.criteria("firstName").equal(firstName),
                q.criteria("lastName").equal(secondName)
        );
        dbUserList = q.asList();

        return dbUserList;
    }

    // Can search by patterns, we might need for search functions
    public List<DBUser> findUsers(String regExpression) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();

        if (regExpression == null)
            return dbUserList;

        Query<DBUser> q = createQuery();
        q.disableValidation().filter("firstName", regExpression).enableValidation();
        dbUserList = q.asList();

        return dbUserList;
    }


    public DBUser getUserByID(ObjectId objectID) {
        return get(objectID);
    }

    // Mani - handle error and null and exception conditions
    public DBUser getUserByID(String objectId) {
        ObjectId oId = null;
        DBUser dbUser = null;
        if (ObjectId.isValid(objectId)) {
            oId = new ObjectId(objectId);
        }
        if (oId != null)
            dbUser = getUserByID(oId);

        return dbUser;
    }

    public List<DBPrescription> getNewPrescription(ObjectId objectID) {
        return get(objectID).getPrescriptions();
    }

    public List<DBAllergy> getAllergy(ObjectId objectID) {
        return get(objectID).getAllergy();
    }

    public List<DBImmunization> getImmunization(ObjectId objectId) {
        return get(objectId).getImmunizations();
    }

    public List<DBMedication> getMedication(ObjectId objectId) {
        return get(objectId).getMedications();
    }

    public List<DBSleepPattern> getSleepPAttern(ObjectId objectId) {
        return get(objectId).getSleepPattern();
    }

    public List<DBDairyDetails> getDairyDetaills(ObjectId objectId) {
        return get(objectId).getDairyDetails();
    }

    public List<DBUser> getUserByImportDate(long dateOfImport) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();
        Query<DBUser> dbUserQuery = createQuery().retrievedFields(true, "id").filter("dateImported", new Date(dateOfImport));
        dbUserList.addAll(dbUserQuery.asList());
        return dbUserList;
    }

    public List<ObjectId> getUserObjectIdByImportDate(long dateOfImport) {
        List<ObjectId> userObjectIdList = new ArrayList<ObjectId>();
        for (DBUser dbUser : getUserByImportDate(dateOfImport))
            userObjectIdList.add(dbUser.getId());
        return userObjectIdList;
    }

    public List<DBUser> getUserBetweenImportDate(long date1, long date2) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();
        Query<DBUser> dbUserQuery = createQuery().retrievedFields(true, "id");
        dbUserQuery.field("dateImported").greaterThanOrEq(new Date(date1));
        dbUserQuery.field("dateImported").lessThanOrEq(new Date(date2));

        dbUserList.addAll(dbUserQuery.asList());
        return dbUserList;
    }

    public List<ObjectId> getObjectIdBetweenImportDate(long date1, long date2) {
        List<ObjectId> userObjectIdList = new ArrayList<ObjectId>();
        for (DBUser dbUser : getUserBetweenImportDate(date1, date2))
            userObjectIdList.add(dbUser.getId());
        return userObjectIdList;
    }

    public void deleteMedication(ObjectId userId, ObjectId medicationId) {
        if (userId == null || medicationId == null)
            return;

        Query<DBUser> q = createQuery().field("id").equal(userId).limit(1);
        UpdateOperations<DBUser> ops = createUpdateOperations().removeAll(
                "medications", medicationId);
        update(q, ops);

    }

    public void deleteAllergy(ObjectId userId, ObjectId allergyObjId) {
        if (userId == null || allergyObjId == null)
            return;
        System.out.println("deleting allergy from userDao");
        System.out.println("User id " + userId.toString() + " allergy id " + allergyObjId.toString());
        Query<DBUser> q = createQuery().field("id").equal(userId);
        System.out.println(q.countAll());
        UpdateOperations<DBUser> ops = createUpdateOperations().removeAll(
                "allergy", allergyObjId);
        System.out.println(updateFirst(q, ops));
        System.out.println("Delete done");
    }

//	public static DBUserContactInfo newDBUserContactInfo(UserContactInfo userContactInfo) {
//		DBUserContactInfo dbUsrContactInfo = new DBUserContactInfo();
//
//		if (userContactInfo == null)
//			return dbUsrContactInfo;
//
//		dbUsrContactInfo.setAddressLine1(userContactInfo.getAddressLine1());
//		dbUsrContactInfo.setAddressLine2(userContactInfo.getAddressLine2());
//		dbUsrContactInfo.setCity(userContactInfo.getCity());
//		dbUsrContactInfo.setState(userContactInfo.getState());
//		dbUsrContactInfo.setCountry(userContactInfo.getCountry());
//		dbUsrContactInfo.setPincode(userContactInfo.getPincode());
//
//		dbUsrContactInfo.setPhone1(userContactInfo.getPhone1());
//		dbUsrContactInfo.setPhone2(userContactInfo.getPhone2());
//		dbUsrContactInfo.setPhone3(userContactInfo.getPhone3());
//		dbUsrContactInfo.setEmail1(userContactInfo.getEmail1());
//		dbUsrContactInfo.setEmail2(userContactInfo.getEmail2());
//
//		if ( userContactInfo.getPrimaryCareContactList() != null )
//		{
//			for(PrimaryCarePhysician p : userContactInfo.getPrimaryCareContactList()) {
//				DBPrimaryCarePhysician dp = new DBPrimaryCarePhysician();
//				dp.setSalutation(p.getSalutation());
//				dp.setFirstName(p.getFirstName());
//				dp.setMiddleName(p.getMiddleName());
//				dp.setLastName(p.getLastName());
//				dp.setPhone1(p.getPhone1());
//				dp.setPhone2(p.getPhone2());
//				dp.setEmail1(p.getEmail1());
//				dbUsrContactInfo.addPrimaryCarePhysician(dp);
//			}
//		}
//
//		if ( userContactInfo.getEmergencyContactList() != null )
//		{
//			for(EmergencyContact emerg : userContactInfo.getEmergencyContactList()) {
//				DBEmergencyContact dbEme = new DBEmergencyContact();
//				dbEme.setSalutation(emerg.getSalutation());
//				dbEme.setFirstName(emerg.getFirstName());
//				dbEme.setMiddleName(emerg.getMiddleName());
//				dbEme.setLastName(emerg.getLastName());
//				dbEme.setPhone1(emerg.getPhone1());
//				dbEme.setPhone2(emerg.getPhone2());
//				dbEme.setEmail1(emerg.getEmail1());
//				dbUsrContactInfo.addEmergencyContact(dbEme);
//			}
//		}
//
//		return dbUsrContactInfo;
//	}
//	public static UserContactInfo newUserContactInfo(DBUserContactInfo userContactInfo) {
//		UserContactInfo uContactInfo = new UserContactInfo();
//
//		if (userContactInfo == null)
//			return uContactInfo;
//
//		uContactInfo.setId(userContactInfo.getId().toString());
//
//		uContactInfo.setAddressLine1(userContactInfo.getAddressLine1());
//		uContactInfo.setAddressLine2(userContactInfo.getAddressLine2());
//
//		uContactInfo.setCity(userContactInfo.getCity());
//		uContactInfo.setState(userContactInfo.getState());
//		uContactInfo.setCountry(userContactInfo.getCountry());
//		uContactInfo.setPincode(userContactInfo.getPincode());
//
//		uContactInfo.setPhone1(userContactInfo.getPhone1());
//		uContactInfo.setPhone2(userContactInfo.getPhone2());
//		uContactInfo.setPhone3(userContactInfo.getPhone3());
//		uContactInfo.setEmail1(userContactInfo.getEmail1());
//		uContactInfo.setEmail2(userContactInfo.getEmail2());
//
//		for(DBPrimaryCarePhysician p : userContactInfo.getPrimaryCarePhysicialList()) {
//			PrimaryCarePhysician dp = new PrimaryCarePhysician();
//			dp.setId(p.getId().toString());
//			dp.setSalutation(p.getSalutation());
//			dp.setFirstName(p.getFirstName());
//			dp.setMiddleName(p.getMiddleName());
//			dp.setLastName(p.getLastName());
//			dp.setPhone1(p.getPhone1());
//			dp.setPhone2(p.getPhone2());
//			dp.setEmail1(p.getEmail1());
//			uContactInfo.addToPrimaryCareContactList(dp);
//		}
//
//		for(DBEmergencyContact emerg : userContactInfo.getEmergencyContactList()) {
//			EmergencyContact dbEme = new EmergencyContact();
//			dbEme.setId(emerg.getId().toString());
//			dbEme.setSalutation(emerg.getSalutation());
//			dbEme.setFirstName(emerg.getFirstName());
//			dbEme.setMiddleName(emerg.getMiddleName());
//			dbEme.setLastName(emerg.getLastName());
//			dbEme.setPhone1(emerg.getPhone1());
//			dbEme.setPhone2(emerg.getPhone2());
//			dbEme.setEmail1(emerg.getEmail1());
//			uContactInfo.addToEmergencyContactList(dbEme);
//		}
//		return uContactInfo;
//	}
//
    // Date formating exception has to be handled gracefully.
//	public static DBUserBasicInfo newDBUserBasicInfo(UserBasicInfo userBasicInfo) {
//
//		DBUserBasicInfo dbUsrBasicInfo = new DBUserBasicInfo();
//
//		if (userBasicInfo == null)
//			return dbUsrBasicInfo;
//
//		if(userBasicInfo.getDateOfBirth() != 0)
//			dbUsrBasicInfo.setDateOfBirth(new Date(userBasicInfo.getDateOfBirth()));
//		else
//			dbUsrBasicInfo.setDateOfBirth(null);
//		dbUsrBasicInfo.setBloodGroup(userBasicInfo.getBloodGroup());
//		dbUsrBasicInfo.setMartialStatus(userBasicInfo.getMartialStatus());
//		dbUsrBasicInfo.setSex(userBasicInfo.getSex());
//		dbUsrBasicInfo.setAge(userBasicInfo.getAge());
//        dbUsrBasicInfo.setDiabetesRegimen(userBasicInfo.getDiabetesRegimen());
//        dbUsrBasicInfo.setEmployeeId(userBasicInfo.getEmployeeId());
//        dbUsrBasicInfo.setEmployeeStatus(userBasicInfo.getEmployeeStatus());
//        dbUsrBasicInfo.setGroupName(userBasicInfo.getGroupName());
//		if (userBasicInfo.getEducationList() != null )
//		{
//			for(Education edu : userBasicInfo.getEducationList()) {
//				DBEducation dbEdu = new DBEducation();
//				dbEdu.setGraduationLevel(edu.getGraduationLevel());
//				dbEdu.setGraduationName(edu.getGraduationName());
//				dbEdu.setAttendedFrom(new Date(edu.getAttendedFrom()));
//				dbEdu.setAttendedEnd(new Date(edu.getAttendedEnd()));
//				dbUsrBasicInfo.addEducationDetail(dbEdu);
//			}
//		}
//
//		if ( userBasicInfo.getOccupationList() != null )
//		{
//			for(Occupation ocu : userBasicInfo.getOccupationList()) {
//				DBOccupation dbOcu = new DBOccupation();
//				dbOcu.setCompanyName(ocu.getCompanyName());
//				dbOcu.setOccupationTitle(ocu.getCompanyName());
//				dbOcu.setStartDate(new Date(ocu.getStartDate()));
//				dbOcu.setEndDate(new Date(ocu.getEndDate()));
//				dbUsrBasicInfo.addOccupationDetail(dbOcu);
//			}
//		}
//
//		Attachment profileImage = userBasicInfo.getProfileImage();
//		if(profileImage != null)
//			dbUsrBasicInfo.setProfileImage(DBUtil.writeDBAttachement(profileImage));
//
//		return dbUsrBasicInfo;
//	}

//	public static UserBasicInfo newUserBasicInfo(DBUserBasicInfo dbUserBasicInfo) {
//		UserBasicInfo uBasicInfo = new UserBasicInfo();
//		if (dbUserBasicInfo == null)
//			return uBasicInfo;
//
//		uBasicInfo.setId(dbUserBasicInfo.getId().toString());
//		if (dbUserBasicInfo.getDateOfBirth() != null ){
//			uBasicInfo.setDateOfBirth(dbUserBasicInfo.getDateOfBirth().getTime());
//			if ( dbUserBasicInfo.getDateOfBirth().getTime() != 0 )
//				uBasicInfo.setAge(DateUtil.getAge(dbUserBasicInfo.getDateOfBirth()));
//		}
//		uBasicInfo.setBloodGroup(dbUserBasicInfo.getBloodGroup());
//		uBasicInfo.setMartialStatus(dbUserBasicInfo.getMartialStatus());
//		uBasicInfo.setSex(dbUserBasicInfo.getSex());
//
//		uBasicInfo.setDiabetesRegimen(dbUserBasicInfo.getDiabetesRegimen());
//		uBasicInfo.setEmployeeId(dbUserBasicInfo.getEmployeeId());
//		uBasicInfo.setEmployeeStatus(dbUserBasicInfo.getEmployeeStatus());
//		uBasicInfo.setGroupName(dbUserBasicInfo.getGroupName());
//		for(DBEducation edu : dbUserBasicInfo.getEducationDetailList()) {
//			Education dbEdu = new Education();
//			dbEdu.setId(edu.getId().toString());
//			dbEdu.setGraduationLevel(edu.getGraduationLevel());
//			dbEdu.setGraduationName(edu.getGraduationName());
//			if(edu.getAttendedFrom() != null)
//				dbEdu.setAttendedFrom(edu.getAttendedFrom().getTime());
//			if(edu.getAttendedEnd() != null)
//				dbEdu.setAttendedEnd(edu.getAttendedEnd().getTime());
//			uBasicInfo.addToEducationList(dbEdu);
//		}
//
//		for(DBOccupation ocu : dbUserBasicInfo.getOccupationDetailList()) {
//			Occupation dbOcu = new Occupation();
//			dbOcu.setId(ocu.getId().toString());
//			dbOcu.setCompanyName(ocu.getCompanyName());
//			dbOcu.setOccupationTitle(ocu.getCompanyName());
//			if(ocu.getStartDate() != null)
//				dbOcu.setStartDate(ocu.getStartDate().getTime());
//			if(ocu.getEndDate() != null)
//				dbOcu.setEndDate(ocu.getEndDate().getTime());
//			uBasicInfo.addToOccupationList(dbOcu);
//		}
//
//
//		DBAttachement profileImage = dbUserBasicInfo.getProfileImage();
//		if(profileImage != null && profileImage.getFileAttached() != null
//				&& String.valueOf(profileImage.getFileAttached()).trim().length() != 0)
//			uBasicInfo.setProfileImage(DBUtil.readDBAttachement(profileImage));
//		return uBasicInfo;
//	}

//	public static DBUserPreferenceInfo newDBUserPreferenceInfo(UserPreferenceInfo userPreferenceInfo) {
//
//		DBUserPreferenceInfo dbUserPreferenceInfo = new DBUserPreferenceInfo();
//
//		if (userPreferenceInfo == null)
//			return dbUserPreferenceInfo;
//
//		dbUserPreferenceInfo.setEmailAlert(userPreferenceInfo.getEmailAlert());
//		dbUserPreferenceInfo.setSmsAlert(userPreferenceInfo.getSmsAlert());
//		dbUserPreferenceInfo.setDashboardToolList(userPreferenceInfo.getDashboardToolList());
//		dbUserPreferenceInfo.setWidgetList(userPreferenceInfo.getWidgetList());
//
//		return dbUserPreferenceInfo;
//	}

//	public static UserPreferenceInfo newUserPreferenceInfo(DBUserPreferenceInfo dbUserPreferenceInfo)
//	{
//		UserPreferenceInfo userPreferenceInfo = new UserPreferenceInfo();
//		if (dbUserPreferenceInfo == null)
//			return userPreferenceInfo;
//
//		userPreferenceInfo.setId(String.valueOf(dbUserPreferenceInfo.getId()));
//		userPreferenceInfo.setSmsAlert(dbUserPreferenceInfo.getSmsAlert());
//		userPreferenceInfo.setEmailAlert(dbUserPreferenceInfo.getEmailAlert());
//		userPreferenceInfo.setDashboardToolList(dbUserPreferenceInfo.getDashboardToolList());
//		userPreferenceInfo.setWidgetList(dbUserPreferenceInfo.getWidgetList());
//
//		return userPreferenceInfo;
//	}

//	public static List<DBSugarInfo> newDBSugarInfo(List<SugarInfo> sugarInfo) {
//		List<DBSugarInfo> dbSugarInfoList = new ArrayList<DBSugarInfo>();
//		DBSugarInfo dbSugarInfo = new DBSugarInfo();
//		if(sugarInfo == null)
//			return dbSugarInfoList;
//		for( SugarInfo temp : sugarInfo){
//			dbSugarInfo.setPacakgeId(temp.getPacakgeId());
//			dbSugarInfo.setPackageName(temp.getPackageName());
//			dbSugarInfo.setBillingDate(new Date(temp.getBillingDate()));
//			dbSugarInfo.setTreatingPhysician(temp.getTreatingPhysician());
//			dbSugarInfo.setPreferredLanguage(temp.getPreferredLanguage());
//			dbSugarInfo.setDcpFlag(temp.getDcpFlag());
//
//			dbSugarInfoList.add(dbSugarInfo);
//		}
//		return dbSugarInfoList;
//	}

//	public static List<SugarInfo> newSugarInfo(List<DBSugarInfo> dbSugarInfo) {
//		List<SugarInfo> sugarInfoList = new ArrayList<SugarInfo>();
//		if(dbSugarInfo == null)
//			return sugarInfoList;
//
//		for( DBSugarInfo temp : dbSugarInfo) {
//			SugarInfo sugarInfo = new SugarInfo();
//			sugarInfo.setId(temp.getId().toString());
//			sugarInfo.setPacakgeId(temp.getPacakgeId());
//			sugarInfo.setPackageName(temp.getPackageName());
//			if(temp.getBillingDate()!=null){
//				sugarInfo.setBillingDate(temp.getBillingDate().getTime());
//			}
//			sugarInfo.setPreferredLanguage(temp.getPreferredLanguage());
//			sugarInfo.setTreatingPhysician(temp.getTreatingPhysician());
//			sugarInfo.setDcpFlag(temp.getDcpFlag());
//
//			sugarInfoList.add(sugarInfo);
//		}
//		return sugarInfoList;
//	}

//	public static DBUser newDBUser(User user) {
//		DBUser dbUser = new DBUser();
//		if (user == null)
//			return dbUser;
//
//		dbUser.setSalutation(user.getSalutation());
//		dbUser.setFirstName(user.getFirstName());
//		dbUser.setMiddleName(user.getMiddleName());
//		dbUser.setLastName(user.getLastName());
//
//		switch (user.getStatus()) {
//		case ACTIVE:
//			dbUser.setStatus(UserStatus.ACTIVE);
//			break;
//		case INACTIVE:
//			dbUser.setStatus(UserStatus.INACTIVE);
//			break;
//		case TERMINATED:
//			dbUser.setStatus(UserStatus.TERMINATED);
//			break;
//		case NOT_ACTIVATED:
//			dbUser.setStatus(UserStatus.NOT_ACTIVATED);
//			break;
//		}
//
//		dbUser.setMobileNumber(user.getMobileNumber());
//		if(user.getAadharNumber() != null)
//		{ dbUser.setAadharNumber(user.getAadharNumber());
//		}else{
//			dbUser.setAadharNumber("NA");
//		}
//		if(user.getLicense() != null)
//		{ dbUser.setLicense(user.getLicense());
//		}else{
//			dbUser.setLicense("NA");
//		}
//		if(user.getPanCard() != null)
//		{ dbUser.setPanCard(user.getPanCard());
//		}else{
//			dbUser.setPanCard("NA");
//		}
//
//		dbUser.setUserBasicInfo(newDBUserBasicInfo(user.getUserBasicInfo()));
//		dbUser.setUserContactInfo(newDBUserContactInfo(user.getUserContactInfo()));
//		dbUser.setUserPreferenceInfo(newDBUserPreferenceInfo(user.getUserPreferenceInfo()));
//		dbUser.setSugarInfo(newDBSugarInfo(user.getSugarInfo()));
//		return dbUser;
//	}

//	public static User newUser(DBUser dbUser) {
//		User user = new User();
//		if (dbUser == null)
//			return user;
//
//		user.setSalutation(dbUser.getSalutation());
//		user.setId(dbUser.getId().toString());
//		user.setFirstName(dbUser.getFirstName());
//		user.setLastName(dbUser.getLastName());
//		user.setMiddleName(dbUser.getMiddleName());
//		switch (dbUser.getStatus()) {
//		case ACTIVE:
//			user.setStatus(com.healthhiway.businessobject.UserStatus.ACTIVE);
//			break;
//		case INACTIVE:
//			user.setStatus(com.healthhiway.businessobject.UserStatus.INACTIVE);
//			break;
//		case TERMINATED:
//			user.setStatus(com.healthhiway.businessobject.UserStatus.TERMINATED);
//			break;
//		case NOT_ACTIVATED:
//			user.setStatus(com.healthhiway.businessobject.UserStatus.NOT_ACTIVATED);
//			break;
//		}
//		if(dbUser.getDateActivated() != null) //Will not be null, only if activated.
//		user.setDateActivated(dbUser.getDateActivated().getTime());
//
//		user.setMobileNumber(dbUser.getMobileNumber());
//		if(dbUser.getAadharNumber() != null)
//		{ user.setAadharNumber(dbUser.getAadharNumber());
//		}else{
//			user.setAadharNumber("NA");
//		}
//		if(dbUser.getLicense() != null)
//		{ user.setLicense(dbUser.getLicense());
//		}else{
//			user.setLicense("NA");
//		}
//		if(dbUser.getPanCard() != null)
//		{ user.setPanCard(dbUser.getPanCard());
//		}else{
//			user.setPanCard("NA");
//		}
//
//		user.setUserBasicInfo(newUserBasicInfo(dbUser.getUserBasicInfo()));
//		user.setUserContactInfo(newUserContactInfo(dbUser.getUserContactInfo()));
//		user.setUserPreferenceInfo(newUserPreferenceInfo(dbUser.getUserPreferenceInfo()));
//		user.setSugarInfo(newSugarInfo(dbUser.getSugarInfo()));
//		return user;
//	}

//	public List<DataImportCount> getUserImportCountByDateRange(List<ObjectId> userObjectIdList,long fromDate,long toDate)
//	{
//		List<DataImportCount> dataImportCountList = new ArrayList<DataImportCount>();
//
//		String reduceString = "function(obj,prev) { prev.noOfUserImported ++; }";
//
//		BasicDBObject key = new BasicDBObject();
//        key.put("dateImported", "true");
//
//        BasicDBObject cond = new BasicDBObject();
//        cond.put("dateImported", new BasicDBObject("$gte", new Date(fromDate)));
//        cond.put("dateImported", new BasicDBObject("$lte", new Date(toDate)));
//        cond.put("_id" , new BasicDBObject("$in", userObjectIdList));
//
//        BasicDBObject initial = new BasicDBObject();
//        initial.put("noOfUserImported", 0);
//
//        DBCollection coll= getCollection();
//        DBObject dbObject= coll.group(key, cond, initial, reduceString);
//
//        @SuppressWarnings("rawtypes")
//		Map dbObjectMap                  = dbObject.toMap();
//        DataImportCount dataImportCount  = null;
//        SimpleDateFormat mongoDateFormat = new SimpleDateFormat("E MMM dd hh:mm:ss z yyyy");
//        for ( int i= 0; i < dbObjectMap.size();i++)
//        {
//            dataImportCount = new DataImportCount();
//            com.mongodb.CommandResult result = (com.mongodb.CommandResult)dbObject.get(""+i);
//            try
//            {
//            	Date mongoDate =  mongoDateFormat.parse(result.getString("dateImported"));
//            	dataImportCount.setImportDate(mongoDate.getTime());
//            }
//            catch(ParseException pe)
//            {
//            	log.error("PHR | UserDao | getUserImportCountByDateRange | Error ",pe);
//            	dataImportCountList = new ArrayList<DataImportCount>();
//            	break;
//            }
//            dataImportCount.setImportCount(result.getString("noOfUserImported"));
//            dataImportCountList.add(dataImportCount);
//       }
//		return dataImportCountList;
//	}

    public List<DBUser> findDBUserByEmail(String eMail) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();

        if (eMail == null)
            return dbUserList;

        Query<DBUser> q = createQuery().filter("userContactInfo.email1", eMail);
        dbUserList = q.asList();
        log.info("userList is {}" + dbUserList);
        return dbUserList;
    }

    public List<ObjectId> findUserObjectIdByMobileNumber(String mobileNumber) {
        List<ObjectId> userObjectIdList = new ArrayList<ObjectId>();

        Query<DBUser> q = createQuery().filter("mobileNumber", mobileNumber);

        for (DBUser dbUser : q.asList())
            userObjectIdList.add(dbUser.getId());

        return userObjectIdList;
    }

    public List<ObjectId> findUserObjectIdByEmail(String eMail) {
        List<ObjectId> userObjectIdList = new ArrayList<ObjectId>();

        Query<DBUser> q = createQuery().filter("userContactInfo.email1", eMail);

        for (DBUser dbUser : q.asList())
            userObjectIdList.add(dbUser.getId());

        return userObjectIdList;
    }


    public List<DBUser> findDBUserByRoleName(String roleName) {
        Query<DBUser> q = createQuery().field("roles").contains(roleName);

        return q.asList();
    }

    public List<DBUser> findDBUserByGroupName(String groupName) {
        Query<DBUser> q = createQuery().field("userBasicInfo.groupName").equal(groupName);
        return q.asList();
    }

    public List<DBUser> findDBUserByUserCreationRoleName(String userCreationRoleName) {
        Query<DBUser> q = createQuery().field("userCreationRoleName").equal(userCreationRoleName);
        return q.asList();
    }

    public List<DBUser> findDBUserByEntity(String entityName) {
        Query<DBUser> q = createQuery().field("entity").equal(entityName);
        return q.asList();
    }

    public List<DBUser> findDBUserByEntityShortName(String entityShortName) {
        Query<DBUser> q = createQuery().field("entitys").contains(entityShortName);
        return q.asList();
    }

    public List<DBUser> findDBUserByDefaultEntity(String entityShortName) {
        Query<DBUser> q = createQuery().field("defaultEntity").equal(entityShortName);
        return q.asList();
    }

//	public List<UserIdentityData> findUserIdentityDataByEmail(String eMail) {
//		List<DBUser> dbUserList = findDBUserByEmail(eMail);
//		return  getUserIdentityDataList(dbUserList);
//	}

//	public  List<UserIdentityData>  getUserIdentityDataList(List<DBUser> dbUserList )	{
//		List<UserIdentityData> userIdentityDataList = new ArrayList<UserIdentityData>();
//		for(DBUser dbUser : dbUserList){
//			userIdentityDataList.add(getUserIdentityData(dbUser));
//		}
//		return userIdentityDataList;
//	}

//	public UserIdentityData getUserIdentityData(DBUser dbUser) {
//		UserIdentityData userIdentityData = new UserIdentityData();
//		if (dbUser != null){
//			userIdentityData.setFirstName(dbUser.getFirstName());
//			userIdentityData.setLastName(dbUser.getLastName());
//			userIdentityData.setMobileNumber(dbUser.getMobileNumber());
//			userIdentityData.setDefaultEntity(dbUser.getDefaultEntity());
//			userIdentityData.setUserCreationRoleName(dbUser.getUserCreationRoleName());
//			userIdentityData.setCorporateName(dbUser.getCorporateName());
//			if ( dbUser.getUserContactInfo() != null )
//				userIdentityData.setEmail(dbUser.getUserContactInfo().getEmail1());
//
//			userIdentityData.setRoles(dbUser.getRoles());
//			userIdentityData.setEntitys(dbUser.getEntitys());
//			userIdentityData.setUserObjectId(String.valueOf(dbUser.getId()));
//			userIdentityData.setUserStatus(String.valueOf(dbUser.getStatus()));
//
//			List<DBIdentity> dbIdentityList = identityDao.findDBIdentityByUserObjectId(dbUser.getId());
//
//			List<String> uhidList   = new ArrayList<String>();
//
//			for ( DBIdentity dbIdentity : dbIdentityList )
//			{
//				if ( dbIdentity.getUserId() != null)
//				{
//					userIdentityData.setUserId(dbIdentity.getUserId());
//					userIdentityData.setPassword(dbIdentity.getPassword());
//					userIdentityData.setSiteKey(dbIdentity.getSiteKey());
//					userIdentityData.setUserUhid(dbIdentity.getUhid());
//
//				}
//				if ( dbIdentity.getUhid() != null){
//					uhidList.add(dbIdentity.getUhid());
//				}
//			}
//		}
//		return userIdentityData;
//	}


    public DBUser findDBUserByReminderObjectId(ObjectId reminderObjectId) {
        DBUser dbUser = null;
        if (reminderObjectId != null)
            dbUser = createQuery().disableValidation().field("reminders.$id").equal(reminderObjectId).get();
        return dbUser;
    }

    public long getNoOfUsersImported(long fromDate, long toDate, String siteKey) {
        long startTime = System.currentTimeMillis();
        log.info("PHR | UserDao | Coming into getNoOfUsersImported | Start Time " + startTime);
        long noOfUser = 0;
        Query<DBUser> dbUserQuery = createQuery();
        dbUserQuery.field("dateImported").greaterThanOrEq(new Date(fromDate));
        dbUserQuery.field("dateImported").lessThanOrEq(new Date(toDate));

        List<ObjectId> userObjectIdList = new ArrayList<ObjectId>();

        for (DBUser dbUser : dbUserQuery.asList())
            userObjectIdList.add(dbUser.getId());

        if (userObjectIdList.size() != 0)
            noOfUser = identityDao.getNoOfUsersImported(userObjectIdList, siteKey);

        log.info("PHR | UserDao | getNoOfUsersImported | No of User " + noOfUser);

        log.info("PHR | UserDao | Exiting from getNoOfUsersImported | Time Taken" + (System.currentTimeMillis() - startTime));
        return noOfUser;
    }

    public List<String> getUhidsByImportedDate(long fromDate, long toDate, String siteKey) {
        long startTime = System.currentTimeMillis();
        log.info("PHR | UserDao | Coming into getUhidsByImportedDate | Start Time " + startTime);
        Query<DBUser> dbUserQuery = createQuery();
        dbUserQuery.field("dateImported").greaterThanOrEq(new Date(fromDate));
        dbUserQuery.field("dateImported").lessThanOrEq(new Date(toDate));

        List<ObjectId> userObjectIdList = new ArrayList<ObjectId>();
        List<String> uhidList = new ArrayList<String>();
        for (DBUser dbUser : dbUserQuery.asList())
            userObjectIdList.add(dbUser.getId());

        if (userObjectIdList.size() != 0)
            uhidList = identityDao.getUhids(userObjectIdList, siteKey);

        log.info("PHR | UserDao | Exiting from getUhidsByImportedDate | Time Taken" + (System.currentTimeMillis() - startTime));
        return uhidList;
    }

    public List<DBUser> getUsersByUpdatedDate(long date) {
        long startTime = System.currentTimeMillis();
        log.info("PHR | UserDao | Coming into getUhidsByImportedDate | Start Time " + startTime);
        Query<DBUser> dbUserQuery = createQuery();
        dbUserQuery.field("updatedAt").greaterThanOrEq(new Date(date));

        List<DBUser> userList = new ArrayList<DBUser>();
        for (DBUser dbUser : dbUserQuery.asList())
            userList.add(dbUser);

        log.info("PHR | UserDao | Exiting from getUsersByUpdatedDate | Time Taken" + (System.currentTimeMillis() - startTime));
        return userList;
    }

    public DBUser findDBUserByLabTestObjectId(ObjectId labTestObjectId) {
        DBUser dbUser = null;
        if (labTestObjectId != null)
            dbUser = createQuery().disableValidation().field("labtests.$id").equal(labTestObjectId).get();
        return dbUser;
    }

    public DBUser getDoctor(String emailId) {
        DBUser dbUser = null;
        if (emailId != null)
            dbUser = createQuery().filter("userContactInfo.email1", emailId).get();

        return dbUser;
    }

    public List<DBUser> findDBUserByEmail(String defaultEntity, String searchBy, String searchValue) {

        List<DBUser> dbUserList = new ArrayList<DBUser>();
        if (defaultEntity == null || searchBy == null || searchValue == null)
            return dbUserList;
        Query<DBUser> q = createQuery();
        q.and(
                q.criteria("entitys").contains(defaultEntity),
                q.criteria("userContactInfo.email1").equal(searchValue),
                q.criteria("userBasicInfo.entityUserType").equal("Employee")
        );
        dbUserList = q.asList();
        return dbUserList;
    }

    public List<DBUser> findDBUserByEmployeeId(String defaultEntity, String searchBy, String searchValue) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();
        if (defaultEntity == null || searchBy == null || searchValue == null)
            return dbUserList;
        Pattern regExp = Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE);
        Query<DBUser> q = createQuery().filter("userBasicInfo.employeeId", regExp);
        q.and(
                q.criteria("entitys").contains(defaultEntity),
                q.criteria("userBasicInfo.entityUserType").equal("Employee")
        );
        dbUserList = q.asList();
        return dbUserList;
    }

    public List<DBUser> findDBUserByName(String defaultEntity, String searchBy, String searchValue) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();
        if (defaultEntity == null || searchBy == null || searchValue == null)
            return dbUserList;
        Query<DBUser> q = createQuery();
        q.and(
                q.criteria("entitys").contains(defaultEntity),
                q.or(
                        q.criteria("firstName").equal(Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE)),
                        q.criteria("middleName").equal(Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE)),
                        q.criteria("lastName").equal(Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE))
                ),
                q.criteria("userBasicInfo.entityUserType").equal("Employee")
        );
        dbUserList = q.asList();
        return dbUserList;
    }

    public List<DBUser> findDBUserByMobile(String defaultEntity, String searchBy, String searchValue) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();
        log.info("  EntityAdminService Get Users into get users function ");
        log.info("searchBy is {} defaultEntity: {}", searchBy, defaultEntity);
        log.info("searchValue is {}", searchValue);
        if (defaultEntity == null || searchBy == null || searchValue == null)
            return dbUserList;
        Query<DBUser> q = createQuery();
        q.and(
                q.criteria("entitys").contains(defaultEntity),
                q.criteria("mobileNumber").equal(searchValue),
                q.criteria("userBasicInfo.entityUserType").equal("Employee")
        );
        dbUserList = q.asList();
        log.info("userList is {}" + dbUserList);
        return dbUserList;
    }

    public List<DBUser> findDoctorByMobile(String defaultEntity, String searchBy, String searchValue) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();
        log.info("  EntityAdminService Get Users into get users function ");
        log.info("searchBy is {} defaultEntity: {}", searchBy, defaultEntity);
        log.info("searchValue is {}", searchValue);
        if (defaultEntity == null || searchBy == null || searchValue == null)
            return dbUserList;
        Query<DBUser> q = createQuery();
        q.and(
                q.criteria("entitys").contains(defaultEntity),
                q.criteria("mobileNumber").equal(searchValue),
                q.criteria("userBasicInfo.entityUserType").equal("Doctor")
        );
        dbUserList = q.asList();
        log.info("userList is {}" + dbUserList);
        return dbUserList;
    }

    public List<DBUser> findDoctorByEmail(String defaultEntity, String searchBy,
                                          String searchValue) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();
        if (defaultEntity == null || searchBy == null || searchValue == null)
            return dbUserList;
        Query<DBUser> q = createQuery();
        q.and(
                q.criteria("entitys").contains(defaultEntity),
                q.criteria("userContactInfo.email1").equal(searchValue),
                q.criteria("userBasicInfo.entityUserType").equal("Doctor")
        );
        dbUserList = q.asList();
        return dbUserList;
    }

    public List<DBUser> findDoctorByName(String defaultEntity, String searchBy,
                                         String searchValue) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();
        if (defaultEntity == null || searchBy == null || searchValue == null)
            return dbUserList;
        Query<DBUser> q = createQuery();
        q.and(
                q.criteria("entitys").contains(defaultEntity),
                q.or(
                        q.criteria("firstName").equal(Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE)),
                        q.criteria("middleName").equal(Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE)),
                        q.criteria("lastName").equal(Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE))
                ),
                q.criteria("userBasicInfo.entityUserType").equal("Doctor")
        );
        dbUserList = q.asList();
        return dbUserList;
    }

    public List<DBUser> findEmployeeByName(String entity, String groupName, String searchBy, String searchValue) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();
        if (entity == null || entity.trim().length() == 0 ||
                groupName == null || groupName.trim().length() == 0)
            return dbUserList;
        Query<DBUser> q = createQuery();
        q.and(
                q.criteria("entitys").contains(entity),
                q.criteria("userBasicInfo.groupName").equal(groupName),
                q.or(
                        q.criteria("firstName").equal(Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE)),
                        q.criteria("middleName").equal(Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE)),
                        q.criteria("lastName").equal(Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE))
                ),
                q.criteria("userBasicInfo.entityUserType").equal("Employee")
        );
        dbUserList = q.asList();
        return dbUserList;
    }


    public List<DBUser> findEmployeeByEmployeeId(String entity, String groupName, String searchBy, String searchValue) {
        List<DBUser> dbUserList = new ArrayList<DBUser>();
        if (entity == null || entity.trim().length() == 0 ||
                groupName == null || groupName.trim().length() == 0)
            return dbUserList;
        Query<DBUser> q = createQuery();
        q.and(
                q.criteria("entitys").contains(entity),
                q.criteria("userBasicInfo.employeeId").equal(Pattern.compile(searchValue, Pattern.CASE_INSENSITIVE)),
                q.criteria("userBasicInfo.groupName").equal(groupName),
                q.criteria("userBasicInfo.entityUserType").equal("Employee")
        );
        dbUserList = q.asList();
        return dbUserList;
    }

    public Integer getNoOfEmployeeByGroupName(String groupName, String entity) {
        int numofemp = 0;
        if (entity == null || entity.trim().length() == 0 ||
                groupName == null || groupName.trim().length() == 0)
            return numofemp;

        numofemp = (int) createQuery().filter("userBasicInfo.groupName", groupName).field("userBasicInfo.entityUserType").equal("Employee").
                field("entitys").contains(entity).countAll();

        return numofemp;
    }

    public DBUser findDBUserByEmployeeID(String employeeId, String entity) {
        DBUser dbUser = null;

        if (employeeId == null || employeeId.trim().length() == 0)
            return dbUser;
        Pattern regExp = Pattern.compile(employeeId.trim(), Pattern.CASE_INSENSITIVE);
        Query<DBUser> q = createQuery().filter("userBasicInfo.employeeId", regExp)
                .field("entitys").contains(entity);

        for (DBUser tempDBUser : q.asList()) {
            DBUserBasicInfo dbUserBasicInfo = tempDBUser.getUserBasicInfo();
            if (dbUserBasicInfo != null && dbUserBasicInfo.getEmployeeId() != null &&
                    dbUserBasicInfo.getEmployeeId().trim().equalsIgnoreCase(employeeId.trim()))
                dbUser = tempDBUser;
        }
        log.info("Coming in IdentityDao dbUser is {}", dbUser);
        return dbUser;
    }

    public DBUser findByEmailID(String email) {
        DBUser dbUser = null;

        if (email == null)
            return dbUser;

        Query<DBUser> q = createQuery().field("userContactInfo.email1").equal(email).limit(1);
        dbUser = q.get();
        log.info("Coming in IdentityDao dbUser is {}", dbUser);
        return dbUser;
    }
//	public List<DBUser> findDBUserByGroup(String defaultEntity, String groupName,
//			String userStatus, String entityUserType) {
//		List<DBUser> dbUserList = new ArrayList<DBUser>();
//		if(defaultEntity == null || defaultEntity.trim().length() == 0
//			||	groupName == null || groupName.trim().length() == 0
//			||	userStatus == null || userStatus.trim().length() == 0
//			|| entityUserType == null || entityUserType.trim().length() == 0)
//			return dbUserList;
//		Query<DBUser> q = createQuery();
//		q.and(
//				q.criteria("entitys").contains(defaultEntity),
//				q.criteria("userBasicInfo.groupName").equal(groupName),
//				q.criteria("status").equal(com.healthhiway.businessobject.UserStatus.valueOf(userStatus)),
//				q.criteria("userBasicInfo.entityUserType").equal(entityUserType)
//				);
//		dbUserList = q.asList();
//		return dbUserList;
//	}


    public DBUser findDBUserByEmpID(String empId) {
        DBUser dbUser = null;
        if (empId == null || empId.trim().length() == 0)
            return dbUser;
        Pattern regExp = Pattern.compile(empId.trim(), Pattern.CASE_INSENSITIVE);
        Query<DBUser> q = createQuery().filter("userBasicInfo.employeeId", regExp);
        for (DBUser tempDBUser : q.asList()) {
            DBUserBasicInfo dbUserBasicInfo = tempDBUser.getUserBasicInfo();
            if (dbUserBasicInfo != null && dbUserBasicInfo.getEmployeeId() != null &&
                    dbUserBasicInfo.getEmployeeId().trim().equalsIgnoreCase(empId.trim()))
                dbUser = tempDBUser;
        }
        log.info("Coming in IdentityDao dbUser is {}", dbUser);
        return dbUser;
    }
}