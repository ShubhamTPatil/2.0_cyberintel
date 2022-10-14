package com.marimba.apps.securitymgr.db;

import com.marimba.tools.util.QuotedTokenizer;

public class DatabaseAccess {

    public void runQuery(QueryExecutor query) throws Exception {
        query.doQuery();
    }

    public void runQueryAsync(QueryExecutor query) {
        // REMIND: Must use a thread-pool here
        Thread thread = new Thread(query);
        thread.start();
    }

    public String replaceWildcards(String pattern) {
        pattern = pattern.replace('*', '%').replace('?', '_');
        StringBuffer buf = new StringBuffer(pattern.length() + 6);
        QuotedTokenizer tok = new QuotedTokenizer(pattern, ' ', '\\');
        int i = 0;
        while (tok.hasMoreTokens()) {
            if (buf.length() > 0) {
                char c = buf.charAt(buf.length() - 1);
                if (c != '%') {
                    buf.append('%');
                }
            }
            String str = tok.nextToken();
            buf.append(str);
            if (!str.endsWith("%")) {
                buf.append('%');
            }
            i++;
        }
        if (buf.length() == 0) {
            buf.append('%');
        }
        return buf.toString();
    }

}