package com.marimba.apps.subscriptionmanager.arsystem;

import com.marimba.apps.subscription.common.*;
import com.marimba.apps.subscription.common.intf.SubInternalException;
import com.marimba.apps.subscription.common.util.LDAPUtils;
import com.marimba.apps.subscription.common.objects.Target;
import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.tools.ldap.LDAPConnection;
import com.marimba.tools.ldap.LDAPName;
import com.marimba.webapps.intf.CriticalException;
import com.marimba.webapps.intf.SystemException;
import com.marimba.intf.msf.arsys.IARConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.naming.NamingException;
import java.util.*;

/**
 * File extracts target information from the webservice XML Element
 *
 * @author Devendra Vamathevan
 * @version 7.0.0.0 08/21/200
 */
public class ARTargetSource {

	private LDAPEnv ldapenv;
	private LDAPConnection conn;
	Map<String, String> LDAPVarsMap; 


	ARTargetSource(LDAPEnv ldapenv, LDAPConnection conn) {
		this.ldapenv = ldapenv;
		this.conn = conn;
		LDAPVarsMap = LDAPUtils.getLDAPVarStringValues(this.ldapenv.getLDAPConfigProperty("vendor"));
	}

	/**
	 * @param elem dom element ocontaining targets
	 * @return comma separated string of tagets
	 */
	private String getTargetsAsString(Element elem) throws SystemException {

		if (elem == null) {
            ARUtils.debug("GetTargetAsString: Empty Document");
			return null;
		}

		NodeList nodelist = elem.getElementsByTagNameNS(IARConstants.ARELEMENT_NAMESPACE, IARConstants.TAG_ASSOCIATIONQUERY);
        ARUtils.debug("GetTargetAsString: Nodelist Size: "+nodelist.getLength());

		StringBuffer targetn = new StringBuffer(1024);
		for (int i = 0; i < nodelist.getLength(); i++) {
			Node node = nodelist.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				NodeList targetNodelList = ((Element) node).getElementsByTagNameNS(IARConstants.ARELEMENT_NAMESPACE, IARConstants.TAG_ASSOCIATION);
                ARUtils.debug("GetTargetAsString: TargetNodeList Size: "+targetNodelList.getLength());

				for (int j = 0; j < targetNodelList.getLength(); j++) {
					if (targetNodelList.item(j).getNodeType() == Node.ELEMENT_NODE) {
						Element ci = (Element) targetNodelList.item(j);
						String relationType = ARUtils.getChildElemStringValue(IARConstants.ARELEMENT_NAMESPACE, ci, IARConstants.TAG_ITEM_TYPE);
						String targetName =  ARUtils.getChildElemStringValue(IARConstants.ARELEMENT_NAMESPACE, ci, IARConstants.TAG_ITEM_NAME);
                        String targetType = ARUtils.getChildElemStringValue(IARConstants.ARELEMENT_NAMESPACE, ci, IARConstants.RELATED_ITEM_TYPE_CI);
                        String targetDN = ARUtils.getChildElemStringValue(IARConstants.ARELEMENT_NAMESPACE, ci, IARConstants.TAG_ITEM_DETAILS);

                        if(targetType.toLowerCase().indexOf("ldap") != -1) {
                            targetName = targetDN;
                        } else if (targetType.toLowerCase().indexOf("configuration") != -1) {
                            try {
                                if(!(conn.getParser().isDN(targetName)) && !(targetName.equalsIgnoreCase("All Endpoints"))) {
                                    StringBuffer sb = new StringBuffer();
                                    sb.append(targetName);
                                    sb.append(":");
                                    sb.append("machine");
                                    targetName = sb.toString();
                                }
                            } catch (NamingException ne) {
                                LDAPUtils.classifyLDAPException(ne);
                            }
                        }
						if (IARConstants.ITEMTYPE_TARGET.equals(relationType)) {
							targetn.append("\"");
							targetn.append(targetName);
							targetn.append("\",");
                            ARUtils.debug("GetTargets: Relationship Item Type: "+relationType);
                            ARUtils.debug("GetTargets: Target Name: "+targetName+ "with Target Type: "+targetType);
						}
					}
				}
			}
		}
		if (targetn.length() < 1) {
			return null;
		}
		// remove last , since it is not follwed by any other target
		if (',' == targetn.charAt(targetn.length() - 1)) {
			targetn.deleteCharAt(targetn.length() - 1);
		}
		return targetn.toString();
	}


	/**
	 * @param elem dom element ocontaining targets
	 * @return list containing targets
	 * @throws SystemException
	 */

	List getTargets(Element elem) throws SystemException {
        return getTargets(getTargetsAsString(elem));
    }

    List getTargets(String targetn) throws SystemException {

		ArrayList targets = new ArrayList();
		List allEntered = new ArrayList();
		List validEntries = new ArrayList();
		List invalidEntries = new ArrayList();
		Map dntable = new HashMap();
		Map duptable = new HashMap();
		Map objectclass = new HashMap();


		ldapenv.checkIfProvisionKeysExist(null, allEntered, validEntries,
		        invalidEntries, dntable, duptable, objectclass, targetn);

		if (invalidEntries.size() > 0) {

			Iterator itinv = invalidEntries.iterator();
			int error = 0;
            String name=null;
			while (itinv.hasNext()) {
				error++;
                name = (String)itinv.next();
                ARUtils.debug("GetTargets: Invalid Targets: "+name);
			}
			itinv = duptable.keySet().iterator();

			while (itinv.hasNext()) {
				error++;
				String[] targetsArray = (String[]) itinv.next();
				if(targetsArray != null) {
					for(int count=0; count < targetsArray.length; count++) {
						ARUtils.debug("GetTargets: Duplicate Targets: "+targetsArray[count]);
					}
				}
			}
			throw new CriticalException(IErrorConstants.DIST_SAVE_TARGET_NOTFOUND, name);
		}

		Iterator iterator = dntable.keySet().iterator();
		LDAPName ldapName = null;
		try {
			ldapName = conn.getParser();
		} catch (NamingException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}

		while (iterator.hasNext()) {
			String key = (String) iterator.next();
            ARUtils.debug("GetTargets: Target Name: "+key+ " Target ID: "+dntable.get(key)+ " Target Type: "+objectclass.get(key));
			String targetName = null;
			try {
				if (ldapName.isDN(key)) {
					targetName = ldapName.getCN(key);
				} else if (key.indexOf('.') > 0) {
					targetName = key.substring(key.indexOf('.') - 1);
				} else {
					targetName = key;
				}
			} catch (NamingException e) {
				// e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
			}
			Target target = new Target(targetName, (String) objectclass.get(key), (String) dntable.get(key));
            ARUtils.debug("GetTargets: Target Object: "+target.toString());
			targets.add(target);
		}
		return targets;
	}

    public String getTargetsToVerify(Element form)  throws SystemException  {
        List targetObjs = getTargets(form);
        //sample format of
        //targets = "\"dc=marimba,dc=com\" \"container\" \"1\"";
        //targets = "\"CN=fox,CN=computers,dc=marimba,dc=com\" \"machine\" \"2\"";
        String targetsStr = null;
        if (targetObjs == null || targetObjs.size() == 0)     {
            return targetsStr;
        }
        Target tgt = null;
        int len = targetObjs.size();
        StringBuffer strbuf = new StringBuffer();
        for (int i=0; i<len; i++)   {
            tgt = (Target) targetObjs.get(i);
            strbuf.append("\"");
            strbuf.append(tgt.getId());
            strbuf.append("\" \"");
            String type = null;
            try {
                type = LDAPUtils.objClassToTargetType(tgt.getType(), LDAPVarsMap);
            }catch(SubInternalException sie) {
                //ignore the exception if the target type is resolved.
            }
            strbuf.append(type);
            strbuf.append("\"");
        }
        ARUtils.debug("GetTargetsToVerify: "+strbuf.toString());
        return strbuf.toString();
    }
}
