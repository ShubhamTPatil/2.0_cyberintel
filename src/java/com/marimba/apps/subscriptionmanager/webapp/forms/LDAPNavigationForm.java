// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import java.io.*;

import java.net.*;

import javax.servlet.http.HttpServletRequest;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.forms.*;

/**
 * REMIND
 *
 * @author $Author$
 * @version $File$
 */
public class LDAPNavigationForm
    extends ActionForm
    implements IWebAppConstants {
    private String action = ACTION_CREATE;
    private String container = "";
    private String targetName;
    private String targetType;
    private String searchString;
    private String advSearchString;
    private String limitSearch;
    private String advLimitSearch;
    private String searchType;
    private String basicLink;    
    
    // used for duplicate Search box at bottom of page
    private String searchString2;
    private String limitSearch2;
    private String baseURL;

    // for Policy search in Target view    
    private String createDateFrom;
    private String createDateTo;
    private String modifyDateFrom;    
    private String modifyDateTo;
    private String createDateCriteria;
    private String modifyDateCriteria;    
    private boolean orphanPolicy;
    protected String searchQuery;
  	
    
	public String getSearchQuery() {
		return searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public boolean isOrphanPolicy() {
		return orphanPolicy;
	}

	public void setOrphanPolicy(boolean orphanPolicy) {
		this.orphanPolicy = orphanPolicy;
	}

	public String getCreateDateFrom() {
		return createDateFrom;
	}

	public void setCreateDateFrom(String createDateFrom) {
		this.createDateFrom = createDateFrom;
	}

	public String getCreateDateTo() {
		return createDateTo;
	}

	public void setCreateDateTo(String createDateTo) {
		this.createDateTo = createDateTo;
	}

	public String getModifyDateFrom() {
		return modifyDateFrom;
	}

	public void setModifyDateFrom(String modifyDateFrom) {
		this.modifyDateFrom = modifyDateFrom;
	}

	public String getModifyDateTo() {
		return modifyDateTo;
	}

	public void setModifyDateTo(String modifyDateTo) {
		this.modifyDateTo = modifyDateTo;
	}

	public String getCreateDateCriteria() {
		return createDateCriteria;
	}

	public void setCreateDateCriteria(String createDateCriteria) {
		this.createDateCriteria = createDateCriteria;
	}

	public String getModifyDateCriteria() {
		return modifyDateCriteria;
	}

	public void setModifyDateCriteria(String modifyDateCriteria) {
		this.modifyDateCriteria = modifyDateCriteria;
	}

	/**
     * REMIND
     *
     * @param action REMIND
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getAction() {
        return this.action;
    }

    /**
     * REMIND
     *
     * @param container REMIND
     */
    public void setContainer(String container) {
        this.container = container;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getContainer() {
        return this.container;
    }

    /**
     * REMIND
     *
     * @param targetName REMIND
     */
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getTargetName() {
        return this.targetName;
    }

    /**
     * REMIND
     *
     * @param targetType REMIND
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getTargetType() {
        return this.targetType;
    }

    /**
     * REMIND
     *
     * @param searchString REMIND
     */
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSearchString() {
        return this.searchString;
    }
    
    /**
     * REMIND
     *
     * @param advSearchString REMIND
     */
    public void setAdvSearchString(String advSearchString) {
        this.advSearchString = advSearchString;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getAdvSearchString() {
        return this.advSearchString;
    }
    

    /**
     * REMIND
     *
     * @param limitSearch REMIND
     */
    public void setLimitSearch(String limitSearch) {
        this.limitSearch = limitSearch;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getLimitSearch() {
        return this.limitSearch;
    }

    /**
     * REMIND
     *
     * @param advLimitSearch REMIND
     */
    public void setAdvLimitSearch(String advLimitSearch) {
        this.advLimitSearch = advLimitSearch;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getAdvLimitSearch() {
        return this.advLimitSearch;
    }

    /**
     * REMIND
     *
     * @param searchString2 REMIND
     */
    public void setSearchString2(String searchString2) {
        this.searchString2 = searchString2;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSearchString2() {
        return this.searchString2;
    }

    /**
     * REMIND
     *
     * @param limitSearch2 REMIND
     */
    public void setLimitSearch2(String limitSearch2) {
        this.limitSearch2 = limitSearch2;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getLimitSearch2() {
        return this.limitSearch2;
    }

    /**
     * REMIND
     *
     * @param baseURL REMIND
     */
    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getBaseURL() {
        return this.baseURL;
    }
    
    /**
     * REMIND
     *
     * @param searchType REMIND
     */
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getSearchType() {
        return this.searchType;
    }
    
    /**
     * REMIND
     *
     * @param basicLink REMIND
     */
    public void setBasicLink(String basicLink) {
        this.basicLink = basicLink;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    public String getBasicLink() {
        return this.basicLink;
    }

    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset(ActionMapping      mapping,
                      HttpServletRequest request) {
        this.container     = null;
        this.targetName    = null;
        this.targetType    = null;
        this.searchString  = null;
        this.advSearchString = null;
        this.limitSearch   = null;
        this.searchString2 = null;
        this.limitSearch2  = null;
        this.baseURL       = null;
        this.searchType    = null;
        this.basicLink     = null;
        this.advLimitSearch = null;
    }

    /**
     * Validate the properties that have been set from this HTTP request, and return an <code>ActionErrors</code> object that encapsulates any validation
     * errors that have been found.  If no errors are found, return <code>null</code> or an <code>ActionErrors</code> object with no recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     *
     * @return REMIND
     */
    public ActionErrors validate(ActionMapping      mapping,
                                 HttpServletRequest request) {
        return null;
    }
}

