// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.

// $File$, $Revision$, $Date$

package com.marimba.apps.subscriptionmanager.compliance.intf;


/**
 * ICveUpdateConstants
 *
 * @author: Nandakumar Sankaralingam
 * @version: $Date$, $Revision$
 *
 */
public interface ICveUpdateConstants {


    public String CREATE_TABLE_SQL =
            "IF EXISTS ( SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'product_vulnerable_prod_defn') \n" +
                "drop table product_vulnerable_prod_defn;\n" +
            "IF EXISTS ( SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'product_cve_vconf_cpe22') \n" +
                "drop table product_cve_vconf_cpe22;\n" +
            "IF EXISTS ( SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'product_cve_vconf') \n" +
                "drop table product_cve_vconf;\n" +
            "IF EXISTS ( SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'product_cve_reference') \n" +
                "drop table product_cve_reference;\n" +
            "IF EXISTS ( SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'product_cve_impact') \n" +
               "drop table product_cve_impact;\n" +
            "IF EXISTS ( SELECT 1 FROM sysobjects WHERE type = 'U' AND name = 'product_cve_acl') \n" +
              "drop table product_cve_acl;\n" +

            "drop table product_cve_info;\n" +
            "drop table product_info;\n" +
            "drop table vendor_info;\n" +
            "\n" +
            "create table vendor_info (\n" +
            "  id integer not null primary key ,\n" +
            "  name nvarchar(128) not null\n" +
            ");\n" +
            "\n" +
            "create table product_info (\n" +
            "  id integer not null primary key ,\n" +
            "  vendor_id integer not null references vendor_info(id),\n" +
            "  name nvarchar(128) not null\n" +
            ");\n" +
            "\n" +
            "create table product_cve_info (\n" +
            "  id integer not null primary key,\n" +
            "  vendor_id integer not null references vendor_info(id),\n" +
            "  product_id integer not null references product_info(id),\n" +
            "  name nvarchar(128) not null,\n" +
            "  modified datetime,\n" +
            "  published datetime,\n" +
            "  severity nvarchar(20),\n" +
            "  cvss numeric,\n" +
            "  cvss_time datetime,\n" +
            "  cwe nvarchar(64),\n" +
            "  cve_id nvarchar(64),\n" +
            "  summary nvarchar(2048)\n" +
            ");\n" +
            "grant insert, update, delete, select on product_cve_info to inventory;\n" +
            "grant select on product_cve_info to user_view;\n" +
            "\n" +
            "grant insert, update, delete, select on product_info to inventory;\n" +
            "grant select on product_info to user_view;\n" +
            "\n" +
            "grant insert, update, delete, select on vendor_info to inventory;\n" +
            "grant select on vendor_info to user_view;";


    public String UPDATE_TABLE_SQL = "insert into security_cve_info \n" +
            "(type,cve_name,seq,published_date,modified_date,severity, \n" +
            "cvss_score,cvss_base_score,cvss_impact_score,cvss_exploit_score,cvss_vector,nvd_xml_schema,cvss_version) \n" +
            "select 'CVE', name, '90',published ,modified, severity,cvss,cvss , '-1','-1' ,'NA' ,'2.0','2.0' \n" +
            "from cve_info tp with (nolock) \n" +
            "where not exists (select 1 from security_cve_info ts with (nolock) where tp.cve_id = ts.cve_name); \n" +
            "\n" +
            "update security_cve_info set severity='None' where cvss_score is not null and cvss_score = 0; \n" +
            "update security_cve_info set severity='Low' where cvss_score is not null and ((cvss_score  >= 0.1 and cvss_score <=3.9) or cvss_score=-1); \n" +
            "update security_cve_info set severity='Medium' where cvss_score is not null and cvss_score >= 4.0 and cvss_score <=6.9; \n" +
            "update security_cve_info set severity='High' where cvss_score is not null and cvss_score >= 7.0 and cvss_score <=8.9; \n" +
            "update security_cve_info set severity='Critical' where cvss_score is not null and cvss_score >= 9.0 and cvss_score <=10.0;";

}


