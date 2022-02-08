package org.apache.nifi.processors.daxoperation.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.mongodb.MongoClient;

import org.apache.nifi.processors.daxoperation.bo.AccessLevel;
import org.apache.nifi.processors.daxoperation.bo.Circle;
import org.apache.nifi.processors.daxoperation.bo.Resources;
import org.apache.nifi.processors.daxoperation.dbo.DBAccessLevel;
import org.apache.nifi.processors.daxoperation.dbo.DBCircle;
import org.apache.nifi.processors.daxoperation.dbo.DBResources;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.stereotype.Component;

@Component("circleDao")
public class CircleDao extends BasicDAO<DBCircle, ObjectId> {

	public CircleDao(MongoClient mongoClient) {
		super(DBCircle.class, new MongoDBUtil(mongoClient).getDb());
	}

	public static Circle newCircle(DBCircle dbCircle) {
		Circle circle = new Circle();

		if (dbCircle == null)
			return circle;

		circle.setId(dbCircle.getId());
		circle.setName(dbCircle.getName());
		circle.setOwnerId(dbCircle.getOwnerId());
		circle.setCategory(dbCircle.getCategory());
		
		circle.setUserList(new ArrayList<String>());
		for(String userId: dbCircle.getUserList())
			circle.getUserList().add(userId);

		for(String userId: dbCircle.getRemovedUsers())
			circle.getUserList().remove(userId);
		
		Map<Resources, List<AccessLevel>> permissions = new HashMap<Resources, List<AccessLevel>>();
		circle.setPermissions(permissions);

		for (Entry<DBResources, List<DBAccessLevel>> entry : dbCircle.getPermissions().entrySet()) {
			Resources resource = Resources.valueOf(entry.getKey().name());
			
			List<AccessLevel> accessLevelList = new ArrayList<AccessLevel>();
			for (DBAccessLevel accessLevel : entry.getValue())
				accessLevelList.add(AccessLevel.valueOf(accessLevel.name()));

			permissions.put(resource, accessLevelList);
		}

		return circle;
	}

	public static DBCircle newDBCircle(Circle circle) {
		DBCircle dbCircle = new DBCircle();

		if (circle == null)
			return dbCircle;

		dbCircle.setName(circle.getName());
		dbCircle.setOwnerId(circle.getOwnerId());
		dbCircle.setCategory(circle.getCategory());
		dbCircle.setUpdatedAt(new Date());
		
		//UserList will not be set in this operation, Just like the ObjectId.
		
		Map<DBResources, List<DBAccessLevel>> permissions = new HashMap<DBResources, List<DBAccessLevel>>();
		dbCircle.setPermissions(permissions);

		for (Entry<Resources, List<AccessLevel>> entry : circle.getPermissions().entrySet()) {
			DBResources resource = DBResources.valueOf(entry.getKey().name());
			
			List<DBAccessLevel> accessLevelList = new ArrayList<DBAccessLevel>();
			for (AccessLevel accessLevel : entry.getValue())
				accessLevelList.add(DBAccessLevel.valueOf(accessLevel.name()));

			permissions.put(resource, accessLevelList);

		}

		return dbCircle;
	}	
}