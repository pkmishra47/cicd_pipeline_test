package org.apache.nifi.processors.daxoperation.utils;

import org.apache.nifi.processors.daxoperation.bo.*;
import org.apache.nifi.processors.daxoperation.dbo.*;
import org.bson.types.ObjectId;

import java.util.Comparator;
import java.util.Date;

public class Comparators {

    public static int stringCompare(String s1, String s2) {
        return s1.compareTo(s2);
    }

    public static int oIdCompare(ObjectId s1, ObjectId s2) {
        return s1.compareTo(s2);
    }

    public static int intCompare(int v1, int v2) {
        return (v1 < v2 ? -1 : (v1 == v2 ? 0 : 1));
    }

    public static int longCompare(long v1, long v2) {
        return (v1 < v2 ? -1 : (v1 == v2 ? 0 : 1));
    }

    public static int dateCompare(Date d1, Date d2) {
        return longCompare(d1.getTime(), d2.getTime());
    }

    public static Comparator<DBPrescription> newprescriptionComparator() {
        return new Comparator<DBPrescription>() {
            public int compare(DBPrescription p1, DBPrescription p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<DBAllergy> allergyComparator() {
        return new Comparator<DBAllergy>() {
            public int compare(DBAllergy p1, DBAllergy p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<DBImmunization> immunizationComparator() {
        return new Comparator<DBImmunization>() {
            public int compare(DBImmunization p1, DBImmunization p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<DBMedication> medicationComparator() {
        return new Comparator<DBMedication>() {
            public int compare(DBMedication p1, DBMedication p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    /*	public static <T> Comparator<?> tComparator() {
            return new Comparator<T>() {
                public int compare(T p1, T p2) {
                    return stringCompare(p1.getId().toString(), p2.getId().toString());
                }
            };
        }
    */
    public static Comparator<DBLabTest> labTestComparator() {
        return new Comparator<DBLabTest>() {
            public int compare(DBLabTest p1, DBLabTest p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<DBLabTest> labTestDateComparator() {
        return new Comparator<DBLabTest>() {
            public int compare(DBLabTest p1, DBLabTest p2) {
                return dateCompare(p1.getTestDate(), p2.getTestDate());
            }
        };
    }
    public static Comparator<DBDoctorVisit> docVisitComparator() {
        return new Comparator<DBDoctorVisit>() {
            public int compare(DBDoctorVisit p1, DBDoctorVisit p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }

    public static Comparator<DBHealthCheck> healthCheckComparator() {
        return new Comparator<DBHealthCheck>() {
            public int compare(DBHealthCheck p1, DBHealthCheck p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<DBFamilyHistory> familyHistoryComparator() {
        return new Comparator<DBFamilyHistory>() {
            public int compare(DBFamilyHistory p1, DBFamilyHistory p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<SiteMaster> siteMasterComparator(){
        return new Comparator<SiteMaster>(){
            public int compare(SiteMaster p1,SiteMaster p2){
                return stringCompare(p1.getSiteName().toString(),p2.getSiteName().toString());
            }
        };
    }


    public static Comparator<SleepPattern> sleepComparator(){
        return new Comparator<SleepPattern>(){
            public int compare(SleepPattern p1,SleepPattern p2){
                Date dt1 = new Date(p1.getStartDate().getTime());
                Date dt2 = new Date(p2.getStartDate().getTime());
                return dt1.compareTo(dt2);
            }
        };


    }

    public static Comparator<DairyDetails> dairyComparator(){
        return new Comparator<DairyDetails>(){
            public int compare(DairyDetails p1,DairyDetails p2){
                Date dt1 = new Date(p1.getStartDate().getTime());
                Date dt2 = new Date(p2.getStartDate().getTime());
                return dt1.compareTo(dt2);
            }
        };


    }


    public static Comparator<DBRestriction> restrictionsComparator() {
        return new Comparator<DBRestriction>() {
            public int compare(DBRestriction p1, DBRestriction p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }

    public static Comparator<DBHospitalization> hospitilizationComparator() {
        return new Comparator<DBHospitalization>() {
            public int compare(DBHospitalization p1, DBHospitalization p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<DBInsurance> insuranceComparator() {
        return new Comparator<DBInsurance>() {
            public int compare(DBInsurance p1, DBInsurance p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<DBMedicalCondition> medicalConditionComparator() {
        return new Comparator<DBMedicalCondition>() {
            public int compare(DBMedicalCondition p1, DBMedicalCondition p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<DBProcedure> procedureComparator() {
        return new Comparator<DBProcedure>() {
            public int compare(DBProcedure p1, DBProcedure p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<DBTool> toolComparator() {
        return new Comparator<DBTool>() {
            public int compare(DBTool p1, DBTool p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<DBAddress> addressComparator() {
        return new Comparator<DBAddress>() {
            public int compare(DBAddress p1, DBAddress p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }
    public static Comparator<DBReminder> reminderComparator() {
        return new Comparator<DBReminder>() {
            public int compare(DBReminder o1, DBReminder o2) {
                return stringCompare(o1.getId().toString(), o2.getId().toString());
            }
        };
    }

    public static Comparator<DBToolData> toolsDataComparator() {
        return new Comparator<DBToolData>() {
            public int compare(DBToolData p1, DBToolData p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }

    public static Comparator<ToolData> toolsDataComparatorByDate() {
        return new Comparator<ToolData>() {
            public int compare(ToolData p1, ToolData p2) {
                return longCompare(p2.getDateAndTime().getTime(), p1.getDateAndTime().getTime());
            }
        };
    }

//    public static Comparator<SugarToolInfo> sugraToolInfoComparator() {
//        return new Comparator<SugarToolInfo>() {
//            public int compare(SugarToolInfo p1, SugarToolInfo p2) {
//                return longCompare(p2.getInputDate(), p1.getInputDate());
//            }
//        };
//    }

//    public static Comparator<HRATest> hraTestComparatorByDate() {
//        return new Comparator<HRATest>() {
//            public int compare(HRATest p1, HRATest p2) {
//                return longCompare(p2.getDateTaken(), p1.getDateTaken());
//            }
//        };
//    }

    public static Comparator<LabTest> labTestComparatorByDate() {
        return new Comparator<LabTest>() {
            public int compare(LabTest p1, LabTest p2) {
                return longCompare(p2.getTestDate().getTime(), p1.getTestDate().getTime());
            }
        };
    }

    public static Comparator<SiteStats> siteStatComparatorByDate() {
        return new Comparator<SiteStats>() {
            public int compare(SiteStats p1, SiteStats p2) {
                return longCompare(p2.getStatDate().getTime(), p1.getStatDate().getTime());
            }
        };
    }

    public static Comparator<Restriction> restrictionComparatorByDate() {
        return new Comparator<Restriction>() {
            public int compare(Restriction p1, Restriction p2) {
                return longCompare(p2.getStartDate().getTime(), p1.getStartDate().getTime());
            }
        };
    }

    public static Comparator<Allergy> allergyComparatorByDate() {
        return new Comparator<Allergy>() {
            public int compare(Allergy p1, Allergy p2) {
                return longCompare(p2.getStartDate().getTime(), p1.getStartDate().getTime());
            }
        };
    }

    public static Comparator<DoctorVisit> doctorVisitComparatorByDate() {
        return new Comparator<DoctorVisit>() {
            public int compare(DoctorVisit p1, DoctorVisit p2) {
                return longCompare(p2.getDateOfVisit().getTime(), p1.getDateOfVisit().getTime());
            }
        };
    }

//    public static Comparator<Hospitalization> hospitalizationComparatorByDate() {
//        return new Comparator<Hospitalization>() {
//            public int compare(Hospitalization p1, Hospitalization p2) {
//                return longCompare(p2.getDateOfHospitalization(), p1.getDateOfHospitalization());
//            }
//        };
//    }

    public static Comparator<Prescription> prescriptionComparatorByDate() {
        return new Comparator<Prescription>() {
            public int compare(Prescription p1, Prescription p2) {
                return longCompare(p2.getDateOfPrescription().getTime(), p1.getDateOfPrescription().getTime());
            }
        };
    }

    public static Comparator<Immunization> immunizationComparatorByDate() {
        return new Comparator<Immunization>() {
            public int compare(Immunization p1, Immunization p2) {
                return longCompare(p2.getDateOfImmunization().getTime(), p1.getDateOfImmunization().getTime());
            }
        };
    }

    public static Comparator<Insurance> insuranceComparatorByDate() {
        return new Comparator<Insurance>() {
            public int compare(Insurance p1, Insurance p2) {
                return longCompare(p2.getStartDate().getTime(), p1.getStartDate().getTime());
            }
        };
    }

    public static Comparator<MedicalCondition> medicalConditionComparatorByDate() {
        return new Comparator<MedicalCondition>() {
            public int compare(MedicalCondition p1, MedicalCondition p2) {
                return longCompare(p2.getStartDate().getTime(), p1.getStartDate().getTime());
            }
        };
    }

    public static Comparator<Medication> medicationComparatorByDate() {
        return new Comparator<Medication>() {
            public int compare(Medication p1, Medication p2) {
                return longCompare(p2.getStartDate().getTime(), p1.getStartDate().getTime());
            }
        };
    }

    public static Comparator<Medication> medicationComparatorByName() {
        return new Comparator<Medication>() {
            public int compare(Medication p1, Medication p2) {
                return stringCompare(p2.getMedicineName(), p1.getMedicineName());
            }
        };
    }

    public static Comparator<Procedure> procedureComparatorByDate() {
        return new Comparator<Procedure>() {
            public int compare(Procedure p1, Procedure p2) {
                return longCompare(p2.getStartDate().getTime(), p1.getStartDate().getTime());
            }
        };
    }

    public static Comparator<HealthCheck> healthCheckComparatorByDate() {
        return new Comparator<HealthCheck>() {
            public int compare(HealthCheck p1, HealthCheck p2) {
                return longCompare(p2.getHealthCheckDate().getTime(), p1.getHealthCheckDate().getTime());
            }
        };
    }

    public static Comparator<DBExpertChatAppointment> expertChatComparator() {
        return new Comparator<DBExpertChatAppointment>() {
            public int compare(DBExpertChatAppointment p1, DBExpertChatAppointment p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }

    public static Comparator<DBHRATest> hraTestComparator() {
        return new Comparator<DBHRATest>() {
            public int compare(DBHRATest p1, DBHRATest p2) {
                return stringCompare(p1.getId().toString(), p2.getId().toString());
            }
        };
    }

    public static Comparator<Integer> integerComparator() {
        return new Comparator<Integer>() {
            public int compare(Integer o1, Integer o2) {
                int val1 = o1.intValue();
                int val2 = o2.intValue();
                return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
            }
        };
    }

    public static Comparator<Date> dateComparator() {
        return new Comparator<Date>() {
            public int compare(Date o1, Date o2) {
                long val1 = o1.getTime();
                long val2 = o2.getTime();
                return (val1 < val2 ? -1 : (val1 == val2 ? 0 : 1));
            }
        };
    }

    public static Comparator<DBUser> dbUserComparator() {
        return new Comparator<DBUser>() {
            public int compare(DBUser o1, DBUser o2) {
                return stringCompare(o1.getId().toString(), o2.getId().toString());
            }
        };
    }

    public static Comparator<DBCircle> dbCircleComparator() {
        return new Comparator<DBCircle>() {
            public int compare(DBCircle m1, DBCircle m2) {
                return stringCompare(m1.getId().toString(), m2.getId().toString());
            }
        };
    }

    public static Comparator<DBMedicalImage> medicalImageComparator() {
        return new Comparator<DBMedicalImage>() {
            public int compare(DBMedicalImage m1, DBMedicalImage m2) {
                return stringCompare(m1.getId().toString(), m2.getId().toString());
            }
        };
    }

    public static Comparator<MedicalImage> medicalImageComparatorByDate() {

        return new Comparator<MedicalImage>() {
            public int compare(MedicalImage p1, MedicalImage p2) {
                return longCompare(p2.getDateOfImaging().getTime(), p1.getDateOfImaging().getTime());
            }
        };
    }

    public static Comparator<DBMedicalFile> medicalFileComparator() {
        return new Comparator<DBMedicalFile>() {
            public int compare(DBMedicalFile m1, DBMedicalFile m2) {
                return stringCompare(m1.getId().toString(), m2.getId().toString());
            }
        };
    }
    public static Comparator<DBUploadFile> uploadFileComparator() {
        return new Comparator<DBUploadFile>() {
            public int compare(DBUploadFile m1, DBUploadFile m2) {
                return stringCompare(m1.getId().toString(), m2.getId().toString());
            }
        };
    }

    public static Comparator<ObjectId> ObjectIdComparator() {
        return new Comparator<ObjectId>() {
            public int compare(ObjectId m1, ObjectId m2) {
                return oIdCompare(m1, m2);
            }
        };
    }

    public static Comparator<MedicalFile> medicalFileComparatorByDate() {

        return new Comparator<MedicalFile>() {
            public int compare(MedicalFile p1, MedicalFile p2) {
                return longCompare(p2.getUploadedDate().getTime(), p1.getUploadedDate().getTime());
            }
        };
    }

//    public static Comparator<CorpReport> corpUserComparator() {
//        return new Comparator<CorpReport>() {
//            public int compare(CorpReport p1, CorpReport p2) {
//                return longCompare(p2.getDate(), p1.getDate());
//            }
//        };
//    }
}
