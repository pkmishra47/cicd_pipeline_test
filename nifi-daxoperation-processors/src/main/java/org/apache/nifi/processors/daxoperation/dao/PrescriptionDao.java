package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBPrescription;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;


public class PrescriptionDao extends BasicDAO<DBPrescription, ObjectId> {
//    private static PrescriptionDao instance = new PrescriptionDao("");

    public PrescriptionDao(MongoClient mongoClient) {
        super(DBPrescription.class, new MongoDBUtil(mongoClient).getDb());
    }

//    public static PrescriptionDao INSTANCE() {
//        return instance;
//    }


//	public static Prescription newPrescription(DBPrescription dbPrescription) {
//		Prescription prescription = new Prescription();
//
//		if (dbPrescription == null)
//			return prescription;
//
//		prescription.setId(dbPrescription.getId().toString());
//		if(null != dbPrescription.getDateOfPrescription()){
//			prescription.setDateOfPrescription(dbPrescription.getDateOfPrescription().getTime());
//
//		}
//		if(null!=dbPrescription.getEndDate()){
//			prescription.setEndDate(dbPrescription.getEndDate().getTime());
//		}
//		prescription.setNotes(dbPrescription.getNotes());
//		prescription.setPrescribedBy(dbPrescription.getPrescribedBy());
//		prescription.setPrescriptionName(dbPrescription.getPrescriptionName());
//		prescription.setHospitalName(dbPrescription.getHospitalName());
//		prescription.setConsultId(dbPrescription.getConsultId());
//		prescription.setTag(dbPrescription.getTag());
//
//
//
//		if(null!= dbPrescription.getStartDate()){
//			prescription.setStartDate(dbPrescription.getStartDate().getTime());
//
//		}
//		prescription.setSource(dbPrescription.getSource());
//
//		List<PrescriptionDetail> prescriptiondetail = new ArrayList<PrescriptionDetail>();
//		for(DBPrescriptionDetail dbPrescriptionDetail : dbPrescription.getNewprescriptionDetails()){
//			PrescriptionDetail prescriptionDetail = new PrescriptionDetail();
//			prescriptionDetail.setName(dbPrescriptionDetail.getName());
//			prescriptionDetail.setStrength(dbPrescriptionDetail.getStrength());
//			prescriptionDetail.setRoute(dbPrescriptionDetail.getRoute());
//			prescriptionDetail.setHowOften(dbPrescriptionDetail.getHowOften());
//			prescriptiondetail.add(prescriptionDetail);
//		}
//		prescription.setPrescriptionDetails(prescriptiondetail);
//
//		List<DBAttachement> dbAttachementList = dbPrescription.getPrescriptionFiles();
//		if(dbAttachementList != null)
//			prescription.setPrescriptionFiles(DBUtil.readDBAttachement(dbPrescription.getPrescriptionFiles()));
//
//		return prescription;
//	}


//	public static DBPrescription newDBPrescription(Prescription prescription){
//		DBPrescription dbPrescription = new DBPrescription();
//
//		if(prescription == null)
//			return dbPrescription;
//
//		dbPrescription.setDateOfPrescription(new Date(prescription.getDateOfPrescription()));
//		dbPrescription.setEndDate(new Date(prescription.getEndDate()));
//		dbPrescription.setNotes(prescription.getNotes());
//		dbPrescription.setPrescribedBy(prescription.getPrescribedBy());
//		dbPrescription.setPrescriptionName(prescription.getPrescriptionName());
//		dbPrescription.setStartDate(new Date(prescription.getStartDate()));
//		dbPrescription.setSource(prescription.getSource());
//		dbPrescription.setHospitalName(prescription.getHospitalName());
//		dbPrescription.setConsultId(prescription.getConsultId());
//		dbPrescription.setTag(prescription.getTag());
//
//		List<DBPrescriptionDetail> newDBprescriptiondetaillist = new ArrayList<DBPrescriptionDetail>();
//		if(prescription.getPrescriptionDetails() != null )
//			for(PrescriptionDetail prescriptionDetail : prescription.getPrescriptionDetails()){
//				DBPrescriptionDetail dbPrescriptionDetail = new DBPrescriptionDetail();
//				dbPrescriptionDetail.setName(prescriptionDetail.getName());
//				dbPrescriptionDetail.setStrength(prescriptionDetail.getStrength());
//				dbPrescriptionDetail.setRoute(prescriptionDetail.getRoute());
//				dbPrescriptionDetail.setHowOften(prescriptionDetail.getHowOften());
//
//				newDBprescriptiondetaillist.add(dbPrescriptionDetail);
//			}
//		dbPrescription.setNewprescriptionDetails(newDBprescriptiondetaillist);
//
//		List<Attachment> dbAttachementList = prescription.getPrescriptionFiles();
//		if(dbAttachementList != null)
//			dbPrescription.setPrescriptionFiles(DBUtil.writeDBAttachement(dbAttachementList));
//
//		return dbPrescription;
//	}
}