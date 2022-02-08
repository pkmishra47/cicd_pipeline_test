package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.processors.daxoperation.dm.FFInput;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

public class GenerateSiteDetails extends AbstractProcessor {
    private static final String processorName = "GenerateSiteDetails";
    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();
    private static final Charset charset = Charset.forName("UTF-8");

    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
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

    @Override
    protected void init(final ProcessorInitializationContext context) {
        List<PropertyDescriptor> initDescriptors = new ArrayList<>();
        this.descriptors = Collections.unmodifiableList(initDescriptors);

        Set<Relationship> initRelationships = new HashSet<>();
        initRelationships.add(REL_SUCCESS);
        initRelationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(initRelationships);
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
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        logMetaData.put("log_date", (this.getDateUtil().getTodayDate()).format(this.getDateUtil().getDateFormat()));
        Gson gson = GsonUtil.getGson();
        FlowFile flowFile = session.get();

        try {
            String ffContent = readFlowFileContent(flowFile, session);
            FFInput ffInput = gson.fromJson(ffContent, FFInput.class);
            sendOutPutFlowFiles(ffInput, session, flowFile);
        } catch (Exception ex) {
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS,
                    Utility.stringifyException(ex), Level.INFO, null);
            FlowFile output = getFlowFile(session, flowFile, Utility.stringifyException(ex), "");
            session.transfer(output, REL_FAILURE);
        }
        session.remove(flowFile);
    }

    public void sendOutPutFlowFiles(FFInput ffInput, ProcessSession session, FlowFile flowFile) {
        if (ffInput.patients != null && !ffInput.patients.isEmpty()) {
            FFInput out = new FFInput();
            out.patients = ffInput.patients;
            out.siteApiKey = flowFile.getAttribute("siteApiKey");
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out), "patient");
            session.transfer(output, REL_SUCCESS);
        }
        if (ffInput.specialResults != null && !ffInput.specialResults.isEmpty()) {
            FFInput out = new FFInput();
            out.specialResults = ffInput.specialResults;
            out.siteApiKey = flowFile.getAttribute("siteApiKey");
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out), "specialResults");
            session.transfer(output, REL_SUCCESS);
        }
        if (ffInput.dischargeSummaries != null && !ffInput.dischargeSummaries.isEmpty()) {
            FFInput out = new FFInput();
            out.dischargeSummaries = ffInput.dischargeSummaries;
            out.siteApiKey = flowFile.getAttribute("siteApiKey");
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out), "dischargesummary");
            session.transfer(output, REL_SUCCESS);
        }
        if (ffInput.prescriptions != null && !ffInput.prescriptions.isEmpty()) {
            FFInput out = new FFInput();
            out.prescriptions = ffInput.prescriptions;
            out.siteApiKey = flowFile.getAttribute("siteApiKey");
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out), "prescription");
            session.transfer(output, REL_SUCCESS);
        }
        if (ffInput.pharmacyBill != null && !ffInput.pharmacyBill.isEmpty()) {
            FFInput out = new FFInput();
            out.pharmacyBill = ffInput.pharmacyBill;
            out.siteApiKey = flowFile.getAttribute("siteApiKey");
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out), "pharmacybills");
            session.transfer(output, REL_SUCCESS);
        }
        if (ffInput.consultation != null && !ffInput.consultation.isEmpty()) {
            FFInput out = new FFInput();
            out.consultation = ffInput.consultation;
            out.siteApiKey = flowFile.getAttribute("siteApiKey");
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out), "consultations");
            session.transfer(output, REL_SUCCESS);
        }
        if (ffInput.tool != null && !ffInput.tool.isEmpty()) {
            FFInput out = new FFInput();
            out.tool = ffInput.tool;
            out.siteApiKey = flowFile.getAttribute("siteApiKey");
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out), "tools");
            session.transfer(output, REL_SUCCESS);
        }
        if (ffInput.prohealth != null && !ffInput.prohealth.isEmpty()) {
            FFInput out = new FFInput();
            out.prohealth = ffInput.prohealth;
            out.siteApiKey = flowFile.getAttribute("siteApiKey");
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out), "prohealth");
            session.transfer(output, REL_SUCCESS);
        }
        if (ffInput.clinicprohealth != null && !ffInput.clinicprohealth.isEmpty()) {
            FFInput out = new FFInput();
            out.clinicprohealth = ffInput.clinicprohealth;
            out.siteApiKey = flowFile.getAttribute("siteApiKey");
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out), "clinicprohealth");
            session.transfer(output, REL_SUCCESS);
        }
        if (ffInput.homehealthVisit != null && !ffInput.homehealthVisit.isEmpty()) {
            FFInput out = new FFInput();
            out.homehealthVisit = ffInput.homehealthVisit;
            out.siteApiKey = flowFile.getAttribute("siteApiKey");
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out), "homehealthvisits");
            session.transfer(output, REL_SUCCESS);
        }
        if (ffInput.dietPlan != null && !ffInput.dietPlan.isEmpty()) {
            FFInput out = new FFInput();
            out.dietPlan = ffInput.dietPlan;
            out.siteApiKey = flowFile.getAttribute("siteApiKey");
            FlowFile output = getFlowFile(session, flowFile, GsonUtil.getGson().toJson(out), "dietplans");
            session.transfer(output, REL_SUCCESS);
        }
    }

    public FlowFile getFlowFile(ProcessSession session, FlowFile ff, String data, String processorType) {
        FlowFile outputFF = session.create(ff);
        session.putAttribute(outputFF, "processorType", processorType);
        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            out.write(data.getBytes(charset));
        });
        return outputFF;
    }


    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        if (inputFF == null) {
            return null;
        }
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, new InputStreamCallback() {
            @Override
            public void process(InputStream in) throws IOException {
                StreamUtils.fillBuffer(in, buffer);
            }
        });

        return new String(buffer, charset);
    }
}
