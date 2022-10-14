// Copyright 1997-2003, Marimba, Inc. All Rights Reserved.
// Confidential and Proprietary Information of Marimba, Inc.
// Protected by or for use under one or more of the following patents:
// U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075, 6,381,631,
// and 6,430,608. Other Patents Pending.                                                                                                   valid

package com.marimba.apps.subscriptionmanager.webapp.util;

import org.apache.struts.action.*;
import org.apache.struts.util.MessageResources;

import org.apache.commons.validator.*;

import java.io.*;

import java.net.*;

import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.marimba.apps.subscriptionmanager.intf.*;

import com.marimba.tools.ldap.*;

import com.marimba.tools.util.*;

import com.marimba.webapps.intf.*;

import com.marimba.webapps.tools.util.*;
import com.marimba.castanet.schedule.Schedule;

/**
 * This is the common location for method used for input validation. For the GUI, methods are provided with the correct method declaration so that they can be
 * used within the Struts Validation Framework. The  subscriptionmanager/rsrc/etc/validation.xml uses these methods for defining the verification needed for
 * each field. All form validation methods should be written such that null is a possible input. The validation framework allows for "requiring" a field,
 * therefore it doesn't need to be enforced here.
 *
 * @author Angela Saval
 * @version 1.11, 05/17/2002
 */
public class FormValidation
    implements IWebAppConstants,
                   IErrorConstants {
    /**
     * Validates input that is supposed to be a URL.  If http or https is not specified, then http will be assumed.  This method WILL NOT attempt a network
     * connection to verify that the URL can be reached.
     *
     * @param bean Bean validation is being performed on.
     * @param va The current ValidatorAction being performed
     * @param field Field object being validated.
     * @param errors The errors objects to add an ActionError to if the validation fails
     * @param request Current request object.
     * @param application The application's ServletContext.
     *
     * @return boolean false if there is no error, true is there is.
     */
    public static boolean validateURL(java.lang.Object                      bean,
                                      ValidatorAction                       va,
                                      Field                                 field,
                                      org.apache.struts.action.ActionErrors errors,
                                      javax.servlet.http.HttpServletRequest request,
                                      javax.servlet.ServletContext          application) {
        /* Attempt to construct a URL from the input given.
         */
        String urlval = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());
		/*
        if (urlval == null) {
            return true;
        }
		*/
        try {
            ValidationUtil.validateURL(urlval);
        } catch (KnownException ke) {
            /*Create a new action error based off of the exception thrown.
             *The exception for input validation will be gotten from the inputvalidationerr
             *.properties file
             */
            ActionError er = new KnownActionError(ke);
            errors.add(field.getProperty(), er);

            if (DEBUG) {
                System.out.println("FormValidation: known failure for url validate");
            }

            return false;
        } catch (InternalException ie) {
            ActionError er = new InternalActionError(ie);

            if (DEBUG) {
                System.out.println("FormValidation: internal failure for url validate");
            }

            errors.add(field.getProperty(), er);

            return false;
        }

        return true;
    }

    public static boolean validateURLAndPath(java.lang.Object                      bean,
                                      ValidatorAction                       va,
                                      Field                                 field,
                                      org.apache.struts.action.ActionErrors errors,
                                      javax.servlet.http.HttpServletRequest request,
                                      javax.servlet.ServletContext          application) {
        String urlval = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());
        try {
            ValidationUtil.validateURLAndPath(urlval);
        } catch (KnownException ke) {
            /*Create a new action error based off of the exception thrown.
             *The exception for input validation will be gotten from the inputvalidationerr
             *.properties file
             */
            ActionError er = new KnownActionError(ke);
            errors.add(field.getProperty(), er);

            if (DEBUG) {
                System.out.println("FormValidation: known failure for url validate");
            }

            return false;
        } catch (InternalException ie) {
            ActionError er = new InternalActionError(ie);

            if (DEBUG) {
                System.out.println("FormValidation: internal failure for url validate");
            }

            errors.add(field.getProperty(), er);

            return false;
        }
        return true;
    }

    /**
     * Validates input that is supposed to be a host name and optionally a port. Verify that 'name' could be a valid server name.
     *
     * @param bean Bean validation is being performed on.
     * @param va The current ValidatorAction being performed
     * @param field Field object being validated.
     * @param errors The errors objects to add an ActionError to if the validation fails
     * @param request Current request object.
     * @param application The application's ServletContext.
     *
     * @return boolean false if there is no error, true is there is.
     */
    public static boolean validateHostAndOptionalPort(java.lang.Object                      bean,
                                                      ValidatorAction                       va,
                                                      Field                                 field,
                                                      org.apache.struts.action.ActionErrors errors,
                                                      javax.servlet.http.HttpServletRequest request,
                                                      javax.servlet.ServletContext          application) {
        String hostandport = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());

        if (hostandport == null) {
            return true;
        }

        try {
            ValidationUtil.validateHostAndOptionalPort(hostandport);
        } catch (KnownException ke) {
            ActionError er = new KnownActionError(ke);

            if (DEBUG) {
                System.out.println("FormValidation,hostandport: fieldproperty = " + field.getProperty());
            }

            errors.add(field.getProperty(), er);

            if (DEBUG) {
                System.out.println("FormValidation: known failure for hostandport");
            }

            return false;
        } catch (InternalException ie) {
            ActionError er = new InternalActionError(ie);
            errors.add(field.getProperty(), er);

            if (DEBUG) {
                System.out.println("FormValidation: internal failure for hostandport");
            }

            return false;
        }

        return true;
    }

    /**
     * Validates input that is supposed to be a list of hostname:port. Verify that 'name' could be a valid server name.
     *
     * @param bean Bean validation is being performed on.
     * @param va The current ValidatorAction being performed
     * @param field Field object being validated.
     * @param errors The errors objects to add an ActionError to if the validation fails
     * @param request Current request object.
     * @param application The application's ServletContext.
     *
     * @return boolean false if there is no error, true is there is.
     */
    public static boolean validateListHostAndOptionalPort(java.lang.Object                      bean,
                                                          ValidatorAction                       va,
                                                          Field                                 field,
                                                          org.apache.struts.action.ActionErrors errors,
                                                          javax.servlet.http.HttpServletRequest request,
                                                          javax.servlet.ServletContext          application) {
        if (DEBUG) {
			System.out.println("FormValidation,hostandport: validateListHostAndOptionalPort called...");
		}

        String hostandport = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());

		if (DEBUG) {
			System.out.println("FormValidation,hostandport: hostandport:"+hostandport);
		}

        if (hostandport == null) {
			errors.add(field.getProperty(), new KnownActionError(VALIDATION_INVALIDCHARINHOST, getFieldLabel(field.getProperty(), application)));

            return false;
        }

        String[] hosts = Utils.stringToArray(hostandport, ",", false);

        for (int i = 0; i < hosts.length; i++) {
            try {
                ValidationUtil.validateHostAndOptionalPort(hosts [i].trim());
            } catch (KnownException ke) {
                ActionError er = new KnownActionError(ke.getKey(), ke.getArg0(), ke.getArg1(), ke.getArg2(), ke.getArg3());

                if (DEBUG) {
                    System.out.println("FormValidation,hostandport: fieldproperty = " + field.getProperty());
                }

                errors.add(field.getProperty(), er);

                if (DEBUG) {
                    System.out.println("FormValidation: known failure for hostandport");
                }

                return false;
            } catch (InternalException ie) {
                ActionError er = new InternalActionError(ie.getKey(), ie.getArg0(), ie.getArg1(), ie.getArg2(), ie.getArg3());
                errors.add(field.getProperty(), er);

                if (DEBUG) {
                    System.out.println("FormValidation: internal failure for hostandport");
                }

                return false;
            }
        }

        return true;
    }

    /**
     * Validates input that is supposed to be a pool size for LDAP.
     *
     * @param bean Bean validation is being performed on.
     * @param va The current ValidatorAction being performed
     * @param field Field object being validated.
     * @param errors The errors objects to add an ActionError to if the validation fails
     * @param request Current request object.
     * @param application The application's ServletContext.
     *
     * @return boolean false if there is no error, true if there is.
     */
    public static boolean validatePoolSize(java.lang.Object                      bean,
                                           ValidatorAction                       va,
                                           Field                                 field,
                                           org.apache.struts.action.ActionErrors errors,
                                           javax.servlet.http.HttpServletRequest request,
                                           javax.servlet.ServletContext          application) {
        if (DEBUG) {
            System.out.println("FormValidation: validate PoolSize called");
        }

        /* Attempt to construct a URL from the input given.
         */
        String value = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());

        if (value == null) {
            return true;
        }

        try {
            ValidationUtil.validatePoolSize(value);
        } catch (KnownException ke) {
            /*Create a new action error based off of the exception thrown.
             *The exception for input validation will be gotten from the inputvalidationerr
             *.properties file
             */
            ActionError er = new KnownActionError(ke);
            errors.add(field.getProperty(), er);

            return false;
        } catch (InternalException ie) {
            ActionError er = new InternalActionError(ie);
            errors.add(field.getProperty(), er);

            return false;
        }

        return true;
    }

    /**
     * <p>
     * Checks if the field isn't null and length of the field is greater than zero not  including whitespace.
     * </p>
     *
     * @param bean The bean validation is being performed on.
     * @param va The <code>ValidatorAction</code> that is currently being performed.
     * @param field The <code>Field</code> object associated with the current field  being validated.
     * @param errors The <code>ActionErrors</code> object to add errors to if any  validation errors occur.
     * @param request Current request object.
     * @param application The application's <code>ServletContext</code>.
     *
     * @return REMIND
     */
    public static boolean validateRequired(Object             bean,
                                           ValidatorAction    va,
                                           Field              field,
                                           ActionErrors       errors,
                                           HttpServletRequest request,
                                           ServletContext     application) {
        String value = null;

        if ((field.getProperty() != null) && (field.getProperty()
                                                       .length() > 0)) {
            value = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());
        }

        if (GenericValidator.isBlankOrNull(value) && !"cancel".equals(request.getParameter("action"))) {
            errors.add(field.getProperty(), new KnownActionError(VALIDATION_FIELD_REQUIRED, getFieldLabel(field.getProperty(), application)));
            return false;
        } else {
            return true;
        }
    }

    /**
     * <p>
     * Checks if the field can safely be converted to an int primitive.
     * </p>
     *
     * @param bean The bean validation is being performed on.
     * @param va The <code>ValidatorAction</code> that is currently being performed.
     * @param field The <code>Field</code> object associated with the current field  being validated.
     * @param errors The <code>ActionErrors</code> object to add errors to if any  validation errors occur.
     * @param request Current request object.
     * @param application The application's <code>ServletContext</code>.
     *
     * @return REMIND
     */
    public static boolean validateInteger(Object             bean,
                                          ValidatorAction    va,
                                          Field              field,
                                          ActionErrors       errors,
                                          HttpServletRequest request,
                                          ServletContext     application) {
        String value = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());

        if (!GenericValidator.isBlankOrNull(value) && !GenericValidator.isInt(value)) {
            errors.add(field.getProperty(), new KnownActionError(VALIDATION_FIELD_INTEGER, getFieldLabel(field.getProperty(), application)));

            return false;
        } else {
            return true;
        }
    }

    /**
     * <p>
     * Checks if the field can be a tuner property value.  Right now, only integer is checked However, this could be expanded to check other attributes
     * </p>
     *
     * @param bean The bean validation is being performed on.
     * @param va The <code>ValidatorAction</code> that is currently being performed.
     * @param field The <code>Field</code> object associated with the current field  being validated.
     * @param errors The <code>ActionErrors</code> object to add errors to if any  validation errors occur.
     * @param request Current request object.
     * @param application The application's <code>ServletContext</code>.
     *
     * @return REMIND
     */
    public static boolean validateTunerProps(Object             bean,
                                             ValidatorAction    va,
                                             Field              field,
                                             ActionErrors       errors,
                                             HttpServletRequest request,
                                             ServletContext     application) {
        IMapProperty  beanProp =  (IMapProperty) bean;
        String value = (String) beanProp.getValue(field.getProperty());

        //Get the type of the tuner property to validate
        String type = GUIUtils.getValueAsString((IMapProperty) bean, TCHPROPS_TUNERPROPTYPE);

        if ("integer".equals(type)) {
            if (!GenericValidator.isBlankOrNull(value) && !GenericValidator.isInt(value)) {
                errors.add(field.getProperty(), new KnownActionError(VALIDATION_FIELD_INTEGER));

                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }
    /**
     * <p>
     * Checks if the field can be a channel property value.  Right now, only update.schedule property is checked to accept
     * for valid schedule string. However, this could be expanded to check other properties
     * </p>
     *
     * @param bean The bean validation is being performed on.
     * @param va The <code>ValidatorAction</code> that is currently being performed.
     * @param field The <code>Field</code> object associated with the current field  being validated.
     * @param errors The <code>ActionErrors</code> object to add errors to if any  validation errors occur.
     * @param request Current request object.
     * @param application The application's <code>ServletContext</code>.
     *
     * @return REMIND Nageswara Rao: need to take care for G11N schedule string
     */
    public static boolean validateChannelProps(Object bean,
                                               ValidatorAction va,
                                               Field field,
                                               ActionErrors errors,
                                               HttpServletRequest request,
                                               ServletContext application){
        IMapProperty  beanProp =  (IMapProperty) bean;
        String chPropName = ( String )beanProp.getValue( "channel_prop_name" );
        String chPropValue = (String) beanProp.getValue( field.getProperty() );
        if( TUNER_PROP_UPDATE_SCH.equalsIgnoreCase( chPropName ) || TUNER_PROP_MAR_SCH_FILTER.equalsIgnoreCase( chPropName ) || TUNER_PROP_REBOOT_SCH.equalsIgnoreCase( chPropName )){
            try {
                StringTokenizer tokens = new StringTokenizer(chPropValue.toUpperCase(), " ");
                String word = tokens.nextToken();
                Schedule schedule = null;
                if ("NEVER".equals(word)) {
                    // Restrict setting "NEVER" to marimba.schedule.filter [Defect Id: MA29294]
                    if(TUNER_PROP_MAR_SCH_FILTER.equalsIgnoreCase(chPropName)) {
                        errors.add(chPropName, new KnownActionError(VALIDATION_INVALIDSCHED, chPropValue));
                        return false;
                    }
                } else {
                    schedule = Schedule.readSchedule( chPropValue );
                    // Scheduler return "NEVER" for invalid schedule string
                    if( TUNER_PROP_VALUE_NEVER.equalsIgnoreCase( schedule.toString()) ){
                        errors.add( chPropName, new KnownActionError( VALIDATION_INVALIDSCHED, chPropValue ) );
                        return false;
                    }
                 }
            } catch (NoSuchElementException exp) {
                errors.add(chPropName, new KnownActionError(VALIDATION_INVALIDSCHED, chPropValue));
                return false;
            }
        }
        return true;
    }
    /**
     * <p>
     * Checks if a fields value is within a range (min &amp; max specified  in the vars attribute).
     * </p>
     *
     * @param bean The bean validation is being performed on.
     * @param va The <code>ValidatorAction</code> that is currently being performed.
     * @param field The <code>Field</code> object associated with the current field  being validated.
     * @param errors The <code>ActionErrors</code> object to add errors to if any  validation errors occur.
     * @param request Current request object.
     * @param application The application's <code>ServletContext</code>.
     *
     * @return REMIND
     */
    public static boolean validateRange(Object             bean,
                                        ValidatorAction    va,
                                        Field              field,
                                        ActionErrors       errors,
                                        HttpServletRequest request,
                                        ServletContext     application) {
        String value = GUIUtils.getValueAsString((IMapProperty)bean, field.getProperty());
        String sMin = field.getVarValue("min");
        String sMax = field.getVarValue("max");
        String name = field.getProperty();

        if (!GenericValidator.isBlankOrNull(value)) {
            try {
                int iValue = Integer.parseInt(value);
                int min = Integer.parseInt(sMin);
                int max = Integer.parseInt(sMax);

                if (!GenericValidator.isInRange(iValue, min, max) && !"cancel".equals(request.getParameter("action"))) {
                    errors.add(name, new KnownActionError(VALIDATION_FIELD_INTEGER_RANGE, getFieldLabel(name, application), sMin, sMax));

                    return false;
                }
            } catch (Exception e) {
                errors.add(field.getProperty(), new KnownActionError(VALIDATION_FIELD_INTEGER, getFieldLabel(name, application)));

                return false;
            }
        }

        return true;
    }

    static String getFieldLabel(String         field,
                                ServletContext application) {
        MessageResources resources = (MessageResources) application.getAttribute(IWebAppsConstants.INPUTERRORS);

        if (resources == null) {
            return "";
        } else {
            return WebAppUtils.getMessage(resources, Locale.getDefault(), field);
        }
    }

	/**
     * Validates input for Base DN
     *
     * @param bean Bean validation is being performed on.
     * @param va The current ValidatorAction being performed
     * @param field Field object being validated.
     * @param errors The errors objects to add an ActionError to if the validation fails
     * @param request Current request object.
     * @param application The application's ServletContext.
     *
     * @return boolean false if there is no error, true is there is.
     */
    public static boolean notNullBaseDn(java.lang.Object                      bean,
                                      ValidatorAction                       va,
                                      Field                                 field,
                                      org.apache.struts.action.ActionErrors errors,
                                      javax.servlet.http.HttpServletRequest request,
                                      javax.servlet.ServletContext          application) {

        String value = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());

        if (value == null) {
			errors.add(field.getProperty(), new KnownActionError(VALIDATION_INVALIDBASEDN, getFieldLabel(field.getProperty(), application)));
            return false;
        }

        return true;
    }

	/**
     * Validates input for Bind DN
     *
     * @param bean Bean validation is being performed on.
     * @param va The current ValidatorAction being performed
     * @param field Field object being validated.
     * @param errors The errors objects to add an ActionError to if the validation fails
     * @param request Current request object.
     * @param application The application's ServletContext.
     *
     * @return boolean false if there is no error, true is there is.
     */
    public static boolean notNullBindDn(java.lang.Object                      bean,
                                      ValidatorAction                       va,
                                      Field                                 field,
                                      org.apache.struts.action.ActionErrors errors,
                                      javax.servlet.http.HttpServletRequest request,
                                      javax.servlet.ServletContext          application) {

        String value = GUIUtils.getValueAsString((IMapProperty) bean, field.getProperty());

        if (value == null) {
			errors.add(field.getProperty(), new KnownActionError(VALIDATION_INVALIDBINDDN, getFieldLabel(field.getProperty(), application)));
            return false;
        }

        return true;
    }
}
