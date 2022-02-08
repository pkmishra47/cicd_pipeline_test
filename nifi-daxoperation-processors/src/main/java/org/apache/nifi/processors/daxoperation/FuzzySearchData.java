package org.apache.nifi.processors.daxoperation;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.nifi.processors.daxoperation.utils.*;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.OutputStreamCallback;
import org.apache.nifi.stream.io.StreamUtils;
import org.slf4j.event.Level;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import me.xdrop.fuzzywuzzy.algorithms.WeightedRatio;
import me.xdrop.fuzzywuzzy.model.ExtractedResult;
import me.xdrop.fuzzywuzzy.ratios.PartialRatio;
import me.xdrop.fuzzywuzzy.ratios.SimpleRatio;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;

@InputRequirement(Requirement.INPUT_REQUIRED)
@Tags({"dax", "PHR", "JPEG", "PNG", "PDF", "OCR", "AWS", "TEXTRACT"})
@CapabilityDescription("Processor to compress query/file data")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})

public class FuzzySearchData extends AbstractProcessor {

    private static final String processorName = "FuzzySearchData";
    private static final Charset charset = StandardCharsets.UTF_8;

    private LogUtil logUtil = null;
    private static Gson gson = null;

    private String matchType = null;
    private Integer extractTop = null;
    private Integer searchConfidence = null;
    private Integer clutterConfidence = null;
    private String lookupOrder = null;
    // private String salesPath = null;
    // private String alphaPath = null;
    private String dosagePath = null;
    private Map<String, Map<Integer, String>> seggregatedMedicines = new HashMap<>();
    private Map<String, List<Map<String, Object>>> dosageData = new HashMap<>();
    private Map<String, Integer> subStringsSold = new HashMap<>();
    private DateUtil dateUtil = null;

    public String getProcessorName() {
        return FuzzySearchData.processorName;
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
        if (FuzzySearchData.gson == null) {
            FuzzySearchData.gson = GsonUtil.getGson();
        }
        return FuzzySearchData.gson;
    }

    public String getMatchType() {
        return this.matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public Integer getExtractTop() {
        return this.extractTop;
    }

    public void setExtractTop(Integer extractTop) {
        this.extractTop = extractTop;
    }

    public Integer getSearchConfidence() {
        return this.searchConfidence;
    }

    public void setSearchConfidence(Integer searchConfidence) {
        this.searchConfidence = searchConfidence;
    }

    public Integer getClutterConfidence() {
        return this.clutterConfidence;
    }

    public void setClutterConfidence(Integer clutterConfidence) {
        this.clutterConfidence = clutterConfidence;
    }

    public String getLookupOrder() {
        return this.lookupOrder;
    }

    public void setLookupOrder(String lookupOrder) {
        this.lookupOrder = lookupOrder;
    }

    // public List<String> getChoices() {
    //     return this.choices;
    // }

    // public void setChoices(List<String> choices) {
    //     this.choices = choices;
    // }

    // public String getSalesPath() {
    //     return this.salesPath;
    // }

    // public void setSalesPath(String salesPath) {
    //     this.salesPath = salesPath;
    // }

    // public String getAlphaPath() {
    //     return this.alphaPath;
    // }

    // public void setAlphaPath(String alphaPath) {
    //     this.alphaPath = alphaPath;
    // }

    public String getDosagesPath() {
        return this.dosagePath;
    }

    public void setDosagesPath(String dosagePath) {
        this.dosagePath = dosagePath;
    }

    public Map<String, Map<Integer, String>> getSeggregatedDict() {
        return this.seggregatedMedicines;
    }

    public void setSeggregatedDict(Map<String, Map<Integer, String>> seggregatedMedicines) {
        this.seggregatedMedicines = seggregatedMedicines;
    }

    public Map<String, List<Map<String, Object>>> getDosageData() {
        return this.dosageData;
    }

    public void setDosageData(Map<String, List<Map<String, Object>>> dosageData) {
        this.dosageData = dosageData;
    }


    public Map<String, Integer> getSubStringsSold() {
        return this.subStringsSold;
    }

    public void setSubStringsSold(Map<String, Integer> subStringsSold) {
        this.subStringsSold = subStringsSold;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    static final PropertyDescriptor LOOKUP_ORDER = new PropertyDescriptor.Builder().name("LOOKUP_ORDER")
            .displayName("LOOKUP ORDER").description("Order of choices i.e., sales or alpha order").required(true)
            .allowableValues("sales", "alpha").addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

    static final PropertyDescriptor MATCH_TYPE = new PropertyDescriptor.Builder().name("MATCH_TYPE")
            .displayName("MATCH TYPE").description("Type Of Match i.e., simpleMatch or partialMatch").required(true)
            .allowableValues("simpleRatio", "partialRatio", "weightedRatio")
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

    static final PropertyDescriptor EXTRACT_TOP = new PropertyDescriptor.Builder().name("EXTRACT_TOP")
            .displayName("EXTRACT TOP").description("Number of words to fetch, Restricted to 3 words if input >3").required(true)
            .addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR).build();

    static final PropertyDescriptor SEARCH_CONFIDENCE = new PropertyDescriptor.Builder().name("SEARCH_CONFIDENCE")
            .displayName("CONFIDENCE SCORE").description("Search Terms below this confidence will not return")
            .required(true).addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR).build();

    static final PropertyDescriptor CLUTTER_CONFIDENCE = new PropertyDescriptor.Builder().name("CLUTTER_CONFIDENCE")
            .displayName("CLUTTER CONFIDENCE").description("Output Terms below this confidence will be ignored")
            .required(true).addValidator(StandardValidators.POSITIVE_INTEGER_VALIDATOR).build();

    static final PropertyDescriptor SALES_PATH = new PropertyDescriptor.Builder().name("SALES_PATH")
            .displayName("SKUS BASED ON SALES").description("Provide path for SKU's available in a text file")
            .required(true).addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

    static final PropertyDescriptor ALPHA_PATH = new PropertyDescriptor.Builder().name("ALPHA_PATH")
            .displayName("SKUS BASED ON ALPHABETS").description("Provide path for SKU's available in a text file")
            .required(true).addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

    static final PropertyDescriptor DOSAGE_PATH = new PropertyDescriptor.Builder().name("SKU_DATA_PATH")
            .displayName("SKU DOSAGES DATA").description("CSV file with 'ITEMNAME | SUB_STR | DOSAGE | SOLD_QTY' as headers in the provided sequence respectively")
            .required(true).addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();

    static final Relationship REL_SUCCESS = new Relationship.Builder().name("SUCCESS")
            .description("marks process successful when processor achieves success condition.").build();

    static final Relationship REL_FAILURE = new Relationship.Builder().name("FAILURE")
            .description("marks process failure when processor achieves failure condition.").build();

    static final Relationship REL_API_RESPONSE = new Relationship.Builder().name("API_RESPONSE")
            .description("On successful execution API response is redirected through this relationship").build();

    private List<PropertyDescriptor> descriptors;
    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(LOOKUP_ORDER);
        descriptors.add(MATCH_TYPE);
        descriptors.add(EXTRACT_TOP);
        descriptors.add(SEARCH_CONFIDENCE);
        descriptors.add(CLUTTER_CONFIDENCE);
        descriptors.add(SALES_PATH);
        descriptors.add(ALPHA_PATH);
        descriptors.add(DOSAGE_PATH);
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
        setLookupOrder(context.getProperty(LOOKUP_ORDER).toString());
        setMatchType(context.getProperty(MATCH_TYPE).toString());
        setSearchConfidence(context.getProperty(SEARCH_CONFIDENCE).asInteger());
        setClutterConfidence(context.getProperty(CLUTTER_CONFIDENCE).asInteger());
        // setSalesPath(context.getProperty(SALES_PATH).toString());
        // setAlphaPath(context.getProperty(ALPHA_PATH).toString());
        setDosagesPath(context.getProperty(DOSAGE_PATH).toString());

        Integer extractTop = context.getProperty(EXTRACT_TOP).asInteger();
        if (extractTop > 3) {
            extractTop = 3;
        }
        setExtractTop(extractTop);

        Map<String, Object> logMetaData = new HashMap<>();
        // String path = "";

        // if (getLookupOrder().equals("sales")) {
        //     path = getSalesPath();
        // } else {
        //     path = getAlphaPath();
        // }
        // List<String> test = loadTxtFile(path, logMetaData);

        loadItemsSold(context.getProperty(DOSAGE_PATH).toString(), logMetaData);

        List<String> test = new ArrayList<>();

        if (getLookupOrder().equals("sales")) {
            test = substringListWRTSales();
        } else {
            test = substringListWRTApha();
        }
        Map<String, Map<Integer, String>> vals = seggregateMedicinesOnFirstCharacter(test);
        setSeggregatedDict(vals);
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {

        FlowFile inputFF = session.get();
        ObjectMapper oMapper = new ObjectMapper();
        Map<String, Object> logMetaData = new HashMap<>();
        Map<String, Object> flowFileContent = new HashMap<>();
        long startTime = this.getDateUtil().getCurrentEpochInMillis();

        try {
            logMetaData.put("processor_name", getProcessorName());

            if (inputFF == null) {
                getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS,
                        Constants.UNINITIALIZED_FLOWFILE_ERROR_MESSAGE, Level.ERROR, null);
                return;
            }

            String ffContent = readFlowFileContent(inputFF, session);
            Map<String, Object> ffinput = getGson().fromJson(ffContent, Map.class);

            Object processedLines = ffinput.get("processedLines");
            Object textract = ffinput.get("textract");
            Object rejectedLines = ffinput.get("rejectedLines");
            Object allLines = ffinput.get("allLines");
            Object allHandwrittenLines = ffinput.get("allHandwrittenLines");
            Object fileName = ffinput.get("fileName");
            Object rawDosages = ffinput.get("rawDosages");
            Object parsedDosages = ffinput.get("parsedDosages");
            Object documentType = ffinput.get("documentType");
            Map<String, Map<String, Object>> parsedLineCoordinates = oMapper.convertValue(ffinput.get("parsedLineCoordinates"), Map.class);

            flowFileContent.put("rawDosages", getGson().toJson(rawDosages));
            flowFileContent.put("parsedDosages", getGson().toJson(parsedDosages));
            flowFileContent.put("textract", getGson().toJson(textract));
            flowFileContent.put("rejectedLines", getGson().toJson(rejectedLines));
            flowFileContent.put("processedLines", getGson().toJson(processedLines));
            flowFileContent.put("detectedLines", getGson().toJson(allLines));
            flowFileContent.put("handwrittenLines", getGson().toJson(allHandwrittenLines));
            flowFileContent.put("fileName", fileName);
            flowFileContent.put("documentType", documentType);

            logMetaData.put("fileName", fileName);

            if (processedLines == null) {
                getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, "Invalid FlowFile Content", Level.ERROR, null);
                return;
            }

            List<String> inputList = oMapper.convertValue(processedLines, List.class);
            Map<String, String> finalDosagesMap = oMapper.convertValue(parsedDosages, Map.class);

            List<ExtractedResult> extractedResults = null;

            Map<String, Object> fuzzyData = new HashMap<>();
            Map<Integer, String> sortedMap = new TreeMap<>();
            
            Set<String> suggestedMedicines = new HashSet<>();
            Map<String, Object> recommendedMedicines = new HashMap<>();
            Set<String> suggestedSKUId = new HashSet<>();

            Map<String, Object> fuzzyData_greater_than_90 = new HashMap<>();
            Set<String> suggestedMedicines_greater_than_90 = new HashSet<>();
            Map<String, Object> recommendedMedicines_greater_than_90 = new HashMap<>();

            Map<String, Object> fuzzyData_greater_than_75 = new HashMap<>();
            Set<String> suggestedMedicines_greater_than_75 = new HashSet<>();
            Map<String, Object> recommendedMedicines_greater_than_75 = new HashMap<>();

//            Map<String, OCRLineResponseObject> APIResponseObject = new HashMap<>();
            Map<String, Object> APIResponseObject = new HashMap<>();
            List<OCRLineResponseObject> apiResponseObjectList = new ArrayList<>();

            List<Integer> top5Scores = new ArrayList<>();

            for (String inputText : inputList) {

                String[] splitStrings = inputText.split(" ");
                for (String inText : splitStrings) {
                    char[] splitText = inText.toCharArray();
                    if (splitText.length != 0) {
                        String firstLetter = Character.toString(splitText[0]).toLowerCase();
                        Map<Integer, String> charDict = getSeggregatedDict().get(firstLetter);
                        if (charDict != null) {
                            sortedMap.putAll(charDict);
                        }
                    }
                }

                List<String> choilList = new ArrayList<>(sortedMap.values());

                if (getMatchType().equals("simpleRatio")) {
                    extractedResults = FuzzySearch.extractTop(inputText.toUpperCase(), choilList, new SimpleRatio(),
                            getExtractTop(), getSearchConfidence());
                } else if (getMatchType().equals("partialRatio")) {
                    extractedResults = FuzzySearch.extractTop(inputText.toUpperCase(), choilList, new PartialRatio(),
                            getExtractTop(), getSearchConfidence());
                } else if (getMatchType().equals("weightedRatio")) {
                    extractedResults = FuzzySearch.extractTop(inputText.toUpperCase(), choilList, new WeightedRatio(),
                            getExtractTop(), getSearchConfidence());
                }

                sortedMap.clear();

                getLogUtil().logMessage(logMetaData, LogStatus.SUCCESS, LogType.GENERAL,
                        inputText + " -- " + gson.toJson(extractedResults), Level.INFO, null);

                Map<Integer, List<Integer>> scoreIndexMap = new HashMap<>();
                int firstScore = -1;
                int suggestedTextSize = -1;
                if (extractedResults != null && extractedResults.size() > 0) {
                    firstScore = extractedResults.get(0).getScore();
                    for (ExtractedResult val : extractedResults) {
                        List<Integer> indices = getScoreIndices(scoreIndexMap, val);
                        scoreIndexMap.put(val.getScore(), indices);
                        if (suggestedTextSize < val.getString().length())
                            suggestedTextSize = val.getString().length();
                    }
                }

                OCRLineResponseObject lineResponseObject = new OCRLineResponseObject();

                if (!(firstScore >= -1 && firstScore < getClutterConfidence()) || !(suggestedTextSize >= -1 && suggestedTextSize < 4)) {
                    fuzzyData.put(inputText, extractedResults);

                    String rawLine = (String) parsedLineCoordinates.get(inputText).get("text");
                    OCRLineCoordinates lineCoordinates = new OCRLineCoordinates();
                    lineCoordinates.setHeight((Double) parsedLineCoordinates.get(inputText).get("height"));
                    lineCoordinates.setLeft((Double) parsedLineCoordinates.get(inputText).get("left"));
                    lineCoordinates.setTop((Double) parsedLineCoordinates.get(inputText).get("top"));
                    lineCoordinates.setWidth((Double) parsedLineCoordinates.get(inputText).get("width"));
                    lineResponseObject.setCoordinates(lineCoordinates);

                    Map<Integer, Map<Integer, String>> recommendationMap = Utility.getRecommendationRankBasedOnScoreIndex(scoreIndexMap);
                    logMetaData.put("recommendationMap", recommendationMap);

                    List<Map<String, Object>> recoMedObjects = new ArrayList<>();

                    List<Map<String, Object>> recoMedObjects_greater_than_90 = new ArrayList<>();
                    List<Map<String, Object>> recoMedObjects_greater_than_75 = new ArrayList<>();

                    List<ExtractedResult> extractedResults_greater_than_90 = new ArrayList<>();
                    List<ExtractedResult> extractedResults_greater_than_75 = new ArrayList<>();

                    Integer count = 0;

                    for (ExtractedResult result : extractedResults) {
                        if (count.equals(0) && result.getScore()>=70) {
                            top5Scores.add(result.getScore());
                            count++;
                        }
                        suggestedMedicines.add(result.getString());

                        List<Map<String, Object>> skus = getDosageData().get(result.getString());

                        OCRUtil ocrUtil = new OCRUtil();
                        Map<String, Object> itemObject = ocrUtil.getRecommendedMedicineAndVariants(skus, finalDosagesMap.get(inputText));

                        String skuId = (String) itemObject.get("SKU_ID");
                        String soldItemName = (String) itemObject.get("ITEMNAME");
                        Object variants = itemObject.get("VARIANTS");

                        itemObject.put("INPUTTEXT", result.getString());
                        recoMedObjects.add(itemObject);
                        suggestedSKUId.add(skuId);

                        if (result.getScore()>=90) {
                            suggestedMedicines_greater_than_90.add(result.getString());
                            recoMedObjects_greater_than_90.add(itemObject);
                            extractedResults_greater_than_90.add(result);
                        }

                        if (result.getScore()>=75) {
                            suggestedMedicines_greater_than_75.add(result.getString());
                            recoMedObjects_greater_than_75.add(itemObject);
                            extractedResults_greater_than_75.add(result);
                        }

                        OCRLineRecommendation recommendation = new OCRLineRecommendation();
                        recommendation.setId(skuId);
                        recommendation.setMatch(result.getString());
                        recommendation.setScore(result.getScore());
                        recommendation.setIndex(result.getIndex());
                        recommendation.setItemname(soldItemName);
                        recommendation.setVariants(variants);

                        String rank = "";
                        if (recommendationMap.get(result.getScore()) != null && recommendationMap.get(result.getScore()).get(result.getIndex()) != null)
                            rank = recommendationMap.get(result.getScore()).get(result.getIndex());

                        recommendation.setRecommendationrank(rank);
                        lineResponseObject.addRecommendations(recommendation);
                    }
                    recommendedMedicines.put(inputText, recoMedObjects);
                    lineResponseObject.setText(rawLine);
                    apiResponseObjectList.add(lineResponseObject);

                    recommendedMedicines_greater_than_90.put(inputText, recoMedObjects_greater_than_90);
                    recommendedMedicines_greater_than_75.put(inputText, recoMedObjects_greater_than_75);

                    if (extractedResults_greater_than_90.size()!=0) {
                        fuzzyData_greater_than_90.put(inputText, extractedResults_greater_than_90);
                    }

                    if (extractedResults_greater_than_75.size()!=0) {
                        fuzzyData_greater_than_75.put(inputText, extractedResults_greater_than_75);
                    }
                }
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

            if (ffinput.get("type") != null && ffinput.get("type").equals("topScores")) {
                Map<String, Object> topScores = new HashMap<>();
                for (Integer i=0; i<scoreSize; i++) {
                    topScores.put("Top_" + (i+1), top5Scores.get(i));
                }
                APIResponseObject = topScores;
            }

            flowFileContent.put("recommendedMedicines", getGson().toJson(recommendedMedicines));
            flowFileContent.put("recommendedMedicines_greater_than_90", getGson().toJson(recommendedMedicines_greater_than_90));
            flowFileContent.put("recommendedMedicines_greater_than_75", getGson().toJson(recommendedMedicines_greater_than_75));
            flowFileContent.put("fuzzyData", getGson().toJson(fuzzyData));
            flowFileContent.put("fuzzyData_greater_than_90", getGson().toJson(fuzzyData_greater_than_90));
            flowFileContent.put("fuzzyData_greater_than_75", getGson().toJson(fuzzyData_greater_than_75));
            flowFileContent.put("medicineLinesCount", fuzzyData.size());
            flowFileContent.put("medicineLinesCount_greater_than_90", fuzzyData_greater_than_90.size());
            flowFileContent.put("medicineLinesCount_greater_than_75", fuzzyData_greater_than_75.size());
            flowFileContent.put("suggestedMedicines", getGson().toJson(suggestedMedicines));
            flowFileContent.put("suggestedMedicines_greater_than_90", getGson().toJson(suggestedMedicines_greater_than_90));
            flowFileContent.put("suggestedMedicines_greater_than_75", getGson().toJson(suggestedMedicines_greater_than_75));
            flowFileContent.put("suggestedSKUId", getGson().toJson(suggestedSKUId));

            List<Object> test = new ArrayList<>();
            test.add(flowFileContent);
            FlowFile outputFF = writeContentToOutputSession(session, test);
            session.transfer(outputFF, REL_SUCCESS);
            inputFF = updateFlowFile(session, inputFF, this.getGson().toJson(APIResponseObject));
            session.transfer(inputFF, REL_API_RESPONSE);
            log_site_processing_time(startTime, fileName.toString(), logMetaData);
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

    private void markProcessorFailure(ProcessSession session, FlowFile flowFile, Map<String, Object> logMetaData,
                                      Map<String, Object> otherLogDetails, String logMessage) {
        session.putAttribute(flowFile, "message", logMessage);
        session.transfer(flowFile, REL_FAILURE);
        getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, logMessage, Level.INFO,
                otherLogDetails);
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
            getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Utility.stringifyException(e),
                    Level.ERROR, null);
        }
        return words;
    }

    public Map<String, Map<Integer, String>> seggregateMedicinesOnFirstCharacter(List<String> medicines) {
        Map<String, Map<Integer, String>> seggregatedDictionary = new HashMap<>();

        int index = 0;
        for (String med : medicines) {
            String firstLetter = Character.toString(med.toCharArray()[0]).toLowerCase();
            if (seggregatedDictionary.containsKey(firstLetter)) {
                seggregatedDictionary.get(firstLetter).put(index, med);
            } else {
                Map<Integer, String> characterDictionary = new HashMap<>();
                characterDictionary.put(index, med);
                seggregatedDictionary.put(firstLetter, characterDictionary);
            }
            index++;
        }
        return seggregatedDictionary;
    }

    public void loadItemsSold(String fileName, Map<String, Object> logMetaData) {
        Map<String, List<Map<String, Object>>> map = new HashMap<>();
        Map<String, Integer> substrSold = new HashMap<>();

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
                    map.put(str[2], skusObjects);
                }

                Integer qty = str[4].equals("") ? 0 : Integer.parseInt(str[4]);
                
                if (substrSold.containsKey(str[2])) {
                    substrSold.put(str[2], substrSold.get(str[2]) + qty);
                } else {
                    substrSold.put(str[2], qty);
                }
            }
        } catch (Exception e) {
            getLogUtil().logMessage(logMetaData, LogStatus.FAIL, LogType.ACTIVITY_STATUS, Utility.stringifyException(e), Level.ERROR, null);
        }
        setDosageData(map);
        setSubStringsSold(substrSold);
    }

    private List<Integer> getScoreIndices(Map<Integer, List<Integer>> scoreIndexMap, ExtractedResult val) {
        List<Integer> indices;
        if (scoreIndexMap.get(val.getScore()) != null)
            indices = scoreIndexMap.get(val.getScore());
        else
            indices = new ArrayList<>();
        indices.add(val.getIndex());
        Collections.sort(indices);
        return indices;
    }

    private List<String> substringListWRTSales() {
        Map<Integer, List<String>> salesData = new TreeMap<>(Collections.reverseOrder());

        if (getSubStringsSold() != null) {
            for (Map.Entry<String, Integer> tes : getSubStringsSold().entrySet()) {
                if (salesData.containsKey(tes.getValue())) {
                    salesData.get(tes.getValue()).add(tes.getKey());
                } else {
                    List<String> subStrList = new ArrayList<>();
                    subStrList.add(tes.getKey());
                    salesData.put(tes.getValue(), subStrList);
                }
            }

        }

        List<String> val = new ArrayList<>();

        for (Map.Entry<Integer, List<String>> sEntry : salesData.entrySet()) {
            val.addAll(sEntry.getValue());
        }
        return val;
    }

    private List<String> substringListWRTApha() {
        List<String> val = new ArrayList<>();
        if (getDosageData()!=null) {
            val.addAll(getDosageData().keySet());
        }
        Collections.sort(val);
        return val;
    }
}