package org.apache.nifi.processors.daxoperation.dm;

public class DeeplinkRequestData {
    private String pid;
    private String c;
    private String mobileNumber;
    private String carePersonaId;
    private boolean is_retargeting;
    private String deep_link_value;
    private String af_dp;
    private String af_ios_url;
    private boolean deferredDeepLink;

    public boolean getDeferredDeepLink() {
        return deferredDeepLink;
    }

    public void setDeferredDeepLink(boolean deferredDeepLink) {
        this.deferredDeepLink = deferredDeepLink;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getCarePersonaId() {
        return carePersonaId;
    }

    public void setCarePersonaId(String carePersonaId) {
        this.carePersonaId = carePersonaId;
    }

    public boolean isIs_retargeting() {
        return is_retargeting;
    }

    public void setIs_retargeting(boolean is_retargeting) {
        this.is_retargeting = is_retargeting;
    }

    public String getDeep_link_value() {
        return deep_link_value;
    }

    public void setDeep_link_value(String deep_link_value) {
        this.deep_link_value = deep_link_value;
    }

    public String getAf_dp() {
        return af_dp;
    }

    public void setAf_dp(String af_dp) {
        this.af_dp = af_dp;
    }

    public String getAf_ios_url() {
        return af_ios_url;
    }

    public void setAf_ios_url(String af_ios_url) {
        this.af_ios_url = af_ios_url;
    }
}
