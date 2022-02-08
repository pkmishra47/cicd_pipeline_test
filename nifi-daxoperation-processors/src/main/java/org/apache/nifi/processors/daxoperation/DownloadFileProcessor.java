package org.apache.nifi.processors.daxoperation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.bo.ImportData;
import org.apache.nifi.processors.daxoperation.dm.FileDownloadResponse;
import org.apache.nifi.processors.daxoperation.dm.ImportFile;
import org.apache.nifi.processors.daxoperation.dm.OcrRequestData;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.DateUtil;
import org.apache.nifi.processors.daxoperation.utils.GsonUtil;
import org.apache.nifi.processors.daxoperation.utils.LogUtil;
import org.apache.nifi.processors.daxoperation.utils.ServiceUtil;
import org.apache.nifi.stream.io.StreamUtils;
import org.json.JSONObject;
import org.slf4j.event.Level;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * @author Pradeep Mishra
 */
@Tags({"daxoperations", "downloadfile", "ocr"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class DownloadFileProcessor extends AbstractProcessor {
    private static final String processorName = "DownloadFileProcessor";
    private static final Charset charset = StandardCharsets.UTF_8;
    private LogUtil logUtil = null;
    private ServiceUtil serviceUtil = null;
    private String fileUrlFieldName = "";
    private DateUtil dateUtil = null;
    private static Gson gson = null;

    public String getProcessorName() {
        return DownloadFileProcessor.processorName;
    }

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public String getFileUrlFieldName() {
        return this.fileUrlFieldName;
    }

    public void setFileUrlFieldName(String fileUrlFieldName) {
        this.fileUrlFieldName = fileUrlFieldName;
    }

    public ServiceUtil getServiceUtil() {
        if (this.serviceUtil == null)
            this.serviceUtil = new ServiceUtil();
        return this.serviceUtil;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    public Gson getGson() {
        if (DownloadFileProcessor.gson == null)
            DownloadFileProcessor.gson = GsonUtil.getGson();
        return DownloadFileProcessor.gson;
    }

    public void setServiceUtil(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public static final PropertyDescriptor FileURL_Field_Name = new PropertyDescriptor
            .Builder()
            .name("FileURL_Field_Name")
            .displayName("FileURL_Field_Name")
            .description("mention field name containing file url available in Json received")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("marks process successful when processor achieves success condition.")
            .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("FAILURE")
            .description("marks process failure when processor achieves failure condition.")
            .build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        this.descriptors = List.of(FileURL_Field_Name);
        this.relationships = Set.of(REL_SUCCESS, REL_FAILURE);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        fileUrlFieldName = context.getProperty(FileURL_Field_Name).toString();
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        FlowFile inputFF = session.get();
        long startTime = this.getDateUtil().getCurrentEpochInMillis();
        String fileName = "";
        String type = "recommendation";

        try {
            logMetaData.put("processor_name", this.getProcessorName());

            if (inputFF == null)
                throw new Exception("Exception occurred while processing");

            String ffContent = readFlowFileContent(inputFF, session);
            JSONObject jsonContent = new JSONObject(ffContent);

            if (jsonContent.get(this.getFileUrlFieldName()) == null || jsonContent.get(this.getFileUrlFieldName()).toString().isEmpty())
                throw new Exception("Invalid field value");

            String fileUrl = jsonContent.get(this.getFileUrlFieldName()).toString();
            if (jsonContent.get("type") != null && jsonContent.get("type").toString().equals("topScores"))
                type = "topScores";

            fileName = fileUrl;
            logMetaData.put("fileName", fileUrl);
            FileDownloadResponse details = this.getServiceUtil().getFileContentResponse(fileUrl, logMetaData);
            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "downloading file from: " + fileUrl, Level.INFO, null);

            if (details != null && details.getContent() != null && details.getFileName() != null && details.getFileType() != null) {
                session.putAttribute(inputFF, "fileName", details.getFileName());
                session.putAttribute(inputFF, "fileType", details.getFileType());

                ImportFile iFile = new ImportFile();
                iFile.setFileName(details.getFileName());
                iFile.setFileType(details.getFileType());
                iFile.setContent(details.getContent());

                ObjectMapper mapper = new ObjectMapper();
                OcrRequestData ocrRequestData = new OcrRequestData();
                ocrRequestData.setData(compress(mapper.writeValueAsString(iFile)));
                ocrRequestData.setType(type);

                inputFF = updateFlowFile(session, inputFF, this.getGson().toJson(ocrRequestData));
                session.transfer(inputFF, REL_SUCCESS);
            } else
                throw new Exception("Invalid file content fetched");
            log_site_processing_time(startTime, fileName, logMetaData);
        } catch (Exception ex) {
            session.transfer(inputFF, REL_FAILURE);
        }
    }

    private void log_site_processing_time(long startTime, String fileName, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        long endTime = this.getDateUtil().getCurrentEpochInMillis();
        long duration = endTime - startTime;
        otherDetails.put("execution_duration", duration);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, fileName + ", execution_duration: " + duration, Level.INFO, otherDetails);
    }

    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, in -> StreamUtils.fillBuffer(in, buffer));

        return new String(buffer, charset);
    }

    public String compress(String data) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes("UTF8"));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out);

        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0)
            gzipOutputStream.write(buf, 0, len);

        in.close();
        out.close();
        gzipOutputStream.finish();
        gzipOutputStream.close();

        return Base64.encodeBase64String(out.toByteArray());
    }

    private FlowFile updateFlowFile(ProcessSession session, FlowFile ff, String data) {
        ff = session.write(ff, out -> out.write(data.getBytes(charset)));
        return ff;
    }
}

