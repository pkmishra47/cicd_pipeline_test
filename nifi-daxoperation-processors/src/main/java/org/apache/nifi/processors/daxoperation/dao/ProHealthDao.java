package org.apache.nifi.processors.daxoperation.dao;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.nifi.processors.daxoperation.bo.ConsultationDetail;
import org.apache.nifi.processors.daxoperation.bo.InvestigationDetail;
import org.apache.nifi.processors.daxoperation.bo.ProHealthInfo;
import org.apache.nifi.processors.daxoperation.bo.SugarLabresult;
import org.apache.nifi.processors.daxoperation.dbo.DBProHealth;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.mongodb.MongoClient;

@Component("ProHealthDao")
public class ProHealthDao extends BasicDAO<DBProHealth, ObjectId> {
	private static Logger log = LoggerFactory.getLogger(ProHealthDao.class);
	
	public ProHealthDao(MongoClient mongoClient) {
		super(DBProHealth.class, new MongoDBUtil(mongoClient).getDb());
		ensureIndexes();
	}
	
	// public static ProHealthInfo newProHealthInfo(DBProHealth dbProHealth) {
	// 	ProHealthInfo info = new ProHealthInfo();
		
	// 	if(dbProHealth == null)
	// 		return info;

	// 	if(dbProHealth.getTestDate() != null)
	// 		info.setTestDate(dbProHealth.getTestDate().getTime());
	// 	info.setGuestLocation(dbProHealth.getGuestLocation());
	// 	info.setAhcno(dbProHealth.getAhcno());
	// 	info.setProHealthLocation(dbProHealth.getProHealthLocation());
	// 	info.setContactPreference(dbProHealth.getContactPreference());
	// 	info.setAhcphysician(dbProHealth.getAhcphysician());
	// 	info.setDoctorsSpeciality(dbProHealth.getDoctorsSpeciality());
	// 	info.setHeight(dbProHealth.getHeight());
	// 	info.setWeight(dbProHealth.getWeight());
	// 	info.setImpression(dbProHealth.getImpression());
	// 	info.setDietChanges(dbProHealth.getDietChanges());
	// 	info.setActivityChanges(dbProHealth.getActivityChanges());
	// 	info.setOtherLifeStyleChanges(dbProHealth.getOtherLifeStyleChanges());
	// 	info.setFollowupPreview(dbProHealth.getFollowupPreview());
	// 	info.setAllergy(dbProHealth.getAllergy());
	// 	info.setSystolicBP(dbProHealth.getSystolicBP());
	// 	info.setDiastolicBP(dbProHealth.getDiastolicBP());
	// 	info.setBmi(dbProHealth.getBmi());
		
	// 	try {
	// 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
	// 		DocumentBuilder builder = factory.newDocumentBuilder();
			
	// 		List<InvestigationDetail> investigationDetails = new ArrayList<InvestigationDetail>();
	// 		if(dbProHealth.getInvestigationsDetail() != null) {
	// 			ByteArrayInputStream input = new ByteArrayInputStream(
	// 							   dbProHealth.getInvestigationsDetail().getBytes("UTF-8"));
	// 			Document inv = builder.parse(input);
	// 			NodeList nList = inv.getElementsByTagName("GeneralAdviceDetail");
	// 			for (int temp = 0; temp < nList.getLength(); temp++) {
	// 	            Node nNode = nList.item(temp);
		            
	// 	            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	// 	                Element eElement = (Element) nNode;
	// 	                InvestigationDetail detail = new InvestigationDetail();
		                
	// 	                detail.setTestName(eElement.getAttribute("TestName"));
	// 	                detail.setTestSchedule(eElement.getAttribute("TestSchedule"));
	// 	                detail.setRepeatFrequency(eElement.getAttribute("RepeatFrequency"));
	// 	                detail.setRepeatEveryFreq(eElement.getAttribute("RepeatEveryFreq"));
	// 	                detail.setCheckDaysRep(eElement.getAttribute("ChkDaysRep"));
	// 	                detail.setTestNotesInstructions(eElement.getAttribute("TestNotesInstructions"));
		                
	// 	                investigationDetails.add(detail);
	// 	            }
	// 			}
	// 			input.close();
	// 		}
	// 		info.setInvestigationsDetails(investigationDetails);
			
	// 		List<ConsultationDetail> consultationDetails = new ArrayList<ConsultationDetail>();
	// 		if(dbProHealth.getConsultationDetail() != null) {
	// 			ByteArrayInputStream input = new ByteArrayInputStream(
	// 				   dbProHealth.getConsultationDetail().getBytes("UTF-8"));
	
	// 			Document cons = builder.parse(input);
	// 			NodeList nList = cons.getElementsByTagName("ReferralConsultantDetail");
	// 			for (int temp = 0; temp < nList.getLength(); temp++) {
	// 			     Node nNode = nList.item(temp);
				     
	// 			     if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	// 			         Element eElement = (Element) nNode;
	// 			         ConsultationDetail detail = new ConsultationDetail();
				         
	// 			         detail.setSpecialityName(eElement.getAttribute("SpecialityName"));
	// 			         detail.setDoctorName(eElement.getAttribute("DoctorName"));
	// 			         detail.setTestNotesInstructions(eElement.getAttribute("TestNotesInstructions"));
				         
	// 			         consultationDetails.add(detail);
	// 		         }
	// 			}
	// 			input.close();
	// 		}
	// 		info.setConsultationDetails(consultationDetails);
	// 		info.setTests(new ArrayList<SugarLabresult>());
	// 	} catch(Exception ex) {
	// 		log.info("Unable to parse XML Content", ex);
	// 	}
				
	// 	return info;
	// }
}