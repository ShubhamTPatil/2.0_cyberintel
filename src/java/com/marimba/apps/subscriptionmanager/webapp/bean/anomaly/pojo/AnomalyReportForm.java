package com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.pojo;

import java.util.ArrayList;
import java.util.List;
import com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.User;
import com.marimba.apps.subscriptionmanager.webapp.forms.AbstractForm;

public class AnomalyReportForm extends
    AbstractForm {

  String topLevelStats = "";

  public String getTopLevelStats() {
    return topLevelStats;
  }

  public void setTopLevelStats(String topLevelStats) {
    this.topLevelStats = topLevelStats;
  }


}
