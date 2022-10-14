// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.LDAPEnv;
import com.marimba.apps.subscription.common.LDAPVars;
import com.marimba.apps.subscription.common.TargetResHelper;
import com.marimba.apps.subscription.common.intf.ILDAPDataSourceContext;
import com.marimba.apps.subscription.common.intf.IUser;
import com.marimba.apps.subscription.common.intf.SubKnownException;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscription.common.objects.dao.LDAPSubscription;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.actions.AbstractAction;
import com.marimba.apps.subscriptionmanager.webapp.system.LDAPBean;
import com.marimba.apps.subscriptionmanager.webapp.util.GUIUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.LDAPWebappUtils;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelComparator;
import com.marimba.apps.subscriptionmanager.webapp.util.TargetChannelMap;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.intf.admin.IUserDirectory;
import com.marimba.intf.msf.acl.AclException;
import com.marimba.intf.msf.acl.AclStorageException;
import com.marimba.intf.util.IProperty;
import com.marimba.tools.ldap.*;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.IWebAppsConstants;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.tools.util.WebAppUtils;
import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;

import javax.naming.Name;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchResult;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.ServletContext;
import java.util.*;

/**
 * Custom Tag to return a list of packages according to the target list passed in. This is used for both single select and multi select mode in the GUI for
 * target details. It constructs a list of packages that are associated with the selected target(s). This list of targets is sorted by the chosen column in
 * the GUI.
 *
 * @author Angela Saval
 * @version $Revision$, $Date$
 */
public class GetPkgsFromTargetsTag
        extends TagSupport
        implements IWebAppConstants,
        IWebAppsConstants,
        IAppConstants,
        ISubscriptionConstants {

	final static boolean DEBUG = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;
	final static boolean DEBUG2 = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG2;
	final static boolean DEBUG3 = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG3;
	boolean isOrphanPolicy = false;
    MessageResources resources;

	/* These are the attributes that are to be retrieved from the entries returned
	 * from the LDAP query.  Since we will be getting subscription entries back, we
	 * can know which attributes to get from the entries.
	 */

	static TargetChannelComparator comp = new TargetChannelComparator(TARGET_DIRECTLYASSIGNED_KEY);


	/* Used for tracking the state of what column has been chosen to sort by for the
	 * list of packages on the target details page.
	 */
	String statebeanstring;
	boolean sortorder = IWebAppsConstants.ASCENDING;

	/* This is used for obtaining the list of selected targets.
	 * MAIN_PAGE_TARGET is the variable that was used to set the vector
	 * of targets to the session
	 */
	String sessiontgs = MAIN_PAGE_TARGET;
	String search = null;

	/*
	 * The main allows for accessing subscription configuration (needed for the search base)
	 */
	SubscriptionMain main;
	Map<String, String> LDAPVarsMap;

	/* This is the connection that is used for resolving group membership.
	 */
	ILDAPDataSourceContext ldapCtx;

	/* This is the connection that is used for obtaining the list of subscriptions
	 * associated with a target.
	 */
	LDAPConnection subConn;
	LDAPConnection browseConn;

	/* Used to determine if collections are in centralized mode or not.  This
	 * is needed to determine if the selected machine is a member of collection
	 * since collections in centralized mode can contain members from other domains
	 */
	String collmode = "distributed";

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
	 * Copies any selected or excluded targets into the temp variables used by the select_exclude.jsp page.  This is necessary to populate the selected and
	 * excluded boxes with any targets previously chosen.
	 *
	 * @return REMIND
	 * @throws JspException if a JSP exception has occurred
	 */
	public int doStartTag()
	        throws JspException {
		Locale locale = (Locale) pageContext.getAttribute(Globals.LOCALE_KEY, PageContext.SESSION_SCOPE);

		if (locale == null) {
			locale = Locale.getDefault();
		}

		comp.setLocale(locale);
		pageContext.getSession().removeAttribute("aclread");
		HttpSession session = pageContext.getSession();
		LDAPBean ldapBean = (LDAPBean) session.getAttribute(SESSION_LDAP);
		String tenantName = (String) session.getAttribute(SESSION_TENANTNAME);
		ServletContext context = pageContext.getSession().getServletContext();
		main = TenantHelper.getTenantSubMain(context, session, tenantName);
		LDAPVarsMap = main.getLDAPVarsMap();
		//main = (SubscriptionMain) pageContext.getServletContext().getAttribute(APP_MAIN);

		boolean browsingLDAP = main.getUsersInLDAP() || "ldap".equals(ldapBean.getEntryPoint());
		resources = (MessageResources) pageContext.getAttribute(Globals.MESSAGES_KEY, PageContext.APPLICATION_SCOPE);
		comp.setMessageResources(resources);

		List targets = AbstractAction.getSelectedTargets(session);

		if ((targets == null) || targets.isEmpty()) {
			//no results should be displayed if all of the selected targets have been
			//removed. This was done to implement the clear list functionality
			session.removeAttribute(SESSION_PKGS_FROMTGS_RS);
			session.removeAttribute(TARGET_PKGS_BEAN);

			return (EVAL_BODY_INCLUDE);
		}

		session.removeAttribute("hasPackages");
		session.removeAttribute(IWebAppConstants.INVALID_NONEXIST);
		//verify that the statebean is defined. Otherwise this method is not being used
		//properly
		IMapProperty statebean = (IMapProperty) session.getAttribute(statebeanstring);

		if (statebean == null) {
			//REMIND: we need to save a tag  exception  here that shows up in the
			//error page
			return (EVAL_BODY_INCLUDE);
		}

		String sorttype = (String) statebean.getValue("sorttype");

		if (sorttype == null) {
			sorttype = CH_TITLE_KEY;
		}

		// This conversion from "tgmap_contenttype" to "type" is because the
		// TargetChannelMap.getType is used for target type, and type is used for channels.
		// Didn't want to change that implementation fo TargetChannelMap.getType
		if (TARGETCHANNELMAP_CONTENTTYPE.equals(sorttype)) {
			sorttype = CH_TYPE_KEY;
		}

		comp.setSortProperty(sorttype);

		boolean sortorder = true;
		String sortorderstring = (String) statebean.getValue("sortorder");

		if (!"true".equals(sortorderstring)) {
			sortorder = false;
		}

		comp.setSortOrder(sortorder);

		try {
			ldapCtx = GUIUtils.getUser((HttpServletRequest) pageContext.getRequest());
			subConn = ldapCtx.getSubConn();
			browseConn = ldapCtx.getBrowseConn();

			collmode = ldapCtx.getProperty(LDAPConstants.CONFIG_COLLECTIONMODE);
		} catch (SystemException se) {
            if (DEBUG) {
                se.printStackTrace();
            }
			WebAppUtils.saveTagException(pageContext, se);
		}

		if (DEBUG) {
			System.out.println("acls are on: " + main.isAclsOn());
		}


		pageContext.getSession()
		        .setAttribute("aclwrite", "on");

		StringBuffer searchFilter = createSearchFilter_FindPolicyForTargets(session, targets, browsingLDAP);



		//domain
		//NamingEnumeration nenum;
		//HashMap tgMapTbl = new HashMap(50); /* Used to store the channels as they are gotten from
		// * the search results
		//*/

		HashMap resultsTbl; // The results returned when it is known that
		// the the channel exists for all targets in multimode

		resultsTbl = getAllPakages(searchFilter, main, browsingLDAP,
		        targets, subConn, browseConn);
		// Set LDAP results to session var
		ArrayList result = new ArrayList(resultsTbl.values());

		if (DEBUG) {
			System.out.println("GetPkgsFromTargetsTag: results size = " + result.size());
		}

		//Pass the list into the comparator
		Collections.sort(result, comp);

		if (DEBUG) {
			System.out.println("GetPkgsFromTargetsTag: results size after = " + result.size());
		}

		if (result.size() > 0) {
			session.setAttribute("hasPackages", "true");
		}

		pageContext.getSession()
		        .setAttribute(SESSION_PKGS_FROMTGS_RS, result);
		pageContext.getSession()
		        .removeAttribute(TARGET_PKGS_BEAN);

		if (DEBUG) {
			System.out.println("set page_pkgs_fromtgs_rs to session: " + result);
			System.out.println("removed previous target_pkgs_bean");
		}

		return (EVAL_BODY_INCLUDE);
	}

	/*
	 * This obtains the target from the subscription attributes. To construct a target it needs
	 * its name and type.
	 *
	 * When the target is obtained from the subscription, we cannot be certain yet of
	 * its type because there may be 4.7 subscriptions in the subscription base.  4.7 did not
	 * contain the target type as an attribute in subscription.
	 *
	 *
	 * @return Target The constructed target that is used for listing directly assigned to column
	 *        in the target view.
	 */
	Target getTarget(LDAPConnection conn,
	                 Attributes srAttrs,
	                 boolean usersInLDAP)
	        throws NamingException,
	        SystemException {
		//get the target that corresponds to the result
		Target tgdn;
		String targetID = "";
		Attribute nameAttr;
		String targetType = TYPE_USER;
		boolean useTargetDN = true;

		if (!usersInLDAP) {
			/*Get the attribute for user and group to see which is defined.
			 *We rely on the fact that both are not defined for a target
			 */
			nameAttr = srAttrs.get(LDAPVarsMap.get("TARGET_TX_USER"));

			if (nameAttr == null) {
				nameAttr = srAttrs.get(LDAPVarsMap.get("TARGET_TX_GROUP"));

				if (nameAttr != null) {
					//obtain the name of the USER GROUP sourced from the transmitter
					targetID = (String) nameAttr.get();
					useTargetDN = false;
					targetType = TYPE_USERGROUP;
				}

				/* This means that we should interpret the target DN
				 * Since this is should be a machine,machine group, or
				 * a collection.
				 */
			} else {
				//obtain the name of the USER sourced from the transmitter
				targetID = (String) nameAttr.get();
				useTargetDN = false;
			}
		}

		/* The target DN whenever a machine, machine group, or collection
		 * is specified.  This conditional can be entered even when
		 * users and user group are sourced from the transmitter.
		 */
		if (useTargetDN) {
			nameAttr = srAttrs.get(LDAPVarsMap.get("TARGETDN"));

			if (nameAttr != null) {
				targetID = (String) nameAttr.get();
			} else if (srAttrs.get(LDAPVarsMap.get("TARGET_ALL")) != null) {
				targetID = ISubscriptionConstants.TYPE_ALL;
			}
            String targetTypeAtt = LDAPVarsMap.get("TARGETTYPE");
            String subsNameAtt = LDAPVarsMap.get("SUBSCRIPTION_NAME");
            String collAtt = LDAPVarsMap.get("COLLECTION_CLASS");
			if(!isOrphanPolicy){				targetType = LDAPWebappUtils.getTargetType(conn, srAttrs, targetID, targetTypeAtt, subsNameAtt, collAtt, LDAPVarsMap);				}						
		}

        String allEndpoints = resources.getMessage(Locale.getDefault(), "page.global.All");
		tgdn = new Target(LDAPWebappUtils.getTargetName(srAttrs, targetType, conn, allEndpoints, LDAPVarsMap), targetType, targetID);

		if (DEBUG) {
			System.out.println("GetPkgsFromTargetsTag: tgdn.getName() = " + tgdn.getName());
			System.out.println("GetPkgsFromTargetsTag: tgdn.getType() = " + tgdn.getType());
			System.out.println("GetPkgsFromTargetsTag: tgdn.getId() = " + tgdn.getId());
		}

		return tgdn;
	}

	/**
	 * Process the end of this tag.  The default implementation does nothing.
	 *
	 * @return REMIND
	 * @throws JspException if a JSP exception has occurred
	 */
	public int doEndTag()
	        throws JspException {
		return (EVAL_PAGE);
	}

	static String extractChannelName(String url) {
		int i = url.lastIndexOf('/');

		if (i == -1) {
			return url;
		}

		return url.substring(i + 1);
	}

	StringBuffer createSearchFilter_FindPolicyForTargets(HttpSession session, List targets, boolean browsingLDAP) throws JspException {
		Target tg;
		//Target tgselect;
		IProperty subConfig = main.getSubscriptionConfig();
		String childContainer = main.getSubBaseWithNamespace(ldapCtx);
		boolean primaryAdmin = Utils.isPrimaryAdmin((HttpServletRequest) pageContext.getRequest());
		StringBuffer searchFilter = new StringBuffer(1024);
		//Figure out if we are in single or multi mode.  It dictates the search filter used
		boolean multiMode = false;

		if (session.getAttribute(SESSION_MULTITGBOOL) != null) {
			multiMode = true;
		}
		if (!multiMode) {
			//Just get the target from the first element in the list
			tg = (Target) targets.get(0);


			try {

				// Resolve Group and Container Memberships

				IUserDirectory userDirectory = null;

				if (!browsingLDAP && (TYPE_USER.equals(tg.getType()) || TYPE_USERGROUP.equals(tg.getType()))) {
					try {
						userDirectory = main.getUserDirectoryTx((HttpServletRequest) pageContext.getRequest());
					} catch (SystemException se) {
						WebAppUtils.saveTagException(pageContext, se);
					}
				}

				TargetResHelper targetDNs = null;
				// do group and resolution if its mot a domain and its not target type ALL
				if (!TYPE_ALL.equals(tg.getType()) && !TYPE_DOMAIN.equals(tg.getType()) && !TYPE_SITE.equals(tg.getType())) {
					try {
						targetDNs = getGroupsAndContainers(tg.getId(), tg.getType(), subConfig, userDirectory, ldapCtx, childContainer, browsingLDAP);
					} catch (LDAPException e) {
						e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
					}
				}


				// Check Acls
				if (!primaryAdmin && main.isAclsOn()) {
					IUser user = (IUser) session.getAttribute(SESSION_SMUSER);
					if (DEBUG) {
						System.out.println("User = " + user);
						System.out.println("User name = " + user.getName());
						System.out.println("tg = " + tg);
						System.out.println("tg.getId = " + tg.getId());
						System.out.println("targetDNs = " + targetDNs);
					}
					if (targetDNs != null) {
						if (DEBUG) {
							System.out.println("targetDNs.getTargetsSet() = " + targetDNs.getTargetsSet());
						}
					}

					if (!main.getAclMgr().subReadPermissionExists(user, tg,
					        targetDNs == null ? null : targetDNs.getTargetsSet(),
					        false, main.getUsersInLDAP())) {
						// update the session to indicate no read permission exists
						// no read also implies no write
						if (DEBUG) {
							System.out.println("User doesn't have Subscription read permission on target");
						}
						pageContext.getSession()
						        .setAttribute("aclread", "on");

					}

					if (!main.getAclMgr().subWritePermissionExists(user, tg,
					        targetDNs == null ? null : targetDNs.getTargetsSet(),
					        false, main.getUsersInLDAP())) {
						// update the session to indicate no write permission exists
						if (DEBUG) {
							System.out.println("User doesn't have Subscription write permission on target");
						}
						pageContext.getSession()
						        .removeAttribute("aclwrite");

					}
				}

				// Construct the searchFilter
				// Add the search components for Domain, AllAll, and the selected target itself

				LDAPWebappUtils.appendTargetAll(searchFilter, LDAPVarsMap);
				if (!TYPE_ALL.equals(tg.getType()) && !TYPE_DOMAIN.equals(tg.getType()) && !TYPE_SITE.equals(tg.getType())) {					if(IWebAppConstants.INVALID_NONEXIST.equals(tg.getType())){						session.setAttribute(IWebAppConstants.INVALID_NONEXIST, IWebAppConstants.INVALID_NONEXIST);						isOrphanPolicy = true;						if (LDAPVars.ACTIVE_DIRECTORY.equals(LDAPVarsMap.get("DIRECTORY_TYPE")) || LDAPVars.ADAM.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {							searchFilter=new StringBuffer("(" + LDAPVarsMap.get("AD_UID") + "=" + LDAPName.escapeComponentValue(tg.getID()) + ")");						 												}else{							searchFilter=new StringBuffer("(" + LDAPVarsMap.get("TARGETDN") + "=" + tg.getID() + ")");												}						return searchFilter;					} else {						searchFilter.append("(" + LDAPVarsMap.get("TARGETDN") + "=" + LDAPSearchFilter.escapeComponentValue(tg.getID()) + ")");											}				} else {
					if (TYPE_DOMAIN.equals(tg.getType())) {
						LDAPWebappUtils.getDomainAssignmentFilter(subConn, tg.getID(), searchFilter, LDAPVarsMap.get("TARGETDN"));
					}
				}
				if (targetDNs != null) {
					searchFilter.append(targetDNs.getSearchString());
				}
				if(TYPE_SITE.equals(tg.getType())) {
					searchFilter = new StringBuffer();
					LDAPWebappUtils.append2Filter(searchFilter, LDAPVarsMap.get("TARGET_ALL"), "true");
					searchFilter.append( "(&(" + LDAPVarsMap.get("SUBSCRIPTION_NAME") + "=" + tg.getID() + ")("+ LDAPVarsMap.get("TARGETTYPE") + "=" + TYPE_SITE + "))") ;
				}
				searchFilter.insert(0, "(|");
				searchFilter.append(")");


			} catch (AclStorageException aclexc) {
                if (DEBUG) {
                    aclexc.printStackTrace();
                }
				WebAppUtils.saveTagException(pageContext, aclexc.getRootCause());
			} catch (AclException aclexc) {
                if (DEBUG) {
                    aclexc.printStackTrace();
                }
				WebAppUtils.saveTagException(pageContext, aclexc.getRootCause());
			} catch (LDAPLocalException localexc) {
                if (DEBUG) {
                    localexc.printStackTrace();
                }
				WebAppUtils.saveTagException(pageContext, localexc.getRootException());
			} catch (NameNotFoundException ne) {
                if (DEBUG) {
                    ne.printStackTrace();
                }
				WebAppUtils.saveTagException(pageContext, new SubKnownException(SUB_LDAP_TARGETDELETED, tg.getId()));
			} catch (NamingException nexc) {
				try {
					LDAPUtils.classifyLDAPException(nexc, null, true);
				} catch (SystemException sysexc) {
                    if (DEBUG) {
                        sysexc.printStackTrace();
                    }
					WebAppUtils.saveTagException(pageContext, sysexc);
				}
			}

			if (DEBUG2) {
				System.out.println("GetPkgsFromTargetTag: single select, target name= " + tg.getName());
				System.out.println("GetPkgsFromTargetTag: single select, target id= " + tg.getId());
				System.out.println("GetPkgsFromTargetTag: single select, searchFilter= " + searchFilter);
			}
		} else { // multimode

			if (DEBUG) {
				System.out.println("GetPkgsFromTargetsTag: multiselect mode");
			}

			searchFilter.append("(|");

			String tgstring = "";

			/* If the users are sourced from the transmitter,
			 *  we need to construct a different
			 * search filter depending on if a user or a user group was selected
			 */
			String attributeName = null;
			String attributeValue = null;
			boolean noReadAndWrite = false;

			synchronized (targets) {
				for (ListIterator ite = targets.listIterator(); ite.hasNext();) {
					tg = (Target) ite.next();

					boolean atLeastOneTargetVisible = false;

					try {
						if (!primaryAdmin && main.isAclsOn()) {
							IUser user = (IUser) session.getAttribute(SESSION_SMUSER);
							if (DEBUG) {
								System.out.println("User = " + user);
								System.out.println("User name = " + user.getName());
								System.out.println("tg = " + tg);
								System.out.println("tg.getId = " + tg.getId());
							}

							if (!main.getAclMgr().subReadPermissionExists(user, tg,
							        new ArrayList(), true, main.getUsersInLDAP())) {
								// update the session to indicate no read permission exists
								// no read also implies no write
								if (!atLeastOneTargetVisible && !ite.hasNext()) {
									// if this is the last target, and none of them are visible, there's
									// no point in the subsequent ldap query to lookup the policies
									pageContext.getSession()
									        .setAttribute("aclread", "on");
								}
								noReadAndWrite = true;
							}

							if (!main.getAclMgr().subWritePermissionExists(user, tg, new ArrayList(), true, main.getUsersInLDAP())) {
								// update the session to indicate no write permission exists
								if (DEBUG) {
									System.out.println("User doesn't have Subscription write permission on target");
								}
								pageContext.getSession()
								        .removeAttribute("aclwrite");
							}
							if (noReadAndWrite) {
								noReadAndWrite = false;
								continue;
							}
						}
					} catch (AclException aclexc) {
                        if (DEBUG) {
                            aclexc.printStackTrace();
                        }
						WebAppUtils.saveTagException(pageContext, aclexc.getRootCause());
					} catch (AclStorageException aclexc) {
                        if (DEBUG) {
                            aclexc.printStackTrace();
                        }
						WebAppUtils.saveTagException(pageContext, aclexc.getRootCause());
					} catch (NameNotFoundException ne) {
                        if (DEBUG) {
                            ne.printStackTrace();
                        }
						WebAppUtils.saveTagException(pageContext, new SubKnownException(SUB_LDAP_TARGETDELETED, tg.getId()));
					} catch (NamingException nexc) {
						try {
							LDAPUtils.classifyLDAPException(nexc, null, true);
						} catch (SystemException sysexc) {
                            if (DEBUG) {
                                sysexc.printStackTrace();
                            }
							WebAppUtils.saveTagException(pageContext, sysexc);
						}
					}

					/* Make a list of the target dns that have been selected
					* within the search filter.  This is because we are only interested
					* in these for multiple select.
					*/
					if (TYPE_ALL.equals(tg.getType())) {
						attributeName = LDAPVarsMap.get("TARGETTYPE");
						attributeValue = TYPE_ALL;
					} else if(IWebAppConstants.INVALID_NONEXIST.equals(tg.getType())){						session.setAttribute(IWebAppConstants.INVALID_NONEXIST, IWebAppConstants.INVALID_NONEXIST);						isOrphanPolicy = true;						if (LDAPVars.ACTIVE_DIRECTORY.equals(LDAPVarsMap.get("DIRECTORY_TYPE")) || LDAPVars.ADAM.equals(LDAPVarsMap.get("DIRECTORY_TYPE"))) {							searchFilter=new StringBuffer("(" + LDAPVarsMap.get("AD_UID") + "=" + LDAPName.escapeComponentValue(tg.getID()) + ")");						}else{																					searchFilter=new StringBuffer("(" + LDAPVarsMap.get("TARGETDN") + "=" + tg.getID() + ")");						}						return searchFilter;											} else {
                        if (!main.getUsersInLDAP()) {
							if (TYPE_USER.equals(tg.getType())) {
								attributeName = LDAPVarsMap.get("TARGET_TX_USER");
							} else {
								if (TYPE_USERGROUP.equals(tg.getType())) {
									attributeName = LDAPVarsMap.get("TARGET_TX_GROUP");
								} else if(TYPE_SITE.equals(tg.getType())) {
									tgstring = tgstring + "(&(" + LDAPVarsMap.get("TARGETTYPE") + "=" + TYPE_SITE + ")";
									attributeName = LDAPVarsMap.get("SUBSCRIPTION_NAME");
								} else {
									/* This is a machine or a machine group
									* so there should be a search on the targetdn
									*/
									attributeName = LDAPVarsMap.get("TARGETDN");
								}
							}
						} else { //user in LDAP
							if(TYPE_SITE.equals(tg.getType())) {
								tgstring = tgstring + "(&(" + LDAPVarsMap.get("TARGETTYPE") + "=" + TYPE_SITE + ")";
								attributeName = LDAPVarsMap.get("SUBSCRIPTION_NAME");
							} else {
								attributeName = LDAPVarsMap.get("TARGETDN");
							}
							
						}

						attributeValue = LDAPSearchFilter.escapeComponentValue(tg.getId());
					}

					tgstring = tgstring + "(" + attributeName + "=" + attributeValue + ")";
					if(TYPE_SITE.equals(tg.getType())) {
						tgstring = tgstring + ")";
					}
				}

				searchFilter.append(tgstring);
				searchFilter.append(")");
			}
		}
		return searchFilter;
	}

	/**
	 * Retrieves the query string that should be used to obtain the subscriptions that a particular user, machine, or group will recieve.
	 *
	 * @param targetID       This is of the target ID (Distinguished Name).
	 * @param targetType     This is the target Type of the target.
	 * @param subConfig      Embodies all of the information needed from the configurations stored in LDAP
	 * @param userDirectory  Used if we are to obtain users from the transmitter instead of LDAP
	 * @param ldapCtx
	 * @param childContainer REMIND
	 * @param usersInLdap    REMIND
	 * @return REMIND
	 * @throws LDAPException      REMIND
	 * @throws LDAPLocalException REMIND
	 * @throws NamingException    REMIND
	 */
	TargetResHelper getGroupsAndContainers(String targetID,
	                                       String targetType,
	                                       IProperty subConfig,
	                                       IUserDirectory userDirectory,
	                                       ILDAPDataSourceContext ldapCtx,
	                                       String childContainer,
	                                       boolean usersInLdap)
	        throws LDAPException,
	        LDAPLocalException,
	        NamingException {

		TargetResHelper targetDNs = new TargetResHelper(usersInLdap, LDAPVarsMap);

		// get all DNs for the user and machineName
		String[] memberOfattr = new String[1];
		memberOfattr[0] = LDAPVarsMap.get("MEMBEROF");

		String[] resultDNs = null;

		if (usersInLdap || (!usersInLdap && (TYPE_MACHINEGROUP.equals(targetType) || TYPE_COLLECTION.equals(targetType)))) {
			// A search for the group ID should only be done in LDAP if
			// users are not sourced from the transmitter
			resultDNs = new String[1];
			resultDNs[0] = targetID;
			if (DEBUG) {
				System.out.println("GetPkgsFromTargetTag: groupID = " + targetID);
			}
		}

		long starttime = System.currentTimeMillis();
		LDAPEnv ldapEnv = main.getLDAPEnv();

		// Resolve group membership
		//get the domain only for the child container
		//String domainDN = LDAPConnUtils.getDomainDNFromDN(ldapCtx.getSubConn(), childContainer);

		//Convert into the DNS format
		//String subDomain = LDAPConnUtils.getDomainFromDN(ldapCtx.getSubConn(), domainDN);

		Set groupDNs = ldapEnv.getGroupMembership(resultDNs, ldapCtx, ldapCtx.getConnectionCache());
		// We default to USERGROUP for all groups from LDAP
		for (Iterator ite = groupDNs.iterator(); ite.hasNext();) {
			String dn = (String) ite.next();
			targetDNs.add(dn, ISubscriptionConstants.TYPE_USERGROUP);
		}

		DEBUGTIME(System.currentTimeMillis() - starttime, "Finish resolving group membership");

		if (usersInLdap || (!usersInLdap && (TYPE_MACHINEGROUP.equals(targetType) || TYPE_COLLECTION.equals(targetType)))) {
			Map containersMap;
			HashSet iSet = new HashSet(targetDNs.getTargetsSet());
			iSet.add(targetID);
			containersMap = ldapEnv.getEnclosingContainers(subConn, iSet);
			if (DEBUG) {
				System.out.println("containerersMap.size() " + containersMap.size());
			}

			targetDNs.addAll(containersMap);
		}

		if (DEBUG) {
			System.out.println("GetPkgsFromTargetsTag: group resolution results " + targetDNs.getTargetsMap().size());
		}

		// add all user/group targets that point to user/groups
		// as seen from the Transmitter, i.e. via IUserDirectory
		if ((!usersInLdap) && (userDirectory != null)) {
			if (TYPE_USER.equals(targetType)) {
				DEBUGTIME(starttime, "Start search for user from Tx " + targetID);
				targetDNs.add(targetID, TYPE_USER);

				String[] groups = userDirectory.listGroups(LDAPSearchFilter.escapeComponentValue(targetID));

				if (groups != null) {
					if (groups.length > 0) {
						for (int i = 0; i != groups.length; i++) {
							targetDNs.add(groups[i], TYPE_USERGROUP);
						}
					}
				}
			} else if (TYPE_USERGROUP.equals(targetType)) {
				targetDNs.add(targetID, TYPE_USERGROUP);
			}
		}

		if (DEBUG) {
			System.out.println("GetPkgFromTargetsTag: number of groups and containers for search = " + targetDNs.getTargetsMap().size());
		}

		return targetDNs;
	}

	public HashMap getAllPakages(StringBuffer searchFilter,
	                             SubscriptionMain main,
	                             boolean browsingLDAP,
	                             List targets, LDAPConnection subConn, LDAPConnection browseConn) throws JspException {
		HashMap resultsTbl = new HashMap(50);
		NamingEnumeration nenum;
		SearchResult sr;
		Attributes srAttrs;
		Target tgdn;
		String url;
		String state1;
		Channel chn;
		TargetChannelMap testmap;
		Channel testch;
		String base = main.getSubBase();
		HashMap tgMapTbl = new HashMap(50);				HashSet<Integer> usedPriorities =new HashSet<Integer>();		
		Target tgselect = (Target) targets.get(0);/* Used to store the channels as they are gotten from
					    * the search results
					    */


		boolean multiMode = false;

		if (pageContext.getSession().getAttribute(SESSION_MULTITGBOOL) != null) {
			multiMode = true;
		}

		if (pageContext.getSession().getAttribute("aclread") == null) {
			try {
				if (DEBUG) {
					System.out.println("GetPkgsFromTargetsTag: final Search Filter = " + searchFilter);
					System.out.println("searchRecs.base= " + base);
				}

				nenum = subConn.search(searchFilter.toString(), LDAPUtils.getLDAPVarArr("channelAttributeAsArray", main.getDirType()), base, LDAPConstants.LDAP_SCOPE_SUBTREE);

				if (nenum != null) {
					while (nenum.hasMoreElements()) {
						sr = (SearchResult) nenum.next();
						srAttrs = sr.getAttributes();

						if (srAttrs != null) {
							tgdn = getTarget(browseConn, srAttrs, browsingLDAP);
							Hashtable chnValueMaps = LDAPSubscription.loadChannelAttrs(srAttrs, LDAPVarsMap);
							Hashtable chnInitMap = (Hashtable) chnValueMaps.get(LDAPVarsMap.get("CHANNEL"));

							for (Enumeration ite = chnInitMap.keys(); ite.hasMoreElements();) {
								/* This obtains the url with an initial state set
								 * We only iterate through the initial channels url because an initial
								 * state must always be set for the channel.
								 */
								url = (String) ite.nextElement();
								state1 = (String) chnInitMap.get(url);

								if (LDAPSubscription.isNone(state1)) {
									continue;
								}

								chn = LDAPSubscription.createContent(url, state1, chnValueMaps, LDAPVarsMap, main.getTenantName(), main.getChannel());

								// Create a target Channel Map forordering.
								String resultChildContainer;
								String subName = LDAPName.unescapeJNDISearchResultName(sr.getName());
								// Obtain the child container of the result (subscription
								// base not included in the DN)
								Name subDN = subConn.getParser()
								        .parse(subName);
								subDN.remove(subDN.size() - 1);

								resultChildContainer = subDN.toString();
								TargetChannelMap tgmap;


								tgmap = new TargetChannelMap(tgdn, resultChildContainer);

								// In order to distinguish whether a package has been directly assigned to
								// a target, a comparison is made between the target that was selected (tgselect)
								// and the target dn (tgdn).  This is then set to true or false for the
								// target channel map using setIsSelectedTarget
								if (!multiMode) {
									if (TYPE_ALL.equalsIgnoreCase(tgdn.getType())) {
										if (TYPE_ALL.equalsIgnoreCase(tgselect.getType())) {
											tgmap.setIsSelectedTarget("true");
										}
									} else {
										if (!browsingLDAP && (TYPE_USER.equals(tgdn.getType())
										        || TYPE_USERGROUP.equals(tgdn.getType()))) {
											// If sourcing user from Transmitter, and target is
											// a user/user group, we use normal case insensitive string compare
											if (tgdn.getId().equalsIgnoreCase(tgselect.getId())) {
												tgmap.setIsSelectedTarget("true");
											}
										} else {
											if(TYPE_SITE.equals(tgdn.getType())) {
												if (tgdn.getType().equalsIgnoreCase(tgselect.getType())) {
													tgmap.setIsSelectedTarget("true");
												}
											} else {
												String targetType = tgdn.getType();
												Name selectDN = subConn.getParser()
												        .parse(tgselect.getId());
												Name targetDN = subConn.getParser()
												        .parse(tgdn.getId());
												boolean targettable = targetDN.equals(selectDN);

												if (!TYPE_DOMAIN.equals(targetType)) {
													// Reconstruct the result DN to include the Subscription base.
													if (resultChildContainer.length() > 0) {
														subDN = subConn.getParser()
														        .parse(resultChildContainer + "," + base);
													} else {
														subDN = subConn.getParser()
														        .parse(base);
													}
													String childContainer = main.getSubBaseWithNamespace(ldapCtx);
													Name subContainerDN = subConn.getParser()
													        .parse(childContainer);
													targettable = targettable && subDN.equals(subContainerDN);
												}

												if (targettable) {
													tgmap.setIsSelectedTarget("true");
												}
											}
										}
									}
								}

								tgmap.addChannel(chn);

								if (!multiMode) {
									/* Each channel should be unique in the table so that even common channels
									 * are displayed.  This is done for single select mode so that the user
									 * can see the values for channel attributes (state, secState,etc) for
									 * each target SEPARATELY.
									 */
									tgMapTbl.put(chn.getUrl() + tgdn.getID() + tgdn.getName(), tgmap);
								} else {
									/* With multiple select mode, we want to be able to see the INTERSECTION
									* of the channels for all selected targets. Therefore, we do not need
									* to make each target channel map unique.  In fact we want there to be
									* intersections so that we can find the common attributes among all of the
									* channels
									*/
									testmap = (TargetChannelMap) tgMapTbl.get(chn.getUrl());
									usedPriorities.add(chn.getOrder());

									if (testmap == null) {
										/* This is a channel instance has not occurred yet, therefore
										 * we add it to the target channel map hashtable without
										 * comparison.
										 */
										tgmap.getChannel().incIntersectCount();
										tgMapTbl.put(chn.getUrl().toString(), tgmap);
									} else {
										/* Another channel with the same package URL was found on another selected
										 * target.  Therefore, we increment the intersection count on the channel
										 * stored in the targetChannelMap.  This is used later to help determine
										 * which channels should be in the result table for sorting.
										 *
										 */
										/* This returns the channel which tracks the inconsistencies
										 */
										testch = testmap.getChannel();

										/* Compare the qualities of the channel to see if they are the same.
										 * If not, then set the value to be inconsistent.
										 */
										testmap.addChannel(chn); //This will update the inconsistency information

										testch.incIntersectCount(); /* increments the reference count for this channel.
									    * This is used determine the result set for the
									    * multiple mode.
									    */

										tgMapTbl.put(chn.getUrl(), testmap);
									}
								}
							}

							//end of the loop to go through the packages in a subscription
						}

						// end iff for the search result attributes
					}

					// end of while loop to go through the entries returned from the search.
				}
			} catch (NamingException ne) {
				WebAppUtils.saveTagException(pageContext, ne);
			} catch (SystemException se) {
				WebAppUtils.saveTagException(pageContext, se);
			}

			/* Figure out which channels should be included in the returned set.
			 * This is determined by looking at the count on the channels
			 */
			if (multiMode) {
				Channel chntest;
				TargetChannelMap tgmaptest;
				if (!tgMapTbl.isEmpty()) {
					Iterator ite = tgMapTbl.values().iterator();

					while (ite.hasNext()) {
						tgmaptest = (TargetChannelMap) ite.next();
						chntest = tgmaptest.getChannel();

						if (chntest.getIntersectCount() == targets.size()) {
							resultsTbl.put(chntest.getUrl(), tgmaptest);														usedPriorities.remove(chntest.getOrder());
						}
					}
				}				pageContext.getSession().setAttribute("usedPriorities",usedPriorities);								
			} else {
				resultsTbl = tgMapTbl;
			}

			if (DEBUG) {
				System.out.println("GetPkgsFromTargetsTag: resultsTbl size = " + resultsTbl.size());
			}
		}
		return resultsTbl;
	}

	// DEBUG message to compute the time (in milliseconds) required for
	// each LDAP query and for the complete processing of an update request.
	static void DEBUGTIME(long starttime,
	                      String msg) {
		if (DEBUG) {
			long now = System.currentTimeMillis() - starttime;
			System.out.println("DEBUGTIME: " + msg + " at : " + now + "(ms)");
		}
	}
}
