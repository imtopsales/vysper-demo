package com.imtopsales.vysper.service.impl;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.modules.ServerRuntimeContextService;
import org.apache.vysper.xmpp.modules.roster.Roster;
import org.apache.vysper.xmpp.modules.roster.RosterException;
import org.apache.vysper.xmpp.modules.roster.RosterItem;
import org.apache.vysper.xmpp.modules.roster.persistence.RosterManager;

import com.imtopsales.vysper.service.QiyeApi;

/**
 * 部门组织架构相关的服务接口，需实现vysper的RosterManager和ServerRuntimeContextService接口
 */
class RosterService implements RosterManager,
        ServerRuntimeContextService {
    
    private QiyeApi api;

    public RosterService(QiyeApi api) {
        this.api = api;
    }

    @Override public String getServiceName() {
        return RosterManager.SERVER_SERVICE_ROSTERMANAGER;
    }

    @Override public Roster retrieve(Entity jid) throws RosterException {
        return api.retrieveRoster(jid);
    }

    @Override public void addContact(Entity jid, RosterItem rosterItem)
            throws RosterException {
        throw new RosterException("不支持此操作，请联系贵司邮箱管理员添加新账户");
    }

    @Override public RosterItem getContact(Entity jidUser, Entity jidContact)
            throws RosterException {
        if (null == jidUser) {
            return null;
        }
        
        Roster roster = retrieve(jidUser);
        if (null == roster) {
            return null;
        }
        
        return roster.getEntry(jidContact);  
    }

    @Override public void removeContact(Entity jid, Entity jidContact)
            throws RosterException {
        throw new RosterException("不支持此操作，请联系贵司邮箱管理员删除该账户");
    }

}
