package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.bo.DietPlan;
import org.apache.nifi.processors.daxoperation.dbo.DBDietPlan;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import java.util.List;

public class DietPlanDao extends BasicDAO<DBDietPlan, ObjectId>  {
	public DietPlanDao(MongoClient mongoClient) {
		super(DBDietPlan.class, new MongoDBUtil(mongoClient).getDb());
	}
	
	public static DietPlan newDietPlan(DBDietPlan dbDietPlan) {
		DietPlan dietPlan = new DietPlan();
		if(dbDietPlan == null)
			return dietPlan;
		
		dietPlan.setDate(dbDietPlan.getDate());
		dietPlan.setAge(dbDietPlan.getAge());
		dietPlan.setProtein(dbDietPlan.getProtein());
		dietPlan.setFat(dbDietPlan.getFat());
		dietPlan.setEnergy(dbDietPlan.getEnergy());
		dietPlan.setCarbohydrates(dbDietPlan.getCarbohydrates());
		dietPlan.setWeight(dbDietPlan.getWeight());
		dietPlan.setHeight(dbDietPlan.getHeight());
		dietPlan.setBmi(dbDietPlan.getBmi());
		dietPlan.setDiagnosis(dbDietPlan.getDiagnosis());
		dietPlan.setComments(dbDietPlan.getComments());
		dietPlan.setDietician(dbDietPlan.getDietician());
		dietPlan.setConsultant(dbDietPlan.getConsultant());
		dietPlan.setFooditems(dbDietPlan.getFooditems());
		
		return dietPlan;
	}
	
	public List<DBDietPlan> getDietPlanByObjectIds(List<ObjectId> oids) {
		if(oids==null)
			return null;
		if(oids.size()==0)
			return null;

		Query<DBDietPlan> q = createQuery();
		q.and(
		        q.criteria("_id").in(oids)		        
		);
		return q.asList();
	}
	
}