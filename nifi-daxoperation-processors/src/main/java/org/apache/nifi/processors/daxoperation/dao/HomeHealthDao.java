package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;

import org.apache.nifi.processors.daxoperation.bo.HomeHealth;
import org.apache.nifi.processors.daxoperation.dbo.DBHomeHealth;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.stereotype.Component;

@Component("homeHealthDao")
public class HomeHealthDao extends BasicDAO<DBHomeHealth, ObjectId> {

	public HomeHealthDao(MongoClient mongoClient) {
		super(DBHomeHealth.class, new MongoDBUtil(mongoClient).getDb());
	}
	
	public static HomeHealth newHomeHealth(DBHomeHealth dbHomeHealth) {
		HomeHealth homeHealth = new HomeHealth();
		if(dbHomeHealth == null)
			return homeHealth;
		
		homeHealth.setId(dbHomeHealth.getId());
		homeHealth.setTemperature(dbHomeHealth.getTemperature());
		homeHealth.setWeight(dbHomeHealth.getWeight());
		homeHealth.setPulse(dbHomeHealth.getPulse());
		homeHealth.setBloodPressureSystolic(dbHomeHealth.getBloodPressureSystolic());
		homeHealth.setBloodPressureDiastolic(dbHomeHealth.getBloodPressureDiastolic());
		homeHealth.setRespiration(dbHomeHealth.getRespiration());
		homeHealth.setOxygenUse(dbHomeHealth.getOxygenUse());
		homeHealth.setOxygenDevice(dbHomeHealth.getOxygenDevice());
		homeHealth.setOxygenValue(dbHomeHealth.getOxygenValue());
		homeHealth.setPainScore(dbHomeHealth.getPainScore());
		homeHealth.setRemarks(dbHomeHealth.getRemarks());
		homeHealth.setSpO2(dbHomeHealth.getSpO2());
		homeHealth.setCheckupDate(dbHomeHealth.getCheckupDate());
	
		return homeHealth;
	}
}