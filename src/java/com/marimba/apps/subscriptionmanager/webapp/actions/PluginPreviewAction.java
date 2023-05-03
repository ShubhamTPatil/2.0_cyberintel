// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.beans.ChangeRequestBean;
import com.marimba.intf.msf.*;
//import com.marimba.intf.msf.extension.IChangeRequestManagement;
import com.marimba.intf.util.*;
import com.marimba.webapps.struts.validation.ValidationUtil;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.*;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.webapp.forms.SetPluginForm;

import com.marimba.tools.ldap.LDAPConstants;

import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.IWebAppsConstants;

import com.marimba.webapps.tools.util.PropsBean;

/**
 * This action will save the plugin properties to a config file in the data
 * directory. These properties are then used by the publish code to set the
 * properties in the plugin.
 *
 * @author Angela Saval
 * @version 1.25,12/15/2002
 */
public final class PluginPreviewAction extends AbstractAction {

	private static final String[] configProps = { "title1", "pluginStatus", "title2", "publishurl",
			"publishurl.username", "publishurl.password", "publishurl.subscribeuser", "publishurl.subscribepassword",
			"title4", "securityinfo.url", "securityinfo.subscribeuser", "securityinfo.subscribepassword", "title5",
			"customscanner.url", "customscanner.subscribeuser", "customscanner.subscribepassword", "title6", "db.type",
			"db.hostname", "db.port", "db.name", "db.username", "db.password", "title7", "db.thread.min",
			"db.thread.max", "title8", "repeaterInsert", "title9", "elasticurl", "title10", "cveFiltersDir", "title11",
			"cvedownloader.url", "cvedownloader.subscribeuser", "cvedownloader.subscribepassword", "title3", "vendor",
			"ldaphost", "basedn", "binddn", "bindpasswd", "usessl", "authmethod", "poolsize", "lastgoodhostexptime"

	};

	private static final String[] subConfigProps = { "title1", "subscriptionmanager.pluginStatus", "title2",
			"subscriptionmanager.publishurl", "publishurl.username", "publishurl.password", "publishurl.subscribeuser",
			"publishurl.subscribepassword", "title4", "subscriptionmanager.securityinfo.url",
			"subscriptionmanager.securityinfo.subscribeuser", "subscriptionmanager.securityinfo.subscribepassword",
			"title5", "subscriptionmanager.customscanner.url", "subscriptionmanager.customscanner.subscribeuser",
			"subscriptionmanager.customscanner.subscribepassword", "title6", "subscriptionmanager.dbType",
			"subscriptionmanager.db.hostname", "subscriptionmanager.db.port", "subscriptionmanager.db.username",
			"subscriptionmanager.db.password", "title7", "subscriptionmanager.db.thread.min",
			"subscriptionmanager.db.thread.max", "title8", "subscriptionmanager.repeaterInsert", "title9",
			"subscriptionmanager.elasticurl", "title10", "subscriptionmanager.cveFiltersDir", "title11",
			"subscriptionmanager.cvedownloader.url", "subscriptionmanager.cvedownloader.subscribeuser",
			"subscriptionmanager.cvedownloader.subscribepassword", "title3", "subscriptionmanager.vendor",
			"subscriptionmanager.ldaphost", "subscriptionmanager.basedn", "subscriptionmanager.binddn",
			"subscriptionmanager.bindpasswd", "subscriptionmanager.usessl", "subscriptionmanager.authmethod",
			"subscriptionmanager.poolsize", "subscriptionmanager.lastgoodhostexptime", "subscriptionmanager.pallowprov",

	};

	static HashMap<String, String> hidden = new HashMap<String, String>();
	static HashMap<String, String> externalized = new HashMap<String, String>();

	static {
		hidden.put("publishpass", "publishpass");
		hidden.put("subscribepass", "subscribepass");
		hidden.put("bindpasswd2", "bindpasswd2");
		hidden.put("db.password", "db.password");
	}

	static {
		externalized.put("usessl", "usessl");
		externalized.put("authmethod", "authmethod");
		externalized.put("vendor", "vendor");
		externalized.put("pallowprov", "pallowprov");
		externalized.put("repeaterInsert", "repeaterInsert");
	}

	private HashMap<String, ChangeRequestBean> changeRequestBeans = new HashMap<String, ChangeRequestBean>();

	/**
	 * REMIND
	 *
	 * @param mapping  REMIND
	 * @param form     REMIND
	 * @param request  REMIND
	 * @param response REMIND
	 *
	 * @return REMIND
	 *
	 * @throws IOException      REMIND
	 * @throws ServletException REMIND
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		init(request);
		Vector errors = new Vector();
		ServletContext sc = servlet.getServletConfig().getServletContext();
		SubscriptionMain smmain = TenantHelper.getTenantSubMain(sc, request);
		IMapProperty formbean = (IMapProperty) form;
		SetPluginForm userForm = (SetPluginForm) form;
		String publishUrl = (String) formbean.getValue("publishurl");

		debug("execute(), SetPluginSaveAction: publishUrl = " + publishUrl);

		String pluginStatus = (String) formbean.getValue("pluginStatus");
		System.out.println("Retrieved plugin status : " + pluginStatus);
		request.getSession().setAttribute("pluginStatus", pluginStatus);

		userForm.setPrevPage("previewCancel");

		// get saved values
		SetPluginForm oldForm = new SetPluginForm();
		oldForm.loadPublishedProps(smmain.getConfig());

		// display values
		Vector<PropsBean> listing = new Vector<PropsBean>();
		PropsBean entry;
		int titleIndex = 0;

		String showAll = request.getParameter("showAll");
		showAll = (showAll != null) ? showAll : "true";
		entry = new PropsBean();
		entry.setValue("showall", showAll);
		listing.add(entry);

		String vendor = (String) userForm.getValue("vendor");

		if ((vendor == null) || LDAPConstants.VENDOR_AD.equals(vendor)) {
			hidden.put("ldaphost", "ldaphost");
			hidden.put("basedn", "basedn");
		}

		// Authentication Type property display in policy plugin preview page if AD
		// directory service
		if ((vendor == null) || !LDAPConstants.VENDOR_AD.equals(vendor)) {
			hidden.put("authmethod", "authmethod");
		}

		boolean showTitle = true;
		int titleAt = 0;

		// debug("execute(), tenant.isPolicyPluginEnabledForChangeRequest() - " +
		// false);
		if (false) {
			checkDuplicateChangeRequests(publishUrl, request);
		}
		changeRequestBeans = new HashMap<String, ChangeRequestBean>();
		for (int i = 0; i < configProps.length; i++) {
			entry = new PropsBean();

			if (configProps[i].startsWith("title")) {
				// remove previous title if it does not have elements
				removeTitle(listing, showTitle, titleAt);
				showTitle = false;
				titleAt = listing.size();
				entry.setValue("title", configProps[i]);
				listing.add(entry);
			} else {
				// Be it String[] or String getValue would return only String
				String formValue = (String) userForm.getValue(configProps[i]);
				String oldValue = (String) oldForm.getValue(configProps[i]);

				formValue = (formValue == null) ? "" : formValue;
				oldValue = (oldValue == null) ? "" : oldValue;

				if (DEBUG2) {
					if (configProps[i] != null && configProps[i].indexOf("pass") == -1) {
						System.out.print(configProps[i] + " = ");
						System.out.print(formValue);
						System.out.print(" = ");
						System.out.print(oldValue);
					}
				}

				if (hidden.get(configProps[i]) != null) {
					entry.setValue("hidden", "true");
				}

				if (externalized.get(configProps[i]) != null) {
					entry.setValue("externalized", "true");
				}

				// debug("execute(), tenant.isPolicyPluginEnabledForChangeRequest() - " +
				// false);
				if (false) {
					ChangeRequestBean bean = getChangeRequestBean(subConfigProps[i], oldValue, formValue);
					debug("execute(), bean - " + bean);
					if (bean != null) {
						changeRequestBeans.put(bean.toString(), bean);
					}
				}

				if (!formValue.equals(oldValue)) {
					if ("true".equalsIgnoreCase(showAll)) {
						entry.setValue("changed", "true");
					}
				} else {
					// dont show if equal and showall is false
					if ("false".equalsIgnoreCase(showAll)) {
						entry.setValue("hidden", "true");
					}
				}

				entry.setValue("displayname", configProps[i]);

				if ("bindpasswd".equals(configProps[i]) || "publishurl.password".equals(configProps[i])
						|| "publishurl.subscribepassword".equals(configProps[i])
						|| "securityinfo.subscribepassword".equals(configProps[i])
						|| "cvedownloader.subscribepassword".equals(configProps[i])
						|| "customscanner.subscribepassword".equals(configProps[i])
						|| "db.password".equals(configProps[i])) {
					if (((formValue == null) || (formValue.length() == 0))) {
						entry.setValue("assignedValue", "");
					} else {
						entry.setValue("assignedValue", "*******");
					}
				} else if ("ldaphost".equals(configProps[i]) && isCloudEnabled()) {
					if (((formValue == null) || (formValue.length() == 0))) {
						entry.setValue("assignedValue", "");
					} else {
						entry.setValue("assignedValue", "*******");
					}
				} else {
					entry.setValue("assignedValue", formValue);
				}

				showTitle = (showTitle || (entry.getValue("hidden") == null));
				listing.add(entry);
			}
		}
		validateSettings(formbean, errors, request.getLocale());
		if (errors.size() > 0) {
			request.setAttribute("errors", errors.elements());
			return mapping.findForward("edit");
			// request.setAttribute(Globals.ERROR_KEY, dbtask.errors);
			// retPage = "view";
		}
		HttpSession session = request.getSession();

		if (changeRequestBeans.size() > 0) {
			session.setAttribute("change_request_beans", changeRequestBeans.values());
		} else {
			session.removeAttribute("change_request_beans");
		}

		removeTitle(listing, showTitle, titleAt);

		debug("execute(), Listing size - " + listing.size());

		session.setAttribute("preview_values", listing);

		return mapping.findForward("success");
	}

	// removes title if there are no changes
	public void removeTitle(Vector listing, boolean showTitle, int titleAt) {
		if (!showTitle) {
			listing.remove(titleAt);
		}
	}

	private ChangeRequestBean getChangeRequestBean(String key, String oldValue, String newValue) {
		if ((key == null) || (key.trim().length() < 1)) {
			return null;
		}
		if ((oldValue != null) && (!oldValue.equals(newValue))) {
			ChangeRequestBean bean = new ChangeRequestBean();

			if ("".equals(oldValue) && !"".equals(newValue)) {
				bean.setOperationType("");
			} else if (!"".equals(oldValue) && "".equals(newValue)) {
				bean.setOperationType("");
			} else {
				bean.setOperationType("");
			}

			bean.setKey(key);
			bean.setOldValue(oldValue);
			bean.setNewValue(newValue);

			return bean;
		}

		return null;
	}

	void checkDuplicateChangeRequests(String publishUrl, HttpServletRequest request) {
		try {
//            if (tenant.isPolicyPluginEnabledForChangeRequest()) {
//                IChangeRequestManagement changeReqMgmt = tenant.getChangeRequestManagement();
//                debug("checkDuplicateChangeRequests(), changeReqMgmt - " + changeReqMgmt);
//                if (changeReqMgmt != null) {
//                    Vector<HashMap<String, String>> changeRequests = changeReqMgmt.getChangeRequestsAsMap(tenant.getName(), IChangeRequestManagement.POLICY_PLUGIN_CHANGE_REQUEST, publishUrl);
//                    debug("checkDuplicateChangeRequests(), changeRequests - " + changeRequests);
//                    for (int i = 0; i < changeRequests.size(); i++) {
//                        HashMap<String, String> changeRequest = changeRequests.get(i);
//                        String changeRequestStatus = changeRequest.get("requestStatus");
//                        if ((changeRequestStatus != null) && (changeRequestStatus.startsWith("Closed"))) {
//                            //ignore...
//                        } else {
//                            debug("checkDuplicateChangeRequests(), getTenantTunerConfig() - " + getTenantTunerConfig());
//                            if (getTenantTunerConfig() != null) {
//                                debug("checkDuplicateChangeRequests(), getTenantTunerConfig().getProperty(\"marimba.serviceautomation.cms.configchange.integration\") - " + getTenantTunerConfig().getProperty("marimba.serviceautomation.cms.configchange.integration"));
//                                if ("servicenow".equals(getTenantTunerConfig().getProperty("marimba.serviceautomation.cms.configchange.integration"))) {
//                                    request.setAttribute("duplicateChangeRequestExists", "servicenow");
//                                }
//                                if ("remedyforce".equals(getTenantTunerConfig().getProperty("marimba.serviceautomation.cms.configchange.integration"))) {
//                                    request.setAttribute("duplicateChangeRequestExists", "remedyforce");
//                                }
//                            }
//                            break;
//                        }
//                    }
//                }
//            }
		} catch (Throwable t) {
			if (DEBUG2) {
				t.printStackTrace();
			}
		}
	}

	public String getUserName(HttpServletRequest request) {
		IUserPrincipal userPrincipal = (IUserPrincipal) request.getUserPrincipal();
		String userName = userPrincipal.getName();
		return userName;
	}

	public IConfig getTenantTunerConfig() {
//        if (null != tenant) {
//            return tenant.getTunerConfig();
//        }
		return null;
	}

	public void debug(String s) {
		if (DEBUG2) {
			System.out.println("vDesk, PluginPreviewAction.java => " + s);
		}
	}

	private void validateSettings(IMapProperty formbean, Vector errors, Locale locale) {
		if (!isValidHost(getPropertyValue(formbean, "db.hostname"))) {
			// errors.add("dbHost", new ActionMessage("errors.database.db.invalidhost"));
			errors.addElement(printMsg(locale, "errors.required", printMsg(locale, "errors.database.db.invalidhost")));
		}
		if (!isValidPort(getPropertyValue(formbean, "db.port"))) {
			errors.addElement(printMsg(locale, "errors.required", printMsg(locale, "errors.database.db.invalidport")));
		}
		if (!isValidDbName(getPropertyValue(formbean, "db.name"))) {
			errors.addElement(printMsg(locale, "errors.required", printMsg(locale, "errors.database.db.invaliddb")));
		}
		if (!isValidDbName(getPropertyValue(formbean, "db.username"))) {
			errors.addElement(
					printMsg(locale, "errors.required", printMsg(locale, "errors.database.db.invalidUserName")));
		}
		if (!isValidDbName(getPropertyValue(formbean, "db.password"))) {
			errors.addElement(
					printMsg(locale, "errors.required", printMsg(locale, "errors.database.db.invalidPassword")));
		}

		if (!isValidNumericInput(getPropertyValue(formbean, "db.thread.min"))
				|| !isValidNumericInput(getPropertyValue(formbean, "db.thread.max"))) {
			errors.addElement(printMsg(locale, "errors.numeric", printMsg(locale, "errors.database.db.con")));
		} else {

			if (!isValidPositiveInt(getPropertyValue(formbean, "db.thread.min"))) {
				errors.addElement(printMsg(locale, "errors.positive", printMsg(locale, "errors.database.db.min.con")));
			}
			if (!isValidPositiveInt(getPropertyValue(formbean, "db.thread.max"))) {
				errors.addElement(printMsg(locale, "errors.positive", printMsg(locale, "errors.database.db.max.con")));
			}
			if (!validateConnectionSettings(formbean)) {
				errors.addElement(printMsg(locale, "errors.database.conminmax"));
			}
		}
		if (!isValidBindDn(getPropertyValue(formbean, "binddn"))) {
			errors.addElement(printMsg(locale, "errors.required", printMsg(locale, "errors.directory.binddn")));
		}

		if (!isValidBaseDn(getPropertyValue(formbean, "basedn"))) {
			errors.addElement(printMsg(locale, "errors.required", printMsg(locale, "errors.directory.basedn")));
		}
		if (!isValidLdapHost(getPropertyValue(formbean, "ldaphost"))) {
			errors.addElement(printMsg(locale, "errors.required", printMsg(locale, "errors.directory.ldaphost")));
		}

	}

	boolean validateConnectionSettings(IMapProperty formbean) {
		String minStr = getPropertyValue(formbean, "db.thread.min");
		String maxStr = getPropertyValue(formbean, "db.thread.max");

		int min = Integer.parseInt(minStr);
		int max = Integer.parseInt(maxStr);

		if (min < 0) {
			return true;
		}

		if (!validateDigits(minStr) || !validateDigits(maxStr)) {
			return false;
		}

		try {
			return max >= min;
		} catch (NumberFormatException nfe) {
			// ignore
		}
		return false;
	}

	// This method return true only if the string is consisted of ISO-8859 digits
	public boolean validateDigits(String v) {
		String value = v.trim();
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if (!(c == '0' || c == '1' || c == '2' || c == '3' || c == '4' || c == '5' || c == '6' || c == '7'
					|| c == '8' || c == '9')) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	public boolean isValidNumericInput(String input) {
		if (input.matches("[a-zA-Z_]+")) {
			debug("User should not allow to enter alphabets");
			return false;
		} else {
			debug("Received valid input");
			return true;
		}
	}

	private boolean isValidDbName(String dbName) {
		return ValidationUtil.required(dbName);
	}

	private boolean isValidBindDn(String bindDn) {
		return ValidationUtil.required(bindDn);
	}

	private boolean isValidBaseDn(String baseDn) {
		return ValidationUtil.required(baseDn);
	}

	private boolean isValidLdapHost(String ldaphost) {
		return ValidationUtil.required(ldaphost);
	}

	private boolean isValidHost(String host) {
		return ValidationUtil.required(host);
	}

	private boolean isValidPort(String port) {
		return ValidationUtil.required(port) && ValidationUtil.isPort(port);
	}

	private boolean isValidPositiveInt(String value) {
		return ValidationUtil.required(value) && ValidationUtil.isPositive(value);
	}

	public String getPropertyValue(IMapProperty props, String property) {
		if ((props.getValue(property)) instanceof String[]) {
			return ((String[]) props.getValue(property))[0];
		}
		return (String) props.getValue(property);
	}

	protected String printMsg(Locale locale, String msgKey) {
		return resources.getMessage(locale, msgKey);
	}

	protected String printMsg(Locale locale, String msgKey, String arg) {
		return resources.getMessage(locale, msgKey, arg);
	}
}
