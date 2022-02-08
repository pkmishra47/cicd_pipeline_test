package org.apache.nifi.processors.daxoperation.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.nifi.processors.daxoperation.models.OCRRecommendationVariant;

public class OCRUtil {

    public Integer getHandWrittenLinesCount(List<String> allLineIds, Map<String, Map<String, Object>> data) {

        Pattern specialCharacterPattern = Pattern.compile("[\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\.\\@\\/\\:\\;\\<\\=\\>\\?\\@\\[\\]\\^\\_\\`\\{\\|\\}\\~]");
        Pattern allNumbersMatchPattern = Pattern.compile("[0-9]+");
        Integer linesHandwrittenCount = 0;

        for (String val : allLineIds) {
            // clear special characters & and if all numbers then exclude
            // <4 characters lins also exclude 
            String text = (String) data.get(val).get("TEXT");

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
            }

            String numbersRemovedString = allNumbersMatchPattern.matcher(processedString).replaceAll(" ").trim();

            char[] checkCharLength = numbersRemovedString.toLowerCase().toCharArray();
            String checkCharLengthN = "";
            for (int charPos = 0; charPos < checkCharLength.length; charPos++) {
                int asciVal = checkCharLength[charPos];
                if (asciVal >= 97 && asciVal <= 122) {
                    checkCharLengthN += checkCharLength[charPos];
                }
            }

            if (checkCharLengthN.length() < 4) {
                continue;
            }

            List<String> childIds = (List) data.get(val).get("CHILD");
            Integer wordsHandwrittenCount = 0;
            for (String child : childIds) {
                try {
                    String childTextType = (String) data.get(child).get("TYPE");
                    if (childTextType.equals("HANDWRITING")) {
                        wordsHandwrittenCount++;
                    }
                } catch (NullPointerException ignore) {
                    //2022-02-03: For Carepersona flow, we are getting modified Textextract object which may not have child words.
                }
            }

            if (wordsHandwrittenCount == childIds.size()) {
                linesHandwrittenCount++;
            }
        }

        return linesHandwrittenCount;
    }

    public Map<String, Object> getRecommendedMedicineAndVariants(List<Map<String, Object>> skus, Object DosageExtracted) {
        Map<String, Object> itemObject = new HashMap<>();
        String soldItemName = "", skuId = "";

        Integer extractedIndex = -1;
        if (skus != null) {
            skuId = (String) skus.get(0).get("SKU_ID");
            soldItemName = (String) skus.get(0).get("ITEMNAME");
            extractedIndex = 0;
            Integer count = -1;
            for (Map<String, Object> skuObject : skus) {
                count++;
                String dosage = (String) skuObject.get("DOSAGE");
                if (dosage.equals(DosageExtracted)) {
                    soldItemName = (String) skuObject.get("ITEMNAME");
                    skuId = (String) skuObject.get("SKU_ID");
                    extractedIndex = count;
                    break;
                }
            }
        }

        itemObject.put("SKU_ID", skuId);
        itemObject.put("ITEMNAME", soldItemName);

        if (extractedIndex == -1) {
            return itemObject;
        }

        Integer noOfVariants = 2;
        List<OCRRecommendationVariant> variants = new ArrayList<>();

        Integer variantRank = 0;
        for (int i = 0; i < skus.size(); i++) {

            if (i == extractedIndex) {
                continue;
            }

            variantRank++;
            OCRRecommendationVariant variant = new OCRRecommendationVariant();
            Map<String, Object> sku = skus.get(i);
            variant.setId((String) sku.get("SKU_ID"));
            variant.setItemname((String) sku.get("ITEMNAME"));
            variant.setVariantRank(variantRank);
            variants.add(variant);

            if (noOfVariants == variants.size()) {
                break;
            }
        }

        itemObject.put("VARIANTS", variants);

        return itemObject;
    }
}
