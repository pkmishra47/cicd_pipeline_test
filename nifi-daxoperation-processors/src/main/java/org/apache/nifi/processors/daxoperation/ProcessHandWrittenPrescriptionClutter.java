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
@Tags({"dax", "PHR", "JPEG", "PNG", "PDF", "OCR", "AWS", "TEXTRACT"})
@CapabilityDescription("Processor to clear clutter from textract handwritten prescription")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute = "", description = "")})
@WritesAttributes({@WritesAttribute(attribute = "", description = "")})

public class ProcessHandWrittenPrescriptionClutter extends AbstractProcessor {

    private static final String processorName = "ProcessHandWrittenPrescriptionClutter";
    private static final Charset charset = StandardCharsets.UTF_8;

    private LogUtil logUtil = null;
    private static Gson gson = null;

    private Set<String> setOfDictWords = null;
    private Set<String> setOfMedicalDictionary = null;
    private DateUtil dateUtil = null;

    public String getProcessorName() {
        return ProcessHandWrittenPrescriptionClutter.processorName;
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
        if (ProcessHandWrittenPrescriptionClutter.gson == null) {
            ProcessHandWrittenPrescriptionClutter.gson = GsonUtil.getGson();
        }
        return ProcessHandWrittenPrescriptionClutter.gson;
    }

    public Set<String> getListOfDictWords() {
        return this.setOfDictWords;
    }

    public void setListOfDictWords(Set<String> setOfDictWords) {
        this.setOfDictWords = setOfDictWords;
    }

    public Set<String> getListOfMedicalDictionary() {
        return this.setOfMedicalDictionary;
    }

    public void setListOfMedicalDictionary(Set<String> setOfMedicalDictionary) {
        this.setOfMedicalDictionary = setOfMedicalDictionary;
    }

    public DateUtil getDateUtil() {
        if (this.dateUtil == null)
            this.dateUtil = new DateUtil();
        return this.dateUtil;
    }

    public static final PropertyDescriptor DICT_WORDS_LIST = new PropertyDescriptor
            .Builder().name("DICT_WORDS")
            .displayName("DICT WORDS")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .description("LIST OF JUNK WORDS")
            .build();

    public static final PropertyDescriptor MEDICAL_DICTIONARY_LIST = new PropertyDescriptor
            .Builder().name("MEDICAL_DICTIONARY")
            .displayName("MEDICAL DICTIONARY")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .description("MEDICAL DICTIONARY WORDS")
            .build();

    static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("SUCCESS")
            .description("marks process successful when processor achieves success condition.")
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
        descriptors.add(DICT_WORDS_LIST);
        descriptors.add(MEDICAL_DICTIONARY_LIST);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<>();
        relationships.add(REL_SUCCESS);
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
        Map<String, Object> logMetaData = new HashMap<>();
        logMetaData.put("processor_name", getProcessorName());
        String dictWordsFilePath = context.getProperty(DICT_WORDS_LIST).toString();
        Set<String> dictWordsSet = new HashSet<>();
        dictWordsSet.addAll(loadTxtFile(dictWordsFilePath, logMetaData));
        setListOfDictWords(dictWordsSet);

        String medicalDictionaryFilePath = context.getProperty(MEDICAL_DICTIONARY_LIST).toString();
        Set<String> medicalDictionarySet = new HashSet<>();
        medicalDictionarySet.addAll(loadTxtFile(medicalDictionaryFilePath, logMetaData));
        setListOfMedicalDictionary(medicalDictionarySet);
    }

    // Clutter Removal
    // =======================
    // 1. Removing lines if atleast 1 word is printed in the line
    // 2. Reject lines if there any restricted words in the line i.e., restricted words from english & medical dictionary
    // 3. Replace all special characters with spaces
    // 4. If "-" contains any character pre & post, then its not replaced
    // 5. Post replacing special charac if the number of characters in the line <4 then reject the line
    // 6. Similarly replace Integers too & check point 5
    // 7. If the line is handwritten, not junk & lenght of the line >3 then push the line to "processedStrings"
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
            Map<String, Map<String, Object>> rawLineCoordinates = oMapper.convertValue(ffRawInput.get("rawLineCoordinates"), Map.class);

            inputFileName = inputFF.getAttribute("fileName");
            logMetaData.put("fileName", inputFileName);

            Object documentType = ffRawInput.get("documentType");

            List<String> outputStrings = new ArrayList<>();
            String blockType = "";
            Map<String, Object> lines = new HashMap<>();
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
                    lines.put(test.id, relationLine);
                }

                if (blockType != null && blockType.equals("WORD")) {
                    words.put(test.id, test.textType);
                }

            }

            Map lineData = null;
            String text = "";
            List<String> childs = null;
            Boolean handwritten = true;
            Boolean junkWords = false;
            Set<String> wordsToRemove = new HashSet<>();

            wordsToRemove.addAll(getListOfDictWords());
            wordsToRemove.addAll(getListOfMedicalDictionary());

            Map<String, Object> rejectedLines = new HashMap<>();
            Map<String, Object> dosages = new HashMap<>();
            Map<String, Object> finalDosages = new HashMap<>();

            // Pattern extractDosage = Pattern.compile(" (([0-9 \\/\\.]+)[a-zA-Z ]*)$");
            // Pattern extractDosage = Pattern.compile("(([0-9\\/\\.]+)([g|m|l|IU| ]+))");
            Pattern extractDosage = Pattern.compile("(([a-z \\(\\)]+)([0-9\\/\\.]+)([g|m|l|IU|k| ]+))", Pattern.CASE_INSENSITIVE);
            Pattern specialCharacterPattern = Pattern.compile("[\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\.\\@\\/\\:\\;\\<\\=\\>\\?\\@\\[\\]\\^\\_\\`\\{\\|\\}\\~]");
            Pattern intervalTermsPattern = Pattern.compile("(Am|Pm|After|Age|Daily|Cap|Tab|Days|Day|Yearly|Years|Year|Monthly|Months|Month|Once|Twice|Thrice|Weeks|Weekly|Week|Breakfast|Lunch|Dinner)", Pattern.CASE_INSENSITIVE);
            Pattern allNumbersMatchPattern = Pattern.compile("[0-9]+");
            Pattern nonDigitCharacterPattern = Pattern.compile("[^0-9]+");

            Map<String, Map<String, Object>> parsedLineCoordinates = new HashMap<>();

            for (Map.Entry<String, Object> entry : lines.entrySet()) {
                lineData = (Map) entry.getValue();
                childs = (List) lineData.get("child");
                text = (String) lineData.get("text");
                for (String val : childs) {
                    if (words.get(val).equals("PRINTED")) {
                        handwritten = false;
                        break;
                    }
                }

                if (handwritten) {
                    allHandwrittenLines.add(text);
                }

                Matcher matcher = extractDosage.matcher(text + " ");

                String specialCharactersRemovedString = specialCharacterPattern.matcher(text).replaceAll(" ").trim();

                int previous = 0;
                int current = 0;
                int next = 0;

                String processedString = "";
                if (specialCharactersRemovedString.length() > 3) {
                    processedString += specialCharactersRemovedString.charAt(0);
                    for (int i = 1; i < (specialCharactersRemovedString.length() - 1); i++) {
                        previous = (int) specialCharactersRemovedString.charAt(i - 1);
                        current = (int) specialCharactersRemovedString.charAt(i);
                        next = (int) specialCharactersRemovedString.charAt(i + 1);
                        if (current == 45 && previous == 32 && next == 32) {
                            processedString += " ";
                        } else {
                            processedString += specialCharactersRemovedString.charAt(i);
                        }
                    }
                    processedString += specialCharactersRemovedString.charAt(specialCharactersRemovedString.length() - 1);
                } else {
                    rejectedLines.put(specialCharactersRemovedString, "Less than 4 characters");
                }

                processedString = intervalTermsPattern.matcher(processedString).replaceAll("");

                String numbersRemovedString = allNumbersMatchPattern.matcher(processedString).replaceAll(" ").trim();
                if (numbersRemovedString.length() < 4) {
                    rejectedLines.put(processedString, "Lines with all numbers");
                    handwritten = true;
                    junkWords = false;
                    continue;
                }

                processedString = processedString.trim();

                Set<String> lineHashSet = new HashSet<>();

                lineHashSet.addAll(Arrays.asList(processedString.toLowerCase().split("\\s+")));
                lineHashSet.retainAll(wordsToRemove);

                if (lineHashSet.size() > 0) {
                    junkWords = true;
                }

                if (handwritten && !junkWords && processedString != null && processedString.length() > 4) {

                    char[] checkCharLength = processedString.toLowerCase().toCharArray();
                    String checkCharLengthN = "";
                    for (int charPos = 0; charPos < checkCharLength.length; charPos++) {
                        int val = checkCharLength[charPos];
                        if (val >= 97 && val <= 122) {
                            checkCharLengthN += checkCharLength[charPos];
                        }
                    }

                    if (checkCharLengthN.length() < 5) {
                        rejectedLines.put(processedString, "Less than 4 characters");
                        handwritten = true;
                        junkWords = false;
                        continue;
                    }

                    Map<String, Object> coo = new HashMap<>();
                    coo.put("text", text);
                    coo.putAll(rawLineCoordinates.get(text));
                    parsedLineCoordinates.put(processedString, coo);

                    outputStrings.add(processedString);

                    String extractedDosageString = "";
                    while (matcher.find()) {
                        extractedDosageString = matcher.group(3).trim() + ",";
                        if (extractedDosageString != null && !extractedDosageString.equals("") && !extractedDosageString.equals(".")) {
                            break;
                        }
                    }

                    if (!extractedDosageString.equals("")) {
                        dosages.put(text, extractedDosageString.trim());
                        finalDosages.put(processedString, nonDigitCharacterPattern.matcher(extractedDosageString).replaceAll(" ").trim());
                    }

                } else if (processedString.length() < 5) {
                    rejectedLines.put(processedString, "Less than 5 characters");
                } else if (handwritten && junkWords) {
                    rejectedLines.put(text, lineHashSet);
                } else if (!handwritten) {
                    rejectedLines.put(text, "Contains printed word/letter");
                }
                handwritten = true;
                junkWords = false;
            }

            flowFileContent.put("processedLines", outputStrings);
            flowFileContent.put("textract", ffinput);
            flowFileContent.put("rejectedLines", rejectedLines);
            flowFileContent.put("allLines", allLines);
            flowFileContent.put("allHandwrittenLines", allHandwrittenLines);
            flowFileContent.put("fileName", inputFileName);
            flowFileContent.put("rawDosages", dosages);
            flowFileContent.put("parsedDosages", finalDosages);
            flowFileContent.put("documentType", documentType);
            flowFileContent.put("parsedLineCoordinates", parsedLineCoordinates);
            flowFileContent.put("type", ffRawInput.get("type"));
            inputFF = updateFlowFile(session, inputFF, this.getGson().toJson(flowFileContent));
            session.transfer(inputFF, REL_SUCCESS);
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
}