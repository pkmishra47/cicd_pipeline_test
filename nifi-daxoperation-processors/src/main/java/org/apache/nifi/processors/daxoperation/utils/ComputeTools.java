package org.apache.nifi.processors.daxoperation.utils;



import org.apache.nifi.processors.daxoperation.bo.ToolData;
import org.apache.nifi.processors.daxoperation.bo.ToolParameters;

import java.util.List;

public class ComputeTools {
    public static ToolData calculateToolsResult(String toolName, ToolData toolData) {
        List<ToolParameters> toolParams = toolData.getToolParameters();
        double value;
        String result = "";

        if (toolName.equalsIgnoreCase("bmi")) {
            // bmi
            ToolParameters bmiTp = new ToolParameters();
            bmiTp.setTestName("Bmi");

            double height = getParamValue("Height", toolParams);
            double weight = getParamValue("Weight", toolParams);
            height = Double.parseDouble(convertBmiData(String.valueOf(height), toolParams.get(0).getUnits()));
            weight = Double.parseDouble(convertBmiData(String.valueOf(weight), toolParams.get(1).getUnits()));
            double bmi = getParamValue("Bmi", toolParams);
            if (bmi < 1) {
                value = weight / (height * height);
                bmiTp.setTestValue(String.valueOf(Math.round(value*100.0)/100.0));
                bmiTp.setParamType("result");
                toolData.getToolParameters().add(bmiTp);
            } else {
                value = bmi;
            }

            ToolParameters resultTp = new ToolParameters();
            resultTp.setTestName("Bmi Result");

            if (value <= 16.5)
                result = "Severely underweight";
            if (value > 16.5 && value <= 18.5)
                result = "Underweight";
            if (value > 18.5 && value <= 25)
                result = "Normal";
            if (value > 25 && value <= 30)
                result = "OverWeight";
            if (value > 30 && value <= 35)
                result = "Obese Class I";
            if (value > 35 && value <= 40)
                result = "Obese Class II";
            if (value > 40)
                result = "Obese Class III";

            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("blood glucose")) {
            // sugarLevel
            ToolParameters resultTp = new ToolParameters();
            resultTp.setTestName("Observation");
            value = getParamValue("Blood sugar level", toolParams);

            if (toolParams.get(1).getUnits().equalsIgnoreCase("Fasting")) {
                if (value < 75)
                    result = "Needs immediate attention, PLS Contact your nearest Apollo Sugar clinic";
                else if (value > 140)
                    result = "Oops! This might need attention. Contact your local Apollo Sugar clinic";
                else
                    result = "Congratulations! Your Blood Sugar readings are in control";
            } else {
                if (value < 75)
                    result = "Needs immediate attention, PLS Contact your nearest Apollo Sugar clinic";
                else if (value > 200)
                    result = "Oops! This might need attention. Contact your local Apollo Sugar clinic";
            }

            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("Post Prandial Blood Sugar")) {
            // sugarLevel
            ToolParameters resultTp = new ToolParameters();
            resultTp.setTestName("Observation");
            value = getParamValue("Post Prandial Blood Sugar", toolParams);
            result = "";
            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("Random Blood Sugar")) {
            // sugarLevel
            ToolParameters resultTp = new ToolParameters();
            resultTp.setTestName("Observation");
            value = getParamValue("Random Blood Sugar", toolParams);
            result = "";
            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("Fasting Blood Sugar")) {
            // sugarLevel
            ToolParameters resultTp = new ToolParameters();
            resultTp.setTestName("Observation");
            value = getParamValue("Fasting Blood Sugar", toolParams);
            result = "";
            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("Hemoglobin A1c")) {
            // sugarLevel
            ToolParameters resultTp = new ToolParameters();
            resultTp.setTestName("Observation");
            value = getParamValue("Hemoglobin A1c", toolParams);
            if (value < 6.0)
                result = "";
            else if(value >= 6.0 && value <= 6.4)
                result = "";
            else if(value > 6.4 && value < 8)
                result = "";
            else if(value >= 8)
                result = "";
            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("Pulse OxyMeter")) {
            //Beats per minute
            ToolParameters beatsTp = new ToolParameters();
            beatsTp.setTestName("Observation");
            value = getParamValue("Beats per minute", toolParams);
            result = "";
            beatsTp.setTestValue(result);
            beatsTp.setParamType("result");
            toolData.getToolParameters().add(beatsTp);
            // Oxygen level
            ToolParameters oxyTp = new ToolParameters();
            oxyTp.setTestName("Observation");
            value = getParamValue("Oxygen Level", toolParams);
            result = "";
            oxyTp.setTestValue(result);
            oxyTp.setParamType("result");
            toolData.getToolParameters().add(oxyTp);
        } else if (toolName.equalsIgnoreCase("Temperature")) {
            // temperature
            ToolParameters resultTp = new ToolParameters();
            resultTp.setTestName("Observation");
            value = getParamValue("Temperature", toolParams);

            if (value < 36)
                result = "Needs immediate attention, PLS Contact your nearest Apollo clinic/Hospital";
            else if (value > 37)
                result = "Oops! This might need attention. Contact your local Apollo clinic/Hospital";
            else
                result = " Your body temperature in optimal;";

            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("Cholesterol")) { // Multiple
            // totalCholestral

            ToolParameters totalTp = new ToolParameters();
            totalTp.setTestName("Total Cholestral");
            value = getParamValue("Total", toolParams);

            result = "Desirable";
            if (value >= 200)
                result = "Undesirable";

            totalTp.setTestValue(result);
            totalTp.setParamType("result");
            toolData.getToolParameters().add(totalTp);

            // ldlCholestral

            ToolParameters ldlTp = new ToolParameters();
            ldlTp.setTestName("LDL Cholestral");

            value = getParamValue("Ldl", toolParams);
            String ldlResult = "Desirable";
            if (value >= 130)
                ldlResult = "Undesirable";

            ldlTp.setTestValue(ldlResult);
            ldlTp.setParamType("result");
            toolData.getToolParameters().add(ldlTp);

            // hdlCholestral

            ToolParameters hdlTp = new ToolParameters();
            hdlTp.setTestName("HDL Cholestral");

            value = getParamValue("Hdl", toolParams);
            String hdlResult = "Desirable";
            if (value <= 40)
                hdlResult = "Undesirable";

            hdlTp.setTestValue(hdlResult);
            hdlTp.setParamType("result");
            toolData.getToolParameters().add(hdlTp);
        } else if (toolName.equalsIgnoreCase("pre-mix insulin (bid)")) {
            // sugarLevel

            ToolParameters resultTp = new ToolParameters();
            value = getParamValue("Blood sugar level", toolParams);

            if (toolParams.get(1).getUnits().equalsIgnoreCase("Fasting")) {
                if (value <= 70)
                    result = "Reading of <70 indicate Hypoglycemia <br>" + "Act Fast - Follow - '15-15' rule… <br>"
                            + "Take 15gms of glucose or hard candies or a teaspoon of honey/jam, wait for 15 minutes, "
                            + "and then test - if blood glucose level (>70mg/dl) then follow with a sandwich or a meal. "
                            + "If not, take a second dose of 15 grams and test again. "
                            + "Repeat the 15-15 rule until your symptoms are relieved";
                if (value >= 71 && value <= 90)
                    result = "Reduce your night dose of 'pre-mix insulin' by 2 units  ";
                if (value >= 91 && value <= 130)
                    result = "Congrats- Your sugar reading indicate good control – keep it up";
                if (value >= 131 && value <= 180)
                    result = "Increase your night dose of 'pre-mix insulin' by 2 units";
                if (value >= 181 && value <= 600)
                    result = "Increase your night dose of 'pre-mix insulin' by 4 units";
                if (value >= 601)
                    result = "Schedule appointment with your consultant/ reach the emergency (Call-1066)";
            } else if (toolParams.get(1).getUnits().equalsIgnoreCase("Post Breakfast")) {
                if (value <= 70)
                    result = "Reading of <70 indicate Hypoglycemia <br>" + "Act Fast - Follow - '15-15' rule… <br>"
                            + "Take 15gms of glucose or hard candies or a teaspoon of honey/jam, wait for 15 minutes, "
                            + "and then test - if blood glucose level (>70mg/dl) then follow with a sandwich or a meal. "
                            + "If not, take a second dose of 15 grams and test again. "
                            + "Repeat the 15-15 rule until your symptoms are relieved";
                if (value >= 71 && value <= 110)
                    result = "Reduce your 'pre-breakfast pre-mix insulin' by 2 units";
                if (value >= 111 && value <= 180)
                    result = "Congrats- Your sugar reading indicate good control – keep it up";
                if (value >= 181 && value <= 220)
                    result = "Increase your 'pre-breakfast pre-mix insulin' by 2 units";
                if (value >= 221 && value <= 600)
                    result = "Increase 'pre-breakfast pre-mix insulin' by 4 units";
                if (value >= 601)
                    result = "Schedule appointment with your consultant/ reach the emergency (Call-1066)";
            } else { // Post Dinner.
                if (value <= 70)
                    result = "Reading of <70 indicate Hypoglycemia <br>" + "Act Fast - Follow - '15-15' rule… <br>"
                            + "Take 15gms of glucose or hard candies or a teaspoon of honey/jam, wait for 15 minutes, "
                            + "and then test - if blood glucose level (>70mg/dl) then follow with a sandwich or a meal. "
                            + "If not, take a second dose of 15 grams and test again. "
                            + "Repeat the 15-15 rule until your symptoms are relieved";
                if (value >= 71 && value <= 110)
                    result = "Reduce your 'pre-breakfast pre-mix insulin' by 2 units";
                if (value >= 111 && value <= 180)
                    result = "Congrats- Your sugar reading indicate good control – keep it up";
                if (value >= 181 && value <= 220)
                    result = "Increase your 'pre-breakfast pre-mix insulin' by 2 units";
                if (value >= 221 && value <= 600)
                    result = "Increase 'pre-breakfast pre-mix insulin' by 4 units";
                if (value >= 601)
                    result = "Schedule appointment with your consultant/ reach the emergency (Call-1066)";
            }

            resultTp.setTestName(toolParams.get(1).getUnits());
            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("fasting glucose")) {
            // sugarLevel

            ToolParameters resultTp = new ToolParameters();
            resultTp.setTestName("Observation");
            value = getParamValue("Blood sugar level", toolParams);

            if (value > 110)
                result = "Diabetics";
            if (value >= 70 && value < 110)
                result = "Normal";
            if (value < 70)
                result = "Low Sugar";

            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("basal bolus insulin regimen")) {
            // sugarLevel

            ToolParameters resultTp = new ToolParameters();
            value = getParamValue("Value", toolParams);

            if (toolParams.get(1).getUnits().equalsIgnoreCase("Fasting")) {
                if (value <= 70)
                    result = "Reading of <70 indicate Hypoglycemia <br>" + "Act Fast - Follow - '15-15' rule… <br>"
                            + "Take 15gms of glucose or hard candies or a teaspoon of honey/jam, wait for 15 minutes, "
                            + "and then test - if blood glucose level (>70mg/dl) then follow with a sandwich or a meal. "
                            + "If not, take a second dose of 15 grams and test again. "
                            + "Repeat the 15-15 rule until your symptoms are relieved";
                if (value >= 71 && value <= 90)
                    result = "Reduce your night dose of 'long acting insulin' by 2 units";
                if (value >= 91 && value <= 130)
                    result = "Congrats- Your sugar reading indicate good control – keep it up";
                if (value >= 131 && value <= 180)
                    result = "Increase your night dose of 'long acting insulin' by 2 units";
                if (value >= 181 && value <= 600)
                    result = "Increase your night dose of 'long acting insulin' by 4 units";
                if (value >= 601)
                    result = "Contact the ER";
            } else if (toolParams.get(1).getUnits().equalsIgnoreCase("Post Breakfast")) {
                if (value <= 70)
                    result = "Reading of <70 indicate Hypoglycemia <br>" + "Act Fast - Follow - '15-15' rule… <br>"
                            + "Take 15gms of glucose or hard candies or a teaspoon of honey/jam, wait for 15 minutes, "
                            + "and then test - if blood glucose level (>70mg/dl) then follow with a sandwich or a meal. "
                            + "If not, take a second dose of 15 grams and test again. "
                            + "Repeat the 15-15 rule until your symptoms are relieved";
                if (value >= 71 && value <= 110)
                    result = "Reduce your 'pre-breakfast short acting insulin' by 2 units";
                if (value >= 111 && value <= 180)
                    result = "Congrats- Your sugar reading indicate good control – keep it up";
                if (value >= 181 && value <= 220)
                    result = "Increase your 'pre-breakfast short acting insulin' by 2 units";
                if (value >= 221 && value <= 600)
                    result = "Increase your night dose of 'pre-breakfast short acting insulin' by 4 units";
                if (value >= 601)
                    result = "Contact the ER";
            } else if (toolParams.get(1).getUnits().equalsIgnoreCase("Post Lunch")) {
                if (value <= 70)
                    result = "Reading of <70 indicate Hypoglycemia <br>" + "Act Fast - Follow - '15-15' rule… <br>"
                            + "Take 15gms of glucose or hard candies or a teaspoon of honey/jam, wait for 15 minutes, "
                            + "and then test - if blood glucose level (>70mg/dl) then follow with a sandwich or a meal. "
                            + "If not, take a second dose of 15 grams and test again. "
                            + "Repeat the 15-15 rule until your symptoms are relieved";
                if (value >= 71 && value <= 110)
                    result = "Reduce your 'pre-lunch short acting insulin' by 2 units";
                if (value >= 111 && value <= 180)
                    result = "Congrats- Your sugar reading indicate good control – keep it up";
                if (value >= 181 && value <= 220)
                    result = "Increase your 'pre-lunch short acting insulin' by 4 units";
                if (value >= 221 && value <= 600)
                    result = "Increase your 'pre-lunch short acting insulin' by 4 units";
                if (value >= 601)
                    result = "Schedule appointment with your consultant/ reach the emergency (Call-1066)";
            } else if (toolParams.get(1).getUnits().equalsIgnoreCase("Post Dinner")) {
                if (value <= 70)
                    result = "Reading of <70 indicate Hypoglycemia <br>" + "Act Fast - Follow - '15-15' rule… <br>"
                            + "Take 15gms of glucose or hard candies or a teaspoon of honey/jam, wait for 15 minutes, "
                            + "and then test - if blood glucose level (>70mg/dl) then follow with a sandwich or a meal. "
                            + "If not, take a second dose of 15 grams and test again. "
                            + "Repeat the 15-15 rule until your symptoms are relieved";
                if (value >= 71 && value <= 110)
                    result = "Reduce your 'pre-dinner short acting insulin' by 2 units";
                if (value >= 111 && value <= 180)
                    result = "Congrats- Your sugar reading indicate good control – keep it up";
                if (value >= 181 && value <= 220)
                    result = "Increase your 'pre-dinner short acting insulin' by 2 units";
                if (value >= 221 && value <= 600)
                    result = "Increase your 'pre-dinner short acting insulin' by 4 units";
                if (value >= 601)
                    result = "Schedule appointment with your consultant/ reach the emergency (Call-1066)";
            }
            resultTp.setTestName(toolParams.get(1).getUnits());
            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("blood pressure")) {
            // diastolic

            ToolParameters resultTp = new ToolParameters();
            resultTp.setTestName("Diastolic Observation");
            value = getParamValue("Diastolic", toolParams);

            String diastolicResult = "Low Blood Pressure";
            int diastolicScore = 0;
            if (value >= 60 && value < 80) {
                diastolicResult = "Normal";
                diastolicScore = 1;
            } else if (value >= 80 && value < 90) {
                diastolicResult = "Pre-Hypertension";
                diastolicScore = 2;
            } else if (value >= 90 && value < 100) {
                diastolicResult = "Hypertension, Stage 1";
                diastolicScore = 3;
            } else if (value >= 100) {
                diastolicResult = "Hypertension, Stage 2";
                diastolicScore = 4;
            }

            resultTp.setTestValue(diastolicResult);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);

            // systolic

            resultTp = new ToolParameters();
            resultTp.setTestName("Systolic Observation");
            value = getParamValue("Systolic", toolParams);
            String systolicResult = "Low Blood Pressure";
            int systolicScore = 0;

            if (value >= 80 && value < 120) {
                systolicResult = "Normal";
                systolicScore = 1;
            } else if (value >= 120 && value < 140) {
                systolicResult = "Pre-Hypertension";
                systolicScore = 2;
            } else if (value >= 140 && value < 160) {
                systolicResult = "Hypertension, Stage 1";
                systolicScore = 3;
            } else if (value >= 160) {
                systolicResult = "Hypertension, Stage 2";
                systolicScore = 4;
            }

            if (diastolicScore <= systolicScore)
                result = systolicResult;
            else
                result = diastolicResult;
            resultTp.setTestValue(systolicResult);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("resting heart rate")) {
            // heartrate

            ToolParameters resultTp = new ToolParameters();
            resultTp.setTestName("Heart Rate");
            value = getParamValue("Beats per minute", toolParams);

            if (value >= 49f && value <= 62f)
                result = "Excellent";
            else if (value >= 63f && value <= 70f)
                result = "Good";
            else if (value >= 71f && value <= 74f)
                result = "Average";
            else if (value >= 75f)
                result = "Poor";
            else
                result = "";

            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("single long acting insulin")) {
            // sugarLevel

            ToolParameters resultTp = new ToolParameters();
            value = getParamValue("Blood sugar level", toolParams);

            if (value <= 70)
                result = "Reading of <= 70 indicate Hypoglycemia </br>" + "Act Fast - Follow - '15-15' rule… </br>"
                        + "Take 15gms of glucose or hard candies or a teaspoon of honey/jam, wait for 15 minutes, "
                        + "and then test - if blood glucose level (>70mg/dl) then follow with a sandwich or a meal. "
                        + "If not, take a second dose of 15 grams and test again. "
                        + "Repeat the 15-15 rule until your symptoms are relieved";
            if (value >= 71 && value <= 90)
                result = "Reduce your night dose of 'long acting insulin' by 2 units";
            if (value >= 91 && value <= 130)
                result = "Congrats- Your sugar reading indicate good control – keep it up";
            if (value >= 131 && value <= 180)
                result = "Increase your night dose of 'long acting insulin' by 2 units";
            if (value >= 181 && value <= 600)
                result = "Increase your night dose of 'long acting insulin' by 4 units";
            if (value >= 601)
                result = "Schedule appointment with your consultant/ reach the emergency";

            resultTp.setTestName(toolParams.get(1).getUnits());
            resultTp.setTestValue(result);
            resultTp.setParamType("result");
            toolData.getToolParameters().add(resultTp);
        } else if (toolName.equalsIgnoreCase("body fat percentage")) {
            // body fat percentage

            ToolParameters bfpTp = new ToolParameters();
            bfpTp.setTestName("Body Fat Percentage");

            double height = getParamValue("Height", toolParams);
            double weight = getParamValue("Weight", toolParams);
            height = Double.parseDouble(convertBmiData(String.valueOf(height), toolParams.get(0).getUnits()));
            weight = Double.parseDouble(convertBmiData(String.valueOf(weight), toolParams.get(1).getUnits()));
            value = weight / (height * height);
            int genderIndex = 0;
            if (toolParams.get(2).getUnits().equalsIgnoreCase("male"))
                genderIndex = 1;
            int age = (int) Math.round(getParamValue("Age", toolParams));
            float bodyFat;
            if (age >= 0 && age <= 12)
                bodyFat = (float) ((1.51 * value) - (0.70 * age) - (3.6 * genderIndex) + 1.4);
            else
                bodyFat = (float) ((1.20 * value) + (0.23 * age) - (10.8 * genderIndex) - 5.4);

            bfpTp.setTestValue(String.valueOf(Math.round(bodyFat*100.0)/100.0));
            bfpTp.setParamType("result");
            toolData.getToolParameters().add(bfpTp);

            if (genderIndex == 1) {
                ToolParameters resultTp = new ToolParameters();
                resultTp.setTestName("Result");
                if (bodyFat < 10)
                    result = "Low Level of Body Fat Percentage";
                if (bodyFat >= 10 && bodyFat < 14)
                    result = "Adequate Level of Body Fat Percentage";
                if (bodyFat >= 14 && bodyFat < 21)
                    result = "Athletic Level of Body Fat Percentage";
                if (bodyFat >= 21 && bodyFat < 25)
                    result = "Fitness Level of Body Fat Percentage";
                if (bodyFat >= 25 && bodyFat < 32)
                    result = "Average Level of Body Fat Percentage";
                if (bodyFat >= 32)
                    result = "Obese Level of Body Fat Percentage";
                resultTp.setTestValue(result);
                resultTp.setParamType("result");
                toolData.getToolParameters().add(resultTp);
            } else { // UNDEFINED is considered FEMALE.
                ToolParameters resultTp = new ToolParameters();
                resultTp.setTestName("Result");
                if (bodyFat < 2)
                    result = "Low Level of Body Fat Percentage";
                if (bodyFat >= 2 && bodyFat < 6)
                    result = "Adequate Level of Body Fat Percentage";
                if (bodyFat >= 6 && bodyFat < 14)
                    result = "Athletic Level of Body Fat Percentage";
                if (bodyFat >= 14 && bodyFat < 18)
                    result = "Fitness Level of Body Fat Percentage";
                if (bodyFat >= 18 && bodyFat < 25)
                    result = "Average Level of Body Fat Percentage";
                if (bodyFat >= 25)
                    result = "Obese Level of Body Fat Percentage";
                resultTp.setTestValue(result);
                resultTp.setParamType("result");
                toolData.getToolParameters().add(resultTp);
            }
        }
        return toolData;
    }

    private static double getParamValue(String paramName, List<ToolParameters> params) {
        for (ToolParameters param : params) {
            if (param.getTestName().equalsIgnoreCase(paramName))
                return Double.parseDouble(param.getTestValue());
        }
        return 0;
    }

    private static String convertBmiData(String value, String unit) {
        String val = value;
        if (unit != null) {
            if (unit.equals("ft"))
                val = String.format("%.2f", Double.parseDouble(value) * 0.3048);
            if (unit.equals("cm"))
                val = String.format("%.2f", Double.parseDouble(value) / 100);
            if (unit.equals("pounds"))
                val = String.format("%.2f", Double.parseDouble(value) / 2.2);
        }
        return val;
    }
}

