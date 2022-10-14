package com.marimba.apps.subscriptionmanager.webapp.forms;

import java.util.ArrayList;

/*  Copyright 2009, BMC Software. All Rights Reserved.
    Confidential and Proprietary Information of BMC Software.
    Protected by or for use under one or more of the following patents:
    U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
    and 6,430,608. Other Patents Pending.

    $File$, $Revision$, $Date$

     Get and Set the selected Packages

     @author Selvaraj Jegatheesan
     @version 4, 2009/03/16
*/


public class PackageDetailsViewForm
            extends AbstractForm {


    private ArrayList channelsList = new ArrayList(100);
    private String url = null;
    private String type = null;
    private String title = null;
    private String[] selectedChannels = new String[0];
    private String taskid = null;

    public String getUrl() {
        return url;
    }

    public void setUrl(String channelUrl) {
        this.url = channelUrl;
    }
    public String getType() {
        return type;
    }

    public void setType(String channelType) {
        this.type = channelType;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String channelTitle) {
        this.title = channelTitle;
    }

    public ArrayList getChannelsList() {
		return channelsList;
	}

    public void setChannelsList(Object channelsList) {
        this.channelsList.add(channelsList);
    }

    public String[] getSelectedChannels() {
        return selectedChannels;
    }
    public void setSelectedChannels(String[] selectedChannels) {
        this.selectedChannels = selectedChannels;
    }

    public String getTaskid() {
        return taskid;
    }
    public void setTaskid(String taskid) {
        this.taskid = taskid;
    }


}
