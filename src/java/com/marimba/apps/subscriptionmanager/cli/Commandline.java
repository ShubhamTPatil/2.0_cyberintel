// Copyright 1997-2009, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$



package com.marimba.apps.subscriptionmanager.cli;



import java.io.*;



import java.net.*;

import java.util.*;

import java.text.BreakIterator;



import javax.servlet.ServletContext;





import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.intf.*;
import com.marimba.apps.subscription.common.intf.objects.*;
import com.marimba.apps.subscription.common.objects.*;
import com.marimba.apps.subscription.common.util.*;
import com.marimba.apps.subscriptionmanager.MergeAllSub;
import com.marimba.apps.subscriptionmanager.util.Utils;
import com.marimba.apps.subscriptionmanager.*;
import com.marimba.apps.subscription.common.LDAPQueryInfo;
import com.marimba.apps.subscriptionmanager.cli.tools.GetADInfo;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.users.*;
import com.marimba.apps.subscriptionmanager.webapp.forms.*;
import com.marimba.webapps.intf.SystemException;

import com.marimba.castanet.schedule.*;



import com.marimba.intf.msf.*;

import com.marimba.intf.msf.acl.*;



import com.marimba.intf.util.*;



import com.marimba.tools.config.*;



import com.marimba.tools.gui.*;



import com.marimba.tools.ldap.*;



import com.marimba.tools.util.*;



import com.marimba.webapps.intf.*;



import marimba.io.*;



/**

 * Processes command line args. This file contains code from file CLIProcessor.java in 4.7 Subscription Manager

 *

 * @author Simon Wynn

 * @author Damodar Hegde

 * @author Vidya Viswanathan

 * @author Theen-Theen Tan

 * @author Narasimhan Mahendrakumar

 * @author Kumaravel Ayyakkannu

 * @version $Revision$, $Date$

 */

public class Commandline

    implements FilenameFilter,

                   ISubscriptionConstants,

                   IAppConstants,

                   IErrorConstants,

        IAclConstants {



	SubscriptionMain controller;
	String dirType;

    ServletContext   context;

    StringResources  resources;

    Hashtable        validPrimaryStates;

    Hashtable        validSecondaryStates;

    Hashtable        validContentType;

    Hashtable        validArgs;

    Hashtable        validTypes;

    Hashtable        displayStateMapping;

    Hashtable        aclMapping;

    StringBuffer commStr;

    CLIUser      cliUser;

    String       namespace;

    ICommandLine cmsCli;



    SubscriptionCLICmdFactory cmdFactory;

    private TreeMap	         cliUsageGrps = new TreeMap();

    /**

     * Class constructor

     *

     * @param context servletcontext used to access webapp files

     * @param controller subscriptionmain object for providing dirctory , config objects etc.

     */

    public Commandline(ServletContext   context,

                       SubscriptionMain controller, String dirType) {

        this.context    = context;

        this.controller = controller;
        dirType = dirType;


        // Valid primary states when a secondary state is specified

        validPrimaryStates = new Hashtable();

        validPrimaryStates.put(STATE_AVAILABLE, "state");

        validPrimaryStates.put(STATE_SUBSCRIBE_NOINSTALL, "state");



        // Valid secondary states

        validSecondaryStates = new Hashtable();

        validSecondaryStates.put(STATE_SUBSCRIBE, "state");

        validSecondaryStates.put(STATE_SUBSCRIBE_START, "state");

        validSecondaryStates.put(STATE_SUBSCRIBE_PERSIST, "state");

        validSecondaryStates.put(STATE_START_PERSIST, "state");



        // Valid value for Content Type

        validContentType = new Hashtable();

        validContentType.put(CONTENT_TYPE_APPLICATION, "type");

        validContentType.put(CONTENT_TYPE_PATCHGROUP, "type");

        validContentType.put(CONTENT_TYPE_ALL, "type");



        // Subscription state to  Display state mapping

        displayStateMapping = new Hashtable();

        displayStateMapping.put("available", "advertise");

        displayStateMapping.put("subscribe_noinstall", "stage");

        displayStateMapping.put("subscribe", "install");

        displayStateMapping.put("subscribe_start", "install-start");

        displayStateMapping.put("start_persist", "install-start-persist");

        displayStateMapping.put("subscribe_persist", "install-persist");

        displayStateMapping.put("delete", "uninstall");

        displayStateMapping.put("exclude", "exclude");

        displayStateMapping.put("primary", "primary");



        // Acl user input mapped to constants used internally

        aclMapping = new Hashtable();

        aclMapping.put("acl", ACL_PERMISSION);

        aclMapping.put("sub", SUBSCRIPTION_PERMISSION);

        aclMapping.put("template", TEMPLATE_PERMISSION);



        // validArgs Hashtable stores Valid arguments for command line

        // interface . These arguments start with a "-" character. Any

        // subarguments to a command can also start with a "-" character

        // for e.g. -delete -all , however the subarguement cannot be

        // same as one of the valid arguments.

        // If a new argument needs to be added to the Command line interface

        // then following steps

        // 1. Add a new key/value representing the new command in the validArgs

        //    Hashtable in this Commandline constructor method.

        // 2. In method processArgs , which parses the Command line arguements

        //    using a for loop , add a new condition to check for the new

        //    command and add the new command and its arguemnts into 'cmds'

        //    Hashtable.

        // 3. Determine the order in the which the new command should be

        //    processed and add code to call the method  to handle the command

        //    in processArgs method at appropriate location.

        // 4. If the command succeeds then processArgs should return

        //    value 'false' else return value 'true'.

        validArgs = new Hashtable();

        validArgs.put("-machines", "-machines");

        validArgs.put("-ldapservers", "-ldapservers");

        validArgs.put("-subscribe", "-subscribe");

        validArgs.put("-changeorder", "-changeorder");

        validArgs.put("-export", "-export");

        validArgs.put("-import", "-import");

        validArgs.put("-delete", "-delete");

        validArgs.put("-publish", "-publish");

        validArgs.put("-tuner", "-tuner");

        validArgs.put("-help", "-help");

        validArgs.put("-clientcertpw", "-clientcertpw");

        validArgs.put("-publishpw", "-publishpw");

        validArgs.put("-list", "-list");

        validArgs.put("-mode", "-mode");

        validArgs.put("-runquery", "-runquery");

        validArgs.put("-D", "-D");

        validArgs.put("-w", "-w");

        validArgs.put("-h", "-h");

        validArgs.put("-p", "-p");

        validArgs.put("-Z", "-Z");

        validArgs.put("-usedn", "-usedn");

        validArgs.put("-basedn", "-basedn");

        validArgs.put("-auth", "-auth");

        validArgs.put("-namespace", "-namespace");

        validArgs.put("-getdirectoryinfo", "getdirectoryinfo");

        validArgs.put("-setpluginparam", "-setpluginparam");

        validArgs.put("-txadminaccess", "-txadminaccess");

        validArgs.put("-aclCheck", "-aclCheck");

        validArgs.put("-aclGet", "-aclGet");

        validArgs.put("-aclSet", "-aclSet");

        validArgs.put("-aclRemove", "-aclRemove");

        validArgs.put("-configSet", "-configSet");

        validArgs.put("-patchsubscribe","-patchsubscribe");

        validArgs.put("-ldapqc", "-ldapqc");   // ldapQryCollection

        validArgs.put("-remedysubscribe","-remedysubscribe");

        validArgs.put("-copypolicy","-copypolicy");

        validArgs.put("-complianceenabled", "-complianceenabled");


	validTypes  = new Hashtable();

	validTypes.put("machine", "machine");

	validTypes.put("machinegroup", "machinegroup");

	validTypes.put("user", "user");

	validTypes.put("usergroup", "usergroup");

	validTypes.put("all", "all");

	validTypes.put("container", "container");

	validTypes.put("collection", "collection");

	validTypes.put("ldapqc", "ldapqc");





        // load string resources

        URL url = null;



        try {

            url = context.getResource("/");

        } catch (MalformedURLException e) {

            e.printStackTrace();

        }



        resources = new com.marimba.tools.gui.StringResources("commandline", Locale.getDefault(), url);



        try {

        	InputStream fout = null;

	    	try {

	    		url = context.getResource("/sub-cmdline.txt");

	    		fout = url.openStream();

	    		readArgSpec(fout);

	    	} finally {

	    		if ( fout != null ) {

	    		//close the stream as this causes leaks.

	    			fout.close();

	    		}

	    	}

        } catch (IOException ie) {

        	ie.printStackTrace();

        }



        Properties systemProps = System.getProperties();

        systemProps.put("channel.nogui", "true");

    }

    //Step1:  Parsing command line arguments

    /**

     * Wrapper for processing command line arguements. The arguements are passd as a single string This method is called from CommandlineAction.java

     *

     * @param commandargs A single string containing Command line arguements

     * @param form Form used in the .jsp to specify the CLI string

     *

     * @return true if command failed to execute succesfully false if command executes succesfully

     *

     * @throws SystemException REMIND

     */

    public boolean processArgsWrapper(String          commandargs,

                                      CommandlineForm form)

        throws SystemException {

        // parse the arguments

        Vector          v = new Vector();



        StringTokenizer st = new StringTokenizer(commandargs);



        while (st.hasMoreTokens()) {

            String u = st.nextToken();

            v.addElement(u);

        }



        String[] args = new String[v.size()];

        v.copyInto(args);



        boolean retval = processArgs(args);



        if (form != null) {

            form.setOutput(getOutputStr());

        } else {

        	if(DEBUG) {

                System.out.println("Form object is null. Dumping Output string:" + getOutputStr());

        	}

        }



        return retval;

    }

    /**

     * Processes command line arguements. The arguements are passd as an array of strings This method is called from SubscriptionCLIServlet.java Only one

     * command line session can be invoked at any time . If a user calls runchannel to start another command line session when one session is in progress then

     * the second call will wait till the first one is over.

     *

     * @param cms A string array containing Command line arguements

     *

     * @return true if command failed to execute succesfully false if command executes succesfully

     *

     * @throws SystemException REMIND

     */

    synchronized public boolean processArgs(ICommandLine cms)

        throws SystemException {

        String[] args = cms.getArguments();



		if (DEBUG5) {

		    for (int i = 0; i < args.length; i++) {

				System.out.print(args [i]);

		    }

		    System.out.println("");

		}



        cmsCli = cms;



        return processArgs(args);

    }



    /**

     * REMIND

     *

     * @param args REMIND

     *

     * @return REMIND

     *

     * @throws SystemException REMIND

     */

    synchronized public boolean processArgs(String[] args)

        throws SystemException {

        commStr   = new StringBuffer(2056);

        namespace = null;

        String cmdoption = null;



        boolean   warn_ldapparams = false;

        Hashtable cmds;



        cmds = new Hashtable();



        // First parse the args and add everything to a hashtable.

        // This enables the args to be handled in any order



        try {



            if (args != null) {

                if (args.length == 0) {

                    usage();



                    return true;

                }



                for (int i = 0; i < args.length; i++) {



                    if (args [i].equals("-machines")) {

                        if (args.length > (i + 1)) {

                            cmds.put("machines", args [i++]);

                            cmds.put("machines:args", args [i]);



                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-ldapservers")) {

                        if (args.length > (i + 1)) {

                            cmds.put("ldapservers", args [i++]);

                            cmds.put("ldapservers:args", args [i]);

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-clientcertpw")) {

                        if (args.length > (i + 1)) {

                            cmds.put("clientcertpw", args [i++]);

                            cmds.put("clientcertpw:args", args [i]);

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-help")) {

                        cmds.put("help", args [i]);

                    } else if (args [i].equals("-export")) {

                        if (args.length > (i + 1)) {

                            cmds.put("export", args [i++]);

                            cmds.put("export:args", args [i]);

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-complianceenabled")) {
                    	if (args.length > (i + 1)) {
                            cmds.put("complianceenabled", args [i++]);
                            int c = 0;
                            for (; i != args.length; i++) {
                                if (validArgs.containsKey(args [i])) {
                                    i--;
                                    break;
                                }
                                cmds.put("complianceenabled:args" + c++, args [i]);
                            }
                        } else {
                            usage();
                            return true;
                        }
                    } else if (args [i].equals("-txadminaccess")) {

                        cmds.put("txadminaccess", args [i]);



                        if (args.length > (i + 1)) {

                            cmds.put("txadminaccess", args [i++]);



                            int c = 0;



                            for (; i != args.length; i++) {

                                if (validArgs.containsKey(args [i])) {

                                    i--;



                                    break;

                                }



                                cmds.put("txadminaccess:args" + c++, args [i]);

                            }

                        }

                    } else if (args [i].equals("-publish")) {

                        cmds.put("publish", args [i]);



                        if (args.length > (i + 1)) {

                            if (validArgs.containsKey(args [i + 1])) {

                                cmds.put("publish:args", "publish:noarg");

                            } else {

                                cmds.put("publish:args", args [++i]);

                            }

                        } else {

                            cmds.put("publish:args", "publish:noarg");

                        }

                    } else if (args [i].equals("-publishpw")) {

                        if (args.length > (i + 1)) {

                            cmds.put("publishpw", args [i++]);



                            int c = 0;



                            for (; i != args.length; i++) {

                                if (validArgs.containsKey(args [i])) {

                                    i--;



                                    break;

                                }



                                cmds.put("publishpw:args" + c++, args [i]);

                            }

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-subscribe")) {

                        cmdoption = "subscribe";

                        if (args.length > (i + 1)) {

                            cmds.put("subscribe", args [i++]);



                            int c = 0;



                            for (; i != args.length; i++) {

                                if (validArgs.containsKey(args [i])) {

                                    i--;



                                    break;

                                }

                                if ("-policysource".equalsIgnoreCase(args[i])) {

                                        if (processPolicyFile(cmds, args[++i], c, cmdoption)) {

                                        return true;

                                        }

                                } else {

                                    cmds.put("subscribe:args" + c++, args [i]);

                                }

                            }

                            // retrieve the channel url from the -subscribe arguments

                            String url = null;

                            String arg = null;

                            int index =0;

                            for (int k = 0; k < cmds.size()-1; k++) {
                                arg = (String)cmds.get("subscribe:args" + k);

                                if (arg != null && (arg.startsWith("http://") || arg.startsWith("https://"))) {

                                    index = arg.lastIndexOf("=");

                                    url = arg.substring( 0, index+1 );

                                    if (checkSpecialChar(url)) {

                                        return true;

                                    }

                                }

                            }

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-changeorder")) {

                        if (args.length > (i + 1)) {

                          cmds.put("changeorder", args [i++]);



                          int c = 0;



                          for (; i != args.length; i++) {

                              if (validArgs.containsKey(args [i])) {

                                  i--;



                                  break;

                              }



                              cmds.put("changeorder:args" + c++, args [i]);

                          }

                      } else {

                          usage();



                          return true;

                      }

                    } else if (args [i].equals("-tuner")) {

                        cmdoption = "tuner";

                        if (args.length > (i + 1)) {

                            cmds.put("tuner", args [i++]);



                            int c = 0;



                            for (; i != args.length; i++) {

                                if (validArgs.containsKey(args [i])) {

                                    i--;



                                    break;

                                }

                                if ("-propertysource".equalsIgnoreCase(args[i])) {

                                    if(i == args.length -1) {

                                        usage();

                                        return true;

                                    } else {

                                        if (processPolicyFile(cmds, args[++i], c, cmdoption)) {

                                            return true;

                                        }

                                    }

                                }

                                else {

                                    if ("-targetsource".equalsIgnoreCase(args[i])) {

                                        if(i == args.length -1) {

                                        usage();

                                        return true;

                                        }

                                    }

                                    cmds.put("tuner:args" + c++, args [i]);

                                }

                            }

                            // retrieve the channel url from the -tuner arguments

                            String arg = null;

                            for (int k = 0; k < cmds.size()-1; k++) {

                                arg = (String)cmds.get("tuner:args" + k);

                                if (arg != null && ((arg.indexOf ("http://") > -1 ) || (arg.indexOf ("https://") > -1 ))) {

                                    if (checkSpecialChar(arg)) {

                                        return true;

                                    }

                                }

                            }

                    } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-import")) {

                        if (args.length > (i + 1)) {

                            cmds.put("import", args [i++]);



                            int c = 0;



                            for (; i != args.length; i++) {

                                if (validArgs.containsKey(args [i])) {

                                    i--;



                                    break;

                                }



                                cmds.put("import:args" + c++, args [i]);

                            }

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-copypolicy")) {
                        if (args.length == 5) {
                            if (validArgs.containsKey(args [i + 1])) {
                            	usage();
                            	return true;
                            } else {
                            	if(!("-sourcetarget".equalsIgnoreCase(args[1]) && "-desttarget".equalsIgnoreCase(args[3]))) {
                            		usage();
                            		return true;
                            	}
                            	cmds.put("copypolicy", args [i]);
                                i++;
                                int c = 0;
                                for (; i != args.length; i++) {
                                    if (validArgs.containsKey(args [i])) {
                                        i--;
                                        break;
                                    }
                                    cmds.put("copypolicy:args" + c++, args [i]);
                                }
                            }
                        } else {
                        	usage();
                        	return true;
                        }

                    } else if (args [i].equals("-delete")) {

                        if (args.length > (i + 1)) {

                            cmds.put("delete", args [i++]);



                            int c = 0;



                            for (; i != args.length; i++) {

                                if (validArgs.containsKey(args [i])) {

                                    i--;



                                    break;

                                }



                                cmds.put("delete:args" + c++, args [i]);

                            }

                        } else {

                            usage();



                            return true;

                        }

                    }  else if(args [i].equals("-ldapqc")) {

                           if (args.length > (i + 1)) {

                             cmds.put("ldapqc", args [i++]);



                             int c = 0;



                             for (; i != args.length; i++) {



                                 cmds.put("ldapqc:args" + c++, args [i]);

                             }

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-list")) {

                        cmds.put("list", args [i]);



                        if (args.length > (i + 1)) {

                            if (validArgs.containsKey(args [i + 1])) {

                                cmds.put("list:args", "list:noarg");

                            } else {

                                i++;



                                int c = 0;



                                for (; i != args.length; i++) {

                                    if (validArgs.containsKey(args [i])) {

                                        i--;



                                        break;

                                    }



                                    cmds.put("list:args" + c++, args [i]);

                                }

                            }

                        } else {

                            cmds.put("list:args", "list:noarg");

                        }

                    } else if (args [i].equals("-mode")) {

                        cmds.put("mode", args [i]);



                        if (args.length > (i + 1)) {

                            if (validArgs.containsKey(args [i + 1])) {

                                cmds.put("mode:args", "mode:noarg");

                            } else {

                                cmds.put("mode:args", args [++i]);

                            }

                        } else {

                            cmds.put("mode:args", "mode:noarg");

                        }

                    } else if (args [i].equals("-runquery")) {

                        if (args.length > (i + 1)) {

                            cmds.put("runquery", args [i++]);



                            int c = 0;



                            for (; i != args.length; i++) {

                                if (validArgs.containsKey(args [i])) {

                                    i--;



                                    break;

                                }



                                cmds.put("runquery:args" + c++, args [i]);

                            }

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-D") || args [i].equals("-w") || args [i].equals("-h") || args [i].equals("-p") || args [i].equals("-Z") || args [i]

                                                                                                                                                                .equals("-usedn") || args [i]

                                                                                                                                                                                         .equals("-auth") || args [i]

                                                                                                                                                                                                                 .equals("-basedn")) {

                        if (args.length > (i + 1)) {

                            // We still need to read in the argument even if we are

                            // ignoring the parameter otherwise subsequent parsing

                            // will fail.

                            i++;

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-namespace")) {

                        if (args.length > (i + 1)) {

                            cmds.put("namespace", args [i++]);

                            cmds.put("namespace:args", args [i]);

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-getdirectoryinfo")) {

                        cmds.put("getdirectoryinfo", args [i++]);

						Props configProps = new Props();

                        cmds.put("getdirectoryinfoprops", configProps);

                        if (args.length > (i + 2)) {

                            for (; i < args.length;  ) {

                            	if(DEBUG5) {

    								System.out.println("--" + args[i]);

                            	}

                                configProps.setProperty(args[i++], args [i++]);

                            }

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-setpluginparam")) {

                        if (args.length > (i + 1)) {

                            cmds.put("setpluginparam", args [i++]);



                            int c = 0;



                            for (; i != args.length; i++) {

                                if (validArgs.containsKey(args [i])) {

                                    i--;



                                    break;

                                }



                                cmds.put("setpluginparam:args" + c++, args [i]);

                            }

                        } else {

                            usage();



                            return true;

                        }

		    } else if (args [i].equals("-patchsubscribe")) {

                        if (args.length > (i + 1)) {

                            cmds.put("patchsubscribe", args [i++]);



                            int c = 0;



                            for (; i != args.length; i++) {

                                if (validArgs.containsKey(args [i])) {

                                    i--;



                                    break;

                                }



                                cmds.put("patchsubscribe:args" + c++, args [i]);

                            }

                        } else {

                            usage();



                            return true;

                        }

                    } else if (args [i].equals("-upgrade")) {

                        i = getCommandArguments(args, cmds, "upgrade", i);

                    } else if (args [i].equals("-configSet")) {

                        i = getCommandArguments(args, cmds, "configSet", i);

                    } else if (args [i].equals("-remedysubscribe")) {

                        if (args.length > (i + 1)) {
                             cmds.put("remedysubscribe", args [i++]);

                            int c = 0;
                            for (; i != args.length; i++) {
                                if (validArgs.containsKey(args [i])) {
                                    i--;
                                    break;
                                }
                                cmds.put("remedysubscribe:args" + c++, args [i]);
                            }
                        } else {
                            usage();
                            return true;
                        }
                    } else {

                        printMessage(resources.getString("cmdline.unknownarg") + " " + args [i]);

                        usage();



                        return true;

                    }

                }

            } else {

                usage();



                return true;

            }



            //

            // process args in a logical order

            //

            // help, installscript, collectionscript, updatescript

            // commands should not require Subscription LDAP setup

            if (cmds.get("help") != null) {

                usage();



                return false;

            }



            if (cmds.get("getdirectoryinfo") != null) {

				GetADInfo adinfo = new GetADInfo((IProperty) cmds.get("getdirectoryinfoprops"));

				adinfo.execute();

                return true;



            }



            cliUser = new CLIUser(cmsCli.getUser(), controller);

            cliUser.initialize();



    	    checkAdminRole();

            // Intialize the factory for creating CLI command objects.

            cmdFactory = new SubscriptionCLICmdFactory(resources, controller, cliUser);



            if (cliUser.getSubConn() != null) {

                printMessage(resources.getString("cmdline.ldapconnect.connected") + " " + cliUser.getName() + " (" + cliUser.getFullName() +")");

            }



            if (cmds.get("host") != null) {

                warn_ldapparams = true;

            }



            if (cmds.get("port") != null) {

                warn_ldapparams = true;

            }



            if (cmds.get("auth") != null) {

                warn_ldapparams = true;

            }



            if (cmds.get("basedn") != null) {

                warn_ldapparams = true;

            }



            if (cmds.get("mode") != null) {

                //5.0 release of Subscription doesn't support this option

                printMessage(resources.getString("cmdline.mode.removed"));

            }



            if (cmds.get("runquery") != null) {

                //5.0 release of Subscription doesn't support this option

                printMessage(resources.getString("cmdline.runquery.removed"));

            }



            if (cmds.get("bindpw") != null) {

                if (warn_ldapparams) {

                    printMessage(resources.getString("cmdline.ldapconnect.ignoreparams"));

                }

            }



            // Following command require users to login into the LDAP Server

            if (cmds.get("ldapservers") != null) {

                if (!checkConnection()) {

                    return true;

                }



                String arg = (String) cmds.get("ldapservers:args");



                if (copyLDAPServersFile(arg)) {

                    return true;

                }

            }



            if (cmds.get("namespace") != null) {

                if (!checkConnection()) {

                    return true;

                }



                String ns = (String) cmds.get("namespace:args");



                if ("".equals(ns)) {

                    ns = null;

                }



                setNameSpace(ns);

            }

            if (cmds.get("machines") != null) {

                if (!checkConnection()) {

                    return true;

                }



                // Do not allow for importing machine for Active Directory

                if (null == dirType || LDAPConstants.VENDOR_AD.equals(dirType)) {

                    printMessage(resources.getString("cmdline.noimportforactivedir"));



                    return true;

                }



                String arg = (String) cmds.get("machines:args");



                if (loadMachines(arg)) {

                    return true;

                }

            }



            if (cmds.get("delete") != null) {

                if (!checkConnection()) {

                    return true;

                }

		if(!isDeleteValidate(cmds)) {

		    return true;

		}

                if (delete(cmds)) {

                    return true;

                }

            }

            if (cmds.get("ldapqc") != null) {

                if (!checkConnection()) {

                return true;

            }

	    if(!isLdapqcConfigSetValid(cmds)) {

		return true;

	    }

            if (ldapqc(cmds)) {

                return true;

            }

          }

            if (cmds.get("subscribe") != null) {

                if (!checkConnection()) {

                    return true;

                }



		if(!isSubscribeArgsValid(cmds)) {

		    return true;

        }

                if ("-modify".equalsIgnoreCase((String)cmds.get("subscribe:args0"))) {

                    if ("-targetsource".equalsIgnoreCase((String)cmds.get("subscribe:args1")))  {

                        if (subscribeMulti(cmds)) {

                            return true;

                        }

                    } else if(("-remove".equalsIgnoreCase((String)cmds.get("subscribe:args1")))) {

                        if ("-targetsource".equalsIgnoreCase((String)cmds.get("subscribe:args2")))  {

                            if (subscribeMulti(cmds)) {

                                return true;

                            }

                        }

                        else {

                            if (subscribe(cmds)) {

                                return true;

                            }

                        }

                    }

                    else {

                        if (subscribe(cmds)) {

                            return true;

                        }

                    }

                } else if ("-targetsource".equalsIgnoreCase((String)cmds.get("subscribe:args0"))) {

                    if (subscribeMulti(cmds)) {

                        return true;

                    }

                } else {

                    if (subscribe(cmds)) {

                        return true;

                    }

                }

            }

             if (cmds.get("changeorder") != null) {

                if (!checkConnection()) {

                    return true;

                }

                if(!isChangeorderArgsValid(cmds)) {

		    return true;

		}

                if (changeorder(cmds)) {

                    return true;

                }

              }



            if (cmds.get("patchsubscribe") != null) {

                if (!checkConnection()) {

                    return true;

                }



		if (!isPatchSubscribeArgsValid(cmds)) {

		    return true;

		}



                if (patchsubscribe(cmds)) {

                    return true;

                }

            }

            if (cmds.get("remedysubscribe") != null) {
                if (!checkConnection()) {
                    return true;
                }

		        if (!isRemedySubscribeArgsValid(cmds)) {
        		    return true;
                }

                if (remedysubscribe(cmds)) {
                    return true;
                }
            }



            if (cmds.get("tuner") != null) {

                if (!checkConnection()) {

                    return true;

                }



		        if(!isTunerPropsArgsValid(cmds)){

		            return true;

		        }



                if ("-modify".equalsIgnoreCase((String)cmds.get("tuner:args0"))) {

                    if ("-targetsource".equalsIgnoreCase((String)cmds.get("tuner:args1"))) {

                        if (tunerMulti(cmds)) {

                            return true;

                        }

                    } else if(("-remove".equalsIgnoreCase((String)cmds.get("tuner:args1")))) {

                        if ("-targetsource".equalsIgnoreCase((String)cmds.get("tuner:args2")))  {

                            if (tunerMulti(cmds)) {

                                return true;

                            }

                        } else {

                            if (tuner(cmds)) {

                                return true;

                            }

                        }



                    } else {

                        if (tuner(cmds)) {

                            return true;

                        }

                    }

                } else if ("-targetsource".equalsIgnoreCase((String)cmds.get("tuner:args0"))) {

                    if (tunerMulti(cmds)) {

                        return true;

                    }

                } else {

                    if (tuner(cmds)) {

                        return true;

                    }

                }

            }



            if (cmds.get("txadminaccess") != null) {

                if (!checkConnection()) {

                    return true;

                }



                if (txadminaccess(cmds)) {

                    return true;

                }

            }



            if (cmds.get("import") != null) {

                if (!checkConnection()) {

                    return true;

                }



                if (importFiles(cmds)) {

                    return true;

                }

            }

            if(cmds.get("complianceenabled") != null) {
                if (!checkConnection()) {
                   return true;
                }

                if(complianceOption(cmds)) {
                    return true;
                }
            }

            if(cmds.get("export") != null) {
                if (!checkConnection()) {
                   return true;
                }

                String arg = (String)cmds.get("export:args");
                if(export(arg)) {
                    return true;
                }
            }

            if (cmds.get("clientcertpw") != null) {

                String arg = (String) cmds.get("clientcertpw:args");



                if (certPassword(arg)) {

                    return true;

                }

            }



            if (cmds.get("list") != null) {

                if (!checkConnection()) {

                    return true;

                }

                if(!isListPolicyValidate(cmds)){

					return true;

				}

                if (list(cmds)) {

                    return true;

                }

            }



            if ((cmds.get("setpluginparam") != null) && (cmds.get("publish") == null)) {

                printMessage(resources.getString("cmdline.plugin.publishrequired"));



                return true;

            }



            // NOTE: Publish should always be the last action performed

            if (cmds.get("publish") != null) {

                //NOTE: this processes 'publishpw' too

                String arg = (String) cmds.get("publish:args");



                // if there is only one 'publishpw', assume it is

                // the password

                String username;

                String password;



                if (cmds.containsKey("publishpw:args1")) {

                    username = (String) cmds.get("publishpw:args0");

                    password = (String) cmds.get("publishpw:args1");

                } else {

                    username = null;

                    password = (String) cmds.get("publishpw:args0");

                }



                if ("publish:noarg".equals(arg)) {

                    printMessage("Publish URL can not be null");



                    return true;

                }



                // SetPluginParam command would be called to populate CMS or published values even

                // -setpluginparam is not passed. If passed, the value will be overriden with the one

                // given by the user.

                ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.SET_PLUGIN_PARAM);

                boolean isFailed = command.execute(cmds);

                commStr.append(command.getOutputStr());

                if (isFailed) {

                    return true;

                }



                // REMIND tcube verifyLDAPConnection for publish

                //String invhosts = controller.verifyLDAPConnections();

                // If there are invalid hosts then dispaly the warning dialog

                // along with the list of invalid hosts

                //if (!"".equals(invhosts)) {

                //	printMessage(resources.getString("cmdline.publish.couldnotconnect") + invhosts);

                //   }

                if (publish(arg, username, password)) {

                    return true;

                }

            }

            if (cmds.get("configSet") != null) {

                if (!checkConnection()) {

                    return true;

                }



		if(!isConfigSetValidate(cmds)) {

		     return true;

		}

                if (configSet(cmds)) {

                    return true;

                }

            }
            if (cmds.get("copypolicy") != null) {
            	if(copyPolicy(cmds)) {
            		return true;
            	}
            }

        } catch (Throwable ex) {

            classifyCLIException(ex);

        }



        // were done...

        printMessage(resources.getString("cmdline.done"));



        return false;

    }



    boolean processPolicyFile(Hashtable cmds, String filename, int pos, String cmdop) {

        File fileSrc;

        FastInputStream fileSrcStr;

        StringBuffer token = new StringBuffer();

        String cloption = null;

        int ch = 0;



        try {

            fileSrc = new File(filename);



            if (fileSrc.exists()) {

                fileSrcStr = new FastInputStream(fileSrc);

                try {

                    boolean doubleQuote = false;

                    if(cmdop.equals("subscribe"))

                        cloption = "subscribe:args";

                    if(cmdop.equals("tuner"))

                        cloption = "tuner:args";



                    do {

                       ch = fileSrcStr.read();

                       if (ch == '"') {

                          doubleQuote = !doubleQuote;

                          continue;

                       }

                       if (ch == '\r') {

                         ch = fileSrcStr.read();

                       }

                       if ((ch =='\n'|| ch == ' '|| ch == -1) && !doubleQuote && (token.length()) != 0) {

                            cmds.put(cloption + pos++, token.toString());

                            token.delete(0, token.length());

                       } else {

                          token.append((char) ch);

                       }

                      } while (ch != -1);

                  } finally {

                    fileSrcStr.close();

                  }

                } else {

                printMessage(resources.getString("cmdline.filenotfound")+" "+filename);

                return true;

            }

        } catch (IOException ioEx) {

            ioEx.printStackTrace();

            return true;

        }

        return false;

    }



    void classifyCLIException(Throwable ex)

         throws SystemException {

              if (ex instanceof SystemException) {

                  throw (SystemException) ex;

              } else {

                  /* The Exception thrown is not a system exception.  This means that

                   * it is unexpected behavior and should be an internal exception

                   */

                  InternalException ie = new InternalException(ex, IWebAppsConstants.SYSTEM_INTERNAL_EXCEPTION, ex.getMessage());

                  ex.printStackTrace();

                  throw ie;

              }

    }

    //Step2:  Validating arguments specific to every command displaying usage

    boolean isTunerPropsArgsValid(Hashtable cmds) {

      	int    i = 0;

	String arg;



	String name = null;

	String type = null;

	boolean status = false;

    boolean isModifyproperty = false;



        //arg0

	    if("-modify".equals((String) cmds.get("tuner:args" + i++))) {

    	    isModifyproperty = true;

	    } else {

	        // Shifting the index backwards since -modify is not found

	        i--;

	    }

        String arg1 = ((String) cmds.get("tuner:args" + i++)).toLowerCase();



	    if ("-dn".equals(arg1)) {

		name = (String) cmds.get("tuner:args" + i++);

		type = (String) cmds.get("tuner:args" + i++);



		if ("-type".equals(type)) {

		    type = (String) cmds.get("tuner:args" + i++);



		    if (type == null) {

			usage();



			return false;

		    }



		    type = type.toLowerCase();



		    if (!validTypes.containsKey(type)) {

			printMessage(resources.getString("cmdline.invtargettype") + " " + type);



			return false;

		    }

	    }

	    }  else if((!("-targetsource".equalsIgnoreCase(arg1))) && (!("-remove".equalsIgnoreCase(arg1)))) {

		// create the props file based on the name and type args

		// first validate the type

		type = (String) cmds.get("tuner:args" + i++);



		if (type == null) {

		    usage();



		    return false;

		}



		type = type.toLowerCase();



		if (!validTypes.containsKey(type)) {

		    printMessage(resources.getString("cmdline.invtargettype") + " " + type);



		    return false;

		}

	    }

	  return true;

    }

    boolean isSubscribeArgsValid(Hashtable cmds) {

	int i = 0;

	String arg;

	String name = null;

	String type = null;

	boolean isModifyPolicy = false;



	//arg0

	if("-modify".equals((String) cmds.get("subscribe:args" + i++))) {

	    isModifyPolicy = true;

	} else {

	    // Shifting the index backwards since -modify is not found

	    i--;

	}



	// arg0 if -modify is not entered by the user

	String dnSwitchOrTargetName = ((String) cmds.get("subscribe:args" + i++)).toLowerCase();



	if ("-dn".equals(dnSwitchOrTargetName)) {

	    // arg2 is name and arg3 is type if -modify is not given

	    name = (String) cmds.get("subscribe:args" + i++);

	    type = (String) cmds.get("subscribe:args" + i++);



	    if ("-type".equals(type)) {

		// arg4 is type if -modify is not given

		type = (String) cmds.get("subscribe:args" + i++);



		if (type == null) {

		    usage();



		    return false;

		}



		type = type.toLowerCase();



		if (!validTypes.containsKey(type)) {

		    printMessage(resources.getString("cmdline.invtargettype") + " " + type);



		    return false;

		}

	    }

	} else if((!("-targetsource".equalsIgnoreCase(dnSwitchOrTargetName))) && (!("-remove".equalsIgnoreCase(dnSwitchOrTargetName)))) {

	    // create the props file based on the name and type args

	    // first validate the type

	    name = dnSwitchOrTargetName.toLowerCase();

	    type = (String) cmds.get("subscribe:args" + i++);



	    if (type == null) {

		usage();



		return false;

	    }



	    type = type.toLowerCase();



	    if (!validTypes.containsKey(type)) {

		printMessage(resources.getString("cmdline.invtargettype") + " " + type);



		return false;

	    }



	}

      return true;

    }

    boolean isPatchSubscribeArgsValid(Hashtable cmds) {

	int i = 0;

        String arg;

        String name = null;

        String type = null;

        boolean isModifyPolicy = false;



        //arg0

        if("-modify".equals((String) cmds.get("patchsubscribe:args" + i++))) {

        } else {

            // Shifting the index backwards since -modify is not found

            i--;

        }

        // arg0 if -modify is not entered by the user

        String dnSwitchOrTargetName = ((String) cmds.get("patchsubscribe:args" + i++)).toLowerCase();



	if ("-dn".equals(dnSwitchOrTargetName)) {

                // arg2 is name and arg3 is type if -modify is not given

                name = (String) cmds.get("patchsubscribe:args" + i++);

                type = (String) cmds.get("patchsubscribe:args" + i++);



                if ("-type".equals(type)) {

                    // arg4 is type if -modify is not given

                    type = (String) cmds.get("patchsubscribe:args" + i++);



                    if (type == null) {

                        usage();



                        return false;

                    }



                    type = type.toLowerCase();



                    if (!validTypes.containsKey(type)) {

                        printMessage(resources.getString("cmdline.invtargettype") + " " + type);



                        return false;

                    }



                }

            } else {

                // create the props file based on the name and type args

                // first validate the type

                name = dnSwitchOrTargetName.toLowerCase();

                type = (String) cmds.get("patchsubscribe:args" + i++);



                if (type == null) {

                    usage();



                    return false;

                }



                type = type.toLowerCase();



                if (!validTypes.containsKey(type)) {

                    printMessage(resources.getString("cmdline.invtargettype") + " " + type);



                    return false;

                }

            }

	return true;

    }

    boolean isRemedySubscribeArgsValid(Hashtable cmds) {
        int i = 0;
        String name = null;
        String type = null;

        //arg0
        if("-modify".equals((String) cmds.get("remedysubscribe:args" + i++))) {

        } else {
            // Shifting the index backwards since -modify is not found
            i--;
        }

        // arg0 if -modify is not entered by the user
        String dnSwitchOrTargetName = ((String) cmds.get("remedysubscribe:args" + i++)).toLowerCase();
	    if ("-dn".equals(dnSwitchOrTargetName)) {
            // arg2 is name and arg3 is type if -modify is not given
            name = (String) cmds.get("remedysubscribe:args" + i++);
            type = (String) cmds.get("remedysubscribe:args" + i++);
            if ("-type".equals(type)) {
                // arg4 is type if -modify is not given
                type = (String) cmds.get("remedysubscribe:args" + i++);
                if (type == null) {
                    usage();
                    return false;
                }
                type = type.toLowerCase();
                if (!validTypes.containsKey(type)) {
                    printMessage(resources.getString("cmdline.invtargettype") + " " + type);
                    return false;
                }
            }
        } else {
            // create the props file based on the name and type args
            // first validate the type
            name = dnSwitchOrTargetName.toLowerCase();
            type = (String) cmds.get("remedysubscribe:args" + i++);

            if (type == null) {
                usage();
                return false;
            }
            type = type.toLowerCase();
            if (!validTypes.containsKey(type)) {
                printMessage(resources.getString("cmdline.invtargettype") + " " + type);
                return false;
            }
        }
	    return true;
    }

    boolean isChangeorderArgsValid(Hashtable cmds) {

	 int i = 0;

	String  name = null;

	String  type = null;

	 // arg0 if -modify is not entered by the user

        String dnSwitchOrTargetName = ((String) cmds.get("changeorder:args" + i++)).toLowerCase();



	 if ("-dn".equals(dnSwitchOrTargetName)) {

                // arg2 is name and arg3 is type if -modify is not given

              name = (String) cmds.get("changeorder:args" + i++);

              type = (String) cmds.get("changeorder:args" + i++);



                if ("-type".equals(type)) {

                    // arg4 is type if -modify is not given

                    type = (String) cmds.get("changeorder:args" + i++);



                    if (type == null) {

                        usage();



                        return false;

                    }



                    type = type.toLowerCase();



                    if (!validTypes.containsKey(type)) {

                        printMessage(resources.getString("cmdline.invtargettype") + " " + type);



                        return false;

                    }



                }

            } else {

                // create the props file based on the name and type args

                // first validate the type

                name = dnSwitchOrTargetName.toLowerCase();

                type = (String) cmds.get("changeorder:args" + i++);



                if (type == null) {

                    usage();



                    return false;

                }



                type = type.toLowerCase();



                if (!validTypes.containsKey(type)) {

                    printMessage(resources.getString("cmdline.invtargettype") + " " + type);



                    return false;

                }

            }

	return true;

    }

    boolean isListPolicyValidate(Hashtable cmds) {

   	String arg = (String) cmds.get("list:args");



	if ("list:noarg".equals(arg)) {

            return true;

	} else if ("-dn".equals(cmds.get("list:args0"))) {

	    String dn = (String) cmds.get("list:args1");

	    String type = (String) cmds.get("list:args2");



    	if ("-type".equals(type)) {

	    type = (String) cmds.get("list:args3");



	    if (type == null) {

		    usage();



		    return false;

	    }

	    type = type.toLowerCase();



	    if (!validTypes.containsKey(type)) {

		    printMessage(resources.getString("cmdline.invtargettype") + " " + type);



		    return false;

	    }

    	}

    	} else if ("-channel".equals(cmds.get("list:args0"))) {

            String channelURL = (String) cmds.get("list:args1");



            if (channelURL == null) {

                printMessage(resources.getString("cmdline.provideurl"));

                return false;

            }



            try {

                // creating URL object to catch if the url provided is in wrong format

                new URL(channelURL);

                return !(checkSpecialChar(channelURL));

            } catch(MalformedURLException me) {

                printMessage(resources.getString("cmdline.invpackurl") + " " + channelURL);

                return false;

            }

        }else if ("-cascade".equals(cmds.get("list:args0"))) {

        	return true;

    	} else {

    		if (((String)cmds.get("list:args0")).endsWith(SUBSCRIPTION_EXT)) {



    		} else {

		  String fname = (String) cmds.get("list:args0");

		  String type = (String) cmds.get("list:args1");



		  if (type == null) {

			  usage();



		      return false;

		  }

		  type = type.toLowerCase();

		  if (!validTypes.containsKey(type)) {

		      printMessage(resources.getString("cmdline.invtargettype") + " " + type);



		      return false;

		  }

		}

	}

	return true;

    }

    boolean isConfigSetValidate(Hashtable cmds) {

	String    key = (String) cmds.get("configSet:args0");

	if (key == null) {

		usage();

		return false;

	}

	return true;

    }

    boolean isLdapqcConfigSetValid(Hashtable cmds) {



 	boolean createFlag = "-create".equals(cmds.get("ldapqc:args0"));

        boolean modifyFlag = "-modify".equals(cmds.get("ldapqc:args0"));

        boolean deleteFlag = "-delete".equals(cmds.get("ldapqc:args0"));

        boolean previewFlag = "-preview".equals(cmds.get("ldapqc:args0"));

        boolean listFlag = "-list".equals(cmds.get("ldapqc:args0"));

        boolean refreshFlag = "-refresh".equals(cmds.get("ldapqc:args0"));

        boolean configFlag = "-config".equals(cmds.get("ldapqc:args0"));

        boolean removeTaskFlag = "-removetask".equals(cmds.get("ldapqc:args0"));

        boolean listTaskFlag = "-listtask".equals(cmds.get("ldapqc:args0"));

        boolean addTaskFlag = "-addtask".equals(cmds.get("ldapqc:args0"));



	 if(configFlag) {

	   String    key = (String) cmds.get("ldapqc:args1");

	    if (key == null) {

		    usage();

		    return false;

	    }

	 } else if(createFlag) {

            return true;

	 }  else if(modifyFlag) {

            return true;

	 }  else if(deleteFlag) {

	     boolean preview = (cmds.containsValue("-query") || cmds.containsValue("-schedule") || cmds.containsValue("-searchBase") || cmds.containsValue("-filter") );

	    if(preview) {

		 usage();

		 return false;

	    }

	 } else  if(previewFlag) {

	       boolean previewExisting =cmds.containsValue("-cname");

	       boolean preview = (cmds.containsValue("-query") || cmds.containsValue("-schedule") || cmds.containsValue("-searchBase") || cmds.containsValue("-filter") );

		if(previewExisting) {

			 if(preview) {

			     usage();

			     return false;

			 }

		} else {

		    if(cmds.containsValue("-schedule")) {

			    usage();

			    return false;

		    }

		}

	 } else if(listFlag) {

		boolean preview = (cmds.containsValue("-query") || cmds.containsValue("-schedule") || cmds.containsValue("-searchBase") || cmds.containsValue("-filter") );

		if(preview) {

		     usage();

		     return false;

		}

	 } else if(refreshFlag) {

	     boolean necessaryParam = cmds.containsValue("-cname");

	     boolean unwantedParam = (cmds.containsValue("-query") || cmds.containsValue("-schedule") || cmds.containsValue("-searchBase") || cmds.containsValue("-filter") );

	     if(necessaryParam) {

		  if(unwantedParam) {

		      usage();

		      return false;

		  }

	     }

	 } else if(removeTaskFlag) {

         boolean preview = (cmds.containsValue("-query") || cmds.containsValue("-schedule") || cmds.containsValue("-searchBase") || cmds.containsValue("-filter"));

           if (preview) {

              usage();

              return true;

           }

	 } else if(addTaskFlag) {

         boolean preview = (cmds.containsValue("-query") || cmds.containsValue("-schedule") || cmds.containsValue("-searchBase") || cmds.containsValue("-filter"));

         if (preview) {

            usage();

            return true;

         }

     } else if(listTaskFlag) {



                boolean isAll ="-all".equals(cmds.get("ldapqc:args1"));

	     	if(!isAll) {

		    usage();

		    return false;

                }

	 } else {

	     usage();

	     return false;

	 }

	return true;

    }

    boolean isDeleteValidate(Hashtable cmds) {

	boolean allflag = "-all".equals(cmds.get("delete:args0"));

	boolean cascadeflag = "-cascade".equals(cmds.get("delete:args0"));

	boolean dnflag = "-dn".equals(cmds.get("delete:args0"));



	   if (dnflag) {

                String dn = (String) cmds.get("delete:args1");

                String type = (String) cmds.get("delete:args2");



                if ("-type".equals(type)) {

                    type = (String) cmds.get("delete:args3");



                    if (type == null) {

                        usage();



                        return false;

                    }



                    type = type.toLowerCase();



                    if (!validTypes.containsKey(type)) {

                        printMessage(resources.getString("cmdline.invtargettype") + " " + type);



                        return false;

                    }

                }

            } else if (allflag) {

                   return true;

	    } else if (cascadeflag) {

                   return true;

	    } else {

		  if (!((String) cmds.get("delete:args0")).endsWith(SUBSCRIPTION_EXT)) {

		    // delete subscription objects based on 'targetname' and 'targettype' syntax

                    String fname = (String) cmds.get("delete:args0");

                    String type = (String) cmds.get("delete:args1");



                    if (type == null) {

                        usage();



                        return false;

                    }



                    type = type.toLowerCase();



                    if (!validTypes.containsKey(type)) {

                        printMessage(resources.getString("cmdline.invtargettype") + " " + type);



                        return false;

                    }

	     }

	  }

       return true;

    }

    /**

     * Set the client cert password.

     *

     * @param password Password for client certification during publish

     *

     * @return REMIND

     */

    private boolean certPassword(String password)

			throws SystemException {



    	checkPrimaryAdminRole();



        Properties systemProps = System.getProperties();

        systemProps.put("channel.clientcertpw", password);



        return false;

    }

   //Step3:  Creating and running the appropriate command objects with the valid args.

    private boolean subscribe(Hashtable args)

    throws SystemException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.CREATE_SUBSCRIPTION);

        boolean isFailed = command.execute(args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    //Step3:  Creating and running the appropriate command objects with the valid args.

    private boolean subscribeMulti(Hashtable args)

    throws SystemException {

    ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.CREATE_MULTI_SUBSCRIPTION);

    boolean isFailed = command.execute(args);

    commStr.append(command.getOutputStr());

    if (isFailed) {

        return true;

    }

    return false;

}

    //Step3:  Creating and running the appropriate tuner command objects with the valid args.

    private boolean tunerMulti(Hashtable args) throws SystemException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.CREATE_MULTI_PROPS);

        boolean isFailed = command.execute(args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

        return false;

    }



    private boolean list(Hashtable args) throws SystemException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.LIST_SUBSCRIPTION);

        boolean isFailed = command.execute(args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    private boolean delete(Hashtable args) throws SystemException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.DELETE_SUBSCRIPTION);

        boolean isFailed = command.execute(args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    private boolean ldapqc(Hashtable args) throws SystemException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.LDAPQC);

        boolean isFailed = command.execute(context, args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    private boolean changeorder(Hashtable args) throws SystemException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.PRIORITY_ORDERING);

        boolean isFailed = command.execute(args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    private boolean tuner(Hashtable args) throws SystemException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.SET_TUNER_PROPS);

        boolean isFailed = command.execute(args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    private boolean patchsubscribe(Hashtable args) throws SystemException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.PATCH_SUBSCRIPTION);

        boolean isFailed = command.execute(args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    private boolean remedysubscribe(Hashtable args) throws SystemException {
        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.REMEDY_SUBSCRIPTION);
        boolean isFailed = command.execute(args);
        commStr.append(command.getOutputStr());

        if (isFailed) {
             return true;
         }

    	return false;
     }

    private boolean importFile(String file) throws SystemException, IOException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.IMPORT_FILE);

        boolean isFailed = command.execute(file);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    private boolean importFiles(Hashtable args) throws SystemException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.IMPORT_FILE);

        boolean isFailed = command.execute(args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    private boolean export(String dir) throws SystemException, IOException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.EXPORT_FILE);

        boolean isFailed = command.execute(dir);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }
    private boolean complianceOption(Hashtable complianceOptions) throws SystemException, IOException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.COMPLIANCE_SETTINGS);

        boolean isFailed = command.execute(complianceOptions);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }
    private boolean loadMachines(String file) throws SystemException, IOException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.LOAD_MACHINES);

        boolean isFailed = command.execute(file);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;



    }

    private boolean copyLDAPServersFile(String file) throws SystemException, IOException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.COPY_LDAPSERVERS_FILES);

        boolean isFailed = command.execute(file);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }
    private boolean copyPolicy(Hashtable args) throws SystemException {
    	try {
	        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.COPY_POLICY);
	        boolean isFailed = command.execute(args);

	        commStr.append(command.getOutputStr());

	        if (isFailed) {
	            return true;
	        }
    	} catch(Exception ec) {
    		if(DEBUG5) {
    			ec.printStackTrace();
    		}
    	}
    	return false;
    }

    private boolean publish(String pubUrl, String username, String password) throws SystemException {

    	Hashtable   args = new Hashtable();

	if( pubUrl != null  ) {

    		args.put("publishUrl" , pubUrl);

	}

	if( username != null ) {

    		args.put("username" , username);

	}

	if( password != null ) {

    		args.put("password" , password);

	}

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.PUBLISH);

        boolean isFailed = command.execute(args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    private boolean configSet(Hashtable args) throws SystemException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.CONFIG_SET);

        boolean isFailed = command.execute(args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    private boolean txadminaccess(Hashtable args) throws SystemException {

        ISubscriptionCLICommand command = cmdFactory.getCommand(SubscriptionCLICmdFactory.TX_ADMIN_ACCESS);

        boolean isFailed = command.execute(args);

        commStr.append(command.getOutputStr());

        if (isFailed) {

            return true;

        }

    	return false;

    }

    /* FilenameFilter for .sub files

    *

    * @param dir  directory name where the file resides

    * @param name name of the file

    */

   public boolean accept(File   dir,

                         String name) {

       if (name.endsWith(SUBSCRIPTION_EXT)) {

           return true;

       } else {

           return false;

       }

   }

    // --------------------------Command parser helping methods----------------------------------

   /**

   * REMIND

   *

   * @return REMIND

   */

   public String getUserName() {

	    return cliUser.getName();

   }



   /**

    * REMIND

    *

    */

    public void destroy() {



         if (cliUser != null) {

             cliUser.destroy();

         }

    }

    void checkPrimaryAdminRole() throws SystemException {

		boolean isPrimary = Utils.isPrimaryAdmin(cliUser.getUser());

		if  (!isPrimary) {

		    throw new SubKnownException(ACL_ROLE_NOTPRIMARY);

		}

    }

    void checkAdminRole() throws SystemException {

		boolean isAdmin = Utils.isAdministrator(cliUser.getUser());

		if  (!isAdmin) {

		    throw new SubKnownException(ACL_ROLE_NOTADMIN);

		}

    }

    /**

     * Print the output string and append it for display later

     * @param msg String to be displayed

     */

    private void printMessage(String msg) {

        commStr.append(msg);

        commStr.append("\n");

    }

    // REMIND: Need here and ListPolicy.java class also.

    /**

     * @param ns REMIND

     */

    public void setNameSpace(String ns) {
        cliUser.setProperty(IUser.PROP_NAMESPACE, controller.upgradeNamespace(ns));
    }

    private int getCommandArguments(String[] args, Hashtable cmds, String command, int start) {



		if (args.length > (start + 1)) {

			cmds.put(command, args [start++]);

			int c = 0;

			for (; start != args.length; start++) {



				if (validArgs.containsKey(args [start])) {

					start--;

					break;

				}

				cmds.put(command + ":args" + c, args [start]);

				c++;

			}

		} else {

			cmds.put(command, args [start]);

		}

		return start;

	}



    void readArgSpec(InputStream fout) {

    	  	try {

    	  	    FastInputStream in = new FastInputStream(fout);

    	  	    try {

    	  		String line;

    	  		Set cmds = new TreeSet();

    	  		while ((line = in.readLine()) != null) {

    	  		    if (line.startsWith("#") || "\n".equals(line) || line.length() == 0) {

    	  			continue;

    	  		    }

    	  		    if (line.startsWith("cmdline.usage.hdr")) {

    	  			cmds = new TreeSet();

    	  			cliUsageGrps.put(line, cmds);

    	  		    } else {

    	  			cmds.add(line);

    	  		    }

    	  		}

    	  	    } finally {

    	  		in.close();

    	  	    }

    	  	} catch (IOException e) {

    	  	    e.printStackTrace();

    	  	}

    }

    /**

     * checks if an LDAP connection is available

     *

     * @return true if an LDAP connection is available

     *

     * @throws SystemException REMIND

     */

    private boolean checkConnection()

        throws SystemException {

        return checkConnection(true);

    }

    private boolean checkSpecialChar(String url) {



        // Validating special characters in the channel url

        if (url != null) {

            for (int j=0; j < SPL_CHARS.length; j++) {

                if (url.indexOf(SPL_CHARS[j]) != -1) {

                    // Checking there is no = sign in Package URL.

                    // For example, http://10.10.51.24:5282/Pack==subscribe is not valid.

                    if (SPL_CHARS[j] == '=') {

                        if (url.indexOf(SPL_CHARS[j]) != url.lastIndexOf(SPL_CHARS[j])) {

                            printMessage(resources.getString("cmdline.pkgurlsplchar") + " " + url);

                            return true;

                        }

                    } else {

                        printMessage(resources.getString("cmdline.pkgurlsplchar") + " " + url);

                        return true;

                    }

                }

            }

        }

        return false;

   }

    private boolean checkConnection(boolean checkUpdateContainer)

        throws SystemException {

        // check for an LDAP connection

        if ((cliUser.getSubConn() == null) || (cliUser.getBrowseConn() == null)) {

            printMessage(resources.getString("cmdline.ldapconnect.notconnected"));



            return false;

        }



        if (checkUpdateContainer) {

            // Check that we have ran the -upgrade command

            MergeAllSub.check(cliUser.getSubConn(), controller.getSubBase(), controller.getLDAPVarsMap());

        }



        return true;

    }

    public void usage() {

       printMessage("");

       printMessage(resources.getString("cmdline.usage.header"));

       printMessage("");

	   String cmd;



	   for (Iterator ite = cliUsageGrps.keySet().iterator(); ite.hasNext();) {

	   cmd = (String) ite.next();

	   printHeader(cmd);



	   for (Iterator ite2 = ((Set) cliUsageGrps.get(cmd)).iterator(); ite2.hasNext();) {

	       cmd = (String) ite2.next();

	       printDesc(cmd);

	       printMessage("");

	   }

	   }

       printMessage("");

   }

   public String getOutputStr() {

       if(commStr != null) {

	   return commStr.toString();

       } else {

	   return "";

       }

   }

   private void printHeader(String key) {

	   StringBuffer msg = new StringBuffer(resources.getString(key));

	   int sz = msg.length();

	   msg.append("\n");

	   for (int i = 0; i < sz; i++) {

	       msg.append('-');

	   }

	   msg.append("\n");

	   printMessage(msg.toString());

       }

   private void printDesc(String key) {

	   String tmp;

	   int    i = 0;



	   tmp = key + ".opt";

	   if (resources.getString(tmp) != null) {

	       printDescTop(tmp);

	   } else {

	       while ((resources.getString((tmp + i))) != null) {

		   printDescTop(tmp + i);

		   i++;

	       }

	   }

	   tmp = key + ".descr";

	   if (resources.getString(tmp) != null) {

	       printDescTop(tmp);

	   } else {

	       while ((resources.getString((tmp + i))) != null) {

		   int j = 0;

		   String tmp2 = tmp + i;

		   printDescTop(tmp2);

		   while (( resources.getString(tmp2 + "." + j)) != null) {

		       printDescDetails(tmp2 + "." + j);

		       j++;

		   }

		   i++;

	       }

	   }

       }



       private void printDescTop(String key) {

	   printDescLine(key, "", 70);

       }



       private void printDescDetails(String key) {

	   printDescLine(key, "\t", 60);

       }



       private void printDescLine(String key,

				  String indent,

				  int    linecnt) {

	       String msg = resources.getString(key);

	   BreakIterator boundary = BreakIterator.getLineInstance(Locale.US);

	   boundary.setText(msg);



	   int start = boundary.first();

	   int cnt = 0;

	   int lstart = start;



	   for (int end = boundary.next(); end != BreakIterator.DONE; start = end, end = boundary.next()) {

	       if ((cnt + (end - start)) >= linecnt) {

		   printMessage(indent + msg.substring(lstart, end));

		   cnt    = 0;

		   lstart = end;



		   continue;

	       } else {

		   cnt += (end - start);

	       }

	   }



	   if (lstart < msg.length()) {

	       printMessage(indent + msg.substring(lstart));

	   }

       }

}

