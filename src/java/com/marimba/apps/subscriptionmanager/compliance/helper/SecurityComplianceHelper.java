package com.marimba.apps.subscriptionmanager.compliance.helper;

import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import com.marimba.intf.msf.*;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.approval.ApprovalPolicyStorage;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.tools.util.URLUTF8Encoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Hashtable;

public class SecurityComplianceHelper extends HttpServlet implements IWebAppConstants {
	HttpSession session;
	SubscriptionMain main;
	ITenant tenant;
	public void init() throws ServletException {
        super.init();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        service(request, response);
    }

    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        ServletContext context = request.getSession().getServletContext();
        this.session = request.getSession();
        this.main = TenantHelper.getTenantSubMain(context, request);
        this.tenant = main.getTenant();
        String command = request.getParameter("command");
        System.out.println("Command :" + command);
        
		if (null != command && "scapcompliance".equals(command)) {
			String targetName = (String) session.getAttribute(SELECTED_TARGET_NAME);
			String targetType = (String) session.getAttribute(SELECTED_TARGET_TYPE);
			String targetId = (String) session.getAttribute(SELECTED_TARGET_ID);

			System.out.println("Target Name :" + targetName);
			System.out.println("Target Type :" + targetType);
			System.out.println("Target Id :" + targetId);

			char[] buf = new char[10485760];
			PrintWriter writer = null;
			InputStream is = null;
			try {
				
				String filePath = "d:\\scap\\demo1.html"; // example path
				response.setContentType("text/html; charset=UTF-8"); 
				response.setHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
				response.setHeader("Pragma", "no-cache");
				writer = response.getWriter();
				//is = new FileInputStream(readHtmlFile);
				boolean status = getBlobReportPageStream(targetName, targetId, targetType);
				
				if(status) {
					filePath = main.getDataDirectory().getAbsolutePath() + File.separator + "scapreports" + File.separator + targetId + ".html";
					File readHtmlFile = new File(filePath);
					if(readHtmlFile.exists()) {
						int k = 0;
						is = new FileInputStream(readHtmlFile);
						while ((k = is.read()) != -1) {
							writer.write(k);
						}
					} else {
						System.out.println("report file not found :" + filePath);
						writer.write("<html><b>No reports found</b></html>");
					}
				} else {
					System.out.println("record not found");
					writer.write("<html><b>No reports found</b></html>");
				}
			} catch (Exception ed) {
				ed.printStackTrace();
				System.out.println("record error found");
				writer.write("<html><b>No reports found</b></html>");
			} finally {
				if (null != writer) {
					writer.flush();
				}
				if (null != writer) {
					writer.close();
				}
			}
			//response.sendRedirect("distribution/security_machine_complaince.jsp"); 
			return;
		}
    }
    private boolean getBlobReportPageStream(String targetName, String targetId, String targetType) {
    	boolean status = false;
    	ApprovalPolicyStorage storage;
    	try {
    		storage = main.getDBStorage();
    		if(null != storage) {
    			status = storage.getBlobPolicyByTarget(targetName, targetId, targetType);
    		} else {
    			System.out.println("Datasource is not initialized");
    		}
    	} catch(Exception ed) {
    		System.out.println("Failed to get blob policy report:" + ed.getMessage());
    	}
    	return status;
    }
}
