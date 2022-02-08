package org.apache.nifi.processors.daxoperation.dao;

import java.util.List;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBHospitalization;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;

//import com.healthhiway.businessobject.Attachment;
//import com.healthhiway.businessobject.Hospitalization;
//import com.healthhiway.dbo.DBAttachement;
//import com.healthhiway.dbo.DBHospitalization;
//import com.healthhiway.util.DBUtil;
//import com.healthhiway.util.MongoDB;

public class HospitalizationDao extends BasicDAO<DBHospitalization, ObjectId> {

    public HospitalizationDao(MongoClient mongoClient) {
        super(DBHospitalization.class, new MongoDBUtil(mongoClient).getDb());
    }

    public List<DBHospitalization> getDoctorVisit() {
        return find().asList();
    }


//	public static DBHospitalization newDBDHospitalization(Hospitalization hospitalization) {
//
//		DBHospitalization dbHospitalization = new DBHospitalization();
//
//		if(hospitalization == null)
//			return dbHospitalization;
//
//		dbHospitalization.setDateOfHospitilization(new Date(hospitalization.getDateOfHospitalization()));
//		dbHospitalization.setHospitalName(hospitalization.getHospitalName());
//		dbHospitalization.setDoctorName(hospitalization.getDoctorName());
//		dbHospitalization.setReasonForAdmission(hospitalization.getReasonForAdmission());
//		dbHospitalization.setDiagnosisNotes(hospitalization.getDiagnosisNotes());
//		dbHospitalization.setDateOfDischarge(new Date(hospitalization.getDateOfDischarge()));
//		dbHospitalization.setDischargeSummary(hospitalization.getDischargeSummary());
//		dbHospitalization.setDoctorInstruction(hospitalization.getDoctorInstruction());
//		dbHospitalization.setDateOfNextVisit(new Date(hospitalization.getDateOfNextVisit()));
//		dbHospitalization.setSource(hospitalization.getSource());
//		List<Attachment> dbAttachementList = hospitalization.getHospitalizationFiles();
//		if(dbAttachementList != null)
//			dbHospitalization.setHospitlizationFiles(DBUtil.writeDBAttachement(dbAttachementList));
//
//		return dbHospitalization;
//	}

//	public static Hospitalization newHospitalization(DBHospitalization dbHospitilization) {
//		Hospitalization hospitalization = new Hospitalization();
//
//		if(dbHospitilization == null)
//			return hospitalization;
//		hospitalization.setId(dbHospitilization.getId().toString());
//		if(dbHospitilization.getDateOfHospitilization() != null)
//			hospitalization.setDateOfHospitalization(dbHospitilization.getDateOfHospitilization().getTime());
//		hospitalization.setHospitalName(dbHospitilization.getHospitalName());
//		hospitalization.setDoctorName(dbHospitilization.getDoctorName());
//		hospitalization.setReasonForAdmission(dbHospitilization.getReasonForAdmission());
//		hospitalization.setDiagnosisNotes(dbHospitilization.getDiagnosisNotes());
//		hospitalization.setDateOfDischarge(dbHospitilization.getDateOfDischarge().getTime());
//		hospitalization.setDischargeSummary(dbHospitilization.getDischargeSummary());
//		hospitalization.setDoctorInstruction(dbHospitilization.getDoctorInstruction());
//		if(dbHospitilization.getDateOfNextVisit() != null)
//			hospitalization.setDateOfNextVisit(dbHospitilization.getDateOfNextVisit().getTime());
//		hospitalization.setSource(dbHospitilization.getSource());
//		List<DBAttachement> dbAttachementList = dbHospitilization.getHospitlizationFiles();
//		if(dbAttachementList != null)
//			hospitalization.setHospitalizationFiles(DBUtil.readDBAttachement(dbHospitilization.getHospitlizationFiles()));
//
//		return hospitalization;
//	}
}