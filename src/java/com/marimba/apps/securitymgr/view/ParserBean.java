package com.marimba.apps.securitymgr.view;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: Mar 29, 2017
 * Time: 12:11:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class ParserBean {
    
    private String orgVal;
    private String contentTitle;
    private String contentFileName;
    private String profileID;
    private String profileTitle;
    private String customTemplateName;
    private String scapOption;
    private String type;
    public ParserBean(String value, String scapOption) {
        this.orgVal = value;
        this.scapOption = scapOption;
        init();
    }

    private void init() {
    	if (null == orgVal || orgVal.trim().isEmpty()) return;
        String tmp[] = orgVal.split("\\$-\\$");
        if("standard".equalsIgnoreCase(scapOption)) {
	        if (tmp.length < 4) return;
	        this.contentTitle = tmp[1];
	        this.contentFileName = tmp[1] + "::" + tmp[0];
	        this.profileID = tmp[1] + "::" + tmp[2];
	        this.profileTitle = tmp[1] + "::" + tmp[3];
            if (tmp.length > 4) {
                this.type = tmp[4];
            }
            System.out.println(this);
        } else if("customize".equalsIgnoreCase(scapOption)) {
        	if (tmp.length < 5) return;
        	this.customTemplateName = tmp[1];
	        this.contentTitle = tmp[1] + "::" + tmp[2];
	        this.contentFileName = tmp[1] + "::" + tmp[0];
	        this.profileID = tmp[1] + "::" + tmp[3];
	        this.profileTitle = tmp[1] + "::" + tmp[4];
            if (tmp.length > 5) {
                this.type = tmp[5];
            }
            System.out.println(this);      
        }
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getContentFileName() {
        return contentFileName;
    }

    public String getProfileID() {
        return profileID;
    }

    public String getProfileTitle() {
        return profileTitle;
    }

    public String getCustomTemplateName() {
		return customTemplateName;
	}

    public String getType() {
        return type;
    }

    public void setType(String contentType) {
        this.type = type;
    }

    public String toString() {
    	if("customize".equalsIgnoreCase(scapOption)) {
    		return "Template Name : " + customTemplateName + "contentTitle : " + contentTitle + " contentFileName : " + contentFileName + " profileID : " + profileID + " profileTitle : " + profileTitle;
    	}
        return "contentTitle : " + contentTitle + " contentFileName : " + contentFileName + " profileID : " + profileID + " profileTitle : " + profileTitle;
    }
}
