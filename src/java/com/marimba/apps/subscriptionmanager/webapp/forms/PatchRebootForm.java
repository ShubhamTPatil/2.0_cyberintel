package com.marimba.apps.subscriptionmanager.webapp.forms;



import com.marimba.apps.subscription.common.ISubscriptionConstants;

import com.marimba.apps.subscriptionmanager.intf.IAppConstants;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;

import com.marimba.webapps.intf.IMapProperty;

import org.apache.struts.action.ActionErrors;

import org.apache.struts.action.ActionMapping;

import org.apache.struts.util.MessageResources;



import javax.servlet.http.HttpServletRequest;

import java.util.Locale;



/**

 * Created by IntelliJ IDEA.

 * User: dvamathevan

 * Date: Dec 29, 2003

 * Time: 10:50:02 PM

 * To change this template use Options | File Templates.

 */

public class PatchRebootForm

        extends AbstractForm

        implements ISubscriptionConstants,

        IAppConstants,

        IWebAppConstants,

        IMapProperty {





    /* The distribution form is initialized from the DistAsgInitForm. It must ALWAYS be

     * initialized when entering the distribution assignment page.  This is so that we have

     * no residual distribution bean that is affected by a previous edit.

     *

     */

    public void initialize(MessageResources resources,

                           Locale locale,

                           HttpServletRequest request) {



        props.clear();



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

    public ActionErrors validate(ActionMapping mapping,

                                 HttpServletRequest request) {

        return null;

    }





   	public void reset(ActionMapping actionMapping, HttpServletRequest httpServletRequest) {

        clearCheckedItems();

    }



    public void clearCheckedItems() {

       props.remove("displayAlert");

       props.remove("allowReboot");

    }





    public void clearPagingVars(HttpServletRequest request) {

        request.getSession().

                removeAttribute((String) getValue(SESSION_PERSIST_SELECTED));

        request.getSession()

                .removeAttribute(IWebAppConstants.SESSION_DIST_PAGEPKGS_BEAN);

        clearCheckedItems();

    }



}

