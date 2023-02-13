// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.compliance.util;

import java.io.*;
import java.net.*;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.intf.logs.*;
import com.marimba.intf.util.*;
import com.marimba.intf.application.*;
import com.marimba.intf.certificates.*;
import com.marimba.intf.castanet.*;

import com.marimba.castanet.copy.*;
import com.marimba.castanet.storage.*;
import com.marimba.apps.securitymgr.compliance.util.Tools;

import com.marimba.tools.config.*;
import com.marimba.tools.util.*;
import com.marimba.tools.util.Props;
import com.marimba.tools.util.Password;

/**
 *  ChannelCopier Utility
 *  w.r.t channel copy operation into master tx (source to destination)
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class ChannelCopier implements CopierEnvironment, IObserver, CopierConstants {
    File dir;
    File storeLocation;
    IStorage store;
    IApplicationContext context;
    Copier copier;
    int exitCode;
    private String user;
    private String password;
    boolean credentialsFailed;
    private String certId;
    private String certPassword;
    private boolean txDown;
    private boolean publish = false;
    protected SubscriptionMain main;
    
    public ChannelCopier(SubscriptionMain main, String user, String pwd, String certId, String certPwd) {
	this.user = user;
	this.password = pwd;
	this.certId = certId;
	this.certPassword = certPwd;
    this.main = main;
	copier = new Copier(this, main.getFeatures(), main.getDataDirectory().toString());
	copier.setVerbose(true);
	copier.addObserver(this, 0, 0);
    }

    public ChannelCopier(SubscriptionMain main, String user, String pwd, String certId, String certPwd, IApplicationContext context) {
        this.user = user;
        this.password = pwd;
        this.certId = certId;
        this.certPassword = certPwd;
        this.main = main;
        this.context=context;
        copier = new Copier(this, main.getFeatures(), main.getDataDirectory().toString());
        copier.setVerbose(true);
        copier.addObserver(this, 0, 0);
    }

    public void dispose() {
	copier.removeObserver(this);
    }


    public synchronized int copy(Props props, URL src, URL dest) throws IOException {
       return copy(props, src, null, dest, null);
    }

    public synchronized int copyChannel(Props props, URL src, URL dest) throws IOException {
        return copyChannel(props, src, null, dest, null);
    }

    public synchronized int copy(Props props, URL src, String srcSegment, URL dest, String destSegment) throws IOException {
	publish = !dest.getProtocol().equals("file");

	//
	// If this is going to be a signed patch channel that's being published to a transmitter,
	// then use the little pen icon as the thumbnail so that users can see from the transmitter
	// listing which channels are signed and which are not.
	//

    //todo : uncomment this once we find a way to unzip a patch
	if (!dest.getProtocol().equals("file")) {
        if (getSigningEnabled()) {
            try {
            InputStream in = main.getChannel().getFile("webapp/images/signed.gif").getInputStream();
            File iconFile = new File(new File(props.getFile().getParent()), "signed.gif");
            OutputStream out = new FileOutputStream(iconFile);
             Tools.copyStream(in, out, true);
            props.setProperty("thumbnail","signed.gif");
            props.save();
            } catch (IOException e) {
                logStatus(null, e);
                return -1;
            }
        } else {
            File iconFile = new File(new File(props.getFile().getParent()), "signed.gif");
            if (iconFile.exists()) {
                iconFile.delete();
            }
            props.remove("thumbnail");
            props.save();

        }
	}

	try {
	    synchronized (ChannelCopier.class) {
	    copier.copy(getCopyOp(props, src, srcSegment, dest, destSegment));
	    }
	} finally {
	    if (storeLocation != null && storeLocation.isDirectory()) {
		if (store != null) {
		    store.close();
		    store = null;
		}
		Tools.rmDir(storeLocation);
	    }
	}
            logStatus("EXIT CODE = " + exitCode);
            return exitCode;
    }

    public synchronized int copyChannel(Props props, URL src, String srcSegment, URL dest, String destSegment) throws IOException {
        publish = !dest.getProtocol().equals("file");
        try {
            synchronized (ChannelCopier.class) {
                copier.copy(getCopyOp(props, src, srcSegment, dest, destSegment));
            }
        } finally {
            if (storeLocation != null && storeLocation.isDirectory()) {
                if (store != null) {
                    store.close();
                    store = null;
                }
                Tools.rmDir(storeLocation);
            }
        }
        logStatus("EXIT CODE = " + exitCode);
        return exitCode;
    }


    public synchronized int delete(Props props, URL src, URL dest) throws IOException {
	copier.delete(getCopyOp(props, src, dest));
	return exitCode;
    }

    public void notify(Object sender, int msg, Object arg) {
	// REMIND: observe copier messages
	switch (msg) {
	    // copy status messages
          case COPY_FAILED:
            logStatus("COPY_FAILED " + arg);
            break;
          case COPY_CANCELLED:
            logStatus("COPY_CANCELLED " + arg);
            break;
          case COPY_PARTIAL_CANCEL:
            logStatus("COPY_PARTIAL_CANCEL " + arg);
            break;
          case COPY_COMPLETED:
            logStatus("COPY_COMPLETED " + arg);
            break;
          case COPY_STARTING:
            logStatus("COPY_STARTING " + arg);
            break;
          case COPY_EXCEPTION:
            logStatus("COPY_EXCEPTION " + arg);
            break;
          case SRC_SENDING:
            logStatus("SENDING " + arg);
            break;
          case SRC_CONNECTING:
            logStatus("CONNECTING " + arg);
            break;
	  case SRC_MISSING_INDEX:
            logStatus("MISSING_INDEX " + arg);
            break;
          case SRC_WAITING:
            logStatus("WAITING " + arg);
            break;
          case SRC_RECEIVING_SEGMENTS:
            logStatus("RECEIVING SEGMENTS " + arg);
            break;
          case SRC_RECEIVING_FILES:
            logStatus("RECEIVING FILES "+ arg);
            break;
          case SRC_SCANNING:
            logStatus("SCANNING "+ arg);
            break;
          case SRC_COMPUTING_CHECKSUM:
            logStatus("COMPUTING CHECKSUM "+ arg);
            break;
          case SRC_NEED_FILE:
            logStatus("NEED FILE " + arg);
            break;
          case LOG_DST_EXCEPTION:
            txDown = true;
            logStatus("EXCEPTION " + arg);
            break;
          case LOG_DST_REQUIRED_FILES_MISSING:
            logStatus("REQUIRED FILES MISSING");
            break;
          case LOG_DST_PUBLISH_FAILED:
            logStatus("PUBLISH FAILED segment=" + arg);
            break;
          case LOG_DST_UNKNOWN_HOST:
            txDown = true;
            logStatus("UNKNOWN HOST " + arg);
            break;
          case DST_STARTING_SEGMENT:
            logStatus("STARTING SEGMENT "+ arg);
            break;
          case DST_CALCULATING_REQUIREMENTS:
            logStatus("CALCULATING REQUIREMENTS "+ arg);
            break;
          case DST_PUBLISHING_SEGMENT:
            logStatus( "PUBLISHING SEGMENT "+ arg);
            break;
          case DST_FILES_CHANGED:
            logStatus("FILES CHANGED "+ arg);
            break;
          case DST_CONNECTION_REFUSED:
            txDown = true;
            logStatus("CONNECTION REFUSED " + arg);
            break;
          case DST_PUBLISH_SERVICE_UNAVAILABLE:
            txDown = true;
            logStatus("PUBLISH SERVICE UNAVAILABLE " + arg);
            break;
          case DELETE_FAILED:
            exitCode = 1;
            logStatus("DELETE_FAILED " + arg);
            break;
      case 5300:
            logStatus(null, (Throwable)arg);
            break;
          default:
            logStatus("id=" + msg + " arg=" + arg);
            break;
	}
    }

    public boolean getCredentialsFailed() {
	return credentialsFailed;
    }

    public boolean getTransmitterUnavailable() {
        return txDown;
    }

    /*
     * CopierEnvironment methods
     */

    /**
     * Environment provides an IApplicationContext for the embedding channel
     *
     * @return an IApplicationContext
     */
    public IApplicationContext getApplicationContext() {
        if(this.context != null){
            return this.context;
        }
        return null;
    }

    /**
     * Get copier HTTP config settings from environment
     *
     * @return a CopierHTTPConfig
     */
    public CopierHTTPConfig getCopierHTTPConfig() {
	return new CopierHTTPConfig(new ConfigUtil((IConfig) main.getFeatures().getChild("tunerConfig")));
    }

    /**
     * Environment provides a mechanism to ask the user for credentials
     * to authenticate to a realm.
     *
     * @param realm a String containing the name of the HTTP realm
     * @param failed true if the previous set of credentials failed
     * @param trans3x true if the transmitter is 3.x
     *
     * @return a String containing username:password
     */
    public String getCredentials(String realm, boolean failed, boolean trans3x) {
        logStatus("ChannelPublisher.getCredentials " + realm + " " + failed);
        credentialsFailed = failed;
        if (!failed) {
            return user+":"+Password.decode(password);
        }
    return null;
    }

    /**
     * Environment provides a local storage for holding channel data during
     * a copy operation.  This is temporary data and may be deleted after a
     * copy operation completes.
     *
     * @return an IStorage
     */
    public IStorage getLocalStorage() {
	if (store == null) {
	    try {
		storeLocation = Tools.getTempFolder(main.getDataDirectory());
		store = new StorageManager(storeLocation, null);
		store.open();
	    } catch (IOException ex) {
          //  main.log(ILogConstants.LOG_MAJOR, null, ex);
            ex.printStackTrace();
		return null;
	    }
	}

	return store;
    }

    /**
     * Environment is notified when a copy operation is complete and its exit value
     *
     * @param code an integer representing the copier's exit status
     */
    public void exit(int code) {
	this.exitCode = code;
    }

    /**
     * Get signing preference from the environment
     *
     * @return true if signing is enabled
     * @deprecated
     * As of Indy.  See:
     * #getSignAll
     * #getUnsignAll
     * #getSignUnchanged
     */
    public boolean getSigningEnabled() {
	return certId != null;
    }

    /**
     * Get unsigned signing behavior from the environment
     *
     * @return true if signing is enabled for unsigned channels
     * @deprecated
     * As of Indy.  See:
     * #getSignAll
     * #getUnsignAll
     * #getSignUnchanged
     */
    public boolean getSignUnsigned() {
	return certId != null;
    }

    /**
     * Get whether to sign all channels
     *
     * @return true if signing is enabled for all channels
     */
    public boolean getSignAll() {
	return certId != null;
    }

    /**
     * Get whether to unsign all channels
     *
     * @return true if copier should unsign all channels
     */
    public boolean getUnsignAll() {
	return false;
    }

    /**
     * Get whether to leave the signing alone
     *
     * @return true if copier shouldn't sign/resign or unsign
     */
    public boolean getSignUnchanged() {
	return certId != null;
    }

    /**
     * Get signing certificate ID from environment
     *
     * @return a String containing the signing certificate ID
     */
    public String getCertificateID() {
	return certId;
    }

    /**
     * Environment provides a mechanism for asking the user to specify the private key
     * password for a specific ICertificateEntry.
     *
     * @param entry an ICertificateEntry to retrieve the password for
     *
     * @return a String containing the private key password
     */
    public String getCertificatePassword(ICertificateEntry entry) {
	if (entry.getCertId().equals(certId)) {
	    return certPassword;
	}
	return null;
    }

    /**
     * Ask the environment if  we want to allow copying thru SSL to clear text
     * transmitters.
     */
    public boolean copyThroughSSL() {
	return false;
    }

    /**
     * We need to prune the cache after we are done with one segment. This is
     * handled by the environment
     */
    public void trimCache() {
    }

    // Private

    private CopyOperation getCopyOp(Props props, URL src, URL dest) {
        return getCopyOp(props, src, null, dest, null);
    }

    private CopyOperation getCopyOp(Props props, URL src, String srcSegment, URL dest, String dstSegment) {
	CopyOperation op = new CopyOperation();

	String urlString = src.toExternalForm();

	// Set source
	op.setChannelTitle(urlString);
	op.setSrcURL(urlString);

    op.setSrcSeg(srcSegment);

	// Set destination
	op.setDstURL(dest.toExternalForm());
	op.setProperties(props);

    op.setDstSeg(dstSegment);

	// Set the password
	if (user != null) {
	    if (publish) {
		op.setDstUser(user);
	    } else {
		op.setSrcUser(user);
	    }
	}
	if (password != null) {
	    if (publish) {
		op.setDstPassword(Password.decode(password));
	    } else {
		op.setSrcPassword(Password.decode(password));
	    }
	}
	if (publish && certId != null) {
	    op.setSigning("sign");
	    op.setCertID(certId);
	}

	return op;
    }


    private void logStatus(String msg) {
	    System.out.println("LogInfo: " + msg);
    }

    private void logStatus(String message, Throwable exception) {
        System.out.println("LogInfo: " + message);
        exception.printStackTrace();
    }
}

