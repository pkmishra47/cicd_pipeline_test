package org.apache.nifi.processors.daxoperation;

import com.google.gson.Gson;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.slf4j.event.Level;

import java.nio.charset.Charset;
import java.util.*;

public class HoldFlowFile extends AbstractProcessor {
    static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("On successful instance creation, flow file is routed to 'success' relationship").build();
    static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("On instance creation failure, flow file is routed to 'failure' relationship").build();

    private static final String processorName = "HoldFlowFile";
    private static final Charset charset = Charset.forName("UTF-8");

    public static final PropertyDescriptor TIME_LIMIT = new PropertyDescriptor
            .Builder().name("TIME_LIMIT")
            .displayName("TIME LIMIT")
            .required(false)
            .addValidator(StandardValidators.NON_NEGATIVE_INTEGER_VALIDATOR)
            .description("Default : 1200")
            .build();

    public static final PropertyDescriptor FF_DELETE_LIMIT = new PropertyDescriptor
            .Builder().name("FF_DELETE_LIMIT")
            .displayName("FF_DELETE_LIMIT")
            .required(false)
            .addValidator(StandardValidators.NON_NEGATIVE_INTEGER_VALIDATOR)
            .description("Default : 86400")
            .build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;
    private LogUtil logUtil = null;
    private DateUtil dateUtil = null;
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
        initDescriptors.add(TIME_LIMIT);
        initDescriptors.add(FF_DELETE_LIMIT);
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
//        loadContextProperties(context);
//        dbcpService = context.getProperty(DBCP_SERVICE).asControllerService(DBCPService.class);
    }


    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", processorName);
        logMetaData.put("log_date", (this.getDateUtil().getTodayDate()).format(this.getDateUtil().getDateFormat()));
        Gson gson = GsonUtil.getGson();
        FlowFile flowFile = session.get();

        Long timeLimit = context.getProperty(TIME_LIMIT).asLong();
        Long deleteLimit = context.getProperty(FF_DELETE_LIMIT).asLong();
        if(flowFile == null){
            return;
        }
        try {
            Long created_epoch = null;
            Long updated_epoch = null;
            try {
                created_epoch = Long.parseLong(flowFile.getAttribute("created_epoch"));
                updated_epoch = Long.parseLong(flowFile.getAttribute("updated_epoch"));
            }
            catch (Exception eee){
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("unparsiable date creating our own "), Level.INFO, null);
                created_epoch = this.getDateUtil().getCurrentEpochInMillis();
                updated_epoch = created_epoch;
                session.putAttribute(flowFile, FlowFileAttributes.CREATED_EPOCH, created_epoch+"");
                session.putAttribute(flowFile, FlowFileAttributes.UPDATED_EPOCH, created_epoch+"");
            }

            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("created epoch %d",created_epoch ), Level.INFO, null);

            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("updated epoch %d",updated_epoch ), Level.INFO, null);

            long now = new Date().getTime();

            long creatDiff = (now - created_epoch)/1000;
            long updateDiff = (now - updated_epoch)/1000;

            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("created diff %d",creatDiff ), Level.INFO, null);

            this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                    String.format("updated diff %d",updateDiff ), Level.INFO, null);
            if (creatDiff >= deleteLimit) {
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("Flow file is %d old, removing it from queue",creatDiff ), Level.INFO, null);
                session.transfer(flowFile, REL_FAILURE);
                return;
            }
            if (updateDiff < timeLimit) {
                long sleepTime = (timeLimit - updateDiff) * 1000;
                this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.ACTIVITY_STATUS,
                        String.format("sleep time in millies %d",sleepTime ), Level.INFO, null);
                Thread.sleep(sleepTime);
            }

            session.transfer(flowFile, REL_SUCCESS);
        }
        catch (Exception ex){
            this.getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS,
                    Utility.stringifyException(ex), Level.INFO, null);
            session.transfer(flowFile, REL_SUCCESS);
        }
//        session.remove(flowFile);
    }
}
