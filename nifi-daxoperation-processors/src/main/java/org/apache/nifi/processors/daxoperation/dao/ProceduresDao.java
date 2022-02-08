package org.apache.nifi.processors.daxoperation.dao;

import java.util.List;

import com.mongodb.MongoClient;

import org.apache.nifi.processors.daxoperation.dbo.DBProcedure;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;

public class ProceduresDao  extends BasicDAO<DBProcedure, ObjectId> {
	
	public ProceduresDao(MongoClient mongoClient) {
		super(DBProcedure.class, new MongoDBUtil(mongoClient).getDb());
	}

	public List<DBProcedure> getDDBProcedures() {
		return find().asList();
	}
		
	// public static DBProcedure newDBProcedure(Procedure procedure) {
		
	// 	DBProcedure dbProcedure = new DBProcedure();
		
	// 	if(procedure == null)
	// 		return dbProcedure;
	
	// 	dbProcedure.setProcedureName(procedure.getProcedureName());
	// 	dbProcedure.setStartDate(procedure.getStartDate());
	// 	dbProcedure.setEndDate(procedure.getEndDate());
	// 	dbProcedure.setDoctorTreated(procedure.getDoctorTreated());		
	// 	dbProcedure.setNotes(procedure.getNotes());
	// 	dbProcedure.setSource(procedure.getSource());
	// 	List<Attachment> dbAttachment = procedure.getProcedureFiles();
	// 	if(dbAttachment != null)
	// 		dbProcedure.setProcedureFiles(DBUtil.writeDBAttachement(dbAttachment));
		
	// 	return dbProcedure;
	// }
	
	// public static Procedure newProcedure(DBProcedure dbProcedure) {
		
	// 	Procedure procedure = new Procedure();
		
	// 	if(dbProcedure == null)
	// 		return procedure;
		
	// 	procedure.setId(dbProcedure.getId().toString());
	// 	procedure.setProcedureName(dbProcedure.getProcedureName());
	// 	if(dbProcedure.getStartDate() != null)
	// 		procedure.setStartDate(dbProcedure.getStartDate());
	// 	if(dbProcedure.getEndDate() != null)
	// 		procedure.setEndDate(dbProcedure.getEndDate());
	// 	procedure.setDoctorTreated(dbProcedure.getDoctorTreated());		
	// 	procedure.setNotes(dbProcedure.getNotes());
	// 	procedure.setSource(dbProcedure.getSource());
	// 	List<DBAttachement> dbAttachementList = dbProcedure.getProcedureFiles();
	// 	if(dbAttachementList != null)
	// 		procedure.setProcedureFiles(DBUtil.readDBAttachement(dbAttachementList));
		
	// 	return procedure;
	// }
}