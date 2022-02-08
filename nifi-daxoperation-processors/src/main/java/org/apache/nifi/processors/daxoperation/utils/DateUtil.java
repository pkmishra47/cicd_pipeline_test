package org.apache.nifi.processors.daxoperation.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private DateTimeFormatter timestampFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
    private DateTimeFormatter dateFormat1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter dateFormat2 = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private DateTimeFormatter csvDateFormat = DateTimeFormatter.ofPattern("ddMMyyyy");
    private DateTimeFormatter oracleformat = DateTimeFormatter.ofPattern("dd/MM/yyyy-HH:mm:ss");
    private DateTimeFormatter sqlformat = DateTimeFormatter.ofPattern("dd-MMMM-yyyy HH:mm:ss");
    private DateTimeFormatter mysqlformat = DateTimeFormatter.ofPattern("YYYY-MM-DD");
    private final String ASIA_KOLKATA = "Asia/Kolkata";

    public DateTimeFormatter getTimestampTextFormat() {
        return this.timestampFormat;
    }

    public DateTimeFormatter getTimeTextFormat() {
        return this.timeFormat;
    }

    public DateTimeFormatter getDateFormat() {
        return this.dateFormat1;
    }

    public DateTimeFormatter getDateFormat2() {
        return this.dateFormat2;
    }

    public DateTimeFormatter getCsvDateFormat() {
        return this.csvDateFormat;
    }

    public DateTimeFormatter getOracleformat() {
        return oracleformat;
    }

    public DateTimeFormatter getSqlformat() {
        return sqlformat;
    }

    public DateTimeFormatter getMysqlformat() {
        return mysqlformat;
    }

    public LocalDate getTodayDate() {
        return LocalDate.now(ZoneId.of(ASIA_KOLKATA));
    }

    public boolean isValidDate(String strDate) {
        boolean result = true;
        DateTimeFormatter formatter = this.getDateFormat();

        if (strDate.isEmpty()) {
            return true;
        }

        try {
            formatter.parse(strDate);
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    public boolean isValidTime(String strTime) {
        boolean result = true;
        DateTimeFormatter formatter = this.getTimeTextFormat();

        try {
            formatter.parse(strTime);
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    public LocalDate getParsedDate(String strDate) {
        LocalDate date = null;

        try {
            date = LocalDate.parse(strDate, this.getDateFormat());
        } catch (Exception e) {
            date = null;
        }
        return date;
    }

    public LocalTime getParsedTime(String strTime) {
        LocalTime time = null;

        try {
            time = LocalTime.parse(strTime, this.getTimeTextFormat());
        } catch (Exception e) {
            time = null;
        }
        return time;
    }

    public String getStrDate(LocalDate dt) {
        return dt.format(this.getDateFormat());
    }

    public String getCsvDate(LocalDate dt) {
        return dt.format(this.getCsvDateFormat());
    }

    public Long getEpochTimeInSecond() {
        return Instant.now().getEpochSecond();
    }

    public Long getCurrentEpochInMillis() {
        return Instant.now().toEpochMilli();
    }
}