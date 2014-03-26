package com.imtopsales.vysper.service.impl;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.authorization.AccountCreationException;
import org.apache.vysper.xmpp.authorization.AccountManagement;
import org.apache.vysper.xmpp.authorization.UserAuthorization;

import com.imtopsales.vysper.service.QiyeApi;

/**
 * 用户相关的服务接口，需实现vysper的UserAuthorization和AccountManagement接口
 */
class UserService implements UserAuthorization, AccountManagement {
    private QiyeApi api;

    public UserService(QiyeApi api) {
        this.api = api;
    }

    @Override public void addUser(Entity username, String password)
            throws AccountCreationException {
        throw new AccountCreationException("请通过邮箱管理后台添加账户");
    }

    @Override public void changePassword(Entity username, String password)
            throws AccountCreationException {
        throw new AccountCreationException("请在邮箱中修改密码");
    }

    @Override public boolean verifyAccountExists(Entity jid) {
        return api.isAccountExists(jid.getBareJID());
    }

    @Override public boolean verifyCredentials(Entity jid, String passwordCleartext,
            Object credentials) {
        return api.verifyCredentials(jid, passwordCleartext);
    }

    @Override public boolean verifyCredentials(String username, String passwordCleartext,
            Object credentials) {
        return verifyCredentials(EntityImpl.parseUnchecked(username), passwordCleartext, credentials);  
    }

}
