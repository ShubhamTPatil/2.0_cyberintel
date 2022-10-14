// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.users;

import java.io.File;
import java.util.*;

import javax.naming.NamingException;
import javax.servlet.http.HttpSession;

import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.users.IUserDataSource;
import com.marimba.apps.subscriptionmanager.intf.users.IUserDataSourceContext;
import com.marimba.intf.ldap.ILDAPManagedFactory;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.msf.*;
import com.marimba.intf.util.IProperty;
import com.marimba.rpc.RPCSession;
import com.marimba.tools.ldap.*;
import com.marimba.tools.util.Password;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.SystemException;

/**
 * A subscription manager administrator
 *
 * @author Theen-Theen Tan
 * @version 1.13, 04/23/2003
 */
public class User
    implements IUserDataSourceContext,
                   IAppConstants {
    final static boolean DEBUG = IAppConstants.DEBUG;
    LDAPConnection       browseconn;
    LDAPConnection       subconn;
    LDAPConnection       currentDCconn;
    LDAPConnectionCache  connCache;
    boolean              initialized = false;
    RPCSession           rpcSession;

    // This is the RPC session that is used for obtaining the user
    // and user group listing if sourcing users from the plugin
    // transmitter. It has to be per user session to ensure that
    // another user does not close this session.
    HttpSession      httpSession; // the user session for the current instance of the the logged in user
    SubscriptionMain main;
	String           userDN = null;
    String           resolvedDN = null;
    String           encPassword;
    Props            props = new Props();
    File             userFile;
    IUserDataSource  src;
    boolean          exists = false;
    IUserPrincipal   wauser; // The user passed by CSF.  May contain role information.
    IProperty        ldapCfg;
    LDAPEnv          ldapEnv;
    Set              groupMemberships;
    Set              userContainers;
    boolean 		 kerberosEnabled;
    String			 tenantName;
    ITenant 		 tenant;
    /**
     * Creates a new User object.
     *
     * @param wauser REMIND
     * @param httpSession REMIND
     * @param password REMIND
     * @param main REMIND
     *
     * @throws SystemException REMIND
     */
    public User(IUserPrincipal   wauser,
                HttpSession      httpSession,
                String           password,
                SubscriptionMain main)
        throws SystemException {
        if (DEBUG) {
            //System.out.println("Getting User Preference for " + wauser.getName());
        }

        this.wauser           = wauser;
        this.tenant			  = main.getTenant();
        this.tenantName		  = tenant.getName();
        this.encPassword      = Password.encode(password);
        this.httpSession      = httpSession;
        this.main             = main;
        System.out.println("Tenant Data dir :" + main.getDataDirectory());
        this.ldapCfg          = main.getLDAPConfig();
        this.ldapEnv          = main.getLDAPEnv();
        this.connCache        = new LDAPConnectionCache(this);
        this.rpcSession       = null;
        this.groupMemberships = new TreeSet();
        this.userContainers   = new TreeSet();

		this.kerberosEnabled = LDAPConstants.AUTHMETHOD_KERBEROS.equalsIgnoreCase(this.ldapCfg.getProperty(LDAPConstants.PROP_AUTHMETHOD));
        
        if (DEBUG2) {
            connCache.ldapCfgDebug();
        }

        if (DEBUG) {
            //System.out.println("opened " + props.getFile().getAbsolutePath());
        }
    }
    public String getTenantName() {
    	return tenantName;
    }

    public ITenant getTenant() {
    	return tenant;
    }

    public void setMessage(String url, boolean showOnce) { return; }

    /**
     * Loads a User object from persistant storage
     *
     * @throws SystemException REMIND
     */
    public void load()
        throws SystemException {
        if (exists) {
            src.load(this);
        } else {
            if (DEBUG) {
                System.out.println("Setting Defaults");
            }

            setDefaults();
            save();
        }

        if (DEBUG) {
            System.out.println("User Loaded");
            System.out.println(toString());
        }
    }

    /**
     * Saves a User object from persistant storage
     *
     * @throws SystemException REMIND
     */
    public void save()
        throws SystemException {
        src.save(this);

        if (DEBUG) {
            System.out.println("User Saved");
        }
    }

    /**
     * Deletes a User object from persistant storage
     *
     * @throws SystemException REMIND
     */
    public void delete()
        throws SystemException {
        src.delete(this);
        exists = false;
        props  = new Props();

        // REMIND tcube use objmgr to delete so that handle to User won't be available
        if (DEBUG) {
            System.out.println("User Deleted");
        }
    }

    //
    // Implements IProperty
    //

    /**
     * Implements IUserDataSourceContext
     *
     * @param key REMIND
     * @param value REMIND
     */
    public void setProperty(String key,
                            String value) {
        props.setProperty(key, value);
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Props getProps() {
        return this.props;
    }

    /**
     * REMIND
     *
     * @param props REMIND
     */
    public void setProps(Props props) {
        this.props = props;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public boolean exists() {
        return this.exists;
    }

    /**
     * REMIND
     *
     * @param ext REMIND
     */
    public void setExists(boolean ext) {
        this.exists = ext;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public RPCSession getRPCSession() {
        return rpcSession;
    }

    /**
     * REMIND
     *
     * @param inRPCSession REMIND
     */
    public void setRPCSession(RPCSession inRPCSession) {
        this.rpcSession = inRPCSession;
    }

    /**
     * Implements ILDAPDataSourceContext
     *
     * @return REMIND
     */
    /**
     * Returns the browsing ldap connection for this user
     *
     * @return REMIND
     */
    public LDAPConnectionCache getConnectionCache() {
        return connCache;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public LDAPConnection getBrowseConn() {
        return browseconn;
    }

    /**
     * Sets the browsing ldap connection for this user
     *
     * @param conn REMIND
     */
    public void setBrowseConn(LDAPConnection conn) {
        this.browseconn = conn;
    }

    /**
     * Returns the subscription ldap connection for this user
     *
     * @return REMIND
     */
    public LDAPConnection getSubConn() {
        return subconn;
    }

    /**
     * Sets the subscription ldap connection for this user
     *
     * @param conn REMIND
     */
    public void setSubConn(LDAPConnection conn) {
        this.subconn = conn;
    }

    /**
     * Returns the collection ldap connection for this user
     *
     * @return REMIND
     */
    public LDAPConnection getCollConn() {
        return currentDCconn;
    }

    /**
     * Sets the subscription ldap connection for this user
     *
     * @param conn REMIND
     */
    public void setCollConn(LDAPConnection conn) {
        this.currentDCconn = conn;
    }

    /**
     * Returns the namespace for this user
     *
     * @return REMIND
     */
    public String getNameSpace() {
        return getProperty(PROP_NAMESPACE);
    }
    public IProperty getSubscriptionConfig() {
    	return main.getSubscriptionConfig();
    }
    public Map<String, String> getLDAPVarsMap() {
    	return main.getLDAPVarsMap();
    }
    /**
     * Properties are obtained in precedence of : - PROP_PASSWORD and PROP_USERRDN from this user - LDAP properties from LDAP configuration obtained from CMS -
     * Any other properties associated with this user, for example GUI preferences The USERRDN and PASSWORD needs to be before the CMS's config to simulate
     * the an ldapConfig without into connCache
     *
     * @param key REMIND
     *
     * @return REMIND
     */
    public String getProperty(String key) {
    	if(IUserPrincipal.DN.equals(key)) {
    		return wauser.getProperty(key);
    	}
    	
        if (LDAPConstants.PROP_USERRDN.equals(key)) {
            return userDN;
        }

        if (LDAPConstants.PROP_PASSWORD.equals(key)) {
            return encPassword;
        }

        String ldapvalue = ldapCfg.getProperty(key);

        if (ldapvalue != null) {
            return ldapvalue;
        }

        return props.getProperty(key);
    }
    
    /**
     * Obtains the property pairs for the user preferences and the ldap environment
     *
     * @return REMIND
     */
    public String[] getPropertyPairs() {
        int      length = 0;

        String[] proppairs = props.getPropertyPairs();

        if (proppairs != null) {
            length = length + proppairs.length;
        }

        String[] ldappairs = ldapCfg.getPropertyPairs();

        if (proppairs != null) {
            length = length + ldappairs.length;
        }

        String[] pairresults = new String[length];
        int      pos = 0;

        if (proppairs != null) {
            System.arraycopy(proppairs, 0, pairresults, 0, proppairs.length);
            pos = proppairs.length;
        }

        if (ldappairs != null) {
            System.arraycopy(ldappairs, 0, pairresults, pos, proppairs.length);
        }

        return pairresults;
    }

    /*
     *    Handle multi-user session
     */

    /**
     * The session created when the user logged in.
     *
     * @return REMIND
     */
    public HttpSession getSession() {
        return this.httpSession;
    }

    /**
     * Implements IUserPrincipal
     *
     * @return REMIND
     */
    public String getName() {
        return wauser.getName();
    }

    public String getFullName() {
        return wauser.getFullName();
    }

    /**
     * REMIND
     *
     * @param another REMIND
     *
     * @return REMIND
     */
    public boolean equals(Object another) {
        if (another instanceof User) {
            return wauser.getName()
                         .equalsIgnoreCase(((User) another).getName());
        }

        return false;
    }


    /**
     * REMIND
     *
     * @return REMIND
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getClass().getName());
        buf.append("[user=")
           .append(getName())
           .append("-")
           .append(httpSession.getId())
           .append('-')
           .append(rpcSession)
           .append(']');

        return buf.toString();
    }

    private void setDefaults() {
        setProperty("showExclude", "false");
    }

    /**
     * REMIND
     */
    public void destroy() {
        if (DEBUG) {
            System.out.println("User:destroy " + (wauser == null?"":wauser.getName()));
        }

        if (rpcSession != null) {
            rpcSession.close();
            rpcSession = null;
        }

        if (subconn != null) {
            subconn.unbind();
            subconn = null;
        }

        if (browseconn != null) {
            browseconn.unbind();
            browseconn = null;
        }

        if (currentDCconn != null) {
            currentDCconn.unbind();
            currentDCconn = null;
        }

        if (connCache != null) {
            connCache.cleanup();

            // GCG: should not this also be set to null?
        }

        if (props != null) {
            props.close();
        }
    }

    /**
     * REMIND
     *
     * @throws SystemException REMIND
     */
    public void connectToLDAP()
        throws SystemException {
    	resolvedDN = main.resolveUserDN(getName());
    	
    	// if kerberos feature enabled then user DN value should be short name of user name only
    	if(kerberosEnabled) {
    		userDN = getName();
    	} else {
    		userDN = resolvedDN;
    	}
        
        main.getLDAPEnv()
            .connectUserToLDAP(this);
    }

    void loadGroupMemberships() {
        if (main.getUsersInLDAP()) {
            //groupMemberships
            String[]      resultDNs = null;
            resultDNs     = new String[1];
            resultDNs [0] = resolvedDN;

            IProperty subConfig = main.getSubscriptionConfig();
            String collmode = "distributed";
            collmode = ldapCfg.getProperty(LDAPConstants.CONFIG_COLLECTIONMODE);

            String childContainer = main.getSubBaseWithNamespace(this);

            // Resolve group membership
            // get the domain only for the child container
            String domainDN = LDAPConnUtils.getDomainDNFromDN(getSubConn(), childContainer);

            // convert into the DNS format
            String subDomain = LDAPConnUtils.getDomainFromDN(getSubConn(), domainDN);


            try {
                // retrieve all the containers that the user is in and store them
                //
                userContainers = new TreeSet(main.getLDAPEnv().getEnclosingContainers(main.getAdminUser().getBrowseConn(), resolvedDN));
                // now retrieve the groups the user is in
                //
                groupMemberships = new TreeSet(main.getLDAPEnv().getGroupMembership(resultDNs, ldapCfg, connCache));
            } catch (LDAPException le) {
                le.printStackTrace();
            } catch (LDAPLocalException lle) {
                lle.printStackTrace();
            } catch (NamingException ne) {
                ne.printStackTrace();
            }
        }
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Set getGroupMemberships() {
        return groupMemberships;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public Set getUserContainers() {
        return userContainers;
    }

    /**
     * REMIND
     *2
     * @return REMIND
     */
    public boolean isInitialized() {
        return initialized;
    }

	  /**
      *  Retrieves the LDAP environment authenticated with the user's credentials
      * @return the LDAP environment authenticated with the user's credentials
      */
     public ILDAPManagedFactory getLDAPManagedFactory() {
        return null;
     }
    /**
     * REMIND
     *
     * @throws SystemException REMIND
     */
    public void initialize()
        throws SystemException {
        src = UserManager.getUserDataSource(main);
        src.open(this);
        connectToLDAP();
        loadGroupMemberships();
        load();
        initialized = true;

        if (DEBUG) {
            System.out.println("initialize User key = " + getName());
        }
    }

    /**
     * REMIND
     *
     * @throws SystemException REMIND
     */
    public IUserPrincipal getUser() {
		return this.wauser;
    }

    public boolean hasRole(String role){
       return this.wauser.hasRole(role);
    }

    public boolean hasRole(String role, String application){
        return this.wauser.hasRole(role, application);
    }

    public Hashtable getRoles() {
        return this.wauser.getRoles();
    }

    public Locale getLocale() {
        return this.wauser.getLocale();
    }

    public boolean hasPermOnAppln(String application) {
        return this.wauser.hasPermOnAppln(application);
    }

    public String getUserRole() {
        return this.wauser.getUserRole();
    }

    public HashMap listApplications() {
        return this.wauser.listApplications();
    }
    public SubscriptionMain getMain() {
		return main;
	}
	public void setMain(SubscriptionMain main) {
		this.main = main;
	}

}
