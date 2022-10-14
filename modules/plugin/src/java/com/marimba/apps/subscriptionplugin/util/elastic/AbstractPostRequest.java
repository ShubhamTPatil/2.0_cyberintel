// Copyright 2017, BMC Software. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents:
// U.S Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionplugin.util.elastic;

import java.io.*;
import java.net.*;

import com.marimba.castanet.http.*;
import com.marimba.castanet.ssl.*;
import com.marimba.intf.certificates.*;
import com.marimba.intf.http.*;
import com.marimba.intf.plugin.IPluginContext;
import com.marimba.intf.ssl.*;
import com.marimba.intf.tuner.ILoginService;
import com.marimba.intf.util.*;
import com.marimba.io.*;
import com.marimba.tools.config.*;
import com.marimba.tools.net.*;
import com.marimba.tools.util.*;
import com.marimba.apps.subscriptionplugin.IPluginDebug;

public abstract class AbstractPostRequest implements HTTPConstants, IPluginDebug {
	HTTPManager httpMgr;
	HTTPConfig httpConfig;
	ILoginService login;
	protected int httpRetCode;
	protected String userName, passWord;
	
	public AbstractPostRequest(IPluginContext context) {
		// create the http manager for forwarding reports
		IConfig tunerConfig = (IConfig)context.getFeature("config");
		this.httpConfig = new HTTPConfig(new ConfigUtil(tunerConfig));
		try {
			ISSLProvider ssl = (ISSLProvider) context.getFeature("ssl");
			ICertProvider cert = (ICertProvider) context.getFeature("certificates");
			this.login = (ILoginService)context.getFeature("login");
			this.httpMgr = new HTTPSManager(this.httpConfig, cert, ssl);
		} catch (UnsatisfiedLinkError e) {
			this.httpMgr = new HTTPManager(httpConfig);
		}
		httpRetCode = 0;
		userName = null;
		passWord = null;
	}
	
	public AbstractPostRequest(HTTPManager httpMgr, HTTPConfig httpConfig, ILoginService login) {
		this.httpMgr = httpMgr;
		this.httpConfig = httpConfig;
		this.login = login;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}
	
	protected abstract void debug(boolean level, String msg);
	
	protected abstract String getConnectionURL();
	protected abstract boolean writeRequest(OutputStream out) throws IOException;
	protected abstract void readResponse(InputStream in, int contentLength) throws IOException;
	
	public void sendRequest() {
		try {
			String url = getConnectionURL();
			if(url == null) {
				debug(ERROR, "sendRequest(), No URL set, so no stopping of any ElasticServer is possible!");
				return;
			}
			URL u = new URL(url);
			HTTPRequest req = httpMgr.create(u);
			FastOutputStream fout = new FastOutputStream(100);
			boolean hasContent = writeRequest(fout);
			byte[] finalRecordBytes = null;
			if(hasContent) {
				finalRecordBytes = fout.toByteArray();
			}
			
			do {
				if(req != null) {
					debug(INFO, "sendRequest(), Request was NOW physically created to: "+u.toString());
				} else {
					debug(ERROR, "sendRequest(), Request was NOT created since it was NULL!");
				}
				
				
				
				//connecting...
				req.connect(false);
				debug(INFO, "sendRequest(), Keep alive is true...");
				req.addField("Keep-Alive","true");
				req.addField("Accept", "*/*");
				req.addField("Content-type","application/x-www-form-urlencoded");
				
				if(userName != null && passWord != null) {
					debug(INFO, "sendRequest(), doing AFTER connect, got username and decoded password as: "+userName+":"+passWord);
					String chAuth = "Basic "+Password.encode(userName+":"+passWord);
					debug(INFO, "sendRequest(), chauth returned after encoding is and sending: "+chAuth);
					req.addField("authorization", chAuth);
					debug(INFO, "sendRequest(), authorization in lower case set to: "+chAuth);
				} else {
					req.addField("authorization", "Basic ");
				}
				
				//trigger HTTP POST
				if(finalRecordBytes != null) {
					debug(INFO, "sendRequest(), POSTING with length: "+finalRecordBytes.length);
					req.post(finalRecordBytes.length);
					OutputStream out = req.getOutputStream();
					out.write(finalRecordBytes, 0, finalRecordBytes.length);
					out.flush();
				} else {
					debug(INFO, "sendRequest(), Doing post without any content!");
					req.post(0);
				}
				
				
			} while (req.reply() == HTTP_RETRY);
			
			httpRetCode = req.getResult();
			
			switch(httpRetCode) {
				case HTTP_OK:
					debug(INFO, "sendRequest(), HTTP reply OK, so Elastic Search seemed to have accepted it, printing out reply");
					InputStream in = req.getInputStream();
					readResponse(in, req.getContentLength());
					break;
				default:
					debug(INFO, "sendRequest(), HTTP reply returned: "+req.reply());
					break;
			}
			req.close();
			debug(INFO, "sendRequest(), Request finished transferred...");
		} catch(Throwable t) {
			if(ERROR) {
				t.printStackTrace();
			}
		}
		
	}
	
	public int getHttpReplyCode() {
		return httpRetCode;
	}
	
}


