// Copyright 2019-2022, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
// $File$, $Revision$, $Date$

package com.marimba.apps.securitymgr.compliance;

import com.marimba.apps.securitymgr.db.DatabaseAccess;
import com.marimba.apps.securitymgr.db.QueryExecutor;
import com.marimba.apps.securitymgr.view.SCAPBean;
import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.beans.*;
import com.marimba.apps.subscriptionmanager.compliance.intf.ComplianceConstants;
import com.marimba.apps.subscriptionmanager.compliance.view.MachineBean;
import com.marimba.apps.subscriptionmanager.compliance.view.MachineListBean;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import com.marimba.intf.db.IStatementPool;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 *  Dashboard Info Details
 *  w.r.t fetch new dashboard data from DB
 *
 * @author Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 */

public class DashboardInfoDetails implements ComplianceConstants {

    public static class GetMachineDetails extends DatabaseAccess {

        SubscriptionMain main = null;
        HashMap<String, String> complianceMap = new HashMap<String, String>();
        List<MachineBean> list = new ArrayList<MachineBean>();



        public GetMachineDetails(SubscriptionMain main, String targetId) {
            GetMachineData result = new GetMachineData(main, targetId);

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

        public int getMachinesCount() {
            return list.size();
        }

    }



    static class GetMachineData extends QueryExecutor {

        String targetID = null;
        String compliance = null;
        MachineListBean machineListBean = new MachineListBean();
        Map<String, MachineBean> macBeanList = new HashMap<String, MachineBean>();

        GetMachineData (SubscriptionMain main, String targetId) {

            super(main);

            this.targetID = targetId;

            if("all".equalsIgnoreCase(targetID)) {

                targetID = "all_all";
            }
        }

        protected void execute(IStatementPool pool) throws SQLException {
            String sql = "select xccdf.machine_id id, xccdf.assigned_target_name target_name, os.product product, xccdf.overall_compliant_level status, count(*) count \n" +
                    "from inv_security_xccdf_compliance xccdf, inv_os os\n" +
                    "where xccdf.machine_id= os.machine_id and UPPER(assigned_target_name) like UPPER('"+targetID+"')\n" +
                    "group by xccdf.machine_id, xccdf.assigned_target_name, os.product, xccdf.overall_compliant_level";

            PreparedStatement st = pool.getConnection().prepareStatement(sql);
            debug("GetMachineDetails() Query Str: " + sql);
            ResultSet rs = st.executeQuery();
            Map<String, Set<String>> compDetails = new HashMap<String, Set<String>>();

            try {

                while (rs.next()) {
                    String machineId = rs.getString(1);
                    String status = rs.getString("status");
                    String osType = rs.getString("product");
                    MachineBean machineBean = new MachineBean(machineId);
                    machineBean.setMachineID(machineId);
                    machineBean.setOsProduct(osType);
                    machineBean.setComplianceLevel(status);
                    macBeanList.put(machineId, machineBean);

                    Set<String> targetStatus = (null != compDetails.get(machineId)) ? compDetails.get(machineId) : new HashSet<String>(3);
                    targetStatus.add(status);
                    compDetails.put(machineId, targetStatus);
                }

            } finally {

                rs.close();

            }

            for (String machineId : compDetails.keySet()) {
                Set<String> set = compDetails.get(machineId);
                if (null == set) continue;
                String finalStatus = STR_LEVEL_NON_COMPLIANT;
                if (set.size() == 2) {
                    finalStatus = set.contains(STR_LEVEL_NON_COMPLIANT) ? STR_LEVEL_NON_COMPLIANT : STR_LEVEL_COMPLIANT;
                }

                if (set.size() == 1) {
                    if (set.contains(STR_LEVEL_COMPLIANT)) finalStatus = STR_LEVEL_COMPLIANT;
                    if (set.contains(STR_LEVEL_NON_COMPLIANT)) finalStatus = STR_LEVEL_NON_COMPLIANT;
                    if (set.contains(STR_LEVEL_NOT_APPLICABLE)) finalStatus = STR_LEVEL_NOT_APPLICABLE;
                }

                MachineBean aBean = macBeanList.get(machineId);
                if (null != aBean) aBean.setComplianceLevel(finalStatus);
            }

        }



        public List<MachineBean> getMachineBeanList() {
            return new ArrayList<MachineBean>(macBeanList.values());
        }

    }



    public static class GetMachineCount extends DatabaseAccess {

        SubscriptionMain main = null;
        int count = 0;


        public GetMachineCount (SubscriptionMain main, String targetId) {
            GetLast24HourMachineData result = new GetLast24HourMachineData(main, targetId);
            try {
                runQuery(result);
                count= result.getMachineCount();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }



        public int getMachinesCount() {
            return count;
        }

    }



    static class GetMachineCountData extends QueryExecutor {
        String targetID = null;
        int count = 0;

        GetMachineCountData (SubscriptionMain main, String targetId) {
            super(main);
            this.targetID = targetId;
            if("all".equalsIgnoreCase(targetID)) {
                this.targetID = "all_all";
            }
        }

        protected void execute(IStatementPool pool) throws SQLException {

            String queryStr = "select COUNT(*) from inv_machine im" +
                    "where exists (select 1 from inv_security_xccdf_compliance  sxc where sxc.machine_id = im.id" +
                    "and UPPER(sxc.assigned_target_name) = UPPER('"+ targetID +"')";

            debug("GetMachineCountData() Query Str :" + queryStr);

            PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
            ResultSet rs = st.executeQuery();

            try {
                if(rs.next()) {
                    count = rs.getInt(1);
                }

            } finally {
                rs.close();
            }

        }

        public int getMachinesCount() {
            return count;
        }

    }



    public static class GetLast24HourMachineDetails extends DatabaseAccess {
        SubscriptionMain main = null;
        int count = 0;

        public GetLast24HourMachineDetails (SubscriptionMain main, String targetId) {
            GetLast24HourMachineData result = new GetLast24HourMachineData(main, targetId);

            try {
                runQuery(result);
                count= result.getMachineCount();
            } catch (Exception dae) {
                dae.printStackTrace();
            }

        }

        public int getMachinesCount() {
            return count;
        }

    }


    static class GetLast24HourMachineData extends QueryExecutor {
        String targetID = null;
        int count = 0;


        GetLast24HourMachineData (SubscriptionMain main, String targetId) {
            super(main);
            this.targetID = targetId;

            if("all".equalsIgnoreCase(targetID)) {
                this.targetID = "all_all";
            }
        }

        protected void execute(IStatementPool pool) throws SQLException {
            String queryStr = "select COUNT(*) from ( " +
                    "select machine_id, max(finished_at) as finished_at  from inv_security_xccdf_compliance sxc where " +
                    " upper(sxc.assigned_target_name) like  UPPER('"+ targetID +"') " +
                    " group by machine_id ) a " +
                    " where a.finished_at >= DATEADD(day, -1, GETDATE())";

            debug("GetLast24HourMachineData() Query Str :" + queryStr);

            PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
            ResultSet rs = st.executeQuery();

            try {
                if(rs.next()) {
                    count = rs.getInt(1);
                }
            } finally {
                rs.close();
            }

        }


        public int getMachineCount() {
            return count;
        }
    }

    public static class GetSecurityInUseDetails extends DatabaseAccess {
        SubscriptionMain main = null;
        int count = 0;

        public GetSecurityInUseDetails (SubscriptionMain main, String targetId) {
            GetSecurityInUseData result = new GetSecurityInUseData(main, targetId);

            try {
                runQuery(result);
                count= result.getSecurityCount();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }



        public int getSecurityCount() {
            return count;
        }

    }



    static class GetSecurityInUseData extends QueryExecutor {

        String targetID = null;
        int count = 0;

        GetSecurityInUseData (SubscriptionMain main, String targetId) {
            super(main);
            this.targetID = targetId;

            if("all".equalsIgnoreCase(targetID)) {
                targetID = "%";
            }
        }

        protected void execute(IStatementPool pool) throws SQLException {
            String queryStr = "select count(*) as 'Security Scanner in use' from security_xccdf_content sxct " +
                    "where exists (select 1 from inv_security_xccdf_compliance  sxc where sxct.id = sxc.content_id " +
                    "and UPPER(sxc.assigned_target_name) like UPPER('"+ targetID +"'))";
            debug("GetSecurityInUseData() Query Str :" + queryStr);

            PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
            ResultSet rs = st.executeQuery();

            try {
                if(rs.next()) {
                    count = rs.getInt(1);
                }
            } finally {
                rs.close();
            }
        }

        public int getSecurityCount() {
            return count;
        }

    }

    //For Chart
     public static class GetOverallComplianceDetails extends DatabaseAccess {
         SubscriptionMain main = null;
         int count = 0;

        Map<String, String> map = new HashMap<String, String>();

        public GetOverallComplianceDetails (SubscriptionMain main, String targetId, String compliantLevel) {
            GetOverallComplianceData result = new GetOverallComplianceData(main, targetId, compliantLevel);

            try {
                runQuery(result);
                map = result.getCompliantMap();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public Map<String, String> getCompliantMap() {
            return map;
        }

        public int getCount() {
            return count;
        }

    }



    static class GetOverallComplianceData extends QueryExecutor {

        String targetID = null;
        int nonCompliantCount = 0;
        int compliantCount = 0;
        String compliantLevel = null;
        Map<String, String> result = new HashMap<String, String>();

        GetOverallComplianceData (SubscriptionMain main, String targetId, String compliantLevel) {
            super(main);
            this.targetID = targetId;
            this.compliantLevel = compliantLevel;

            if("all".equalsIgnoreCase(targetID)) {
                targetID = "all_all";
            }
        }

        protected void execute(IStatementPool pool) throws SQLException {
           String queryStr = "select a.Compliant, b.[Non-Compliant] from" +
                  "(select count(*) as 'Compliant' from  inv_security_xccdf_compliance  sxc" +
                    "where overall_compliant_level = 'Compliant'" +
                    "and UPPER(sxc.assigned_target_name) = UPPER('"+ targetID +"')  a ," +
                    "(select count(*) as 'Non-Compliant' from  inv_security_xccdf_compliance  sxc" +
                    "where overall_compliant_level = 'No Compliant'" +
                    "and UPPER(sxc.assigned_target_name) = UPPER('"+ targetID +"') b";

            debug("GetOverallComplianceData() Query Str :" + queryStr);
            PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
            ResultSet rs = st.executeQuery();

            try {
                if(rs.next()) {
                    compliantCount = rs.getInt(1);
                    nonCompliantCount = rs.getInt(2);
                }

                result.put(COMPLAINT, String.valueOf(compliantCount));
                result.put(NON_COMPLAINT, String.valueOf(nonCompliantCount));

            } finally {
                rs.close();
            }

        }

        public Map<String, String> getCompliantMap() {
            return result;
        }
    }



    public static class GetSCAPTypeComplianceDetails extends DatabaseAccess {

        SubscriptionMain main = null;
        List<SCAPBean> scapBeanList= new ArrayList<SCAPBean>();

        public GetSCAPTypeComplianceDetails (SubscriptionMain main, String targetId) {
            GetSCAPTypeComplianceData result = new GetSCAPTypeComplianceData(main, targetId);
            try {
                runQuery(result);
                scapBeanList.addAll(result.getScapBeanList());
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }


        public List<SCAPBean> getScapBeanList() {
            return scapBeanList;
        }

    }



    static class GetSCAPTypeComplianceData extends QueryExecutor {
        String targetID = null;
        int count = 0;
        String compliantLevel = null;
        List<SCAPBean> scapBeanList= new ArrayList<SCAPBean>();

        GetSCAPTypeComplianceData(SubscriptionMain main, String targetId) {
            super(main);
            this.targetID = targetId;
            if ("all".equalsIgnoreCase(targetID)) {
                targetID = "%";
            }
        }

        protected void execute(IStatementPool pool) throws SQLException {
            String queryStr = "select content_name, profile_name, content_target_os, COUNT('NON-COMPLIANT') 'non-compliant', COUNT ('COMPLIANT') 'compliant' " +
                            "from inv_security_xccdf_compliance sxc " +
                            "where upper(sxc.assigned_target_name) like upper('" + targetID + "') " +
                            "group by content_name, profile_name, content_target_os";

            debug("GetSCAPTypeComplianceData() Query Str :" + queryStr);
            PreparedStatement st = pool.getConnection().prepareStatement(queryStr);

            ResultSet rs = st.executeQuery();
            try {

                while (rs.next()) {
                    SCAPBean scapBean = new SCAPBean();
                    scapBean.setComplianceLevel(rs.getInt("non-compliant") == 0 ? COMPLAINT : NON_COMPLAINT);
                    scapBean.setType(rs.getString("content_target_os"));
                    scapBeanList.add(scapBean);
                 }
             } finally {
                 rs.close();
             }
         }


        public List<SCAPBean> getScapBeanList() {
             return scapBeanList;
         }

    }


    //For scanner-wise compliant
    public static class GetScannerWiseCompliant extends DatabaseAccess {
        SubscriptionMain main = null;
        int count = 0;
        Map<String, Map<String, Integer>> scannerMap = new HashMap<String, Map<String, Integer>>();

        public GetScannerWiseCompliant (SubscriptionMain main, String targetId) {
            GetScannerWiseCompliantData result = new GetScannerWiseCompliantData(main, targetId);

            try {
                runQuery(result);
                scannerMap = result.scannerMap();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }


        public Map<String, Map<String, Integer>> getScannerMap() {
            return scannerMap;
        }

    }



    static class GetScannerWiseCompliantData extends QueryExecutor {

        String targetID = null;
        Map<String, Map<String, Integer>> scannerMap = new HashMap<String, Map<String, Integer>>();

        GetScannerWiseCompliantData (SubscriptionMain main, String targetId) {
            super(main);
            this.targetID = targetId;

            if("all".equalsIgnoreCase(targetID)) {
                targetID = "all_all";
            }
        }

        protected void execute(IStatementPool pool) throws SQLException {

            String queryStr = "select a.content_title, a.profile_name,  a.Compliant, a.[Compliant] as 'Non-Compliant' from " +
                    "        (select sxc.content_title,sxc.profile_name,overall_compliant_level, count(*) as 'Compliant' " +
                    "         from  inv_security_xccdf_compliance sxc " +
                    "              where overall_compliant_level = 'Compliant' " +
                    "              and upper(sxc.assigned_target_name) like upper('"+targetID+"')  " +
                    "              group by sxc.content_title,sxc.profile_name,overall_compliant_level " +
                    "              union " +
                    "        select sxc.content_title,sxc.profile_name, overall_compliant_level,count(*) as 'Compliant' " +
                    "        from  inv_security_xccdf_compliance sxc " +
                    "              where overall_compliant_level = 'Non-Compliant' " +
                    "              and upper(sxc.assigned_target_name) like upper('"+targetID+"')  " +
                    "              group by sxc.content_title,sxc.profile_name,overall_compliant_level ) a";

            debug("GetScannerWiseCompliantData() Query Str :" + queryStr);

            PreparedStatement st = pool.getConnection().prepareStatement(queryStr);

            ResultSet rs = st.executeQuery();

            try {

                while (rs.next()) {
                    HashMap<String, Integer> map = new HashMap<String, Integer>(2);
                    map.put(COMPLAINT, rs.getInt(3));
                    map.put(NON_COMPLAINT, rs.getInt(4));
                    scannerMap.put(rs.getString(1), map);
                }
            } finally {
                rs.close();
            }

        }

        public Map<String, Map<String, Integer>> scannerMap() {
            return scannerMap;
        }

    }


    public static class GetContentInfoForAllEndpoints extends DatabaseAccess {
        SubscriptionMain main;
        Map<String, String> contentsMap = new TreeMap<String, String>();

        public GetContentInfoForAllEndpoints(SubscriptionMain subMain) {
            main = subMain;
            GetContentInfoForAllEndpointsData result = new GetContentInfoForAllEndpointsData();
            try {
                runQuery(result);
            } catch (Exception dae) {
                dae.printStackTrace();
            }

        }



        public Map<String, String> getContentsMap() {
            debug("GetContentInfoForAllEndpoints : contentsMap : " + contentsMap);
            return contentsMap;
       }



        class GetContentInfoForAllEndpointsData extends QueryExecutor {

            GetContentInfoForAllEndpointsData() {
                super(main);
            }



            protected void execute(IStatementPool pool) throws SQLException {
                String queryStr = "select content_title, content_name from inv_security_xccdf_compliance";

                debug("GetContentInfoForAllEndpointsData() Query Str : " + queryStr);
                PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
                ResultSet rs = st.executeQuery();

                try {
                    while(rs.next()) {
                        contentsMap.put(rs.getString(1), rs.getString(2));
                    }

                } finally {
                    rs.close();
                    st.close();
                }

            }

        }

    }



    public static class GetProfileInfoForAllEndpoints extends DatabaseAccess {
        SubscriptionMain main;
        String contentTitle = "";
        Map<String, String> profilesMap = new TreeMap<String, String>();


        public GetProfileInfoForAllEndpoints(SubscriptionMain subMain, String content_Title) {
            main = subMain;
            contentTitle = content_Title;

            GetProfileInfoForAllEndpointsData result = new GetProfileInfoForAllEndpointsData();
            try {
                runQuery(result);
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public Map<String, String> getProfilesMap() {
            debug("GetContentInfoForAllEndpoints : profilesMap : " + profilesMap);
            return profilesMap;
        }



        class GetProfileInfoForAllEndpointsData extends QueryExecutor {

            GetProfileInfoForAllEndpointsData() {
                super(main);
            }

            protected void execute(IStatementPool pool) throws SQLException {
                String queryStr = "select profile_name, profile_title from inv_security_xccdf_compliance where content_title = '"+contentTitle+"'";
                debug("GetProfileInfoForAllEndpointsData() Query Str : " + queryStr);

                PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
                ResultSet rs = st.executeQuery();

                try {
                    while(rs.next()) {
                        profilesMap.put(rs.getString(1), rs.getString(2));
                    }
                } finally {
                    rs.close();
                    st.close();
                }
            }
        }

    }



    public static class GetGroupNames extends DatabaseAccess {

        SubscriptionMain main;
        String contentTitle = "";
        Collection<String> groupIds = new HashSet<String>();
        Map<String, String> groupNameMap = new TreeMap<String, String>(); // Key -> ruleid, value -> ruleName


        public GetGroupNames(SubscriptionMain subMain, Collection<String> ruleids) {
            main = subMain;
            groupIds.addAll(ruleids);
            GetRuleNameData result = new GetRuleNameData();

            try {
                runQuery(result);
            } catch (Exception dae) {
                dae.printStackTrace();
            }

        }


        public Map<String, String> getGroupNameMap() {
            debug("GetContentInfoForAllEndpoints : groupNameMap : " + groupNameMap);
            return groupNameMap;
        }



        class GetRuleNameData extends QueryExecutor {

            GetRuleNameData() {
                super(main);
            }


            protected void execute(IStatementPool pool) throws SQLException {

                String group_ids = getCollectionToString(groupIds);
                String queryStr = "select distinct group_name, group_title from security_xccdf_group where group_name in (" + group_ids + ")";
                debug("GetGroupNames() Query Str : " + queryStr);
                PreparedStatement st = pool.getConnection().prepareStatement(queryStr);

                ResultSet rs = st.executeQuery();

                try {
                    while(rs.next()) {
                        groupNameMap.put(rs.getString(1), rs.getString(2));
                    }
                } finally {
                    rs.close();
                    st.close();
                }
            }

        }

    }



    public static class GetRuleNames extends DatabaseAccess {

        SubscriptionMain main;
        String contentTitle = "";
        Collection<String> ruleIds = new HashSet<String>();
        Map<String, String> ruleNameMap = new TreeMap<String, String>(); // Key -> ruleid, value -> ruleName


        public GetRuleNames(SubscriptionMain subMain, Collection<String> ruleids) {
            main = subMain;
            ruleIds.addAll(ruleids);

            GetRuleNameData result = new GetRuleNameData();

            try {

                runQuery(result);

            } catch (Exception dae) {
                dae.printStackTrace();
            }

        }



        public Map<String, String> getRuleNameMap() {
            debug("GetContentInfoForAllEndpoints : groupNameMap : " + ruleNameMap);
            return ruleNameMap;
        }



        class GetRuleNameData extends QueryExecutor {


            GetRuleNameData() {
                super(main);
            }



            protected void execute(IStatementPool pool) throws SQLException {

                String rule_ids = getCollectionToString(ruleIds);
                String queryStr = "select distinct rule_name, rule_title from security_xccdf_group_rule where rule_name in (" + rule_ids + ")";

                debug("GetRuleNames() Query Str : " + queryStr);
                PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
                ResultSet rs = st.executeQuery();

                try {

                   while(rs.next()) {

                        ruleNameMap.put(rs.getString(1), rs.getString(2));

                    }

                } finally {
                    rs.close();
                    st.close();
                }

            }

        }

    }



    private static String getCollectionToString(Collection<String> collection) {

        StringBuilder result = new StringBuilder();

        for(String string : collection) {
            result.append("'").append(string).append("',");
        }

        return result.length() > 0 ? result.substring(0, result.length() - 1): "''";
    }



    //For scanner-wise compliant

    public static class GetTotalMachineCount extends DatabaseAccess {

        SubscriptionMain main = null;

        int count = 0;

        Map<String, Map<String, Integer>> scannerMap = new HashMap<String, Map<String, Integer>>();



        public GetTotalMachineCount (SubscriptionMain main, String targetId) {

            GetTotalMachineCountData result = new GetTotalMachineCountData(main, targetId);

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



    static class GetTotalMachineCountData extends QueryExecutor {



        String targetID = null;

        int count = 0;



        GetTotalMachineCountData (SubscriptionMain main, String targetId) {

            super(main);

            this.targetID = targetId;

            if("all".equalsIgnoreCase(targetID)) {

                targetID = "%";

            }

        }


        protected void execute(IStatementPool pool) throws SQLException {

            String queryStr = "select COUNT(im.id) from ldapsync_target_membership ctm, " +
                    "    ldapsync_targets_machines cmt, inv_machine im " +
                    "    where UPPER(ctm.memberof_name) like  upper('"+targetID+"') " +
                    "    and ctm.target_id = cmt.target_id and im.id = cmt.machine_id " +
                    "    and exists (select 1 from inv_security_xccdf_compliance isxc " +
                    "    where isxc.assigned_target_name = ctm.memberof_name)";


            debug("GetTotalMachineCountData() Query Str :" + queryStr);
            PreparedStatement st = pool.getConnection().prepareStatement(queryStr);

            ResultSet rs = st.executeQuery();

            try {
                while (rs.next()) {
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



    //All Endpoints
    public static class GetAllEndpointMachines extends DatabaseAccess {
        SubscriptionMain main = null;
        HashMap<String, String> complianceMap = new HashMap<String, String>();
        List<MachineBean> list = new ArrayList<MachineBean>();


        public GetAllEndpointMachines(SubscriptionMain main, String targetId) {
            GetAllEndpointMachinesData result = new GetAllEndpointMachinesData(main, targetId);
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

        public int getMachinesCount() {
            return list.size();
        }

    }


    static class GetAllEndpointMachinesData extends QueryExecutor {

        Map<String, MachineBean> macBeanList = new HashMap<String, MachineBean>();

        GetAllEndpointMachinesData (SubscriptionMain main, String targetId) {
            super(main);
        }

        protected void execute(IStatementPool pool) throws SQLException {
            String sql = "select a.machine_id, b.product product, dbo.MachineOverallCompStatus(a.machine_id) status from (\n" +
                    "select distinct machine_id from inv_security_xccdf_compliance\n" +
                    ") a, inv_os b where a.machine_id = b.machine_id \n";

            debug("GetAllEndpointMachines() Query str: " + sql);
            PreparedStatement st = pool.getConnection().prepareStatement(sql);

            ResultSet rs = st.executeQuery();
            Map<String, Set<String>> compDetails = new HashMap<String, Set<String>>();

            try {
                while (rs.next()) {
                    String machineId = rs.getString(1);
                    String status = rs.getString("status");
                    MachineBean machineBean = new MachineBean(machineId);
                    machineBean.setMachineID(machineId);
                    machineBean.setOsProduct(rs.getString("product"));
                    machineBean.setComplianceLevel(status);
                    macBeanList.put(machineId, machineBean);
                    Set<String> targetStatus = compDetails.get(machineId);

                    if (null == targetStatus) targetStatus = new HashSet<String>();
                    targetStatus.add(status);
                    compDetails.put(machineId, targetStatus);
                }
            } finally {
                rs.close();
            }

            for (String machineId : compDetails.keySet()) {
                Set<String> set = compDetails.get(machineId);
                String finalStatus = "NON-COMPLIANT";
                if (set.size() == 2) {
                    finalStatus = set.contains("NON-COMPLIANT") ? "NON-COMPLIANT" : "COMPLIANT";
                }

                if (set.size() == 1) {
                    if (set.contains("COMPLIANT")) finalStatus = "COMPLIANT";
                    if (set.contains("NON-COMPLIANT")) finalStatus = "NON-COMPLIANT";
                    if (set.contains("NOT APPLICABLE")) finalStatus = "NOT APPLICABLE";
                }

                MachineBean aBean = macBeanList.get(machineId);

                if (null != aBean) aBean.setComplianceLevel(finalStatus);
            }

        }

        public List<MachineBean> getMachineBeanList() {
            return new ArrayList<MachineBean>(macBeanList.values());
        }

    }


    //For All Endpoint scanner-wise compliant
    public static class GetAllEndpointScannerWiseCompliant extends DatabaseAccess {
        SubscriptionMain main = null;
        int count = 0;
        Map<String, SCAPBean> scannerMap = new HashMap<String, SCAPBean>();

        public GetAllEndpointScannerWiseCompliant (SubscriptionMain main, String targetId) {
            GetAllEndpointScannerWiseCompliantData result = new GetAllEndpointScannerWiseCompliantData(main, targetId);
            try {
                runQuery(result);
                scannerMap = result.getScannerMap();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }



        public Map<String, SCAPBean> getScannerMap() {
            return scannerMap;
        }
    }



    static class GetAllEndpointScannerWiseCompliantData extends QueryExecutor {
        String targetID = null;
        Map<String, SCAPBean> scannerMap = new HashMap<String, SCAPBean>();

        GetAllEndpointScannerWiseCompliantData (SubscriptionMain main, String targetId) {
            super(main);
            this.targetID = targetId;
            if("all".equalsIgnoreCase(targetID)) {
                targetID = "%";
            }
        }

        protected void execute(IStatementPool pool) throws SQLException {

           String queryStr = "select content_title, overall_compliant_level, count(overall_compliant_level) as count  from inv_security_xccdf_compliance sxc " +
                    "group by content_title, overall_compliant_level";
            debug("GetAllEndpointScannerWiseCompliantData() Query Str :" + queryStr);

            PreparedStatement st = pool.getConnection().prepareStatement(queryStr);
            ResultSet rs = st.executeQuery();

            try {

                 while (rs.next()) {

                    String contentTitle = rs.getString(1);
                    SCAPBean scapBean = (scannerMap.get(contentTitle) == null) ? new SCAPBean() : scannerMap.get(contentTitle);
                    scapBean.setTitle(contentTitle);

                    if (COMPLAINT.equals(rs.getString(2))) {
                       scapBean.setCompliantCount(rs.getInt(3));
                    } else {
                        scapBean.setNonCompliantCount(rs.getInt(3));
                    }

                    scannerMap.put(contentTitle, scapBean);
                }

            } finally {
                rs.close();
            }
        }

        public Map<String, SCAPBean> getScannerMap() {
            return scannerMap;
        }

    }

    public static class GetComplianceReportNotCheckedInData extends DatabaseAccess {
        List<ReportingNotCheckedInBean> rptNotCheckedIn = new ArrayList<ReportingNotCheckedInBean>();

        public GetComplianceReportNotCheckedInData(SubscriptionMain main) {
            GetComplianceReportNotCheckedInInfo result = new GetComplianceReportNotCheckedInInfo(main);
            try {
                runQuery(result);
                rptNotCheckedIn = result.getReportNotCheckedInInfo();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public List<ReportingNotCheckedInBean> getReportNotCheckedInInfo() {
            return rptNotCheckedIn;
        }
    }



    public static class GetComplianceReportingData extends DatabaseAccess {
        Map<String, String> complianceResult = new LinkedHashMap<String, String>();
        String complianceType = "";

        public GetComplianceReportingData(SubscriptionMain main, String complianceType) {
            this.complianceType = complianceType;
            GetComplianceReportingInfo result = new GetComplianceReportingInfo(main, complianceType);
            try {
                runQuery(result);
                if ("reporting".equalsIgnoreCase(complianceType)) {
                    int checkedIn = result.getCheckedIn();
                    int notCheckedIn = result.getNotCheckedIn();
                    complianceResult.put("checkedIn", String.valueOf(checkedIn));
                    complianceResult.put("notCheckedIn", String.valueOf(notCheckedIn));
                } else if ("security".equalsIgnoreCase(complianceType)) {
                    int compliant = result.getCompliant();
                    int nonCompliant = result.getNonCompliant();
                    complianceResult.put("compliant", String.valueOf(compliant));
                    complianceResult.put("nonCompliant", String.valueOf(nonCompliant));
                } else {
                    int compliant = result.getCompliant();
                    int nonCompliant = result.getNonCompliant();
                    complianceResult.put("compliant", String.valueOf(compliant));
                    complianceResult.put("nonCompliant", String.valueOf(nonCompliant));
                }
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public Map<String, String> getResult() {
            return complianceResult;
        }
    }


    public static class GetAllEndpointMachineCount extends DatabaseAccess {
        SubscriptionMain main = null;
        int count = 0;

        public GetAllEndpointMachineCount(SubscriptionMain main, String osType) {
            GetAllEndpointMachineCountData result = new GetAllEndpointMachineCountData(main, osType);
            try {
                runQuery(result);
                count = result.getCount();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public int getMachinesCount() {
            return count;
        }
    }

    public static class GetVulnerableStatisticsInfo extends DatabaseAccess {
        Map<String, String> vulStatsInfo = new LinkedHashMap<String, String>();

        public GetVulnerableStatisticsInfo(SubscriptionMain main) {

            GetVulnerableStatsInfoData result = new GetVulnerableStatsInfoData(main);

            try {
                runQuery(result);
                vulStatsInfo = result.getVulnerableStatsInfo();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public Map<String, String> getVulnerableStatsInfo() {
            return vulStatsInfo;
        }
    }


    public static class GetAgeingVulnerableBySeverityInfo extends DatabaseAccess {
        Map<String, String> vulSeverityInfo = new LinkedHashMap<String, String>();

        public GetAgeingVulnerableBySeverityInfo(SubscriptionMain main) {

            GetAgeingVulnerableBySeverityData result = new GetAgeingVulnerableBySeverityData(main);

            try {
                runQuery(result);
                vulSeverityInfo = result.getVulnerableStatsInfo();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public Map<String, String> getVulnerableSeverityInfo() {
            return vulSeverityInfo;
        }
    }


    public static class GetChannelStoreCredentialsInfo extends DatabaseAccess {
        Map<String, String> channelStoreCreds = new LinkedHashMap<String, String>();

        public GetChannelStoreCredentialsInfo(SubscriptionMain main, String username, String password) {

            GetChannelStoreCredentialsData result = new GetChannelStoreCredentialsData(main, username, password);

            try {
                runQuery(result);
                channelStoreCreds = result.getChannelStoreCredentialsInfo();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public Map<String, String> getChannelStoreCredentialsInfo() {
            return channelStoreCreds;
        }
    }

    static class GetVulnerableStatsInfoData extends QueryExecutor {
        Map<String, String> vulStatsInfo = new LinkedHashMap<String, String>();

        GetVulnerableStatsInfoData(SubscriptionMain main) {
            super(main);
        }

        protected void execute(IStatementPool pool) throws SQLException {

            String sqlStr = " select t1.severity as severity_name , count(t1.severity) as severity_count from inv_sec_oval_defn_cve_records t1, security_cve_patch_info t2 \n" +
                    " where t1.reference_name not like 'cpe%' \n" +
                    " and t1.severity != 'null' and t1.repository_id = t2.repository_id \n" +
                    " and t1.severity != ' '\n" +
                    "group by t1.severity ";

            // Taken results from derived table
            String sqlStr2 = "select * from ds_derived_donught_chart";

            PreparedStatement st = pool.getConnection().prepareStatement(sqlStr);
            ResultSet rs = st.executeQuery();
            try {
                while(rs.next()) {
                 String severity = rs.getString("severity_name");
                 String severityCnt = String.valueOf(rs.getInt("severity_count"));
                 vulStatsInfo.put(severity, severityCnt);
                }
            } finally {
                rs.close();
            }
        }

        public Map<String, String> getVulnerableStatsInfo() {
            return vulStatsInfo;
        }

    }

    static class GetAgeingVulnerableBySeverityData extends QueryExecutor {
        Map<String, String> vulAgeingInfo = new LinkedHashMap<String, String>();

        GetAgeingVulnerableBySeverityData(SubscriptionMain main) {
            super(main);
        }

        protected void execute(IStatementPool pool) throws SQLException {
            String sqlStr2 = "select  severity, count(severity) as 'vulnerable_count' , min(published_date) Identified_date,min(datediff(day,published_date,getdate())) ageing_days\n" +
                    "from inv_security_oval_compliance a, inv_sec_oval_defn_cve_details b where \n" +
                    "a.content_id = b.content_id and\n" +
                    "b.definition_severity in (select distinct(severity) from inv_sec_oval_defn_cve_details where severity !=' ') \n" +
                    "and b.severity != ' '  and b.severity != 'null' \n" +
                    "and overall_compliant_level like 'NON%'\n" +
                    "group by severity \n" +
                    "order by severity";

            // Taken results from derived table
            //String sqlStr = "select * from ds_derived_ageing_chart";
            String sqlStr = "SELECT severity\r\n"
            		+ "	,COUNT(*) AS vulnerable_count\r\n"
            		+ "	,t1.published_date AS identified_date\r\n"
            		+ "	,DATEDIFF(DAY, t1.published_date, GETDATE()) AS ageing_days\r\n"
            		+ "FROM inv_sec_oval_defn_cve_records t1\r\n"
            		+ "	,security_cve_patch_info t2\r\n"
            		+ "WHERE t1.reference_name NOT LIKE 'cpe%'\r\n"
            		+ "	AND t1.severity != 'null'\r\n"
            		+ "	AND t1.repository_id = t2.repository_id\r\n"
            		+ "	AND t1.severity != ' '\r\n"
            		+ "	AND t1.published_date IS NOT NULL\r\n"
            		+ "GROUP BY t1.severity, t1.published_date;";

            PreparedStatement st = pool.getConnection().prepareStatement(sqlStr);
            ResultSet rs = st.executeQuery();
            try {
                int idx = 0;
                while(rs.next()) {
                    String severity = rs.getString("severity");
                    String severityCnt = String.valueOf(rs.getInt("vulnerable_count"));
                    String ageingDays = String.valueOf(rs.getInt("ageing_days"));
                    vulAgeingInfo.put(severity + ":" + String.valueOf(idx), severityCnt + "," + ageingDays);
                    idx++;
                }
            } finally {
                rs.close();
            }
        }

        public Map<String, String> getVulnerableStatsInfo() {
            return vulAgeingInfo;
        }

    }


    public static class GetPriorityPatchesInfo extends DatabaseAccess {
        List<PriorityPatchesBean> prtyPatchesInfo = new ArrayList<PriorityPatchesBean>();

        public GetPriorityPatchesInfo(SubscriptionMain main) {

            GetPriorityPatchesData result = new GetPriorityPatchesData(main);

            try {
                runQuery(result);
                prtyPatchesInfo = result.getPriorityPatchesInfo();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public List<PriorityPatchesBean> getPriorityPatchesInfo() {
            return prtyPatchesInfo;
        }
    }


    public static class GetMitigatePatchesInfo extends DatabaseAccess {
        List<MitigatePatchesBean> mitigatePatchesInfo = new ArrayList<MitigatePatchesBean>();

        public GetMitigatePatchesInfo(SubscriptionMain main, String repIDs) {

            GetMitigatePatchesData result = new GetMitigatePatchesData(main, repIDs);

            try {
                runQuery(result);
                mitigatePatchesInfo = result.getMitigatePatchesInfo();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public List<MitigatePatchesBean> getMitigatePatchesInfo() {
            return mitigatePatchesInfo;
        }
    }

    public static class GetTopVulnerabilitiesInfo extends DatabaseAccess {
        List<TopVulnerableStatusBean> topVulInfo = new ArrayList<TopVulnerableStatusBean>();

        public GetTopVulnerabilitiesInfo(SubscriptionMain main) {

            GetTopVulnerabilitiesData result = new GetTopVulnerabilitiesData(main);

            try {
                runQuery(result);
                topVulInfo = result.getTopVulnerabilitiesInfo();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public List<TopVulnerableStatusBean> getTopVulnerabilitiesInfo() {
            return topVulInfo;
        }
    }

    public static class GetPatchComplianceStatusInfo extends DatabaseAccess {
        List<PatchComplianceStatusBean> patchCompStatusInfo = new ArrayList<PatchComplianceStatusBean>();
        String action;

        public GetPatchComplianceStatusInfo(SubscriptionMain main, String action) {
            this.action = action;
            GetPatchComplianceStatusData result = new GetPatchComplianceStatusData(main, action);

            try {
                runQuery(result);
                patchCompStatusInfo = result.getPatchComplianceStatusInfo();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public List<PatchComplianceStatusBean> getPatchComplianceStatusInfo() {
            return patchCompStatusInfo;
        }
    }

    static class GetMitigatePatchesData extends QueryExecutor {
        List<MitigatePatchesBean> mitigatePatchesInfo = new ArrayList<MitigatePatchesBean>();
        String repIDs;

        GetMitigatePatchesData(SubscriptionMain main, String repIds) {
            super(main);
            repIDs = repIds;
        }

        protected void execute(IStatementPool pool) throws SQLException {
            String sqlStr = "select  distinct im.name as machinename, ap.current_status, patchgrp.patchgroup from  GetPatchGroupInformation() patchgrp, all_patch ap, inv_machine im\n" +
                    "where ap.repository_id = patchgrp.patch\n" +
                    "and im.id = ap.machine_id\n" +
                    "and ap.repository_id  IN( " + repIDs + ") \n" +
                    "and exists (select 1 from ldapsync_targets_marimba ltm \n" +
                    "               where ltm.marimba_table_primary_id = im.id) \n" +
                    "order by im.name";

            PreparedStatement st = pool.getConnection().prepareStatement(sqlStr);
            ResultSet rs = st.executeQuery();
            try {
                while(rs.next()) {
                    MitigatePatchesBean mitiPatchesBean = new MitigatePatchesBean();
                    String machineName = rs.getString("machinename");
                    String currentStatus = rs.getString("current_status");
                    String patchGroup = rs.getString("patchgroup");

                    System.out.println("DebugInfo-Result: "+ machineName + "\t" + currentStatus + "\t" + patchGroup);
                    mitiPatchesBean.setMachineName(machineName);
                    mitiPatchesBean.setStatus(currentStatus);
                    mitiPatchesBean.setPatchGroupName(patchGroup);

                    mitigatePatchesInfo.add(mitiPatchesBean);
                }
            } finally {
                rs.close();
            }
        }

        public List<MitigatePatchesBean> getMitigatePatchesInfo() {
            return mitigatePatchesInfo;
        }

    }

    static class GetPriorityPatchesData extends QueryExecutor {
        List<PriorityPatchesBean> prtyPatchesInfo = new ArrayList<PriorityPatchesBean>();

        GetPriorityPatchesData(SubscriptionMain main) {
            super(main);
        }

        protected void execute(IStatementPool pool) throws SQLException {
            String sqlStr2 = " select distinct ap.repository_id as \"PatchName\", t2.severity as \"Severity\" ,count(distinct im.id) as \"AffectedMachines\"\n" +
                    "                    from inv_sec_oval_defn_cve_details t1, all_patch ap, security_cve_info t2, inv_machine im\n" +
                    "                    where ap.repository_id=t1.repository_id\n" +
                    "                    and (t1.reference_name = t2.cve_name and t1.severity != 'null' and t1.severity = 'Critical')\n" +
                    "                    and im.id = ap.machine_id\n" +
                    "                    and exists (select 1 from all_patch ap where\n" +
                    "                    (ap.repository_id = t1.repository_id) \n" +
                    "                       and (ap.current_status = 'Missing' or ap.current_status = 'Available-SP'))\n" +
                    "                    group by ap.repository_id, t2. severity, im.id";

            // Taken results from derived table
            String sqlStr = "select * from ds_derived_crit_patches";

            PreparedStatement st = pool.getConnection().prepareStatement(sqlStr);
            ResultSet rs = st.executeQuery();
            try {
                while(rs.next()) {
                    PriorityPatchesBean prtyPatchesBean = new PriorityPatchesBean();
                    String patchName = rs.getString("patch_name");
                    String severity = rs.getString("severity");
                    String affectedMachines = String.valueOf(rs.getInt("affected_machines"));
                  // System.out.println("DebugInfo-Result: "+ patchName + "\t" + severity + "\t" + affectedMachines);
                    prtyPatchesBean.setPatchName(patchName);
                    prtyPatchesBean.setSeverity(severity);
                    prtyPatchesBean.setAffectedMachines(affectedMachines);

                    prtyPatchesInfo.add(prtyPatchesBean);
                }
            } finally {
                rs.close();
            }
        }

        public List<PriorityPatchesBean> getPriorityPatchesInfo() {
            return prtyPatchesInfo;
        }

    }


    static class GetTopVulnerabilitiesData extends QueryExecutor {
        List<TopVulnerableStatusBean> topVulInfo = new ArrayList<TopVulnerableStatusBean>();

        GetTopVulnerabilitiesData(SubscriptionMain main) {
            super(main);
        }

        protected void execute(IStatementPool pool) throws SQLException {

            String sqlStr2 = "select  t1.cve_name as cve_id, t1.severity as severity,count(distinct ap.machine_id) as " +
                    "affected_machines,   ap.repository_id as patch_id,  case when (ap.current_status = 'Missing' or " +
                    "ap.current_status = 'Available-SP')  then 'Patch Not Applied'   " +
                    "when (ap.current_status = 'Effectively-Installed' or  ap.current_status = 'Installed') " +
                    " then 'Patch Applied'   when (ap.current_status = 'Reboot-pending' or  " +
                    "ap.current_status = 'Reboot-Pending-SP')  then 'Patch Applied Reboot Required'   " +
                    "else 'Patch Failed'  end as status,   (t1.cvss_score * count(distinct ap.machine_id)) " +
                    "as risk_score   from inv_sec_oval_defn_cve_records t1, security_cve_patch_info t2, all_patch ap   " +
                    "where t1.reference_name not like 'cpe%' and t1.severity != 'null'  and t1.severity != ''  " +
                    "and t1.repository_id = t2.repository_id  and t2.cve_name = ap.[cve id]  " +
                    "and exists (select 1 from ldapsync_targets_marimba ltm " +
                    "where ltm.marimba_table_primary_id = ap.machine_id)  " +
                    "group by t1.cve_name, t1.severity, t1.cvss_score, " +
                    "ap.current_status,ap.repository_id order by   risk_score desc";

            // Taken results from derived table
            String  sqlStr = "select * from ds_derived_top_vuln";

            PreparedStatement st = pool.getConnection().prepareStatement(sqlStr2);
            ResultSet rs = st.executeQuery();
            try {
                while(rs.next()) {
                    TopVulnerableStatusBean topVulBean = new TopVulnerableStatusBean();
                    String cveID = rs.getString("cve_id");
                    String severity = rs.getString("severity");
                    String affectedMachines = String.valueOf(rs.getInt("affected_machines"));
                    String patchID = rs.getString("patch_id");
                    String status = rs.getString("status");
                    String risk_score = rs.getString("risk_score");

                    topVulBean.setCveId(cveID);
                    topVulBean.setSeverity(severity);
                    topVulBean.setAffectedMachines(affectedMachines);
                    topVulBean.setPatchId(patchID);
                    topVulBean.setStatus(status);
                    topVulBean.setRiskScore(risk_score);

                    topVulInfo.add(topVulBean);
                }
            } finally {
                rs.close();
                sqlStr2=null;
            }
        }

        public List<TopVulnerableStatusBean> getTopVulnerabilitiesInfo() {
            return topVulInfo;
        }

    }

    static class GetPatchComplianceStatusData extends QueryExecutor {
        List<PatchComplianceStatusBean> patchCompStatusInfo = new ArrayList<PatchComplianceStatusBean>();
        String action;

        GetPatchComplianceStatusData(SubscriptionMain main, String action) {
            super(main);
            this.action = action;
        }

        protected void execute(IStatementPool pool) throws SQLException {

            String sqlStr = "select distinct im.id, im.name, patchgrp.patch, patchgrp.patchgroup, mc.compliance_level \n" +
                    "from inv_machine im, all_patch ap, inv_machinecompliance mc, GetPatchGroupInformation() patchgrp\n" +
                    "where  im.id =  ap.machine_id\n" +
                    "and ap.repository_id = patchgrp.patch\n" +
                    "and (im.id = mc.machine_id\n" +
                    "and mc.url like '%PatchManagement%')\n" +
                    "and (upper(ap.current_status) = upper('Effectively-Installed') or upper(ap.current_status) = upper('Installed'))\n" +
                    "and mc.compliance_level ='COMPLIANT' ";

            if ("patch_notapplied".equalsIgnoreCase(action)) {
                 sqlStr = "select distinct im.id, im.name, patchgrp.patch, patchgrp.patchgroup, mc.compliance_level \n" +
                         "from inv_machine im, all_patch ap, inv_machinecompliance mc, GetPatchGroupInformation() patchgrp\n" +
                         "where  im.id =  ap.machine_id\n" +
                         "and ap.repository_id = patchgrp.patch\n" +
                         "and (im.id = mc.machine_id\n" +
                         "and mc.url like '%PatchManagement%')\n" +
                         "and (upper(ap.current_status) = upper('Missing') or upper(ap.current_status) = upper('Available-SP'))\n" +
                         "and mc.compliance_level ='NON-COMPLIANT'";
            } else if ("patch_notscanned".equalsIgnoreCase(action)) {
                sqlStr = "select distinct im.id, im.name, patchgrp.patch, patchgrp.patchgroup from inv_machine im, all_patch ap, \n" +
                        "inv_machinecompliance mc, GetPatchGroupInformation() patchgrp\n" +
                        "where  im.id =  ap.machine_id\n" +
                        "and ap.repository_id = patchgrp.patch\n" +
                        "and (im.id = mc.machine_id\n" +
                        "and mc.url like '%PatchManagement%')\n" +
                        "  and not exists (select 1 from inv_tunerchannel itc,inv_tuner it\n" +
                        "  where (itc.url like '%SubscriptionService%' or itc.url like '%PatchService%') and\n" +
                        "  (itc.tuner_id = it.tuner_id and it.machine_id = im.id)\n" +
                        "  and it.machine_id = im.id \n" +
                        "  and itc.state = 'subscribed' or itc.state = 'running')";
            }

            PreparedStatement st = pool.getConnection().prepareStatement(sqlStr);
            ResultSet rs = st.executeQuery();
            try {
                while(rs.next()) {
                    PatchComplianceStatusBean pcsBean = new PatchComplianceStatusBean();
                    String machineId = rs.getString("id");
                    String machineName = rs.getString("name");
                    String patch = rs.getString("patch");
                    String patchgroup = rs.getString("patchgroup");
                    String complevel = "";
                    if ("patch_applied".equalsIgnoreCase(action) || "patch_notapplied".equalsIgnoreCase(action)) {
                      complevel = rs.getString("compliance_level");
                    } else {
                      complevel = "Not Scanned";
                    }

                    pcsBean.setMachineId(machineId);
                    pcsBean.setMachineName(machineName);
                    pcsBean.setPatch(patch);
                    pcsBean.setPatchGroup(patchgroup);
                    pcsBean.setComplianceLevel(complevel);

                    patchCompStatusInfo.add(pcsBean);
                }
            } finally {
                rs.close();
            }
        }

        public List<PatchComplianceStatusBean> getPatchComplianceStatusInfo() {
            return patchCompStatusInfo;
        }

    }

    public static class GetScanEndPointMachineCount extends DatabaseAccess {
       int count = 0;

        public GetScanEndPointMachineCount(SubscriptionMain main, String scanType) {

          GetEndpointMachineScanInfo result = new GetEndpointMachineScanInfo(main, scanType);

            try {
                runQuery(result);
                count = result.getScanCount();
            } catch (Exception dae) {
                dae.printStackTrace();
            }
        }

        public int getScanCount() {
            return count;
        }
    }

     // To get VScan or Patch Scan machines count
    static class GetEndpointMachineScanInfo extends QueryExecutor {
        int count = 0;
        String scanType;

         GetEndpointMachineScanInfo(SubscriptionMain main, String scanType) {
            super(main);
            this.scanType = scanType;
        }

        protected void execute(IStatementPool pool) throws SQLException {

            String vscanSQL = "select  COUNT(*) as vscan_count from inv_machine im \n" +
                              " where exists (select 1 from inv_security_oval_compliance  soc where \n" +
                              " soc.machine_id = im.id and soc.content_name like '%oval.vulnerability%')";

            String patchScanSQL = "select  COUNT(*) as patchscan_count from inv_machine im \n" +
                                  " where exists (select 1 from inv_security_oval_compliance  soc where \n" +
                                  " soc.machine_id = im.id and soc.content_name like '%oval.patch%')";

            PreparedStatement st = null;
            if ("vscan".equalsIgnoreCase(scanType)) {
                st = pool.getConnection().prepareStatement(vscanSQL);
            } else if ("patchscan".equalsIgnoreCase(scanType)) {
                st = pool.getConnection().prepareStatement(patchScanSQL);
            }

            ResultSet rs = st.executeQuery();
            try {
                if (rs.next()) {
                    if ("vscan".equalsIgnoreCase(scanType)) {
                        count = rs.getInt("vscan_count");
                    } else if ("patchscan".equalsIgnoreCase(scanType)) {
                        count = rs.getInt("patchscan_count");
                    }
                }
            } finally {
                rs.close();
            }

        }

        public int getScanCount() {
            return count;
        }

    }



    static class GetAllEndpointMachineCountData extends QueryExecutor {
        int count = 0;
        String osType;

        GetAllEndpointMachineCountData(SubscriptionMain main, String osType) {
            super(main);
            this.osType = osType;
        }


        protected void execute(IStatementPool pool) throws SQLException {
            String sql = "";
            if ("all".equals(osType)) {
               sql =  "select  COUNT(distinct im.id) as 'Scanned Machines Count' \n" +
                        " from inv_machine im, ldapsync_targets_marimba ltm  \n" +
                        " where ltm.marimba_table_primary_id = im.id" ;
            } else {
               sql = 	"select  COUNT(distinct ios.machine_id) as 'Scanned Machines Count' " +
                       " from inv_os ios where product like '%" + osType + "%' and exists (select 1 from ldapsync_targets_marimba ltm " +
                       " where ltm.marimba_table_primary_id = ios.machine_id) ";
            }
            PreparedStatement st = pool.getConnection().prepareStatement(sql);

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

    static class GetComplianceReportNotCheckedInInfo extends QueryExecutor {
        List<ReportingNotCheckedInBean> rptNotCheckedIn = new ArrayList<ReportingNotCheckedInBean>();
        GetComplianceReportNotCheckedInInfo(SubscriptionMain main) {
            super(main);
        }

        protected void execute(IStatementPool pool) throws SQLException {
         PreparedStatement st = pool.getConnection().prepareStatement("select HostName, Scantime from derived_inv_compliance dic " +
                 "where dic.ComplianceStaus = 'NotCheckedIn' " +
                 "and dic.scantime > (select getutcdate() - 100)");   // todo needs revert back to 1
            ResultSet rs = st.executeQuery();
            try {
                    while (rs.next()) {
                       ReportingNotCheckedInBean beanObj = new ReportingNotCheckedInBean(); 
                       String hostName = rs.getString("HostName");
                       String scanTime = rs.getString("Scantime");
                       beanObj.setHostName(hostName);
                       beanObj.setScanTime(scanTime);
                       rptNotCheckedIn.add(beanObj); 
                    }
            } finally {
                rs.close();
            }

        }

        public List<ReportingNotCheckedInBean> getReportNotCheckedInInfo() {
            return rptNotCheckedIn;
        }
    }

    static class GetComplianceReportingInfo extends QueryExecutor {
        int checkedIn = 0;
        int notCheckedIn = 0;
        int notAvailable = 0;

        int compliant = 0;
        int nonCompliant = 0;
        String complianceType;

        GetComplianceReportingInfo(SubscriptionMain main, String complianceType) {
            super(main);
            this.complianceType = complianceType;
        }


        protected void execute(IStatementPool pool) throws SQLException {
            PreparedStatement st = null;

            if ("reporting".equalsIgnoreCase(complianceType)) {
                st = pool.getConnection().prepareStatement("select count(*) as 'Count', ComplianceStaus as 'Type' from derived_inv_compliance dic \n" +
                        "   where exists (select 1 from ldapsync_targets_marimba ltm where ltm.marimba_table_primary_id = dic.machine_id)\n" +
                        "    group by ComplianceStaus having count (*) > 0");
            } else if ("security".equalsIgnoreCase(complianceType)) {
                st = pool.getConnection().prepareStatement("select count(distinct machinename) as 'Count' , overall_compliant_level as 'Type' \n" +
                        "from inv_security_oval_comp_overall group by overall_compliant_level having count(*) > 0");
            } else {
                st = pool.getConnection().prepareStatement("select * from derived_patch_compliance");
            }

            ResultSet rs = st.executeQuery();
            try {
                if ("reporting".equalsIgnoreCase(complianceType)) {
                    while (rs.next()) {
                        int count =  rs.getInt("Count");
                        String type = rs.getString("Type");
                        if ("CheckedIn".equalsIgnoreCase(type)) {
                            checkedIn = count;
                        } else if ("NotCheckedIn".equalsIgnoreCase(type)) {
                            notCheckedIn = count;
                        }
                    }
                } else if ("security".equalsIgnoreCase(complianceType)) {
                    while (rs.next()) {
                        int count =  rs.getInt("Count");
                        String type = rs.getString("Type");
                        if ("COMPLIANT".equalsIgnoreCase(type)) {
                            compliant = count;
                        } else if ("NON-COMPLIANT".equalsIgnoreCase(type)) {
                            nonCompliant = count;
                        }
                    }
                } else {
                    if (rs.next()) {
                        nonCompliant = rs.getInt("vulnerable");
                        compliant = rs.getInt("non_vulnerable");
                    }
                }
            } finally {
                rs.close();
            }

        }

        public int getCheckedIn() {
            return checkedIn;
        }

        public int getNotCheckedIn() {
            return notCheckedIn;
        }

        public int getNotAvailable() {
            return notAvailable;
        }

        public int getCompliant() {
            return compliant;
        }

        public int getNonCompliant() {
            return nonCompliant;
        }

    }

    static class GetChannelStoreCredentialsData extends QueryExecutor {
        Map<String, String> channelStoreCredsInfo = new LinkedHashMap<String, String>();
        String username;
        String password;
        
        GetChannelStoreCredentialsData(SubscriptionMain main, String username, String password) {
            super(main);
            this.username = username;
            this.password = password;
        }

        protected void execute(IStatementPool pool) throws SQLException {

            String sqlStr = "select * from cs_user_details where upper(user_name)=upper('" + username + "') and password='" + password + "'";

            PreparedStatement st = pool.getConnection().prepareStatement(sqlStr);
            ResultSet rs = st.executeQuery();
            try {
                while(rs.next()) {
                    String user = rs.getString("user_name");
                    String pwd = rs.getString("password");
                    channelStoreCredsInfo.put("username", user);
                    channelStoreCredsInfo.put("password", pwd);
                }
            } finally {
                rs.close();
            }
        }

        public Map<String, String> getChannelStoreCredentialsInfo() {
            return channelStoreCredsInfo;
        }

    }

    private static void debug(String msg) {

        if (IAppConstants.DEBUG5) System.out.println("DashboardInfoDetails: " + msg);

    }

}
