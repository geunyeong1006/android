package com.cyberone.android.listitem;

public class NewsDetailItem {
    private String regDtime;
    private String bbsTit;

    public NewsDetailItem(String regDtime, String bbsTit){
        this.regDtime = regDtime;
        this.bbsTit = bbsTit;
    }

    public String getRegDtime() {
        return regDtime;
    }

    public String getBbsTit() {
        return bbsTit;
    }
}

