package com.marimba.apps.securitymgr.utils;

import java.io.*;
import java.net.*;

import com.marimba.castanet.http.*;
import com.marimba.intf.http.*;
import com.marimba.io.*;
import com.marimba.tools.net.*;
import com.marimba.tools.util.*;

public abstract class AbstractPostRequest implements HTTPConstants {
    public final static int DEBUG = DebugFlag.getDebug("SECURITY/PLUGINANALYTICS");
    HTTPManager httpMgr;
    HTTPConfig httpConfig;
    protected int httpRetCode;
    protected String userName, passWord;

    public AbstractPostRequest(HTTPManager httpMgr, HTTPConfig httpConfig) {
        this.httpMgr = httpMgr;
        this.httpConfig = httpConfig;
        httpRetCode = 0;
        userName = null;
        passWord = null;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    protected abstract void debug(int level, String msg);

    protected abstract String getConnectionURL();
    protected abstract boolean writeRequest(OutputStream out) throws IOException;
    protected abstract void readResponse(InputStream in, int contentLength) throws IOException;

    public void sendRequest() {
        try {
            String url = getConnectionURL();
            if(url == null) {
                debug(3,"No URL set, so no stopping of any ElasticServer is possible!");
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
                    debug(3,"Request was NOW physically created to: "+u.toString());
                } else {
                    debug(3,"Request was NOT created since it was NULL!");
                }



                //connecting...
                req.connect(false);
                debug(3,"Keep alive is true...");
                req.addField("Keep-Alive","true");
                req.addField("Accept", "*/*");
                req.addField("Content-type","application/x-www-form-urlencoded");

                if(userName != null && passWord != null) {
                    debug(3,"doing AFTER connect, got username and decoded password as: "+userName+":"+passWord);
                    String chAuth = "Basic "+Password.encode(userName+":"+passWord);
                    debug(3,"chauth returned after encoding is and sending: "+chAuth);
                    req.addField("authorization", chAuth);
                    debug(3,"authorization in lower case set to: "+chAuth);
                } else {
                    req.addField("authorization", "Basic ");
                }

                //trigger HTTP POST
                if(finalRecordBytes != null) {
                    debug(3,"POSTING with length: "+finalRecordBytes.length);
                    req.post(finalRecordBytes.length);
                    OutputStream out = req.getOutputStream();
                    out.write(finalRecordBytes, 0, finalRecordBytes.length);
                    out.flush();
                } else {
                    debug(3,"Doing post without any content!");
                    req.post(0);
                }


            } while (req.reply() == HTTP_RETRY);

            httpRetCode = req.getResult();

            switch(httpRetCode) {
                case HTTP_OK:
                    debug(4,"HTTP reply OK, so Elastic Search seemed to have accepted it, printing out reply");
                    InputStream in = req.getInputStream();
                    readResponse(in, req.getContentLength());
                    break;
                default:
                    debug(4,"HTTP reply returned: "+req.reply());
                    break;
            }
            req.close();
            debug(3,"Request finished transferred...");
        } catch(Throwable t) {
            if(DEBUG >= 3) {
                t.printStackTrace();
            }
        }

    }

    public int getHttpReplyCode() {
        return httpRetCode;
    }

}


