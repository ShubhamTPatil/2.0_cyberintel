// Copyright 1997-2005, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.util;

import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.view.MachineBean;
import com.marimba.apps.subscriptionmanager.compliance.view.MachinePackageBean;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Locale;
import java.util.List;
import java.util.Iterator;

import org.apache.struts.util.MessageResources;

/**
 * Provide some useful function for front end
 *
 * @author   Zheng Xia
 * @version  $Revision$
 */
public class WebUtil implements IAppConstants{

    public static String jsEncode(String v) {
	// Encode ' to '' for javascript string
	if (v == null) {
	    return "";
	}

	// Not ', just return the same string
	if (v.indexOf("'") == -1) {
	    return v;
	}

	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < v.length(); i++) {
	    char ch = v.charAt(i);
	    sb.append(ch);

	    // One more if it's single quote
	    if (ch == '\'') {
		sb.append(ch);
	    }
	}
	return sb.toString();
    }

    /**
     * This strips out '.' characters from parameters passed to Report Center.
     * This is to work around a bug in RC where it uses . as a wildcard in sql.
     * Once we move to JDK 1.4, replace this with a RegEx
     * @param toStrip
     * @return
     */
    public static String stripDots(String toStrip) {
        if(null == toStrip) return toStrip;

        
        int len= toStrip.length();
		StringBuffer newStr = new StringBuffer();
		for(int i=0; i<len;i++){
			char c = toStrip.charAt(i);
			if(c=='.')
			newStr.append("\\.");
			 else
			newStr.append(c);
		}
		return newStr.toString();
    }

    public static SimpleDateFormat getComplianceDateTimeFormat() {
        return (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance( ComplianceConstants.INPUT_COMPLIANCE_DATESTYLE, ComplianceConstants.INPUT_COMPLIANCE_DATESTYLE );
    }

    public static SimpleDateFormat getComplianceDateFormat() {
        return new SimpleDateFormat("yyyy.MM.dd 'at' hh:mm:ss a");
    }

    public static SimpleDateFormat getComplianceDateFormat( Locale locale ) {
        if( locale == null ){
            return (SimpleDateFormat)SimpleDateFormat.getDateInstance( ComplianceConstants.INPUT_COMPLIANCE_DATESTYLE );
        } else {
            return (SimpleDateFormat)SimpleDateFormat.getDateInstance( ComplianceConstants.INPUT_COMPLIANCE_DATESTYLE, locale );
        }
    }

    public static SimpleDateFormat getComplianceDateTimeFormat( Locale locale ) {
        if( locale == null ){
            return getComplianceDateTimeFormat();
        } else {
            return (SimpleDateFormat)SimpleDateFormat.getDateTimeInstance( ComplianceConstants.INPUT_COMPLIANCE_DATESTYLE, ComplianceConstants.INPUT_COMPLIANCE_DATESTYLE, locale );
        }
    }

    public static String getStackTrace( Exception exception ){
        StackTraceElement traceElements[] = exception.getStackTrace();
        StringBuffer stackTrace = new StringBuffer();
        stackTrace.append( exception.getMessage() );
        // Maximum two frames of error message set display
        int frames = 2;
        if( traceElements.length < frames ){
            frames = traceElements.length;
        }
        for( int indx = 0; indx < frames; indx++ ){
            stackTrace.append( traceElements[indx].toString() );
        }
        return stackTrace.toString(); 
    }

    public static void checkEndpointState( List machineList, HttpServletRequest request, MessageResources resources ){
        Iterator iterator = machineList.iterator();
        Object bean = null;
        String mainState = null;
        MachineBean machineBean = null;
        MachinePackageBean machinePackageBean = null;
        while( iterator.hasNext() ){
            bean = iterator.next();

            if( bean instanceof MachineBean ){
                // For Non-Compliant results in OverallCompliance
                machineBean = ( MachineBean )bean;
                // checking whether state reported by inventory is present in AppResources
                if( !resources.isPresent( request.getLocale(), "page.global.compliance."+machineBean.getEndpointState() ) ){
                    mainState = getMainState( machineBean.getEndpointState() );
                    // checking whether main state reported by inventory is present in AppResources
                    if( resources.isPresent( request.getLocale(), "page.global.compliance."+mainState ) ){
                        if( DEBUG5 ){
                            System.out.println( "Considering state "+mainState+" for "+machineBean.getEndpointState() );
                        }
                        /* considering main state in the absence of mainstate(substate) in AppReources
                         To do: add main states aborted, resume-pending states in AppResources */
                        machineBean.setEndpointState( mainState );
                    } else {
                        // substituting null for un-available main state
                        if( DEBUG5 ){
                            System.out.println( "Substituting state N/A for "+machineBean.getEndpointState() );
                        }
                        machineBean.setEndpointState( null );
                    }
                }
            } else if( bean instanceof MachinePackageBean ){
                // For Non-Compliant results in Compliance reports
                machinePackageBean = ( MachinePackageBean )bean;
                // checking whether state reported by inventory is present in AppResources
                if( !resources.isPresent( request.getLocale(), "page.global.compliance."+machinePackageBean.getEndPointState() ) ){
                    mainState = getMainState( machinePackageBean.getEndPointState() );
                    // checking whether main state reported by inventory is present in AppResources
                    if( resources.isPresent( request.getLocale(), "page.global.compliance."+mainState ) ){
                        if( DEBUG5 ){
                            System.out.println( "Considering state "+mainState+" for "+machinePackageBean.getEndPointState() );
                        }
                        /* considering main state in the absence of mainstate(substate) in AppReources
                         To do: add main states aborted, resume-pending states in AppResources*/
                        machinePackageBean.setEndPointState( mainState );
                    } else {
                        if( DEBUG5 ){
                            System.out.println( "Substituting state N/A for "+machinePackageBean.getEndPointState() );
                        }
                        // substituting null for un-available main state
                        machinePackageBean.setEndPointState( null );
                    }
                }
            }
        }
    }

    private static String getMainState( String state ){
        if( state != null ){
            int subStateStart = 0;
            if( ( subStateStart = state.indexOf( '(' ) ) != -1 ){
                return state.substring( 0, subStateStart );
            } else {
                return state;
            }
        }
        return state;
    }
}
