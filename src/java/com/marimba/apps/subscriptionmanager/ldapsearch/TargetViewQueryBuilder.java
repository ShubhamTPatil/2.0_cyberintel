// Copyright 1997-2010, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.ldapsearch;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;

import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.webapp.intf.GUIConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;

import com.marimba.tools.ldap.LDAPName;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.webapps.tools.util.PropsBean;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;


/**
 * This interface is implemented by LDAPContext and LDAPContextMock. LDAPContext
 * provides LDAPConnection object to search ldap with the given query.
 * LDAPContextMock is used for the unit testing purpose.
 * 
 * @author Venkatesh Jeyaraman
 * @version 1.0, 15/07/2010
 */

public class TargetViewQueryBuilder extends LdapSearch {
	final static boolean DEBUG = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;
	public static final String EQUAL = "=";
	public static final String LESSTHAN_EQUAL = "<=";
	public static final String GREATERTHAN_EQUAL = ">=";

	public String getQuery(boolean addQuery) {
		StringBuffer ldapQuery=new StringBuffer();
		String createDateCriteria = (String)criteria.getValue(ISubscriptionConstants.CREATE_DATE_CRITERIA);
        String modifyDateCriteria = (String)criteria.getValue(ISubscriptionConstants.MODIFY_DATE_CRITERIA);
        String modifyDateFrom = (String)criteria.getValue(ISubscriptionConstants.MODIFY_DATE_FROM);
        String createDateFrom = (String)criteria.getValue(ISubscriptionConstants.CREATE_DATE_FROM);                       
		String targetName = (String)criteria.getValue(ISubscriptionConstants.TARGET_NAME);	
		
		if(targetName == null){
			targetName = "";
		}		
		Map<String, String> LDAPVarsMap = context.getLDAPVarsMap();
		ldapQuery.append(formRelation(LDAPVarsMap.get("SUBSCRIPTION_NAME"), EQUAL, LDAPVarsMap.get("CONTAINER_PREFIX2") + EQUAL + targetName + "*"));		
		ldapQuery.append(formRelation(LDAPVarsMap.get("SUBSCRIPTION_NAME"), EQUAL, LDAPConstants.DOMAIN_PREFIX + EQUAL + targetName + "*"));
		ldapQuery.append(formRelation(LDAPVarsMap.get("SUBSCRIPTION_NAME"), EQUAL, LDAPVarsMap.get("SUBSCRIPTION_NAME") + EQUAL + targetName + "*"));
		ldapQuery.insert(0, "(|");
		ldapQuery.append(")");
		if(null != createDateFrom && !createDateFrom.equals("")){
        		if(createDateCriteria.equals(BEFORE)){
        			ldapQuery.append(formRelation(LDAPVarsMap.get("CREATE_DATE"), LESSTHAN_EQUAL, getLDAPDate(createDateFrom, false)));
            }else if(createDateCriteria.equals(AFTER)){
            	ldapQuery.append(formRelation(LDAPVarsMap.get("CREATE_DATE"), GREATERTHAN_EQUAL, getLDAPDate(createDateFrom, true)));
            }else{
            	String createDateTo = (String)criteria.getValue(ISubscriptionConstants.CREATE_DATE_TO);
            	ldapQuery.append(formRelation(LDAPVarsMap.get("CREATE_DATE"), GREATERTHAN_EQUAL, getLDAPDate(createDateFrom, false)));
            	ldapQuery.append(formRelation(LDAPVarsMap.get("CREATE_DATE"), LESSTHAN_EQUAL, getLDAPDate(createDateTo, true)));
            }
        }     
        if(null != modifyDateFrom && !modifyDateFrom.equals("")){
    		if(modifyDateCriteria.equals(BEFORE)){
    			ldapQuery.append(formRelation(LDAPVarsMap.get("MODIFY_DATE"), LESSTHAN_EQUAL, getLDAPDate(modifyDateFrom, false)));
        }else if(modifyDateCriteria.equals(AFTER)){
        	ldapQuery.append(formRelation(LDAPVarsMap.get("MODIFY_DATE"), GREATERTHAN_EQUAL, getLDAPDate(modifyDateFrom, true)));
        }else{
        	String modifyDateTo = (String)criteria.getValue(ISubscriptionConstants.MODIFY_DATE_TO);
        	ldapQuery.append(formRelation(LDAPVarsMap.get("MODIFY_DATE"), GREATERTHAN_EQUAL, getLDAPDate(modifyDateFrom, false)));
        	ldapQuery.append(formRelation(LDAPVarsMap.get("MODIFY_DATE"), LESSTHAN_EQUAL, getLDAPDate(modifyDateTo, true)));
        }
    }        
		return "(&" + ldapQuery.toString() + ")";  
	}

	protected Vector<PropsBean> filterSchedule(Enumeration result) throws NamingException {
        HashMap<String, PropsBean> chAttrMap = new HashMap<String, PropsBean>(10);
        Vector<PropsBean> vec = new Vector<PropsBean>();
        chAttrMap = loadChannelAttr(result);
        chAttrMap = validate(chAttrMap);
        Map<String, PropsBean> sortedMap = new TreeMap<String, PropsBean>(chAttrMap);
        for (String str : sortedMap.keySet()) {
            vec.add(sortedMap.get(str));
        }
        return (vec);
	}

	private HashMap<String, PropsBean> loadChannelAttr(Enumeration results) throws NamingException {
        HashMap<String, PropsBean> chAttrsMap = new HashMap<String, PropsBean>(10);
        String dn = "";        
        NamingEnumeration nenum = (NamingEnumeration) results;      
        boolean orphanPolicy = Boolean.parseBoolean((String)criteria.getValue(ISubscriptionConstants.ORPHAN_POLICY));
        Map<String, String> LDAPVarsMap = context.getLDAPVarsMap();
        // logic to create hashtable by parsing the attributes array
        while (nenum.hasMoreElements()) {
            SearchResult sr = (SearchResult) nenum.nextElement();
            Attributes srAttrs = sr.getAttributes();                        
            String resultName = sr.getName();
            PropsBean entry = new PropsBean();
            String objectclass = null;
            if (!((null == resultName) || "".equals(resultName))) {            	
            	for (int i = 0; i < attrs.length; i++) {
                    Attribute attr = srAttrs.get(attrs[i]);                    
                    if (null != attr) {
                    	 NamingEnumeration attrs = attr.getAll();
                    	while (attrs.hasMoreElements()) {
                            Object value = attrs.nextElement();
                            if (value instanceof String) {
                            	String strValue = (String) value;                            	
                                if (LDAPConstants.OBJECT_CLASS.equalsIgnoreCase(attr.getID()) && strValue.equalsIgnoreCase(LDAPVarsMap.get("SUBSCRIPTION_CLASS"))){
                                	objectclass = strValue;
                                	if (null != objectclass ) {                                	                                                                    	
                                    	dn = LDAPName.unescapeJNDISearchResultName(resultName) + "," + context.getBase();
                                    	entry.setValue(GUIConstants.HASPOLICIES, "true");                                    	
                                        entry.setValue(LDAPConstants.OBJECT_CLASS, objectclass);  
                                        entry.setValue(GUIConstants.TYPE, objectclass);  
                                        LDAPWebappUtils.setTargetAbleProperty(entry, false, LDAPVarsMap.get("USER_CLASS"));
                                }
                                }else if(attr.getID().equalsIgnoreCase(LDAPVarsMap.get("TARGETTYPE"))){
                                	 entry.setValue("type", strValue );
                                }else if(LDAPVarsMap.get("CREATE_DATE").equalsIgnoreCase(attr.getID())){
                                	 entry.setValue(LDAPVarsMap.get("CREATE_DATE"),strValue );
                                }else if(LDAPVarsMap.get("MODIFY_DATE").equalsIgnoreCase(attr.getID())){
                                	 entry.setValue(LDAPVarsMap.get("MODIFY_DATE"), strValue);
                                } else if(LDAPVarsMap.get("TARGETDN").equalsIgnoreCase(attr.getID())){                                	                                	 
                                	entry.setValue(LDAPVarsMap.get("DN"), strValue);                                 	                                 	
                                }else if(LDAPVarsMap.get("AD_UID").equalsIgnoreCase(attr.getID())){ 
                                	 entry.setValue(LDAPVarsMap.get("AD_UID"), strValue);
                                }
                            }
                    	}            
                    }            	
            	}
            	if(null != entry.getValue("type") && null != entry.getValue(LDAPVarsMap.get("DN"))){
            		if(orphanPolicy){
            			if(!validatePolicy((String)entry.getValue(LDAPVarsMap.get("DN")))){
            				chAttrsMap.put(dn, entry);
            			}
            		} else{
            			chAttrsMap.put(dn, entry);
            		}
            	}	            	            	    	
            } 
        }
        return chAttrsMap;
	}
    
private HashMap<String, PropsBean> validate(HashMap<String, PropsBean> chAttrsMap) {					
	   return chAttrsMap;  
 }	 

private String getLDAPDate(String givenDate, boolean isToDate){
	SimpleDateFormat sourceDate=new SimpleDateFormat("MM/dd/yyyy");	
	try {
		if(isToDate){
			Date date=sourceDate.parse(givenDate);
			date.setHours(23);
			date.setMinutes(59);
			date.setSeconds(59);
			return getLDAPTime(date);
		}else{
			return getLDAPTime(sourceDate.parse(givenDate));	
		}		
	} catch (ParseException e) {
		e.printStackTrace();
	}	
	return null;
}

public String getLDAPTime(Date datetime) {
    String     ldapDate = null;
    DateFormat formatter = null;
    Map<String, String> LDAPVarsMap = context.getLDAPVarsMap();
    if (datetime != null) {
        if (LDAPVars.ACTIVE_DIRECTORY.equals(LDAPVarsMap.get("DIRECTORY_TYPE")) || LDAPVars.ADAM.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {
            formatter = new SimpleDateFormat("yyyyMMddHHmmss'.0Z'");
        	} else {
            formatter = new SimpleDateFormat("yyyyMMddHHmmss'Z'");
        	}
        ldapDate = formatter.format(datetime);
    	}
    return ldapDate;
	}

private StringBuffer formRelation(String attr, String operator, String value) {
    StringBuffer sb = new StringBuffer();
    sb.append("(");
    sb.append(attr);
    sb.append(operator);
    sb.append(value);
    sb.append(")");
    return sb;
	}

	private boolean validatePolicy(String strValue) {
		if((strValue == null) || strValue.trim().equals("")){
			return false;
		}									
		Map<String, String> LDAPVarsMap = context.getLDAPVarsMap();
		if (LDAPVars.ACTIVE_DIRECTORY.equals(LDAPVarsMap.get("DIRECTORY_TYPE")) || LDAPVars.ADAM.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {
			if (strValue.indexOf(LDAPConstants.ORPHAN_DN) != -1) {
				return false;
			}
		} else {
			try {
				context.getConn().getAttributes(strValue, super.attrs);
			} catch (NameNotFoundException e) {
				return false;
			} catch (Exception e){
				return true;
			}			
		}		
		return true;
	}
}