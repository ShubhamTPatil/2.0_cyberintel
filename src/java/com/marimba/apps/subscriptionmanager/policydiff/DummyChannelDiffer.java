package com.marimba.apps.subscriptionmanager.policydiff;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.marimba.apps.subscriptionmanager.intf.IPolicyDiffConstants;
import com.marimba.apps.subscription.common.objects.Channel;
import com.marimba.webapps.intf.SystemException;

public class DummyChannelDiffer implements IPolicyDiffConstants {
    String KEY_OLD_VALUE = "oldvalue";
    String KEY_NEW_VALUE = "newvalue";
    private Map<String, String> addedPropsMap;
    private Map<String, Map<String, String>> modifiedPropsMap;
    private Map<String, String> deletedPropsMap;
    private Channel oldChannel, newChannel;
    String mode;
    
	public DummyChannelDiffer(Channel oldChannel, Channel newChannel, String mode) {
        this.oldChannel = oldChannel;
        this.newChannel = newChannel;
        this.mode = mode;
        this.addedPropsMap = new LinkedHashMap<String, String>();
        this.modifiedPropsMap = new LinkedHashMap<String, Map<String, String>>();
        this.deletedPropsMap = new LinkedHashMap<String, String>();
		if("add".equalsIgnoreCase(mode)) {
			calcAddedProbs();
		} else if("update".equalsIgnoreCase(mode)) {
			calcModifiedProps();
		} else if("delete".equalsIgnoreCase(mode)) {
			calcDeletedProbs();
		}

	}
    public String getKEY_OLD_VALUE() {
		return KEY_OLD_VALUE;
	}
	public void setKEY_OLD_VALUE(String kEY_OLD_VALUE) {
		KEY_OLD_VALUE = kEY_OLD_VALUE;
	}
	public String getKEY_NEW_VALUE() {
		return KEY_NEW_VALUE;
	}
	public void setKEY_NEW_VALUE(String kEY_NEW_VALUE) {
		KEY_NEW_VALUE = kEY_NEW_VALUE;
	}
	public Map<String, String> getAddedPropsMap() {
		return addedPropsMap;
	}
	public void setAddedPropsMap(Map<String, String> addedPropsMap) {
		this.addedPropsMap = addedPropsMap;
	}

	public Map<String, Map<String, String>> getModifiedPropsMap() {
		return modifiedPropsMap;
	}
	public void setModifiedPropsMap(
			Map<String, Map<String, String>> modifiedPropsMap) {
		this.modifiedPropsMap = modifiedPropsMap;
	}
	public Map<String, String> getDeletedPropsMap() {
		return deletedPropsMap;
	}
	public void setDeletedPropsMap(Map<String, String> deletedPropsMap) {
		this.deletedPropsMap = deletedPropsMap;
	}
	public boolean hasDummyChannelChanged() {
		if("add".equalsIgnoreCase(mode)) {
			return !addedPropsMap.isEmpty();
		} else if("update".equalsIgnoreCase(mode)) {
			return !modifiedPropsMap.isEmpty();
		} else if("delete".equalsIgnoreCase(mode)) {
			return !deletedPropsMap.isEmpty();
		}
		return false;
	}
	private void calcAddedProbs() {
        if (null == newChannel) {
            return;
        }
        Enumeration oldProperties, newProperties;
            try {
                if(null != oldChannel) {
	            	oldProperties = oldChannel.getPropertyKeys();
	                newProperties = newChannel.getPropertyKeys();
	
	                List<String> oldkeys = new ArrayList<String>();
	                List<String> newkeys = new ArrayList<String>();
	
	                while(oldProperties.hasMoreElements()) {
	                    oldkeys.add((String) oldProperties.nextElement());
	                }
	
	                while(newProperties.hasMoreElements()) {
	                    newkeys.add((String) newProperties.nextElement());
	                }
	                if (newkeys.containsAll(oldkeys)) {
	                    newkeys.removeAll(oldkeys);
	                    for (String keyval : newkeys) {
	                        if (null != newChannel.getProperty(keyval)) {
	                            addedPropsMap.put(keyval, newChannel.getProperty(keyval));
	                        }
	                    }
	                }
                } else {
            		String[] propertyPairs = newChannel.getPropertyPairs();

                    for (int count = 0; count < propertyPairs.length; count += 2) {
                    	addedPropsMap.put(propertyPairs[count], propertyPairs[count + 1]);
                    }

                }

            }  catch (Exception ex) {
            	System.out.println("Failed to calculate added channel props");
            }
            System.out.println("addedPropsMap .size() " + addedPropsMap.size());
    }

    private void calcModifiedProps() {
        if (null == oldChannel || null == newChannel) {
            return;
        }

        String oldvalue;
        String tmpstr;

        Map<String, String> tmpMap;
            try {
                String[] oldSubpairs = oldChannel.getPropertyPairs();
                for (int i = 0, length = oldSubpairs.length; i < length; i += 2) {
                    oldvalue = newChannel.getProperty(oldSubpairs [i]);
                    if (hasChanged(oldSubpairs [i])) {
                        if (null != oldvalue && !oldSubpairs [i + 1].equals(oldvalue)) {
                            tmpMap = new HashMap<String, String>(2);
                            tmpstr = oldChannel.getProperty(oldSubpairs[i]);
                            tmpMap.put(KEY_OLD_VALUE, tmpstr.equals(STR_NULL) ? STR_NONE : tmpstr );
                            tmpstr = newChannel.getProperty(oldSubpairs[i]);
                            tmpMap.put(KEY_NEW_VALUE, tmpstr.equals(STR_NULL) ? STR_NONE : tmpstr );
                            modifiedPropsMap.put(oldSubpairs[i], tmpMap);
                        }
                    }
                }

            } catch (Exception ex) {
                // Skip it now
            	System.out.println("Failed to calculate modified channel props");
            }
            System.out.println("modifiedPropsMap .size() " + modifiedPropsMap.size());
    }

    private void calcDeletedProbs() {
        if (null == oldChannel) {
            return;
        }
        String key;
            try {
                Enumeration oldproperties = oldChannel.getPropertyKeys();
                while(oldproperties.hasMoreElements()) {
                    key = (String) oldproperties.nextElement();
                    if (null == newChannel || null == newChannel.getProperty(key)) {
                        deletedPropsMap.put(key, oldChannel.getProperty(key));
                    }
                }

            }  catch (Exception ex) {
                // Skip it now
            	System.out.println("Failed to calculate Deleted channel props");
            }
            System.out.println("deletedPropsMap .size() " + deletedPropsMap.size());
    }
    private boolean hasChanged(String key)  throws SystemException {

        String val = oldChannel.getProperty(key);
        if (null == val) {
            return true;
        }
        // it hasn't changed if and only val equals the value in the newchannel.
        return !val.equals(newChannel.getProperty(key));
    }

}
