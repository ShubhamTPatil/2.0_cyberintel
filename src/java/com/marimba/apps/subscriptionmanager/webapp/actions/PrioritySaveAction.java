// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.webapp.actions;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscription.common.ISubscriptionConstants;
import com.marimba.apps.subscription.common.objects.Channel;

import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.webapp.system.DistributionBean;
import com.marimba.apps.subscriptionmanager.webapp.forms.DistAsgForm;
import com.marimba.apps.subscriptionmanager.cli.commands.InstallPriority;

import com.marimba.webapps.intf.GUIException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.webapps.intf.KnownException;

/**
 * This action is responsible changing the install priority of a channel.
 *
 * @author Michele Lin
 * @author Sunil Ramakrishnan
 */
public final class PrioritySaveAction extends AbstractAction implements IWebAppConstants {
    public ActionForward execute(ActionMapping       mapping,
                                 ActionForm          form,
                                 HttpServletRequest  req,
                                 HttpServletResponse response)
        throws IOException, ServletException {
        
    InstallPriority  ip = null;    	
   	if(req.getSession().getAttribute(SESSION_MULTITGBOOL)!=null){
   		ip = new InstallPriority();
   		HashSet<Integer> usedPriorities = (HashSet<Integer>) req.getSession().getAttribute("usedPriorities");
   		ip.setUsedPriorities(usedPriorities);   		
   	}else{
   		ip = new InstallPriority();   		
   	}

   	String action = req.getParameter("action");
    HttpSession session = req.getSession();
	DistributionBean distributionBean = (DistributionBean) session.getAttribute(SESSION_DIST);
    ArrayList channelList = distributionBean.getApplicationChannels();
	int startingPriority =  distributionBean.getStartingPriority();
    resetUpdateChannelOrders((DistAsgForm)form);
	String itemStr = (String) req.getParameter("item");
	int item = 0;
	if (itemStr != null) {
	    item = Integer.parseInt(itemStr);
	}

        if (DEBUG) {
            System.out.println("action = " + action + ", item = " + item);
            for (int i = 0; i < channelList.size(); i++) {
                System.out.println("channelList[" + i + "] = " + channelList.get(i));
            }
        }

	// if the channel priorities have not been initialized yet, initialize it.

        if (ACTION_MOVE_TO_TOP.equals(action)) {
            moveToTop(channelList, item);
        } else if (ACTION_MOVE_UP.equals(action)) {
	    moveUp(channelList, item);
        } else if (ACTION_MOVE_DOWN.equals(action)) {
	    moveDown(channelList, item);
        } else if (ACTION_MOVE_TO_BOTTOM.equals(action)) {
	    moveToBottom(channelList, item);
	} else if (ACTION_SET_STARTING_PRIORITY.equals(action)) {
        validate(channelList, startingPriority);
        distributionBean.setPreState(channelList);
        initializeOrder((DistAsgForm)form, channelList, startingPriority, req, ip);
        ((DistAsgForm)form).setValue(SESSION_DIST_PAGEPKGS_PREORDER, "true");
	} else if (ACTION_MODIFY_EXISTING_PRIORITY.equals(action)) {
        ArrayList list = distributionBean.getModifiedChannelOrders();
        distributionBean.setPreState( channelList );
        ((DistAsgForm)form).setValue(SESSION_DIST_PAGEPKGS_PREORDER, "true");
        modifyOrder(channelList, list, ip);
        distributionBean.removeModifiedChannelOrders();
    } else if(ACTION_REVERT_PRIORITY.equals(action)){
        revertPriority(channelList, distributionBean.getPreState(), ip);
        distributionBean.clearPreState();
        (( DistAsgForm)form).setValue(SESSION_DIST_PAGEPKGS_PREORDER, "false");
    }

	// Asks the SetPagingResultsTag to reset the results from
	// the DistributionBean
	session.setAttribute(SESSION_PERSIST_RESETRESULTS, "true");

	return (mapping.findForward("success"));
    }

    private void modifyOrder(ArrayList channelList, ArrayList modifiedPriorityChannels, InstallPriority ip) throws GUIException {         
         ip.setExistingChannels(channelList);
         if(modifiedPriorityChannels.size() >0 ) {
            ip.setNewChannelList(modifiedPriorityChannels);
            ip.execute();
         }
     }

    private void revertPriority( ArrayList channelList, Hashtable preState, InstallPriority ip){        
        ip.reverPriority( channelList, preState );
    }

     public void resetUpdateChannelOrders(DistAsgForm form) {
        form.removeChangeOrders();
     }
    /**
     * Validates the starting priority.
      * @param channelList
     * @param startingPriority
     */
    private void validate(ArrayList channelList, int startingPriority) throws GUIException {
         if(startingPriority < 1 ||
                 (startingPriority + channelList.size()) > DistAsgValidateAction.MAX_INSTALL_PRIORITY) {
             throw new GUIException(new KnownException(IErrorConstants.ASSIGN_PRIORITY_EXCEED));
         }
    }
    /**
     * initiallize the channel order, so that all
     * the order of the channels in this policy
     * are concurrent and starts at the starting priority.
     *
     * @param channelList the list of channels directly
     * assigned to this policy.
     */
    private void initializeOrder(DistAsgForm form, ArrayList channelList, int startingPriority, HttpServletRequest request, InstallPriority ip) {
        for (int i = 0; i < channelList.size(); i++) {
        	if(request.getSession().getAttribute(SESSION_MULTITGBOOL)!=null){
        		startingPriority = ip.findNextPriority(startingPriority);
        	}
            Channel channel = (Channel) channelList.get(i);
            String maintainInc = (String)form.getValue("changeOrderInc#" + channel.hashCode());
            if( maintainInc == null || "false".equals(maintainInc)){
                channel.setOrder(startingPriority ++ );
            }
        }
    }
    /**
     * Move the selected channel up in the install order by 1
     * @param channelList the list of channels directly
     * assigned to this policy.
     * @param item the priority of the selected channel
     */
    private void moveUp(ArrayList channelList, int item) {
	// if it is already the top dog, can't do any better.
	// in fact, the GUI prevents this case, but...
	if (item == 0) {
	    return;
	}

	// else push this channel up by one
	Channel channel = (Channel) channelList.get(item);
	channel.setOrder(channel.getOrder() - 1);

	// and then push down the one on top
	channel =  (Channel) channelList.get(item - 1);
	channel.setOrder(channel.getOrder() + 1);
    }

    /**
     * Move the selected channel down in the install order by 1
     * @param channelList the list of channels directly
     * assigned to this policy.
     * @param item the priority of the selected channel
     */
    private void moveDown(ArrayList channelList, int item) {
	// if it is already the, can't get any worse.
	// in fact, the GUI prevents this also, but...
	if (item == (channelList.size() - 1)) {
	    return;
	}

	// else push this channel down by one
	Channel channel = (Channel) channelList.get(item);
	channel.setOrder(channel.getOrder() + 1);

	// and then push up the one below
	channel =  (Channel) channelList.get(item + 1);
	channel.setOrder(channel.getOrder() - 1);
    }

    /**
     * Move the selected channel to the top
     * @param channelList the list of channels directly
     * assigned to this policy.
     * @param item the priority of the selected channel
     */
    private void moveToTop(ArrayList channelList, int item) {
	// if it is already the top dog, can't do any better.
	// in fact, the GUI prevents this case, but...
	if (item == 0) {
	    return;
	}

	// else push this channel up all the way up
	Channel topChannel = (Channel) channelList.get(0);
	Channel channel = (Channel) channelList.get(item);
	channel.setOrder(topChannel.getOrder());

	// and then push down all the ones on top by one
	for (int i = 0; i < item; i++) {
	    channel =  (Channel) channelList.get(i);
	    channel.setOrder(channel.getOrder() + 1);
	}
    }

    /**
     * Move the selected channel to the bottom
     * @param channelList the list of channels directly
     * assigned to this policy.
     * @param item the priority of the selected channel
     */
    private void moveToBottom(ArrayList channelList, int item) {
	// if it is already the, can't get any worse.
	// in fact, the GUI prevents this also, but...
	if (item == (channelList.size() - 1)) {
	    return;
	}

	// else push this channel down all the way to the bottom
	Channel bottomChannel = (Channel) channelList.get(channelList.size() - 1);
	Channel channel = (Channel) channelList.get(item);
	channel.setOrder(bottomChannel.getOrder());

	// and then push up all the ones below by one
	for (int i = item + 1; i < channelList.size(); i++) {
	    channel =  (Channel) channelList.get(i);
	    channel.setOrder(channel.getOrder() - 1);
	}
    }
}
