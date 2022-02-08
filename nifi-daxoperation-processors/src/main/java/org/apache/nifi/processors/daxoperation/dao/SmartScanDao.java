package org.apache.nifi.processors.daxoperation.dao;

import java.util.List;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBSmartScan;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

public class SmartScanDao extends BasicDAO<DBSmartScan, ObjectId> {
	
	public SmartScanDao(MongoClient mongoClient) {
		super(DBSmartScan.class, new MongoDBUtil( mongoClient).getDb());
	}
	
	
	public DBSmartScan getById(String objectIdString) {
		DBSmartScan dbsmartscan = null;

		Query<DBSmartScan> q = createQuery().field("_id").equal(new ObjectId(objectIdString)).limit(1);
		List<DBSmartScan> dbsmartscanList = q.asList();
		if (dbsmartscanList.size() > 0) {
			dbsmartscan = dbsmartscanList.get(0);
		}
		return dbsmartscan;
	}
}