package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.bo.Attachment;
import org.apache.nifi.processors.daxoperation.bo.HealthCheck;
import org.apache.nifi.processors.daxoperation.dbo.DBAttachement;
import org.apache.nifi.processors.daxoperation.dbo.DBHealthCheck;
import org.apache.nifi.processors.daxoperation.utils.DBUtil;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;

import java.util.Date;
import java.util.List;

public class HealthCheckDao extends BasicDAO<DBHealthCheck, ObjectId>
{
	
	public HealthCheckDao(MongoClient mongoClient)
	{
		super(DBHealthCheck.class, new MongoDBUtil( mongoClient).getDb());
	}

	public List<DBHealthCheck> getHealthCheck() 
	{
		return find().asList();
	}
		
	
	public static DBHealthCheck newDBHealthCheck(HealthCheck healthCheck) {
		
		DBHealthCheck dbHealthCheck = new DBHealthCheck();
		
		if(healthCheck == null)
			return dbHealthCheck;
		
		dbHealthCheck.setHealthCheckName(healthCheck.getHealthCheckName());
		Date healthCheckDate = new Date(healthCheck.getHealthCheckDate().getTime());
		Date appointmentDate = new Date(healthCheck.getAppointmentDate().getTime());
		dbHealthCheck.setHealthCheckDate(healthCheckDate);
	//	dbHealthCheck.setAppointmentDate(appointmentDate);
		dbHealthCheck.setClinicName(healthCheck.getClinicName());
		dbHealthCheck.setHealthCheckSummary(healthCheck.getHealthCheckSummary());
		dbHealthCheck.setSource(healthCheck.getSource());
		dbHealthCheck.setHealthCheckType(healthCheck.getHealthCheckType());
		dbHealthCheck.setFollowupDate(new Date(healthCheck.getFollowupDate().getTime()));
		dbHealthCheck.setKeywords(healthCheck.getKeywords());
		
		List<Attachment> healthCheckFilesList =healthCheck.getHealthCheckFiles();

		if(healthCheckFilesList != null)
			dbHealthCheck.setHealthCheckFilesList(DBUtil.writeDBAttachement(healthCheckFilesList));
	
		return dbHealthCheck;
	}
	
	public static HealthCheck newHealthCheck(DBHealthCheck dbHealthCheck) {
		HealthCheck healthCheck = new HealthCheck();
		
		if(dbHealthCheck == null)
			return healthCheck;
		
		healthCheck.setId(dbHealthCheck.getId());
		healthCheck.setHealthCheckName(dbHealthCheck.getHealthCheckName());
		healthCheck.setHealthCheckSummary(dbHealthCheck.getHealthCheckSummary());
		healthCheck.setSource(dbHealthCheck.getSource());
		healthCheck.setHealthCheckType(dbHealthCheck.getHealthCheckType());
		healthCheck.setKeywords(dbHealthCheck.getKeywords());
		healthCheck.setClinicName(dbHealthCheck.getClinicName());
		
		if(dbHealthCheck.getHealthCheckDate() != null)
			healthCheck.setHealthCheckDate(dbHealthCheck.getHealthCheckDate());
		
//		if(dbHealthCheck.getAppointmentDate() != null)
//			healthCheck.setAppointmentDate(dbHealthCheck.getAppointmentDate().getTime());
			
		if(dbHealthCheck.getFollowupDate() != null)
         	healthCheck.setFollowupDate(dbHealthCheck.getFollowupDate());
		
		List<DBAttachement> dbHealthCheckFiles = dbHealthCheck.getHealthCheckFilesList();
		if(dbHealthCheckFiles != null)
			healthCheck.setHealthCheckFiles(DBUtil.readDBAttachement(dbHealthCheckFiles));
	
		return healthCheck;
	}
}
