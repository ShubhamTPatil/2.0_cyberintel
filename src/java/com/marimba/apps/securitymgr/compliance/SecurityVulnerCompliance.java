package com.marimba.apps.securitymgr.compliance;

import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.beans.SecurityProfileDetailsBean;
import com.marimba.apps.subscriptionmanager.beans.SecurityTargetDetailsBean;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.view.MachineBean;
import com.marimba.apps.subscriptionmanager.compliance.view.MachineListBean;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.apps.subscriptionmanager.webapp.util.Utils;
import com.marimba.intf.db.IStatementPool;
import com.marimba.tools.util.TimeUtil;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class SecurityVulnerCompliance implements ComplianceConstants {
	public static class GetGroupCompliance extends DatabaseAccess {
        SubscriptionMain main = null;
        MachineListBean machineListBean = null;
        public GetGroupCompliance (SubscriptionMain main, SecurityTargetDetailsBean securityTargetDetailsBean, String selectedTargetId) {
            GetGroupComplianceData getTargetComplianceData = new GetGroupComplianceData(main, securityTargetDetailsBean, selectedTargetId );
            try {
                runQuery(getTargetComplianceData);
                machineListBean= getTargetComplianceData.getMachineListBean();

            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }
        public MachineListBean getMachineListBean() {
            return machineListBean;
        }
    }

    static class GetGroupComplianceData extends QueryExecutor {

        SecurityTargetDetailsBean securityTargetDetailsBean = null;

        MachineListBean machineListBean = new MachineListBean();
        List<MachineBean> list = new ArrayList<MachineBean>();
        String selectedTargetId = null;

        GetGroupComplianceData (SubscriptionMain main,SecurityTargetDetailsBean securityTargetDetailsBean, String selectedTargetId) {
            super(main);
            this.securityTargetDetailsBean = securityTargetDetailsBean;
            this.selectedTargetId = selectedTargetId;
        }
        protected void execute(IStatementPool pool) throws SQLException {
        	boolean isRecord = false;
            try {
            	String targetID = securityTargetDetailsBean.getAssignedToID();
            	if("all".equalsIgnoreCase(targetID)) {
            		targetID = "all_all";
            	}
            	if("all".equalsIgnoreCase(selectedTargetId)) {
            		selectedTargetId = "all_all";
            	}
            	String queryStr = "select machine_name, machine_id,overall_compliant_level from " +
                "inv_security_oval_compliance isxc where " +
                "UPPER(assigned_target_name) = UPPER('"+ targetID +"')" +
                "and UPPER(content_title) = UPPER('"+ securityTargetDetailsBean.getSelectedSecurityContentName() +"')" +
                "and UPPER(profile_name) = UPPER('"+ securityTargetDetailsBean.getSelectedProfileId() +"')" +
                "and exists (" +
                		"select 1 from inv_subscriptiontargets ist " +
                		"where UPPER(target_name) = UPPER('"+ selectedTargetId +"')" +
                		"and isxc.machine_id = ist.machine_id)";
            	System.out.println("Query Str :" + queryStr);
                PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
                ResultSet rs = st.executeQuery();

                try {
                    while (rs.next()) {
//                    	isRecord = true;
                    	//setComplianceLevel(rs.getString(2));
                        MachineBean machineBean = new MachineBean(rs.getString(1));
                        machineBean.setMachineID(rs.getString(2));
                        machineBean.setComplianceLevel(rs.getString(3));
                        list.add(machineBean);
                       debug("Compliant level :" + rs.getString(3));
                    }
                } finally {
                    rs.close();
                }
                machineListBean.setList(list);
            } catch(Exception ed) {
                ed.printStackTrace();
            }
//            if(!isRecord) {
//            	setComplianceLevel(NOT_CHECKEDIN);
//            }
//            securityTargetDetailsBean.setComplaintLevel(getOverallComplianceLevel());
//            securityTargetDetailsBean.setCompliantCount(Integer.toString(compliantCnt));
//            securityTargetDetailsBean.setNonCompliantCount(Integer.toString(noncomplaintCnt));
//            securityTargetDetailsBean.setCheckinCount(Integer.toString(notCheckedCnt));
        }

        public MachineListBean getMachineListBean() {
            return machineListBean;
        }

    }

    public static class GetOverallCompliance extends DatabaseAccess {
        SubscriptionMain main = null;
        String overallComplianceLevel = "NOT_CHECKEDIN";

        public GetOverallCompliance (SubscriptionMain main, String targetId, String machineName, String machineId) {
            GetOverallComplianceData result = new GetOverallComplianceData(main, targetId, machineName, machineId);
            try {
                runQuery(result);
                overallComplianceLevel= result.getOverallComplianceLevel();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public String getOverallComplianceLevel() {
        	return overallComplianceLevel;
        }
    }

    static class GetOverallComplianceData extends QueryExecutor {

        String targetID = null;
        String machineName = null;
        String machineId = null;
        String compliance = null;
        int compliantCnt, noncomplaintCnt, notCheckedCnt, notApplicable, total = 0;

        GetOverallComplianceData (SubscriptionMain main, String targetId, String machineName, String machineId) {
            super(main);
            this.targetID = targetId;
            this.machineName = machineName;
            this.machineId = machineId;
            List<MachineBean> list = new ArrayList<MachineBean>();
            if("all".equalsIgnoreCase(targetID)) {
        		targetID = "all_all";
        	}
        }
        protected void execute(IStatementPool pool) throws SQLException {

        	String queryStr = "select machine_name, machine_id, overall_compliant_level from " +
                    "inv_security_oval_compliance where " +
                    "UPPER(assigned_target_name) = UPPER('"+ targetID +"')"+
                    "and UPPER(machine_name) = UPPER('"+ machineName +"')";
        	System.out.println("Query str :" + queryStr);

            PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
            ResultSet rs = st.executeQuery();
            try {
                while (rs.next()) {
                	System.out.println("setting compliance level :" + rs.getString(3));
                	setComplianceLevel(rs.getString(3));
                }
            } finally {
                rs.close();
            }
        }
        private void setComplianceLevel(String complianceLevel) {
            if(COMPLAINT.equals(complianceLevel)) {
                compliantCnt++;
                total++;
            } else if(NON_COMPLAINT.equals(complianceLevel)) {
                noncomplaintCnt++;
                total++;
            } else if(NOT_APPLICABLE.equals(complianceLevel)) {
                notApplicable++ ;
                total++;
            }
        }
        public String getOverallComplianceLevel() {
            if(noncomplaintCnt > 0) {
                return NON_COMPLAINT;
            }
            if(total!=0 && notApplicable == total) {
                return NOT_APPLICABLE;
            }
            if (noncomplaintCnt == 0 && total !=0) {
                return COMPLAINT;
            }
            return NOT_CHECKEDIN;
        }
    }

    public static class GetTargetMachine extends DatabaseAccess {
        SubscriptionMain main = null;
        HashMap<String, String> complianceMap = new HashMap<String, String>();
        List<MachineBean> list = new ArrayList<MachineBean>();

        public GetTargetMachine (SubscriptionMain main, String targetId) {
            GetTargetMachineData result = new GetTargetMachineData(main, targetId);
            try {
                runQuery(result);
                list= result.getMachineBeanList();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public List<MachineBean> getMachineBeanList() {
            return list;
        }
    }

    static class GetTargetMachineData extends QueryExecutor {

        String targetID = null;
        String compliance = null;
        MachineListBean machineListBean = new MachineListBean();
        List<MachineBean> list = new ArrayList<MachineBean>();

        GetTargetMachineData (SubscriptionMain main, String targetId) {
            super(main);
            this.targetID = targetId;
        	if("all".equalsIgnoreCase(targetID)) {
        		targetID = "all_all";
        	}
        }
        protected void execute(IStatementPool pool) throws SQLException {

            PreparedStatement st = pool.getConnection().prepareStatement("select distinct im.name, im.id from ldapsync_target_membership ctm, " +
            		"ldapsync_targets_machines cmt, inv_machine im where UPPER(ctm.memberof_name) = UPPER('"+ targetID +"') " +
            				"and ctm.target_id = cmt.target_id and im.id = cmt.machine_id");

            ResultSet rs = st.executeQuery();
            try {
                while (rs.next()) {
                    MachineBean machineBean = new MachineBean(rs.getString("name"));
                    machineBean.setMachineID(rs.getString("id"));
                    list.add(machineBean);
                }
            } finally {
                rs.close();
            }
        }

        public List<MachineBean> getMachineBeanList() {
            return list;
        }
    }

    public static class GetMachineCompliance extends DatabaseAccess {
        SubscriptionMain main = null;

        public GetMachineCompliance(SubscriptionMain main, SecurityTargetDetailsBean securityTargetDetailsBean) {
            GetMachineComplianceData getTargetComplianceData = new GetMachineComplianceData(main, securityTargetDetailsBean);
            try {
                runQuery(getTargetComplianceData);
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }
    }

    static class GetMachineComplianceData extends QueryExecutor {

        SecurityTargetDetailsBean securityTargetDetailsBean = null;
        int compliantCnt, noncomplaintCnt, notCheckedCnt, total = 0;

        GetMachineComplianceData(SubscriptionMain main,SecurityTargetDetailsBean securityTargetDetailsBean) {
            super(main);
            this.securityTargetDetailsBean = securityTargetDetailsBean;
        }
        protected void execute(IStatementPool pool) throws SQLException {
            try {
            	String targetID = securityTargetDetailsBean.getAssignedToID();
            	if("all".equalsIgnoreCase(targetID)) {
            		targetID = "all_all";
            	}
            	String queryStr = "select machine_name, overall_compliant_level from " +
                "inv_security_oval_compliance where " +
                "UPPER(assigned_target_name) = UPPER('"+ targetID +"')" +
                "and UPPER(content_title) = UPPER('"+ securityTargetDetailsBean.getSelectedSecurityContentName() +"')" +
                "and UPPER(machine_name) = UPPER('"+ securityTargetDetailsBean.getTargetName() +"')" +
                "and UPPER(profile_name) = UPPER('"+ securityTargetDetailsBean.getSelectedProfileId() +"')" ;
            	System.out.println("Query str :" + queryStr);
            	PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
                ResultSet rs = st.executeQuery();
                try {
                    while (rs.next()) {
                        securityTargetDetailsBean.setComplaintLevel(rs.getString(2));
                    }
                } finally {
                    rs.close();
                }
            } catch(Exception ed) {
                ed.printStackTrace();
            }
        }
    }
    public static class GetProfileComplianceReport extends DatabaseAccess {
        SubscriptionMain main = null;

        public GetProfileComplianceReport(SubscriptionMain main, SecurityProfileDetailsBean securityProfileDetailsBean) {
            GetProfileComplianceReportDetails targetComplianceReportDetails = new GetProfileComplianceReportDetails(main, securityProfileDetailsBean);
            try {
                runQuery(targetComplianceReportDetails);
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }
    }

    static class GetProfileComplianceReportDetails extends QueryExecutor {

        SecurityProfileDetailsBean securityProfileDetailsBean = null;

        GetProfileComplianceReportDetails(SubscriptionMain main, SecurityProfileDetailsBean securityProfileDetailsBean) {
            super(main);
            this.securityProfileDetailsBean = securityProfileDetailsBean;
        }
        protected void execute(IStatementPool pool) throws SQLException {
            try {
            	String targetID = securityProfileDetailsBean.getAssignedToID();
            	if("all".equalsIgnoreCase(targetID)) {
            		targetID = "all_all";
            	}
            	String queryStr = "select * from " +
                "inv_security_oval_compliance where " +
                "UPPER(assigned_target_name) = UPPER('"+ targetID +"') " +
                "and UPPER(machine_name) = UPPER('"+ securityProfileDetailsBean.getTargetName() +"') " +
                "and UPPER(content_name) = UPPER('"+ securityProfileDetailsBean.getContentName() +"') " +
                "and UPPER(profile_name) = UPPER('"+ securityProfileDetailsBean.getProfileName() +"')" ;
            	System.out.println("Query str :" + queryStr);
            	PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
                ResultSet rs = st.executeQuery();
                try {
                    if (rs.next()) {
                        securityProfileDetailsBean.setTargetOS(rs.getString("content_target_os"));
                        securityProfileDetailsBean.setContentId(rs.getString("content_id"));
                        securityProfileDetailsBean.setContentName(rs.getString("content_name"));
                        securityProfileDetailsBean.setContentTitle(rs.getString("content_title"));
                        securityProfileDetailsBean.setContentDescription(rs.getString("content_desc"));
                        securityProfileDetailsBean.setContentFileName(rs.getString("content_file_name"));
                        securityProfileDetailsBean.setProfileId(rs.getInt("profile_id"));
                        securityProfileDetailsBean.setProfileName(rs.getString("profile_name"));
                        securityProfileDetailsBean.setProfileTitle(rs.getString("profile_title"));
                        securityProfileDetailsBean.setProfileDescription(rs.getString("profile_desc"));
                        securityProfileDetailsBean.setPerformedBy(rs.getString("performed_by"));
                        securityProfileDetailsBean.setComplaintLevel(rs.getString("overall_compliant_level"));
                        securityProfileDetailsBean.setAssginedToName(rs.getString("assigned_target_name"));

                        Timestamp startedAtTimestamp = rs.getTimestamp("started_at", TimeUtil.getGMTCalendar());
                        String startedAt = null;
                        if (startedAtTimestamp != null) {
                            startedAt = Utils.formatDate(startedAtTimestamp.getTime(), Locale.getDefault());
                            securityProfileDetailsBean.setStartTime(startedAt);
                        }

                        Timestamp finishedAtTimestamp = rs.getTimestamp("started_at", TimeUtil.getGMTCalendar());
                        String finishedAt = null;
                        if (finishedAtTimestamp != null) {
                            finishedAt = Utils.formatDate(finishedAtTimestamp.getTime(), Locale.getDefault());
                            securityProfileDetailsBean.setFinishTime(finishedAt);
                        }

                        byte[] fileBytes = rs.getBytes("rules_compliance");
						InputStream inputStream = (fileBytes != null) ? new ByteArrayInputStream(fileBytes) : null;
                        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder sb = new StringBuilder();
                        try {
                        String line = null;
                        while ((line = br.readLine()) != null) {
                            sb.append(line);
                        }
                        } catch (IOException iox) {
                            iox.printStackTrace();
                        } finally {
                            if (br != null) {
                                br.close();
                            }
                        }
                        securityProfileDetailsBean.setRulesJSONResult(sb.toString());
                    }
                } finally {
                    rs.close();
                }
            } catch(Exception ed) {
                ed.printStackTrace();
            }
        }
    }

    public static class GetMachineOSCount extends DatabaseAccess {
        SubscriptionMain main = null;
        int count;

        public GetMachineOSCount(SubscriptionMain main, String targetId, String osType) {
            GetMachineOSCountData result = new GetMachineOSCountData(main, targetId, osType);
            try {
                runQuery(result);
                count = result.getCount();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public int getCount() {
            return count;
        }
    }

    static class GetMachineOSCountData extends QueryExecutor {

        String targetID = null;
        int count = 0;

        String osType;
        GetMachineOSCountData(SubscriptionMain main, String targetId, String osType) {
            super(main);
            this.targetID = targetId;
            this.osType = osType;
            if("all".equalsIgnoreCase(targetID)) {
                targetID = "all_all";
            }
        }
        protected void execute(IStatementPool pool) throws SQLException {

            PreparedStatement st = pool.getConnection().prepareStatement("select count(*) from ldapsync_target_membership ctm, " +
                    "ldapsync_targets_machines cmt, inv_machine im where UPPER(ctm.memberof_name) = UPPER('"+ targetID +"') " +
                    "and ctm.target_id = cmt.target_id and im.id = cmt.machine_id and  exists (select 1 from inv_os os where os.machine_id = im.id " +
                    "and os.product like '%"+ osType +"%')");

            ResultSet rs = st.executeQuery();
            try {
                if (rs.next()) {
                    count = rs.getInt(1);
                }
            } finally {
                rs.close();
            }
        }

        public int getCount() {
            return count;
        }
    }

    private static void debug(String msg) {
        if (IAppConstants.DEBUG) System.out.println("Security Vluner Compliance: " + msg);
    }

}
