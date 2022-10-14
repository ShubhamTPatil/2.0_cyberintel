package com.marimba.apps.subscriptionmanager.arsystem;

import com.marimba.intf.msf.arsys.IARManager;
import com.marimba.intf.msf.arsys.ITaskContext;
import com.marimba.intf.msf.arsys.IARConstants;
import com.marimba.intf.msf.arsys.ARManagerException;
import com.marimba.intf.msf.*;
/**
 * An utility class to get AR Context from tenant using ARManager
 *
 * @author Vamathevan, Devendra
 * @author Helen Wu
 * @version $Revision$, $Date$
 */

public class ARConnectionManager {

	public synchronized static ITaskContext getARContext(ITenant tenant) throws ARManagerException {
		if(null != tenant) {
			try {
				IARManager arMgr = tenant.getArMgr();
				ITaskContext arContext = (ITaskContext) arMgr.createContext(IARConstants.TYPE_TASK);
				return arContext;
			} catch(Exception ex) {
				throw new ARManagerException("Failed to get AR Context from tenant");
			}
		} else {
			throw new ARManagerException("Failed to get AR Context from tenant");
		}
	}
}
