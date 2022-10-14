package com.marimba.apps.subscriptionmanager.cli.commands.intf;


public interface ISubscribe {
	int VALIDATESTATE          = 0;
	int VALIDATESECSTATE       = 1;
	int VALIDATEORDER          = 2;
	int VALIDATECONTENTTYPE    = 3;
	int VALIDATEEXPTBLACKOUT   = 4;
	
	String SCHEDBLACKOUT 			= "-schedblackout";
	String SCHEDPRIMARY 			= "-schedprimary";
    String SCHEDSECONDARY 			= "-schedsecondary";
    String SCHEDUPDATE 				= "-schedupdate";
    String SCHEDVERREPAIR 			= "-schedverifyrepair";
    String SCHEDPOSTPONE 			= "-schedpostpone";

    String ADVERTISE 				= "advertise";
    String STAGE 					= "stage";
    String INSTALL 					= "install";
    String ASSIGN 					= "assign";
    String INSTALL_START 			= "install-start";
    String INSTALL_START_PERSIST 	= "install-start-persist";
    String INSTALL_PERSIST 			= "install-persist";
    String UNINSTALL 				= "uninstall";
    
    String AVAILABLE 				= "available";
    String SUBSCRIBE_NOINSTALL 		= "subscribe_noinstall";
    String SUBSCRIBE 				= "subscribe";
    String SUBSCRIBE_START 			=  "subscribe_start";
    String START_PERSIST 			= "start_persist";
    String SUBSCRIBE_PERSIST 		= "subscribe_persist";
    String DELETE 					= "delete";
    String EXCLUDE 					= "exclude";
    String PRIMARY 					= "primary";
}
