package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBSugarInfo;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author developer
 */

@Component("SugarInfoDao")
public class SugarInfoDao extends BasicDAO<DBSugarInfo, ObjectId> {
    @Autowired
    IdentityDao identityDao;

    public SugarInfoDao(MongoClient mongoClient) {
        super(DBSugarInfo.class, new MongoDBUtil(mongoClient).getDb());
        ensureIndexes();
    }
}