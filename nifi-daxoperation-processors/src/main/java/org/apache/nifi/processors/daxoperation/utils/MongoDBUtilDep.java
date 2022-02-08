package org.apache.nifi.processors.daxoperation.utils;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.slf4j.event.Level;

import java.util.Map;

public class MongoDBUtilDep {
//    private static Logger log = LoggerFactory.getLogger(MongoDB.class);

    private static String dbName = "HHAppData";
    private static String dbServer = "127.0.0.1";
    private static String dbPort = "27017";
    private static String dbUser = null;
    private static String dbPassword = null;
    private static MongoDBUtilDep INSTANCE = new MongoDBUtilDep();
    private static MongoClient mongoClient = null;
    private static String dbUrl;
    private DB mDB = null;

    private static LogUtil logUtil;
    private static Map<String, Object> logMetaData;


    private Datastore db;

    public static MongoDBUtilDep getInstance() {
        return INSTANCE;
    }

    public String getDBName() {
        return dbName;
    }

    public static void setDbUrl(String url){
        logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                "setting db url", Level.INFO, null);
        dbUrl = url;
    }
    public static void setLogMetaData(Map<String, Object> lmd){
        logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                "setting log meta data", Level.INFO, null);
        logMetaData = lmd;
    }
    public MongoDBUtilDep() {
        try {
//            loadConfiguration();
            String dbUrl;
            if(dbUser == null){
                dbUrl = String.format("mongodb://%s/admin", dbUser, dbPassword, dbServer);
            }
            else{
                dbUrl = String.format("mongodb://%s:%s@%s/admin", dbUser, dbPassword, dbServer);
            }

//            log.info("The DB URL is:{}", dbUrl);
            logUtil.logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    "hello Guys From Mongo Util", Level.INFO, null);
            mongoClient = new MongoClient(new MongoClientURI(dbUrl));
            db = new Morphia().createDatastore(mongoClient, dbName);
            db.ensureIndexes();
            mDB = mongoClient.getDB(dbName);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing mongo db", e);
        }
//        log.info("Initialized mongoDB: {}", dbName);
    }

    public static Datastore getDb() {
        return INSTANCE.db;
    }

    public static DB getDB() {
        return INSTANCE.mDB;
    }

    public boolean collectionExists(String collectionName) {
        if(mDB != null)
            return mDB.collectionExists(collectionName);
        return false;
    }

//    private void loadConfiguration() {
//        try {
//            Properties props = new Properties();
//            props.load(new FileInputStream("appengine.properties"));
//            dbName = props.getProperty("appengine.db");
//            dbServer = props.getProperty("appengine.dbserver");
//            dbPort = props.getProperty("appengine.dbport");
//            dbUser = props.getProperty("appengine.dbuser");
//            dbPassword = props.getProperty("appengine.dbpassword");
//        } catch (Exception ex) {
////            log.error("Error while reading configuration", ex);
//            Runtime.getRuntime().exit(-1);
//        }
//    }
}
