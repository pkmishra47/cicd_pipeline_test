package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBBillDownload;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BillDownloadDao extends BasicDAO<DBBillDownload, ObjectId> {
	private static Logger log = LoggerFactory.getLogger(BillDownloadDao.class);

	public BillDownloadDao(MongoClient mongoClient) {
		super(DBBillDownload.class, new MongoDBUtil(mongoClient).getDb());
	}
	
	public DBBillDownload getUserDetails(String id) {
		DBBillDownload dbBillDownload = null;
		
		if(id == null)
			return dbBillDownload;
		
		Query<DBBillDownload> q = createQuery().field("downloadId").equal(id).limit(1);
		List<DBBillDownload> dbUserList = q.asList();
		if(dbUserList.size() > 0)
			dbBillDownload = dbUserList.get(0);
		
		return dbBillDownload;
	}
}