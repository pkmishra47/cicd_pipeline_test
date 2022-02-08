package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBBill;
import org.apache.nifi.processors.daxoperation.dbo.DBDax;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import java.util.List;

public class DaxDao extends BasicDAO<DBDax, ObjectId> {
    public DaxDao(MongoClient mongoClient) {
        super(DBDax.class, new MongoDBUtil(mongoClient).getDb());
    }

    public DBDax findEntityByName(String entityName) {
        DBDax dbDax = null;

        Query<DBDax> q = createQuery().field("entity_name").equal(entityName).limit(1);
        List<DBDax> dbDaxList = q.asList();
        if (dbDaxList.size() > 0) {
            dbDax = dbDaxList.get(0);
        }
        return dbDax;
    }
}
