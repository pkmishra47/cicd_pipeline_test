package org.apache.nifi.processors.daxoperation;

import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.DateUtil;
import org.apache.nifi.processors.daxoperation.utils.LogUtil;
import org.apache.nifi.processors.daxoperation.utils.ServiceUtil;
import org.apache.nifi.processors.daxoperation.utils.Utility;
import org.json.JSONObject;
import org.slf4j.event.Level;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PrismAPICallProcessor extends AbstractProcessor {
    private static final String processorName = "PrismAPICallProcessor";
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private ServiceUtil serviceUtil = null;
    private String prismAPIDomain = "";
    private String prismAPIKey = "";
    private Map<String, Object> logMetaData = new HashMap<>();

    public static final PropertyDescriptor PRISM_API_DOMAIN = new PropertyDescriptor
            .Builder()
            .name("PRISM_API_DOMAIN")
            .displayName("PRISM_API_DOMAIN")
            .description("PRISM_API_DOMAIN")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final PropertyDescriptor PRISM_API_KEY = new PropertyDescriptor
            .Builder()
            .name("PRISM_API_KEY")
            .displayName("PRISM_API_KEY")
            .description("PRISM_API_KEY")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship")
            .build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship")
            .build();

    private static final Charset charset = StandardCharsets.UTF_8;

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

    public ServiceUtil getServiceUtil() {
        if (this.serviceUtil == null)
            this.serviceUtil = new ServiceUtil();
        return this.serviceUtil;
    }

    public void setServiceUtil(ServiceUtil serviceUtil) {
        this.serviceUtil = serviceUtil;
    }

    public String getPrismAPIDomain() {
        return this.prismAPIDomain;
    }

    public void setPrismAPIDomain(String prismAPIDomain) {
        this.prismAPIDomain = prismAPIDomain;
    }

    public String getPrismAPIKey() {
        return this.prismAPIKey;
    }

    public void setPrismAPIKey(String prismAPIKey) {
        this.prismAPIKey = prismAPIKey;
    }

    @Override
    protected void init(final ProcessorInitializationContext context) {
        this.descriptors = List.of(PRISM_API_DOMAIN, PRISM_API_KEY);
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
        prismAPIDomain = context.getProperty(PRISM_API_DOMAIN).evaluateAttributeExpressions().getValue();
        prismAPIKey = context.getProperty(PRISM_API_KEY).evaluateAttributeExpressions().getValue();
    }

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        logMetaData.put("processor_name", processorName);
        FlowFile inputFF = session.get();

        try {
            String ffContent = Utility.readFlowFileContent(inputFF, session);
            JSONObject jsonContent = new JSONObject(ffContent);

            if (jsonContent.get("consultationId") != null && !jsonContent.get("consultationId").toString().isEmpty()) {
                String consultationId = jsonContent.get("consultationId").toString();
                logMetaData.put("consultationId", consultationId);
                boolean isSuccess = this.getServiceUtil().triggerPrismCarePersonaFlow(this.prismAPIDomain, this.prismAPIKey, consultationId, logMetaData);
                session.transfer(inputFF, REL_SUCCESS);
            }
        } catch (Exception ex) {
            markProcessorFailure(session, inputFF, logMetaData, null, Utility.stringifyException(ex));
        }
    }

    public void markProcessorFailure(ProcessSession session, FlowFile flowFile, Map<String, Object> logMetaData, Map<String, Object> otherLogDetails, String logMessage) {
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
    }
}