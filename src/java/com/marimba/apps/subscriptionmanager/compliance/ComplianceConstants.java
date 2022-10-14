// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.

package com.marimba.apps.subscriptionmanager.compliance;

import com.marimba.apps.subscription.common.intf.IComplianceConstants;

import java.util.HashSet;
import java.util.Set;

/**
 * REMIND
 *
 * @author Gregory Gerard
 * @version $File$
 */
public class ComplianceConstants implements IComplianceConstants {
    public final static String sComplianceTypePolicy = "policy"; // was TYPE_POLICY
    public final static String sComplianceTypeTarget = "target"; // was TYPE_TARGET
    public final static int    sComplianceScrubberDoNothing = 0x00000000;
    public final static int    sComplianceScrubberRemoveEntriesWithInvalidSubscriptions = 0x00000001;
    public final static int    sComplianceScrubberRemoveSynthesizedEntries = 0x00000002;
    public final static int    sComplianceScrubberInvalidateEntriesWithOldPolicies = 0x00000004;
    public final static int    sComplianceScrubberSynthesizeEntriesThatShouldExist = 0x00000008;
    public final static int    sComplianceScrubberDeleteErroneouslySynthesizedEntries = 0x00000010;
    public final static int    sComplianceScrubberInvalidateEntriesWithOldPackages = 0x00000020;
    public final static int    sComplianceScrubberDoEverything = sComplianceScrubberRemoveEntriesWithInvalidSubscriptions | sComplianceScrubberRemoveSynthesizedEntries | sComplianceScrubberInvalidateEntriesWithOldPolicies | sComplianceScrubberSynthesizeEntriesThatShouldExist | sComplianceScrubberDeleteErroneouslySynthesizedEntries | sComplianceScrubberInvalidateEntriesWithOldPackages;

    // new constants    
    public final static String sComplianceLevelTotalString = "TOTAL";
    public final static String sComplianceLevelCompliantString = "COMPLIANT";
    public final static String sComplianceLevelNonCompliantString = "NON-COMPLIANT";
    public final static String sComplianceLevelUnknownString = "NOT-CHECKED-IN";
    public final static String sSQLReplacementString = "'REPLACE_THIS_INCLUDING_THE_QUOTE'";
    public final static String sSubscriptionServiceComplianceAgentString = IComplianceConstants.COMPLIANCE_VAL_AGENT_SUBSCRIPTION;
    public final static String sSubscriptionManagerComplianceAgentString = IComplianceConstants.COMPLIANCE_VAL_POLICY_AGENT_SUBSCRIPTION;
    public final static String sSubscriptionManagerPolicyAgentString = sSubscriptionManagerComplianceAgentString;
    public final static Set    sPolicyAgentsStringSet = new HashSet();
    public final static Set    sComplianceAgentsStringSet = new HashSet();

    static {
        sPolicyAgentsStringSet.add(ComplianceConstants.sSubscriptionManagerPolicyAgentString);
        sComplianceAgentsStringSet.add(ComplianceConstants.sSubscriptionManagerComplianceAgentString);
        sComplianceAgentsStringSet.add(ComplianceConstants.sSubscriptionServiceComplianceAgentString);
    }

    public final static String sScrubberFlagString = "subscriptionmanager.compliance.scrubber.bitFlags";
    public final static String JSQL_JDBC_DRIVER_NAME = "com.jnetdirect.jsql.JSQLConnection";

}
