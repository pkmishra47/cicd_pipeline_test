package org.apache.nifi.processors.daxoperation.db_helper;

import org.apache.nifi.processors.daxoperation.models.DbType;
import org.apache.nifi.processors.daxoperation.utils.LogUtil;

public class DBProcessor {
    private LogUtil logUtil = null;
    private DbType dbType = null;

    public DBProcessor(DbType dbType) {
        this.dbType = dbType;
    }

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public DbType getDbType() {
        return this.dbType;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public String getQueryToFetchFileInfo() {
        String query = "";
        query = "SELECT ${COLUMNS} FROM LAB.T_SAVEAUTOPRISMFILEPATH WHERE TRIM(UHID)=${UHID} AND TRIM(PATIENTIDENTIFIERNO)=${PATIENTIDENTIFIERNO} AND TRIM(FILETYPE) = ${FILETYPE} AND TRIM(FILEPATH) LIKE ${FILENAME}";
        return query;
    }

    public String getQueryToUpdateFileInfo() {
        String query = "";
        query = "UPDATE LAB.T_SAVEAUTOPRISMFILEPATH SET ${FIELDS_TO_UPDATE} WHERE ID=${ID}";
        return query;
    }
}
