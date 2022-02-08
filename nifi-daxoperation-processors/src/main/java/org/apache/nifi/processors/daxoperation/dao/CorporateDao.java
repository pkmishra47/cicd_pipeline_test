package org.apache.nifi.processors.daxoperation.dao;

import java.util.List;

import com.mongodb.MongoClient;

import org.apache.nifi.processors.daxoperation.dbo.DBCorporate;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.stereotype.Component;

@Component("corporateDao")
public class CorporateDao extends BasicDAO<DBCorporate, ObjectId>{

	public CorporateDao(MongoClient mongoClient) {
		super(DBCorporate.class, new MongoDBUtil(mongoClient).getDb());
	}

	public DBCorporate getCorporate(String name) {
		DBCorporate dbCorporate = null;
		 if(name != null)
			 dbCorporate =  createQuery().filter("name", name).get();
		 return dbCorporate;
	}
	public List<DBCorporate> getAll(){
		return find().asList();
	}
	
	// public static DBCorporate newDBCorporate(Corporate corporate) {
	// 	DBCorporate dbCorporate = new DBCorporate();
		
	// 	if(corporate == null)
	// 		return dbCorporate;
	// 	dbCorporate.setName(corporate.getName());
	// 	dbCorporate.setDisplayName(corporate.getDisplayName());
	// 	dbCorporate.setMobileNumber(corporate.getMobileNummber());
	// 	dbCorporate.setContactPerson(corporate.getContactPerson());
	// 	if(corporate.getExclusivePages() !=null){
	// 		for(LabPackage lp : corporate.getExclusivePages()){
	// 			DBLabPackage dblp =new DBLabPackage();
	// 			dblp.setPackageId(lp.getPackageId());
	// 			dblp.setPackageName(lp.getPackageName());
	// 			dblp.setSiteKey(lp.getSiteKey());
	// 			dbCorporate.addExclusivePackages(dblp);
	// 		}
	// 	}	
	// 	if(corporate.getUserAlsoPackages() != null){
	// 		for(LabPackage lp : corporate.getUserAlsoPackages()){
	// 			DBLabPackage dblp =new DBLabPackage();
	// 			dblp.setPackageId(lp.getPackageId());
	// 			dblp.setPackageName(lp.getPackageName());
	// 			dblp.setSiteKey(lp.getSiteKey());
	// 			dbCorporate.addUseralsoPackages(dblp);
	// 		}
	// 	}	
	// 	return dbCorporate;
	// }
	// public static Corporate newCorporate(DBCorporate dbCorporate) {
	// 	Corporate corporate = new Corporate();
		
	// 	if(dbCorporate == null)
	// 		return corporate;
		
	// 	corporate.setId(dbCorporate.getId());
	// 	corporate.setName(dbCorporate.getName());
	// 	corporate.setDisplayName(dbCorporate.getDisplayName());
	// 	corporate.setContactPerson(dbCorporate.getContactPerson());
	// 	corporate.setMobileNumber(dbCorporate.getMobileNumber());
		
	// 	for (DBLabPackage dblp : dbCorporate.getExclusivePackages()) {
	// 		LabPackage lp = new LabPackage();
	// 		lp.setPackageName(dblp.getPackageName());
	// 		lp.setPackageId(dblp.getPackageId());
	// 		lp.setSiteKey(dblp.getSiteKey());
	// 		corporate.addToExclusivePackages(lp);
	// 	}
	// 	for (DBLabPackage dblp : dbCorporate.getUseralsoPackages()) {
	// 		LabPackage lp = new LabPackage();
	// 		lp.setPackageName(dblp.getPackageName());
	// 		lp.setPackageId(dblp.getPackageId());
	// 		lp.setSiteKey(dblp.getSiteKey());
	// 		corporate.addToUseralsoPackages(lp);
	// 	}
		
		
	// 	return corporate;
		
	// }
	
}