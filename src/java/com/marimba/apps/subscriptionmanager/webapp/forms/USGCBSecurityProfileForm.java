package com.marimba.apps.subscriptionmanager.webapp.forms;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import javax.servlet.http.HttpServletRequest;
import com.marimba.webapps.tools.util.KnownActionError;
import com.marimba.apps.subscriptionmanager.webapp.util.*;
import com.marimba.tools.config.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class USGCBSecurityProfileForm extends AbstractForm {
	String status;
	String statusDesc;
	String name;
	String description;
	String forceApplyEnabled;
	String action;
	String selectedProfile = null;
	final String FALSE = "false"; 
	String selectedSCAPContent;
	String selectedSCAPProfile;
	String selectedSCAPProfileSelect;
	String scapEnabled = "false";
	String create = "false";
	private Map<String, String> affectedTargets = new Hashtable<String, String>();
    private boolean targetsAffected = false;
    String modifiedRules;
    String initialIFramePath;

	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getStatusDesc() {
		return statusDesc;
	}
	public void setStatusDesc(String statusDesc) {
		this.statusDesc = statusDesc;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getForceApplyEnabled() {
		return forceApplyEnabled;
	}
	public void setForceApplyEnabled(String forceApplyEnabled) {
		this.forceApplyEnabled = forceApplyEnabled;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	
	public String getSelectedSCAPContent() {
		return selectedSCAPContent;
	}
	public void setSelectedSCAPContent(String selectedSCAPContent) {
		this.selectedSCAPContent = selectedSCAPContent;
	}

	public String getScapEnabled() {
		return scapEnabled;
	}
	public void setScapEnabled(String scapEnabled) {
		this.scapEnabled = scapEnabled;
	}
	public Map<String, String> getAffectedTargets() {
		return affectedTargets;
	}
	public void setAffectedTargets(Map<String, String> targets) {
		affectedTargets = targets;

		if (affectedTargets != null && affectedTargets.size() > 0) {
			targetsAffected = true;
		} else {
			targetsAffected = false;
		}
	}

	public Set getAffectedTargetsSet() {
		return affectedTargets.keySet();
	}

	public Map<String, String> getAffectedTargetsHash() {
		return affectedTargets;
	}

	public boolean getTargetsAffected() {
		return targetsAffected;
	}

	public void setTargetsAffected(boolean targetsAffected) {
		this.targetsAffected = targetsAffected;
	}

	public void initialize() {
        super.initialize();
        setStatus("");
        setStatusDesc("");
    }

    public void reset(ActionMapping mapping, HttpServletRequest request) {
        System.out.println("ActionDebug: Reset method called(mapping, request)");
        setStatus("");
        setStatusDesc("");
    }

    public void reset() {
        setName("");
        setDescription("");
        setSelectedSCAPContent("");
        setForceApplyEnabled(FALSE);
        setSelectedSCAPProfile("");
        selectedProfile = null;
    }

    public void setSelectedProfile(String selectedProfile) {
        this.selectedProfile = selectedProfile;
    }

    public String getSelectedProfile() {
        return selectedProfile;
    }
    
    public String getCreate() {
		return create;
	}

	public void setCreate(String create) {
		this.create = create;
	}

	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();

        if (("save".equals(request.getParameter("action"))) || ("apply".equals(request.getParameter("action")))) {
            if (((name == null) || (name.trim().length() < 1))) {
                errors.add("Profile Name", new KnownActionError(VALIDATION_POWERPROFILE_EMPTY));
            } else {
                // Remove multiple space in between two words.
                name = name.replaceAll("\\s+", " ");
                
                if (!Pattern.matches("[\\w\\s+]+", name)) {
                    errors.add("Profile Name", new KnownActionError(VALIDATION_POWERPROFILE_SPLCHAR));
                }
            }
            if (((description == null) || (description.trim().length() < 1))) {
                errors.add("Profile desc", new KnownActionError(VALIDATION_PROFILE_DESCRIPTION));
            }
        }

        return errors;
    }

    public void loadProfile(Hashtable props) {
        System.out.println("USGCBSecurityProfileForm:: Loading profile: " + props.get(USGCB_SECURITY_PROFILE_NAME));
        reset();
        System.out.println("USGCBSecurityProfileForm:: Loading profile: after reset() method...");

        try {
            scapEnabled = (String) props.get(USGCB_SECURITY_ENABLED);
            name = (String) props.get(USGCB_SECURITY_TEMPLATE_NAME);
            description = (String) props.get(USGCB_SECURITY_TEMPLATE_DESC);
            forceApplyEnabled = (String) props.get(USGCB_SECURITY_FORCE_APPLY);
            selectedSCAPContent = (String) props.get(USGCB_SECURITY_SCAP_SELECTEDCONTENT_FILENAME);
            String selectedProfileId = (String) props.get(USGCB_SECURITY_SCAP_SELECTEDPROFILE_ID);
			if ((selectedSCAPContent != null) && (selectedProfileId != null)) {
				//ignore...
			} else {
				if (SCAPUtils.getSCAPUtils().getSupportedUSGCBContents() != null) {
					selectedSCAPContent = SCAPUtils.getSCAPUtils().getSupportedUSGCBContentsXML()[0];
					selectedProfileId = (String) (SCAPUtils.getSCAPUtils().getProfilesForScapContent(selectedSCAPContent).keySet().toArray()[0]);
				}
			}
			initialIFramePath = "/spm/securitymgmt?command=gethtml&target=windows&customize=true&content=" + selectedSCAPContent + "&profile=" + selectedProfileId;
			if ((name != null) && name.trim().length() > 0) {
				initialIFramePath += "&template=" + name + ".properties";
			}
			setSelectedSCAPProfile(selectedProfileId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("USGCBSecurityProfileForm:: Loading profile: method leaving...");
    }

	public String getSelectedSCAPProfile() {
		return selectedSCAPProfile;
	}

	public void setSelectedSCAPProfile(String selectedSCAPProfile) {
		this.selectedSCAPProfile = selectedSCAPProfile;
	}

	public String getSelectedSCAPProfileSelect() {
		return selectedSCAPProfileSelect;
	}

	public void setSelectedSCAPProfileSelect(String selectedSCAPProfileSelect) {
		this.selectedSCAPProfileSelect = selectedSCAPProfileSelect;
	}

	public String getModifiedRules() {
        return modifiedRules;
    }

    public void setModifiedRules(String modifiedRules) {
        this.modifiedRules = modifiedRules;
    }

    public String getInitialIFramePath() {
        return initialIFramePath;
    }

    public void setInitialIFramePath(String initialIFramePath) {
        this.initialIFramePath = initialIFramePath;
    }
}
