package org.apache.nifi.processors.daxoperation.db_helper;

import org.apache.nifi.processors.daxoperation.models.DbType;
import org.apache.nifi.processors.daxoperation.utils.DateUtil;
import org.apache.nifi.processors.daxoperation.utils.LogUtil;

public class DBOps {
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;

    public LogUtil getLogUtil() {
        if (this.logUtil == null) {
            this.logUtil = new LogUtil();
        }
        return this.logUtil;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    public void setDateUtil(DateUtil dateUtil) {
        this.dateUtil = dateUtil;
    }

    public DbType findDbType(String dbConnUrl) {
        if (dbConnUrl.contains("sqlserver"))
            return DbType.SQL_SERVER;
        if (dbConnUrl.contains("snowflake"))
            return DbType.SNOWFLAKE;
        if (dbConnUrl.contains(("mysql")))
            return DbType.MYSQL;
        if (dbConnUrl.contains(("oracle")))
            return DbType.Oracle;
        return null;
    }
}