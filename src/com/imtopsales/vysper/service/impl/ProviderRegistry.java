package com.imtopsales.vysper.service.impl;

import org.apache.vysper.storage.OpenStorageProviderRegistry;
import org.apache.vysper.xmpp.modules.extension.xep0160_offline_storage.MemoryOfflineStorageProvider;

import com.imtopsales.vysper.service.QiyeApi;

/**
 * 需要继承vysper的OpenStorageProviderRegistry
 */
public class ProviderRegistry extends OpenStorageProviderRegistry {
    private QiyeApi api;
    
    public void setApi(QiyeApi api) {
        this.api = api;
    }
    
    public void init() {
        add(new UserService(api));
        add(new RosterService(api));
        
        // 离线留言, TODO 改用数据库实现?
        add(new MemoryOfflineStorageProvider());
    }
}
