// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.intf.objects.ISubscription;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.TunerProfileForm;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.intf.msf.IWebApplicationInfo;
import com.marimba.io.FastInputStream;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.taglib.txlisting.TransmitterListing;
import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.webapps.tools.util.UTFUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;


/**
 * This action corresponds to the editing of transmitter login settings. This will obtain the setting current for the specified target. It will then load these
 * settings to be editing in the "Transmitter Login" page.
 */
public final class TunerProfileAction
        extends AbstractAction {
	final static String SERVICE_NAME = "InfrastructureService";
	final static String PROFILE_PREFIX = ".profile_";
    final static String PROFILE_PROPERTY = "marimba.tuner.update.profile";
    final static String PROFILE_TX = "marimba.tuner.update.profile.transmitterUrl";
    
	/**
	 * REMIND
	 *
	 * @param mapping  REMIND
	 * @param form     REMIND
	 * @param request  REMIND
	 * @param response REMIND
	 * @return REMIND
	 * @throws IOException REMIND
	 */
	public ActionForward execute(ActionMapping mapping,
	                             ActionForm form,
	                             HttpServletRequest request,
	                             HttpServletResponse response)
	        throws IOException {
		TunerProfileForm tProfileForm = (TunerProfileForm) form;
		String action = request.getParameter("action");
		try {
			if (DEBUG5) {
				tProfileForm.dump();
			}
			if ("load".equals(action)) {
				ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
				String profileName = getTChPropValue(sub.getProperty("", PROFILE_PROPERTY));
                String transmitterUrl = getTChPropValue(sub.getProperty("", PROFILE_TX));
				if (profileName == null) {
					profileName = "";
				}
				// strip = this is neded to work around defects
				// previous versions saved a badly formatted string
				profileName = stripProfilePrefix(profileName);
                                tProfileForm.setValue("tunerUpdateProfile", profileName);
                                if ( profileName != null &&  ! "".equals(profileName) && transmitterUrl != null && ! transmitterUrl.equals("")) {
                                    tProfileForm.setValue("txName", transmitterUrl);
                                } else {
				    tProfileForm.setValue("txName", getApplicationHostnPort());
                                }
				return mapping.findForward(action);
			} else if ("preview".equals(action)) {
				return mapping.findForward(action);
			} else if ("start".equals(action)) {
				String savedProfile = (String) tProfileForm.getValue("tunerUpdateProfile");
				String savedTxName = (String) tProfileForm.getValue("txName");
				tProfileForm.setValue("savedTunerUpdateProfile", savedProfile);
				tProfileForm.setValue("savedTxName", savedTxName);
				GUIUtils.setToSession(request, IWebAppConstants.SESSION_DISPLAY_RS, load(tProfileForm));
				return mapping.findForward(action);
			} else if ("getProfile".equals(action)) {
				GUIUtils.setToSession(request, IWebAppConstants.SESSION_DISPLAY_RS, load(tProfileForm));
				return mapping.findForward(action);
			} else if ("ok".equals(action)) {
				ISubscription sub = (ISubscription) GUIUtils.getFromSession(request, PAGE_TCHPROPS_SUB);
				String profileName = (String) tProfileForm.getValue("tunerUpdateProfile");
                                String profileTx   = (String) tProfileForm.getValue("txName");
				if (profileName == null || profileName.length() == 0) {
					sub.setProperty("", PROFILE_PROPERTY, null);
				} else {
					profileName =  stripProfilePrefix(profileName);
                                         profileName = PROFILE_PREFIX + profileName;
                    profileName = stripOutExistingPriority(sub.getProperty("", PROFILE_PROPERTY), profileName);
					sub.setProperty("", PROFILE_PROPERTY, profileName);
				}

                if (profileName == null || profileName.length() == 0 || profileTx == null || profileTx.length() == 0) {
                    sub.setProperty("", PROFILE_TX, null);
				} else {
                    profileTx = stripOutExistingPriority(sub.getProperty("", PROFILE_TX), profileTx);
                    sub.setProperty("", PROFILE_TX, profileTx);
				}

				// load main page so that user can view preview
				action = "load";
				GUIUtils.removeFromSession(request, IWebAppConstants.SESSION_DISPLAY_RS);
				return mapping.findForward(action);
			} else if ("cancel".equals(action)) {
				// reset to stored value and reload main page
				String savedProfile = (String) tProfileForm.getValue("savedTunerUpdateProfile");
				String savedTxName = (String) tProfileForm.getValue("savedTxName");
				tProfileForm.setValue("tunerUpdateProfile", savedProfile);
				tProfileForm.setValue("txName", savedTxName);
				action = "load";
				GUIUtils.removeFromSession(request, IWebAppConstants.SESSION_DISPLAY_RS);
				return mapping.findForward(action);
			} else if ("add".equals(action)) {
				//profile name must be prefixed with ._profile
				String newProfile = request.getParameter("profile");
				newProfile = stripProfilePrefix(newProfile);
				add(newProfile, request.getParameter("type"), tProfileForm);
				return mapping.findForward(action);
			}

		} catch (SystemException e) {
			e.printStackTrace();  //To change body of catch statement use Options | File Templates.
		}
		String forward = tProfileForm.getForward();
		return (new ActionForward(forward, true));

	}

	private String stripProfilePrefix(String newProfile) {
		int pos;
		while ((pos = newProfile.indexOf("=")) > -1) {
			newProfile = newProfile.substring(pos + 1);
		}
		if (newProfile.startsWith(PROFILE_PREFIX)) {
			pos = newProfile.indexOf(PROFILE_PREFIX);
			if (pos > -1){
				newProfile =  newProfile.substring(pos + PROFILE_PREFIX.length());
			}
		}
		return newProfile;
	}

	private PropsBean getTunerConfig(String host) {
		String txName = host;
		host = UTFUtils.convertFromURLUTF8(host);
		if (host.endsWith("/")) {
			host = host.substring(0, host.length() - 1);
		}
		if (WEBAPP_DEBUG) {
			System.out.println("Host : " + host);
		}

		String protocol = "http";
		if (host.indexOf("https://") >= 0) {
			protocol = "https";
			host = host.substring("https://".length());
		}
		if (host.indexOf("http://") >= 0) {
			host = host.substring("http://".length());
		}


		int idx = host.indexOf('/');
		String path = "/";
		if (idx >= 0) {
			path = host.substring(idx);
			host = host.substring(0, idx);
		}

		int port = 80;
		idx = host.indexOf(':');
		if (idx >= 0) {
			String portStr = host.substring(idx + 1);
			try {
				port = Integer.parseInt(portStr);
			} catch (Exception e) {
				port = -1;
			}
			host = host.substring(0, idx);
		}


		URL url = null;
		FastInputStream fis = null;

		//first set the URL to the txurl given by the user
		//later on, it will change if the TXList is found

		try {
			// get the transmitter information
			url = new URL(protocol, host, port, "/list");
			if (WEBAPP_DEBUG) {
				System.out.println("Url : " + url.toString());
			}
			fis = new FastInputStream(url.openStream(), 4 * 1024);
			TransmitterListing txList = new TransmitterListing();
			txList.getList(fis, host, url);

			if (txName.endsWith(SERVICE_NAME)) {
				//we have the full path to the channel get the info on that channel
				Vector allChannels = txList.getChannels(path);
				if (allChannels != null) {
					for (Enumeration e = allChannels.elements(); e.hasMoreElements();) {
						PropsBean p = (PropsBean) e.nextElement();
						if ("channel".equals(p.getProperty("type")) && p.getProperty("url").endsWith(SERVICE_NAME)) {
							p.setProperty("url", p.getProperty("url"));
							return p;
						}
					}
				}
			} else {
				Vector allChannels = txList.getChannelsOnly(path, true);
				if (allChannels != null) {
					for (Enumeration e = allChannels.elements(); e.hasMoreElements();) {
						PropsBean p = (PropsBean) e.nextElement();
						if ("channel".equals(p.getProperty("type")) && p.getProperty("url").endsWith(SERVICE_NAME)) {
							p.setProperty("url", p.getProperty("url"));
							return p;
						}
					}
				}
			}
		} catch (Exception e) {
			if (DEBUG3) {
				System.out.println("Failed to get channel list for: " + url + "\n\t" + e);
			}
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();  //To change body of catch statement use Options | File Templates.
				}
			}
		}
		return null;
	}

	private Collection load(TunerProfileForm tProfileForm) {

		try {
			String url = (String) tProfileForm.getValue("txName");
			PropsBean p = getTunerConfig(url);
			if (p != null) {
				tProfileForm.setValue("txName", p.getValue("url"));
				ArrayList profiles = new ArrayList();
				String seg = p.getProperty("segment");
				StringTokenizer tk = new StringTokenizer(seg, "|");
				while (tk.hasMoreElements()) {
					String s = tk.nextToken();
					if (s.startsWith(".profile_")) {
						s = s.substring(9);
						PropsBean pp = new PropsBean();
						pp.setProperty(LDAPConstants.DISPLAY_NAME, s);
						pp.setProperty("dn", s);
						profiles.add(pp);
					}
				}
				return profiles;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return new ArrayList();
	}

	private String getApplicationHostnPort() throws MalformedURLException {

		ServletContext sc = servlet.getServletConfig()
		        .getServletContext();
		IWebApplicationInfo webAppInfo = (IWebApplicationInfo) sc.getAttribute("com.marimba.servlet.context.info");
		URL subUrl = new URL(webAppInfo.getChannel().getURL());
		return new URL(subUrl.getProtocol(), subUrl.getHost(), subUrl.getPort(), "").toString();

	}

	private void add(String profileName, String type, TunerProfileForm tProfileForm) {
		if (profileName != null)
			if ("_remove_".equals(profileName)) {
				tProfileForm.setValue("tunerProfileType", "");
				tProfileForm.setValue("tunerUpdateProfile", "");
			} else {
				tProfileForm.setValue("tunerUpdateProfile", profileName);
				tProfileForm.setValue("tunerProfileType", type);
			}
	}
}



