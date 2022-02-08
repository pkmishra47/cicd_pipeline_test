package org.apache.nifi.processors.daxoperation.dao;

import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBsms;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;


public class SMSDao extends BasicDAO<DBsms, ObjectId>
{
	public SMSDao(MongoClient mongoClient) {
		super(DBsms.class, new MongoDBUtil(mongoClient).getDb());
	}
	
	public DBsms findAccessCodeForActivation(String uhid,String mobileNumber,String activationCode) 
	{
		Query<DBsms> query = createQuery().field("uhid").equal(uhid)
				.field("mobileNumber").equal(mobileNumber).field("accessCode").equal(activationCode)
				.field("accessCodeStatus").equal("ACTIVE").field("smsPurpose").equal("ACTIVATION");
		
		DBsms dbSMS = null;
		if ( query.asList().size() != 0)
			dbSMS = query.asList().get(0);
		
		return dbSMS;
	}
	

	public List<DBsms> findActiveAccessCodesForActivation(String uhid, String mobileNumber)
	{
		Query<DBsms> query = createQuery().field("uhid").equal(uhid)
				.field("mobileNumber").equal(mobileNumber)
				.field("accessCodeStatus").equal("ACTIVE").field("smsPurpose").equal("ACTIVATION");
		
		return query.asList();
	}
	
	public List<DBsms> findActiveAccessCodesForOTP(String id,String mobileNumber)
	{
		Query<DBsms> query = createQuery().field("downloadId").equal(id)
				.field("mobileNumber").equal(mobileNumber)
				.field("accessCodeStatus").equal("ACTIVE").field("smsPurpose").equal("OTP");
		
		return query.asList();
	}
	
	public List<DBsms> findActiveAccessCodesForReset(String uhid,String mobileNumber)
	{
		Query<DBsms> query = createQuery().field("uhid").equal(uhid)
				.field("mobileNumber").equal(mobileNumber)
				.field("accessCodeStatus").equal("ACTIVE").field("smsPurpose").equal("PASSWORD-RESET");
		
		return query.asList();
	}
	
	public DBsms findAccessCodeByUserIdAndResetCodeForReset(ObjectId userObjectId,String resetCode)
	{
		Query<DBsms> query = createQuery().disableValidation().filter("dbUser.$id", userObjectId)
				.field("accessCode").equal(resetCode)
				.field("accessCodeStatus").equal("ACTIVE").field("smsPurpose").equal("PASSWORD-RESET");
		
		DBsms dbSMS = null;
		if ( query.asList().size() != 0)
			dbSMS = query.asList().get(0);
		
		return dbSMS;
	}
	

	public List<DBsms> findActiveAccessCodesForDelete(ObjectId userObjectId,String mobileNumber)
	{
		Query<DBsms> query = createQuery().disableValidation().filter("dbUser.$id", userObjectId)
				.field("mobileNumber").equal(mobileNumber)
				.field("accessCodeStatus").equal("ACTIVE").field("smsPurpose").equal("DELETE");
		
		return query.asList();
	}
	
	public DBsms findDBsmsById(ObjectId dbSMSId) 
	{
		return get(dbSMSId);
	}
	
	public DBsms findAccessCodeByUserIdAndDeleteCodeForDelete(ObjectId userObjectId,String deleteConfirmCode)
	{
		Query<DBsms> query = createQuery().disableValidation().filter("dbUser.$id", userObjectId)
				.field("accessCode").equal(deleteConfirmCode)
				.field("accessCodeStatus").equal("ACTIVE").field("smsPurpose").equal("DELETE");
		
		DBsms dbSMS = null;
		if ( query.asList().size() != 0)
			dbSMS = query.asList().get(0);
		
		return dbSMS;
	}
	
	public List<DBsms> findActiveAccessCodesForLinking(String uhid,String mobileNumber)
	{
		Query<DBsms> query = createQuery().field("uhid").equal(uhid)
				.field("mobileNumber").equal(mobileNumber)
				.field("accessCodeStatus").equal("ACTIVE").field("smsPurpose").equal("LINKING");
		
		return query.asList();
	}
	
	public DBsms findAccessCodeForLinking(String siteKey,String uhid,String mobileNumber,String linkingActivationCode) 
	{
		Query<DBsms> query = createQuery().field("uhid").equal(uhid)
				.field("mobileNumber").equal(mobileNumber).field("accessCode").equal(linkingActivationCode)
				.field("accessCodeStatus").equal("ACTIVE").field("smsPurpose").equal("LINKING")
				.field("siteKey").equal(siteKey);
		
		DBsms dbSMS = null;
		if ( query.asList().size() != 0)
			dbSMS = query.asList().get(0);
		
		return dbSMS;
	}
	
	public List<DBsms> findLabTestIntimationSMSByuserObjectId(Object userObjectId,long date)
	{
		Query<DBsms> query = createQuery().disableValidation().filter("dbUser.$id", userObjectId)
				.field("smsPurpose").equal("LABTEST-INTIMATION")
				.field("sendAt").greaterThanOrEq(new Date(date));
		
		return query.asList();
	}
	
	public List<DBsms> findDBSMSToIntimateByDateRange(long date,String intimationType,int noOfRecordToBeFetch)
	{
		Query<DBsms> query = createQuery().field("sendResult").equal(null)
				.field("smsPurpose").equal(intimationType)
				.field("sendAt").greaterThanOrEq(new Date(date)).limit(noOfRecordToBeFetch);
		
		return query.asList();
	}

	public List<DBsms> findDBSMSToIntimateByDateRangeV2(long date,String intimationType,int noOfRecordToBeFetch)
	{
		Query<DBsms> query = createQuery().field("smsPurpose").equal(intimationType)
				.field("sendAt").greaterThanOrEq(new Date(date)).limit(noOfRecordToBeFetch);

		return query.asList();
	}
	
	public List<DBsms> findActiveAccessCodesForRecovery(String uhid,String mobileNumber)
	{
		Query<DBsms> query = createQuery().field("uhid").equal(uhid)
				.field("mobileNumber").equal(mobileNumber)
				.field("accessCodeStatus").equal("ACTIVE").field("smsPurpose").equal("RECOVERY");
		
		return query.asList();
	}
	
	public DBsms findAccessCodeForRecovery(String uhid,String mobileNumber,String recoveryCode) 
	{
		Query<DBsms> query = createQuery().field("uhid").equal(uhid)
				.field("mobileNumber").equal(mobileNumber).field("accessCode").equal(recoveryCode)
				.field("accessCodeStatus").equal("ACTIVE").field("smsPurpose").equal("RECOVERY");
		
		DBsms dbSMS = null;
		if ( query.asList().size() != 0)
			dbSMS = query.asList().get(0);
		
		return dbSMS;
	}
}
