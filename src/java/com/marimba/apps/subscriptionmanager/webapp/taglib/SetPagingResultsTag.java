// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.taglib;

import org.apache.struts.util.RequestUtils;

import java.io.PrintStream;

import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.marimba.apps.subscriptionmanager.intf.IListProcessor;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.GenericPagingBean;

import com.marimba.webapps.intf.SystemException;

import com.marimba.webapps.tools.util.WebAppUtils;

/**
 * Custom tag that sets the requested page of results into the session. It also sets this new startIndex to the paging bean which is stored in the form.  The
 * sublist of the results can be passed through a IListProcessor class to do whatever processing needed.
 *
 * @author Michele Lin
 * @version 1.11, 07/31/2002
 */
public class SetPagingResultsTag extends TagSupport implements IWebAppConstants {
    final static boolean DEBUG = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;

    private static boolean sIsDebugEnabled = DEBUG;
    private static PrintStream sDebugPrintStream = System.out;

    private String formName;
    private String resultsName;
    private String property;
    private String beanName;
    private String displayResultsName;
    private String listProcessor;

    protected static void debugEntered(String inMethodNameString) {
        if (sIsDebugEnabled) {
            sDebugPrintStream.println(SetPagingResultsTag.class.getName() + ": " + inMethodNameString + ": entered");
        }
    }

    protected static void debugLeaving(String inMethodNameString, Object inMessageObject) {
        if (sIsDebugEnabled) {
            sDebugPrintStream.println(SetPagingResultsTag.class.getName() + ": " + inMethodNameString + ": leaving: " + inMessageObject);
        }
    }

    protected static void debugLeaving(String inMethodNameString) {
        if (sIsDebugEnabled) {
            sDebugPrintStream.println(SetPagingResultsTag.class.getName() + ": " + inMethodNameString + ": leaving: <void>");
        }
    }

    protected static void debugMessage(String inMethodNameString, Object inMessageObject) {
        if (sIsDebugEnabled) {
            sDebugPrintStream.println(SetPagingResultsTag.class.getName() + ": " + inMethodNameString + ": " + inMessageObject);
        }
    }

    /**
     * REMIND
     *
     * @param formName REMIND
     */
    public void setFormName(String formName) {
        this.formName = formName;
    }

    /**
     * REMIND
     *
     * @param resultsName REMIND
     */
    public void setResultsName(String resultsName) {
        this.resultsName = resultsName;
    }

    /**
     * REMIND
     *
     * @param property REMIND
     */
    public void setProperty(String property) {
        this.property = property;
    }

    /**
     * REMIND
     *
     * @param beanName REMIND
     */
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }

    /**
     * REMIND
     *
     * @param displayResultsName REMIND
     */
    public void setDisplayResultsName(String displayResultsName) {
        this.displayResultsName = displayResultsName;
    }

    /**
     * REMIND
     *
     * @param proc REMIND
     */
    public void setListProcessor(String proc) {
        this.listProcessor = proc;
    }

    /**
     * Determines which page of result to display and sets this to the session. The following are tag attributes formName- the name of the form that is storing
     * the paging bean beanName- the name of the generic paging bean stored by the form resultsName- the name of the session var of bean that contains the
     * full result set Page is a request parameter page- which page to display: 'prev', 'next', or 0-indexed page displayResultsName- the name of the session
     * variable to set the results to be displayed
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doStartTag() throws JspException {
        final String dCurrentMethod = "doStartTag()";
        debugEntered(dCurrentMethod);

        int ret = SKIP_BODY;

        debugMessage(dCurrentMethod, "formName=" + formName + "; beanName=" + beanName + "; resultsName=" + resultsName + "; property=" + property + "; displayResultsName=" + displayResultsName + "; listProcessor=" + listProcessor);

        HttpServletRequest req = (HttpServletRequest) pageContext.getRequest();
        HttpSession session = pageContext.getSession();

        // REMIND: mlin: should store/retrieve the paging bean in the form tempoarily get/store the bean in the session
        GenericPagingBean pageBean = (GenericPagingBean) session.getAttribute(beanName);

        debugMessage(dCurrentMethod, "pageBean=" + pageBean);

        int startIndex = -1;
        int endIndex = -1;
        int total = 0;
        int countPerPage = DEFAULT_GENPAGING_COUNT_PER_PAGE;

        if ((pageBean == null) || (pageBean.getResults() == null)) {
            debugMessage(dCurrentMethod, "init new set of results");

            pageBean = new GenericPagingBean();

            // get the results vector that was stored for us in the session in most cases, this is page_gen_rs
            java.util.List resultsList = getResultsList();

            if (resultsList != null) {
                pageBean.setResults(resultsList);
                total = resultsList.size();

                debugMessage(dCurrentMethod, "total=" + total);
                debugMessage(dCurrentMethod, "resultsList=" + resultsList);

                // copy the results from the bean/session to the paging bean
                pageBean.setTotal(total);
                pageBean.setCountPerPage(countPerPage);

                // for a new result set, display from index 0
                startIndex = 0;
            } else {
                // zero out paging bean for display
                pageBean.setTotal(0);
                pageBean.setCountPerPage(countPerPage);

                // leave startIndex set to -1
            }
        } else {
            // Asked by an action to reset the results from the specified bean.  For example, the results listing is being resorted
            // due to a previous action. We do not want to just reset the whole paging bean because that resets the starting index

    	    boolean resetResults = "true".equals((String) session.getAttribute(SESSION_PERSIST_RESETRESULTS));
	        if (resetResults) {
		        java.util.List resultsList = getResultsList();
		        if (resultsList != null) {
                    pageBean = new GenericPagingBean();
		            pageBean.setResults(resultsList);
                    pageBean.setStartIndex(0);
                    pageBean.setTotal(resultsList.size());
		        }
                pageBean.setCountPerPage(countPerPage);
		        session.removeAttribute(SESSION_PERSIST_RESETRESULTS);
	        }
	    
            debugMessage(dCurrentMethod, "paging through results");

            String page = req.getParameter("page");

            debugMessage(dCurrentMethod, "page=" + page);

            startIndex = pageBean.getStartIndex();
            total = pageBean.getTotal();

            // REMIND: mlin: in the future, countPerPage will be configurable
            countPerPage = pageBean.getCountPerPage();

            // calculate the startIndex of the page we want
            if ("prev".equals(page)) {
                debugMessage(dCurrentMethod, "previous page requested");

                startIndex = startIndex - countPerPage;

                if (startIndex < 0) {
                    // remind mlin: throw GUI exception that there is no previous page
                }
            } else if ("next".equals(page)) {
                debugMessage(dCurrentMethod, "next page requested");

                startIndex = startIndex + countPerPage;

                if (startIndex > total) {
                    // REMIND: mlin: throw GUI exception that there is no next page
                }
            } else if ("current".equals(page)) {
                debugMessage(dCurrentMethod, "current == current");

                // the initial value of startIndex is the current value
            } else {
                debugMessage(dCurrentMethod, "numbered page requested");

                // page represents the 0-indexed page number we want to display
                if ((page != null) && !"".equals(page)) {
                    try {
                        int pageNum = Integer.parseInt(page.trim());
                        startIndex = pageNum * countPerPage;

                        if ((startIndex < 0) || (startIndex > total)) {
                            // remind mlin: throw bad page requested error
                        }
                    } catch (NumberFormatException nfe) {
                        // remind mlin: throw internal error that there was a bad number format
                    }
                } else {
                    // remind mlin: throw internal error that page requested was blank
                }
            }
        }

        // figure out what session var name to set the display results
        String resultsSessionVar = SESSION_DISPLAY_RS;

        debugMessage(dCurrentMethod, "resultsSessionVar=" + resultsSessionVar);

        // resultsName is an optional attribute to this tag
        if (displayResultsName != null) {
            resultsSessionVar = displayResultsName;
        }

        if (startIndex >= 0) {
            // calcluate the endIndex of the page we want
            endIndex = startIndex + countPerPage;
            endIndex = (endIndex > total) ? total : endIndex;

            debugMessage(dCurrentMethod, "startIndex=" + startIndex + "; endIndex=" + endIndex + "; total=" + total);

            // get this sub-set of results that will be displayed to the request
            List displayList = (pageBean.getResults()).subList(startIndex, endIndex);
            if (listProcessor != null) {
                IListProcessor processor = retriveListProcessorFromSession();

                try {
                    if (processor != null) {
                        processor.process(displayList, req, pageContext.getServletContext());
                    }
                } catch (SystemException se) {
                    WebAppUtils.saveTagException(pageContext, se);
                }
            }

            if (DEBUG) {
                debugMessage(dCurrentMethod, "displayList=<multiple lines below>");

                for (int i = 0; i < displayList.size(); i++) {
                    debugMessage(dCurrentMethod, "displayList.get(" + i + ")=" + displayList.get(i));
                }

                debugMessage(dCurrentMethod, "displayList: <EOList>");
            }

            // set this sub-set to be displayed to the session
            req.setAttribute(resultsSessionVar, displayList);
        } else {
            // else the result set is empty
            req.removeAttribute(resultsSessionVar);
        }

        // set the new startIndex value in the paging bean
        pageBean.setStartIndex(startIndex);

        // set the new startIndex value in the paging bean
        pageBean.setEndIndex(endIndex);

        // REMIND: mlin: store bean in form
        session.setAttribute(beanName, pageBean);
        debugMessage(dCurrentMethod, "setting session attribute; name=" + beanName + "; pageBean=" + pageBean);

        debugLeaving(dCurrentMethod, "ret=" + ret);

        return ret;
    }

    /**
     * Process the end of this tag.  The default implementation does nothing.
     *
     * @return REMIND
     *
     * @exception JspException if a JSP exception has occurred
     */
    public int doEndTag() throws JspException {
        final String dCurrentMethod = "doEndTag()";
        debugEntered(dCurrentMethod);

        int ret = EVAL_PAGE;

        debugLeaving(dCurrentMethod, "ret=" + ret);

        return ret;
    }

    public String getListProcessor() {
        return this.listProcessor;
    }

    /**
     * REMIND
     *
     * @return REMIND
     */
    private IListProcessor retriveListProcessorFromSession() {
        ServletContext sc = pageContext.getServletContext();

        return (IListProcessor) sc.getAttribute(listProcessor);
    }

    private java.util.List getResultsList() throws JspException {
        if ((property == null) || (property.length() == 0)) {
            debugMessage("getResultsList", "getting results from session");
            return (java.util.List) RequestUtils.lookup(pageContext, resultsName, null);
        } else {
            debugMessage("getResultsList", "getting results from bean");
            return (java.util.List) RequestUtils.lookup(pageContext, resultsName, property, null);
        }
    }
}
