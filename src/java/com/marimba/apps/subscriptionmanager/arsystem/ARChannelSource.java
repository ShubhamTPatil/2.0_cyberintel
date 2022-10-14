// Copyright 1997-2005, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionmanager.arsystem;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.intf.msf.arsys.IARConstants;
import com.marimba.intf.util.IProperty;
import com.marimba.webapps.intf.SystemException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

/**
 * File extracts channel information from the webservice XML Element
 *
 * @author Devendra Vamathevan
 * @version 7.0.0.0 08/21/200
 */
public class ARChannelSource {

	/**
	 * @param elem
	 * @return
	 */
	public List getChannels(Element elem) throws SystemException {

		ArrayList channels = new ArrayList();

		if (elem == null) {
            ARUtils.debug("GetChannels: Empty Document");
			return null;
		}

		String channelURL;

		NodeList nodelist = elem.getElementsByTagNameNS(IARConstants.ARELEMENT_NAMESPACE, IARConstants.TAG_ASSOCIATIONQUERY);
        ARUtils.debug("GetChannels: Nodelist Size: "+nodelist.getLength());

		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				NodeList targetNodelList = ((Element) node).getElementsByTagNameNS(IARConstants.ARELEMENT_NAMESPACE, IARConstants.TAG_ASSOCIATION);
                ARUtils.debug("GetChannels: TargetNodeList Size: "+targetNodelList.getLength());

				for (int j = 0; j < targetNodelList.getLength(); j++) {
					if (targetNodelList.item(j).getNodeType() == Node.ELEMENT_NODE) {
						Element ci = (Element) targetNodelList.item(j);
						String itemType = ARUtils.getChildElemStringValue(IARConstants.ARELEMENT_NAMESPACE, ci, IARConstants.TAG_ITEM_TYPE);
						channelURL = ARUtils.getChildElemStringValue(IARConstants.ARELEMENT_NAMESPACE, ci, IARConstants.TAG_ITEM_DETAILS);
						String channelState = ARUtils.getChildElemStringValue(IARConstants.ARELEMENT_NAMESPACE, ci, IARConstants.TAG_ITEM_STATE);
						if (IARConstants.ITEMTYPE_SOURCE.equals(itemType)) {
							Channel ch = createChannel(channelURL,channelState);
                            ARUtils.debug("GetChannels: Relationship Item Type: "+itemType+" Channel URL: "+channelURL+" Channel State: "+channelState);
							channels.add(ch);
						}
					}
				}
			}
		}
		return channels;
	}

   public String getChannelsAsString(Element elem)  throws SystemException {
        List channelList = getChannels(elem);

        StringBuffer strbuf = new StringBuffer();
        int len = channelList.size();
        Channel channel = null;
        for (int i = 0; i < len; i++) {
			channel = (Channel) channelList.get(i);
            strbuf.append(channel.getUrl());
            if (i != len-1) {
                strbuf.append(",");
            }
		}
       if (strbuf.length() == 0)    {
           return null;
       }
       ARUtils.debug("GetChannelAsString: "+strbuf.toString());

    	return strbuf.toString();
	}
    
    private HashMap getList(String parameterString) {
        HashMap hm = new HashMap();
        if (null != parameterString) {
            String[] parameters = parameterString.split(",");
            for (int i = 0; i < parameters.length; i = i+ 2) {
                hm.put(parameters[i],parameters[i+1]);
            }
        }
        return hm;
    }
    public List getChannels(String channels) throws SystemException {
          ArrayList chList = new ArrayList();
          HashMap hm = getList(channels);
          Iterator it = hm.keySet().iterator();
           while (it.hasNext()) {
               Object o =  it.next();
               Channel ch = createChannel((String)o,(String) hm.get(o));
               chList.add(ch);
           }
        return chList;
    }

    Channel createChannel(String channelURL, String channelState) throws SystemException {
        IProperty chProperties = ARUtils.getChannelProperties(channelURL);
		String title = getChannelTitle(channelURL, chProperties);
	    //convert ARChannel state to subscription channel state
		channelState = getChannelState(channelState, chProperties);
		Channel ch = new Channel(channelURL, title, channelState, null);
		ch.setType(getChannelType(chProperties));
        ARUtils.debug("Create Channel: "+ch.toString());
        return ch;
    }

	/**
	 * @param channelURL extract the channel name from the URL
	 * @return
	 */
	private String getChannelTitle(String channelURL, IProperty chProperties) {
		String chURL;
		if (chProperties != null) {
			chURL = chProperties.getProperty("title");
			if (chURL != null) {
				return chURL;
			}
		}
		int pos = channelURL.lastIndexOf("/");
		if (pos > 0) {
			return channelURL.substring(pos + 1);
		}
		return channelURL;
	}

	/**
	 * @param channelState channel state specified by user
	 * @return always subscribe (install) or delete based on the users input
	 * @throws SystemException if a value other instal or delete is chosen
	 */
	private String getChannelState(String channelState, IProperty channelProps) throws SystemException {
        String channelType = getChannelType(channelProps);
		if (channelState != null) {
			channelState = channelState.toUpperCase();
            channelState = channelState.trim();
            ARUtils.debug("GetChannelState: Channel State: "+channelState);
			if (IARConstants.CHANNELSTATE_DELETE.equals(channelState)) {
                if(channelType.indexOf("patch") != -1) {
                    throw new SystemException(IErrorConstants.AR_INVALID_PATCH_STATE);
                } else {
                    return ISubscriptionConstants.STATE_DELETE;
                }
			} else if (IARConstants.CHANNELSTATE_INSTALL.equals(channelState)) {
                    return ISubscriptionConstants.STATE_SUBSCRIBE;               
			}
		}
		throw new SystemException(IErrorConstants.AR_INVALID_CHANNEL_STATE);
	}

	/**
	 * @param chProperties Iproperty containing value return by transmitter
	 * @return return the type given by tx. This only works if the TX is 7.0 or >
	 */
	private String getChannelType(IProperty chProperties) {
		if (chProperties != null) {
            String[] pairs = chProperties.getPropertyPairs();
            for(int i=0;i<pairs.length;i++) {
                ARUtils.debug("Channel Properties: "+pairs[i]);
            }
			String chType = chProperties.getProperty("channeltype");
			if (chType != null && chType.toLowerCase().startsWith("patch")) {
				return ISubscriptionConstants.CONTENT_TYPE_PATCHGROUP;
			}
		}
		return ISubscriptionConstants.CONTENT_TYPE_APPLICATION;
	}
}

