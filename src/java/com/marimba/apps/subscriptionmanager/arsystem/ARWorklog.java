// Copyright 1997-2005, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionmanager.arsystem;

import com.marimba.intf.msf.arsys.ARManagerException;
import com.marimba.intf.msf.arsys.IARConstants;
import com.marimba.intf.msf.ITenant;
import com.marimba.webapps.intf.SystemException;
import com.marimba.apps.subscriptionmanager.intf.IARTaskConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import org.w3c.dom.Element;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.*;

/**
 * File extracts/updates channel information from the webservice XML Element
 *
 * @author Devendra Vamathevan
 * @version 7.0.0.0 08/21/2005
 */

public class ARWorklog {
	private CharArrayWriter charArrayWriter;
	private Map request;
    private String userName;
    private ITenant tenant;

	public ARWorklog(Map request, ITenant tenant) {
		this.request = request;
		this.tenant = tenant;
		charArrayWriter = new CharArrayWriter(1024);
        userName = (String)request.get(IARTaskConstants.AR_USER);
        writeUser(userName);
	}

	public void write(String msg) throws SystemException {
		try {
			charArrayWriter.write(msg);
			//todo put EOLN CONSTANT here if needed
			//charArrayWriter.write
		} catch (IOException e) {
            ARUtils.debug("ARWorkLog: Error in Writing");
			throw new SystemException(e, "write error");
		}
	}

	public void close(String status) throws SystemException {

		Element e = null;
		try {
			// String status, String summary, String message, String filename, InputStream in)
			e = ARConnectionManager.getARContext(this.tenant).updateTask(request,
                    IARConstants.TMS_RETURN_CODE_SUCCESS,
			        status, charArrayWriter.toString());
		} catch (ARManagerException err) {
			StringBuffer rootcause = new StringBuffer(255);
				if (err.getMessage() != null) {
					rootcause.append(err.getMessage());
					if ( err.getRootCause() != null ) {
						rootcause.append(". ").append(err.getRootCause().getMessage());
					}
				}

				throw new SystemException(IErrorConstants.AR_CONNECTION_ERROR, rootcause.toString());
		}
        ARUtils.debug("ARWorkLog: Saved WorkLog Info: "+charArrayWriter.toString());
        ARUtils.debug("ARWorkLog: Query Task Reuturned: "+ e);
		charArrayWriter.close();
	}

	public void save(ArrayList list) {
		try {
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				StringBuffer sb = (StringBuffer) iter.next();

				write(sb.toString());
				write("\n");				
			}
			write(" ");
            ARUtils.debug("ARWorkLog: Saved the worklog");
		} catch (SystemException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}

	public void savefail(Exception exp, String status) throws SystemException {
		try {
			write(exp.toString());
			ARConnectionManager.getARContext(this.tenant).updateTask(request,
			        IARConstants.TMS_RETURN_CODE_ERROR,
			        status, charArrayWriter.toString());
			charArrayWriter.close();
		} catch (SystemException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.

		} catch (ARManagerException err) {
			StringBuffer rootcause = new StringBuffer(255);
				if (err.getMessage() != null) {
					rootcause.append(err.getMessage());
					if ( err.getRootCause() != null ) {
						rootcause.append(". ").append(err.getRootCause().getMessage());
					}
				}
                ARUtils.debug("ARWorkLog: Error in Connecting: "+rootcause.toString());
				throw new SystemException(IErrorConstants.AR_CONNECTION_ERROR, rootcause.toString());
		}
	}

    public void close(Map taskMap, String status, String summary)  throws ARManagerException {
        ARConnectionManager.getARContext(this.tenant).updateTask(taskMap, status, summary, charArrayWriter.toString());
        ARUtils.debug("ARWorkLog: Closed the worklog");
        charArrayWriter.close();
    }

    private void writeUser(String userName) {
        if(userName == null) {
            return;
        }
        try {
            write(userName);
            write("\n");
            ARUtils.debug("ARWorkLog: Appended the User Name with Work Log");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
