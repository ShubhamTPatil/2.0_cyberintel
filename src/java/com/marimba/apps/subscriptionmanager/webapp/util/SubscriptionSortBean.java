// Copyright 1997-2003, Marimba Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.
// $File$, $Revision$, $Date$
package com.marimba.apps.subscriptionmanager.webapp.util;

import java.util.*;

import com.marimba.webapps.intf.*;
import com.marimba.webapps.tools.view.*;

/**
 * Handles the Sorting for Subscription
 *
 * @author Narasimhan L Mahendrakumar
 * @version 1.00, 9/23/2003
 */

 public class SubscriptionSortBean extends SortBean {
    final static boolean DEBUG = com.marimba.apps.subscriptionmanager.intf.IAppConstants.DEBUG;
	
	List list;
	
	public SubscriptionSortBean() { }

	public SubscriptionSortBean(List list) {
		this.list = list;
	}

	public static Collection sortList(Collection collection, Comparator comparator) {
		Object[] sortArray = collection.toArray();
		Arrays.sort(sortArray, comparator);
		List newList = new ArrayList(Arrays.asList(sortArray));
		return newList;
	}
	
	public Comparator getComparator(String viewAttribute, String sortField, int sortOrder) {
		return new SubsComparator(sortField, sortOrder); // default comparator
    }

	public class SubsComparator extends BaseComparator {
		
		public SubsComparator(String sortField, int sortOrder) {
			super(sortField,sortOrder);
		}

		public int compare(Object o1, Object o2) {
			int result = 0;
			try {
				// compare attributes of IBasicBeans
				if (o1 instanceof IMapProperty && o2 instanceof IMapProperty) {
					IMapProperty bean1 = (IMapProperty) o1;
					IMapProperty bean2 = (IMapProperty) o2;
					if (sortOrder == SORT_NONE) {
						String name1 = bean1.getValue(sortField).toString();
						String name2 = bean2.getValue(sortField).toString();
						result = compareString(name1, name2);
					} else {  
						
						String name1 = bean1.getValue(sortField).toString();
						String name2 = bean2.getValue(sortField).toString();
						result = compareString(name1, name2);
					}
				} else { // compare as Strings
					String s1 = o1 == null ? null : o1.toString();
					String s2 = o2 == null ? null : o2.toString();
					result = compareString(s1, s2);
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			return result;
		}
	}
 }
