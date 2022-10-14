// Copyright 1996-2013, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.util;

import com.marimba.apps.subscriptionmanager.intf.IErrorConstants;
import com.marimba.apps.subscriptionmanager.intf.IWebAppConstants;
import com.marimba.webapps.intf.IMapProperty;
import com.marimba.webapps.intf.InternalException;
import com.marimba.webapps.intf.KnownException;
import org.apache.commons.validator.Field;
import org.apache.commons.validator.GenericValidator;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class to keep validation code for input.  This can be used from the commandline or the GUI. Please note that the methods in the ValidationUtil expect the
 * value to be defined.
 *
 * @author Angela Saval
 * @version 1.2, 01/31/2002
 */

public class ValidationUtil implements IWebAppConstants, IErrorConstants {

    private static boolean DEBUG = IWebAppConstants.DEBUG;

    /**
     * Verify that string specified is a valid URL by trying to construct a URL.
     *
     * @param value The string representing the URL.
     *
     * @throws KnownException REMIND
     * @throws InternalException REMIND
     */
    public static void validateURL(String value) throws KnownException, InternalException {
        if (value == null) {
            //throw new InternalException(USETX_INVALID_PLUGIN_URL, "ValidationUtil.validateURL", value);
            throw new KnownException(VALIDATION_INVALIDURL, value);
        }

        int httpi = value.indexOf("http://");
        if (httpi < 0) {
            value = "http://" + value;
        }
        try {
            new URL(value);
        } catch (Exception ex) {
            throw new KnownException(VALIDATION_INVALIDURL, value);
        }
    }

    public static void validateURLAndPath(String value) throws KnownException, InternalException {
        validateURL(value);
        try{
            URL url = new URL(value);
            if(url.getFile().endsWith("/")) {
                throw new Exception();
            }
        } catch (Exception ex) {
            throw new KnownException(VALIDATION_INVALIDURL, value);
        }
    }

    /**
     * Validates the connection pool size for an LDAP connection
     *
     * @param value The string representing the URL.
     *
     * @throws KnownException REMIND
     * @throws InternalException REMIND
     */
    public static void validatePoolSize(String value) throws KnownException, InternalException {
        if (value == null) {
            //throw new InternalException(VALIDATION_INTERNAL_WRONGARG, "ValidationUtil.validatePoolSize", value);
            throw new KnownException(VALIDATION_POOLSIZENONINT, value);
        }

        try {
            new Integer(value);
        } catch (Exception ex) {
            throw new KnownException(VALIDATION_POOLSIZENONINT, value);
        }
    }

    /**
     * Verifies the parameter passed in is a valid server name.  This method will not verify that the server is reachable. Validation is done according to RFC
     * 1035 A url to refer to for this is http://www.freesoft.org/CIE/RFC/1035/6.htm CHAR        =  &lt;any ASCII character>         sub-domain  =  domain-ref
     * / domain-literal domain      =  sub-domain ("." sub-domain) domain-literal =  "[" (dtext / quoted-pair) "]" domain-ref  =  atom ; symbolic reference
     * dtext       =  &lt;any CHAR excluding "[",     ; => may be folded "]", "\" & CR, & including linear-white-space>
     *
     * @param value REMIND
     *
     * @throws KnownException REMIND
     * @throws InternalException REMIND
     */
    public static void validateHostAndOptionalPort(String value) throws KnownException, InternalException {
        if (value == null) {
            //throw new InternalException(VALIDATION_INTERNAL_WRONGARG, "ValidationUtil.validateHostAndOptionalPort", value);
            throw new KnownException(VALIDATION_INVALIDCHARINHOST, value, value);
        }

        for (int i = value.length(); i-- > 0;) {
            char c = value.charAt(i);

            switch (c) {
                // the following list of characters are not ok
                case '\\':
                case ' ':
                case ',':
                    throw new KnownException(VALIDATION_INVALIDCHARINHOST, value, String.valueOf(c));
            }
        }

        // Next, verify that there if there is a port specified, it is an integer
        int pind = value.lastIndexOf(":");

        if (pind < 0) {
            //Nothing to verify since port was not specified
            return;
        }

        String port = value.substring(pind + 1);

        try {
            new Integer(port);
        } catch (Exception e) {
            throw new KnownException(VALIDATION_INVALIDPORT, port);
        }
    }

    /**
     * REMIND
     *
     * @param bean REMIND
     * @param field REMIND
     *
     * @return REMIND
     */
    public static boolean validateRequiredNoAddError(Object bean, Field  field) {
        String value = null;

        if ((field.getProperty() != null) && (field.getProperty()
                .length() > 0)) {
            value = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());
        }

        if (DEBUG) {
            System.out.println("validateRequirednoAddError, value= " + value);
        }

        return !GenericValidator.isBlankOrNull(value);
    }

    /**
     * REMIND
     *
     * @param bean REMIND
     * @param field REMIND
     *
     * @return REMIND
     */
    public static boolean validateIntegerNoAddError(Object bean, Field  field) {
        String value = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());
        value = value.trim();

        if (DEBUG) {
            System.out.println("validateIntegernoAddError, value= " + value);
        }

        return !(!GenericValidator.isBlankOrNull(value) && !GenericValidator.isInt(value));
    }

    /**
     * REMIND
     *
     * @param bean REMIND
     * @param field REMIND
     *
     * @return REMIND
     */
    public static boolean validateRangeNoAddError(Object bean, Field  field) {
        String value = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());
        value = value.trim();

        String sMin = field.getVarValue("min");
        String sMax = field.getVarValue("max");

        if (DEBUG) {
            System.out.println("validateRangeNoAddError");
            System.out.println("value= " + value);
            System.out.println("sMin= " + sMin);
            System.out.println("sMax= " + sMax);
        }

        if (!GenericValidator.isBlankOrNull(value)) {
            try {
                int iValue = Integer.parseInt(value);
                int min = Integer.parseInt(sMin);
                int max = Integer.parseInt(sMax);

                if (!GenericValidator.isInRange(iValue, min, max)) {
                    return false;
                }
            } catch (Exception e) {
                // validateInteger should be called previously
                return false;
            }
        }

        return true;
    }

    /**
     * Verify the given password is to statisfy the following password rules
     * password must be exactly 8 characters long, must have at least 1 digit ,
     * at least 1 non alpha-numeric character  and both lowercase and uppercase latin letters
     *
     * @param passwd REMIND
     * @param max8Char REMIND
     *
     */
    public static boolean checkPasswordStrength(String passwd, String max8Char) {
        int upperCase = 0, lowerCase = 0, numbers = 0, special = 0, pwdLength = 0;

        // Rules variables
        int PASSWORD_LOWER_CASE = 1;
        int PASSWORD_UPPER_CASE = 1;
        int PASSWORD_NUMERIC = 1;
        int PASSWORD_SPECIAL = 1;
        int PASSWORD_LENNGTH = 8;

        pwdLength = passwd.length();

        if("true".equals(max8Char)) {
            if(pwdLength != PASSWORD_LENNGTH) {
                return false;
            }
        } else {
            if(pwdLength < PASSWORD_LENNGTH) {
                return false;
            }
        }

        Pattern passwordPattern;
        Matcher passwordMatch;

        // LETTERS
        passwordPattern = Pattern.compile(".??[a-z]");
        passwordMatch = passwordPattern.matcher(passwd);
        while (passwordMatch.find()) // [verified] at least one Lower case letter
        {
            lowerCase += 1;
        }

        passwordPattern = Pattern.compile(".??[A-Z]");
        passwordMatch = passwordPattern.matcher(passwd);
        while (passwordMatch.find()) // [verified] at least one Upper case letter
        {
            upperCase += 1;
        }
        // NUMBERS
        passwordPattern = Pattern.compile(".??[0-9]");
        passwordMatch = passwordPattern.matcher(passwd);
        while (passwordMatch.find()) // [verified] at least one digit
        {
            numbers += 1;
        }
        // SPECIAL CHAR
        passwordPattern = Pattern.compile(".??[:,!,@,#,$,%,^,&,*,?,_,~]");
        passwordMatch = passwordPattern.matcher(passwd);
        while (passwordMatch.find()) // [verified] at least one special character
        {
            special += 1;
        }

        if(!(lowerCase >= PASSWORD_LOWER_CASE && upperCase >= PASSWORD_UPPER_CASE && numbers >= PASSWORD_NUMERIC && special >= PASSWORD_SPECIAL)) {
            return false;
        }

        return true;
    }
}
