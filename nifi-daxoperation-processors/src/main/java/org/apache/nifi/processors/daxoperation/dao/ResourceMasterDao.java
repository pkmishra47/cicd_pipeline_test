package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.bo.ResourceMaster;
import org.apache.nifi.processors.daxoperation.bo.Resources;
import org.apache.nifi.processors.daxoperation.dbo.DBResourceMaster;
import org.apache.nifi.processors.daxoperation.dbo.DBResources;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.apache.nifi.processors.daxoperation.utils.NVPair;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceMasterDao extends BasicDAO<DBResourceMaster, ObjectId> {

    public ResourceMasterDao(MongoClient mongoClient) {
        super(DBResourceMaster.class, new MongoDBUtil(mongoClient).getDb());
    }

    public List<String> getResourceDetails(String resourceName) {
        List<String> resDetail = new ArrayList<String>();

        if(resourceName == null)
            return resDetail;

        Query<DBResourceMaster> q = createQuery();
        q.criteria("dbResourceType").equal(resourceName);

        List<DBResourceMaster> dbList = q.asList();
        if(dbList.isEmpty()) {
//			log.error("Could not get record of the resource: " + resourceName);
            return resDetail;
        }

        for(DBResourceMaster rm : dbList)
            resDetail.add(rm.getDbResourceName());

//		log.info("Returning resource details for: {} of size: {}", resourceName, resDetail.size());
        return resDetail;
    }

    public List<NVPair> getResourceDetail(String resourceType, String resourceName /* naming conversion needed */) {

        List<NVPair> detailMap = new ArrayList<NVPair>();
        DBResourceMaster rm = null;

        if(resourceType ==null || resourceName == null)
            return detailMap;

        Query<DBResourceMaster> q = createQuery();
        q.criteria("dbResourceType").equal(resourceType);
        q.criteria("dbResourceName").equal(resourceName);

        List<DBResourceMaster> resMasterList = q.asList();
        if(resMasterList.isEmpty())
            return detailMap;

        if(resMasterList.size() > 0)
            rm = resMasterList.get(0);


        return rm.getResourceMasterDetail();
    }

    public DBResourceMaster getResourceMaster(String resourceName) {
        DBResourceMaster dbMetaMaster = null;

        if(resourceName == null)
            return dbMetaMaster;

        Query<DBResourceMaster> q = createQuery().field("dbResourceName").equal(resourceName).limit(1);

        List<DBResourceMaster> dbMetaList = q.asList();
        if(dbMetaList.size() > 0)
            dbMetaMaster = dbMetaList.get(0);

        return dbMetaMaster;

    }


    public static ResourceMaster newResourceMaster(DBResourceMaster dbResMetaMaster) {

        ResourceMaster resourceMaster = new ResourceMaster();

        if(dbResMetaMaster == null)
            return resourceMaster;

        resourceMaster.setId(dbResMetaMaster.getId());

        DBResources dbResourceType = DBResources.fromString(dbResMetaMaster.getDbResourceType());
        if(dbResourceType != null) {
            switch(dbResourceType){
                case IMMUNIZATION:
                    resourceMaster.setResourceType(Resources.IMMUNIZATION);
                    break;
                case MEDICATION:
                    resourceMaster.setResourceType(Resources.MEDICATION);
                    break;
                case TOOL:
                    resourceMaster.setResourceType(Resources.TOOL);
                    break;
                case PROCEDURE:
                    resourceMaster.setResourceType(Resources.PROCEDURE);
                    break;
                case MEDICALCONDITION:
                    resourceMaster.setResourceType(Resources.MEDICALCONDITION);
                    break;
                case ALLERGY:
                    resourceMaster.setResourceType(Resources.ALLERGY);
                    break;
                case HOSPITAL:
                    resourceMaster.setResourceType(Resources.HOSPITAL);
                    break;
                case DOCTOR:
                    resourceMaster.setResourceType(Resources.DOCTOR);
                    break;
                case INSURANCE:
                    resourceMaster.setResourceType(Resources.INSURANCE);
                case TESTNAME:
                    resourceMaster.setResourceType(Resources.TESTNAME);
                case TPA:
                    resourceMaster.setResourceType(Resources.TPA);
                    break;
            }
        }
        resourceMaster.setResourceName(dbResMetaMaster.getDbResourceName());
        Map<String,String> rm = new HashMap<String,String>();
        for(NVPair np : dbResMetaMaster.getResourceMasterDetail()) {
            rm.put(np.getName(),np.getValue());
        }
        resourceMaster.setResourceDetail(rm);
        resourceMaster.setNotes(dbResMetaMaster.getNotes());

        return resourceMaster;
    }

    public void dropCollection() {
        getCollection().drop();
    }

}