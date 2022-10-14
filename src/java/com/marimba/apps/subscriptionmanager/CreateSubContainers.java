// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager;

import java.util.*;

import javax.naming.*;
import javax.naming.directory.*;

import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.intf.*;
import com.marimba.apps.subscription.common.intf.objects.*;
import com.marimba.apps.subscription.common.intf.objects.dao.*;
import com.marimba.apps.subscription.common.objects.dao.*;
import com.marimba.apps.subscription.common.util.*;

import com.marimba.apps.subscriptionmanager.intf.*;

import com.marimba.tools.ldap.*;

import com.marimba.webapps.intf.*;

/**
 * Creates a hierarchy of containers under the Subscription base container that represents the structure of an AD forest. For each domain, there's a
 * Subscription object of type container that is used to target all the objects for that domain. DN structure of containers For the roots :
 * "cn=namespace1.com, &lt;subscription base>" For subdomains : "cn=westchild, cn=west, cn=namespace1.com, &lt;subscription base>" "cn=abc, cn=namespace2.com,
 * &lt;subscription base>"
 *
 * @author Theen-Theen Tan
 * @version 1.6, 04/04/2003
 */
public class CreateSubContainers
    implements IErrorConstants,
                   IAppConstants {
    static String[]  containerAttrs = { "showInAdvancedViewOnly: FALSE", "objectclass: top", "objectclass: container" };
    SubscriptionMain main;
    String           subBase;
    LDAPConnection   subConn;
    IUser            user;
    List             existingContainers = new ArrayList(ISubscriptionConstants.DEF_COLL_SIZE);
    List             resConExist = new ArrayList(ISubscriptionConstants.DEF_COLL_SIZE);
    List             resConNew = new ArrayList(ISubscriptionConstants.DEF_COLL_SIZE);
    List             resSubExist = new ArrayList(ISubscriptionConstants.DEF_COLL_SIZE);
    List             resSubNew = new ArrayList(ISubscriptionConstants.DEF_COLL_SIZE);

    /**
     * Creates a new CreateSubContainers object.
     *
     * @param main REMIND
     * @param user REMIND
     *
     * @throws SystemException REMIND
     * @throws SubKnownException REMIND
     */
    public CreateSubContainers(SubscriptionMain main,
                               IUser            user)
        throws SystemException {
        if (!LDAPConstants.VENDOR_AD.equals(main.getLDAPProperty(LDAPConstants.PROP_VENDOR))) {
            throw new SubKnownException(CONTAINER_NOTAD);
        }

        this.main    = main;
        this.subBase = main.getSubBase();
        this.user    = user;
        this.subConn = this.user.getSubConn();
    }

    /**
     * Create containers under the Subscription Container to reflect an AD forest's domain structure. Does nothing for IPlanet.
     *
     * @throws SystemException REMIND
     * @throws SubKnownException REMIND
     */
    public void create()
        throws SystemException {
        resConNew.clear();
        resSubExist.clear();
        resSubNew.clear();

        try {
            String cfgCtx = LDAPConnUtils.getInstance(main.getTenantName())
                                         .getConfigNamingContext(subConn);
            List   domains = subConn.searchAndReturnDomains(cfgCtx);
            Vector roots = ((LDAPEnvAD) main.getLDAPEnv()).getRoots();
            getExistingContainer();

            // Create the tree roots first in the case that we have tree roots
            // with different levels of namespaces
            List roottmp = new ArrayList((Vector) roots.clone());

            while (!roottmp.isEmpty()) {
                Iterator ite = roottmp.iterator();

                while (ite.hasNext()) {
                    Name root = (Name) ite.next();
                    createContainer(LDAPSubscription.getDomainDn(root, subConn, subBase, roots, main.getLDAPVarsMap()));
                    createDomainSub(root);
                    ite.remove();
                }
            }

            // We start with the higher level domains before
            // the subdomains so that the containers for the
            // parent domain would be created before the sub domains.
            int level = 1;

            while (!domains.isEmpty()) {
                Iterator ite = domains.iterator();

                while (ite.hasNext()) {
                    Name dom = (Name) ite.next();

                    if (dom.size() == level) {
                        createContainer(LDAPSubscription.getDomainDn(dom, subConn, subBase, roots, main.getLDAPVarsMap()));
                        createDomainSub(dom);
                        ite.remove();
                    }
                }

                level++;
            }

            createAllSub();
        } catch (NamingException ne) {
            LDAPUtils.classifyLDAPException(ne);
        } catch (LDAPLocalException le) {
            throw new SubKnownException(le, CONTAINER_CREATEFAILED);
        }
    }

    /**
     * Returns a list domains for which child containers exists since the last execution of create().
     *
     * @return a list of DN(javax.directory.naming.Name) of the domains
     */
    public List getContainerExist() {
        return resConExist;
    }

    /**
     * Returns a list domains for which child containers are created since the last execution of create().
     *
     * @return a list of DN(javax.directory.naming.Name) of the domains
     */
    public List getContainerNew() {
        return resConNew;
    }

    /**
     * Returns a list domains for which Subscription objects are created since the last execution of create().
     *
     * @return a list of DN(javax.directory.naming.Name) of the domains
     */
    public List getDomainSubExist() {
        return resSubExist;
    }

    /**
     * Returns a list domains for which Subscription objects are created since the last execution of create().
     *
     * @return a list of DN(javax.directory.naming.Name) of the domains
     */
    public List getDomainSubNew() {
        return resSubNew;
    }

    private void getExistingContainer()
        throws NamingException {
        List containerStr = main.listNameSpaces(subConn);
        existingContainers.clear();
        resConExist.clear();

        for (int i = 0; i < containerStr.size(); i++) {
            existingContainers.add(subConn.getParser().parse((String) containerStr.get(i)));
            resConExist.add(containerStr.get(i));
        }
    }

    private boolean containerExists(Name con)
        throws NamingException {
        for (int i = 0; i < existingContainers.size(); i++) {
            if (con.equals((Name) existingContainers.get(i))) {
                return true;
            }
        }

        return false;
    }

    private void createContainer(Name conName)
        throws NamingException {
        if (containerExists(conName)) {
            return;
        }

        Name res = subConn.getParser()
                          .parse(subBase);

        for (int i = 0; i < conName.size(); i++) {
            res.add(conName.get(i).toString());
        }

        boolean exists = false;

        for (int j = 0; j < existingContainers.size(); j++) {
            if (((Name) existingContainers.get(j)).equals(conName)) {
                exists = true;

                break;
            }
        }

        if (!exists) {
            existingContainers.add(conName);
        }

        subConn.createObject(res.toString(), containerAttrs, false);
        resConNew.add(conName.toString());
    }

    private void createDomainSub(Name domain)
        throws NamingException, 
                   SystemException {
        ISubscription sub = ObjectManager.createSubscription(domain.toString(), ISubscriptionConstants.TYPE_DOMAIN, user);

        if (sub.exists()) {
            resSubExist.add(domain.toString());
        } else {
            sub.save();
            resSubNew.add(domain.toString());
        }
    }

    private void createAllSub()
        throws NamingException, 
                   SystemException {
        ISubscription sub = ObjectManager.createSubscription(ISubscriptionConstants.TYPE_ALL, ISubscriptionConstants.TYPE_ALL, user);

        if (sub.exists()) {
            resSubExist.add(ISubscriptionConstants.TYPE_ALL);
        } else {
            sub.save();
            resSubNew.add(ISubscriptionConstants.TYPE_ALL);
        }
    }
}
