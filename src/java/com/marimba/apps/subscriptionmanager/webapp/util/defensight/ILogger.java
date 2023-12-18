package com.marimba.apps.subscriptionmanager.webapp.util.defensight;

public interface ILogger {

  void info(String message, int counters);

  void info(String message);

  void error(String message);

  void handleError(Exception e);

}
