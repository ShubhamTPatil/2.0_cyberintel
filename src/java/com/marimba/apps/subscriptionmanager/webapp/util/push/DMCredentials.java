// Copyright 1997-2004, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.util.push;

/**
 * Created by IntelliJ IDEA.
 * @author  Anantha Kasetty
 * @version $Revision$, $Date$
 * $File$
 * 
 */


import java.util.*;

import com.marimba.apps.sdm.intf.simplified.*;
import com.marimba.intf.target.*;

public class DMCredentials implements ICredentials{

    HashMap credentials;

    private DMCredentials(){
        credentials = new HashMap(5);
    }

    public static ICredentials getInstance(String user, String password){
        DMCredentials cred = new DMCredentials();
        cred.addCredentials(user,password);
        return cred;
    }

    public String [] getModules(){
        String [] modules = {IDMContext.TUNER_MODULE};
        return modules;
    }

    public void addCredentials(String user, String password){
        addCredentials(IDMContext.TUNER_MODULE,user,password);
    }

    void addCredentials(String module, String user, String password){
        List list = (List)credentials.get(module);
        if (list == null){
            list = new Vector();
            list.add(user);
            if (password == null){
                password = "";
            }
            list.add(password);
            credentials.put(module,list);
        } else {
            list.add(user);
            if (password == null){
                password = "";
            }
            list.add(password);
        }
    }

    public String [] getCredentials(String module){
        List list = (List)credentials.get(module);
        if (list == null){
            return new String[0];
        } else {
            return (String[])list.toArray(new String [0]);
        }
    }
}
