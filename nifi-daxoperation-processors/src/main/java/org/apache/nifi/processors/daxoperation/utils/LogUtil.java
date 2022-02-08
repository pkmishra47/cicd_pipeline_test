package org.apache.nifi.processors.daxoperation.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.slf4j.event.Level;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class LogUtil {
    private final String ASIA_KOLKATA = "Asia/Kolkata";
    private Gson gson = null;

    public Gson getGson() {
        if (this.gson == null)
            this.gson = new GsonBuilder().serializeNulls().create();
        return this.gson;
    }

    public void logMessage(Map<String, Object> logMetaData, LogStatus logStatus, LogType logType, String message, Level level, Map<String, Object> otherDetails) {
        try {
            HashMap<String, Object> logDetails = new HashMap<>();
            logDetails.putAll(logMetaData);
            logDetails.put("log_date", (LocalDate.now(ZoneId.of(ASIA_KOLKATA)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            logDetails.put("log_datetime", (LocalDateTime.now(ZoneId.of(ASIA_KOLKATA)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))));
            logDetails.put("log_timezone", ASIA_KOLKATA);
            logDetails.put("app", "dax");
            logDetails.put("status", logStatus.toString());
            logDetails.put("log_type", logType.toString());
            logDetails.put("message", message);
            logDetails.put("level", level);

            if (otherDetails != null && !otherDetails.isEmpty())
                logDetails.putAll(otherDetails);

            System.out.println(this.getGson().toJson(logDetails));
        } catch (Exception ex) {
            StringWriter stm = new StringWriter();
            PrintWriter wrt = new PrintWriter(stm);
            ex.printStackTrace(wrt);
            wrt.close();
            System.out.println(stm.toString());
        }
    }
}
