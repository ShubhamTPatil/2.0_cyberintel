// Copyright 1997-2009, BMC Software Inc. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.*;
import java.io.*;

import com.marimba.webapps.intf.SystemException;

/**
 * Assign tuner props and custom channel props to targets.
 *
 * @author      Selvaraj Jegatheesan
 * @version 	$Revision$, $Date$
 */
public class TunerPropsMulti extends TunerProps {

    public TunerPropsMulti() {}

    public boolean run(Hashtable args) throws SystemException {
	    boolean isFailed = false;
	    isFailed = TunerPropsMulti(args);
	    return isFailed;
    }

    public boolean TunerPropsMulti(Hashtable args) throws SystemException {

        int i = 0;
        boolean isModifyProperty = false;

        if ("-modify".equals((String) args.get("tuner:args" + i++))) {
	        isModifyProperty = true;
	    } else {
	        // Shifting the index backwards since -modify is not found
	        i--;
	    }

        String tunerpropSourceSwitch = ((String) args.get("tuner:args" + i++)).toLowerCase();
        BufferedReader propRdr = null;

        try {

            if("-remove".equalsIgnoreCase(tunerpropSourceSwitch)) {
                tunerpropSourceSwitch = ((String) args.get("tuner:args" + i++)).toLowerCase();
            }

            if ("-targetsource".equalsIgnoreCase(tunerpropSourceSwitch)) {

                String tunerpropsFileName=(String) args.get("tuner:args" + i--);
                File propsrc = new File(tunerpropsFileName);

                if (propsrc.exists()) {
                    propRdr = new BufferedReader(new InputStreamReader(new FileInputStream(propsrc)));
                    String line = null;

                    while ((line = propRdr.readLine()) != null) {

                        // removing quote symbols if any at the beginning or at the end of the line.
                        while (line.startsWith("\"")) {
                            line = line.substring(1, line.length());
                        }
                        while (line.endsWith("\"")) {
                            line = line.substring(0, line.length()-1);
                        }
                        args.put(("tuner:args" + i), "-dn");
                        args.put(("tuner:args" + (i + 1)), line);
                        tuner(args);
                    }
                 } else {
                    printMessage(resources.getString("cmdline.filenotfound")+" "+tunerpropsFileName);
                    return true;
                }
            }
        } catch (SystemException se) {
            se.printStackTrace();
            //continue assigning properties for the remaining targets
        } catch(Exception e) {
             e.printStackTrace();
             return true;
        } finally {
            if (propRdr != null) {
                try {
                    propRdr.close();
                } catch (IOException exp) {}
            }
            if(sb.getOutputStr().length() > 0) {
                printMessage(sb.getOutputStr());
            }
        }
        return false;
    }
}