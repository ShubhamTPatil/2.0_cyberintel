// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.actions;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.intf.ILDAPDataSourceContext;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelComparator;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.intf.msf.acl.AclException;
import com.marimba.intf.msf.acl.AclStorageException;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.KnownException;
import com.marimba.webapps.intf.SystemException;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import javax.naming.Name;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;

/**
 * This class handles the situation when a user selects a target from the package view page.
 *
 * @author Jayaprakash Paramasivam
 * @version 1.0, 05/09/2005

 */
public final class AddTargetsAction
        extends AbstractAction {
    final static boolean DEBUG = IAppConstants.DEBUG;
    static TargetChannelComparator comp = new TargetChannelComparator(ISubscriptionConstants.TARGET_DIRECTLYASSIGNED_KEY);

    /**
     * REMIND
     *
     * @param mapping  REMIND
     * @param form     REMIND
     * @param request  REMIND
     * @param response REMIND
     * @return REMIND
     * @throws IOException      REMIND
     * @throws ServletException REMIND
     * @throws GUIException     REMIND
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
            throws IOException,
            ServletException {
        init(request);
        String targetName = request.getParameter("name");
        String targetType = request.getParameter("type");
        String targetID = request.getParameter("id");
        String forward = "package";

        if (DEBUG) {
            System.out.println("AddTargetsPkgAction : Target Name:  "+targetName);
            System.out.println("AddTargetsPkgAction : Target Type:  "+targetType);
            System.out.println("AddTargetsPkgAction : Target ID:  "+targetID);
        }
        String childContainer = null;
        LDAPBean ldapBean = getLDAPBean(request);
        ILDAPDataSourceContext ldapCtx;
        HashMap results = new HashMap(50);
        LDAPConnection subConn;

        try {
            targetType = LDAPUtils.objClassToTargetType(targetType, LDAPVarsMap);
        } catch (SubInternalException ex) {
            new GUIException(ex);
        }

        ServletContext sc = servlet.getServletConfig()
                .getServletContext();
        SubscriptionMain main = TenantHelper.getTenantSubMain(sc, request);

        // If sourcing users from Transmitter, and we are not browsing user
        // group from the Transmitter, the default Target type will be
        // machinegroup
        if (!main.getUsersInLDAP() && TYPE_USERGROUP.equals(targetType) && "ldap".equals(ldapBean.getEntryPoint())) {
            targetType = TYPE_MACHINEGROUP;
        }

        Target target = new Target(targetName, targetType, targetID);
        String patchSessionVar = ADD_REMOVE_PATCH;
        String packageSessionVar = ADD_REMOVE_PACKAGE;

        HttpSession session = request.getSession();
        ArrayList targetList  = (ArrayList) session.getAttribute(SESSION_TGS_FROMPKGS_RS);
        List patchSessionList = (ArrayList) session.getAttribute(patchSessionVar);
        List packageSessionList = (ArrayList) session.getAttribute(packageSessionVar);

        if (null == patchSessionList) {
            patchSessionList = new ArrayList(DEF_COLL_SIZE);
        }

        if (null == packageSessionList) {
            packageSessionList = new ArrayList(DEF_COLL_SIZE);
        }

        String  channelSessionVar = MAIN_PAGE_PACKAGE;

        if (request.getSession().getAttribute(SESSION_MULTIPKGBOOL) != null) {
            channelSessionVar = MAIN_PAGE_M_PKGS;
        }
        ArrayList channels = new ArrayList(DEF_COLL_SIZE);

        channels = (ArrayList) session.getAttribute(channelSessionVar);

        TargetChannelMap tchannelMap = null;
        Channel selectedChn = null;
        // Obtain the child container of the result (subscription
        // base not included in the DN)

        boolean primaryAdmin = Utils.isPrimaryAdmin(request);

        if (!primaryAdmin && main.isAclsOn()) {
            IUser user = (IUser) session.getAttribute(SESSION_SMUSER);

            if(DEBUG) {
                System.out.println("User = " + user);
                System.out.println("User name = " + user.getName());
                System.out.println("tg = " + target);
                System.out.println("tg.getId = " + target.getId());
            }

            try {
                if (!main.getAclMgr().subWritePermissionExists(user, target,null, true, main.getUsersInLDAP())) {
                    if(DEBUG) {
                        System.out.println("User doesn't have Subscription write permission on target");
                    }
                    throw new GUIException(new KnownException(ADD_TARGET_NO_WRITE_PERMISSION,target.getName()));
                }
            } catch (AclStorageException ae) {
                throw new GUIException(new KnownException(ADDREMOVETG_ACLSTORAGE_ERROR));
            } catch (AclException ae) {
                throw new GUIException(new KnownException(ADDREMOVETG_ACL_ERROR));
            } catch (NamingException nexc) {
                try {
                    LDAPUtils.classifyLDAPException(nexc, null, true);
                } catch (SystemException sysexc) {
                    throw new GUIException(sysexc);
                }
            }
        }

        try {
            ldapCtx = main.getLDAPDataSourceContext(GUIUtils.getUser(request));
            subConn = ldapCtx.getSubConn();
            childContainer = main.getSubBaseWithNamespace(ldapCtx);
            String subName = LDAPName.unescapeJNDISearchResultName(childContainer);
            Name subDN = subConn.getParser()
                    .parse(subName);
            subDN.remove(subDN.size() - 1);
            childContainer = subDN.toString();

            for (int i = 0; i < channels.size(); i++) {
                tchannelMap = new TargetChannelMap(target, childContainer);
                selectedChn = (Channel) channels.get(i);
                tchannelMap.addChannelEdit(selectedChn);
                tchannelMap.setState("subscribe");
                results.put(selectedChn.getUrl().toString(), tchannelMap);
            }

            List result = new ArrayList(results.values());

            if(isMember(main, subConn, targetList, tchannelMap)) {
                throw new KnownException(IErrorConstants.ADD_TARGET_ITEM_ALREADY_EXISTS);
            }

            if ( (packageSessionList != null) && ( packageSessionList.size() != 0) ) {
                if(!isMember(main, subConn, packageSessionList, tchannelMap)){
                    result.addAll(packageSessionList);
                } else {
                    throw new KnownException(IErrorConstants.ADD_TARGET_ITEM_ALREADY_EXISTS);
                }
            }

            if ( (patchSessionList != null) && ( patchSessionList.size() != 0) ) {
                if(!isMember(main, subConn, patchSessionList, tchannelMap)){
                    result.addAll(patchSessionList);
                } else {
                    throw new KnownException(IErrorConstants.ADD_TARGET_ITEM_ALREADY_EXISTS);
                }
            }


            Collections.sort(result, comp);
            // Set new Target list to session var
            List patchResult = new ArrayList(DEF_COLL_SIZE);
            List packageResult = new ArrayList(DEF_COLL_SIZE);

            for(int i=0;i<result.size();i++) {
                TargetChannelMap tcmap = (TargetChannelMap) result.get(i);
                Hashtable hash = tcmap.getChannels();
                Set channelURLs = hash.keySet();
                Iterator ite = channelURLs.iterator();
                while(ite.hasNext()) {
                    Channel ch = tcmap.getChannel((String)ite.next());
                     if(CONTENT_TYPE_PATCHGROUP.equals(ch.getType())) {
                        if(!isMember(main, subConn, patchResult, tcmap)){
                            patchResult.add(tcmap);
                        }
                    } else {
                        if(!isMember(main, subConn, packageResult, tcmap)){
                            packageResult.add(tcmap);
                        }
                    }
                }
            }
//            GUIUtils.setToSession(request, targetsSessionVar, (Object) result);
            GUIUtils.setToSession(request, patchSessionVar, (Object) patchResult);
            GUIUtils.setToSession(request, packageSessionVar, (Object) packageResult);

//            GUIUtils.setToSession(request, SESSION_PERSIST_RESETRESULTS, "true");

            if(patchResult.size() > 0) {
                forward = "patch";
            }
        } catch (KnownException ke) {
            throw new GUIException(ke);
        } catch (NamingException ne) {
            throw new GUIException(ne);
        } catch (SystemException se) {
            GUIException guie = new GUIException(se);
            throw guie;
        }
        return (mapping.findForward(forward));
    }

    private boolean isMember(SubscriptionMain main, LDAPConnection conn, List targetsList, TargetChannelMap target)
            throws NamingException {
        boolean result = false;
        Target tg = target.getTarget();
        String addDN = null;
        String existDN = null;
        Map<String, String> LDAPVarsMap = main.getLDAPVarsMap();

        if(!main.getUsersInLDAP()) {
            addDN = tg.getId();
        } else {
            addDN = LDAPWebappUtils.escapeDN(conn, tg, LDAPVarsMap.get("TARGET_ALL"));
        }

        if (target != null) {
            int size = targetsList.size();

            for (int i = 0; i < size; i++) {
                TargetChannelMap tmap = (TargetChannelMap)targetsList.get(i);
                Target tg1 = tmap.getTarget();
                if(!main.getUsersInLDAP()) {
                    existDN = tg1.getId();
                } else {
                    existDN = LDAPWebappUtils.escapeDN(conn, tg1, LDAPVarsMap.get("TARGET_ALL"));
                }

                if(addDN.equals(existDN)){
                    result = true;

                    break;
                }
            }
        }

        if (DEBUG) {
            System.out.println("Added TargetChannelMap to the list : "+ target.toString());
            System.out.println("isMember= " + result);
        }
        return result;
    }
}

