package com.marimba.apps.securitymgr.view;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: intteiva
 * Date: Mar 30, 2017
 * Time: 5:18:36 PM
 * To change this template use File | Settings | File Templates.
 */

public class SCAPValueParserBean {
    
    private Set<String> contentTitles;
    private Map<String, String> propsMap;
    private Map<String, String> profileIds;
    private Map<String, String> profileTitles;
    private Map<String, String> contentIds;
    private Map<String, String> contentFileNames;
    private Map<String, Map<String, String>> contentDetails;

    public SCAPValueParserBean(Map<String, String> propsMap) {
        this.propsMap = propsMap;
        init();
    }

    private void init() {
        if (null == propsMap || propsMap.isEmpty()) return;

        this.contentTitles = new HashSet<String>();
        this.contentIds = new HashMap<String, String>();
        this.contentFileNames = new HashMap<String, String>();
        this.profileIds = new HashMap<String, String>();
        this.profileTitles = new HashMap<String, String>();

        this.contentDetails = new HashMap<String, Map<String, String>>();

        String propsContentTitles = propsMap.get("props_content_title");
        String propsContentIds = propsMap.get("props_content_id");
        String propsContentFileNames = propsMap.get("props_content_fileName");
        String propsProfileIds = propsMap.get("props_profile_id");
        String propsProfileTitles = propsMap.get("props_profile_title");

        parseContentTitle(propsContentTitles);
        parseContentIds(propsContentIds);
        parseContentFileNames(propsContentFileNames);
        parseProfileIds(propsProfileIds);
        parseProfileTitles(propsProfileTitles);

        for (String title : contentTitles) {
            Map<String, String> detail = new HashMap<String, String>();
            detail.put("content_title", title);
            detail.put("content_id", contentIds.get(title));
            detail.put("content_filename", contentFileNames.get(title));
            detail.put("profile_id", profileIds.get(title));
            detail.put("profile_title", profileTitles.get(title));
            contentDetails.put(title, detail);
        }
    }

    public Set<String> getContentTitles() {
        return contentTitles;
    }

    public Map<String, Map<String, String>> getContentDetails() {
        return contentDetails;
    }

    private void parseContentTitle(String propsVal) {
        if (null == propsVal || propsVal.isEmpty()) return;
        String[] titles = propsVal.split(";");
        contentTitles.addAll(Arrays.asList(titles));
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
