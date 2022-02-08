package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;


@Entity(value = "FamilyHistory", noClassnameStored = true)
public class DBFamilyHistory implements Comparable<DBFamilyHistory>{
	@Id private ObjectId id;
    @Property private Date createdAt;
	@Property private InheritedDiseaseName DiseaseName ; 
	@Embedded  
	private List<DBFamilyHistoryDetail> familyHistoryDetail = new ArrayList<DBFamilyHistoryDetail>(); 
	public static enum InheritedDiseaseName {
		Diabetes,
	   Cancer, 
	   Stroke, 
	   HeartAttack,
	   HighBloodPressure,
	   BleedingDisorder,
	   GeneticDisorder,
	   Arthritis,
	   MentalIllness
	}
	
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

		
	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public List<DBFamilyHistoryDetail> getFamilyHistoryDetail() {
		return familyHistoryDetail;
	}

	public void setFamilyHistoryDetail(List<DBFamilyHistoryDetail> familyHistoryDetail) {
		this.familyHistoryDetail = familyHistoryDetail;
	}

	public void addFamilyHistoryDetail(DBFamilyHistoryDetail familyhisDetail) {
		familyHistoryDetail.add(familyhisDetail);
	}
	public InheritedDiseaseName getDiseaseName() { 
	        return DiseaseName; 
    }
	public void setDiseaseName(InheritedDiseaseName diseaseName) { 
	     DiseaseName = diseaseName; 
 	}

	@Override
	public int compareTo(DBFamilyHistory dbFamilyHistory) {
		if(dbFamilyHistory == null)
			throw new IllegalArgumentException("DBFamilyHistory is Null");

		return Comparators.stringCompare(id.toString(), dbFamilyHistory.getId().toString());
	} 
}

