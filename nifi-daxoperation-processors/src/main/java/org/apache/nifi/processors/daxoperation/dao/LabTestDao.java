package org.apache.nifi.processors.daxoperation.dao;

// import java.util.ArrayList;
// import java.util.Calendar;
// import java.util.Date;
import java.util.List;

import com.mongodb.MongoClient;

// import org.apache.nifi.processors.daxoperation.bo.Attachment;
// import org.apache.nifi.processors.daxoperation.bo.LabResult;
// import org.apache.nifi.processors.daxoperation.bo.LabTest;
// import org.apache.nifi.processors.daxoperation.bo.Test;
// import org.apache.nifi.processors.daxoperation.dbo.DBAttachement;
// import org.apache.nifi.processors.daxoperation.dbo.DBLabResult;
import org.apache.nifi.processors.daxoperation.dbo.DBLabTest;
// import org.apache.nifi.processors.daxoperation.dbo.DBTest;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("labTestDao")
public class LabTestDao extends BasicDAO<DBLabTest, ObjectId> {
	
	public LabTestDao(MongoClient mongoClient) {
		super(DBLabTest.class, new MongoDBUtil(mongoClient).getDb());
	}
	
	public DBLabTest getTestById(String orderId, String billId, String siteKey) {
		if(orderId == null || billId == null || siteKey == null)
			return null;

		Query<DBLabTest> q = createQuery();
		q.and(
		        q.criteria("orderId").equal(orderId),
		        q.criteria("billId").equal(billId),
		        q.criteria("siteKey").equal(siteKey)
		);
		return q.get();
	}
	
	
	public List<DBLabTest> getTestByObjectIds(List<ObjectId> oids) {
		if(oids==null)
			return null;
		if(oids.size()==0)
			return null;

		Query<DBLabTest> q = createQuery();
		q.and(
		        q.criteria("_id").in(oids)		        
		);
		return q.asList();
	}

	// public static DBLabTest newDBLabTest(LabTest labTest) {
	// 	DBLabTest dbLabTest = new DBLabTest();

	// 	if (labTest == null)
	// 		return dbLabTest;

	// 	dbLabTest.setReferredBy(labTest.getReferredBy());
	// 	dbLabTest.setTestDate(new Date(labTest.getTestDate()));
	// 	dbLabTest.setUhid(labTest.getUhid());
	// 	dbLabTest.setTag(labTest.getTag());
	// 	dbLabTest.setConsultId(labTest.getConsultId());
	// 	dbLabTest.setDateImported(new Date(labTest.getDateImported()));
	// 	dbLabTest.setSiteKey(labTest.getSiteKey());
	// 	List<DBTest> dbTests = new ArrayList<DBTest>();
	// 	for(Test test : labTest.getTests()){
	// 		DBTest dbTest = new DBTest();
	// 		dbTest.setTestName(test.getTestName());
	// 		dbTest.setSource(test.getSource());
	// 		dbTest.setTestid(test.getTestId());
	// 		dbTest.setObservation(test.getObservation());
	// 		dbTest.setNotes(test.getNotes());
	// 		dbTest.setDepartmentName(test.getDepartmentName());
	// 		dbTest.setExpectedTAT(test.getExpectedTAT());
	// 		Date followupDate = test.getFollowupDate();
	// 		dbTest.setFollowupDate(followupDate);
	// 		List<Attachment> dbAttachementList = test.getTestResultFiles();
	// 		if(dbAttachementList != null)
	// 			dbTest.setTestResultFiles(DBUtil.writeDBAttachement(dbAttachementList));
	// 		List<DBLabResult> dbResults = new ArrayList<DBLabResult>();
	// 		for(LabResult result : test.getResults()){
	// 			DBLabResult dbResult = new DBLabResult();
	// 			dbResult.setParameterName(result.getParameterName());
	// 			dbResult.setOutOfRange(result.isOutOfRange());
	// 			dbResult.setRange(result.getRange());
	// 			dbResult.setUnit(result.getUnit());
	// 			dbResult.setResult(result.getResult());
	// 			dbResult.setResultDateTime(new Date(result.getResultDate()));
	// 			dbResults.add(dbResult);
	// 		}
	// 		dbTest.setResults(dbResults);		
	// 		dbTests.add(dbTest);
	// 	}
	// 	dbLabTest.setTests(dbTests);
		
	// 	return dbLabTest;
	// }

	// public static LabTest newLabTest(DBLabTest dbLabTest) {
	// 	LabTest labTest = new LabTest();

	// 	if (dbLabTest == null)
	// 		return labTest;

	// 	labTest.setId(dbLabTest.getId().toString());
	// 	Date dbTestDate = dbLabTest.getTestDate();
	// 	if(dbTestDate != null)
	// 		labTest.setTestDate(dbTestDate.getTime());
	// 	labTest.setReferredBy(dbLabTest.getReferredBy());
	// 	labTest.setUhid(dbLabTest.getUhid());
	// 	labTest.setSiteKey(dbLabTest.getSiteKey());
	// 	labTest.setConsultId(dbLabTest.getConsultId());
	// 	labTest.setTag(dbLabTest.getTag());
	// 	labTest.setOrderId(dbLabTest.getOrderId());
	// 	Date dbImportDate = dbLabTest.getDateImported();
	// 	if ( dbImportDate != null)
	// 		labTest.setDateImported(dbImportDate.getTime());
	// 	labTest.setPackageId(dbLabTest.getPackageId());
	// 	labTest.setPackageName(dbLabTest.getPackageName());
	// 	List<Test> tests = new ArrayList<Test>();
	// 	for(DBTest dbTest : dbLabTest.getTests()){
	// 		Test test = new Test();
	// 		test.setTestName(dbTest.getTestName());
	// 		test.setSource(dbTest.getSource());
	// 		test.setTestId(dbTest.getTestid());
	// 		test.setObservation(dbTest.getObservation());
	// 		test.setNotes(dbTest.getNotes());
	// 		test.setCheckedBy(dbTest.getCheckedBy());
	// 		test.setIdentifier(dbTest.getIdentifier());
	// 		test.setVisitId(dbTest.getVisitId());
	// 		test.setPatientservice(dbTest.getPatientservice());
	// 		Date ReceivedOn = dbTest.getReceivedOn();
	// 		if (ReceivedOn != null)
	// 			test.setReceivedOn(dbTest.getReceivedOn().getTime());
	// 		Date getPrintedOn = dbTest.getPrintedOn();
	// 		if (getPrintedOn != null)
	// 			test.setPrintedOn(getPrintedOn.getTime());
	// 		Date first_report_printed_on = dbTest.getFirst_report_printed_on();
	// 		if (first_report_printed_on != null)
	// 			test.setFirst_report_printed_on(first_report_printed_on.getTime());
	// 		Date collectedOn = dbTest.getCollectedOn();
	// 		if (collectedOn != null)
	// 			test.setCollectedOn(collectedOn.getTime());
	// 		Date reportedOn = dbTest.getReportedOn();
	// 		if (reportedOn != null)
	// 			test.setReportedOn(reportedOn.getTime());
	// 		test.setDrn(dbTest.getDrn());
	// 		test.setLaboratoryHod(dbTest.getLaboratoryHod());
	// 		test.setSigningDocName(dbTest.getSigningDocName());
	// 		test.setDoc_sign(dbTest.getDoc_sign());
	// 		test.setDepartmentName(dbTest.getDepartmentName());
	// 		test.setSpecimen(dbTest.getSpecimen());
	// 		test.setExpectedTAT(dbTest.getExpectedTAT());
	// 		if (dbTest.getFollowupDate() != null)
	// 			test.setFollowupDate(dbTest.getFollowupDate().getTime());
	// 		List<DBAttachement> dbAttachementList = dbTest.getTestResultFiles();
	// 		if(dbAttachementList != null)
	// 			test.setTestResultFiles(DBUtil.readDBAttachement(dbAttachementList));
	// 		List<LabResult> results = new ArrayList<LabResult>();
	// 		for(DBLabResult dbResult : dbTest.getResults()) {
	// 			LabResult result = new LabResult();
	// 			result.setParameterName(dbResult.getParameterName());
	// 			result.setResult(dbResult.getResult());
	// 			if(dbResult.getRange() != null)
	// 				result.setRange(dbResult.getRange().replaceAll("\\n", "<br/>"));
	// 			result.setUnit(dbResult.getUnit());
	// 			result.setOutOfRange(dbResult.isOutOfRange());
	// 			Date resultDate = dbResult.getResultDateTime();
	// 			if (resultDate != null)
	// 				result.setResultDate(resultDate.getTime());
	// 			results.add(result);
	// 		}
	// 		test.setResults(results);
	// 		tests.add(test);			
	// 	}
	// 	labTest.setTests(tests);
		
	// 	return labTest;
	// }
	
	/*public List<DataImportCount> getTestImportCountByDateRange(long fromDate,long toDate,String siteKey)
	{
		List<DataImportCount> dataImportCountList = new ArrayList<DataImportCount>();
		String reduceString = "function(obj,prev) { prev.noOfTestImported ++; }"; 
		
	    BasicDBObject key = new BasicDBObject();
		key.put("dateImported", "true");
		
		BasicDBObject cond = new BasicDBObject();
		cond.put("dateImported", new BasicDBObject("$gte", new Date(fromDate)));
		cond.put("dateImported", new BasicDBObject("$lte", new Date(toDate)));
		cond.put("siteKey" , siteKey);
		
		BasicDBObject initial = new BasicDBObject();
		initial.put("noOfTestImported", 0);
	
		DBCollection coll= ds.getCollection(DBLabTest.class);
		DBObject dbObject= coll.group(key, cond, initial, reduceString);
		
		@SuppressWarnings("rawtypes")
		Map dbObjectMap                  = dbObject.toMap();
		DataImportCount dataImportCount  = null;
		SimpleDateFormat mongoDateFormat = new SimpleDateFormat("E MMM dd hh:mm:ss z yyyy");
		
		for ( int i= 0; i < dbObjectMap.size();i++)
		{
			dataImportCount = new DataImportCount();
			com.mongodb.CommandResult result = (com.mongodb.CommandResult)dbObject.get(""+i);
			try
            {
            	dataImportCount.setImportDate( mongoDateFormat.parse(result.getString("dateImported")).getTime()); 
            }
            catch(ParseException pe)
            {
            	log.error("PHR | LabTestDao | getTestImportCountByDateRange | Error ",pe);
            	dataImportCountList = new ArrayList<DataImportCount>();
            	break;
            }
			
			dataImportCount.setImportCount(result.getString("noOfTestImported"));
			dataImportCountList.add(dataImportCount);
		}
		return dataImportCountList;
	}*/
	
	/*public Map<Long,Integer> getTestImportCountByDateRange(long fromDate,long toDate,String siteKey) throws ParseException 
	{
		long startTime = System.currentTimeMillis();
		log.info("PHR | LabTestDao | Coming into getTestImportCountByDateRange | Start Time "+startTime);
		
		SimpleDateFormat mongoDateFormat   = new SimpleDateFormat("E MMM dd hh:mm:ss z yyyy");
		SimpleDateFormat displayDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		
		Map<Long,Integer> testImportCountMap = new HashMap<Long,Integer>();
		
		BasicDBObject cond = new BasicDBObject();
		cond.put("dateImported", new BasicDBObject("$gte", new Date(fromDate)).append("$lte", new Date(toDate)));
		cond.put("siteKey" , siteKey);
		
		DBCursor dbCursor= createQuery().find(cond);
		
		while ( dbCursor.hasNext())
		{
			DBObject dbObject = dbCursor.next();
			Date mongoDate    = mongoDateFormat.parse(String.valueOf(dbObject.get("dateImported")));
			long importedDate = displayDateFormat.parse(displayDateFormat.format(mongoDate)).getTime();
			Integer count = testImportCountMap.get(importedDate);
			if (count == null)
				testImportCountMap.put(importedDate,1);
			else
				testImportCountMap.put(importedDate, ++count);
		}
		
		log.info("PHR | LabTestDao | Exiting from  getTestImportCountByDateRange | Time Taken "+(System.currentTimeMillis()-startTime));
		return testImportCountMap;
	}*/
	
	// public int getTestCountByTestDate(long fromDate,long toDate, String siteKey)
	// {
	// 	long startTime = System.currentTimeMillis();
	// 	log.info("PHR | LabTestDao | Coming into getTestCountByTestDate | Start Time "+startTime);
		
	// 	Query<DBLabTest> dbLabTestQuery = createQuery();
	// 	dbLabTestQuery.field("testDate").greaterThanOrEq(new Date(fromDate));
	// 	dbLabTestQuery.field("testDate").lessThanOrEq(new Date(toDate));
	// 	dbLabTestQuery.field("siteKey").equal(siteKey);
	// 	int noOfTest = 0;
	// 	List<DBLabTest> dbLabTestList = dbLabTestQuery.asList();
		
	// 	for(DBLabTest dbLabTest : dbLabTestList)
	// 		noOfTest = noOfTest + dbLabTest.getTests().size();
	
	// 	log.info("PHR | LabTestDao | getTestCountByTestDate | No of Test "+noOfTest);
		
	// 	log.info("PHR | LabTestDao | Exiting from getTestCountByTestDate | Time Taken"+(System.currentTimeMillis()-startTime));
	// 	return noOfTest;
	// }
	// public int getImportedTestCountByTestDate(long fromDate,long toDate, String siteKey)
	// {
	// 	long startTime = System.currentTimeMillis();
	// 	log.info("PHR | LabTestDao | Coming into getImportedTestCountByTestDate | Start Time "+startTime);
		
	// 	Query<DBLabTest> dbLabTestQuery = createQuery();
	// 	dbLabTestQuery.field("dateImported").greaterThanOrEq(new Date(fromDate));
	// 	dbLabTestQuery.field("dateImported").lessThanOrEq(new Date(toDate));
	// 	dbLabTestQuery.field("siteKey").equal(siteKey);
	// 	int noOfTest = 0;
	// 	List<DBLabTest> dbLabTestList = dbLabTestQuery.asList();
		
	// 	for(DBLabTest dbLabTest : dbLabTestList)
	// 		noOfTest = noOfTest + dbLabTest.getTests().size();
	
	// 	log.info("PHR | LabTestDao | getImportedTestCountByTestDate | No of Test "+noOfTest);
		
	// 	log.info("PHR | LabTestDao | Exiting from getImportedTestCountByTestDate | Time Taken"+(System.currentTimeMillis()-startTime));
	// 	return noOfTest;
	// }
	
	// public List<DBLabTest> findOutStandingLabTests(String siteKey,int turnArroundTime) 
	// {
	// 	Date currentDate = new Date();
	// 	Calendar cal = Calendar.getInstance();
	// 	cal.setTime(currentDate);
	// 	cal.add(Calendar.MINUTE, -turnArroundTime);
		
	// 	log.info("PHR | LabTestDao | findOutStandingLabTests | Current Time  "+currentDate);
	// 	log.info("PHR | LabTestDao | findOutStandingLabTests | TurnArround Time  "+cal.getTime());
		
	// 	Query<DBLabTest> dbLabTestQuery = createQuery().field( "siteKey").equal(siteKey)
	// 											.field("tests.resultsReceived").equal(new Boolean("False"))
	// 											.field("tests.expectedTAT").equal(turnArroundTime)
	// 											.field("testDate").lessThanOrEq(cal.getTime());
		
	// 	return dbLabTestQuery.asList();
	// }			
}