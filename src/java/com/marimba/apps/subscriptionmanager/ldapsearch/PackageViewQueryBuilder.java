// Copyright 1997-2010, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ldapsearch;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.dao.LDAPSubscription;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.tools.util.PropsBean;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * This interface is implemented by LDAPContext and LDAPContextMock. LDAPContext provides LDAPConnection object to
 * search ldap with the given query. LDAPContextMock is used for the unit testing purpose.
 *
 * @author Venkatesh Jeyaraman
 * @version 1.0, 15/07/2010
 */

public class PackageViewQueryBuilder extends LdapSearch implements IWebAppsConstants {

    public static final String AND = "&";
    public static final String OR = "|";
    public static final String NOT = "!";

    private StringBuffer createQuery(String url) {
        Map<String, String> LDAPVarsMap = context.getLDAPVarsMap();
        StringBuffer ldapQuery = new StringBuffer();
        String prefix = ((url.length() > 0) ? url : "*");

        if (null != criteria) {
            String type = (String) criteria.getValue("searchType");
            String value = (String) criteria.getValue("searchQuery");

            if ("basic".equals(type)) {
                value = (String) criteria.getValue("search");
                value = prefix + "=" + value;
                ldapQuery.append(formBasicSearchFilter());

                return ldapQuery;
            }

            // parsing advanced search parameters
            value = (String) criteria.getValue(LDAPVarsMap.get("CHANNELWOWENABLED"));
            if ("enabled".equalsIgnoreCase(value)) {
                ldapQuery.append(formRelation(LDAPVarsMap.get("CHANNELWOWENABLED"), "*"));
            } else if ("disabled".equalsIgnoreCase(value)) {
                StringBuffer wowBuffer = formRelation(LDAPVarsMap.get("CHANNELWOWENABLED"), "*");
                ldapQuery.append(addOperand(wowBuffer, NOT));
            }
            System.out.println("LDAPQuery after wow attribute: " + ldapQuery);

            value = (String) criteria.getValue(LDAPVarsMap.get("CHANNELEXEMPTBLACKOUT"));
            if ("enabled".equalsIgnoreCase(value)) {
                ldapQuery.append(formRelation(LDAPVarsMap.get("CHANNELEXEMPTBLACKOUT"), "*"));
            } else if ("disabled".equalsIgnoreCase(value)) {
                StringBuffer sb1 = formRelation(LDAPVarsMap.get("CHANNELEXEMPTBLACKOUT"), url + "=true");
                ldapQuery.append(addOperand(sb1, NOT));
            }
            System.out.println("LDAPQuery after exempt from blackout attribute: " + ldapQuery);

            value = (String) criteria.getValue(LDAPVarsMap.get("CHANNELTITLE"));
            if (null != value && value.trim().length() > 0) {
                if (value.startsWith("strt")) {
                    value = prefix + "=" + value.substring(4) + "*";
                } else if (value.startsWith("ends")) {
                    value = prefix + "=*" + value.substring(4);
                } else if (value.startsWith("cntn")) {
                    value = prefix + "=*" + value.substring(4) + "*";
                } else {
                    value = prefix + "=" + value.substring(4);
                }
                ldapQuery.append(formRelation(LDAPVarsMap.get("CHANNELTITLE"), value));
            }
            System.out.println("LDAPQuery after title attribute: " + ldapQuery);

            value = (String) criteria.getValue(LDAPVarsMap.get("CHANNEL"));
            if (null != value && value.trim().length() > 0) {
                value = prefix + "=" + value + ",*";
                ldapQuery.append(formRelation(LDAPVarsMap.get("CHANNEL"), value));
            }
            System.out.println("LDAPQuery after primary state attribute: " + ldapQuery);

            value = (String) criteria.getValue(LDAPVarsMap.get("CHANNELSEC"));
            if (null != value && value.trim().length() > 0) {
                value = prefix + "=" + value;
                ldapQuery.append(formRelation(LDAPVarsMap.get("CHANNELSEC"), value));
            }
            System.out.println("LDAPQuery after secondary state: " + ldapQuery);

            value = (String) criteria.getValue(LDAPVarsMap.get("CHANNELINITSCHED"));
            if (null != value && value.trim().length() > 0) {
                // before -after ========> should be caught at javascript validation itself
                // before -after 12/3/2008
                // before 12/3/2008-after
                // before 12/3/2008-after 12/3/2008

                ldapQuery.append(formRelation(LDAPVarsMap.get("CHANNELINITSCHED"), "*"));
            }
            System.out.println("LDAPQuery after primary schedule: " + ldapQuery);

            value = (String) criteria.getValue(LDAPVarsMap.get("CHANNELSECSCHED"));
            if (null != value && value.trim().length() > 0) {
                ldapQuery.append(formRelation(LDAPVarsMap.get("CHANNELSECSCHED"), "*"));
            }
            System.out.println("LDAPQuery after secondary schedule: " + ldapQuery);

            value = (String) criteria.getValue(LDAPVarsMap.get("CHANNELUPDATESCHED"));
            if (null != value && value.trim().length() > 0) {
                ldapQuery.append(formRelation(LDAPVarsMap.get("CHANNELUPDATESCHED"), "*"));
            }
            System.out.println("LDAPQuery after update schedule: " + ldapQuery);

            value = (String) criteria.getValue(LDAPVarsMap.get("CHANNELVERREPAIRSCHED"));
            if (null != value && value.trim().length() > 0) {
                ldapQuery.append(formRelation(LDAPVarsMap.get("CHANNELVERREPAIRSCHED"), "*"));
            }
            System.out.println("LDAPQuery after verify repair schedule: " + ldapQuery);
        }
        return ldapQuery;
    }

    public String getQuery(boolean addQuery) {
        Map<String, String> LDAPVarsMap = context.getLDAPVarsMap();
        String query1 = "(|(" + LDAPVarsMap.get("TARGETDN") + "=*)(" + LDAPVarsMap.get("TARGET_ALL") + "=true))";

        StringBuffer sb = new StringBuffer();
        if (null != channelsList) {
            for (int i = 0; i < channelsList.size(); i++) {
                Channel chn = (Channel) channelsList.get(i);
                String url = LDAPName.escapeComponentValue(chn.getUrl());
                sb.append(createQuery(url));
            }
            if (channelsList.size() > 0) {
                sb.append(query1);
            }
        } else {
            sb = createQuery("");
        }

        sb = addOperand(sb, AND);
        System.out.println("Advanced LDAP Search Query: " + sb);

        return sb.toString();
    }

    private StringBuffer addOperand(StringBuffer query, String op) {
        return new StringBuffer().append("(" + op).append(query).append(")");
    }

    private StringBuffer formRelation(String attr, String value) {
        StringBuffer sb = new StringBuffer();
        sb.append("(");
        sb.append(attr);
        sb.append("=");
        sb.append(value);
        sb.append(")");

        return sb;
    }
    private String getSearch() {
        String search = (String) criteria.getValue("search");

        if ((null == search) || ("".equals(search.trim()))) {
            return "*";
        }
        return search.trim();
    }

    private StringBuffer formBasicSearchFilter() {
        StringBuffer sb = new StringBuffer();
        Map<String, String> LDAPVarsMap = context.getLDAPVarsMap();
        if ("*".equals(getSearch())) {
            // search filter added to display only the packages that are
            // assigned
            // to targets browsed from ldap. Previously it displays the packages
            // includes targets that are assigned to the targets sourced from
            // tx.
            sb.append("(&");
            sb.append("(" + LDAPVarsMap.get("CHANNEL") + "=*)");
            if (isUsersInLDAP()) {
                // (&(mrbachannel=*)(|(mrbatargetdn=*)(mrbatargetall=true)))
                // (&(channel)(|(retrieve ldap objects with targetDN
                // property)(retrieve all_all target with property targetall)))
                sb.append("(|(" + LDAPVarsMap.get("TARGETDN") + "=*)("
                        + LDAPVarsMap.get("TARGET_ALL") + "=true)))");
            } else {
                // (&(mrbachannel=*)(|(mrbatargettxuser=*)(!(mrbatargettype=user))))
                // (&(channel)(|(retrieve ldap objects with targetDN
                // property)(retrieve all_all target with property targetall)))
                sb.append("(|(" + LDAPVarsMap.get("TARGET_TX_USER") + "=*)(!("
                        + LDAPVarsMap.get("TARGETTYPE") + "=user))))");
            }
        } else {
            // Appending * to the front of <user_search> in
            // (LDAPVars.CHANNELTITLE=*<user_search>) so that exact match of
            // <user_search>
            // will work. This is because the mrbaChannelTitle is actually
            // <url>=<title>,
            // not just title.
            // Appending * around <user_search> in
            // (LDAPVars.CHANNELTITLE=*<user_search>*)
            // because we are trying to match against the channelname from the
            // url. Since
            // mrbaChannel is actually <url>=<state>, we need the * around
            // <user_search>.
            if (isUsersInLDAP()) {
                // search filter added to display only the packages that are
                // assigned to targets
                // browsed from ldap. Previously it displays the packages
                // includes targets that
                // are assigned to the targets sourced from tx.
                // (&(|(mrbachanneltitle=*pkg)(mrbachannel=*pkg*))(|(mrbatargetdn=*)(mrbatargetall=true)))
                sb.append("(&");
                sb.append("(|(" + LDAPVarsMap.get("CHANNELTITLE") + "=*" + getSearch()
                        + ")(" + LDAPVarsMap.get("CHANNEL") + "=*" + getSearch() + "*))");
                sb.append("(|(" + LDAPVarsMap.get("TARGETDN") + "=*)("
                        + LDAPVarsMap.get("TARGET_ALL") + "=true)))");
            } else {
                // (&(|(mrbachanneltitle=*pkg)(mrbachannel=*pkg*))(|(mrbatargettxuser=*)(!(mrbatargettype=true))))
                sb.append("(&");
                sb.append("(|(" + LDAPVarsMap.get("CHANNELTITLE") + "=*" + getSearch()
                        + ")(" + LDAPVarsMap.get("CHANNEL") + "=*" + getSearch() + "*))");
                sb.append("(|(" + LDAPVarsMap.get("TARGET_TX_USER") + "=*)(!("
                        + LDAPVarsMap.get("TARGETTYPE") + "=user))))");
            }
        }
        return sb;
    }
    public Vector<PropsBean> filterSchedule(Enumeration result) throws NamingException {
        HashMap<String, HashMap<String, PropsBean>> tgtMap = null;

        tgtMap = loadChannelAttr(result);
        tgtMap = validate(tgtMap);
        return reArrangeMap(tgtMap);
    }

    private Vector<PropsBean> reArrangeMap(HashMap<String, HashMap<String, PropsBean>> tgtMap) {
        HashMap<String, PropsBean> finalMap = new HashMap<String, PropsBean>();

        Iterator dnItr = tgtMap.keySet().iterator();

        while (dnItr.hasNext()) {
            String dn = (String) dnItr.next();
            HashMap chAttrsMap = tgtMap.get(dn);

            Iterator urlItr = chAttrsMap.keySet().iterator();

            while (urlItr.hasNext()) {
                String url = (String) urlItr.next();
                PropsBean props = (PropsBean) chAttrsMap.get(url);

                finalMap.put(url, props);
            }

        }

        return new Vector<PropsBean>(finalMap.values());
    }

    private void addEntryToMap(String fullName, HashMap<String, HashMap<String, PropsBean>> tgtMap, String propsKey,
                               String propsValue, String hashKey) {
        HashMap<String, PropsBean> chAttrsMap = tgtMap.get(fullName);
        if (null == chAttrsMap) {
            chAttrsMap = new HashMap<String, PropsBean>(10);
        }
        PropsBean entry = chAttrsMap.get(hashKey);

        if (null == entry) {
            entry = new PropsBean();
        }

        entry.setProperty(propsKey, propsValue);
        chAttrsMap.put(hashKey, entry);
        tgtMap.put(fullName, chAttrsMap);
    }

    private String[] getStringArray(String attrValue, char delim) {
        String[] strArray = new String[2];
        int ind = attrValue.indexOf(delim);
        if (ind != -1) {
            strArray[0] = attrValue.substring(0, ind);
            strArray[1] = attrValue.substring(ind + 1);
        } else {
            strArray[0] = attrValue;
        }

        return strArray;
    }

    private String createURL(String url) {
        try {
            // convert the host name to lowercase.
            URL uri = new URL(url);
            if (uri.getHost() != null) {
                int index = url.indexOf(uri.getHost());
                if (index != -1) {
                    StringBuffer buffer = new StringBuffer(url);
                    buffer.replace(index, index + uri.getHost().length(), uri.getHost().toLowerCase());
                    url = buffer.toString();
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("Failed to create a URL object, so adding the original url as it is to the hashmap");
            e.printStackTrace();
        }

        return url;
    }

    private HashMap<String, HashMap<String, PropsBean>> loadChannelAttr(Enumeration results) throws NamingException {
        HashMap<String, HashMap<String, PropsBean>> tgtMap = new HashMap<String, HashMap<String, PropsBean>>(10);
        NamingEnumeration nenum = (NamingEnumeration) results;
        Map<String, String> LDAPVarsMap = context.getLDAPVarsMap();
        try {
            // logic to create hashtable by parsing the attributes array
            while (nenum.hasMoreElements()) {
                SearchResult sr = (SearchResult) nenum.nextElement();
                String fullName = sr.getNameInNamespace();
                Attributes srAttrs = sr.getAttributes();
                System.out.println("load channel attribute for target : " + fullName);
                for (int i = 0; i < attrs.length; i++) {
                    Attribute attr = srAttrs.get(attrs[i]);
                    if (null != attr) {
                        NamingEnumeration attrs = attr.getAll();
                        System.out.println("Attribute get ID : "+ attr.getID());
                        while (attrs.hasMoreElements()) {
                            Object value = attrs.nextElement();

                            if (value instanceof String) {
                                String strValue = (String) value;
                                System.out.println("Input value :" + strValue);
                                String[] urlValue = getStringArray(strValue, '=');
                                urlValue[0] = createURL(urlValue[0]);

                                // if attrs[i] is channeltitle find title and update propsbean
                                if (LDAPVarsMap.get("CHANNELTITLE").equals(attr.getID())) {
                                    addEntryToMap(fullName, tgtMap, "url", urlValue[0], urlValue[0]);
                                    addEntryToMap(fullName, tgtMap, "title", urlValue[1], urlValue[0]);
                                } else if (LDAPVarsMap.get("CHANNEL").equals(attr.getID())) {
                                    // if attrs[i] is channel then find type and update propsbean
                                    String[] stateType = getStringArray(urlValue[1], ',');

                                    //ToDo: need to check it later
                                    if (LDAPSubscription.isNone(stateType[0])) {
                                        continue;
                                    }

                                    if (ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP.equals(stateType[1])) {
                                        stateType[1] = ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP;
                                    } else {
                                        stateType[1] = ISubscriptionConstants.CONTENT_TYPE_APPLICATION;
                                    }

                                    addEntryToMap(fullName, tgtMap, "url", urlValue[0], urlValue[0]);
                                    addEntryToMap(fullName, tgtMap, "type", stateType[1], urlValue[0]);
                                }
                                // split url and value and store it in a propsbean
                                addEntryToMap(fullName, tgtMap, attr.getID(), urlValue[1], urlValue[0]);
                            }
                        }
                    }
                }
                // need to check all values available or not
                if(null != tgtMap) {
                    HashMap<String, PropsBean> chAttrsMap = tgtMap.get(fullName);

                    Set urlKeySet = chAttrsMap.keySet();
                    Iterator urlItr = urlKeySet.iterator();

                    HashSet<String> urlsToBeRemoved = new HashSet<String>(10);
                    while (urlItr.hasNext()) {
                        String url = (String) urlItr.next();
                        boolean notExistsUrl = false;
                        boolean notExistsType = false;
                        boolean notExistsTitle = false;

                        PropsBean props = chAttrsMap.get(url);
                        if(null == props.getProperty("url")) {
                            notExistsUrl = true;
                        }
                        if(null == props.getProperty("type")) {
                            notExistsType = true;
                        }
                        if(null == props.getProperty("title")) {
                            notExistsTitle = true;
                        }
                        if(notExistsUrl && notExistsType && notExistsTitle) {
                            urlsToBeRemoved.add(url);
                        }
                    }
                    if (urlsToBeRemoved.size() > 0) {
                        Iterator removeItr = urlsToBeRemoved.iterator();

                        while (removeItr.hasNext()) {
                            String removalURL = (String) removeItr.next();
                            System.out.println("load channel attribute - Removal URL since url, title and type attributes are missing:" + removalURL);
                            chAttrsMap.remove(removalURL);
                        }
                    }
                    urlsToBeRemoved.clear();
                }
            }
        } catch (Exception ex) {
            System.out.println("PackageViewQueryBuilder: Exception occured while loading channel attributes");

            if (DEBUG) {
                ex.printStackTrace();
            }
        }

        return tgtMap;
    }

    private HashMap<String, HashMap<String, PropsBean>> validate(HashMap<String, HashMap<String, PropsBean>> tgtMap) {
        String type = (String) criteria.getValue("searchType");
        HashSet<String> urlsToBeRemoved = new HashSet<String>(10);
        Map<String, String> LDAPVarsMap = context.getLDAPVarsMap();
        Set dnKeySet = tgtMap.keySet();
        Iterator dnItr = dnKeySet.iterator();

        try {
            while (dnItr.hasNext()) {
                String dn = (String) dnItr.next();
                HashMap<String, PropsBean> chAttrsMap = tgtMap.get(dn);

                Set urlKeySet = chAttrsMap.keySet();
                Iterator urlItr = urlKeySet.iterator();

                while (urlItr.hasNext()) {
                    String url = (String) urlItr.next();
                    PropsBean props = chAttrsMap.get(url);
                    if (DEBUG) {
                        System.out.println("processing url : " + url);
                    }
                    String value = props.getProperty(LDAPVarsMap.get("CHANNELTITLE"));
                    if ("basic".equals(type)) {
                        // Package search result should be filter by either URL or channel Name based
                        String showURL = ((String) criteria.getValue("show_url"));
                        if(null != showURL && "true".equalsIgnoreCase(showURL)) {
                            value = url;
                        }

                        // if channel title is missing or having no value for this attribute
                        // then fetch the title from package url
                        if (value == null) {
                            value = getChannelNameFromUrl(url);
                        }

                        if (null == props.getProperty(LDAPVarsMap.get("CHANNELTITLE"))) {
                            props.setProperty(LDAPVarsMap.get("CHANNELTITLE"), value);
                            props.setProperty("title", value);
                        }

                        if (!checkTitleMatch(value, (String) criteria.getValue("search"))) {
                            urlsToBeRemoved.add(url);
                        }
                        continue;
                    }
                    if (value == null) {
                        value = getChannelNameFromUrl(url);
                    }

                    if (null == props.getProperty(LDAPVarsMap.get("CHANNELTITLE"))) {
                        props.setProperty(LDAPVarsMap.get("CHANNELTITLE"), value);
                        props.setProperty("title", value);
                    }
                    if (!checkTitleMatch(value, (String) criteria.getValue(LDAPVarsMap.get("CHANNELTITLE")))) {
                        urlsToBeRemoved.add(url);
                    }

                    value = props.getProperty(LDAPVarsMap.get("CHANNEL"));
                    if (!checkState(value, (String) criteria.getValue(LDAPVarsMap.get("CHANNEL"))) ||
                            !checkType(props.getProperty("type"), (String) criteria.getValue("contentType"))) {
                        urlsToBeRemoved.add(url);
                    }

                    value = props.getProperty(LDAPVarsMap.get("CHANNELSEC"));
                    if (!checkState(value, (String) criteria.getValue(LDAPVarsMap.get("CHANNELSEC")))) {
                        urlsToBeRemoved.add(url);
                    }

                    value = props.getProperty(LDAPVarsMap.get("CHANNELWOWENABLED"));
                    if (!checkBoolean(value, (String) criteria.getValue(LDAPVarsMap.get("CHANNELWOWENABLED")))) {
                        urlsToBeRemoved.add(url);
                    }

                    value = props.getProperty(LDAPVarsMap.get("CHANNELEXEMPTBLACKOUT"));
                    if (!checkBoolean(value, (String) criteria.getValue(LDAPVarsMap.get("CHANNELEXEMPTBLACKOUT")))) {
                        urlsToBeRemoved.add(url);
                    }

                    value = props.getProperty(LDAPVarsMap.get("CHANNELINITSCHED"));
                    if (!checkSchedule(value, (String) criteria.getValue(LDAPVarsMap.get("CHANNELINITSCHED")))) {
                        urlsToBeRemoved.add(url);
                    }

                    value = props.getProperty(LDAPVarsMap.get("CHANNELSECSCHED"));
                    if (!checkSchedule(value, (String) criteria.getValue(LDAPVarsMap.get("CHANNELSECSCHED")))) {
                        urlsToBeRemoved.add(url);
                    }

                    value = props.getProperty(LDAPVarsMap.get("CHANNELUPDATESCHED"));
                    if (!checkSchedule(value, (String) criteria.getValue(LDAPVarsMap.get("CHANNELUPDATESCHED")))) {
                        urlsToBeRemoved.add(url);
                    }

                    value = props.getProperty(LDAPVarsMap.get("CHANNELVERREPAIRSCHED"));
                    if (!checkSchedule(value, (String) criteria.getValue(LDAPVarsMap.get("CHANNELVERREPAIRSCHED")))) {
                        urlsToBeRemoved.add(url);
                    }
                }

                if (urlsToBeRemoved.size() > 0) {
                    Iterator removeItr = urlsToBeRemoved.iterator();

                    while (removeItr.hasNext()) {
                        chAttrsMap.remove((String) removeItr.next());
                    }
                }

                urlsToBeRemoved.clear();
            }
        } catch (Exception ex) {
            System.out.println("PackageViewQueryBuilder: Exception occured while validating channel attributes");
            if (DEBUG) {
                ex.printStackTrace();
            }
        }

        return tgtMap;
    }
    private String getChannelNameFromUrl(String url) {
        int idx = url.lastIndexOf("/");
        if (idx > 0) {
            return url.substring(idx+1);
        }

        return null;
    }
    private boolean checkSchedule(String ldapValue, String inputValue) {
        if (null == inputValue) {
            return true;
        }

        if (null == ldapValue) {
            return false;
        }

        String inputActExp[] = getStringArray(inputValue, '-');
        String ldapActExp[] = getStringArray(ldapValue, '-');

        // compare activation schedule
        boolean act = compare(ldapActExp[0], inputActExp[0]);

        // compare expiration schedule
        boolean exp = compare(ldapActExp[1], inputActExp[1]);
        return (act && exp);
    }

    private boolean compare(String ldapValue, String inputValue) {
        String[] comp_ipdate = null;

        if (null == inputValue) {
            return true;
        } else {
            comp_ipdate = getStringArray(inputValue, ' ');
            if (null == comp_ipdate[1] || comp_ipdate[1].trim().length() == 0) {
                return true;
            }
        }

        if (null != ldapValue) {
            int ind = ldapValue.indexOf(ACTIVE);
            if (ind != -1) {
                ldapValue = (ldapValue.substring(ind + ACTIVE.length())).trim();
            }

            String[] date_time = getStringArray(ldapValue, '@');
            if (date_time.length > 0) {
                ldapValue = date_time[0];
            } else {
                return false;
            }
        } else {
            return false;
        }

        try {
            SimpleDateFormat sdf = (SimpleDateFormat)
                    SimpleDateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
            Date inputDate = sdf.parse(comp_ipdate[1]);
            Date ldapDate = sdf.parse(ldapValue);

            if (AFTER.equals(comp_ipdate[0])) {
                return ldapDate.after(inputDate);
            } else {
                return ldapDate.before(inputDate);
            }
        } catch (ParseException parseEx) {
            System.out.println("Problem while parsing date information");
            if  (DEBUG) {
                parseEx.printStackTrace();
            }
            return false;
        }
    }

    private boolean checkBoolean(String ldapValue, String inputValue) {
        if (null == inputValue) {
            return true;
        }

        if (null != ldapValue &&
                (ldapValue.contains("true") || ldapValue.contains("update") || ldapValue.contains("init"))) {
            ldapValue = "enabled";
        } else {
            ldapValue = "disabled";
        }

        return inputValue.equals(ldapValue);
    }

    private boolean checkType(String ldapValue, String inputValue) {
        if (null == inputValue || inputValue.trim().length() == 0) {
            return true;
        }

        if (null == ldapValue || ldapValue.trim().length() == 0) {
            return false;
        }

        if (inputValue.indexOf(ldapValue) != -1) {
            return true;
        }

        return false;
    }

    private boolean checkState(String ldapValue, String inputValue) {
        if (null == inputValue) {
            return true;
        }

        if (null == ldapValue) {
            return false;
        }

        String[] stateType = getStringArray(ldapValue, ',');

        if (inputValue.equals(stateType[0])) {
            return true;
        }

        return false;
    }

    private boolean checkTitleMatch(String strValue, String chnName) {
        if (null == chnName) {
            return true;
        } else if (null == strValue) {
            return true;
        } else {
            // converting both strings to lower case for the case-insensitive check
            strValue = strValue.toLowerCase();
            chnName = chnName.toLowerCase();

            if (chnName.startsWith("strt")) {
                if (strValue.startsWith(chnName.substring(4))) {
                    return true;
                } else {
                    return false;
                }
            } else if (chnName.startsWith("ends")) {
                if (strValue.endsWith(chnName.substring(4))) {
                    return true;
                } else {
                    return false;
                }
            } else if (chnName.startsWith("cntn")) {
                if (strValue.indexOf(chnName.substring(4)) != -1) {
                    return true;
                } else {
                    return false;
                }
            } else if (chnName.startsWith("equl")) {
                if (strValue.equals(chnName.substring(4))) {
                    return true;
                } else {
                    return false;
                }
            } else {
                chnName = removeWildCardChar(chnName);
                if ("".equals(chnName)) {
                    return true;
                }

                if (strValue.indexOf(chnName) != -1) {
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    private String removeWildCardChar(String chnName) {
        if (chnName.startsWith("*")) {
            chnName = chnName.substring(1);
        }

        if (chnName.endsWith("*")) {
            chnName = chnName.substring(0, chnName.length()-1);
        }

        return chnName;
    }
}