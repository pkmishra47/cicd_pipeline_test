package org.apache.nifi.processors.daxoperation.dao;

import java.util.List;

import com.mongodb.MongoClient;

import org.apache.nifi.processors.daxoperation.dbo.DBCarePersonaOCR;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

public class CarePersonaOCRDao extends BasicDAO<DBCarePersonaOCR, ObjectId> {
	
	public CarePersonaOCRDao(MongoClient mongoClient) {
		super(DBCarePersonaOCR.class, new MongoDBUtil(mongoClient).getDb());
	}
	
	
	public DBCarePersonaOCR getById(String objectIdString) {
		DBCarePersonaOCR dbCarePersonaOCR = null;

		Query<DBCarePersonaOCR> q = createQuery().field("_id").equal(new ObjectId(objectIdString)).limit(1);
		List<DBCarePersonaOCR> dbCarePersonaOCRList = q.asList();
		if (dbCarePersonaOCRList.size() > 0) {
			dbCarePersonaOCR = dbCarePersonaOCRList.get(0);
		}
		return dbCarePersonaOCR;
	}
}