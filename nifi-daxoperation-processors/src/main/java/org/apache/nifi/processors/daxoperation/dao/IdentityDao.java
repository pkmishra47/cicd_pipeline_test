package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBIdentity;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IdentityDao extends BasicDAO<DBIdentity, ObjectId> {
    public IdentityDao(MongoClient mongoClient) {
        super(DBIdentity.class, new MongoDBUtil(mongoClient).getDb());
        ensureIndexes();
    }

    public List<DBIdentity> getUsers() {
        return find().asList();
    }

    public DBIdentity getIdentity(String userId) {
        DBIdentity dbIdentity = null;

        if (userId == null)
            return dbIdentity;

        Query<DBIdentity> q = createQuery().field("userId").equal(userId)
                .limit(1);
        List<DBIdentity> dbUserList = q.asList();
        if (dbUserList.size() > 0)
            dbIdentity = dbUserList.get(0);

        return dbIdentity;
    }


    public DBIdentity getIdentityByGoogleId(String googleId) {
        DBIdentity dbIdentity = null;

        if (googleId == null)
            return dbIdentity;

        Query<DBIdentity> q = createQuery().field("googleId").equal(googleId).limit(1);
        List<DBIdentity> dbUserList = q.asList();
        if (dbUserList.size() > 0)
            dbIdentity = dbUserList.get(0);

        return dbIdentity;
    }

    public DBIdentity getIdentity(String userId, String password) {
        DBIdentity dbIdentity = null;

        if (userId == null || password == null)
            return dbIdentity;

        userId = userId.toLowerCase();
        Query<DBIdentity> q = createQuery().field("userId").equal(userId).limit(1);
        List<DBIdentity> dbUserList = q.asList();
        if (dbUserList.size() > 0) {
            DBIdentity tempDBIdentity = dbUserList.get(0);
            DBUser.UserStatus userStatus = tempDBIdentity.getDbUser().getStatus();
			/*if (userStatus.equals(UserStatus.ACTIVE) && BCrypt.checkpw(password, tempDBIdentity.getPassword() ))
				dbIdentity = tempDBIdentity;
			else if (userStatus.equals(UserStatus.NOT_ACTIVATED ) && password.equals(configuration.getMasterPassword()))
				dbIdentity = tempDBIdentity;
			else
				dbIdentity = null;*/

            /**if(password.equals(configuration.getMasterPassword()))
             dbIdentity = tempDBIdentity;
             else if ((userStatus.equals(DBUser.UserStatus.ACTIVE)|| userStatus.equals(DBUser.UserStatus.CARE_GIVER)) && BCrypt.checkpw(password, tempDBIdentity.getPassword()))
             dbIdentity = tempDBIdentity;
             else
             dbIdentity = null;
             */

        }
        return dbIdentity;
    }

    public DBUser findByUserID(String userId) {
        DBUser dbUser = null;

        if (userId == null)
            return dbUser;

        userId = userId.toLowerCase();
        Query<DBIdentity> q = createQuery().field("userId").equal(userId).limit(1);
        List<DBIdentity> dbIdentityList = q.asList();
        if (dbIdentityList.size() > 0) {
            dbUser = dbIdentityList.get(0).getDbUser();
        }

        return dbUser;
    }


    public DBUser findByUhid(String uhid, String siteKey) {
        DBUser dbUser = null;

        if (uhid == null)
            return dbUser;

        uhid = uhid.toUpperCase();
        Query<DBIdentity> q = createQuery().field("uhid").equal(uhid).field("siteKey").equal(siteKey).limit(1);
        List<DBIdentity> dbIdentityList = q.asList();
        if (dbIdentityList.size() > 0) {
            dbUser = dbIdentityList.get(0).getDbUser();
        }

        return dbUser;
    }

    public DBIdentity findByUhidSitekey(String uhid, String siteKey) {
        DBIdentity dbIdentity = null;

        if (uhid == null || siteKey == null)
            return dbIdentity;

        uhid = uhid.toUpperCase();
        Query<DBIdentity> q = createQuery().field("uhid").equal(uhid).field("siteKey").equal(siteKey);
        dbIdentity = q.get();

        return dbIdentity;
    }

    public DBIdentity findByMobileNumberAndUhid(String mobileNumber, String uhid) {
        DBIdentity dbIdentity = null;

        if (mobileNumber == null || uhid == null)
            return dbIdentity;

        uhid = uhid.toUpperCase();
        Query<DBIdentity> q = createQuery().field("uhid").equal(uhid).field("mobileNumber").equal(mobileNumber);
        List<DBIdentity> dbIdentityList = q.asList();
        if (dbIdentityList.size() == 1) {
            dbIdentity = dbIdentityList.get(0);
        }

        return dbIdentity;
    }

    public DBUser findUserByMobileNumberAndUhid(String mobileNumber, String uhid) {
        DBIdentity dbIdentity = null;
        DBUser dbUser = null;

        if (mobileNumber == null || uhid == null)
            return dbUser;

        uhid = uhid.toUpperCase();
        Query<DBIdentity> q = createQuery().field("uhid").equal(uhid).field("mobileNumber").equal(mobileNumber);
        List<DBIdentity> dbIdentityList = q.asList();
        if (dbIdentityList.size() == 1) {
            dbUser = dbIdentityList.get(0).getDbUser();
        }

        return dbUser;
    }


    public DBIdentity findDBIdentityByMobileNumberAndUhid(String uhid, String mobile) {
        DBIdentity dbIdentity = null;

        if (mobile == null || uhid == null)
            return dbIdentity;

        uhid = uhid.toUpperCase();
        Query<DBIdentity> q = createQuery().field("uhid").equal(uhid).field("mobileNumber").equal(mobile);
        List<DBIdentity> dbIdentityList = q.asList();

        return dbIdentityList.get(0);
    }

    public DBIdentity findDBIdentityByMobileNumberAndHashedUhid(String uhid, String mobile) {
        List<DBIdentity> dbIdentityList = findDBIdentityByMobileNumber(mobile);

        if (uhid != null) {
            for (DBIdentity iden : dbIdentityList) {
                if (iden.getUhid().replaceAll(".(?=.{2})", "*").equals(uhid)) {
                    return iden;

                }
            }
        }
        return null;
    }

    public DBIdentity findDBIdentityByEmailAndUhid(String uhid, String email) {
        DBIdentity dbIdentity = null;

        if (email == null || uhid == null)
            return dbIdentity;
        uhid = uhid.toUpperCase();
        Query<DBIdentity> q = createQuery().field("uhid").equal(uhid).field("email").equal(email).limit(1);
        List<DBIdentity> dbIdentityList = q.asList();

        return dbIdentityList.get(0);
    }

    public List<DBIdentity> getDBIdentityListBySite(String siteKey) {
        Query<DBIdentity> q = createQuery().field("siteKey").equal(siteKey);
        return q.asList();
    }

    public List<ObjectId> getDBUserObjectIdBySite(String siteKey) {
        List<ObjectId> userObjectIdList = new ArrayList<ObjectId>();
        for (DBIdentity dBIdentity : getDBIdentityListBySite(siteKey))
            userObjectIdList.add(dBIdentity.getDbUser().getId());
        return userObjectIdList;
    }

    public List<String> getUhids(List<ObjectId> dbUserObjIds, String siteKey) {
        List<String> uhidList = new ArrayList<String>();
        Query<DBIdentity> query = createQuery();
        query.filter("siteKey", siteKey);
        query.field("dbUser.$id").in(dbUserObjIds);
        for (DBIdentity dbIdentity : query)
            uhidList.add(dbIdentity.getUhid());

        return uhidList;
    }

    public DBUser findByEmail(String email) {
        DBUser dbUser = null;

        if (email == null)
            return dbUser;

        Query<DBIdentity> q = createQuery().field("dbUser.userContactInfo.email1").equal(email).limit(1);
        List<DBIdentity> dbIdentityList = q.asList();

        if (dbIdentityList.size() > 0)
            dbUser = dbIdentityList.get(0).getDbUser();

        return dbUser;
    }

    public List<DBIdentity> findDBIdentityByEmailAndSiteKey(String eMail, List<String> siteKeyList) {
        List<DBIdentity> dbIdentityList = new ArrayList<DBIdentity>();

        if (eMail == null)
            return dbIdentityList;

        Query<DBIdentity> q = createQuery().field("dbUser.userContactInfo.email1").equal(eMail).field("siteKey").in(siteKeyList);
        dbIdentityList = q.asList();
        return dbIdentityList;
    }

    public List<DBIdentity> findDBIdentityByUserObjectId(ObjectId userObjectId) {
        List<DBIdentity> dbIdentityList = new ArrayList<DBIdentity>();

        if (userObjectId == null)
            return dbIdentityList;

		/*DBRef dbRef = ds.createRef(DBUser.class, userObjectId);

		Query<DBIdentity> q = ds.createQuery(DBIdentity.class).filter("dbUser", dbRef);*/

        Query<DBIdentity> q = createQuery().disableValidation().filter("dbUser.$id", userObjectId);
        dbIdentityList = q.asList();

        return dbIdentityList;
    }

    public List<DBIdentity> findDBIdentityByResetPasswordLink(String resetPasswordLink) {

        List<DBIdentity> dbIdentityList = new ArrayList<DBIdentity>();

        Query<DBIdentity> q = createQuery().filter("resetPasswordLinkKey", resetPasswordLink);
        dbIdentityList = q.asList();

        return dbIdentityList;
    }

    public List<DBIdentity> findDBIdentityByResetPasswordLinkAndUserId(String resetPasswordLinkKey, String userId) {
        List<DBIdentity> dbIdentityList = new ArrayList<DBIdentity>();

        Query<DBIdentity> q = createQuery().filter("resetPasswordLinkKey", resetPasswordLinkKey).filter("userId", userId);
        dbIdentityList = q.asList();

        return dbIdentityList;
    }

    public List<DBIdentity> findDBIdentityByUserObjectIdAndUhid(List<ObjectId> userObjectIdList, String uhid) {
        List<DBIdentity> dbIdentityList = new ArrayList<DBIdentity>();
        if (userObjectIdList != null && userObjectIdList.size() != 0) {
            Query<DBIdentity> query = createQuery().disableValidation().field("dbUser.$id").in(userObjectIdList).field("uhid").equal(uhid);
            dbIdentityList = query.asList();
        }
        return dbIdentityList;
    }


    public List<DBIdentity> findDBIdentityByUhid(String uhid) {
        List<DBIdentity> dbIdentityList = new ArrayList<DBIdentity>();
        uhid = uhid.toUpperCase();
        Query<DBIdentity> query = createQuery().field("uhid").equal(uhid);
        dbIdentityList = query.asList();
        return dbIdentityList;
    }

    public DBUser findDBUserByUhid(String uhid) {
        List<DBIdentity> dbIdentityList = null;
        uhid = uhid.toUpperCase();
        Query<DBIdentity> query = createQuery().field("uhid").equal(uhid);
        dbIdentityList = query.asList();
        if (dbIdentityList == null || dbIdentityList.size() < 1)
            return null;
        return dbIdentityList.get(0).getDbUser();
    }


    public List<DBIdentity> findDBIdentityByUhidList(List<String> uhids) {
        List<DBIdentity> dbIdentityList = new ArrayList<DBIdentity>();

        Query<DBIdentity> query = createQuery();

        query.field("uhid").in(uhids);
        for (DBIdentity dbIdentity : query)
            dbIdentityList.add(dbIdentity);

        return dbIdentityList;
    }


    public List<DBIdentity> findDBIdentityByMobileNumber(String mobileNumber) {
        Query<DBIdentity> query = createQuery().field("mobileNumber").equal(mobileNumber);
        return query.asList();
    }

    public List<DBIdentity> findDBIdentityByEmail(String email) {
        List<DBIdentity> dbIdentityList = null;

        if (email == null)
            return dbIdentityList;

        Query<DBIdentity> q = createQuery().field("email").equal(email);
        dbIdentityList = q.asList();


        return dbIdentityList;
    }

    public long getNoOfUsersImported(List<ObjectId> dbUserObjIds, String siteKey) {
        long startTime = System.currentTimeMillis();
//		log.info("PHR | Identity | Coming into getNoOfUsersImported | Start Time "+startTime);

        Query<DBIdentity> query = createQuery();
        query.filter("siteKey", siteKey);
        query.field("dbUser.$id").in(dbUserObjIds);

        long noOfUser = query.countAll();
//		log.info("PHR | Identity | getUhidsDateOfImport | No of User "+noOfUser);

//		log.info("PHR | Identity | Exiting from getNoOfUsersImported | Time Taken"+(System.currentTimeMillis()-startTime));
        return noOfUser;
    }

    public int getNoOfUsersRegistered(long fromDate, long toDate, String siteKey) {
        long startTime = System.currentTimeMillis();
//		log.info("PHR | Identity | Coming into getNoOfUsersRegistered | Start Time "+startTime);

        Query<DBIdentity> dbIdentityQuery = createQuery();
        dbIdentityQuery.field("registrationDate").greaterThanOrEq(new Date(fromDate));
        dbIdentityQuery.field("registrationDate").lessThanOrEq(new Date(toDate));
        dbIdentityQuery.field("siteKey").equal(siteKey);
        int noOfUser = dbIdentityQuery.asList().size();
//		log.info("PHR | Identity | getNoOfUsersRegistered | No of User "+noOfUser);

//		log.info("PHR | Identity | Exiting from getNoOfUsersRegistered | Time Taken"+(System.currentTimeMillis()-startTime));
        return noOfUser;
    }

    public List<String> getUhidsByRegisteredDate(long fromDate, long toDate, String siteKey) {
        long startTime = System.currentTimeMillis();
//		log.info("PHR | Identity | Coming into getUhidsByRegisteredDate | Start Time "+startTime);

        List<String> uhidList = new ArrayList<String>();

        Query<DBIdentity> dbIdentityQuery = createQuery();
        dbIdentityQuery.field("registrationDate").greaterThanOrEq(new Date(fromDate));
        dbIdentityQuery.field("registrationDate").lessThanOrEq(new Date(toDate));
        dbIdentityQuery.field("siteKey").equal(siteKey);

        for (DBIdentity dbIdentity : dbIdentityQuery.asList())
            uhidList.add(dbIdentity.getUhid());

//		log.info("PHR | Identity | Exiting from getUhidsByRegisteredDate | Time Taken"+(System.currentTimeMillis()-startTime));
        return uhidList;
    }

//	public UserIdentityData getUserIdentityData(DBIdentity dbIdentity){
//		UserIdentityData userIdentityData = new UserIdentityData();
//		if (dbIdentity != null){
//			DBUser dbUser = dbIdentity.getDbUser();
//			userIdentityData.setFirstName(dbUser.getFirstName());
//			userIdentityData.setLastName(dbUser.getLastName());
//			userIdentityData.setMiddleName(dbUser.getMiddleName());
//			userIdentityData.setMobileNumber(dbUser.getMobileNumber());
//			userIdentityData.setDefaultEntity(dbUser.getDefaultEntity());
//			userIdentityData.setOtpAttempts(dbIdentity.getOtpAttempts());
//			userIdentityData.setGoogleId(dbIdentity.getGoogleId());
//			if ( dbUser.getUserContactInfo() != null )
//				userIdentityData.setEmail(dbUser.getUserContactInfo().getEmail1());
//
//			userIdentityData.setRoles(dbUser.getRoles());
//			userIdentityData.setEntitys(dbUser.getEntitys());
//			userIdentityData.setUserObjectId(String.valueOf(dbUser.getId()));
//			userIdentityData.setUserStatus(String.valueOf(dbUser.getStatus()));
//			userIdentityData.setUserId(dbIdentity.getUserId());
//			userIdentityData.setPassword(dbIdentity.getPassword());
//			userIdentityData.setIsIndian(dbIdentity.getIsIndian());
//			userIdentityData.setUserUhid(dbIdentity.getUhid());
//			if ( dbIdentity.getLastLogOn() != null)
//				userIdentityData.setLastLogonDate(dbIdentity.getLastLogOn().getTime());
//			if ( dbIdentity.getBlockedTime() != null)
//				userIdentityData.setBlockedTime(dbIdentity.getBlockedTime().getTime());
//			if (dbIdentity.getRegistrationDate() != null)
//				userIdentityData.setRegistrationDate(dbIdentity.getRegistrationDate().getTime());
//			if (dbUser.getDateImported() != null )
//				userIdentityData.setImportedDate(dbUser.getDateImported().getTime());
//
//		}
//		return userIdentityData;
//	}

    public DBUser findBySensorID(String sensorId, String field) {
        DBUser dbUser = null;

        if (sensorId == null)
            return dbUser;

        Query<DBIdentity> q = createQuery().field(field).equal(sensorId).limit(1);
        List<DBIdentity> dbIdentityList = q.asList();
        if (dbIdentityList.size() > 0) {
            dbUser = dbIdentityList.get(0).getDbUser();
        }

        return dbUser;
    }
}