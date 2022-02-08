package org.apache.nifi.processors.daxoperation.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import org.apache.nifi.processors.daxoperation.bo.Notification;
import org.apache.nifi.processors.daxoperation.bo.NotificationLevel;
import org.apache.nifi.processors.daxoperation.bo.NotificationType;
import org.apache.nifi.processors.daxoperation.dbo.DBNotification;
import org.apache.nifi.processors.daxoperation.dbo.DBUser;
import org.apache.nifi.processors.daxoperation.utils.MongoDBUtil;
import org.bson.types.ObjectId;
import org.mongodb.morphia.dao.BasicDAO;
import org.mongodb.morphia.query.Query;

public class NotificationDao extends BasicDAO<DBNotification, ObjectId> {

//	private static Logger log = LoggerFactory.getLogger(NotificationDao.class);

    public NotificationDao(MongoClient mongoClient) {
        super(DBNotification.class, new MongoDBUtil(mongoClient).getDb());
        ensureIndexes();
    }

    public List<DBNotification> getNotifications(String userId) {
        List<DBNotification> notificationList = new ArrayList<DBNotification>();

        if (userId == null)
            return notificationList;

        List<NotificationType> notificationTypeList = new ArrayList<NotificationType>();
        notificationTypeList.add(NotificationType.LabTest);
        notificationTypeList.add(NotificationType.DischargeSummary);
        notificationTypeList.add(NotificationType.Prescription);
        notificationTypeList.add(NotificationType.HealthCheck);
        notificationTypeList.add(NotificationType.Procedure);

        List<NotificationLevel> notificationLevelList = new ArrayList<NotificationLevel>();
        notificationLevelList.add(NotificationLevel.SupportAdmin);
        notificationLevelList.add(NotificationLevel.System);

        Query<DBNotification> q = createQuery().disableValidation()
                .filter("userId", userId)
                .field("notificationType").in(notificationTypeList)
                .field("notificationLevel").in(notificationLevelList).limit(500).order("-_id");

//		log.info("The query is: {}", q);
        notificationList = q.asList();

        return notificationList;
    }

    public void addNotification(DBUser dbUser, String notificationMessage, NotificationLevel notificationLevel,
                                NotificationType notificationType, String siteKey, DBUser initiatedBy, String uhid) {
        DBNotification dbNotification = new DBNotification();
        if (dbUser != null) {
            dbNotification.setUserId(dbUser.getId().toString());
            dbNotification.setUserName(getUserName(dbUser));
        }
        dbNotification.setMessage(notificationMessage);
        dbNotification.setNotificationDate(new Date());
        dbNotification.setNotificationLevel(notificationLevel);
        dbNotification.setNotificationType(notificationType);
        dbNotification.setSiteKey(siteKey);
        dbNotification.setIsRead(false);
        if (initiatedBy != null) {
            dbNotification.setInitiatedUserId(initiatedBy.getId().toString());
            dbNotification.setInitiatedUserName(getUserName(initiatedBy));
        }
        dbNotification.setUhid(uhid);
        save(dbNotification);
    }

    /*
     * Note: We could even implicitly invoke this action from getNotification if our application
     * does not demand too much of consistency
     */
    public void updateNotification(List<DBNotification> notificationList) {
        for (DBNotification dbNotification : notificationList) {
            dbNotification.setIsRead(true);
            save(dbNotification);
        }
        // Have to make the isRead on all these notification to true
    }

    public List<DBNotification> findNotifications(NotificationLevel level, NotificationType type,
                                                  long fromDate, long toDate, List<String> siteKeyList, String uhid) {
        Query<DBNotification> query;
        if (uhid != null && uhid.trim().length() != 0) {
            query = createQuery()
                    .field("notificationDate").greaterThanOrEq(new Date(fromDate))
                    .field("notificationDate").lessThanOrEq(new Date(toDate))
                    .field("notificationLevel").equal(level)
                    .field("notificationType").equal(type)
                    .field("uhid").equal(uhid)
                    .field("siteKey").in(siteKeyList).limit(500).order("-_id");
        } else {
            query = createQuery()
                    .field("notificationDate").greaterThanOrEq(new Date(fromDate))
                    .field("notificationDate").lessThanOrEq(new Date(toDate))
                    .field("notificationLevel").equal(level)
                    .field("notificationType").equal(type)
                    .field("siteKey").in(siteKeyList).limit(500).order("-_id");
        }

//		log.info("The query is: {}", query);
        return query.asList();
    }

    public static Notification newNotification(DBNotification dbNotification) {
        Notification notification = new Notification();

        if (dbNotification == null)
            return notification;

        if (dbNotification.getInitiatedUserId() != null)
            notification.setInitiatedBy(dbNotification.getInitiatedUserName());
        else
            notification.setInitiatedBy("System");

        notification.setMessage(dbNotification.getMessage());
        notification.setSiteKey(dbNotification.getSiteKey());
        notification.setNotificationDate(dbNotification.getNotificationDate());

        if (dbNotification.getUserId() != null)
            notification.setUser(dbNotification.getUserName());
        else
            notification.setUser("System");

        notification.setNotificationLevel(dbNotification.getNotificationLevel());
        notification.setNotificationType(dbNotification.getNotificationType());

        return notification;
    }

    private String getUserName(DBUser dbUser) {
        if (dbUser == null)
            return "";

        String firstName = dbUser.getFirstName() != null ? dbUser.getFirstName().trim() : "";
        String lastName = dbUser.getLastName() != null ? dbUser.getLastName().trim() : "";
        String middleName = dbUser.getMiddleName() != null ? dbUser.getMiddleName().trim() : "";

        String userName = "";
        if (middleName.isEmpty())
            userName = firstName + " " + lastName;
        else
            userName = firstName + " " + middleName + " " + lastName;

        if (dbUser.getMobileNumber() != null && dbUser.getMobileNumber().trim().length() != 0)
            userName = userName + "(" + dbUser.getMobileNumber() + ")";

        return userName;
    }
}