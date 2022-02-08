package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class DBHRASection {
	@Id private ObjectId id;
  	@Property private String sectionName;
  	@Property private String sectionDescription;
  	@Embedded private List<DBHRAQuestion> questions = new ArrayList<DBHRAQuestion>();
  	@Property private int maxScore;
  	@Property private float factor;
  	@Property private boolean isScoreCalculationWithFactor;
  	
  
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}

	public void setQuestions(List<DBHRAQuestion> questions) {
		this.questions = questions;
	}
	public List<DBHRAQuestion> getQuestions() {
		return questions;
	}
	public void setSectionName(String sectionName) {
		this.sectionName = sectionName;
	}
	public String getSectionName() {
		return sectionName;
	}
	public void setSectionDescription(String sectionDescription) {
		this.sectionDescription = sectionDescription;
	}
	public String getSectionDescription() {
		return sectionDescription;
	}
	public int getMaxScore() {
		return maxScore;
	}
	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}
	public boolean isScoreCalculationWithFactor() {
		return isScoreCalculationWithFactor;
	}
	public void setScoreCalculationWithFactor(boolean isScoreCalculationWithFactor) {
		this.isScoreCalculationWithFactor = isScoreCalculationWithFactor;
	}
	public float getFactor() {
		return factor;
	}
	public void setFactor(float factor) {
		this.factor = factor;
	}
	
}