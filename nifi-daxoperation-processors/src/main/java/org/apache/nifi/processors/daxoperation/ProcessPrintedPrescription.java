package org.apache.nifi.processors.daxoperation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.google.gson.Gson;

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
import org.apache.nifi.processors.daxoperation.models.LogStatus;
import org.apache.nifi.processors.daxoperation.models.LogType;
import org.apache.nifi.processors.daxoperation.models.OCRLineCoordinates;
import org.apache.nifi.processors.daxoperation.models.OCRLineRecommendation;
import org.apache.nifi.processors.daxoperation.models.OCRLineResponseObject;
import org.apache.nifi.processors.daxoperation.models.TextractModel;
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;

@InputRequirement(Requirement.INPUT_REQUIRED)
@Tags({"dax", "PHR", "JPEG", "PNG", "PDF", "OCR", "TEXTRACT", "PRINTED"})
@CapabilityDescription("Process printed prescription and extract recommended medicines")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})

public class ProcessPrintedPrescription extends AbstractProcessor {

    private static final String processorName = "ProcessPrintedPrescription";
    private static final Charset charset = StandardCharsets.UTF_8;

    private LogUtil logUtil = null;
    private static Gson gson = null;

    private Set<String> skuSubString = null;
    private Set<String> junkWordsSet = null;
    private String dosagePath = null;
    private Map<String, List<Map<String, Object>>> dosageData = new HashMap<>();
    private DateUtil dateUtil = null;

    public String getProcessorName() {
        return ProcessPrintedPrescription.processorName;
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
        if (ProcessPrintedPrescription.gson == null) {
            ProcessPrintedPrescription.gson = GsonUtil.getGson();
        }
        return ProcessPrintedPrescription.gson;
    }

    public Set<String> getJunkWordsSet() {
        return this.junkWordsSet;
    }

    public void setJunkWordsSet(Set<String> junkWordsSet) {
        this.junkWordsSet = junkWordsSet;
    }

    public Set<String> getSkuSubStrings() {
        return this.skuSubString;
    }

    public void setSkuSubStrings(Set<String> skuSubString) {
        this.skuSubString = skuSubString;
    }

    public String getDosagesPath() {
        return this.dosagePath;
    }

    public void setDosagesPath(String dosagePath) {
        this.dosagePath = dosagePath;
    }

    public Map<String, List<Map<String, Object>>> getDosageData() {
        return this.dosageData;
    }

    public void setDosageData(Map<String, List<Map<String, Object>>> dosageData) {
        this.dosageData = dosageData;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    static final PropertyDescriptor DOSAGE_PATH = new PropertyDescriptor.Builder().name("SKU_DATA_PATH")
            .displayName("SKU DATA PATH").description("CSV file with 'ITEMNAME | SUB_STR | DOSAGE | SOLD_QTY' as headers in the provided sequence respectively")
            .required(true).addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

    static final PropertyDescriptor JUNK_WORDS = new PropertyDescriptor.Builder().name("WORDS_TO_EXCLUDE")
            .displayName("WORDS TO EXCLUDE").description("List of words for exclusion")
            .required(true).addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

    static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("marks process successful when processor achieves success condition.")
            .build();

    static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("FAILURE")
            .description("marks process failure when processor achieves failure condition.")
            .build();

    static final Relationship REL_API_RESPONSE = new Relationship.Builder().name("API_RESPONSE")
            .description("On successful execution API response is redirected through this relationship").build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(DOSAGE_PATH);
        descriptors.add(JUNK_WORDS);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        relationships.add(REL_API_RESPONSE);
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
        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", getProcessorName());

        setDosagesPath(context.getProperty(DOSAGE_PATH).toString());
        loadItemsSold(context.getProperty(DOSAGE_PATH).toString(), logMetaData);

        setSkuSubStrings(getDosageData().keySet());

        String junkWordsPath = context.getProperty(JUNK_WORDS).toString();
        Set<String> junkWordsSet = new HashSet<>();
        junkWordsSet.addAll(loadTxtFile(junkWordsPath, logMetaData));
        setJunkWordsSet(junkWordsSet);
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {

        FlowFile inputFF = session.get();
        ObjectMapper oMapper = new ObjectMapper();
        Map<String, Object> logMetaData = new HashMap<>();
        Map<String, Object> flowFileContent = new HashMap<>();
        String inputFileName = null;
        long startTime = this.getDateUtil().getCurrentEpochInMillis();

        try {
            logMetaData.put("processor_name", getProcessorName());

            if (inputFF == null) {
                getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            String ffContent = readFlowFileContent(inputFF, session);
            Map<String, Object> ffRawInput = getGson().fromJson(ffContent, Map.class);
            List<Map<String, Object>> ffinput = oMapper.convertValue(ffRawInput.get("OCRDATA"), List.class);

            inputFileName = inputFF.getAttribute("fileName");
            logMetaData.put("fileName", inputFileName);

            Object documentType = ffRawInput.get("documentType");
            Map<String, Map<String, Object>> rawLineCoordinates = oMapper.convertValue(ffRawInput.get("rawLineCoordinates"), Map.class);

            Set<String> outputStrings = new HashSet<>();
            String blockType = "";
            Map<String, Object> lineIds = new HashMap<>();
            Map<String, String> words = new HashMap<>();
            List<Map<String, Object>> relationships = null;
            List<String> allLines = new ArrayList<>();
            List<String> allHandwrittenLines = new ArrayList<>();

            TextractModel test = null;
            for (Integer i = 0; i < ffinput.size(); i++) {
                test = oMapper.convertValue(ffinput.get(i), TextractModel.class);
                blockType = test.blockType;
                relationships = test.relationships;
                List<String> idsList = new ArrayList<>();

                if (blockType != null && blockType.equals("LINE")) {
                    allLines.add(test.text);
                    for (Map<String, Object> relationObject : relationships) {
                        if (relationObject.get("type") != null && relationObject.get("type").equals("CHILD")) {
                            idsList.addAll((List) relationObject.get("ids"));
                        }
                    }
                    Map<String, Object> relationLine = new HashMap<>();
                    relationLine.put("text", test.text);
                    relationLine.put("child", idsList);
                    lineIds.put(test.id, relationLine);
                }

                if (blockType != null && blockType.equals("WORD")) {
                    words.put(test.id, test.text);
                }
            }

            Map lineData = null;

            Map<String, Object> rejectedLines = new HashMap<>();
            Map<String, Set<String>> fuzzyData = new HashMap<>();
            Map<String, String> processedLinesToCleanLines = new HashMap<>();

            Pattern specialCharacterPattern = Pattern.compile("[\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\.\\@\\/\\:\\;\\<\\=\\>\\?\\@\\[\\]\\^\\_\\`\\{\\|\\}\\~\\-]");
            Pattern allNumbersMatchPattern = Pattern.compile("[0-9]+");

            for (Map.Entry<String, Object> entry : lineIds.entrySet()) {
                lineData = (Map) entry.getValue();
                String text = (String) lineData.get("text");

                // Clear Special characters
                String specialCharactersRemovedString = specialCharacterPattern.matcher(text).replaceAll(" ").trim();

                String processedString = "";
                if (specialCharactersRemovedString.length() > 3) {
                    processedString = specialCharactersRemovedString;
                } else {
                    rejectedLines.put(specialCharactersRemovedString, "Less than 4 characters");
                    continue;
                }

                // Remove Numbers
                String numbersRemovedString = allNumbersMatchPattern.matcher(processedString).replaceAll(" ").trim();
                if (numbersRemovedString.length() < 4) {
                    rejectedLines.put(processedString, "Lines with all numbers");
                    continue;
                }

                char[] checkCharLength = numbersRemovedString.toLowerCase().toCharArray();
                String checkCharLengthN = "";
                for (int charPos = 0; charPos < checkCharLength.length; charPos++) {
                    int val = checkCharLength[charPos];
                    if (val >= 97 && val <= 122) {
                        checkCharLengthN += checkCharLength[charPos];
                    }
                }

                if (checkCharLengthN.length() < 4) {
                    rejectedLines.put(processedString, "Less than 4 characters");
                    continue;
                }

                outputStrings.add(text);
                processedLinesToCleanLines.put(text, numbersRemovedString);
            }

            for (String unProcessedLine : processedLinesToCleanLines.keySet()) {
                String cleanedLine = processedLinesToCleanLines.get(unProcessedLine).toUpperCase();
                String[] splitVal = cleanedLine.split(" ");

                Set<String> wordsInLine = new HashSet<>();
                for (String val : splitVal) {
                    if (!val.equals("")) {
                        wordsInLine.add(val);
                    }
                }

                Set<String> substringsMatched = Sets.intersection(getSkuSubStrings(), wordsInLine);
                Set<String> substringsAfterExclusion = new HashSet<>();
                substringsAfterExclusion.addAll(Sets.difference(substringsMatched, getJunkWordsSet()));

                Set<String> junkWordsInLine = new HashSet<>();
                junkWordsInLine.addAll(Sets.intersection(substringsMatched, getJunkWordsSet()));

                if (substringsAfterExclusion.size() != 0) {
                    fuzzyData.put(unProcessedLine, substringsAfterExclusion);
                } else if (junkWordsInLine.size() != 0) {
                    rejectedLines.put(unProcessedLine, junkWordsInLine);
                } else {
                    rejectedLines.put(unProcessedLine, "No SubStrings matched");
                }
            }

            // Pattern extractDosage = Pattern.compile(" (([0-9 \\/\\.]+)[a-zA-Z ]*)$");
            // Pattern extractDosage = Pattern.compile("(([0-9\\/\\.]+)([g|m|l|IU| ]+))", Pattern.CASE_INSENSITIVE);
            Pattern extractDosage = Pattern.compile("(([a-z \\(\\)]+)([0-9\\/\\.]+)([g|m|l|IU|k| ]+))", Pattern.CASE_INSENSITIVE);
            Pattern nonDigitCharacterPattern = Pattern.compile("[^0-9]+");
            Map<String, Object> dosages = new HashMap<>();
            Map<String, Object> finalDosages = new HashMap<>();

            for (String fuzzyLine : fuzzyData.keySet()) {
                Matcher dosageMatcher = extractDosage.matcher(fuzzyLine + " ");

                String extractedDosageString = "";
                while (dosageMatcher.find()) {
                    // extractedDosageString = extractedDosageString + dosageMatcher.group(3).trim();
                    extractedDosageString = dosageMatcher.group(3).trim();
                    if (extractedDosageString != null && !extractedDosageString.equals("") && !extractedDosageString.equals(".")) {
                        break;
                    }
                }

                if (!extractedDosageString.equals("")) {
                    dosages.put(fuzzyLine, extractedDosageString.trim());
                    finalDosages.put(fuzzyLine, nonDigitCharacterPattern.matcher(extractedDosageString).replaceAll(" ").trim());
                }
            }

            Set<String> suggestedMedicines = new HashSet<>();
            Map<String, Object> recommendedMedicines = new HashMap<>();
            Set<String> suggestedSKUId = new HashSet<>();

            Map<String, Object> APIResponseObject = new HashMap<>();
            List<OCRLineResponseObject> apiResponseObjectList = new ArrayList<>();

            List<Integer> top5Scores = new ArrayList<>();

            for (String fuzzyLine : fuzzyData.keySet()) {
                List<Map<String, Object>> recoMedObjects = new ArrayList<>();

                OCRLineResponseObject lineResponseObject = new OCRLineResponseObject();
                OCRLineCoordinates lineCoordinates = new OCRLineCoordinates();
                lineCoordinates.setHeight((Double) rawLineCoordinates.get(fuzzyLine).get("height"));
                lineCoordinates.setLeft((Double) rawLineCoordinates.get(fuzzyLine).get("left"));
                lineCoordinates.setTop((Double) rawLineCoordinates.get(fuzzyLine).get("top"));
                lineCoordinates.setWidth((Double) rawLineCoordinates.get(fuzzyLine).get("width"));
                lineResponseObject.setCoordinates(lineCoordinates);

                Integer count = 0;

                for (String fuzzySubstring : fuzzyData.get(fuzzyLine)) {
                    if (count.equals(0)) {
                        top5Scores.add(100);
                        count++;
                    }

                    suggestedMedicines.add(fuzzySubstring);

                    List<Map<String, Object>> skus = getDosageData().get(fuzzySubstring);

                    OCRUtil ocrUtil = new OCRUtil();
                    Map<String, Object> itemObject = ocrUtil.getRecommendedMedicineAndVariants(skus, finalDosages.get(fuzzyLine));

                    String skuId = (String) itemObject.get("SKU_ID");
                    String soldItemName = (String) itemObject.get("ITEMNAME");
                    Object variants = itemObject.get("VARIANTS");

                    itemObject.put("INPUTTEXT", fuzzySubstring);
                    recoMedObjects.add(itemObject);
                    suggestedSKUId.add(skuId);

                    OCRLineRecommendation recommendation = new OCRLineRecommendation();
                    recommendation.setId(skuId);
                    recommendation.setMatch(fuzzySubstring);
                    recommendation.setScore(100);
                    recommendation.setIndex(1);
                    recommendation.setItemname(soldItemName);
                    lineResponseObject.addRecommendations(recommendation);
                    recommendation.setRecommendationrank("PRIMARY");
                    recommendation.setVariants(variants);
                }
                recommendedMedicines.put(fuzzyLine, recoMedObjects);
                lineResponseObject.setText(fuzzyLine);
                apiResponseObjectList.add(lineResponseObject);
            }
            APIResponseObject.put("response", apiResponseObjectList);

            // Top 5 scores
            Collections.sort(top5Scores,Collections.reverseOrder());

            Integer scoreSize = top5Scores.size();

            if (scoreSize>5) {
                scoreSize = 5;
            }

            for (Integer i=0; i<scoreSize; i++) {
                flowFileContent.put("Top_" + (i+1), top5Scores.get(i));
            }

            if (ffRawInput.get("type") != null && ffRawInput.get("type").equals("topScores")) {
                Map<String, Object> topScores = new HashMap<>();
                for (Integer i=0; i<scoreSize; i++) {
                    topScores.put("Top_" + (i+1), top5Scores.get(i));
                }
                APIResponseObject = topScores;
            }
            
            flowFileContent.put("processedLines", getGson().toJson(outputStrings));
            flowFileContent.put("textract", getGson().toJson(ffinput));
            flowFileContent.put("rejectedLines", getGson().toJson(rejectedLines));
            flowFileContent.put("detectedLines", getGson().toJson(allLines));
            flowFileContent.put("handwrittenLines", getGson().toJson(allHandwrittenLines));
            flowFileContent.put("fileName", inputFileName);
            flowFileContent.put("medicineLinesCount", fuzzyData.size());
            flowFileContent.put("medicineLinesCount_greater_than_90", fuzzyData.size());
            flowFileContent.put("medicineLinesCount_greater_than_75", fuzzyData.size());
            flowFileContent.put("fuzzyData", getGson().toJson(fuzzyData));
            flowFileContent.put("rawDosages", getGson().toJson(dosages));
            flowFileContent.put("parsedDosages", getGson().toJson(finalDosages));
            flowFileContent.put("fuzzyData_greater_than_90", getGson().toJson(fuzzyData));
            flowFileContent.put("fuzzyData_greater_than_75", getGson().toJson(fuzzyData));
            flowFileContent.put("recommendedMedicines", getGson().toJson(recommendedMedicines));
            flowFileContent.put("recommendedMedicines_greater_than_90", getGson().toJson(recommendedMedicines));
            flowFileContent.put("recommendedMedicines_greater_than_75", getGson().toJson(recommendedMedicines));
            flowFileContent.put("suggestedMedicines", getGson().toJson(suggestedMedicines));
            flowFileContent.put("suggestedMedicines_greater_than_90", getGson().toJson(suggestedMedicines));
            flowFileContent.put("suggestedMedicines_greater_than_75", getGson().toJson(suggestedMedicines));
            flowFileContent.put("documentType", documentType);
            flowFileContent.put("suggestedSKUId", getGson().toJson(suggestedSKUId));
            FlowFile outputFF = writeContentToOutputSession(session, flowFileContent);
            session.transfer(outputFF, REL_SUCCESS);
            inputFF = updateFlowFile(session, inputFF, this.getGson().toJson(APIResponseObject));
            session.transfer(inputFF, REL_API_RESPONSE);
            log_site_processing_time(startTime, inputFileName, logMetaData);
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

    public List<String> loadTxtFile(String fileName, Map<String, Object> logMetaData) {
        List<String> words = new ArrayList<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = null;

            while ((line = br.readLine()) != null) {
                words.add(line);
            }

        } catch (Exception e) {
            getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Utility.stringifyException(e), Level.ERROR, null);
        }
        return words;
    }

    public void loadItemsSold(String fileName, Map<String, Object> logMetaData) {
        Map<String, List<Map<String, Object>>> map = new HashMap<>();

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line = null;

            // SKU_ID,ITEMNAME,SUB_STR,DOSAGE,SOLD_QTY

            int i = 0;
            while ((line = br.readLine()) != null) {
                if (i == 0) {
                    i++;
                    continue;
                }
                String str[] = line.split(",", -1);
                Map<String, Object> skuData = new HashMap<>();
                skuData.put("SKU_ID", str[0]);
                skuData.put("ITEMNAME", str[1]);
                skuData.put("DOSAGE", str[3]);
                skuData.put("SOLD_QTY", Integer.parseInt(str[4]));
                if (map.containsKey(str[2])) {
                    map.get(str[2]).add(skuData);
                } else {
                    List<Map<String, Object>> skusObjects = new ArrayList<>();
                    skusObjects.add(skuData);
                    map.put(str[2].toUpperCase(), skusObjects);
                }
            }

            br.close();
        } catch (Exception e) {
            getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Utility.stringifyException(e), Level.ERROR, null);
        }
        setDosageData(map);
    }
}