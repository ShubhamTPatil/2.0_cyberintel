package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.util.defensight.CVEDataInsertQueryRunner;

public class CVEDataInsertionUtil {

  public boolean insertBulkData(SubscriptionMain main, String jsonFilePath) {
    try {
      new CVEDataInsertQueryRunner(main, jsonFilePath);
      return true;
    } catch (Exception exception) {
      error(exception.getMessage());
      return false;
    }
  }

  public void error(String message) {
    System.err.println("ERROR : CVEDataInsertionUtil : " + message);
  }

}
