package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBEntity;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("entityDao")
public class EntityDao extends BasicDAO<DBEntity, ObjectId> {
    private static Logger log = LoggerFactory.getLogger(EntityDao.class);

    public EntityDao(MongoClient mongoClient) {
        super(DBEntity.class, new MongoDBUtil(mongoClient).getDb());
    }

    public void dropCollection() {
        getCollection().drop();
    }

    public Map<String, DBEntity> getEntitys() {
        log.info("getting into getEntitys");
        Map<String, DBEntity> dbEntityMap = new HashMap<String, DBEntity>();
        for (DBEntity dbEntity : find().asList()) {
            dbEntityMap.put(dbEntity.getShortName(), dbEntity);
        }
        log.info("The entity Map is: {}", dbEntityMap);
        return dbEntityMap;
    }

    public DBEntity getEntity(String entityName) {
        DBEntity dbEntity = null;
        if (entityName != null)
            dbEntity = createQuery().filter("shortName", entityName).get();

        return dbEntity;
    }

    public List<DBEntity> getAll() {
        return find().asList();
    }
}