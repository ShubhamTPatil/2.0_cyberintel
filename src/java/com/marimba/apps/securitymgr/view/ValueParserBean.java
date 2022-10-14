package com.marimba.apps.securitymgr.view;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: Mar 29, 2017
 * Time: 12:11:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ValueParserBean {
    private String contentTitles;
    private String contentFileNames;
    private String profileIDs;
    private String profileTitles;
    private String newValue;
    private String customTemplateNames;
    private String scapOption;

    public ValueParserBean(String newValue, String scapOption) {
        this.newValue = newValue;
        this.scapOption = scapOption;
        init();
    }

    private void init() {
        if (null == newValue || newValue.trim().isEmpty()) return;
        String tmp[] = newValue.split(":_:");
        for (String aStr : tmp) {
            ParserBean bean = new ParserBean(aStr, scapOption);
            contentTitles = (null == contentTitles || contentTitles.trim().isEmpty()) ? "" + bean.getContentTitle() : contentTitles + ";" + bean.getContentTitle();
            contentFileNames = (null == contentFileNames || contentFileNames.trim().isEmpty()) ? "" + bean.getContentFileName() : contentFileNames + ";" + bean.getContentFileName();
            profileIDs = (null == profileIDs || profileIDs.trim().isEmpty()) ? "" + bean.getProfileID() : profileIDs + ";" + bean.getProfileID();
            profileTitles = (null == profileTitles || profileTitles.trim().isEmpty()) ? "" + bean.getProfileTitle() : profileTitles + ";" + bean.getProfileTitle();
            customTemplateNames = (null == customTemplateNames || customTemplateNames.trim().isEmpty()) ? "" + bean.getCustomTemplateName() : customTemplateNames + ";" + bean.getCustomTemplateName();
        }
        System.out.println(this);
    }

    public String getContentTitles() {
        return contentTitles;
    }

    public String getContentFileNames() {
        return contentFileNames;
    }

    public String getProfileIDs() {
        return profileIDs;
    }

    public String getProfileTitles() {
        return profileTitles;
    }

    public String getCustomTemplateNames() {
		return customTemplateNames;
	}

	public String toString() {
		if("customize".equalsIgnoreCase(scapOption)) {
			return "ValueParserBean Template Name : " + customTemplateNames + " contentTitles : " + contentTitles + " contentFileNames : " + contentFileNames + " profileIDs : " + profileIDs + " profileTitles : " + profileTitles;
		}
        return "ValueParserBean contentTitles : " + contentTitles + " contentFileNames : " + contentFileNames + " profileIDs : " + profileIDs + " profileTitles : " + profileTitles;
    }
}
