package com.stratelia.webactiv.almanach.model;

import java.util.Date;

import com.stratelia.webactiv.persistence.SilverpeasBean;

public class PeriodicityException extends SilverpeasBean {

  private int periodicityId;
  private Date beginDateException;
  private Date endDateException;

  public PeriodicityException() {
    super();
  }

  public Date getBeginDateException() {
    return beginDateException;
  }

  public void setBeginDateException(Date beginDateException) {
    this.beginDateException = beginDateException;
  }

  public Date getEndDateException() {
    return endDateException;
  }

  public void setEndDateException(Date endDateException) {
    this.endDateException = endDateException;
  }

  public int getPeriodicityId() {
    return periodicityId;
  }

  public void setPeriodicityId(int periodicityId) {
    this.periodicityId = periodicityId;
  }

  public String _getTableName() {
    return "SC_Almanach_PeriodicityExcept";
  }
}