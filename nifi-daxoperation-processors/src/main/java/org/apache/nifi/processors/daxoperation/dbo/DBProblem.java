package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

@Entity(value = "Problem", noClassnameStored = true)
public class DBProblem {
	@Id private ObjectId id;
	@Property private String problem; // This is referenced from Problem Master
	@Property private Boolean havingNow;
	
	@Embedded("problemDetail")
	private List<DBProblemDetail> problemDetail = new ArrayList<DBProblemDetail>();
	
	@Property private String notes;
	
	public ObjectId getId() {
		return id;
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public String getProblem() {
		return problem;
	}
	
	public void setProblem(String problem) {
		this.problem = problem;
	}
	
	public void setHavingNow(Boolean havingNow) {
		this.havingNow = havingNow;
	}
	
	public Boolean getHavingNow() {
		return havingNow;
	}
	
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String getNotes() {
		return this.notes;
	}

	public void setProblemDetail(List<DBProblemDetail> problemDetail) {
		this.problemDetail = problemDetail;
	}

	public List<DBProblemDetail> getProblemDetail() {
		return problemDetail;
	}
	
	public void addProblemDetail(DBProblemDetail problemDetail) {
		this.problemDetail.add(problemDetail);
	}
}
