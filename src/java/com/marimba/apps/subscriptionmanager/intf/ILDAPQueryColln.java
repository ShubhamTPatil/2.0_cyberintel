package com.marimba.apps.subscriptionmanager.intf;


import com.marimba.apps.subscriptionmanager.ldapquery.LDAPQueryCollection;
import com.marimba.apps.subscription.common.intf.LogConstants;

/**
 * Constants that can be used from any part of Subscription Manager. This includes the commandline and the webapplication.
 *
 * @author Narayanan. A R
 * @author Kumaravel. A
 * @version 1.0, 20/12/2004
 */
public interface ILDAPQueryColln extends LogConstants {
    int SPM_LDAP_QUERY_COLLN_MIN = 50000;
    int LOG_LDAP_NOSUBSCRIPTIONCONFIG = SPM_LDAP_QUERY_COLLN_MIN  + 1; // major, ldap config
}
