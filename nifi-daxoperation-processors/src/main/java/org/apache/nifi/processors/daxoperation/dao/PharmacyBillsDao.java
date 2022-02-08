package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBPharmacyBills;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import java.util.List;

public class PharmacyBillsDao extends BasicDAO<DBPharmacyBills, ObjectId> {
//	private static PharmacyBillsDao instance = new PharmacyBillsDao();
	
	public PharmacyBillsDao(MongoClient mongoClient) {
		super(DBPharmacyBills.class, new MongoDBUtil( mongoClient).getDb());
	}
	
//	public static PharmacyBillsDao INSTANCE() {
//		return instance;
//	}
	
	
	public DBPharmacyBills findBillsByBillIdAndLocationId(String bill_no, String site_id) {
		DBPharmacyBills dbPharmacyBill = null;

		Query<DBPharmacyBills> q = createQuery().field("bill_no").equal(bill_no).field("site_id").equal(site_id).limit(1);
		List<DBPharmacyBills> dbPharmacyBillList = q.asList();
		if(dbPharmacyBillList.size() > 0) {
			dbPharmacyBill =  dbPharmacyBillList.get(0);
		}	
		return dbPharmacyBill;		
	}
}