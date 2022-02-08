package org.apache.nifi.processors.daxoperation;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.AnalyzeDocumentRequest;
import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.DetectDocumentTextRequest;
import com.amazonaws.services.textract.model.DetectDocumentTextResult;
import com.amazonaws.services.textract.model.Document;
import com.google.gson.Gson;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.*;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.daxoperation.dm.PaperlessContent;
import org.apache.nifi.processors.daxoperation.dm.UnoFFInput;
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.stream.io.StreamUtils;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.awt.image.BufferedImage;

import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.event.Level;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

@InputRequirement(Requirement.INPUT_REQUIRED)
@Tags({"dax", "PHR", "JPEG", "PNG", "PDF", "OCR", "AWS", "TEXTRACT"})
@CapabilityDescription("Processor to get textract data from compressed file")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})

public class OCRDataExtraction extends AbstractProcessor {

    private static final String processorName = "OCRDataExtraction";
    private static final Charset charset = StandardCharsets.UTF_8;

    private LogUtil logUtil = null;
    private static Gson gson = null;
    private DateUtil dateUtil = null;
    private String awsAccessKeyId = null;
    private String awsSecretAccessKey = null;
    private String awsRegion = null;
    private String requestType = null;
    private AmazonTextract textractClient = null;

    public String getProcessorName() {
        return OCRDataExtraction.processorName;
    }

    public LogUtil getLogUtil() {
        if (this.logUtil == null)
            this.logUtil = new LogUtil();
        return this.logUtil;
    }

    public void setLogUtil(LogUtil logUtil) {
        this.logUtil = logUtil;
    }

    public Gson getGson() {
        if (OCRDataExtraction.gson == null) {
            OCRDataExtraction.gson = GsonUtil.getGson();
        }
        return OCRDataExtraction.gson;
    }

    public String getAWSAccessKeyId() {
        return this.awsAccessKeyId;
    }

    public void setAWSAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAWSSecretAccessKey() {
        return this.awsSecretAccessKey;
    }

    public void setAWSSecretAccessKey(String awsSecretAccessKey) {
        this.awsSecretAccessKey = awsSecretAccessKey;
    }

    public String getAwsRegion() {
        return this.awsRegion;
    }

    public void setAwsRegion(String awsRegion) {
        this.awsRegion = awsRegion;
    }

    public String getRequestType() {
        return this.requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    public AmazonTextract getAmazonTextractClient() {
        return this.textractClient;
    }

    public void setAmazonTextractClient(AmazonTextract textractClient) {
        this.textractClient = textractClient;
    }

    static final PropertyDescriptor AWS_ACCESS_KEY_ID = new PropertyDescriptor.Builder()
            .name("AWS_ACCESS_KEY_ID").displayName("AWS ACCESS KEY ID")
            .description("AWS ACCESS KEY ID")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

    static final PropertyDescriptor AWS_SECRET_ACCESS_KEY = new PropertyDescriptor.Builder()
            .name("AWS_SECRET_ACCESS_KEY").displayName("AWS SECRET ACCESS KEY")
            .description("AWS SECRET ACCESS KEY")
            .required(true)
            .sensitive(true)
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

    static final PropertyDescriptor AWS_REGION = new PropertyDescriptor.Builder()
            .name("AWS_REGION").displayName("AWS REGION")
            .description("AWS REGION")
            .required(true)
            .allowableValues(Regions.values())
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

    static final PropertyDescriptor REQUEST_TYPE = new PropertyDescriptor.Builder()
            .name("REQUEST_TYPE").displayName("REQUEST TYPE")
            .description("REQUEST TYPE")
            .required(true)
            .allowableValues("DetectDocumentText", "AnalyzeDocument")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

    static final Relationship REL_HANDWRITTEN = new Relationship.Builder()
            .name("HANDWRITTEN PRESCRIPTIONS")
            .description("Marks process successful and all handwritten prescriptions are redirected to this relationship")
            .build();

    static final Relationship REL_PRINTTED = new Relationship.Builder()
            .name("PRINTED PRESCRIPTIONS")
            .description("Marks process successful and all printed prescriptions are redirected to this relationship")
            .build();

    static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("FAILURE")
            .description("marks process failure when processor achieves failure condition.")
            .build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(AWS_ACCESS_KEY_ID);
        descriptors.add(AWS_SECRET_ACCESS_KEY);
        descriptors.add(AWS_REGION);
        descriptors.add(REQUEST_TYPE);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(REL_HANDWRITTEN);
        relationships.add(REL_PRINTTED);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
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
        setAWSAccessKeyId(context.getProperty(AWS_ACCESS_KEY_ID).toString());
        setAWSSecretAccessKey(context.getProperty(AWS_SECRET_ACCESS_KEY).toString());
        setAwsRegion(context.getProperty(AWS_REGION).toString());
        setRequestType(context.getProperty(REQUEST_TYPE).toString());
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        long startTime = this.getDateUtil().getCurrentEpochInMillis();
        FlowFile inputFF = session.get();
        String fileName = "";
        Map<String, Object> logMetaData = new HashMap<>();
        Map<String, Object> flowFileContent = new HashMap<>();

        try {
            logMetaData.put("processor_name", getProcessorName());

            if (inputFF == null) {
                getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            String ffContent = readFlowFileContent(inputFF, session);
            UnoFFInput ffinput = getGson().fromJson(ffContent, UnoFFInput.class);

            if (ffinput.data == null || (ffinput.data).isEmpty()) {
                logMetaData.put("flowfile_content", ffContent);
                markProcessorFailure(session, inputFF, logMetaData, null, "Invalid flowfile content. Values are either null or blank.");
                return;
            }

            PaperlessContent paperlessContent = extractData(ffinput.data);
            if (paperlessContent.fileName == null || (paperlessContent.fileName).isEmpty() || (paperlessContent.fileType == null) || (paperlessContent.fileType).isEmpty() || paperlessContent.content == null || (paperlessContent.content).isEmpty()) {
                logMetaData.put("fileName", paperlessContent.fileName);
                logMetaData.put("fileType", paperlessContent.fileType);
                markProcessorFailure(session, inputFF, logMetaData, null, "Invalid file request values. Values are either null or blank.");
                return;
            }

            fileName = paperlessContent.fileName;
            logMetaData.put("fileName", fileName);
            if (getAmazonTextractClient() == null) {
                AWSCredentials credentials = new BasicAWSCredentials(getAWSAccessKeyId(), getAWSSecretAccessKey());
                AmazonTextract client = AmazonTextractClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.valueOf(getAwsRegion())).build();
                setAmazonTextractClient(client);
            }

            byte[] decodedContent = Base64.decodeBase64(paperlessContent.content);
            List<Block> contentExtracted = null;

            if (paperlessContent.fileName.contains(".jpeg") || paperlessContent.fileName.contains(".JPEG") || paperlessContent.fileName.contains(".png") || paperlessContent.fileName.contains(".PNG") || paperlessContent.fileName.contains(".jpg")) {
                contentExtracted = extractBlocksFromImages(decodedContent, logMetaData);
            } else if (paperlessContent.fileName.contains(".pdf") || paperlessContent.fileName.contains(".PDF")) {
                contentExtracted = extractBlocksFromPDF(decodedContent, logMetaData);
            } else {
                markProcessorFailure(session, inputFF, logMetaData, null, "Invalid file format or extension received");
                return;
            }

            if (contentExtracted == null) {
                markProcessorFailure(session, inputFF, logMetaData, null, "Failed to extract data");
                return;
            }

            // Classifying prescription based on whole line
            // If atleast one word is handwritten then whole line is considered as handwritten
            List<String> allLineIds = new ArrayList<>();
            Map<String, Map<String, Object>> data = new HashMap<>();
            Map<String, Object> rawLineCoordinates = new HashMap<>();

            for (Block block : contentExtracted) {
                Map<String, Object> blockData = new HashMap<>();
                String id = block.getId();
                String blockType = block.getBlockType();

                if (blockType != null && blockType.equals("LINE")) {
                    allLineIds.add(id);
                    blockData.put("CHILD", block.getRelationships().get(0).getIds());
                    rawLineCoordinates.put(block.getText(), block.getGeometry().getBoundingBox());
                }
                blockData.put("TYPE", block.getTextType());
                blockData.put("TEXT", block.getText());
                data.put(id, blockData);
            }

            OCRUtil ocrUtil = new OCRUtil();
            Integer linesHandwrittenCount = ocrUtil.getHandWrittenLinesCount(allLineIds, data);

            String textType = "PRINTED";
            if (linesHandwrittenCount > 2) {
                textType = "HANDWRITING";
            }

            flowFileContent.put("rawLineCoordinates", rawLineCoordinates);
            flowFileContent.put("URL", paperlessContent.fileName);
            flowFileContent.put("OCRDATA", contentExtracted);
            flowFileContent.put("requestType", getRequestType());
            flowFileContent.put("documentType", textType);
            flowFileContent.put("type", ffinput.type);

            inputFF = updateFlowFile(session, inputFF, this.getGson().toJson(flowFileContent));

            if (textType.equals("PRINTED")) {
                session.transfer(inputFF, REL_PRINTTED);
            } else {
                session.transfer(inputFF, REL_HANDWRITTEN);
            }

            getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, String.format("Extracted file: %s", paperlessContent.fileName), Level.INFO, null);
            log_site_processing_time(startTime, fileName, logMetaData);
        } catch (Exception ex) {
            markProcessorFailure(session, inputFF, logMetaData, null, Utility.stringifyException(ex));
            return;
        }
    }

    private void log_site_processing_time(long startTime, String fileName, Map<String, Object> logMetaData) {
        Map<String, Object> otherDetails = new HashMap<>();
        long endTime = this.getDateUtil().getCurrentEpochInMillis();
        long duration = endTime - startTime;
        otherDetails.put("execution_duration", duration);
        this.getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL, fileName + ", execution_duration: " + duration, Level.INFO, otherDetails);
    }

    private FlowFile updateFlowFile(ProcessSession session, FlowFile ff, String data) {
        ff = session.write(ff, out -> out.write(data.getBytes(charset)));
        return ff;
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

    private PaperlessContent extractData(String data) throws IOException {
        String actualData = decompress(data);
        PaperlessContent file = getGson().fromJson(actualData, PaperlessContent.class);
        return file;
    }

    public String readFlowFileContent(FlowFile inputFF, ProcessSession session) {
        byte[] buffer = new byte[(int) inputFF.getSize()];
        session.read(inputFF, in -> StreamUtils.fillBuffer(in, buffer));

        return new String(buffer, charset);
    }

    private void markProcessorFailure(ProcessSession session, FlowFile flowFile, Map<String, Object> logMetaData, Map<String, Object> otherLogDetails, String logMessage) {
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO, otherLogDetails);
    }

    private FlowFile writeContentToOutputSession(ProcessSession session, Object flowFileContent) {
        Gson gson = GsonUtil.getGson();
        FlowFile outputFF = session.create();

        outputFF = session.write(outputFF, (OutputStreamCallback) out -> {
            String jsonString = gson.toJson(flowFileContent);
            out.write(jsonString.getBytes(charset));
        });
        return outputFF;
    }

    private List<Block> extractBlocksFromImages(byte[] bytes, Map<String, Object> logMetaData) {

        List<Block> blocks = new ArrayList<>();

        try {
            Document doc = new Document();
            doc.setBytes(ByteBuffer.wrap(bytes));
            if (getRequestType().equals("DetectDocumentText")) {
                DetectDocumentTextRequest request = new DetectDocumentTextRequest().withDocument(doc);
                DetectDocumentTextResult result = getAmazonTextractClient().detectDocumentText(request);
                blocks = result.getBlocks();
            } else if (getRequestType().equals("AnalyzeDocument")) {
                AnalyzeDocumentRequest request = new AnalyzeDocumentRequest().withFeatureTypes("TABLES", "FORMS").withDocument(doc);
                AnalyzeDocumentResult result = getAmazonTextractClient().analyzeDocument(request);
                blocks = result.getBlocks();
            }
        } catch (Exception e) {
            getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }
        return blocks;
    }

    private List<Block> extractBlocksFromPDF(byte[] bytes, Map<String, Object> logMetaData) {
        List<Block> allBlocks = new ArrayList<>();

        try {
            PDDocument document = PDDocument.load(bytes);
            PDFRenderer pdfRenderer = new PDFRenderer(document);

            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                BufferedImage bim = pdfRenderer.renderImage(page);
                ByteArrayOutputStream boas = new ByteArrayOutputStream();
                ImageIO.write(bim, "png", boas);
                byte[] pageBytes = boas.toByteArray();
                List<Block> pageBlocks = extractBlocksFromImages(pageBytes, logMetaData);
                allBlocks.addAll(pageBlocks);
            }
        } catch (Exception e) {
            getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.GENERAL, Utility.stringifyException(e), Level.ERROR, null);
        }
        return allBlocks;
    }
}