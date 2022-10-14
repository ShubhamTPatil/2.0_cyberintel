// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.cli.commands;

import java.util.*;

import com.marimba.apps.subscriptionmanager.webapp.util.ChannelComparator;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.apps.subscription.common.ISubscriptionConstants;

/**
 * Contains algorithm to change the priority of the channels in the policy.
 *
 * @author Kumaravel Ayyakkannu
 * @version $Revision$, $Date$
 */
public class InstallPriority {
     static ChannelComparator comparator = new ChannelComparator(ISubscriptionConstants.CH_INSTALL_PRIORITY_KEY);
	 List channelsToModify;
	 List existingChannels;
	 HashSet<Integer> usedPriorities;
	 
	 public InstallPriority () {
		  channelsToModify = new ArrayList();
		  existingChannels = new ArrayList();
		  usedPriorities = new HashSet<Integer>();
	 }
	 
	 public void setUsedPriorities(HashSet<Integer> usedPriorities) {
		this.usedPriorities = usedPriorities;
	}

	public void close() {
		 existingChannels.clear();
	 }
	 public void setNewChannelList(List list) {
		  channelsToModify.addAll(list);
	 }
	 public void setExistingChannels(List list) {
		 existingChannels.addAll(list);
	 }
	 public List getResultChannels() {
		 execute();
		 return existingChannels;
	 }
     public boolean initializeOrder() {

         if( !isAlreadyPrioritized() ) {
             if(!isExceedingMaxPriority()) {
                 for (int i = 0; i < existingChannels.size(); i++) {
                     Channel channel = (Channel) existingChannels.get(i);
                     channel.setOrder(findNextPriority(i + 1));
                 }
                return false;
             } else {
                return true;
             }
         } else {
             return false;
         }
     }
     public boolean isExceedingMaxPriority() {
         boolean initialize = false;
         if((1 + existingChannels.size()) > ISubscriptionConstants.MAX_INSTALL_PRIORITY) {
             initialize = true;
         }
         return initialize;
     }
     public boolean isAlreadyPrioritized() {

        boolean alreadyPrioritized = false;
        for ( int i = 0; i < existingChannels.size() ; i++ ) {
            if( ((Channel)existingChannels.get(i)).getOrder() != ISubscriptionConstants.MAX_INSTALL_PRIORITY) {
              alreadyPrioritized = true;
              break;
            }
        }
         return alreadyPrioritized;
     }
    /**
     * Sorts the list of the channels present in the policy. It performs modification on priority of the channels one by one
     * entered in the command line and rearranges the same.
     */
	 public void execute() {
		   Collections.sort(existingChannels, comparator );
		   rearrange(existingChannels);
		   for( int i = 0;  i < channelsToModify.size() ; i++ ) {
			   modifyChannelPriority(existingChannels, (Channel)channelsToModify.get(i));
			   Collections.sort(existingChannels, comparator );
		   }
	 }
    /**
     * rearranges the priorities of the existing channels.
     * @param list
     */
	 public void rearrange(List list) {

		 Object a[] = list.toArray();
             for ( int i=0; i < a.length - 1 ; i++ ) {
                   int order1 = ((Channel)a[i]).getOrder();
                   int order2 = ((Channel)a[i+1]).getOrder();

                   if( order1 != ISubscriptionConstants.MAX_INSTALL_PRIORITY ) {
                       if( order1 == order2 ) {  // Is collision present ?
                           if( (order1 + 1) < ISubscriptionConstants.MAX_INSTALL_PRIORITY ) {
                           		((Channel)a[i+1]).setOrder(findNextPriority(((Channel)a[i]).getOrder()+1));
                           }
                       }
                   }
             }
             ListIterator i = list.listIterator();
             for (int j = 0; j < a.length; j++) {
                 i.next();
                 i.set(a[j]);
             }
	 }
    /** Modifies the existing channel's priority in the list
     *
     * @param list
     * @param channel
     */
	 public void modifyChannelPriority( List list, Channel channel) {

        Object a[] = list.toArray();
        int startIndex = 0 ;
        boolean isSameOrder = false;
        boolean isSameURL = false;
        boolean toFinish1 = false;
        boolean toFinish2 = false;

        for( int i=0; i < a.length ; i++ ) {
           isSameOrder = (((Channel) a[i]).getOrder())== (channel.getOrder());
           isSameURL = (((Channel) a[i]).getUrl()).equals(channel.getUrl());
            if(isSameOrder) {
                toFinish1 = true;
                if(channel.getOrder()+1 < ISubscriptionConstants.MAX_INSTALL_PRIORITY ) {
                  ((Channel)a[i]).setOrder(findNextPriority(channel.getOrder()+1));
                    startIndex = i; // Get the index to change the order from where the collision occurs.
                }
            }
            if(isSameURL) {
                toFinish2 = true;
                ((Channel)a[i]).setOrder(channel.getOrder());
            }
            if( toFinish1 && toFinish2  ) {
                break;
            }
        }
        ListIterator i = list.listIterator();
        for (int j = 0; j < a.length; j++) {
            i.next();
            i.set(a[j]);
        }
        // Send only the sublist to rearrange.
        rearrange(list.subList(startIndex, list.size()));
	 }

     public void reverPriority( ArrayList curState, Hashtable preList ){
         Channel curApp = null;
         for( int index = 0; index < curState.size(); index++ ) {
             curApp = ( Channel )curState.get( index );
             if( preList.get(curApp.getUrl()) != null ) {
                    curApp.setOrder( ( (Channel)preList.get(curApp.getUrl())).getOrder());    
             }
         }
     }
     
     public int findNextPriority(int nextPriority){
  	   while(usedPriorities.contains(nextPriority)){
  		   nextPriority ++;   
  	   }
  	   return nextPriority;
     }
 }
