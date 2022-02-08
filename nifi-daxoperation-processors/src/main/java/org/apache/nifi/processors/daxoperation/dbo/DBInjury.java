package org.apache.nifi.processors.daxoperation.dbo;

import java.util.List;

import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Property;

@Embedded
public class DBInjury {
	
	@Property private List<String> type;
	@Property private String description;
	@Property private List<String> cause;
	@Property private List<String> incidentLocation;
	@Property private List<String> treatmentGiven;
	@Property private List<String> labInvestigation;
	@Property private List<String> ailment;
	@Property private List<String> comments;
	@Property private String disposal;
	@Property private String ameform;
	
	
	public List<String> getType() {
		return type;
	}
	public void setType(List<String> string) {
		this.type = string;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getCause() {
		return cause;
	}
	public void setCause(List<String> cause) {
		this.cause = cause;
	}
	public List<String> getIncidentLocation() {
		return incidentLocation;
	}
	public void setIncidentLocation(List<String> incidentLocation) {
		this.incidentLocation = incidentLocation;
	}
	public List<String> getTreatmentGiven() {
		return treatmentGiven;
	}
	public void setTreatmentGiven(List<String> treatmentGiven) {
		this.treatmentGiven = treatmentGiven;
	}
	public List<String> getLabInvestigation() {
		return labInvestigation;
	}
	public void setLabInvestigation(List<String> labInvestigation) {
		this.labInvestigation = labInvestigation;
	}
	public List<String> getAilment() {
		return ailment;
	}
	public void setAilment(List<String> ailment) {
		this.ailment = ailment;
	}
	public List<String> getComments() {
		return comments;
	}
	public void setComments(List<String> comments) {
		this.comments = comments;
	}
	public String getDisposal() {
		return disposal;
	}
	public void setDisposal(String disposal) {
		this.disposal = disposal;
	}
	public String getAmeform() {
		return ameform;
	}
	public void setAmeform(String ameform) {
		this.ameform = ameform;
	}
	
	
	
}
