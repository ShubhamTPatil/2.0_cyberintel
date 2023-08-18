package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

import com.marimba.apps.subscriptionmanager.SubscriptionMain;
import com.marimba.apps.subscriptionmanager.webapp.util.defensight.CVEDataInsertQueryRunner;

public class CVEDataInsertionUtil {

  public void insertBulkData(SubscriptionMain main, String jsonFilePath) {
    new CVEDataInsertQueryRunner(main, jsonFilePath);
  }
}
