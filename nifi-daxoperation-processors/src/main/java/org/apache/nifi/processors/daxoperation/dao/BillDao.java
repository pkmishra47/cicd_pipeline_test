package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBBill;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import java.util.List;

public class BillDao extends BasicDAO<DBBill, ObjectId>
{
//	private static BillDao instance = new BillDao();
	public BillDao(MongoClient mongoClient)
	{
		super(DBBill.class, new MongoDBUtil(mongoClient).getDb());
	}

//	public static BillDao INSTANCE() {
//		return instance;
//	}

	public DBBill findBillsByBillno(String bill_no) {
		DBBill dbBill = null;

		Query<DBBill> q = createQuery().field("bill_no").equal(bill_no).limit(1);
		List<DBBill> dbBillList = q.asList();
		if(dbBillList.size() > 0) {
			dbBill =  dbBillList.get(0);
		}
		return dbBill;
	}
}
