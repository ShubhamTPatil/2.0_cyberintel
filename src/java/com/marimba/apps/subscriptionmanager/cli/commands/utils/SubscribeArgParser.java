package com.marimba.apps.subscriptionmanager.cli.commands.utils;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.commons.lang.math.NumberUtils;

public class SubscribeArgParser {

    String str[];

    public SubscribeArgParser(String arg) {
           StringTokenizer  strTokens = new StringTokenizer(arg, ",");
           str = new String[strTokens.countTokens()];
           int count = 0 ;
           while(strTokens.hasMoreTokens()) {
                       str[ count++ ] = strTokens.nextToken();
           }
    }
    public int getArgCount() {
        return  str.length;
    }
    public HashMap parseStatesAndOrder() {

     HashMap mp = new HashMap();
     for( int i = 0 ; i < str.length; i++) {
         String so = str[i];
         if( i == 0 ) {
            mp.put("state1", so);
         }
         if( i > 0 ) {
            mp.put(getKey(so, i+1), so);
         }
     }

     return mp;
    }
    public String getKey(String so, int current) {

         switch (str.length) {
           case 2:

                 if(NumberUtils.isNumber(so)) {
                    return "order";
                 }
                 if("true".equals(so) || "false".equals(so)) {
                    return "exempt";
                 } else {
                    return "state2";
                 }
           case 3:
                 if( current == 2 ) {
                     if(NumberUtils.isNumber(so)) {
                          return "order";
                     }
                     return "state2";
                  }
                 if( current == 3 ) {
                     if(NumberUtils.isNumber(so)) {
                           return "order";
                     }
                    return "exempt";
                 }
           case 4:
                 if( current == 2 ) {
                     return "state2";
                 }
                 if( current == 3 ) {
                     return "order";
                 }
                 if( current == 4 ) {
                     return "exempt";
                 }
         }
         return "";
   }
}
