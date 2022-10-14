// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import org.apache.struts.action.Action;
import org.apache.struts.util.MessageResources;
import org.apache.struts.Globals;

import java.util.*;

import javax.naming.NamingException;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;
import javax.servlet.jsp.tagext.*;

import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.util.LDAPUtils;

import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.ldapsearch.PackageViewQueryBuilder;
import com.marimba.apps.subscriptionmanager.users.User;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.intf.ILdapSearch;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.system.PagingBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelComparator;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;

import com.marimba.tools.ldap.*;

import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;

import com.marimba.webapps.tools.util.WebAppUtils;
import com.marimba.webapps.tools.util.PropsBean;
import com.marimba.intf.msf.acl.AclException;
import com.marimba.intf.msf.acl.AclStorageException;
import com.marimba.intf.msf.acl.IAclConstants;

/**
 * Custom Tag to return a list of targets from a list of packages passed in. This is used for both single select and multi select mode in the GUI.
 *
 * @author Rahul Ravulur
 * @version 1.27, 12/26/2002
 */
public class GetTargetsFromPkgsTag
    extends TagSupport
    implements IWebAppConstants,
                   IWebAppsConstants,
                   IAppConstants,
                   ISubscriptionConstants {
    static TargetChannelComparator comp = new TargetChannelComparator("title");

    // this encapsulates the sort type and sort order of the results
    String           statebeanstring;
    boolean          sortorder = IWebAppsConstants.ASCENDING; // the default sort order
    String           sessionpkgs = MAIN_PAGE_PACKAGE;
    boolean          isPaging;
    HttpSession      session;
    SubscriptionMain main;
    HashMap          targets = new HashMap();
    boolean          multiMode = false;
    String           pagingType;
    String           subBase;
    String           childContainer;
    LDAPConnection   conn;
    MessageResources resources;


    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getStateBean() {
        return this.statebeanstring;
    }

    /**
     * REMIND
     *
     * @param statebeanstring REMIND
     */
    public void setStateBean(String statebeanstring) {
        this.statebeanstring = statebeanstring;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getPkgs() {
        return this.sessionpkgs;
    }

    /**
     * REMIND
     *
     * @param pkgs REMIND
     */
    public void setPkgs(String pkgs) {
        this.sessionpkgs = pkgs;
    }

    /**
     * DOCUMENT ME!
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag()
        throws JspException {
        Locale locale = (Locale) pageContext.getAttribute(Globals.LOCALE_KEY, PageContext.SESSION_SCOPE);

        if (locale == null) {
            locale = Locale.getDefault();
        }

        comp.setLocale(locale);

	    pageContext.getSession().removeAttribute("aclread");
        resources = (MessageResources) pageContext.getAttribute(Globals.MESSAGES_KEY, PageContext.APPLICATION_SCOPE);
        comp.setMessageResources(resources);

        ServletContext servletContext = pageContext.getServletContext();

        session = pageContext.getSession();

        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        main = TenantHelper.getTenantSubMain(servletContext, request);
        /* Obtain the user that is making the request.  This is so that we can get the
         * correct namespace for obtaining subscriptions.
         */

        try {
            subBase        = main.getSubBase();
            childContainer = main.getSubBaseWithNamespace(GUIUtils.getUser(request));
            conn           = LDAPWebappUtils.getSubConn(request);
            pagingType     = (String) session.getAttribute(SESSION_PAGINGTYPE);
        }          
        catch (SystemException se) {
            se.printStackTrace();
            throw new JspException(StringResourcesHelper.getMessage(COULD_OBTAIN_CONNECTION__FROM_PKGS));
        }

        /* Obtain the list of packages from the session
         */
        if (session.getAttribute(SESSION_MULTIPKGBOOL) != null) {
            multiMode   = true;
            sessionpkgs = MAIN_PAGE_M_PKGS;
        }

        if (DEBUG) {
            System.out.println("GetTargetsFromPkgsTag: sessionpkgs = " + sessionpkgs);
        }

        ArrayList packages = (ArrayList) session.getAttribute(sessionpkgs);

        if ((packages == null) || packages.isEmpty()) {
            if (DEBUG) {
                System.out.println("GetTargetsFromPkgsTag: targets is null or empty");
            }

            //no results should be displayed if all of the selected targets have been
            //removed. This was done to implement the clear list functionality
            pageContext.getSession()
                       .removeAttribute(SESSION_TGS_FROMPKGS_RS);

            return (EVAL_BODY_INCLUDE);
        }

        //verify that the statebean is defined. Otherwise this method is not being used
        //properly
        IMapProperty statebean = (IMapProperty) session.getAttribute(statebeanstring);

        if (statebean == null) {
            //REMIND: we need to throw an gui exception from here that shows up in the
            //error page
            if (DEBUG) {
                System.out.println("GetTargetsFromPkgsTag: statebean is null or empty");
            }

            return (EVAL_BODY_INCLUDE);
        }

        String sorttype = request.getParameter("sorttype");

        if (sorttype == null) {
            sorttype = "name";
        }

        // This conversion from "tgmap_contenttype" to "type" is because the
        // TargetChannelMap.getType is used for target type, and type is used for channels.
        // Didn't want to change that implementation fo TargetChannelMap.getType
        if (TARGETCHANNELMAP_CONTENTTYPE.equals(sorttype)) {
            sorttype = CH_TYPE_KEY;
        }

        boolean sortorder = true;
        String sortorderstring = request.getParameter("sortorder");

        if (sortorderstring != null) {
            if (!"true".equals(sortorderstring)) {
                sortorder = false;
            }
        }

        comp.setSortOrder(sortorder);
        comp.setSortProperty(sorttype);
        LDAPBean ldapBean = getLDAPBean(request);

        //This functionality is used to extract the common targets assigned to packages residing in different containers.
        Vector   list=null;
        Vector tgtsVecList=null;
        Hashtable currentListHT=null;



        if(multiMode)
        {
            tgtsVecList= search(request, ldapBean, packages, subBase, childContainer);
            if(tgtsVecList==null || tgtsVecList.size()==0)
            {
             boolean countFlag=false;
            for(int i=0;i<packages.size();i++)
            {
                    currentListHT  =(Hashtable) session.getAttribute(SESSION_TGTS_FROM_PKGS);
                    if(currentListHT==null)
                    {
                     currentListHT=new Hashtable();
                    }
                       if(i>0)
                       countFlag=true;

                        if(currentListHT.get(((Channel)packages.get(i)).getTitle())==null)
                        {
                            List pkgsArrayList=new ArrayList();
                            pkgsArrayList.add((Channel)packages.get(i));
                            list= search(request, ldapBean, pkgsArrayList, subBase, childContainer);
                            
                            for(int count=0;count<list.size();count++)
                            {
                                if(tgtsVecList!=null)
                                {
                                for(int y=0;y<tgtsVecList.size();y++)
                                {
                                    if(((( TargetChannelMap )tgtsVecList.elementAt( y )).getName()).equals(((( TargetChannelMap )list.elementAt( count )).getName())))
                                    {
                                       String isSelectedTarget=(( TargetChannelMap )tgtsVecList.elementAt( y )).getIsSelectedTarget();
                                       String isSelectedTargetFrmList=(( TargetChannelMap )list.elementAt( count )).getIsSelectedTarget();
                                        if(isSelectedTarget.equals("true") && isSelectedTargetFrmList.equals("true"))
                                        {
                                            //Do nothing
                                            //list.setElementAt(( TargetChannelMap )list.elementAt( count ),count);
                                        }
                                        else if(isSelectedTarget.equals("true"))
                                        {
                                            list.setElementAt(( TargetChannelMap )tgtsVecList.elementAt( y ),count);
                                        }
                                    }
                                }
                                }
                            }
                            tgtsVecList=checkExistance(tgtsVecList,list,countFlag);
                            currentListHT.put(((Channel)packages.get(i)).getTitle(),tgtsVecList);
                            session.setAttribute(SESSION_TGTS_FROM_PKGS,currentListHT);
                        }
                        else
                        {
                            tgtsVecList= (Vector)currentListHT.get(((Channel)packages.get(i)).getTitle());
                        }
            }
            }
        }
        else
        {
        tgtsVecList= search(request, ldapBean, packages, subBase, childContainer);
        }
        //Pass the list into the comparator
        Collections.sort(tgtsVecList, comp);
        // REMIND::RCR change search to return an ArrayList
        ArrayList listing = new ArrayList(tgtsVecList);
        session.removeAttribute(TGS_FROMPKGSLIST_BEAN);
        session.setAttribute(SESSION_TGS_FROMPKGS_RS, listing);
        return (EVAL_BODY_INCLUDE);
    }

    /**
     * Process the end of this tag.  The default implementation does nothing.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doEndTag()
        throws JspException {
        return (EVAL_PAGE);
    }

    private Vector search(HttpServletRequest request,
                          LDAPBean           ldapBean,
                          List               channellist,
                          String             subBase,
                          String             childContainer)
        throws JspException {
        // search for the entries one level below the selected container
        Vector listing = null;
        try {
            IUser user = (IUser) session.getAttribute(SESSION_SMUSER);
            boolean primaryAdmin = Utils.isPrimaryAdmin((HttpServletRequest) pageContext.getRequest());
            String allEndpoints = resources.getMessage(Locale.getDefault(), "page.global.All");

            listing = LDAPWebappUtils.searchTargetaForPkg(channellist, subBase, childContainer,
                    allEndpoints, pagingType, user, primaryAdmin, main);

        } catch (NamingException ne) {
            if (DEBUG) {
                ne.printStackTrace();
            }

            try {
                LDAPUtils.classifyLDAPException(ne);
            } catch (SystemException ie) {
                WebAppUtils.saveTagException(pageContext, ie);
            }
        }catch (LDAPLocalException lle) {
            if (DEBUG) {
                lle.printStackTrace();
            }

            WebAppUtils.saveTagException(pageContext, lle);
        } catch (SystemException ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }

            WebAppUtils.saveTagException(pageContext, ex);
        } catch (AclException ae) {
            if (DEBUG) {
                ae.printStackTrace();
            }

            WebAppUtils.saveTagException(pageContext, ae);
        } catch (AclStorageException ae) {
            if (DEBUG) {
                ae.printStackTrace();
            }

            WebAppUtils.saveTagException(pageContext, ae);
        }

        return listing;
    }

    //This function is used to check for the existance of given target in the session maintained targets
    private Vector checkExistance(Vector finalList,Vector currentList,boolean flag)
    {
            if( (finalList == null || finalList.size()==0) &&  (!flag))
            {
                    finalList = currentList;
            }
            else if( finalList != null && currentList != null )
            {
                Vector local_targets = new Vector();
                TargetChannelMap local_target = null;
                    for( int indx = 0; indx < finalList.size(); indx ++ ){
                    local_target = ( TargetChannelMap )finalList.elementAt( indx );

                    TargetChannelMap target = null;
                    contains: for( int curIndx = 0; curIndx < currentList.size(); curIndx++ ){
                        target = ( TargetChannelMap )currentList.elementAt( curIndx );


                        if( target.getName().equals( local_target.getName() ) ){
                            local_targets.add( target );
                            break contains;
                        }
                    }
                }
                finalList = local_targets;
            }
        return finalList;
    }



    /**
     * REMIND
     *
     * @param req REMIND
     *
     * @return REMIND
     */
    public LDAPBean getLDAPBean(HttpServletRequest req) {
        HttpSession session = req.getSession();
        LDAPBean    ldapBean = (LDAPBean) session.getAttribute(SESSION_LDAP);

        if (null == ldapBean) {
            ldapBean = new LDAPBean();
        }

        return ldapBean;
    }

    /**
     * REMIND
     *
     * @param ldapBean REMIND
     * @param req REMIND
     *
     * @throws SubInternalException REMIND
     */
    public void setLDAPBean(LDAPBean           ldapBean,
                            HttpServletRequest req)
        throws SubInternalException {
        if (null == ldapBean) {
            throw new SubInternalException(CANT_SET_NULL_SYSTEM_STATE_LDAPBEAN);
        }

        HttpSession session = req.getSession();
        session.setAttribute(SESSION_LDAP, ldapBean);
    }

    /**
     * REMIND
     *
     * @param req REMIND
     *
     * @return REMIND
     */
    public PagingBean getPagingBean(HttpServletRequest req) {
        HttpSession session = req.getSession();
        PagingBean  pagingBean = (PagingBean) session.getAttribute(SESSION_PAGE);

        if (null == pagingBean) {
            pagingBean = new PagingBean();
        }

        return pagingBean;
    }

    /**
     * REMIND
     *
     * @param pagingBean REMIND
     * @param req REMIND
     *
     * @throws SubInternalException REMIND
     */
    public void setPagingBean(PagingBean         pagingBean,
                              HttpServletRequest req)
        throws SubInternalException {
        if (null == pagingBean) {
            throw new SubInternalException(CANT_SET_NULL_SYSTEM_STATE_PAGINGBEAN);
        }

        HttpSession session = req.getSession();
        session.setAttribute(SESSION_PAGE, pagingBean);
    }

    /**
     * REMIND
     *
     * @param req REMIND
     */
    public void removeLDAPBean(HttpServletRequest req) {
        HttpSession session = req.getSession();
        session.removeAttribute(SESSION_LDAP);
    }
}
