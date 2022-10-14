package com.marimba.apps.securitymgr.webapp.forms;

import com.marimba.apps.securitymgr.view.ArgBean;
import com.marimba.apps.subscriptionmanager.intf.IAppConstants;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

public class VDeskReportForm  extends ActionForm implements IAppConstants {

    private String action;
    private String sql;
    private String desc;
    private String name;
    private String path;
    final static int DEFAULT_ARG_SIZE = 0;

    // map for links
    HashMap map = new HashMap();

    // prepare sql and description for this query
    String sortCol;
    String sortType;

    String numArg;

    // arguments
    ArgBean[] args;
    boolean valid = true;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name= name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }
    // --- Setter methods --- //

    public void setNumArg(String numArg) {
        this.numArg = numArg;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setSort(String sortCol) {
        this.sortCol = sortCol;
    }

    public void setSortType(String sortType) {
        this.sortType = sortType;
    }

    // --- Getter methods --- //

    public String getNumArg() {
        return numArg;
    }

    public String getSort() {
        return sortCol;
    }

    public String getSortType() {
        return sortType;
    }

    public void setArgs(ArgBean[] arguments) {
        this.args = arguments;

        if (args == null || args.length == 0) {
            args = new ArgBean[DEFAULT_ARG_SIZE];
            for (int i = 0; i < args.length; i++) {
                args[i] = new ArgBean();
            }
        }

        for (int i = 0; i < args.length; i++) {
            args[i].prepare(i, args, this);
        }

        // set the number with right value
        numArg = String.valueOf(args.length);
    }

    public ArgBean[] getArgs() {
        return args;
    }

    public boolean getValid() {
        return valid;
    }



    public HashMap getAllParam(ArgBean[] args) {
        getParameters(args);
        return map;
    }

    private void getParameters(ArgBean[] args) {

        put(map, KEY_TYPE, TYPE_FORM_QUERY);
        put(map, KEY_NAME, name);
        //put(map, KEY_FOLDER, folder);
        put(map, KEY_ARGNUM, numArg);
        put(map, KEY_ACTION, action);
        put(map, KEY_SQL, sql);
        //put(map, KEY_DESC, desc);
        put(map, KEY_SORT, sortCol);
        put(map, KEY_SORT_TYPE, sortType);
        for (int i = 0; i < args.length; i++) {
            args[i].getParameters(map);
        }
    }

    private void put(HashMap hm, String key, String value) {
        if (value != null) {
            hm.put(key, value);
        }
    }

    public void reset(ActionMapping actionMapping, HttpServletRequest httpServletRequest) {

    }
}
