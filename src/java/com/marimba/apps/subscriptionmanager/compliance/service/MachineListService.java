// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$


package com.marimba.apps.subscriptionmanager.compliance.service;

import com.marimba.apps.subscriptionmanager.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.core.*;
import com.marimba.apps.subscriptionmanager.compliance.queue.*;
import com.marimba.apps.subscriptionmanager.compliance.intf.*;
import com.marimba.apps.subscriptionmanager.compliance.query.*;
import com.marimba.apps.subscriptionmanager.compliance.result.ListResult;
import com.marimba.apps.subscriptionmanager.compliance.view.PackageBean;
import com.marimba.apps.subscriptionmanager.compliance.view.MachineBean;
import com.marimba.apps.subscriptionmanager.compliance.view.MachineListBean;
import com.marimba.intf.msf.IUserPrincipal;
import com.marimba.intf.msf.*;
import org.apache.log4j.*;

import java.util.*;

/**
 * Service to get a list of machines.
 *
 * @author  Manoj Kumar
 * @version $Revision$, $Date$
 */

public class MachineListService extends Service implements IAppConstants{

    static final Logger sLogger = Logger.getLogger(MachineListService.class);
    private static boolean isDebug = IAppConstants.DEBUG;

    public MachineListService(ComplianceMain main, IUserPrincipal user, ITenant tenant) {
	    super(main,user, tenant);
    }
    
    /**
     * Get machine list based on compliance level, inventory data only
     */
    public MachineListBean getMachines(String target, String level, int page, boolean recalc, Locale userLocale ) {
	    target = (target == null) ? "" : target.toLowerCase();
	    if("all".equalsIgnoreCase(target)) target = "all_all";
	    MachineListBean ml = new MachineListBean();
	    ml.setError(true);
	
        MachineListQuery _mq = new MachineListQuery(target, level);
        _mq.setUser( user, userLocale);
        _mq.setTenant(tenant);
	    _mq.setCheckinLimit(cfgMgr.getInt(ConfigManager.CFG_CHECKIN_LIMIT));
	    setPageInfo(_mq, page);
	    setCacheInfoForList(_mq);

        /* excuting the query immediately
        instead of adding into QueryQueue to
        avoid please wait screen */

        _mq.executeQuery();

        IComplianceResult _mr = _mq.getResult();
        if(_mr != null && _mr instanceof ListResult) {
            ListResult _lr = (ListResult) _mr;
            if (_lr != null) {
		        ml.setList(_lr.get(page));
		        ml.setCurrentPage(page);
		        // ml.setTotal(_lr.getTotalCount());
		        // ml.setTotalPage(_lr.getTotalPage());
		        ml.setError(false);
            }
        } else {
            ml.setList(new ArrayList());
            ml.setCurrentPage(page);
            // ml.setTotal(_lr.getTotalCount());
            // ml.setTotalPage(_lr.getTotalPage());
            ml.setError(true);
        }
        return ml;
    }
    /**
     * Get site machine list based on compliance level, inventory data only
     */
    public MachineListBean getSiteMachines(String target, String level, int page, boolean recalc, Locale userLocale ) {
	    target = (target == null) ? "" : target.toLowerCase();
	    
	    MachineListBean ml = new MachineListBean();
	    ml.setError(true);
	
        MachineListSiteQuery _mq = new MachineListSiteQuery(target, level);
        _mq.setUser( user, userLocale);
        _mq.setTenant(tenant);
	    _mq.setCheckinLimit(cfgMgr.getInt(ConfigManager.CFG_CHECKIN_LIMIT));
	    setPageInfo(_mq, page);
	    setCacheInfoForList(_mq);

        /* excuting the query immediately
        instead of adding into QueryQueue to
        avoid please wait screen */

        _mq.executeQuery();

        IComplianceResult _mr = _mq.getResult();
        if(_mr != null && _mr instanceof ListResult) {
            ListResult _lr = (ListResult) _mr;
            if (_lr != null) {
		        ml.setList(_lr.get(page));
		        ml.setCurrentPage(page);
		        // ml.setTotal(_lr.getTotalCount());
		        // ml.setTotalPage(_lr.getTotalPage());
		        ml.setError(false);
            }
        } else {
            ml.setList(new ArrayList());
            ml.setCurrentPage(page);
            // ml.setTotal(_lr.getTotalCount());
            // ml.setTotalPage(_lr.getTotalPage());
            ml.setError(true);
        }
        return ml;
    }
}
