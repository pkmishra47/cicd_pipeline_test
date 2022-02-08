package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBBill;
import org.apache.nifi.processors.daxoperation.dbo.DBCarePersona;
import org.apache.nifi.processors.daxoperation.dbo.DBIdentity;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

import java.util.ArrayList;
import java.util.List;

public class CarePersonaDao extends BasicDAO<DBCarePersona, ObjectId> {
    public CarePersonaDao(MongoClient mongoClient) {
        super(DBCarePersona.class, new MongoDBUtil(mongoClient).getDb());
    }

    public List<DBCarePersona> findCarePersonaPrescription(String mobileNumber, String prescribedBy) {
        List<DBCarePersona> dbCarePersonaList = new ArrayList<DBCarePersona>();

        Query<DBCarePersona> q = createQuery().filter("mobileNumber", mobileNumber).filter("prescribedBy", prescribedBy);
        dbCarePersonaList = q.asList();

        return dbCarePersonaList;
    }
}
