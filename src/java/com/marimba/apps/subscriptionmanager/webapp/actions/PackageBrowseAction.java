// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.PackageEditForm;
import com.marimba.intf.util.IProperty;
import com.marimba.tools.config.ConfigProps;
import com.marimba.tools.txlisting.TransmitterListing;
import com.marimba.tools.txlisting.TransmitterListingManager;
import com.marimba.tools.util.Password;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.tools.taglib.txlisting.TxListingBean;
import com.marimba.webapps.tools.util.PropsBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Vector;

/**
 * Browsing a Transmitter Listing.  There are two cases this action is called:
 * 1. If TXLIST_CURRENT_URL found in request, this is a traversal when user click on an element in the listing.
 * 2. The user has entered a new url into the browse box, and clicked the "Go" button. We get the url value from the form.
 *
 * Some times the request times out for a huge Tx, so we are no more with the Taglibs to load and display the contents of Tx.
 * Instead of this Taglibs we are going for a delayed action to avoid request time out.
 *
 *
 *
 * @author Theen-Theen Tan
 * @author Tamilselvan Teivasekamani
 * @version $Revision$, $Date$
 */


public final class PackageBrowseAction extends AbstractAction implements IAppConstants {

    protected Task createTask(ActionMapping mapping, ActionForm form,
                              HttpServletRequest request, HttpServletResponse response) {
        return new InitTask(mapping, form, request, response);
    }

    protected class InitTask extends SubscriptionDelayedTask {
        HttpServletRequest request;
        String txlist_currenturl;

        InitTask(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
            super(mapping, form, request, response);
            this.request = request;
            txlist_currenturl = request.getParameter("txlist_currenturl");
        }

        public void execute() {

            debug("Action Called: " + txlist_currenturl);

            String refresh = "true";
            String txurl = ((PackageEditForm) form).getUrl();
            String txUser = ((PackageEditForm) form).getUser();
            String txPwd = ((PackageEditForm) form).getPwd();
            String selection = ((PackageEditForm) form).getSelection();

            if (selection != null && selection.trim().length() > 0) {
                txurl = txurl + ((txurl.endsWith("/")) ? "" : "/") + selection;
            }

            session.setAttribute(TXLIST_CURRENT_URL, txurl);
            session.removeAttribute(TXLIST_BEAN);

            debug("txurl = " + txurl);
            debug("refresh = " + refresh);

            try {
                txurl = list(txurl, txUser, txPwd);
            } catch (IOException ioEx) {
                guiException = new GUIException(ioEx);
                ioEx.printStackTrace();
            }

            ((PackageEditForm) form).setUrl(txurl);
            ((PackageEditForm) form).setUser(txUser);
            ((PackageEditForm) form).setPwd(txPwd);

            session.setAttribute("txlist_currenturl", txurl);
            session.setAttribute(TXLIST_CURRENT_USERNAME, txUser);
            session.setAttribute(TXLIST_CURRENT_PASSWORD, txPwd);

            forward = mapping.findForward("success");
        }

        private String list(String txUrl, String txUser, String txPwd) throws IOException {
            if (txUrl == null) {
                return "";
            }

            String channelUrl = txUrl;
            channelUrl = channelUrl.replace(" ", "+");
            txUser = (txUser == null) ? "" : txUser;
            txPwd = (txPwd == null) ? "" : txPwd;

            String txAuth = txUser + ":"+ txPwd;
            debug("Tx Auth ->" + txAuth);
            String enc = Password.encode(txAuth);

            ConfigProps env = new ConfigProps(null);
            env.setProperty("auth", enc);
            URL  url = null;
            try {
                url = new URL(channelUrl);
                debug("url: " + url);
            } catch (MalformedURLException ex) {
                if (DEBUG) {
                    ex.printStackTrace();
                }

                return "";
            }

            //Scan all the channels in Tx and figure out the required channels
            TransmitterListingManager txlistingmanager = new TransmitterListingManager(10);
            TransmitterListing listing = txlistingmanager.get(url, Locale.getDefault(), env);

            // find whether the given url is folder or not; if its not, listing with parent path
            String path = listing.getParentIfChannel("/localhost" + url.getFile());
            debug("path: " + path);
            IProperty[] entryList = null;
            URL newUrl = null;
            if (path != null) {
                newUrl = new URL(url.getProtocol(), url.getHost(), url.getPort(), path);
                debug("newly constructed url: " + newUrl.toString());
                listing = txlistingmanager.get(newUrl, Locale.getDefault(), env);
                entryList = listing.list("/localhost" + path, locale);
            } else {
                // ToDo: throw proper error message
                return "";
            }

            debug("Host from the url: " + url.getHost());
            debug("getFile() from the url: " + url.getFile());
            debug("Result count from list(): " + entryList.length);

            Vector<PropsBean> result = new Vector<PropsBean>();
            if (path != null && !"/".equals(path)) {
                String parent = "";
                String hostPrefix = newUrl.toExternalForm();
                int index = path.lastIndexOf('/');
                if (index != -1 && index != (path.length() - 1)) {
                    parent = path.substring(0, index);
                }
                Props p = new Props();
                p.setProperty("title", "Up to higher level listing");
                p.setProperty("url", hostPrefix.substring(0,hostPrefix.lastIndexOf('/')));
                p.setProperty("type", "upfolder");
                result.add(new PropsBean(p));
            }

            for (IProperty entry : entryList) {
                Props props = new Props();

                String entryUrl = entry.getProperty("url");
                String entryTitle = entry.getProperty("title");
                String entryType = entry.getProperty("type");
                String entryName = entry.getProperty("name");

                debug ("[Url: " + entryUrl + "] [Type: " + entryType + "] [Title: " + entryTitle + "] [Name: " + entryName + "]");

                int idx = entryUrl.lastIndexOf("/");
                if (idx != -1) {
                    entryUrl = entryUrl.substring(idx + 1);
                }

                String newUrlStr = newUrl.toString();
                props.setProperty("url", newUrlStr + ((newUrlStr.endsWith("/")) ? "" : "/") + entryUrl);
                props.setProperty("title", (entryTitle == null) ? entryName : entryTitle);
                props.setProperty("type", entryType);
                props.setProperty("name", entryName);

                result.add(new PropsBean(props));
            }

            TxListingBean listingBean = new TxListingBean();
            if (isChannel(result, channelUrl)) {
                listingBean.setIsChannel(true);
                int index = channelUrl.lastIndexOf("/");
                if (index > 0) {
                    channelUrl = channelUrl.substring(0, index);
                } else {
                    channelUrl = "/";
                }
            }

                debug("Path is after getValidPath: " + channelUrl);

            listingBean.setList(result);
            listingBean.setPath(channelUrl);

            session.setAttribute(TXLIST_LISTING, listingBean);
            session.setAttribute("txlisting", listing);

            return channelUrl;
        }
    }

    private String getValidPath(String path, com.marimba.webapps.tools.taglib.txlisting.TransmitterListing txList) {
        while (!txList.checkPathValid(path)) {
            int index = path.lastIndexOf("/");
            path = path.substring(0, index);
            if (null == path || path.equals("")){
                return "/";
            }
        }
        return path;
    }

//    public boolean checkPathValid(String path) {
//        if (tree_off == true) {
//            for (Enumeration e = channels.elements(); e.hasMoreElements();) {
//                PropsBean p = (PropsBean) e.nextElement();
//                String chPath = p.getProperty("url");
//                int index = chPath.lastIndexOf("/");
//                if (index > 0) {
//                    chPath = "/" + chPath.substring(0, index);
//                } else {
//                    chPath = "/";
//                }
//
//                if (path.equals(chPath)) {
//                    return true;
//                }
//            }
//        } else {
//            ITreeNode node;
//
//            if (path.startsWith("/") && !path.equals("/")) {
//                path = path.substring(1);
//            }
//
//            node = (DirNode) tree.getRoot().lookup(new StrString(path));
//            if (node != null) {
//                return true;
//            }
//        }
//        return false;
//    }

    private void debug(String msg) {
        if(DEBUG) System.out.println("PackageBrowseAction: " + msg);
    }
}

