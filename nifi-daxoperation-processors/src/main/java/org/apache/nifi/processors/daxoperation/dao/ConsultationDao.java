package org.apache.nifi.processors.daxoperation.dao;

import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.dbo.DBConsultation;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;

public class ConsultationDao extends BasicDAO<DBConsultation, ObjectId> {
    public ConsultationDao(MongoClient mongoClient) {
        super(DBConsultation.class, new MongoDBUtil(mongoClient).getDb());
        ensureIndexes();
    }
}
