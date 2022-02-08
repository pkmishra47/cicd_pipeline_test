package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBPharmacyRecommendation;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import java.util.List;

public class PharmacyRecommendationsDao extends BasicDAO<DBPharmacyRecommendation, ObjectId> {
	
	public PharmacyRecommendationsDao(MongoClient mongoClient) {
		super(DBPharmacyRecommendation.class, new MongoDBUtil( mongoClient).getDb());
	}
	
	
	public DBPharmacyRecommendation findRecommendationsByMobileno(String mobileNo) {
		DBPharmacyRecommendation dbPharmacyRecommendation = null;

		Query<DBPharmacyRecommendation> q = createQuery().field("mobileNumber").equal(mobileNo).limit(1);
		List<DBPharmacyRecommendation> dbPharmacyRecommendationList = q.asList();
		if(dbPharmacyRecommendationList.size() > 0) {
			dbPharmacyRecommendation = dbPharmacyRecommendationList.get(0);
		}	
		return dbPharmacyRecommendation;		
	}
}