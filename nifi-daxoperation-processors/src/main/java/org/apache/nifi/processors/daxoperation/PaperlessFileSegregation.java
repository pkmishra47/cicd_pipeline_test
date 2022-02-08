package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
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
import org.apache.nifi.processors.daxoperation.dm.PaperlessContent;
import org.apache.nifi.processors.daxoperation.dm.UnoFFInput;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Pradeep Mishra
 */
@Tags({"daxoperation", "dataman", "paperless", "file", "segregation"})
@CapabilityDescription("")
@SeeAlso()
@ReadsAttributes({@ReadsAttribute(attribute = "")})
@WritesAttributes({@WritesAttribute(attribute = "")})

public class PaperlessFileSegregation extends AbstractProcessor {
    private static final String processorName = "PaperlessFileSegregation";
    private static final Charset charset = StandardCharsets.UTF_8;
    private LogUtil logUtil = null;
    private static Gson gson = null;
    private DateUtil dateUtil = null;

    public String getProcessorName() {
        return PaperlessFileSegregation.processorName;
    }

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public Gson getGson() {
        if (PaperlessFileSegregation.gson == null)
            PaperlessFileSegregation.gson = GsonUtil.getGson();
        return PaperlessFileSegregation.gson;
    }

    public static final Relationship LabReport = new Relationship.Builder()
            .name("LabResult")
            .description("pass result to LabResult relationship.")
            .build();

    public static final Relationship Bills = new Relationship.Builder()
            .name("Bills")
            .description("pass result to Bills relationship.")
            .build();

    public static final Relationship HealthCheck = new Relationship.Builder()
            .name("HealthCheck")
            .description("pass result to HealthCheck relationship.")
            .build();

    public static final Relationship Unmatched = new Relationship.Builder()
            .name("Unmatched")
            .description("pass result to Unmatched relationship incase there is no match.")
            .build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        this.relationships = Set.of(LabReport, Bills, HealthCheck, Unmatched);
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
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        FlowFile inputFF = session.get();
        String fileName;
        long startTime;

        try {
            logMetaData.put("processor_name", this.getProcessorName());
            startTime = this.getDateUtil().getEpochTimeInSecond();

            if (inputFF == null)
                throw new Exception(Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE);

            String ffContent = readFlowFileContent(inputFF, session);
            UnoFFInput ffinput = this.getGson().fromJson(ffContent, UnoFFInput.class);

            if (ffinput.data == null || (ffinput.data).isEmpty()) {
                logMetaData.put("flowfile_content", ffContent);
                throw new Exception("Invalid flowfile content. Values are either null or blank.");
            }

            PaperlessContent paperlessContent = extractData(ffinput.data);
            if (paperlessContent.fileName == null || (paperlessContent.fileName).isEmpty()) {
                logMetaData.put("fileName", paperlessContent.fileName);
                throw new Exception("Paperless pdf file name is either empty or null");
            }

            fileName = paperlessContent.fileName;
            Long executionID = ffinput.executionID != null ? Long.parseLong(ffinput.executionID) : null;
            logMetaData.put("execution_id", executionID);
            logMetaData.put("fileName", fileName);

            List<String> fileNameAttributes = Arrays.asList(fileName.split("_"));

            if (isLabReportFile(fileNameAttributes))
                transferFileToLabReport(session, ffContent, fileName);
            else if (isBillsFile(fileNameAttributes))
                transferFileToBills(session, ffContent, fileName);
            else if (isHealthCheckFile(fileNameAttributes))
                transferFileToHealthChecks(session, ffContent, fileName);
            else
                transferFileToUnmatched(session, ffContent, fileName);

        } catch (Exception ex) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Utility.stringifyException(ex), Level.ERROR, null);
            session.transfer(session.create(inputFF), Unmatched);
            return;
        }

        long endTime = this.getDateUtil().getEpochTimeInSecond();
        log_site_processing_time(endTime - startTime, fileName, logMetaData);
        session.remove(inputFF);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS, "Activity Completed Successfully", Level.INFO, null);
    }

    private void log_site_processing_time(Long duration, String fileName, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        otherDetails.put("execution_duration", duration);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, "paperless pdf : " + fileName + " routed successfully.", Level.INFO, otherDetails);
    }

    private String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, in -> StreamUtils.fillBuffer(in, buffer));

        return new String(buffer, charset);
    }

    private PaperlessContent extractData(String data) throws IOException {
        String actualData = decompress(data);
        return this.getGson().fromJson(actualData, PaperlessContent.class);
    }

    private String decompress(String data) throws IOException {
        byte[] base64 = Base64.decodeBase64(data);
        GZIPInputStream gzipInputStream = new GZIPInputStream(new ByteArrayInputStream(base64));
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte[] buf = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buf)) > 0)
            out.write(buf, 0, len);

        gzipInputStream.close();
        out.close();

        return StringUtils.newStringUtf8(out.toByteArray());
    }

    private boolean isLabReportFile(List<String> fileNameAttributes) {
        boolean status = true;
        if (!(fileNameAttributes.size() == 9 || fileNameAttributes.size() == 10))
            status = false;

        String[] uhid = fileNameAttributes.get(0).split("\\.");
        if (!(uhid.length == 2) || !(uhid[0].matches("^[A-Z].*$")))
            status = false;

        if (fileNameAttributes.size() == 10 && !fileNameAttributes.get(8).equals("SR"))
            status = false;

        return status;
    }

    private boolean isBillsFile(List<String> fileNameAttributes) {
        boolean status = true;
        if (fileNameAttributes.size() != 7)
            status = false;

        String[] uhid;
        if (fileNameAttributes.size() >= 5)
            uhid = fileNameAttributes.get(4).split("\\.");
        else
            return false;

        if (!(uhid.length == 2) || !(uhid[0].matches("^[A-Z].*$")))
            status = false;

        return status;
    }

    private boolean isHealthCheckFile(List<String> fileNameAttributes) {
        // UHID_PackageName_BillNo_Locationid_YYYYDDMMHHMMSS
        boolean status = true;
        if (fileNameAttributes.size() != 5)
            status = false;

        String[] uhid = fileNameAttributes.get(0).split("\\.");
        if (!(uhid.length == 2) || !(uhid[0].matches("^[A-Z].*$")))
            status = false;

        return status;
    }

    private void transferFileToLabReport(ProcessSession session, String ffContent, String fileName) {
        FlowFile outputFF = session.create();
        outputFF = writeContentToOutputSession(session, outputFF, ffContent);
        session.putAttribute(outputFF, FlowFileAttributes.attachmentName, fileName);
        session.transfer(outputFF, LabReport);
    }

    private void transferFileToBills(ProcessSession session, String ffContent, String fileName) {
        FlowFile outputFF = session.create();
        outputFF = writeContentToOutputSession(session, outputFF, ffContent);
        session.putAttribute(outputFF, FlowFileAttributes.attachmentName, fileName);
        session.transfer(outputFF, Bills);
    }

    private void transferFileToHealthChecks(ProcessSession session, String ffContent, String fileName) {
        FlowFile outputFF = session.create();
        outputFF = writeContentToOutputSession(session, outputFF, ffContent);
        session.putAttribute(outputFF, FlowFileAttributes.attachmentName, fileName);
        session.transfer(outputFF, HealthCheck);
    }

    private void transferFileToUnmatched(ProcessSession session, String ffContent, String fileName) {
        FlowFile outputFF = session.create();
        outputFF = writeContentToOutputSession(session, outputFF, ffContent);
        session.putAttribute(outputFF, FlowFileAttributes.attachmentName, fileName);
        session.transfer(outputFF, Unmatched);
    }

    private FlowFile writeContentToOutputSession(ProcessSession session, FlowFile outputFF, String flowFileContent) {
        outputFF = session.write(outputFF, out -> out.write(flowFileContent.getBytes(charset)));
        return outputFF;
    }
}
