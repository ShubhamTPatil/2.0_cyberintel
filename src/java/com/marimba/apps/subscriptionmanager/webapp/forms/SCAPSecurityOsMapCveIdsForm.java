// Copyright 2021, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;
import com.marimba.webapps.tools.util.KnownActionError;
import com.marimba.apps.subscriptionmanager.webapp.util.*;
import com.marimba.apps.subscriptionmanager.webapp.system.OsBean;
import com.marimba.tools.config.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * SCAPSecurityOsMapCveIdsForm
 *
 * @author Nandakumar Sankaralingam
 * @version $Revision$, $Date$
 */

public class SCAPSecurityOsMapCveIdsForm extends AbstractForm {
	String profileName;
	String profileDesc;
	String osName;
    String cveIds;
	String action;
	String create = "false";
	List<OsBean> osList = new ArrayList<OsBean>();
    String previousOsIndex;

    public String getProfileName() {
    return profileName;
}

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public String getProfileDesc() {
        return profileDesc;
    }

    public void setProfileDesc(String profileDesc) {
        this.profileDesc = profileDesc;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getCveIds() {
        return cveIds;
    }

    public void setCveIds(String cveIds) {
        this.cveIds = cveIds;
    }

    public List<OsBean> getOsList() {
        return osList;
    }

    public void setOsList(List<OsBean> osList) {
        this.osList = osList;
    }

    public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}



	public void initialize() {
        super.initialize();
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        setProfileName("");
        setProfileDesc("");
        setOsName("");
        setCveIds("");
    }

    public void reset() {
        setProfileName("");
        setProfileDesc("");
        setPreviousOsIndex("");
        setCveIds("");
    }

    public String getCreate() {
		return create;
	}
	public void setCreate(String create) {
		this.create = create;
	}

    public String getPreviousOsIndex() {
    return previousOsIndex;
}

    public void setPreviousOsIndex(String previousOsIndex) {
        this.previousOsIndex = previousOsIndex;
    }

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (("save".equals(request.getParameter("action"))) || ("apply".equals(request.getParameter("action")))) {
           // do nothing
        }
        return errors;
    }

}

