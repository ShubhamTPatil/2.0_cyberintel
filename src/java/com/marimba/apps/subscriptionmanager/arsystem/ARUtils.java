// Copyright 1997-2005, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
package com.marimba.apps.subscriptionmanager.arsystem;

import com.marimba.tools.util.DebugFlag;
import com.marimba.tools.util.Props;
import com.marimba.tools.util.URLUTF8Decoder;
import com.marimba.tools.ptree.Tree;
import com.marimba.intf.util.IProperty;
import com.marimba.intf.ptree.ITreeNode;
import com.marimba.io.FastInputStream;
import com.marimba.str.StrString;
import com.marimba.webapps.tools.taglib.txlisting.TransmitterListing;
import org.apache.axis.utils.XMLUtils;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.net.URL;
import java.io.InputStream;

/**
 * File contains common utility methods used by the arsystem package
 *
 * @author Devendra Vamathevan
 * @version 7.0.0.0 08/21/200
 */
public class ARUtils {

	private final static int DEBUG = DebugFlag.getDebug("SUB/ARTASK");

	/**
	 * @param msg         message that should be printed out
	 */
	public static void debug(String msg) {
        if(DEBUG > 4) {
            System.out.println(msg);
        }
	}


   public static String getChildElemStringValue(String nameSpace, Element elem, String childTag) {

		NodeList nodelist = elem.getElementsByTagNameNS(nameSpace, childTag);
		if (nodelist.item(0).getNodeType() == Node.ELEMENT_NODE) {
			return getChildCharacterData((Element) (nodelist.item(0)));
		}

		return null;
	}

	public static String ElementToString(Element element) {
		return XMLUtils.ElementToString(element);

	}

	public static String getChildCharacterData(Element parentEl) {

		if (parentEl == null) {
			return null;
		}
		Node tempNode = parentEl.getFirstChild();
		StringBuffer strBuf = new StringBuffer();
		CharacterData charData;

		while (tempNode != null) {
			switch (tempNode.getNodeType()) {
				case Node.TEXT_NODE:
				case Node.CDATA_SECTION_NODE:
					charData = (CharacterData) tempNode;
					strBuf.append(charData.getData());
					break;
			}
			tempNode = tempNode.getNextSibling();
		}
		return strBuf.toString();
	}

	/**
     * returns attributes of the channel element in XML listing retrieved from url?xml
     * <channel url="m6000/Current/ChannelCopier" channeltype="xxx" title="xxx" descritpion="xxx">
     *
     * @param channelURL
     * @return IProperty:  attributes of the channel element in XML listing
     *         null:  if cannot get channel listing for whatever reason.
     */

   public static IProperty getChannelProperties(String channelURL) {

        ARUtils.debug("GetChannelProperties: XML listing properties for URL: "+channelURL);
        try {
            URL url = new URL(channelURL + "/?xml");

            FastInputStream fis = new FastInputStream(url.openStream(), 4 * 1024);
            TransmitterListing txList = new TransmitterListing();
            txList.getList((InputStream) fis, url.getHost(), url);

            Tree listing = txList.getTree();
            if(DEBUG > 4) {
                listing.print();
            }

            String nodeURL = url.getFile();
            String nodePath = URLUTF8Decoder.decode(nodeURL);
            int index = nodePath.lastIndexOf('/');

            if (index <= 0) {
                // channelURL was an empty string
                return null;
            }

            ITreeNode node = listing.lookup(new StrString(nodePath.substring(0, index)));
            ARUtils.debug("GetChannelProperties: LookUp Node Path: "+nodePath.substring(0, index));

            if ((node == null) || !"channel".equals(node.getType())) {
                ARUtils.debug("GetChannelProperties: No Such Channel Node: "+nodePath.substring(0, index));
                return null;
            }

            String[] pairs = node.getPropertyPairs();
            Props props = new Props();

            for (int j = 0; j < pairs.length; j += 2) {
                props.setProperty(pairs[j], pairs[j + 1]);
                ARUtils.debug("GetChannelProperties: Node List: "+pairs[j]+" : "+pairs[j + 1]);
            }
            return props;
        } catch (Exception e) {
            ARUtils.debug("GetChanelProperties: Exception found: "+e.getMessage());
            return null;
        }
    }

}
