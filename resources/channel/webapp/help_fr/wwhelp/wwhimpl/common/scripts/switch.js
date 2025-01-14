// Copyright (c) 2000-2003 Quadralay Corporation.  All rights reserved.
//

function  WWHSwitch_Object()
{
  this.mParameters     = "";
  this.mImplementation = "javascript";
  this.mSettings       = new WWHCommonSettings_Object();
  this.mMessages       = new WWHCommonMessages_Object();

  this.fExec               = WWHSwitch_Exec;
  this.fParseURLParameters = WWHSwitch_ParseURLParameters;
  this.fProcessURL         = WWHSwitch_ProcessURL;
  this.fSwitch             = WWHSwitch_Switch;

  // Load up messages
  //
  this.mMessages.fSetByLocale(Remedy_Locale);
  //this.mMessages.fSetByLocale(WWHFrame.WWHBrowser.mLocale);
}

function  WWHSwitch_Exec(bParamNormalizeURL,
                         ParamURL)
{
  var  TargetURL   = ParamURL;
  var  bUseJava    = false;
  var  FrameSetURL = "";


  // Determine cookie path
  //
  WWHFrame.WWHBrowser.fSetCookiePath(WWHStringUtilities_GetBaseURL(ParamURL));

  // Normalize URL if necessary
  //
  if (bParamNormalizeURL)
  {
    TargetURL = WWHFrame.WWHBrowser.fNormalizeURL(ParamURL);
  }

  // Process parameters
  //
  this.fProcessURL(TargetURL);

  // Pick frameset to use
  //
  if (this.mImplementation == "single")
  {
    FrameSetURL = "../../common/html/wwhelp.htm";
  }
  else if (this.mImplementation == "java")
  {
    FrameSetURL = "../../java/html/wwhelp.htm";
  }
  else
  {
    FrameSetURL = "../../js/html/wwhelp.htm";
  }

  // Switch to frameset
  //
  this.fSwitch(FrameSetURL);
}

function  WWHSwitch_ParseURLParameters(ParamURL)
{
  var  Result = new Array(null, null, null, "");
  var  Parts;
  var  MaxIndex;
  var  Index;
  var  SingleMarker     = "single=";
  var  ForceJSMarker    = "forcejs=";
  var  AccessibleMarker = "accessible=";
  var  Value;


  // Parse URL parameters
  //
  if (ParamURL.indexOf("?") != -1)
  {
    // Get parameters
    //
    Parts = ParamURL.split("?");
    Parts[0] = Parts[1];
    Parts.length = 1;
    if (Parts[0].indexOf("&") != -1)
    {
      Parts = Parts[0].split("&");
    }

    // Process parameters, preserve non-switch related options
    //
    for (MaxIndex = Parts.length, Index = 0 ; Index < MaxIndex ; Index++)
    {
      if (Parts[Index].indexOf(SingleMarker) == 0)
      {
        Value = Parts[Index].substring(SingleMarker.length, Parts[Index].length);

        if (Value == "true")
        {
          Result[0] = true;
        }
      }
      else if (Parts[Index].indexOf(ForceJSMarker) == 0)
      {
        Value = Parts[Index].substring(ForceJSMarker.length, Parts[Index].length);

        if (Value == "true")
        {
          Result[1] = true;
        }
      }
      else if (Parts[Index].indexOf(AccessibleMarker) == 0)
      {
        Value = Parts[Index].substring(AccessibleMarker.length, Parts[Index].length);

        if ((Value == "true") ||
            (Value == "false") ||
            (Value == "ask"))
        {
          Result[2] = Value;
        }
      }
      else
      {
        if (Result[3].length > 0)
        {
          Result[3] += "&";
        }
        Result[3] += Parts[Index];
      }
    }
  }

  return Result;
}

function  WWHSwitch_ProcessURL(ParamURL)
{
  var  VarURLParameters;
  var  VarURLParam_Single;
  var  VarURLParam_ForceJS;
  var  VarURLParam_Accessible;
  var  VarAccessibleCookie     = "WWH" + this.mSettings.mCookiesID + "_Acs";
  var  VarImplementationCookie = "WWH" + this.mSettings.mCookiesID + "_Impl";
  var  VarAccessible;
  var  VarImplementation;
  var  VarImplementationFromCookie;


  // Parse URL parameters
  //
  VarURLParameters = this.fParseURLParameters(ParamURL);
  VarURLParam_Single     = VarURLParameters[0];
  VarURLParam_ForceJS    = VarURLParameters[1];
  VarURLParam_Accessible = VarURLParameters[2];
  this.mParameters       = VarURLParameters[3];

  // Check for accessibility support
  //
  VarAccessible = "false";
  if ((this.mSettings.mAccessible == "true") ||
      (VarURLParam_Accessible == "true"))
  {
    VarAccessible = "true";
  }
  else if ((this.mSettings.mAccessible == "ask") ||
           (VarURLParam_Accessible == "ask"))
  {
    // Attempt to retrive setting from cookies, if allowed
    //
    VarAccessible = "ask";
    if (this.mSettings.mbCookies)
    {
      VarAccessible = WWHFrame.WWHBrowser.fGetCookie(VarAccessibleCookie);
      if (VarAccessible == null)
      {
        VarAccessible = "ask";
      }
    }

    // Ask if cookie not set or disallowed
    //
    if (VarAccessible == "ask")
    {
      if (confirm(this.mMessages.mUseAccessibleHTML))
      {
        VarAccessible = "true";
      }
      else
      {
        VarAccessible = "false";
      }
    }
  }

  // Determine implementation
  //
  VarImplementation = "java";

  // Attempt to read implementation from cookie
  //
  if (this.mSettings.mbCookies)
  {
    VarImplementationFromCookie = WWHFrame.WWHBrowser.fGetCookie(VarImplementationCookie);
    if ((VarImplementationFromCookie == "java") ||
        (VarImplementationFromCookie == "javascript"))
    {
      VarImplementation = VarImplementationFromCookie;
    }
  }

  // Reset implementation based on URL parameters
  //
  if ((VarURLParam_Single != null) &&
      (VarURLParam_Single == true))
  {
    VarImplementation = "single";
  }
  else if ((VarURLParam_ForceJS != null) &&
           (VarURLParam_ForceJS == true))
  {
    VarImplementation = "javascript";
  }
  else if (VarImplementation == "java")
  {
    if (this.mSettings.mbForceJavaScript)
    {
      VarImplementation = "javascript";
    }
    else if (VarAccessible == "true")
    {
      VarImplementation = "javascript";
    }
    else if (( ! WWHFrame.WWHBrowser.mbJavaCapable) ||
             ( ! WWHFrame.WWHBrowser.mbJavaEnabled))
    {
      VarImplementation = "javascript";
    }
  }

  // Store options in cookies, if possible
  //
  if (this.mSettings.mbCookies)
  {
    // Set accessibility option
    //
    if (((this.mSettings.mAccessible == "ask") &&
         (VarURLParam_Accessible == null)) ||
        (VarURLParam_Accessible == "ask"))
    {
      WWHFrame.WWHBrowser.fSetCookie(VarAccessibleCookie, VarAccessible, this.mSettings.mCookiesDaysToExpire);
    }

    // Set implementation option
    //
    if ((this.mSettings.mbForceJavaScript != "true") &&
        (VarURLParam_ForceJS != "true") &&
        ((VarURLParam_Accessible == null) ||
         (VarURLParam_Accessible != "true")))
    {
      WWHFrame.WWHBrowser.fSetCookie(VarImplementationCookie, VarImplementation, this.mSettings.mCookiesDaysToExpire);
    }
  }

  // Set implementation
  //
  this.mImplementation = VarImplementation;

  // Finalize URL parameters
  //
  if (VarAccessible == "true")
  {
    this.mParameters += "&accessible=true";
  }
  if (this.mParameters.length > 0)
  {
    this.mParameters = "?" + this.mParameters;
  }
}

function  WWHSwitch_Switch(ParamFrameSetURL)
{
  var  SwitchURL;


  // Add parameters to redirect
  //
  SwitchURL = ParamFrameSetURL + this.mParameters;

  // Switch to desired frameset
  // Delay required since this page is processing the action
  //
  WWHFrame.WWHBrowser.fReplaceLocation("WWHFrame", SwitchURL);
}
