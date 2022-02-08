package org.apache.nifi.processors.daxoperation.utils;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apache.nifi.processors.daxoperation.dbo.DBDietPlan;
import org.apache.nifi.processors.daxoperation.dbo.DBProtonDetails;
import org.apache.nifi.processors.daxoperation.dbo.DBSugarInfo;
import org.apache.nifi.processors.daxoperation.dbo.DBTool;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.util.Map;

public class MongoDBUtil {

    private static String dbName = "HHAppData";
    private String dbUrl;
    private DB mDB = null;
    private Datastore db;

    public String getDBName() {
        return dbName;
    }

    public void setDbUrl(String url) {
        dbUrl = url;
        System.out.println("setting db url");
    }

    public MongoDBUtil(MongoClient mongoClient) {
        try {
            Morphia morphia = new Morphia();
//            morphia.mapPackage("org.apache.nifi.processors.daxoperation.dbo" );
            morphia.map(DBTool.class);
            morphia.map(DBSugarInfo.class);
            morphia.map(DBProtonDetails.class);
            morphia.map(DBDietPlan.class);
            db = morphia.createDatastore(mongoClient, dbName);
            db.ensureIndexes();
            mDB = mongoClient.getDB(dbName);
        } catch (Exception e) {
            System.out.println("Error : " + Utility.stringifyException(e));
            throw new RuntimeException("Error initializing mongo db", e);
        }
    }

    public Datastore getDb() {
        return db;
    }

    public DB getDB() {
        return mDB;
    }

    public boolean collectionExists(String collectionName) {
        if (mDB != null)
            return mDB.collectionExists(collectionName);
        return false;
    }
}
