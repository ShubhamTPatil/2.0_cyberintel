// Copyright (c) 2001-2003 Quadralay Corporation.  All rights reserved.
//

function  WWHJavaScriptSettings_Object()
{
  this.mHoverText = new WWHJavaScriptSettings_HoverText_Object();

  this.mTabs   = new WWHJavaScriptSettings_Tabs_Object();
  this.mTOC    = new WWHJavaScriptSettings_TOC_Object();
  this.mIndex  = new WWHJavaScriptSettings_Index_Object();
  this.mSearch = new WWHJavaScriptSettings_Search_Object();
}

function  WWHJavaScriptSettings_HoverText_Object()
{
  this.mbEnabled = true;

  this.mFontStyle = "font-family: Arial, Verdana, Helvetica, sans-serif ; font-size: 8pt";

  this.mWidth = 150;

  this.mForegroundColor = "#000000";
  this.mBackgroundColor = "#FFFFCC";
  this.mBorderColor     = "#999999";
}

function  WWHJavaScriptSettings_Tabs_Object()
{
  this.mFontStyle = "font-family: Arial, Verdana, Arial, Helvetica, sans-serif ; font-size: 10pt";

  this.mSelectedTabColor       = "#006BA5";
  this.mSelectedTabBorderColor = "#FFFFFF";
  this.mSelectedTabTextColor   = "#FFFFFF";

  this.mDefaultTabColor       = "#DEDEDE";
  this.mDefaultTabBorderColor = "#666666";
  this.mDefaultTabTextColor   = "#000000";
}

function  WWHJavaScriptSettings_TOC_Object()
{
  this.mbShow = true;

  this.mFontStyle = "font-family: Arial, Verdana, Helvetica, sans-serif ; font-size: 12px";

  this.mHighlightColor = "#DEDEDE";
  this.mEnabledColor   = "#315585";
  this.mDisabledColor  = "black";

  this.mIndent = 17;
}

function  WWHJavaScriptSettings_Index_Object()
{
  this.mbShow = true;

  this.mFontStyle = "font-family: Arial, Verdana, Helvetica, sans-serif ; font-size: 12px";

  this.mHighlightColor = "#DEDEDE";
  this.mEnabledColor   = "#315585";
  this.mDisabledColor  = "black";

  this.mIndent = 17;

  this.mNavigationFontStyle      = "font-family: Verdana, Arial, Helvetica, sans-serif ; font-size: 7pt ; font-weight: bold";
  this.mNavigationCurrentColor   = "black";
  this.mNavigationHighlightColor = "#DEDEDE";
  this.mNavigationEnabledColor   = "#315585";
  this.mNavigationDisabledColor  = "#999999";
}

function  WWHJavaScriptSettings_Index_DisplayOptions(ParamIndexOptions)
{
  ParamIndexOptions.fSetThreshold(500);
  ParamIndexOptions.fSetSeperator(" - ");
}

function  WWHJavaScriptSettings_Search_Object()
{
  this.mbShow = true;

  this.mFontStyle = "font-family: Arial, Verdana, Helvetica, sans-serif ; font-size: 12px";

  this.mHighlightColor = "#DEDEDE";
  this.mEnabledColor   = "#315585";
  this.mDisabledColor  = "black";

  this.mIndent = 17;

  this.mbResultsByBook = true;
  this.mbShowRank      = true;
}
