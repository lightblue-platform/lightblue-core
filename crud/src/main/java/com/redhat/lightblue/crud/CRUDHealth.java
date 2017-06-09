package com.redhat.lightblue.crud;

public class CRUDHealth {
  private final boolean isHealthy;
  private final String details;

  public CRUDHealth(boolean isHealthy, String details) {
    this.isHealthy = isHealthy;
    this.details = details;
  }

  public boolean isHealthy() {
    return isHealthy;
  }

  public String details() {
    return details;
  }
}
