package org.apache.nifi.processors.daxoperation.dbo;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;



@Entity(value = "HRATest", noClassnameStored = true)
public class DBHRATest implements Comparable<DBHRATest> {
	@Id private ObjectId id;
  	@Property private String result;
  	@Property private Date dateTaken;
  	@Property private String hraShortName;
  	@Property private boolean testComplete;
  	@Property private Map<String, String> completedQuestionAndAnswer = new HashMap<String,String> ();
	@Property private Map<String, String> completedQuestionAndRecommendations = new HashMap<String,String> ();
  	@Property private String hraName;
  	@Property private int score;
  	@Property private int maxScore;
  	
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}

	public void setResult(String result) {
		this.result = result;
	}
	public String getResult() {
		return result;
	}
	public void setDateTaken(Date dateTaken) {
		this.dateTaken = dateTaken;
	}
	public Date getDateTaken() {
		return dateTaken;
	}
	public void setHraShortName(String hraShortName) {
		this.hraShortName = hraShortName;
	}
	public String getHraShortName() {
		return hraShortName;
	}
	public void setTestComplete(boolean testComplete) {
		this.testComplete = testComplete;
	}
	public boolean getTestComplete() {
		return testComplete;
	}
	
	public Map<String, String> getCompletedQuestionAndAnswer() {
		return completedQuestionAndAnswer;
	}
	public void setCompletedQuestionAndAnswer(
			Map<String, String> completedQuestionAndAnswer) {
		this.completedQuestionAndAnswer = completedQuestionAndAnswer;
	}
	
	
	public Map<String, String> getCompletedQuestionAndRecommendations() {
		return completedQuestionAndRecommendations;
	}
	public void setCompletedQuestionAndRecommendations(
			Map<String, String> completedQuestionAndRecommendations) {
		this.completedQuestionAndRecommendations = completedQuestionAndRecommendations;
	}
	public String getHraName() {
		return hraName;
	}
	public void setHraName(String hraName) {
		this.hraName = hraName;
	}
	@Override
	public int compareTo(DBHRATest arg0) {
		if(arg0== null)
			throw new IllegalArgumentException("DBReminder is Null");

		return Comparators.stringCompare(getId().toString(), arg0.getId().toString());
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public int getMaxScore() {
		return maxScore;
	}
	public void setMaxScore(int maxScore) {
		this.maxScore = maxScore;
	}
	
	
	
}