package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.bo.Comments;
import org.apache.nifi.processors.daxoperation.bo.Tool;
import org.apache.nifi.processors.daxoperation.bo.ToolData;
import org.apache.nifi.processors.daxoperation.bo.ToolParameters;
import org.apache.nifi.processors.daxoperation.dbo.DBComments;
import org.apache.nifi.processors.daxoperation.dbo.DBTool;
import org.apache.nifi.processors.daxoperation.dbo.DBToolData;
import org.apache.nifi.processors.daxoperation.dbo.DBToolParameters;
import org.apache.nifi.processors.daxoperation.utils.Comparators;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component("toolsDao")
public class ToolsDao extends BasicDAO<DBTool, ObjectId> {

	public ToolsDao(MongoClient mongoClient) {
		super(DBTool.class, new MongoDBUtil(mongoClient).getDb());
	}

	public  List<DBTool> getTestResult() {
		return find().asList();
	}

	public static ToolParameters newToolParameters(DBToolParameters dbTp) {
		ToolParameters tp = new ToolParameters();
		
		if(dbTp != null) {		
			tp.setTestName(dbTp.getTestName());
			tp.setTestValue(dbTp.getTestValue());
			tp.setRange(dbTp.getRange());
			tp.setUnits(dbTp.getUnits());
			tp.setParamType(dbTp.getParamType());
		}
		return tp;
	}
	
	public static DBToolParameters newDbToolParameters(ToolParameters tp) {
		DBToolParameters dbTp = new DBToolParameters();
		
		if(tp != null) {		
			dbTp.setTestName(tp.getTestName());
			dbTp.setTestValue(tp.getTestValue());
			dbTp.setRange(tp.getRange());
			dbTp.setUnits(tp.getUnits());
			dbTp.setParamType(tp.getParamType());
		}
		return dbTp;
	}

	public static ToolData newToolData(DBToolData dbToolData) {
		ToolData toolData = new ToolData();
		
		if(dbToolData == null)
			return toolData;
		
		toolData.setId(dbToolData.getId());
		List<ToolParameters> tpList = new ArrayList<ToolParameters>();
		for(DBToolParameters tp : dbToolData.getToolParameters()) {
			tpList.add(newToolParameters(tp));		
		}	
		toolData.setToolParameters(tpList);
		toolData.setDateAndTime(dbToolData.getDateAndTime());
		toolData.setSource(dbToolData.getSource());
		toolData.setProgram_name(dbToolData.getProgram_name());
		toolData.setProgram_tag(dbToolData.getProgram_tag());
		
		if ( dbToolData.getCommentsList() != null )
		{
			for(DBComments dbComments : dbToolData.getCommentsList()) {
				Comments comments = new Comments();
				
				comments.setComments(dbComments.getComments());
				comments.setCommentsDate(dbComments.getCommentsDate());
				comments.setCreatedBy(dbComments.getCreatedBy());
				toolData.addToCommentsList(comments);
			}
		}
		return toolData;
	}
	
	public static DBToolData newDBToolData(ToolData toolData) {
		DBToolData dbToolData = new DBToolData();
		
		if(toolData == null)
			return dbToolData;
		
		List<DBToolParameters> toolParamsList =  new ArrayList<DBToolParameters>();	
		for(ToolParameters tp : toolData.getToolParameters()) {
				toolParamsList.add(newDbToolParameters(tp));	
		}			
		dbToolData.setToolParameters(toolParamsList);
		dbToolData.setDateAndTime(toolData.getDateAndTime());
		dbToolData.setSource(toolData.getSource());
		dbToolData.setProgram_name(toolData.getProgram_name());
		dbToolData.setProgram_tag(toolData.getProgram_tag());
		
		
		if ( toolData.getCommentsList() != null )
		{
			for(Comments comments : toolData.getCommentsList()) {
				DBComments dbComments = new DBComments();
				
				dbComments.setComments(comments.getComments());
				dbComments.setCommentsDate(comments.getCommentsDate());
				dbComments.setCreatedBy(comments.getCreatedBy());
				dbToolData.addCommentsDetail(dbComments);
			}
		}
		return dbToolData;
	}

	
	public static Tool newTools(DBTool dbTools) {

		Tool tools = new Tool();

		if (dbTools == null)
			return tools;

		tools.setId(dbTools.getId());
		tools.setToolName(dbTools.getToolName());
		if(dbTools.getCreationDate() != null)
			tools.setDateAndTime(dbTools.getCreationDate());
		
		List<ToolData> tdList = new ArrayList<ToolData>();
		for(DBToolData td : dbTools.getToolsData()) {
			ToolData toolData = newToolData(td);
			tdList.add(toolData);
		}
		Collections.sort(tdList, Comparators.toolsDataComparatorByDate());
		tools.setToolData(tdList);
		tools.setIsActive(dbTools.getIsActive());
		tools.setObservation(dbTools.getObservations());
		
		return tools;
	}
	
	public static DBTool newDBTools(Tool tools) {

		DBTool dbTools = new DBTool();

		if (tools == null)
			return dbTools;
		
		dbTools.setToolName(tools.getToolName());
		dbTools.setCreationDate(tools.getDateAndTime());
		
		List<DBToolData> tdList = new ArrayList<DBToolData>();
		for(ToolData td : tools.getToolData()) {
			DBToolData toolData = newDBToolData(td);
			tdList.add(toolData);
		}
		dbTools.setToolsData(tdList);
		dbTools.setIsActive(tools.getIsActive());
		dbTools.setObservations(tools.getObservation());

		return dbTools;
	}
	
	public DBTool get_DBToolByName(String toolName) {
		DBTool dbTool = null;
		if(toolName == null)
			return dbTool;
		
		Query<DBTool> q = createQuery();
		q.criteria("toolName").equal(toolName);
		dbTool =  (q.asList().size()>0)?q.asList().get(0):null;		
		return dbTool;
	}
	
	public List<DBTool> get_toolsData(List<ObjectId> objIdList, String toolName) {
		List<DBTool> dbToolList = new ArrayList<DBTool>();
		
		if(objIdList == null || toolName == null )
			return dbToolList;
		
		Query<DBTool> q = createQuery().field( "_id" ).in( objIdList );
		q.field("toolName").endsWithIgnoreCase(toolName);
		dbToolList = q.asList();
		
		return dbToolList;
		
	}
	
	public DBTool get_DBToolById(String id)
	{
		DBTool dbTool = null;
		if(id == null)
			return dbTool;
		ObjectId toolObjectId = new ObjectId(id);
		Query<DBTool> q = createQuery();
		q.criteria("_id").equal(toolObjectId);
		
		dbTool =  (q.asList().size()>0)?q.asList().get(0):null;		
		return dbTool;
	}
	
	public List<DBTool> getToolByObjectIds(List<ObjectId> oids) {
		if(oids==null)
			return null;
		if(oids.size()==0)
			return null;

		Query<DBTool> q = createQuery();
		q.and(
		        q.criteria("_id").in(oids)		        
		);
		return q.asList();
	}
	
	
}