package org.apache.nifi.processors.daxoperation.dbo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;

import org.apache.nifi.processors.daxoperation.utils.Comparators;

@Embedded
public class DBToolData implements Comparable<DBToolData>{
	@Id ObjectId id = new ObjectId();
	@Embedded private List<DBToolParameters> toolParameters = new ArrayList<DBToolParameters>();
	@Property private Date dateAndTime;
	@Embedded
	private List<DBComments> commentsList = new ArrayList<DBComments>();
	@Property private String source;
	@Property private String program_name;
	@Property private String program_tag;
	
	

	public List<DBComments> getCommentsList() {
		return commentsList;
	}

	public void setCommentsList(List<DBComments> commentsList) {
		this.commentsList = commentsList;
	}
	
	public void addCommentsDetail(DBComments dbComments) {
		this.commentsList.add(dbComments);
	}

	public void setId(ObjectId id) {
		this.id = id;
	}

	public ObjectId getId() {
		return id;
	}

	public void setDateAndTime(Date dateAndTime) {
		this.dateAndTime = dateAndTime;
	}
	public Date getDateAndTime() {
		return dateAndTime;
	}

	public List<DBToolParameters> getToolParameters() {
		return toolParameters;
	}

	public void setToolParameters(List<DBToolParameters> toolParameters) {
		this.toolParameters = toolParameters;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	@Override
	public int compareTo(DBToolData o) {

		if(o == null)
			throw new NullPointerException("Object Null");
		
		return Comparators.stringCompare(id.toString(), o.getId().toString());
	}

	public String getProgram_tag() {
		return program_tag;
	}

	public void setProgram_tag(String program_tag) {
		this.program_tag = program_tag;
	}

	public String getProgram_name() {
		return program_name;
	}

	public void setProgram_name(String program_name) {
		this.program_name = program_name;
	}
}