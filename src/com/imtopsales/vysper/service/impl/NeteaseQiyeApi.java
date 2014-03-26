package com.imtopsales.vysper.service.impl;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.addressing.EntityFormatException;
import org.apache.vysper.xmpp.addressing.EntityImpl;
import org.apache.vysper.xmpp.modules.roster.AskSubscriptionType;
import org.apache.vysper.xmpp.modules.roster.MutableRoster;
import org.apache.vysper.xmpp.modules.roster.Roster;
import org.apache.vysper.xmpp.modules.roster.RosterGroup;
import org.apache.vysper.xmpp.modules.roster.RosterItem;
import org.apache.vysper.xmpp.modules.roster.SubscriptionType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.imtopsales.vysper.service.QiyeApi;
import com.imtopsales.vysper.util.RSAUtil;
import com.imtopsales.vysper.util.StringMap;
import com.imtopsales.vysper.util.Uid;
import com.imtopsales.vysper.util.google.Lists;
import com.imtopsales.vysper.util.google.Maps;
import com.imtopsales.vysper.util.google.Sets;

/**
 * 网易企业邮箱接口实现
 */
public class NeteaseQiyeApi implements QiyeApi {
    private static final Logger LOG = Logger.getLogger(NeteaseQiyeApi.class);
    
    /**
     * 分组/部门
     */
    static class TinyGroup {
        TinyGroup parent;
        String id;
        String name;
        public TinyGroup(String parentId, String id, String name) {
            this.id = id;
            this.name = name;
            if (null != parentId) {
                this.parent = new TinyGroup(null, parentId, null);
            }
        }
        @Override public String toString() {
            return "{name:" + name + "}"; 
        }
    }
    
    /**
     * 用户
     */
    static class TinyUser {
        TinyGroup group;
        String account;
        String name;
        public TinyUser(TinyGroup group, String account, String name) {
            this.group = group;
            this.account = account;
            this.name = name;
        }
        @Override public String toString() {
            return "{name:" + name + "}"; 
        }
        public String getGroupName() {
            if (null == this.group.name) {
                return "默认";
            }
            StringBuilder buff = new StringBuilder(this.group.name);
            TinyGroup g = group.parent;
            while (null != g) {
                buff.insert(0, "|").insert(0, g.name);
                g = g.parent;
            }
            return buff.toString();
        }
    }
    
    /** 接口的服务器地址，详情可咨询网易客服 */
    private String apiHost;
    
    /** imap服务器地址，详情可咨询网易客服 */
    private String imapHost;
    
    /** cid，详情可咨询网易客服 */
    private String cid;
    
    /** rsa私钥 */
    private String rsaPrivateKey;
    
    private volatile List<TinyUser> cacheUsers;
    
    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public void setImapHost(String imapHost) {
        this.imapHost = imapHost;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setRsaPrivateKey(String rsaPrivateKey) {
        this.rsaPrivateKey = rsaPrivateKey;
    }

    @Override public boolean isAccountExists(Entity bareJID) {
        List<TinyUser> users = loadCacheUsers();
        if (null == users || users.size() < 1) {
            return false;
        }
        
        Uid uid = new Uid(bareJID.toString());
        for (TinyUser u : users) {
            if (u.account.equals(uid.getAccount())) {
                return true;
            }
        }
        
        return false;
    }
    
    @Override public boolean verifyCredentials(Entity jid, String password) {
        // 暂无验证账户密码的api，直接用imap协议顶着先...
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        Session session = Session.getInstance(props);
        URLName urlName = new URLName("imaps", imapHost, 993, null, jid.toString(), password);
        Store store = null;
        try {
            store = session.getStore(urlName);
            store.connect();
            return true;
        } catch (MessagingException e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("method:verifyCredentials,jid:" + jid + ",errorMsg:" + e.getMessage());
            }
            return false;
        } finally {
            if (null != store) {
                try {
                    store.close();
                } catch (Exception e) {
                }
            }
        }
    }

    @Override public Roster retrieveRoster(Entity jid) {
        Uid uid = new Uid(jid.toString());
        MutableRoster roster = new MutableRoster();
        
        List<TinyUser> users = loadCacheUsers();
        
        if (null == users || users.size() < 1) {
            return roster;
        }
        
        for (TinyUser u : users) {
            // 忽略当前账户
            if (u.account.equals(uid.getAccount())) {
                continue;
            }
            
            try {
                Entity ujid = EntityImpl.parse(u.account + "@" + uid.getDomain());
                List<RosterGroup> groups = Lists.newArrayList();
                String groupName = u.getGroupName();
                if (null != groupName) {
                    groups.add(new RosterGroup(groupName));
                }
                RosterItem rosterItem = new RosterItem(
                        ujid, 
                        u.name, 
                        SubscriptionType.BOTH,   
                        AskSubscriptionType.NOT_SET, 
                        groups);
                roster.addItem(rosterItem);
            } catch (EntityFormatException e) {
                LOG.error("method:retrieveRoster,jid:" + jid + ",errorMsg:" + e.getMessage(), e);
            }
        }
        
        return roster;
    }
    
    /**
     * 获取通讯录中的所有账户
     */
    private List<TinyUser> loadCacheUsers() {
        // TODO 换成local cache或memcache什么的
        if (null == cacheUsers) {
            synchronized (this) {
                if (null == cacheUsers) {
                    cacheUsers = loadUsers();
                }
            }
        }
        
        return cacheUsers;
    }
    
    /**
     * 获取通讯录中的所有账户
     */
    private List<TinyUser> loadUsers() {
        // 获取部门信息
        String enc = RSAUtil.encryptWithPriKey(rsaPrivateKey, "action=getDepartmentList");
        String url = String.format("http://%s/domain/services/externalApi?cid=%s&enc=%s", apiHost, cid, enc);
        List<StringMap> items = execute(url);
        
        Map<String, TinyGroup> groupMap = Maps.newHashMap();
        if (null != items && items.size() > 0) {
            for (StringMap item : items) {
                String unitId = item.get("ouid");
                String parentId = null;
                if (item.containsKey("parent_org_unit_id")) {
                    parentId = item.get("parent_org_unit_id");
                }
                String unitName = item.get("org_unit_name");
                groupMap.put(unitId, new TinyGroup(parentId, unitId, unitName));
            }
            
            for (String unitId : groupMap.keySet()) {
                TinyGroup unit = groupMap.get(unitId);
                if (null != unit.parent) {
                    unit.parent = groupMap.get(unit.parent.id);
                }
            }
        }
        
        // 获取所有账户
        Set<String> dupl = Sets.newHashSet();
        List<TinyUser> userList = Lists.newArrayList();
        
        for (String unitId : groupMap.keySet()) {
            enc = RSAUtil.encryptWithPriKey(rsaPrivateKey, String.format("action=getAccountList&ouid=%s&recursion=true", unitId));
            url = String.format("http://%s/domain/services/externalApi?cid=%s&enc=%s", apiHost, cid, enc);
            items = execute(url);
            
            if (null == items || items.size() < 1) {
                continue;
            }
            
            for (StringMap item : items) {
                String accountName = item.get("account_name");
                if (dupl.contains(accountName)) {
                    continue;
                }
                dupl.add(accountName);
                
                userList.add(new TinyUser(
                        groupMap.get(unitId), 
                        accountName, 
                        item.get("nickname")));
            }
        }
        
        return userList;
    }
    
    /**
     * 借助httpclient，发起http请求，把请求body包装为JSONObject返回
     */
    private List<StringMap> execute(String url) {
        try {
            HttpPost httppost = new HttpPost(url);
            
            CloseableHttpClient httpClient = HttpClients.createDefault();
            
            HttpResponse resp = httpClient.execute(httppost);
            String body = decodeUtf8URL(EntityUtils.toString(resp.getEntity()));
            String[] response = null != body ? body.split("\r\n") : null;
            if (null == response) {
                return null;
            }
            if (3 != response.length) {
                LOG.error("method:execute,url:" + url + ",body:[" + body + "]");
                throw new IllegalStateException("not enough lines");
            }
            if (!"200".equals(response[0])) {
                LOG.error("method:execute,url:" + url + ",body:[" + body + "]");
                throw new IllegalStateException("status error " + response[0]);
            }
            
            String xmlBody = response[2];
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource(new StringReader(xmlBody)));
            
            List<StringMap> items = Lists.newArrayList();
            Node root = document.getFirstChild();
            NodeList children = root.getChildNodes();
            for (int i = 0, max = children.getLength(); i < max; i++) {
                Node child = children.item(i);
                NodeList subChildren = child.getChildNodes();
                StringMap sData = new StringMap();
                for (int j = 0, jMax = subChildren.getLength(); j < jMax; j++) {
                    Node n = subChildren.item(j);
                    String value = n.getTextContent();
                    if (null == value || (value = value.trim()).length() < 1) {
                        continue;
                    }
                    sData.put(n.getNodeName(), value);
                }
                items.add(sData);
            }
            
            return items;
        } catch (Exception e) {
            LOG.error("method:execute,url:" + url + ",errorMsg:" + e.getMessage(), e);
            return null;
        }
    }
    
    static String decodeUtf8URL(String data) {
        if (null == data || (data = data.trim()).length() < 1) {
            return data;
        }
        
        try {
            return URLDecoder.decode(data, "utf-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("method:decodeURL,errorMsg:" + e.getMessage());
            return null;
        }
    }

}
