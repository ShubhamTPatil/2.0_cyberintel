// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionplugin;

import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.intf.ICommonErrorConstants;
import com.marimba.apps.subscription.sbrp.SBRPSiteHttpConnector;
import com.marimba.intf.admin.IUserDirectory;
import com.marimba.intf.castanet.IFile;
import com.marimba.intf.castanet.IIndex;
import com.marimba.intf.castanet.IWorkspace;
import com.marimba.intf.certificates.ICertProvider;
import com.marimba.intf.plugin.IRequest;
import com.marimba.intf.ssl.ISSLProvider;
import com.marimba.intf.transmitter.ITTransmitter;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IProperty;
import com.marimba.tools.config.ConfigUtil;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPConstants;
import com.marimba.tools.ldap.LDAPSearchFilter;
import com.marimba.castanet.http.HTTPManager;
import com.marimba.castanet.ssl.HTTPSManager;
import com.marimba.castanet.http.JavaHTTPSManager;
import com.marimba.tools.net.HTTPConfig;
import com.marimba.tools.util.Props;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.transmitter.ITWorkspace;

import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.*;

/**
 * Subscription data source using LDAP.  This is instantiated by
 * <i>com.marimba.apps.subscriptionplugin.SubscriptionPlugin</i>. It is used to handle the data
 * requests from the client endpoints.
 *
 * @author Simon Wynn
 * @author Damodar Hegde
 * @author Vidya Viswanathan
 * @author Angela Saval
 * @author Theen-Theen Tan
 * @author Narayanan A R

 * @version $Revision$, $Date$
 */
class LDAPDataSource
        implements ISubscriptionDataSource,
                   ISubscriptionConstants,
                   LogConstants,
                   IPluginDebug,
                   IErrorConstants,
                   LDAPConstants,
                   ICommonErrorConstants,
                   IPluginConstants {

    private ISubsPluginContext ctx;

    /**
     * This is used if users and user groups are sourced from the transmitter.
     */
    private IUserDirectory userDirectory;

    /**
     * This boolean is used to indicate that the initialization of the config,site, and domain
     * completed succesfully
     */
    private boolean initSucceeded = false;

	// following 4 are used for maintaining a cache of sites to reduce the load for ftp site for validating against XML DTDs
	private long cacheRefreshInterval;
	private long lastUpdatedTime = -1;
	private Map<String, Map<String, String>> networkDetailsMap;
	private Map<String, Map<String, String>> siteDetailsMap;

    /**
     * Construct the data source.  The constructor takes care of initializing the ldap configuration used for
     * credentials, the subscription configuration used for client queries, and the connection cache which keeps
     * track of connectiosn that have been been made to domains. This constructor and initialization
     * only is called when the plugin has been restarted. this occurs when the transmitter is restarted or if
     * the plugin has been been published.
     *
     * @param ctx Subscription Plugin Context
     */
    public LDAPDataSource(ISubsPluginContext ctx, long cacheRefreshInterval) throws SystemException {
        this.cacheRefreshInterval = cacheRefreshInterval;
		this.ctx = ctx;

        new PublishedProperties(ctx);

        userDirectory = (IUserDirectory) ctx.getPluginContext().getFeature("authenticator");

        //This is used to get the credentials for making connections.  For iPlanet, it is
        //possible to supply an ldapservers.txt file that contains credentials for each
        //ldap server.  However, with active directory, we use the SAME credentials for
        //all of the ldap servers, and auto discover which ldap server to use for the
        //domain sent up by the client endpoint.
        Props ldapCfg = ctx.getPublishedPluginProps().readLDAPServerProps();
        if (ldapCfg == null) {
            if(HIGH_PRIORITY_INFO){
                System.out.println("Plugin Debug - LDAPDataSource: ldapCfg is null");
            }
            return;
        }

        new PluginLDAPEnvironment(ctx, ldapCfg);

        //This is a utility class that has various LDAP related methods used by
        // the plugin.
        new PluginLDAPUtils(ctx);

        new SubsMrbaConfigManager(ctx);

        //If the configuration failed, there is no need to initialize LDAP
        if(!ctx.getSubsMrbaConfigMgr().isConfigUsable()) {
            if(HIGH_PRIORITY_INFO){
                System.out.println("LDAPDataSource: mrbaconfig is not usable");
            }
            return;
        }

        initSucceeded = true;
    }

    /**
     * Get data from the data source. This method should be thread safe as the plugin must be able to process many
     * update requests simultaneously Returns the insetupdex if successfull , returns null otherwise
     *
     * @param request Request object containing the details of the endpoint
     *
     * @return Modified index with the new policy
     */
    public IIndex getData(IRequest request) {

        if (!initSucceeded) {
            ctx.log(LOG_ERROR_LDAP_INIT_FAILED, LOG_MAJOR);

            return null;
        }

        ctx.getSubsMrbaConfigMgr().readConfig();

        if (ctx.getSubsMrbaConfigMgr().isPluginDisabled()) {
            ctx.log(LOG_PLUGIN_DISABLED, LOG_AUDIT);

            return null;
        }

		String srcHostAddress = request.getRemoteAddress().getHostAddress();
		
		// Site Based Policy
		String siteName = null;
		
		if(isSiteBasedPolicyEnabled()) {
			if (DETAILED_INFO) {
                ctx.logToConsole("Site Based Policy Enabled ");
            }
			try {
				long currentTime = System.currentTimeMillis();
				long cachedDuration = currentTime - lastUpdatedTime;

				if (lastUpdatedTime == -1 || (cachedDuration > cacheRefreshInterval * 60 * 1000)) {
					ctx.logToConsole ("Fetching site info using ?repinfo and parsing XML file");
					siteName = findEndpointSite(request);
				} else {
					// get the site name for the endpoint from the cache
					ctx.logToConsole ("Fetching site info from cache");
					siteName = getSiteNameForIP(srcHostAddress);
				}
			} catch(Exception ec) {
				if (DETAILED_INFO) {
					ec.printStackTrace();
				}
			}
		}
		// set profile attriubte for custom scanner details and security info details
		/*
        request.setProfileAttribute(NETMASK_PROFILE_ATTR);
		request.setProfileAttribute(NETMASK_PROFILE_ATTR);
		request.setProfileAttribute(NETMASK_PROFILE_ATTR);
		request.setProfileAttribute(NETMASK_PROFILE_ATTR);
		request.setProfileAttribute(NETMASK_PROFILE_ATTR);
		request.setProfileAttribute(NETMASK_PROFILE_ATTR);
		*/
		LDAPEnv env = ctx.getPluginLDAPEnv().getLDAPEnv();
		
        TargetResHelper targetDNs = new TargetResHelper(ctx.getSubsMrbaConfigMgr().isUsersInLdap(), env.getVariables());

        PluginTasks taskDurationLogger = new PluginTasks();
        taskDurationLogger.taskStarted(PluginTasks.ALL_TASKS);

        String searchStr = null;

        fetchAuthenticator();

        try {
            UserMachineDNWrapper userMachineDN = new UserMachineDNWrapper();
            if(identifyTargetsToFetchPolicy(request,
                                            srcHostAddress,
                                            targetDNs,
                                            userMachineDN,
                                            taskDurationLogger) == false) {
                return null;
            }

            // add all user/group targets that point to user/groups as seen from the Transmitter, i.e. via
            // IUserDirectory
            addCurrentUserGroups(request, targetDNs);
            // End sourcing users/usergroups from Tx

            if (DETAILED_INFO) {
                ctx.logToConsole("TARGETALL added to searchString " + searchStr);
            }

            // Fetch policy for the populated target DNs and create nodes
            SubscriptionPolicy policy = new SubscriptionPolicy(ctx, request, taskDurationLogger);
            policy.readSubscriptionPolicies(targetDNs, siteName);


			taskDurationLogger.taskStopped(PluginTasks.ALL_TASKS);
            logRequestProcessedMessage(request, userMachineDN.userAndMachineDNs, taskDurationLogger, srcHostAddress);
            taskDurationLogger.removeTaskInfoObjs();

            // if we dont have read access fail
            if (policy.isNoReadAccess()){
                ctx.log(LOG_PLUGIN_NOREADACCESS, LOG_AUDIT);
                return null;
            }
            return request.getChannelIndex();
        } catch (NamingException ne) {
            ctx.log(LOG_ERROR_LDAP_NAMING, LOG_MAJOR, srcHostAddress, null, ne);
        } catch (IOException ioe) {
            ctx.log(LOG_ERROR_LDAP_IOERROR, LOG_MAJOR, srcHostAddress, null, ioe);
        } catch (RuntimeException rne) {
            ctx.log(LOG_ERROR_LDAP_RUNTIME, LOG_MAJOR, srcHostAddress, null, rne);
		} catch (Exception ex) {
			ctx.log(LOG_ERROR_LDAP_RUNTIME, LOG_MAJOR, srcHostAddress, null, ex);
        } catch (Throwable te) {
            ctx.log(LOG_ERROR_LDAP_RUNTIME, LOG_MAJOR, srcHostAddress, null, te);
            if (te instanceof ThreadDeath) {
                throw (ThreadDeath) te;
            }
        }

        return null;
    }

	private String getSiteNameForIP(String hostAddress) {
		String netIp = hostAddress.substring(0, hostAddress.lastIndexOf ("."));

		Set<String> keySet = networkDetailsMap.keySet();
		for (String key : keySet) {
			System.out.println("key : " + key + ", netIp : " + netIp);

			if (key.startsWith(netIp)) {
				Map<String, String> siteMap = networkDetailsMap.get(key);
				return siteMap.get("sitename:");
			}
		}

		return null;
	}

	private boolean isSiteBasedPolicyEnabled() {
    	try {
    		Props subConfig = ctx.getSubsMrbaConfigMgr().getSubConfig();
    		return Boolean.valueOf(subConfig.getProperty(LDAPConstants.CONFIG_SITEBASEDPOLICY_ENABLED));
    	} catch(Exception ec) {
    		if (DETAILED_INFO) {
				System.out.println("Failed to load site based policy enabled property from Subscription Config object: " + ec.getMessage());
			}
    	}
    	return false;
    }

    private String findEndpointSite(IRequest request) {
    	
    	String netMask = (String) request.getProfileAttribute(NETMASK_PROFILE_ATTR);
		IConfig tunerConfig = (IConfig) ctx.getPluginContext().getFeature("config");
		HTTPConfig httpConfig = new HTTPConfig(new ConfigUtil((IConfig) ctx.getPluginContext().getFeature("config")));
		// ToDo for selva: please retrieve the channel and tenantName from appropriate place and set it below
//		httpConfig.setChannel();
//		httpConfig.setTenantName();

		HTTPManager httpMgr = null;
		String masterURL = null;

		URL url = ctx.getPluginContext().getMasterURL();
		if (url != null) {
			masterURL = url.toString();
		}

		Properties txProps = null;
		String secured = null;
		FileInputStream fis = null;
		try {
			String rootDir = ctx.getPluginContext().getRootDirectory();
			String txPropsFilePath = rootDir + "\\" + "properties.txt";
			if (DETAILED_INFO) {
				System.out.println("Transmitter Root Directory : " + rootDir);
			}
			txProps = new Properties();
			fis = new FileInputStream(new File (txPropsFilePath));
			txProps.load(fis);
			secured = txProps.getProperty("transmitter.http.secure");
		} catch (Exception ioEx) {
			if (DETAILED_INFO) {
				System.out.println("Exception while loading transmitter properties : " + ioEx.getMessage());
				ioEx.printStackTrace();
			}
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (Exception ex) {
				if (DETAILED_INFO) {
					System.out.println("Problem on closing the Tx properties.txt file: " + ex.getMessage());
				}
			}
		}

		if(null == masterURL) {
			if (DETAILED_INFO) {
				System.out.println("Current plugin is master");
			}
			ITWorkspace txWS = (ITWorkspace) ctx.getPluginContext().getFeature("tx-workspace");
			ITTransmitter itTx = txWS.getDefaultTransmitter();
			IConfig txConfig = itTx.getConfig();

			String txIP = null;
			try {
				txIP = InetAddress.getLocalHost().getHostAddress();

				if (DETAILED_INFO) {
					System.out.println("Master Transmitter IP : " + txIP);
				}
			} catch (Exception ex) {
				if (DETAILED_INFO) {
					ex.printStackTrace();
				}
			}

			String port = txConfig.getProperty("port");
			String protocol = "http://";
			if (secured == null) {
				secured = txConfig.getProperty("transmitter.http.secure");
			}

			if (DETAILED_INFO) {
				System.out.println("secured : " + secured);
			}

			if ("true".equalsIgnoreCase(secured)) {
				protocol = "https://";
			} else {
				protocol = "http://";
			}
			masterURL = protocol + txIP + ":" + port + "/";
		}

		if (DETAILED_INFO) {
			System.out.println("masterURL : " + masterURL);
		}

		ISSLProvider ssl = null;
		ICertProvider cert = null;
		try {
			ssl = (ISSLProvider) ctx.getPluginContext().getFeature("ssl");
			cert = (ICertProvider) ctx.getPluginContext().getFeature("certificates");

			if (tunerConfig != null && cert != null) {
				httpMgr = new JavaHTTPSManager(httpConfig, tunerConfig, cert);
			} else {
				httpMgr = new HTTPManager(httpConfig);
			}
		} catch(Throwable e) {
			try {
				if(ssl != null && cert != null) {
					httpMgr = new HTTPSManager(httpConfig, cert, ssl);
				} else {
					httpMgr = new HTTPManager(httpConfig);
				}
			} catch (UnsatisfiedLinkError e1) {
				System.out.println("Exception occurred while connecting with ssl details : " + e1.getMessage());
                httpMgr = new HTTPManager(httpConfig);
			} catch (Exception ex) {
				System.out.println("Exception occurred while connecting with ssl details : " + ex.getMessage());
				httpMgr = new HTTPManager(httpConfig);
				if (DETAILED_INFO) {
					ex.printStackTrace();
				}
			}
		} 

		String siteName = null;
		try {
			if (httpMgr != null) {
				SBRPSiteHttpConnector connector = new SBRPSiteHttpConnector(tunerConfig, httpMgr);
				if (DETAILED_INFO) {
					System.out.println("Transmitter to be connected to get site info : " + masterURL);
				}
				connector.refreshSitesDetails(masterURL);

				List<String> sitesList = connector.getSitesList();
				if (DETAILED_INFO) {
					for (String site : sitesList) {
						ctx.logToConsole("TARGETTYPE Site : " + site);
					}
				}

				if (netMask != null) {
					siteName = connector.getSiteNameForIP(request.getRemoteAddress(), netMask);
					lastUpdatedTime = connector.getLastUpdatedTime();
					networkDetailsMap = connector.getNetworkDetailsMap();				}
				if(INFO) {
					ctx.logToConsole("Sending reply; caching not enabled");
				}
			}
		} catch (Exception ex) {
			System.out.println("Problem on retrieving site info : " + ex.getMessage());

			if (DETAILED_INFO) {
				ex.printStackTrace();
			}
		}

		return siteName;
    }

	/**
     * Need to relace string array with domain objects to represent user and machine DNs.
     */
    private static class UserMachineDNWrapper {
        private String[][] userAndMachineDNs = null;
    }

    private boolean identifyTargetsToFetchPolicy(IRequest request,
                                                 String srcHostAddress,
                                                 TargetResHelper targetDNs,
                                                 UserMachineDNWrapper userMachineDN,
                                                 TaskDurationLogger taskDurationLogger) {
        // Identify targets DNs. Either for provision policy or for resolve user & machine DN
        String policyGroups = isProvisionPolicy(request);
        LDAPEnv env = ctx.getPluginLDAPEnv().getLDAPEnv();
        TargetResHelper targetDNHelper = new TargetResHelper(ctx.getSubsMrbaConfigMgr().isUsersInLdap(), env.getVariables());

        if(policyGroups != null) {
            // Determine Target DNs from the provision list
            List provisionGroupList = getProvisionDNList(policyGroups);
            if(provisionGroupList == null) {
                ctx.log(LOG_ERROR_INVALID_PROVISION_DN, LOG_MAJOR, srcHostAddress, null, null);
                return false;
            }  else {
                ctx.log(LOG_PROVISION_DN_LIST, LOG_AUDIT, srcHostAddress, null, provisionGroupList);
            }

            Iterator it = provisionGroupList.iterator();
            while(it.hasNext()) {
                String targetName = (String)it.next();
                String targetType = ctx.getPluginLDAPUtils().resolveTargetType(targetName, env.getLDAPConfigProperty("vendor"));
                if(targetType != null) {
                    addTargetToRes(targetDNHelper, targetName, targetType);
                }
            }
        } else {
            // Resolve User and Machine DNs
            // Obtains the full DN of the user and the machine. This is needed in order to resolve
            // group membershsip
            try {
                userMachineDN.userAndMachineDNs = ctx.getPluginLDAPUtils().
                                                      getUserAndMachineDNs(request,
                                                                           ctx.getSubsMrbaConfigMgr().getSubConfig(),
                                                                           ctx.getSubsMrbaConfigMgr().getMrbaConfig(),
                                                                           ctx.getPluginLDAPEnv(),
                                                                           taskDurationLogger,
                                                                           ctx.getSubsMrbaConfigMgr().isUsersInLdap(),
                                                                           true);
            } catch (Exception e) {
                ctx.log(LOG_ERROR_LDAP_USERMACHINERES, LOG_AUDIT, srcHostAddress, null, null);
                ctx.log(LOG_ERROR_LDAP_DOWN, LOG_AUDIT, srcHostAddress, null, null);
                if(ERROR) {
                    ctx.logToConsole("Unable to resolve the User and Machine DN", e);
                }
                return false;
            }
            
            //Check to see if the machine name is recieved as "Unknown_host", and avoid sending policy. 
            Object profileObj = request.getProfileAttribute(MACHINENAME_PROFILE_ATTR);

            if (userMachineDN.userAndMachineDNs == null) {
                if (ctx.getSubsMrbaConfigMgr().isUsersInLdap() && ctx.getSubsMrbaConfigMgr().isRestrictAllAll()) {
                    // Error: If we are sourcing users from LDAP and there's no user or machine DN returned.
                    // If a user is sourcing users from a Transmitter and solely using user based targetting,
                    // we do not want to error as the neither the machines nore the users are expected to be in LDAP.
                    ctx.log(LOG_ERROR_LDAP_NORESULTDNS, LOG_MAJOR, srcHostAddress, null, null);

                    return false;
                }
                if(profileObj != null && "unknown_host".equalsIgnoreCase(profileObj.toString().trim())) {
                	ctx.log(LOG_ERROR_LDAP_UNKNOWNHOST_USERDN_NOT_FOUND, LOG_MAJOR, srcHostAddress, null, null);
                	return false;
                }
            } else {
                // Put user and machine DNs as targets
                if (userMachineDN.userAndMachineDNs[0] != null) {
                    for (int i = 0; i < userMachineDN.userAndMachineDNs[0].length; i++) {
						if(DETAILED_INFO) {
						    System.out.println(new Date() + " LDAPDataSource:getData() - Machine DN: " + userMachineDN.userAndMachineDNs[0][i]);
						}

                        addTargetToRes(targetDNHelper, userMachineDN.userAndMachineDNs[0][i], TYPE_MACHINE);
                    }
                } else if(profileObj != null && "unknown_host".equalsIgnoreCase(profileObj.toString().trim())) { 
                	ctx.log(LOG_ERROR_LDAP_UNKNOWNHOST_USERDN_FOUND, LOG_MAJOR, srcHostAddress, null, null);
                	return false;
                }

                if (userMachineDN.userAndMachineDNs[1] != null) {
                    for (int i = 0; i < userMachineDN.userAndMachineDNs[1].length; i++) {
						if(DETAILED_INFO) {
						    System.out.println(new Date() + " LDAPDataSource:getData() - User DN: " + userMachineDN.userAndMachineDNs[1][i]);
						}

                        addTargetToRes(targetDNHelper, userMachineDN.userAndMachineDNs[1][i], TYPE_USER);
                    }
                }
                if (HIGH_PRIORITY_INFO) {
                    ctx.logToConsole("LDAPDataSource: user and machine dns " + targetDNHelper.getTargetsMap().size());
                    ctx.logToConsole("targetDNs size: " + targetDNHelper.getTargetsMap().size());
                }
            }
        }

        // Resolve group membership
        if(!resolveGroupContainerMembership(request, targetDNs, targetDNHelper, taskDurationLogger, srcHostAddress)) {
            return false;
        }
        return true;
    }

    private String isProvisionPolicy(IRequest request) {
        Object groups = request.getProfileAttribute(ISubscriptionConstants.PROVISION_POLICY_GROUPS);
        return (groups != null && isAllowProvision()) ? groups.toString() : null;
    }

    private boolean isAllowProvision() {
        String allowProvision = ctx.getPluginLDAPEnv().getProperty(LDAPConstants.PROP_ALLOW_PROVISION);
        return Boolean.valueOf(allowProvision).booleanValue();
    }

    private void fetchAuthenticator() {
        // userDirectory needs to be checked again even though it has been
        // initialized in the init() method because if the plugin's init()
        // method is called before the Transmitter can initialize
        // the authenaticator then the userDirectory will be null.
        if (userDirectory == null) {
            synchronized (this) {
                if (userDirectory == null) {
                    userDirectory = (IUserDirectory) ctx.getPluginContext().getFeature("authenticator");
                }
            }
        }
    }

    private void logRequestProcessedMessage(IRequest request,
                                            String[][] userAndMachineDNs,
                                            PluginTasks taskDurationLogger,
                                            String src) {
        // list the machine and user dns.
        StringBuffer dns = new StringBuffer();
        String user = request.getUserID();

        StringBuffer clientInfo = new StringBuffer();
        long tunerID =  request.getTunerID();
        
        clientInfo.append(src).append(" / ").append(tunerID);

        if (userAndMachineDNs == null) {
            if (!ctx.getSubsMrbaConfigMgr().isUsersInLdap() && user != null) {
                dns.append("/").append(user);
            }
        } else {
            for (int i = 0; i < userAndMachineDNs.length; i++) {
                if (userAndMachineDNs[i] != null) {
                    for (int j = 0; j < userAndMachineDNs[i].length; j++) {
                        dns.append(userAndMachineDNs[i][j]).append("/");
                    }
                }
            }
        }

        StringBuffer timeString = new StringBuffer();
        timeString.append(dns.toString());
        timeString.append(" : ");
        timeString.append(taskDurationLogger.fetchTimeStampString());
        timeString.append(", connected to: ");
        timeString.append(ctx.getPluginLDAPEnv().getDefaultConnection().getCurrentHost());
        ctx.log(LOG_PROCESSED_REQUEST, LOG_INFO, clientInfo.toString(), null, timeString);
    }

    void addTargetToRes(TargetResHelper helper,
                        String          targetDN,
                        String          targetType) {
        boolean canAdd = true;
        if(!ctx.getSubsMrbaConfigMgr().isResolveUser() && TYPE_USER.equals(targetType)) {
            canAdd = false;
        } else if(!ctx.getSubsMrbaConfigMgr().isResolveMachine() && TYPE_MACHINE.equals(targetType)) {
            canAdd = false;
        }

        if(canAdd) {
            helper.add(ctx.getPluginLDAPUtils().getCanonicalName(targetDN), targetType);
        }
    }

    /**
     * Close the LDAP connection
     */
    public void close() {
        if(LDAPConnection.performanceCollector != null) {
            Iterator it = LDAPConnection.performanceCollector.getPerformanceData();
            while (it.hasNext()) {
                    ctx.logToConsole((String)it.next());
            }
        }

        ctx.getPluginLDAPEnv().cleanup();
    }

    private ArrayList getProvisionDNList(String listString) {
        ArrayList result = null;
        if(listString == null || listString.trim().length()==0) {
            return result;
        }

        //use the string tokenizer to parse the DN strings.
        StringTokenizer st = new StringTokenizer(listString, "\"");
        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            if(ctx.getPluginLDAPEnv().getDefaultConnection() != null) {
                //Validate if it is in the DN format.  If not in DN format, ignore.
                try {
                    if(ctx.getPluginLDAPEnv().getDefaultConnection().getParser().isDN(token)) {
                        if(result == null) {
                            result = new ArrayList();
                        }
                        result.add(token);
                    }
                } catch (NamingException e) {
                    if(WARNING) {
                        ctx.logToConsole("Unable to get parser from the connection.", e);
                    }
                }
            }
        }

        return result;
    }

    private boolean resolveGroupContainerMembership(IRequest request,
                                                    TargetResHelper targetDNs,
                                                    TargetResHelper targetDNHelper,
                                                    TaskDurationLogger taskDurationLogger,
                                                    String src) {
        // resolve group membership
        try {

            taskDurationLogger.taskStarted(PluginTasks.RESOLVE_GROUPS);
            Set groupDNs = new HashSet(10);
            List invalidDNS = new ArrayList();
            boolean resolveMachineDN = false;

            // Iterate the user and machine dns to get invalid DNs
            for (Iterator ite = targetDNHelper.getTargetsSet().iterator(); ite.hasNext();) {
                String targetDN = (String) ite.next();
                try {
                    Set groupDNsSet = ctx.getPluginLDAPEnv().
                                          getLDAPEnv().
                                          getGroupMembership(new String[]{targetDN},
                                                             ctx.getPluginLDAPEnv(),
                                                             ctx.getPluginLDAPEnv().getLDAPConnectionCache());
                    groupDNs.addAll(groupDNsSet);

                    targetDNs.add(targetDN, (String) targetDNHelper.getTargetsMap().get(targetDN));

                } catch (NameNotFoundException nfe) {
                    invalidDNS.add(targetDN);
                }
            }

            for(Iterator ite = invalidDNS.iterator(); ite.hasNext();) {
                String targetDN = (String) ite.next();
                if(DETAILED_INFO) {
                    System.out.println("Target DN is not found in LDAP: Remove : "+targetDN);
                }
                Map helperSet = targetDNHelper.getTargetsMap();
                String targetType = (String) helperSet.get(targetDN);
                if (TYPE_MACHINE.equals(targetType)) {
                    resolveMachineDN = true;
                    ctx.log(LOG_ERROR_LDAP_MACHINE_GROUPMEMBERSHIP, LOG_MAJOR, src, targetDN, null);
                } else {
                    ctx.log(LOG_ERROR_LDAP_USER_GROUPMEMBERSHIP, LOG_MAJOR, src, targetDN, null);
                }
            }

            if (resolveMachineDN) {
                Map targetSet = resolveMachineDNs(request, taskDurationLogger, src);
                if( (targetSet != null) && (targetSet.size() == 0) ) {
                    Object profileObj = request.getProfileAttribute(MACHINENAME_PROFILE_ATTR);
                    ctx.log(LOG_ERROR_LDAP_MACHINE_GROUPMEMBERSHIP, LOG_MAJOR, src, (String)profileObj, null);
                }
                groupDNs.addAll(targetSet.keySet());
                Set groupDNsSet = ctx.getPluginLDAPEnv().
                                      getLDAPEnv().
                                      getGroupMembership((String[]) targetSet.keySet().toArray(new String[0]),
                                                         ctx.getPluginLDAPEnv(),
                                                         ctx.getPluginLDAPEnv().getLDAPConnectionCache());
                groupDNs.addAll(groupDNsSet);
            }

            taskDurationLogger.taskStopped(PluginTasks.RESOLVE_GROUPS);

            for (Iterator ite = groupDNs.iterator(); ite.hasNext();) {
                if (ctx.getSubsMrbaConfigMgr().isUsersInLdap()) {
                    addTargetToRes(targetDNs, (String) ite.next(), ISubscriptionConstants.TYPE_USERGROUP);
                } else {
                    addTargetToRes(targetDNs, (String) ite.next(), ISubscriptionConstants.TYPE_MACHINEGROUP);
                }
            }
            if (HIGH_PRIORITY_INFO) {
                ctx.logToConsole("LDAPDataSource: user and machine dns " + groupDNs.size());
                ctx.logToConsole("targetDNs size: " + targetDNs.getTargetsMap().size());
            }
        } catch (Exception ex) {
            //There was a problem with obtaining the connection for group membership, and the listing
            ctx.log(LOG_ERROR_LDAP_GROUPMEMBERSHIP, LOG_MAJOR, src, null, ex);
            return false;
        }

        // Go through the resulting machine and userdns and add the containers to which they belong
        try {

            Map containersMap = null;

            taskDurationLogger.taskStarted(PluginTasks.RESOLVE_CONTAINERS);
            containersMap = ctx.getPluginLDAPEnv().getLDAPEnv().
                    getEnclosingContainers(ctx.getPluginLDAPEnv().getDefaultConnection(), targetDNs.getTargetsSet());
            taskDurationLogger.taskStopped(PluginTasks.RESOLVE_CONTAINERS);

            targetDNs.addAll(containersMap);

            if (HIGH_PRIORITY_INFO) {
                ctx.logToConsole("LDAPDataSource: container dns " + containersMap.size());
                ctx.logToConsole("targetDNs size: " + targetDNs.getTargetsMap().size());
            }

        } catch (Exception ex) {
            ctx.log(LOG_ERROR_LDAP_ENCLOSINGCONTAINERS, LOG_MAJOR, src, null, ex);
        }
        return true;
    }

    private void addCurrentUserGroups(IRequest request, TargetResHelper targetDNs) {
        String user = request.getUserID();

        if (HIGH_PRIORITY_INFO) {
            ctx.logToConsole("LDAPDataSource: user for users in ldap = " + user);
        }

        if ((!ctx.getSubsMrbaConfigMgr().isUsersInLdap()) && (user != null) && (userDirectory != null)) {
            if (HIGH_PRIORITY_INFO) {
                ctx.logToConsole("LDAPDataSource: users in is set, and user is not null");
            }

            // REMIND: time estimates does not include group search in source user from Tx
            addTargetToRes(targetDNs, user, TYPE_USER);

            String[] groups = userDirectory.listGroups(LDAPSearchFilter.escapeComponentValue(user));

            if (groups != null) {
                if (groups.length > 0) {
                    for (int i = 0; i != groups.length; i++) {
                        addTargetToRes(targetDNs, groups[i], TYPE_USERGROUP);
                    }
                }
            }
        }
    }

    // Fix for the defect SW00246316
    private Map resolveMachineDNs(IRequest request, TaskDurationLogger taskDurationLogger, String src) {
        Map map = new HashMap(2);
        String[][] userAndMachineDNs = null;
        try {
            userAndMachineDNs = ctx.getPluginLDAPUtils().
                                    getUserAndMachineDNs(request,
                                                         ctx.getSubsMrbaConfigMgr().getSubConfig(),
                                                         ctx.getSubsMrbaConfigMgr().getMrbaConfig(),
                                                         ctx.getPluginLDAPEnv(),
                                                         taskDurationLogger,
                                                         ctx.getSubsMrbaConfigMgr().isUsersInLdap(),
                                                         false);
        } catch(Exception e) {
            ctx.log(LOG_ERROR_LDAP_USERMACHINERES, LOG_AUDIT, src, null, null);
            ctx.log(LOG_ERROR_LDAP_DOWN, LOG_AUDIT, src, null, null);
            return null;
        }

        if (userAndMachineDNs[0] != null) {
            for (int i = 0; i < userAndMachineDNs[0].length; i++) {
                if(DETAILED_INFO) {
                    System.out.println(new Date() + " LDAPDataSource:getData() - Machine DN: " + userAndMachineDNs[0][i]);
                }
                map.put(userAndMachineDNs[0][i], TYPE_MACHINE);
            }
        }
        return map;
    }

}
