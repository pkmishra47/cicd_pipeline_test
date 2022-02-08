package org.apache.nifi.processors.daxoperation.utils;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.nifi.processors.daxoperation.dm.FileDownloadResponse;
import org.apache.nifi.processors.daxoperation.dm.PrismProfileResponse;
import org.apache.nifi.processors.daxoperation.dm.ProcessorInfoResponse;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.json.JSONObject;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;

public class ServiceUtil {
    private LogUtil logUtil = null;
    private static Gson gson = null;

    public LogUtil getLogUtil() {
        if (this.logUtil == null) {
            this.logUtil = new LogUtil();
        }
        return this.logUtil;
    }

    public Gson getGson() {
        if (ServiceUtil.gson == null)
            ServiceUtil.gson = new Gson();
        return ServiceUtil.gson;
    }

    public CloseableHttpClient getHttpClient() {
        return HttpClients.createDefault();
    }

    public boolean update247PrismProfileInfo(String apiUrl, String apiKey, String strPrismProfileInfo, Map<String, Object> logMetaData) {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        String strResponseData = "";
        boolean updateStatus = true;
//        Map<String, Object> otherDetails = new HashMap<>();
//        otherDetails.put("body", strPrismProfileInfo);

        try {
            httpclient = getHttpClient();
            HttpPost request = new HttpPost(apiUrl);
            request.setHeader("Content-Type", "application/json;charset=UTF-8");
            request.setHeader("apiKey", apiKey);
            StringEntity entity = new StringEntity(strPrismProfileInfo);
            request.setEntity(entity);
            response = httpclient.execute(request);
            strResponseData = EntityUtils.toString(response.getEntity());
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "247PrismProfileInfo response: " + strResponseData, Level.INFO, null);
            PrismProfileResponse prismProfileResponse = this.getGson().fromJson(strResponseData, PrismProfileResponse.class);
            updateStatus = prismProfileResponse.success;
            response.close();
            httpclient.close();
        } catch (Exception e) {
            updateStatus = false;
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }
        return updateStatus;
    }

    public ProcessorInfoResponse getCurrentProcessorDetails(String daxNifiApiDomnain, String processorID, Map<String, Object> logMetaData) {
        String apiUrl = daxNifiApiDomnain + "/nifi-api/processors/" + processorID;
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        ProcessorInfoResponse processorInfoResponse = null;
        String strResponseData = "";
        Map<String, Object> otherDetails = new HashMap<>();

        try {
            httpclient = getHttpClient();
            HttpGet request = new HttpGet(apiUrl);
            response = httpclient.execute(request);
            strResponseData = EntityUtils.toString(response.getEntity());
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "current processor details response: " + strResponseData, Level.INFO, otherDetails);
            processorInfoResponse = this.getGson().fromJson(strResponseData, ProcessorInfoResponse.class);
            response.close();
            httpclient.close();
        } catch (Exception e) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }

        return processorInfoResponse;
    }

    public void sendStopRequestToCurrentProcessor(String daxNifiApiDomnain, String requestBody, String processorID, Map<String, Object> logMetaData) {
        String apiUrl = daxNifiApiDomnain + "/nifi-api/processors/" + processorID + "/run-status";
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        String strResponseData = "";
        Map<String, Object> otherDetails = new HashMap<>();

        try {
            httpclient = getHttpClient();
            HttpPut request = new HttpPut(apiUrl);
            request.setHeader("Content-Type", "application/json;charset=UTF-8");
            StringEntity entity = new StringEntity(requestBody);
            request.setEntity(entity);
            response = httpclient.execute(request);
            strResponseData = EntityUtils.toString(response.getEntity());
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "sent stop request to current running processor, received response: " + strResponseData, Level.INFO, otherDetails);
            response.close();
            httpclient.close();
        } catch (Exception e) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }
    }

    public FileDownloadResponse getFileContentResponse(String fileUrl, Map<String, Object> logMetaData) throws Exception {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        FileDownloadResponse fileDownloadResponse = null;

        httpclient = getHttpClient();
        HttpGet request = new HttpGet(fileUrl);
        response = httpclient.execute(request);
        HttpEntity httpEntity = response.getEntity();

        if (response.getStatusLine().getStatusCode() != 200)
            throw new Exception("not able to download file from the link provided");

        byte[] data = EntityUtils.toByteArray(httpEntity);
        if (data.length == 0)
            throw new Exception("file is empty");

        ContentType contentType = ContentType.get(httpEntity);
        String mimeType = contentType.getMimeType();

        fileDownloadResponse = new FileDownloadResponse();
        fileDownloadResponse.setContent(data);
        fileDownloadResponse.setFileName(fileUrl);
        fileDownloadResponse.setFileType(mimeType);

        response.close();
        httpclient.close();

        return fileDownloadResponse;
    }

    public String generateDeeplink(String apiUrl, String apiKey, String strDeeplinkRequest, Map<String, Object> logMetaData) {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        String strResponseData = "";
        String outputDeeplink = "";
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("requestBody", strDeeplinkRequest);

        try {
            httpclient = getHttpClient();
            HttpPost request = new HttpPost(apiUrl);
            request.setHeader("Content-Type", "application/json;charset=UTF-8");
            request.setHeader("Authorization", apiKey);
            StringEntity entity = new StringEntity(strDeeplinkRequest);
            request.setEntity(entity);
            response = httpclient.execute(request);

            strResponseData = EntityUtils.toString(response.getEntity());
            otherDetails.put("statusCode", response.getStatusLine().getStatusCode());
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "generateDeeplink response: " + strResponseData, Level.INFO, otherDetails);

            if (response.getStatusLine().getStatusCode() == 200)
                outputDeeplink = strResponseData;

            response.close();
            httpclient.close();
        } catch (Exception e) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }

        return outputDeeplink;
    }

    public boolean sendProhealthInfoToHMDashboard(String apiUrl, String apiKey, String strProhealthRequest, Map<String, Object> logMetaData) {
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        String strResponseData = "";
        boolean updateStatus = false;

        try {
            httpclient = getHttpClient();
            HttpPost request = new HttpPost(apiUrl);
            request.setHeader("Content-Type", "application/json;charset=UTF-8");
            request.setHeader("Authorization", apiKey);

            StringEntity entity = new StringEntity(strProhealthRequest);
            request.setEntity(entity);
            response = httpclient.execute(request);
            strResponseData = EntityUtils.toString(response.getEntity());
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "HMDashboard API response: " + strResponseData, Level.INFO, null);
            if (response.getStatusLine().getStatusCode() == 201)
                updateStatus = true;
            else
                logMetaData.put("HMDashboard_Response", strResponseData);

            response.close();
            httpclient.close();
        } catch (Exception e) {
            updateStatus = false;
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }
        return updateStatus;
    }

    public boolean triggerPrismCarePersonaFlow(String apiDomain, String apiKey, String strConsultationId, Map<String, Object> logMetaData) {
        String apiUrl = apiDomain + "/data/carepersona/save?accessToken=" + apiKey + "&consultationid=" + strConsultationId;
        CloseableHttpResponse response = null;
        CloseableHttpClient httpclient = null;
        String strResponseData = "";
        boolean isSuccess = false;

        try {
            httpclient = getHttpClient();
            HttpGet request = new HttpGet(apiUrl);
            response = httpclient.execute(request);
            strResponseData = EntityUtils.toString(response.getEntity());
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Prism API Response: " + strResponseData, Level.INFO, null);
            JSONObject jsonContent = new JSONObject(strResponseData);
            if (response.getStatusLine().getStatusCode() == 200 && jsonContent.get("errorCode") != null && jsonContent.get("errorCode").toString().equals("0")) {
                isSuccess = true;
            }
            response.close();
            httpclient.close();
        } catch (Exception e) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }
        logMetaData.put("isTriggerSuccess", isSuccess);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "Prism API Trigger:  " + (isSuccess ? "Succeed" : "Failed"), Level.INFO, null);
        return isSuccess;
    }
}
