// Copyright 1996-2015, BMC Software Inc. All Rights Reserved.
// Confidential and Proprietary Information of BMC Software Inc.
// Protected by or for use under one or more of the following patents: U.S. Patent Nos. 5,919,247, 6,272,536, 6,367,075,
// 6,381,631, and 6,430,608. Other Patents Pending.

// $File$

package com.marimba.apps.subscriptionmanager.webapp.forms;

public class DevicePolicyProfileForm extends AbstractForm {
	String create = "true";
	String name = "Android Configuration";
	String description = "Test Android Configuraiton";
	String selectedOS;
	String selectedType;
	
	// Android Configuration Policy options
	String enableSDCard;
	String isEnabledSDCard;
	String enableUSB;
	String isEnabledUSB;
	String enableUSBTethering;
	String isEnabledUSBTethering;
	String enableWiFi;
	String isEnabledWiFi;
	String enableWiFiTethering;
	String isEnabledWiFiTethering;
	String enableBluetooth;
	String isEnabledBluetooth;
	String enableBluetoothTethering;
	String isEnabledBluetoothTethering;
	String enableCamera;
	String isEnabledCamera;
	String enableGPSLocation;
	String isEnabledGPSLocation;
	String enableSendSMS;
	String isEnabledSendSMS;
	String enableBrowser;
	String isEnabledBrowser;
	String enableVPN;
	String isEnabledVPN;
	String enablePOPMail;
	String isEnabledPOPMail;
	String allowMobileData;
	String isAllowedMobileData;
	String allowScreenCapture;
	String isAllowedScreenCapture;
	String allowFactoryReset;
	String isAllowedFactoryReset;
	String allowDeviceDeactivation;
	String isAllowedDeviceDeactivation;
	String enableRoamingData;
	String isEnabledRoamingData;
	String allowAutoSync;
	String isAllowedAutoSync;
	
	// iOS Configuration Policy Options
	String allowInstallApp;
	String isAllowedInstallApp;
	String allowAirDrop;
	String isAllowedAirDrop;
	String allowRemoveApps;
	String isAllowedRemoveApps;
	String allowCelluarData;
	String isAllowedCelluarData;
	String allowCamera;
	String isAllowedCamera;
	String allowFaceTime;
	String isAllowedFaceTime;
	String allowIOSScreenCapture;
	String isAllowedIOSScreenCapture;
	String allowIOSAtoSync;
	String isAllowedIOSAutoSync;
	String allowSiri;
	String isAllowedSiri;
	String allowSiriDeviceLock;
	String isAllowedSiriDeviceLock;
	String allowSiriProFilter;
	String isAllowedSiriProFilter;
	String showUserSiri;
	String isShowedUserSiri;
	String allowIMsg;
	String isAllowedIMsg;
	String allowVoiceDial;
	String isAllowedVoiceDial;
	String allowBkStore;
	String isAllowedBkStore;
	String allowPassBk;
	
	String isAllowedPassBk;
	String allowAppPur;
	String isAllowedAppPur;
	String forceStrPWD;
	String isForceStrPWD;
	String allowMultiGame;
	String isAllowedMultiGame;
	String allowGameCtr;
	String isAllowedGameCtr;
	String allowConfigInstall;
	
	String isAllowedConfigInstall;
	String allowAcctChange;
	String isAllowedAcctChange;
	String allowFindFrd;
	String isAllowedFindFrd;
	String allowCfgHost;
	String isAllowedCfgHost;
	String allowChangeManApps;
	String isAllowedChangeManApps;
	String allowChangeUnManApps;
	String isAllowedChangeUnManApps;
	String allowOTAUpdate;
	String isAllowedOTAUpdate;
	String showCtlCenter;
	String isShowedCtlCenter;
	String showNotificationCenter;
	
	String isShowedNotificationCenter;
	String showTodayView;
	String isShowedTodayView;
	String enableRoaming;
	String isEnabledRoaming;
	String enableDataRoaming;
	String isEnabledDataRoaming;
	String enableVoiceRoaming;
	String isEnabledVoiceRoaming;
	String enablePersonalHotSpot;
	String isEnabledPersonalHotSpot;
	String enableIOSWiFi;
	String isEnabledIOSWiFi;
	String enableVPNAccess;
	String isEnabledVPNAccess;
	
	public String getCreate() {
		return create;
	}
	public void setCreate(String create) {
		this.create = create;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	public String getSelectedOS() {
		return selectedOS;
	}
	public void setSelectedOS(String selectedOS) {
		this.selectedOS = selectedOS;
	}
	public String getSelectedType() {
		return selectedType;
	}
	public void setSelectedType(String selectedType) {
		this.selectedType = selectedType;
	}
	public String getEnableSDCard() {
		return enableSDCard;
	}
	public void setEnableSDCard(String enableSDCard) {
		this.enableSDCard = enableSDCard;
	}
	public String getIsEnabledSDCard() {
		return isEnabledSDCard;
	}
	public void setIsEnabledSDCard(String isEnabledSDCard) {
		this.isEnabledSDCard = isEnabledSDCard;
	}
	public String getEnableUSB() {
		return enableUSB;
	}
	public void setEnableUSB(String enableUSB) {
		this.enableUSB = enableUSB;
	}
	public String getIsEnabledUSB() {
		return isEnabledUSB;
	}
	public void setIsEnabledUSB(String isEnabledUSB) {
		this.isEnabledUSB = isEnabledUSB;
	}
	public String getEnableUSBTethering() {
		return enableUSBTethering;
	}
	public void setEnableUSBTethering(String enableUSBTethering) {
		this.enableUSBTethering = enableUSBTethering;
	}
	public String getIsEnabledUSBTethering() {
		return isEnabledUSBTethering;
	}
	public void setIsEnabledUSBTethering(String isEnabledUSBTethering) {
		this.isEnabledUSBTethering = isEnabledUSBTethering;
	}
	public String getEnableWiFi() {
		return enableWiFi;
	}
	public void setEnableWiFi(String enableWiFi) {
		this.enableWiFi = enableWiFi;
	}
	public String getIsEnabledWiFi() {
		return isEnabledWiFi;
	}
	public void setIsEnabledWiFi(String isEnabledWiFi) {
		this.isEnabledWiFi = isEnabledWiFi;
	}
	public String getEnableWiFiTethering() {
		return enableWiFiTethering;
	}
	public void setEnableWiFiTethering(String enableWiFiTethering) {
		this.enableWiFiTethering = enableWiFiTethering;
	}
	public String getIsEnabledWiFiTethering() {
		return isEnabledWiFiTethering;
	}
	public void setIsEnabledWiFiTethering(String isEnabledWiFiTethering) {
		this.isEnabledWiFiTethering = isEnabledWiFiTethering;
	}
	public String getEnableBluetooth() {
		return enableBluetooth;
	}
	public void setEnableBluetooth(String enableBluetooth) {
		this.enableBluetooth = enableBluetooth;
	}
	public String getIsEnabledBluetooth() {
		return isEnabledBluetooth;
	}
	public void setIsEnabledBluetooth(String isEnabledBluetooth) {
		this.isEnabledBluetooth = isEnabledBluetooth;
	}
	public String getEnableBluetoothTethering() {
		return enableBluetoothTethering;
	}
	public void setEnableBluetoothTethering(String enableBluetoothTethering) {
		this.enableBluetoothTethering = enableBluetoothTethering;
	}
	public String getIsEnabledBluetoothTethering() {
		return isEnabledBluetoothTethering;
	}
	public void setIsEnabledBluetoothTethering(String isEnabledBluetoothTethering) {
		this.isEnabledBluetoothTethering = isEnabledBluetoothTethering;
	}
	public String getEnableCamera() {
		return enableCamera;
	}
	public void setEnableCamera(String enableCamera) {
		this.enableCamera = enableCamera;
	}
	public String getIsEnabledCamera() {
		return isEnabledCamera;
	}
	public void setIsEnabledCamera(String isEnabledCamera) {
		this.isEnabledCamera = isEnabledCamera;
	}
	public String getEnableGPSLocation() {
		return enableGPSLocation;
	}
	public void setEnableGPSLocation(String enableGPSLocation) {
		this.enableGPSLocation = enableGPSLocation;
	}
	public String getIsEnabledGPSLocation() {
		return isEnabledGPSLocation;
	}
	public void setIsEnabledGPSLocation(String isEnabledGPSLocation) {
		this.isEnabledGPSLocation = isEnabledGPSLocation;
	}
	public String getEnableSendSMS() {
		return enableSendSMS;
	}
	public void setEnableSendSMS(String enableSendSMS) {
		this.enableSendSMS = enableSendSMS;
	}
	public String getIsEnabledSendSMS() {
		return isEnabledSendSMS;
	}
	public void setIsEnabledSendSMS(String isEnabledSendSMS) {
		this.isEnabledSendSMS = isEnabledSendSMS;
	}
	public String getEnableBrowser() {
		return enableBrowser;
	}
	public void setEnableBrowser(String enableBrowser) {
		this.enableBrowser = enableBrowser;
	}
	public String getIsEnabledBrowser() {
		return isEnabledBrowser;
	}
	public void setIsEnabledBrowser(String isEnabledBrowser) {
		this.isEnabledBrowser = isEnabledBrowser;
	}
	public String getEnableVPN() {
		return enableVPN;
	}
	public void setEnableVPN(String enableVPN) {
		this.enableVPN = enableVPN;
	}
	public String getIsEnabledVPN() {
		return isEnabledVPN;
	}
	public void setIsEnabledVPN(String isEnabledVPN) {
		this.isEnabledVPN = isEnabledVPN;
	}
	public String getEnablePOPMail() {
		return enablePOPMail;
	}
	public void setEnablePOPMail(String enablePOPMail) {
		this.enablePOPMail = enablePOPMail;
	}
	public String getIsEnabledPOPMail() {
		return isEnabledPOPMail;
	}
	public void setIsEnabledPOPMail(String isEnabledPOPMail) {
		this.isEnabledPOPMail = isEnabledPOPMail;
	}
	public String getAllowMobileData() {
		return allowMobileData;
	}
	public void setAllowMobileData(String allowMobileData) {
		this.allowMobileData = allowMobileData;
	}
	public String getIsAllowedMobileData() {
		return isAllowedMobileData;
	}
	public void setIsAllowedMobileData(String isAllowedMobileData) {
		this.isAllowedMobileData = isAllowedMobileData;
	}
	public String getAllowScreenCapture() {
		return allowScreenCapture;
	}
	public void setAllowScreenCapture(String allowScreenCapture) {
		this.allowScreenCapture = allowScreenCapture;
	}
	public String getIsAllowedScreenCapture() {
		return isAllowedScreenCapture;
	}
	public void setIsAllowedScreenCapture(String isAllowedScreenCapture) {
		this.isAllowedScreenCapture = isAllowedScreenCapture;
	}
	public String getAllowFactoryReset() {
		return allowFactoryReset;
	}
	public void setAllowFactoryReset(String allowFactoryReset) {
		this.allowFactoryReset = allowFactoryReset;
	}
	public String getIsAllowedFactoryReset() {
		return isAllowedFactoryReset;
	}
	public void setIsAllowedFactoryReset(String isAllowedFactoryReset) {
		this.isAllowedFactoryReset = isAllowedFactoryReset;
	}
	public String getAllowDeviceDeactivation() {
		return allowDeviceDeactivation;
	}
	public void setAllowDeviceDeactivation(String allowDeviceDeactivation) {
		this.allowDeviceDeactivation = allowDeviceDeactivation;
	}
	public String getIsAllowedDeviceDeactivation() {
		return isAllowedDeviceDeactivation;
	}
	public void setIsAllowedDeviceDeactivation(String isAllowedDeviceDeactivation) {
		this.isAllowedDeviceDeactivation = isAllowedDeviceDeactivation;
	}
	public String getEnableRoamingData() {
		return enableRoamingData;
	}
	public void setEnableRoamingData(String enableRoamingData) {
		this.enableRoamingData = enableRoamingData;
	}
	public String getIsEnabledRoamingData() {
		return isEnabledRoamingData;
	}
	public void setIsEnabledRoamingData(String isEnabledRoamingData) {
		this.isEnabledRoamingData = isEnabledRoamingData;
	}
	public String getAllowAutoSync() {
		return allowAutoSync;
	}
	public void setAllowAutoSync(String allowAutoSync) {
		this.allowAutoSync = allowAutoSync;
	}
	public String getIsAllowedAutoSync() {
		return isAllowedAutoSync;
	}
	public void setIsAllowedAutoSync(String isAllowedAutoSync) {
		this.isAllowedAutoSync = isAllowedAutoSync;
	}
	public String getAllowInstallApp() {
		return allowInstallApp;
	}
	public void setAllowInstallApp(String allowInstallApp) {
		this.allowInstallApp = allowInstallApp;
	}
	public String getIsAllowedInstallApp() {
		return isAllowedInstallApp;
	}
	public void setIsAllowedInstallApp(String isAllowedInstallApp) {
		this.isAllowedInstallApp = isAllowedInstallApp;
	}
	public String getAllowAirDrop() {
		return allowAirDrop;
	}
	public void setAllowAirDrop(String allowAirDrop) {
		this.allowAirDrop = allowAirDrop;
	}
	public String getIsAllowedAirDrop() {
		return isAllowedAirDrop;
	}
	public void setIsAllowedAirDrop(String isAllowedAirDrop) {
		this.isAllowedAirDrop = isAllowedAirDrop;
	}
	public String getAllowRemoveApps() {
		return allowRemoveApps;
	}
	public void setAllowRemoveApps(String allowRemoveApps) {
		this.allowRemoveApps = allowRemoveApps;
	}
	public String getIsAllowedRemoveApps() {
		return isAllowedRemoveApps;
	}
	public void setIsAllowedRemoveApps(String isAllowedRemoveApps) {
		this.isAllowedRemoveApps = isAllowedRemoveApps;
	}
	public String getAllowCelluarData() {
		return allowCelluarData;
	}
	public void setAllowCelluarData(String allowCelluarData) {
		this.allowCelluarData = allowCelluarData;
	}
	public String getIsAllowedCelluarData() {
		return isAllowedCelluarData;
	}
	public void setIsAllowedCelluarData(String isAllowedCelluarData) {
		this.isAllowedCelluarData = isAllowedCelluarData;
	}
	public String getAllowCamera() {
		return allowCamera;
	}
	public void setAllowCamera(String allowCamera) {
		this.allowCamera = allowCamera;
	}
	public String getIsAllowedCamera() {
		return isAllowedCamera;
	}
	public void setIsAllowedCamera(String isAllowedCamera) {
		this.isAllowedCamera = isAllowedCamera;
	}
	public String getAllowFaceTime() {
		return allowFaceTime;
	}
	public void setAllowFaceTime(String allowFaceTime) {
		this.allowFaceTime = allowFaceTime;
	}
	public String getIsAllowedFaceTime() {
		return isAllowedFaceTime;
	}
	public void setIsAllowedFaceTime(String isAllowedFaceTime) {
		this.isAllowedFaceTime = isAllowedFaceTime;
	}
	public String getAllowIOSScreenCapture() {
		return allowIOSScreenCapture;
	}
	public void setAllowIOSScreenCapture(String allowIOSScreenCapture) {
		this.allowIOSScreenCapture = allowIOSScreenCapture;
	}
	public String getIsAllowedIOSScreenCapture() {
		return isAllowedIOSScreenCapture;
	}
	public void setIsAllowedIOSScreenCapture(String isAllowedIOSScreenCapture) {
		this.isAllowedIOSScreenCapture = isAllowedIOSScreenCapture;
	}
	public String getAllowIOSAtoSync() {
		return allowIOSAtoSync;
	}
	public void setAllowIOSAtoSync(String allowIOSAtoSync) {
		this.allowIOSAtoSync = allowIOSAtoSync;
	}
	public String getIsAllowedIOSAutoSync() {
		return isAllowedIOSAutoSync;
	}
	public void setIsAllowedIOSAutoSync(String isAllowedIOSAutoSync) {
		this.isAllowedIOSAutoSync = isAllowedIOSAutoSync;
	}
	public String getAllowSiri() {
		return allowSiri;
	}
	public void setAllowSiri(String allowSiri) {
		this.allowSiri = allowSiri;
	}
	public String getIsAllowedSiri() {
		return isAllowedSiri;
	}
	public void setIsAllowedSiri(String isAllowedSiri) {
		this.isAllowedSiri = isAllowedSiri;
	}
	public String getAllowSiriDeviceLock() {
		return allowSiriDeviceLock;
	}
	public void setAllowSiriDeviceLock(String allowSiriDeviceLock) {
		this.allowSiriDeviceLock = allowSiriDeviceLock;
	}
	public String getIsAllowedSiriDeviceLock() {
		return isAllowedSiriDeviceLock;
	}
	public void setIsAllowedSiriDeviceLock(String isAllowedSiriDeviceLock) {
		this.isAllowedSiriDeviceLock = isAllowedSiriDeviceLock;
	}
	public String getAllowSiriProFilter() {
		return allowSiriProFilter;
	}
	public void setAllowSiriProFilter(String allowSiriProFilter) {
		this.allowSiriProFilter = allowSiriProFilter;
	}
	public String getIsAllowedSiriProFilter() {
		return isAllowedSiriProFilter;
	}
	public void setIsAllowedSiriProFilter(String isAllowedSiriProFilter) {
		this.isAllowedSiriProFilter = isAllowedSiriProFilter;
	}
	public String getShowUserSiri() {
		return showUserSiri;
	}
	public void setShowUserSiri(String showUserSiri) {
		this.showUserSiri = showUserSiri;
	}
	public String getIsShowedUserSiri() {
		return isShowedUserSiri;
	}
	public void setIsShowedUserSiri(String isShowedUserSiri) {
		this.isShowedUserSiri = isShowedUserSiri;
	}
	public String getAllowIMsg() {
		return allowIMsg;
	}
	public void setAllowIMsg(String allowIMsg) {
		this.allowIMsg = allowIMsg;
	}
	public String getIsAllowedIMsg() {
		return isAllowedIMsg;
	}
	public void setIsAllowedIMsg(String isAllowedIMsg) {
		this.isAllowedIMsg = isAllowedIMsg;
	}
	public String getAllowVoiceDial() {
		return allowVoiceDial;
	}
	public void setAllowVoiceDial(String allowVoiceDial) {
		this.allowVoiceDial = allowVoiceDial;
	}
	public String getIsAllowedVoiceDial() {
		return isAllowedVoiceDial;
	}
	public void setIsAllowedVoiceDial(String isAllowedVoiceDial) {
		this.isAllowedVoiceDial = isAllowedVoiceDial;
	}
	public String getAllowBkStore() {
		return allowBkStore;
	}
	public void setAllowBkStore(String allowBkStore) {
		this.allowBkStore = allowBkStore;
	}
	public String getIsAllowedBkStore() {
		return isAllowedBkStore;
	}
	public void setIsAllowedBkStore(String isAllowedBkStore) {
		this.isAllowedBkStore = isAllowedBkStore;
	}
	public String getAllowPassBk() {
		return allowPassBk;
	}
	public void setAllowPassBk(String allowPassBk) {
		this.allowPassBk = allowPassBk;
	}
	public String getIsAllowedPassBk() {
		return isAllowedPassBk;
	}
	public void setIsAllowedPassBk(String isAllowedPassBk) {
		this.isAllowedPassBk = isAllowedPassBk;
	}
	public String getAllowAppPur() {
		return allowAppPur;
	}
	public void setAllowAppPur(String allowAppPur) {
		this.allowAppPur = allowAppPur;
	}
	public String getIsAllowedAppPur() {
		return isAllowedAppPur;
	}
	public void setIsAllowedAppPur(String isAllowedAppPur) {
		this.isAllowedAppPur = isAllowedAppPur;
	}
	public String getForceStrPWD() {
		return forceStrPWD;
	}
	public void setForceStrPWD(String forceStrPWD) {
		this.forceStrPWD = forceStrPWD;
	}
	public String getIsForceStrPWD() {
		return isForceStrPWD;
	}
	public void setIsForceStrPWD(String isForceStrPWD) {
		this.isForceStrPWD = isForceStrPWD;
	}
	public String getAllowMultiGame() {
		return allowMultiGame;
	}
	public void setAllowMultiGame(String allowMultiGame) {
		this.allowMultiGame = allowMultiGame;
	}
	public String getIsAllowedMultiGame() {
		return isAllowedMultiGame;
	}
	public void setIsAllowedMultiGame(String isAllowedMultiGame) {
		this.isAllowedMultiGame = isAllowedMultiGame;
	}
	public String getAllowGameCtr() {
		return allowGameCtr;
	}
	public void setAllowGameCtr(String allowGameCtr) {
		this.allowGameCtr = allowGameCtr;
	}
	public String getIsAllowedGameCtr() {
		return isAllowedGameCtr;
	}
	public void setIsAllowedGameCtr(String isAllowedGameCtr) {
		this.isAllowedGameCtr = isAllowedGameCtr;
	}
	public String getAllowConfigInstall() {
		return allowConfigInstall;
	}
	public void setAllowConfigInstall(String allowConfigInstall) {
		this.allowConfigInstall = allowConfigInstall;
	}
	public String getIsAllowedConfigInstall() {
		return isAllowedConfigInstall;
	}
	public void setIsAllowedConfigInstall(String isAllowedConfigInstall) {
		this.isAllowedConfigInstall = isAllowedConfigInstall;
	}
	public String getAllowAcctChange() {
		return allowAcctChange;
	}
	public void setAllowAcctChange(String allowAcctChange) {
		this.allowAcctChange = allowAcctChange;
	}
	public String getIsAllowedAcctChange() {
		return isAllowedAcctChange;
	}
	public void setIsAllowedAcctChange(String isAllowedAcctChange) {
		this.isAllowedAcctChange = isAllowedAcctChange;
	}
	public String getAllowFindFrd() {
		return allowFindFrd;
	}
	public void setAllowFindFrd(String allowFindFrd) {
		this.allowFindFrd = allowFindFrd;
	}
	public String getIsAllowedFindFrd() {
		return isAllowedFindFrd;
	}
	public void setIsAllowedFindFrd(String isAllowedFindFrd) {
		this.isAllowedFindFrd = isAllowedFindFrd;
	}
	public String getAllowCfgHost() {
		return allowCfgHost;
	}
	public void setAllowCfgHost(String allowCfgHost) {
		this.allowCfgHost = allowCfgHost;
	}
	public String getIsAllowedCfgHost() {
		return isAllowedCfgHost;
	}
	public void setIsAllowedCfgHost(String isAllowedCfgHost) {
		this.isAllowedCfgHost = isAllowedCfgHost;
	}
	public String getAllowChangeManApps() {
		return allowChangeManApps;
	}
	public void setAllowChangeManApps(String allowChangeManApps) {
		this.allowChangeManApps = allowChangeManApps;
	}
	public String getIsAllowedChangeManApps() {
		return isAllowedChangeManApps;
	}
	public void setIsAllowedChangeManApps(String isAllowedChangeManApps) {
		this.isAllowedChangeManApps = isAllowedChangeManApps;
	}
	public String getAllowChangeUnManApps() {
		return allowChangeUnManApps;
	}
	public void setAllowChangeUnManApps(String allowChangeUnManApps) {
		this.allowChangeUnManApps = allowChangeUnManApps;
	}
	public String getIsAllowedChangeUnManApps() {
		return isAllowedChangeUnManApps;
	}
	public void setIsAllowedChangeUnManApps(String isAllowedChangeUnManApps) {
		this.isAllowedChangeUnManApps = isAllowedChangeUnManApps;
	}
	public String getAllowOTAUpdate() {
		return allowOTAUpdate;
	}
	public void setAllowOTAUpdate(String allowOTAUpdate) {
		this.allowOTAUpdate = allowOTAUpdate;
	}
	public String getIsAllowedOTAUpdate() {
		return isAllowedOTAUpdate;
	}
	public void setIsAllowedOTAUpdate(String isAllowedOTAUpdate) {
		this.isAllowedOTAUpdate = isAllowedOTAUpdate;
	}
	public String getShowCtlCenter() {
		return showCtlCenter;
	}
	public void setShowCtlCenter(String showCtlCenter) {
		this.showCtlCenter = showCtlCenter;
	}
	public String getIsShowedCtlCenter() {
		return isShowedCtlCenter;
	}
	public void setIsShowedCtlCenter(String isShowedCtlCenter) {
		this.isShowedCtlCenter = isShowedCtlCenter;
	}
	public String getShowNotificationCenter() {
		return showNotificationCenter;
	}
	public void setShowNotificationCenter(String showNotificationCenter) {
		this.showNotificationCenter = showNotificationCenter;
	}
	public String getIsShowedNotificationCenter() {
		return isShowedNotificationCenter;
	}
	public void setIsShowedNotificationCenter(String isShowedNotificationCenter) {
		this.isShowedNotificationCenter = isShowedNotificationCenter;
	}
	public String getShowTodayView() {
		return showTodayView;
	}
	public void setShowTodayView(String showTodayView) {
		this.showTodayView = showTodayView;
	}
	public String getIsShowedTodayView() {
		return isShowedTodayView;
	}
	public void setIsShowedTodayView(String isShowedTodayView) {
		this.isShowedTodayView = isShowedTodayView;
	}
	public String getEnableRoaming() {
		return enableRoaming;
	}
	public void setEnableRoaming(String enableRoaming) {
		this.enableRoaming = enableRoaming;
	}
	public String getIsEnabledRoaming() {
		return isEnabledRoaming;
	}
	public void setIsEnabledRoaming(String isEnabledRoaming) {
		this.isEnabledRoaming = isEnabledRoaming;
	}
	public String getEnableDataRoaming() {
		return enableDataRoaming;
	}
	public void setEnableDataRoaming(String enableDataRoaming) {
		this.enableDataRoaming = enableDataRoaming;
	}
	public String getIsEnabledDataRoaming() {
		return isEnabledDataRoaming;
	}
	public void setIsEnabledDataRoaming(String isEnabledDataRoaming) {
		this.isEnabledDataRoaming = isEnabledDataRoaming;
	}
	public String getEnableVoiceRoaming() {
		return enableVoiceRoaming;
	}
	public void setEnableVoiceRoaming(String enableVoiceRoaming) {
		this.enableVoiceRoaming = enableVoiceRoaming;
	}
	public String getIsEnabledVoiceRoaming() {
		return isEnabledVoiceRoaming;
	}
	public void setIsEnabledVoiceRoaming(String isEnabledVoiceRoaming) {
		this.isEnabledVoiceRoaming = isEnabledVoiceRoaming;
	}
	public String getEnablePersonalHotSpot() {
		return enablePersonalHotSpot;
	}
	public void setEnablePersonalHotSpot(String enablePersonalHotSpot) {
		this.enablePersonalHotSpot = enablePersonalHotSpot;
	}
	public String getIsEnabledPersonalHotSpot() {
		return isEnabledPersonalHotSpot;
	}
	public void setIsEnabledPersonalHotSpot(String isEnabledPersonalHotSpot) {
		this.isEnabledPersonalHotSpot = isEnabledPersonalHotSpot;
	}
	public String getEnableIOSWiFi() {
		return enableIOSWiFi;
	}
	public void setEnableIOSWiFi(String enableIOSWiFi) {
		this.enableIOSWiFi = enableIOSWiFi;
	}
	public String getIsEnabledIOSWiFi() {
		return isEnabledIOSWiFi;
	}
	public void setIsEnabledIOSWiFi(String isEnabledIOSWiFi) {
		this.isEnabledIOSWiFi = isEnabledIOSWiFi;
	}
	public String getEnableVPNAccess() {
		return enableVPNAccess;
	}
	public void setEnableVPNAccess(String enableVPNAccess) {
		this.enableVPNAccess = enableVPNAccess;
	}
	public String getIsEnabledVPNAccess() {
		return isEnabledVPNAccess;
	}
	public void setIsEnabledVPNAccess(String isEnabledVPNAccess) {
		this.isEnabledVPNAccess = isEnabledVPNAccess;
	}
}
