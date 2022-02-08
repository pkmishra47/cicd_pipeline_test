package org.apache.nifi.processors.daxoperation.dao;

import java.util.List;

import com.mongodb.MongoClient;

import org.apache.nifi.processors.daxoperation.dbo.DBLabResultDownload;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Component;

@Component("labResultDownloadDao")

public class LabResultDownloadDao extends BasicDAO<DBLabResultDownload, ObjectId> {
	
	public LabResultDownloadDao(MongoClient mongoClient) {
		super(DBLabResultDownload.class, new MongoDBUtil(mongoClient).getDb());
	}
	
	public DBLabResultDownload getUserDetails(String id) {
		DBLabResultDownload dbLabResultDownload = null;
		
		if(id == null)
			return dbLabResultDownload;
		
		Query<DBLabResultDownload> q = createQuery().field("downloadId").equal(id).limit(1);
		List<DBLabResultDownload> dbUserList = q.asList();
		if(dbUserList.size() > 0)
			dbLabResultDownload = dbUserList.get(0);
		
		return dbLabResultDownload;
	}
}