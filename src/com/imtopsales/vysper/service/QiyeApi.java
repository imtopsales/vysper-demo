package com.imtopsales.vysper.service;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.modules.roster.Roster;

public interface QiyeApi {
    /**
     * 判断账户是否存在
     */
    boolean isAccountExists(Entity bareJID);
    
    /**
     * 检查账户和密码是否正确
     */
    boolean verifyCredentials(Entity jid, String password);
    
    /**
     * 获取通讯录信息
     */
    Roster retrieveRoster(Entity jid);
}
