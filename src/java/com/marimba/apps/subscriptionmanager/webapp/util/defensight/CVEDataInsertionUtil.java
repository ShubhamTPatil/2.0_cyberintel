package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.util.defensight.CVEDataInsertQueryRunner;
import com.marimba.intf.util.IConfig;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class CVEDataInsertionUtil {


  LinkedHashMap<String, String> errors = new LinkedHashMap<>();

  public boolean insertBulkData(SubscriptionMain main, String jsonFilePath, IConfig tunerConfig) {
    try {
      new CVEDataInsertQueryRunner(main, jsonFilePath, tunerConfig, errors);

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
