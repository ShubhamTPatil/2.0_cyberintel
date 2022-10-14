// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$



package com.marimba.apps.subscriptionmanager.cli.commands;



import java.util.*;

import java.io.File;

import java.io.FileInputStream;

import java.io.BufferedReader;

import java.io.InputStreamReader;



import com.marimba.webapps.intf.SystemException;



/**

 * Subscribe packages with states and schedules to targets.

 *

 * @author      Venkatesh Jeyaraman

 * @version 	$Revision$, $Date$

 */

public class SubscribeMulti extends Subscribe {





    public SubscribeMulti() {

    }

    public boolean run(Hashtable args) throws SystemException {

	boolean isFailed = false;

	isFailed = subscribeMulti(args);

	return isFailed;

    }

    public boolean subscribeMulti(Hashtable args) throws SystemException {

        int i = 0;

        boolean isModifyPolicy = false;



        if ("-modify".equals((String) args.get("subscribe:args" + i++))) {

	        isModifyPolicy = true;

	    } else {

	        // Shifting the index backwards since -modify is not found

	        i--;

	    }

        String targetSourceSwitch = ((String) args.get("subscribe:args" + i++)).toLowerCase();



        try {

            if("-remove".equalsIgnoreCase(targetSourceSwitch)) {

                targetSourceSwitch = ((String) args.get("subscribe:args" + i++)).toLowerCase();

            }

            if ("-targetsource".equalsIgnoreCase(targetSourceSwitch)) {

                String targetsFileName=(String) args.get("subscribe:args" + i--);

                File tgtsrc = new File(targetsFileName);

                if (tgtsrc.exists()) {

                    BufferedReader tgtRdr = new BufferedReader(new InputStreamReader(new FileInputStream(tgtsrc)));

                    String line = null;

                    try {

                         while ((line = tgtRdr.readLine()) != null) {

                              while (line.startsWith("\"")) {
                                  line = line.substring(1, line.length());
                              }

                              while (line.endsWith("\"")) {
                                  line = line.substring(0, line.length()-1);
                              }

                              args.put(("subscribe:args" + i), "-dn");

                              args.put(("subscribe:args" + (i + 1)), line);

                              try {

                                  subscribe(args);

                              } catch (SystemException se) {

                                  //continue assinging policy for the remaining targets

                              }

                         }

                    } finally {

                          tgtRdr.close();

                    }

                 } else {

                    printMessage(resources.getString("cmdline.filenotfound")+" "+targetsFileName);

                    return true;

                }

            }

        } catch(Exception e) {

             e.printStackTrace();

             return true;

        } finally {

            if(sb.getOutputStr().length() > 0) {

                printMessage(sb.getOutputStr());

            }

        }

        return false;

    }

}