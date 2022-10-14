package com.marimba.apps.securitymgr.view;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomSCAPValueParserBean {
	private Set<String> customTemplates;
	private Map<String, String> contentTitles;
    private Map<String, String> propsMap;
    private Map<String, String> profileIds;
    private Map<String, String> profileTitles;
    private Map<String, String> contentIds;
    private Map<String, String> contentFileNames;
    private Map<String, Map<String, String>> contentDetails;

    public CustomSCAPValueParserBean(Map<String, String> propsMap) {
        this.propsMap = propsMap;
        init();
    }

    private void init() {
        if (null == propsMap || propsMap.isEmpty()) return;

        this.customTemplates = new HashSet<String>();
        this.contentTitles = new HashMap<String, String>();
        this.contentIds = new HashMap<String, String>();
        this.contentFileNames = new HashMap<String, String>();
        this.profileIds = new HashMap<String, String>();
        this.profileTitles = new HashMap<String, String>();

        this.contentDetails = new HashMap<String, Map<String, String>>();

        String propscustomTemplates = propsMap.get("props_custom_template");
        String propsContentTitles = propsMap.get("props_content_title");
        String propsContentIds = propsMap.get("props_content_id");
        String propsContentFileNames = propsMap.get("props_content_fileName");
        String propsProfileIds = propsMap.get("props_profile_id");
        String propsProfileTitles = propsMap.get("props_profile_title");

        parseCustomTemplate(propscustomTemplates);
        parseContentTitles(propsContentTitles);
        parseContentIds(propsContentIds);
        parseContentFileNames(propsContentFileNames);
        parseProfileIds(propsProfileIds);
        parseProfileTitles(propsProfileTitles);

        for (String template : customTemplates) {
            Map<String, String> detail = new HashMap<String, String>();
            detail.put("custom_template", template);
            detail.put("content_title", contentTitles.get(template));
            detail.put("content_id", contentIds.get(template));
            detail.put("content_filename", contentFileNames.get(template));
            detail.put("profile_id", profileIds.get(template));
            detail.put("profile_title", profileTitles.get(template));
            contentDetails.put(template, detail);
        }
    }

    public Set<String> getCustomTemplates() {
		return customTemplates;
	}

    public Map<String, Map<String, String>> getContentDetails() {
        return contentDetails;
    }

    private void parseCustomTemplate(String propsVal) {
        if (null == propsVal || propsVal.isEmpty()) return;
        String[] titles = propsVal.split(";");
        customTemplates.addAll(Arrays.asList(titles));
    }
    private void parseContentTitles(String propsVal) {
        if (null == propsVal || propsVal.isEmpty()) return;
        String[] arr1 = propsVal.split(";");
        for (String titleAndFileName : arr1) {
            String[] arr2 = titleAndFileName.split("::");
            if (arr2.length != 2) continue;
            contentTitles.put(arr2[0], arr2[1]);
        }
    }
    private void parseContentIds(String propsVal) {
        if (null == propsVal || propsVal.isEmpty()) return;
        String[] arr1 = propsVal.split(";");
        for (String titleAndFileName : arr1) {
            String[] arr2 = titleAndFileName.split("::");
            if (arr2.length != 2) continue;
            contentIds.put(arr2[0], arr2[1]);
        }
    }

    private void parseContentFileNames(String propsVal) {
        if (null == propsVal || propsVal.isEmpty()) return;
        String[] arr1 = propsVal.split(";");
        for (String titleAndFileName : arr1) {
            String[] arr2 = titleAndFileName.split("::");
            if (arr2.length != 2) continue;
            contentFileNames.put(arr2[0], arr2[1]);
        }
    }

    private void parseProfileIds(String propsVal) {
        if (null == propsVal || propsVal.isEmpty()) return;
        String[] arr1 = propsVal.split(";");
        for (String titleAndFileName : arr1) {
            String[] arr2 = titleAndFileName.split("::");
            if (arr2.length != 2) continue;
            profileIds.put(arr2[0], arr2[1]);
        }
    }

    private void parseProfileTitles(String propsVal) {
        if (null == propsVal || propsVal.isEmpty()) return;
        String[] arr1 = propsVal.split(";");
        for (String titleAndFileName : arr1) {
            String[] arr2 = titleAndFileName.split("::");
            if (arr2.length != 2) continue;
            profileTitles.put(arr2[0], arr2[1]);
        }
    }
}
