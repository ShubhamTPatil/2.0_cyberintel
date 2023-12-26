package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.util.defensight.CVEDataInsertQueryRunner;
import com.marimba.intf.util.IConfig;
import com.marimba.tools.config.ConfigProps;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class CVEDataInsertionUtil {


  LinkedHashMap<String, String> errors = new LinkedHashMap<>();

  public boolean insertBulkData(SubscriptionMain main, String jsonFilePath, ConfigProps definitionConfig) {
    try {
      new CVEDataInsertQueryRunner(main, jsonFilePath, definitionConfig, errors);

      if (errors.isEmpty()) {
        return true;
      } else {
        return false;
      }
    } catch (Exception exception) {
      error(exception.getMessage());
      return false;
    }
  }

  public void error(String message) {
    System.err.println("ERROR : CVEDataInsertionUtil : " + message);
  }

}
