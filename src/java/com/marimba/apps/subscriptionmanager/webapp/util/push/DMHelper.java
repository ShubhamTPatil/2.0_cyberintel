package com.marimba.apps.subscriptionmanager.webapp.util.push;

import com.marimba.rpc.RPC;
import com.marimba.rpc.RPCSession;
import com.marimba.intf.util.IDirectory;
import com.marimba.intf.util.IConfig;
import com.marimba.intf.util.IObserver;
import com.marimba.intf.target.ICredentials;
import com.marimba.apps.sdm.intf.simplified.*;
import com.marimba.apps.sdm.server.simplified.DMList;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscription.common.StringResourcesHelper;
import com.marimba.tools.util.Props;
import com.marimba.tools.config.ConfigProps;
import com.marimba.webapps.intf.IWebAppsConstants;

import java.io.IOException;
import java.util.*;
import java.net.URL;

/**
 * An utility class that will interface with the DM to accomplish the UpdateNow push tasks.
 * @author  Anantha Kasetty
 * @version $Revision$, $Date$
 *
 *
 */
public class DMHelper {

    private static final String deploymentFolder = "/Policy Management Deployments";
    private static final String DM_TIMEOUT = "dm.timeout";

    private static final String DM_URL   = "dm.url";
    private static final String DM_USERNAME = "dm.username";
    private static final String DM_PASSWORD = "dm.password";
    private static final String DM_SERVERTIMEOUT = "dm.srt" ;
    private static final String INVALID_CONFIG_VALUES = "InvalidConfigValues";
    private static final String FAILED_TO_GET_RPCROOT = "FailedToGetRPCRoot";
    private static final String FAILED_TO_GET_IDIR_INTERFACE_OBJECT = "FailedToGetIDirInterfaceFromRPCRootObject";
    private static final String DEPLOYMENT_MGR_HASNOT_START = "DeploymentManagerHasNotStarted";
    private static final String DEPLOYMENT_MGR_RET_NULL_CONTEXT = "DeploymentManagerReturnedNullContext";

    private static final int MAX_TARGET_LOGS = 1000;
    private DMHelper dm = null;
    private Props dmProps;
    private IDMContext sdmContext;
    private RPCSession rpcSession;
    private ICredentials credentials;
    private static Map<String, DMHelper> dmHelperMap = new Hashtable<String, DMHelper>();

    public RPCSession getRpcSession() {
        return rpcSession;
    }

    public void setRpcSession(RPCSession rpcSession) {
        this.rpcSession = rpcSession;
    }


    public IDMContext getSdmContext() {
        return sdmContext;
    }
    private void setSdmContext(IDMContext context) {
        this.sdmContext = context;
    }

    public Props getDmProps() {
        return dmProps;
    }

    public void setDmProps(Props dmConfig) {
        this.dmProps = dmConfig;
    }

    public void setCrendentials(ICredentials cred) {
        this.credentials = cred;
    }
    public ICredentials getCrendentials() {
        return credentials;
    }
   /**
     * create an instance for the DMHelper based pn the config Props passed
     *
     *
     */
    public  synchronized static DMHelper getInstance (IConfig dmConfig, String tenantName)
           throws DepMgrException  {
    	if(null == dmConfig || null == dmConfig.getProperty(DM_URL)) return null;
    	try {
	    	DMHelper dm = new DMHelper();
	        String url = dmConfig.getProperty(DM_URL);
	        String username = dmConfig.getProperty(DM_USERNAME);
	        String password = dmConfig.getProperty(DM_PASSWORD);
	        String serverTimeout = dmConfig.getProperty(DM_SERVERTIMEOUT);
	
	        Props props = new Props();
	        props.setProperty(DM_USERNAME, username);
	        props.setProperty(DM_PASSWORD, password);
	        props.setProperty(DM_SERVERTIMEOUT, serverTimeout);
	        dm.setDmProps(props);
	
	        dm.init(url, username, password, serverTimeout);
	        dmHelperMap.put(tenantName, dm);
	        return dm;
    	} catch(Exception ec) {
    		System.out.println("Failed to initalize DM Configuration for tenant :" + tenantName);
    	}
    	return null;
    }

    public synchronized static DMHelper getInstance(String tenantName) {
        return dmHelperMap.get(tenantName);
    }

    public static void destroy() {
    	dmHelperMap = new Hashtable<String, DMHelper>();
    }

    private void init(String url, String username, String password, String serverTimeout)
            throws DepMgrException {
        int port;
        String host;
        String protocol;
        try {
            URL dmUrl = new URL(url);

            protocol = dmUrl.getProtocol();
            port = dmUrl.getPort();
            host = dmUrl.getHost();
            boolean secure = false;

            if (port == -1) {
                port = 7717;
            }
            if ("https".equals(protocol.toLowerCase())) {
                secure = true;
            }
            if ( host == null || host.equals("") || username == null || username.equals("")) {
                throw new DepMgrException(StringResourcesHelper.getMessage(INVALID_CONFIG_VALUES));
            }
            RPC rpc = new RPC();

            RPCSession session = rpc.connect(host, port, secure );


            Object root = session.getRoot();
            if (root == null)
                throw new DepMgrException(StringResourcesHelper.getMessage(FAILED_TO_GET_RPCROOT));
            IDirectory dir = (IDirectory) RPC.safeNarrow(root, IDirectory.class);
            if (dir == null)
                throw new DepMgrException(StringResourcesHelper.getMessage(FAILED_TO_GET_IDIR_INTERFACE_OBJECT));


            IDMMain main = (IDMMain) RPC.safeNarrow(dir.getChild("sdm"), IDMMain.class);

            if (main == null)
                throw new DepMgrException(StringResourcesHelper.getMessage(DEPLOYMENT_MGR_HASNOT_START));

            IDMContext context = main.createContext(deploymentFolder, username, password);

            if (context == null)
                throw new DepMgrException(StringResourcesHelper.getMessage(DEPLOYMENT_MGR_RET_NULL_CONTEXT));
            // set the servertimeout which dm.timeout property

            IDMProperty props = context.getDMConfig();
            if (serverTimeout != null && props != null) {
              //  --> not to change server_time out value
              //  props.setProperty(DM_TIMEOUT, serverTimeout);
            }

            setRpcSession(session);
            setSdmContext(context);
        }   catch (IOException e) {
            String additionalMessage = "Unable to connect to DM. (DM URL = " + url + ")(DM Username = " + username + ")(DM Password = " +
                    password + ")";
            throw new DepMgrException(additionalMessage + " " + e.getMessage());
        }
 }

    /**
     * Takes in an array of targets (specified as an URL string) and a string of commands
     * and creates a deployment against the DM
     * @param targets   an array of targets
     * @param cmds      an array of commands that is to be executed against all the targets
     * @return IDMDeployment return an IDMDeployment when successfull
     */
    protected IDMDeployment createDeployment(String[] targets, String[] cmds)
            throws DepMgrException {
        IDMDeployment deployment = null;
        try {
            // get the crendentials
            ICredentials cred = this.getCrendentials();

            // create an IList of targets and cmds
            IList targetList = new DMList(Arrays.asList(targets));
            IList cmdList   =  new DMList(Arrays.asList(cmds));

            // create the deployment against the obtained DMContext
            IDMContext sdmContext = this.getSdmContext();

            // we may need to reinitalize the DmHelper for the
            // DM could have changed (someone could manually delete the subfolder or do someother nasty stuff)

            deployment = sdmContext.createDeployment(cmdList, targetList, cred);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DepMgrException(e.getMessage());
        }

        return deployment;
    }

    /**
     * Takes in an array of targets (specified as an URL string) and a string of commands
     * and creates a deployment against the DM
     * @param targets   an array of targets.  Individual targets are in form of protocol://host:port, an url.
     * @param cmds      an array of commands that is to be executed against all the targets
     * @param cred      credentials to authenticate against the targets
     * @return IDMDeployment return an IDMDeployment when successfull
     */
    public IDMDeployment createDeployment(String[] targets, String[] cmds, ICredentials cred)
            throws DepMgrException {

        // set the crendentials
        this.credentials = cred;
        return createDeployment(targets, cmds);
    }

    /**
     * Takes in an array of targets (specified as an URL string) and a string of commands
     * and creates a deployment against the DM
     * @param targets   an array of targets.  Individual targets are in form of protocol://host:port, an url.
     * @param cmds      an array of commands that is to be executed against all the targets
     * @param tunerLogin     login for the target
     * @param tunerPassword  password for the target
     * @return IDMDeployment return an IDMDeployment when successfull
     */
    public IDMDeployment createDeployment(String[] targets, String[] cmds, String tunerLogin, String tunerPassword)
            throws DepMgrException {

        // create  the crendentials
        ICredentials cred = DMCredentials.getInstance(tunerLogin, tunerPassword);
        return createDeployment(targets, cmds, cred);
    }

    /**
     *  Takes a deploymentID string and returns true if the deployment is still active else return false
     *  @param deploymentID deploymentID of the deployment whose status we are interested in.
     */
    public boolean isDeploymentRunning(String deploymentID) {
        boolean retVal = false;
        if (deploymentID != null) {
            IDMDeployment deployment = getDeploymentFromDeploymentID(deploymentID);
            if (deployment != null) {
                IDMDeploymentStatus deploymentStatus = deployment.getStatus();
                if (deploymentStatus != null) {
                    retVal = deploymentStatus.isRunning();
                }
            }
        }

        return retVal;

    }


    public IDMDeployment getDeploymentFromDeploymentID (String deploymentID) {
        IDMDeployment ret = null;
        // remove the trailing slash from deploymentID
        String canonicalDeploymentID = deploymentID;
        if (deploymentID != null && deploymentID.lastIndexOf('/') == deploymentID.length() -1) {
            canonicalDeploymentID = deploymentID.substring(0, (deploymentID.length()-1));
        }
        if (sdmContext != null) {
            ret = sdmContext.getDeployment(canonicalDeploymentID);
            if (ret == null) {
                // try the full deploymentID
                // have to find out from Bruce why the trailing slash is works sometimes and othertimes returns null
                ret = sdmContext.getDeployment(deploymentID);
            }
        }
        return ret;
    }


    public HashMap getTargetStatus (String deploymentID) {
        IDMDeployment deployment = getDeploymentFromDeploymentID(deploymentID);
        return getTargetStatus(deployment);
    }

    public HashMap getTargetStatus (IDMDeployment deployment) {
        HashMap targetStatusMap = new HashMap();
        IDMDeploymentStatus deploymentStatus = deployment.getStatus();
        if (deploymentStatus != null) {
            IList targetStatusList = deploymentStatus.getTargetStatus();
                while( targetStatusList != null && targetStatusList.hasNext()) {
                    IDMTargetStatus targetStatus = (IDMTargetStatus)RPC.narrow(targetStatusList.next(), IDMTargetStatus.class);
                    if (targetStatus != null) {
                        targetStatusMap.put(targetStatus.getTargetName(), convertStatusCodeToString(targetStatus.getStatus()));
                    }

                }
        }
        return targetStatusMap;
    }

    private String convertStatusCodeToString(String status) {
        String retVal = "Pending";
        if (status.equals(IDMStatus.SUCCEEDED)  || status.equals(IDMStatus.SUCCEEDED_W_ERR)) {
            retVal = "Success";
        } else if (status.equals(IDMStatus.HASNOTRUN) || status.equals(IDMStatus.RUNNING)) {
            retVal = "Pending";
        } else if (status.equals(IDMStatus.FAILED)) {
            retVal = "Failed";
        } else if (status.equals(IDMStatus.STOPPED)) {
            retVal = "Stopped";
        }
        return retVal;
    }


    public ArrayList getTargetLogs(IDMDeployment deployment, String targeturl) {
        ArrayList retLogs = new ArrayList();
        try {
            // Get the deployment
            IDMDeploymentStatus deploymentStatus = deployment.getStatus();
            IDMTargetStatus targetStatus = deploymentStatus.getTargetStatus(targeturl);

            // Get a max number of logs

            IList targetLogList = targetStatus.getLogs(MAX_TARGET_LOGS);
            while(targetLogList.hasNext()) {

                // Need to narrow the IList entry to a IDMLogEntry class
                IDMLogEntry logEntry = (IDMLogEntry)RPC.narrow(targetLogList.next(), IDMLogEntry.class);

                String[] logPropertyPairs = logEntry.getPropertyPairs();

                retLogs.add(new TargetLogEntry(logPropertyPairs));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return retLogs;

    }
}
