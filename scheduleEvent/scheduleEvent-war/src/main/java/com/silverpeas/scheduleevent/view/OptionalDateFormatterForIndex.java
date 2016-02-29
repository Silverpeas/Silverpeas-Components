package com.silverpeas.scheduleevent.view;

import java.text.SimpleDateFormat;
import java.util.Date;


class OptionalDateFormatterForIndex {
  private static final String INDEX_COMPATIBLE_FORMAT = "ddMMyy";
  private static final SimpleDateFormat formatter = new SimpleDateFormat(INDEX_COMPATIBLE_FORMAT);

  public static String format(Date date) {
    return formatter.format(date);
  }
}