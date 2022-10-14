// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.intf.util.IProperty;
import com.marimba.tools.txlisting.TransmitterListing;
import com.marimba.tools.util.Props;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.net.URL;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.StringResourcesHelper;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.operation.SelectedPkgsBean;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageEditForm;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.KnownException;

import com.marimba.webapps.tools.taglib.txlisting.TxListingBean;
import com.marimba.webapps.tools.util.PropsBean;

/**
 * Handles the adding of a package from a transmitter listing page
 *
 * @author Theen-Theen Tan
 * @version $Revision$, $Date$
 */
public final class PackageAddAction extends AbstractAction implements IWebAppsConstants {
    final static boolean DEBUG = IAppConstants.DEBUG;
    /**
     * REMIND
     *
     * @param mapping REMIND
     * @param form REMIND
     * @param req REMIND
     * @param response REMIND
     *
     * @return REMIND
     *
     * @throws IOException REMIND
     * @throws ServletException REMIND
     * @throws GUIException REMIND
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest req,
                                 HttpServletResponse response) throws IOException, ServletException {
        init(req);

        TxListingBean txbean = (TxListingBean) req.getSession().getAttribute(TXLIST_LISTING);
        TransmitterListing listing = (TransmitterListing) req.getSession().getAttribute("txlisting");

        PackageEditForm editForm = (PackageEditForm) form;
        SelectedPkgsBean pkgs = editForm.getSelectedPkgs(req);
        String url = editForm.getAddUrl();
        Vector<PropsBean> chnlist = null;

        try {
            if (url == null) {
                throw new SubInternalException(PKG_INTERNAL_ADD_PATHNOTFOUND);
            }

            if (txbean == null) {
                throw new SubInternalException(PKG_INTERNAL_ADD_TXNOTFOUND);
            }

            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            boolean isChannel = isChannel(txbean.getList(), url);

            // remind: adh, in the future we need a configurable that indicates whether the target LDAP has been
            // changed to handle case sensitivity

            // if two packages are added and their paths are identical apart from case sensitivity, AD will
            // throw an AttributeInUseException.  therefore for AD only we will make sure packages are case
            // insensitively unique until we document this and allow the option of this via configuration.
            Set caseInsensitivePks = new TreeSet(new CIStrComp());
            caseInsensitivePks.addAll(pkgs.getPackageUrls());
            // After 8.5.01, txlisting from shared module so need to encode url for supporting space and other supporting char's
            // todo: after porting catech cms fix for jre 1.8 issue to verify this case
            url = com.marimba.tools.util.URLUTF8Decoder.decode(url);
            // Specific Patch channel changes
            if(isSpecialPatchChannel(url)) {
            	url = com.marimba.tools.util.URLUTF8Decoder.decode(url);
            }
            if (isChannel) {
                for(char aChar : SPL_CHARS) {
                    if(url.indexOf(aChar) != -1){
                        throw new KnownException(PKG_ADD_TARGET_SPLCHAR, url);
                    }
                }

                if (DEBUG) {
                    System.out.println("add channel: " + url);
                }

                chnlist = txbean.getList();

                for (PropsBean channel : chnlist) {
                	String chURL = com.marimba.tools.util.URLUTF8Decoder.decode(channel.getProperty("url"));
                	if(isSpecialPatchChannel(chURL)) {
                		chURL = com.marimba.tools.util.URLUTF8Decoder.decode(chURL);
                	}
                    if (url.equals(chURL)) {
                        if (pkgs.getPackage(url) != null){
                            throw new KnownException(PKG_ADD_PKGEXIST, url);
                        }
                        if(caseInsensitivePks.contains(url)) {
                            throw new KnownException(PKG_ADD_TARGET_SIMILAR_PATH_ERROR, url);
                        }
                        PropsBean channelcopy = (PropsBean) channel.clone();
                        channelcopy.setProperty("type",ISubscriptionConstants.CONTENT_TYPE_APPLICATION);
                        pkgs.addPackage(channelcopy);
                        caseInsensitivePks.add(url);

                        break;
                    }
                }
            } else {
                if (DEBUG) {
                    System.out.println("add directory: " + url);
                }

                TxListingBean listingBean = new TxListingBean();
                chnlist = new Vector<PropsBean>();
                getChannelList(url, listing, listingBean, chnlist, req.getLocale());
                chnlist = listingBean.getList();

                //To check whether the package from the selected folder exist
                boolean pathFlag=false;
                String tempStr=null;
                for (Iterator itetr =caseInsensitivePks.iterator(); itetr.hasNext();) {
                    tempStr=  (String) itetr.next();
                    if(tempStr.startsWith(url)) {
                        pathFlag=true;
                        break;
                    }
                }

                for (PropsBean channel : chnlist) {
                    channel.setProperty("type",ISubscriptionConstants.CONTENT_TYPE_APPLICATION);
                    // Only add packages from folders which is not already in the selected packages bean
                    String chaURL = channel.getProperty("url");
                    for(int i=0; i < SPL_CHARS.length; i++){
                    	chaURL = com.marimba.tools.util.URLUTF8Decoder.decode(chaURL);
                    	if(isSpecialPatchChannel(chaURL)) {
                    		chaURL = com.marimba.tools.util.URLUTF8Decoder.decode(chaURL);
                    	}
                        if(chaURL.indexOf(SPL_CHARS[i]) != -1){
                            throw new KnownException(PKG_ADD_TARGET_SPLCHAR, chaURL);
                        }
                    }
                    if ((pkgs.getPackage(chaURL) != null) && (!pathFlag) ) {
                        throw new KnownException(PKG_ADD_PKGEXIST, url);
                    }

                    if(caseInsensitivePks.contains(chaURL) && (!pathFlag) )  {
                        throw new KnownException(PKG_ADD_GROUP_SIMILAR_PATH_ERROR,chaURL, url);
                    }
                    pkgs.addPackage((PropsBean) channel.clone());
                    caseInsensitivePks.add(chaURL);
                }
            }

            editForm.setSelectedPkgs(pkgs, req);
        } catch (Exception e) {
            throw new GUIException(PKG_ADD_ERROR, e);
        }

        return (mapping.findForward("success"));
    }
    // This method special case for verizon issue
    // they trying to add patch channel using Packages tab in Policy
    // that patch channel contains space char
    // as normally we allowed but here patch channel encode when bundle time using Patch Manager 
    // and another encode happen when publish time .. due to two time of encode problem occured
    // actually fix patch manager also but this is special case so without affect patch manager functionality
    // safe fix Policy Manager itself.
    // for example, 
    // http://HICHEL38784.ad.harman.com:5282/FF16-010.QFF47005999.Firefox Setup 47.0.exe._1880624429 (origional)
    // http://HICHEL38784.ad.harman.com:5282/FF16-010.QFF47005999.Firefox+Setup+47.0.exe._1880624429 (Patch Manager encode)
    // http://HICHEL38784.ad.harman.com:5282/FF16-010.QFF47005999.Firefox%2bSetup%2b47.0.exe._1880624429 (publish encode)
    // fix ix, we need to decode double time and send to ldap only identify patch channel
    private boolean isSpecialPatchChannel(String url) {
    	try {
    		if(null != url && url.indexOf("/Patches/") > 0 && url.indexOf("+") > 0) {
    			System.out.println("Special Patch Channel URL : " + url);
    			return true;
    		}
    	} catch(Exception ed) {
    		ed.printStackTrace();
    	}
    	return false;
    }
    private void getChannelList(String urlStr, TransmitterListing listing, TxListingBean listingBean, Vector<PropsBean> result, Locale locale) throws Exception {
        URL url = new URL(urlStr);
        IProperty[] entryList = listing.list("/localhost" + url.getFile(), locale);

        System.out.println("Host from the url: " + url.getHost());
        System.out.println("getFile() from the url: " + url.getFile());
        System.out.println("Result count from list(): " + entryList.length);

        for (IProperty entry : entryList) {


            if ("folder".equals(entry.getProperty("type"))) {
                String urlString = urlStr + ((urlStr.endsWith("/")) ? "" : "/")  + entry.getProperty("name");
                getChannelList(urlString, listing, listingBean, result, locale);
            } else {
                Props props = new Props();

                String entryUrl = entry.getProperty("url");
                String entryTitle = entry.getProperty("title");
                String entryType = entry.getProperty("type");
                String entryName = entry.getProperty("name");

                System.out.println("[Url: " + entryUrl + "] [Type: " + entryType + "] [Title: " + entryTitle + "] [Name: " + entryName + "]");

                int idx = entryUrl.lastIndexOf("/");
                if (idx != -1) {
                    entryUrl = entryUrl.substring(idx + 1);
                }

                props.setProperty("url", url + ((urlStr.endsWith("/")) ? "" : "/") + entryUrl);
                props.setProperty("title", (entryTitle == null) ? entryName : entryTitle);
                props.setProperty("type", entryType);
                props.setProperty("name", entryName);

                result.add(new PropsBean(props));
            }
        }

        if (isChannel(result, urlStr)) {
            listingBean.setIsChannel(true);
            int index = urlStr.lastIndexOf("/");
            if (index > 0) {
                urlStr = urlStr.substring(0, index);
            } else {
                urlStr = "/";
            }
        }

        listingBean.setList(result);
        listingBean.setPath(urlStr);
    }

    /**
     * This class is a case insensitive comparator.  We need this because if we add two packages
     * that are comparable even due to case differences, AD will throw an AttributeInUseException
     * since it carries out a case-insensitive comparison before adding an attribute.
     */
    public class CIStrComp implements Comparator {
        public int compare(Object o1, Object o2) {
            if(o1 instanceof String && o2 instanceof String) {
                String ci1 = ((String)o1).toLowerCase();
                String ci2 = ((String)o2).toLowerCase();
                return ci1.compareTo(ci2);
            } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                // use standard comparator
                return ((Comparable)o1).compareTo(o2);
            } else if(o1 == null || o2 == null) {
                throw new NullPointerException(StringResourcesHelper.getMessage(OBJECT_IS_NULL, o1, o2));
            } else {
                throw new ClassCastException(StringResourcesHelper.getMessage(OBJECT_NOT_COMPARABLE, o1, o2));
            }
        }
    }

}