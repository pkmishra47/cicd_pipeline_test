package org.apache.nifi.processors.daxoperation.dm;

import java.util.List;
import java.util.Map;

public class FFRealOutput {
    public String executionID;
    public SiteDetails siteDetails;
    public Map<String, SiteDetails> siteMasterMap ;
    public List<Patients> patients;
    public List<SpecialResult> specialResults;
    public String siteApiKey;
    public List<DischargeSummary> dischargeSummaries;
}
