package com.marimba.apps.subscriptionmanager;

import com.marimba.intf.castanet.IChannel;

/**
 * Logger for audit logging in policy manager
 * 
 * @author Tony
 *
 */
public class AuditLogger {
	
	private IChannel m_channel;
	
	public AuditLogger() {
		
	}
	public AuditLogger(IChannel channel) {
		this.m_channel = channel;
	}
	
	public void log(int id, int severity, Object arg) {
        log(id, severity, null, arg);
    }
    public void log(int id, int severity, Object arg, String  target) {
        log(id, severity, null, arg, target);
    }

    public void log(int id, int severity, String src, Object arg) {
        log(id, -1, severity, src, null, null, arg);
    }
    public void log(int id, int severity, String src, Object arg, String target) {
        log(id, -1, severity, src, null, null, arg, target);
    }

    public void log(int id, long tm, int severity, String src, String user, String description, Object arg) {
        log(id, tm, severity, src, user, description, arg, null);
    }
    public void log(int id, long tm, int severity, String src, String user, String description, Object arg, String target) {
        Object logArg = null;
        if (tm == -1) {
            tm = System.currentTimeMillis();
        }
        if (src == null) {
            src = "vDesk";
        }
        if(arg instanceof Throwable) {
            logArg = (Throwable)arg;
        }
        else if (arg != null) {
            if(description != null) {
                logArg = description + "- "+ arg.toString();
            }
            else {
                logArg = arg.toString();
            }
        }
        if(null != m_channel) {
        	m_channel.log(id, tm, severity, src, user, logArg, target);
        }
    }

}
