package org.apache.nifi.processors.daxoperation.bo;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;

public class ToolData {
    public ObjectId id;
    public List<ToolParameters> toolParameters;
    public Date dateAndTime;
    public List<Comments> commentsList;
    public String source;
    public String program_name;
    public String program_tag;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public List<ToolParameters> getToolParameters() {
        return toolParameters;
    }

    public void setToolParameters(List<ToolParameters> toolParameters) {
        this.toolParameters = toolParameters;
    }

    public Date getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(Date dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public List<Comments> getCommentsList() {
        return commentsList;
    }

    public void setCommentsList(List<Comments> commentsList) {
        this.commentsList = commentsList;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getProgram_name() {
        return program_name;
    }

    public void setProgram_name(String program_name) {
        this.program_name = program_name;
    }

    public String getProgram_tag() {
        return program_tag;
    }

    public void setProgram_tag(String program_tag) {
        this.program_tag = program_tag;
    }

    public void addToCommentsList(Comments comments) {
        this.commentsList.add(comments);
    }
}
