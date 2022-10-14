package com.marimba.apps.subscriptionmanager.webapp.util;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.TenantHelper;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.intf.msf.ITenant;
import com.marimba.tools.util.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.HashMap;

public class SecurityMgmtHelper extends HttpServlet implements IWebAppConstants {
    final static int DEBUG = DebugFlag.getDebug("SECURITY/SERVICE");
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

        if (null != command && "scapcontentchange".equals(command)) {
            String content = request.getParameter("content");
            System.out.println("Content :" + content);
            response.setContentType("application/json; charset=utf-8");
            response.setHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");

            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            try {
                HashMap<String, String> profilesForScapContent = com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils.getSCAPUtils().getProfilesForScapContent(content);
                jsonObject.put("content", content);
                jsonObject.put("profilesCount", profilesForScapContent.size());
                for (String profileTitle : profilesForScapContent.values()) {
                    jsonArray.put(profileTitle);
                }
                jsonObject.put("profilesTitle", jsonArray);

                jsonArray = new JSONArray();
                for (String profileId : profilesForScapContent.keySet()) {
                    jsonArray.put(profileId);
                }
                jsonObject.put("profilesId", jsonArray);

                jsonArray = new JSONArray();
                for (String profileId : profilesForScapContent.keySet()) {
                    JSONObject jsonArrObject = new JSONObject();
                    jsonArrObject.put(profileId, profilesForScapContent.get(profileId));
                    jsonArray.put(jsonArrObject.toString());
                }
                jsonObject.put("profiles", jsonArray);
            } catch (Throwable ed) {
                ed.printStackTrace();
            } finally {
                out.println(jsonObject.toString());
                if (null != out) {
                    out.flush();
                }
                if (null != out) {
                    out.close();
                }
            }
            return;
        }
        if (null != command && "gethtml".equals(command)) {
            InputStream is = null;
            String htmlPath = null;
            try {
                String target = request.getParameter("target");
                System.out.println("target - " + target);
                String content = request.getParameter("content");
                System.out.println("content - " + content);
                String profile = request.getParameter("profile");
                System.out.println("profile - " + profile);
                String template = request.getParameter("template");
                System.out.println("template - " + template);
                String format = "true".equalsIgnoreCase(request.getParameter("customize")) ? "html:customize" : "html";
                System.out.println("format - " + format);

                htmlPath = com.marimba.apps.subscriptionmanager.webapp.util.SCAPUtils.getSCAPUtils().list(target, content, profile, format, template);
                System.out.println("htmlPath - " + htmlPath);

                response.setContentType("text/html; charset=UTF-8");
                response.setHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
                response.setHeader("Pragma", "no-cache");

                if(htmlPath != null) {
                    File readHtmlFile = new File(htmlPath);
                    if(readHtmlFile.exists()) {
                        int k = 0;
                        is = new FileInputStream(readHtmlFile);
                        while ((k = is.read()) != -1) {
                            out.write(k);
                        }
                    } else {
                        System.out.println("report file not found :" + htmlPath);
                        out.write("<html><b>Failed to retrieve the details of specified security profile.</b></html>");
                    }
                    if (is != null) {
                        is.close();
                        is = null;
                    }
                } else {
                    System.out.println("record not found");
                    out.write("<html><b>Failed to retrieve the details of specified security profile.</b></html>");
                }
            } catch (Exception ed) {
                if (DEBUG > 7) {
                    ed.printStackTrace();
                    System.out.println("vInspector => SecurityMgmtHelper.java -- service(), record error found");
                }
                if (null != out) {
                    out.write("<html><b>No reports found</b></html>");
                }
            } finally {
                if (null != out) {
                    out.flush();
                }
                if (null != out) {
                    out.close();
                }
                if (null != htmlPath) {
                    new File(htmlPath).delete();
                }
            }
            return;
        }
        if ("loadusgcb".equals(command)) {

            response.setContentType("application/json; charset=utf-8");
            response.setHeader("Cache-Control", "max-age=0, no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");

            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            try {
                jsonObject.put("data", jsonArray);
            } catch (Exception jex) {
                jex.printStackTrace();
            } finally {
                out.println(jsonObject.toString());
                if (null != out) {
                    out.flush();
                }
                if (null != out) {
                    out.close();
                }
            }
        }
    }

    public void debug(String str) {
        if(DEBUG >= 5) {
            System.out.println("vInspector => SecurityMgmtHelper.java -- " + str);
        }
    }

}
