// Copyright 1997-2005, BMC Software. All Rights Reserved. 
// Confidential and Proprietary Information of BMC Software.
// Protected by or for use under one or more of the following patents: 
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631, 
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.view;

/**
 * Result bean for packages
 *
 * @author  Zheng Xia
 * @version $Revision$, $Date$
 */
public class PackageBean {

    String url;
    String name;
    String encodedUrl;
    String encodedName;
    String content_type;

    public PackageBean() {
    }

    public void setName(String name) {
	this.name = name;
    this.encodedName = com.marimba.tools.util.URLUTF8Encoder.encode(name);
    }

    public String getName() {
	return name;
    }
    public String getEncodedName() {
	return encodedName;
    }

    public void setUrl(String url) {
	this.url = url;
    this.encodedUrl = com.marimba.tools.util.URLUTF8Encoder.encode(url);
    }
    public String getUrl() {
	return url;
    }

    public void setContent_type(String content_type) {
        this.content_type = content_type;
    }
    public String getContent_type() { 
        return content_type;
    }

    public String getEncodedUrl() {
	return encodedUrl;
    }
}
