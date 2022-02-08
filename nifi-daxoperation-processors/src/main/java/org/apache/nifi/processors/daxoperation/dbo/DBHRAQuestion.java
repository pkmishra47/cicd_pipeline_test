package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class DBHRAQuestion {
	@Id private ObjectId id;
	@Property private String questionId;
  	@Property private String description;
  	@Property private String questionType;
  	@Property private String question;
  	@Embedded private List<String> choices = new ArrayList<String>();
  	@Embedded private Map<String,Integer> wChoice = new HashMap<String,Integer> (); // Choice with weight
  	@Embedded private Map<String,String> recommendations = new HashMap<String,String> (); // Choice with recommendatios
  	@Embedded private Map<String,String> choiseBasedSubQuestion = new HashMap<String,String>();
  	@Embedded private Map<String,List<String>> subQuestionChoices = new HashMap<String,List<String>>();
  	@Embedded private Map<String,Integer> subQuestionChoiceWeight = new HashMap<String,Integer> (); 
  	@Property private boolean subQuestionAvailable;
	@Embedded private Map<String,Float> repeatQuestionWChoice = new HashMap<String,Float> (); // Choice with weight
  
	public ObjectId getId() {
		return id;
	}
	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getQuestionId() {
		return questionId;
	}
	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setQuestionType(String questionType) {
		this.questionType = questionType;
	}
	public String getQuestionType() {
		return questionType;
	}
	public void setQuestion(String question) {
		this.question = question;
	}
	public String getQuestion() {
		return question;
	}
	
	public List<String> getChoices() {
		return choices;
	}
	public void setChoices(List<String> choices) {
		this.choices = choices;
	}
	public Map<String,Integer> getwChoice() {
		return wChoice;
	}
	public void setwChoice(Map<String,Integer> wChoice) {
		this.wChoice = wChoice;
	}
	public void addWChoice(String choice, int weight) {
		this.wChoice.put(choice, weight);
	}
	public int getChoiceWeight(String choice) {
		return this.wChoice.get(choice);
	}
	public Map<String, String> getRecommendations() {
		return recommendations;
	}
	public void setRecommendations(Map<String, String> recommendations) {
		this.recommendations = recommendations;
	}
	
	public Map<String, String> getChoiseBasedSubQuestion() {
		return choiseBasedSubQuestion;
	}
	public void setChoiseBasedSubQuestion(Map<String, String> choiseBasedSubQuestion) {
		this.choiseBasedSubQuestion = choiseBasedSubQuestion;
	}
	public Map<String, List<String>> getSubQuestionChoices() {
		return subQuestionChoices;
	}
	public void setSubQuestionChoices(Map<String, List<String>> subQuestionChoices) {
		this.subQuestionChoices = subQuestionChoices;
	}
	public Map<String, Integer> getSubQuestionChoiceWeight() {
		return subQuestionChoiceWeight;
	}
	public void setSubQuestionChoiceWeight(
			Map<String, Integer> subQuestionChoiceWeight) {
		this.subQuestionChoiceWeight = subQuestionChoiceWeight;
	}
	public boolean isSubQuestionAvailable() {
		return subQuestionAvailable;
	}
	public void setSubQuestionAvailable(boolean subQuestionAvailable) {
		this.subQuestionAvailable = subQuestionAvailable;
	}
	public Map<String, Float> getRepeatQuestionWChoice() {
		return repeatQuestionWChoice;
	}
	public void setRepeatQuestionWChoice(Map<String, Float> repeatQuestionWChoice) {
		this.repeatQuestionWChoice = repeatQuestionWChoice;
	}
	
	public void addRepeatQuestionWChoice(String choice, float weight) {
		this.repeatQuestionWChoice.put(choice, weight);
	}
	

}