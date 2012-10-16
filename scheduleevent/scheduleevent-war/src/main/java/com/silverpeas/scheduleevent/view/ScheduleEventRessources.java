package com.silverpeas.scheduleevent.view;

import java.text.NumberFormat;

public class ScheduleEventRessources {

  private final static int PERCENT_FORMAT_DIGITS_PRECISION = 1;
  
  public final static String formatInPercent(double rate) {
    NumberFormat percent = NumberFormat.getPercentInstance();
    percent.setMaximumFractionDigits(PERCENT_FORMAT_DIGITS_PRECISION);
    return percent.format(rate);
  }
}
