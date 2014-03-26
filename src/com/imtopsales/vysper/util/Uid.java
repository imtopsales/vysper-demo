package com.imtopsales.vysper.util;

public class Uid {
    private String account;
    private String domain;
    
    public Uid(String email) {
        if (null != email) {
            email = email.trim().toLowerCase();
            if (email.length() > 0) {
                int idx = email.indexOf('@');
                this.account = email.substring(0, idx);
                this.domain = email.substring(idx+1);
            }
        }
    }
    public String getAccount() {
        return account;
    }
    public String getDomain() {
        return domain;
    }
    
    
}
