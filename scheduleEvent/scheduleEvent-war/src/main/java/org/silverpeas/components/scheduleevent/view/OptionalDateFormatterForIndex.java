package org.silverpeas.components.scheduleevent.view;

import org.apache.commons.lang3.time.FastDateFormat;

import java.util.Date;


class OptionalDateFormatterForIndex {
  private static final String INDEX_COMPATIBLE_FORMAT = "ddMMyy";
  private static final FastDateFormat FORMATTER =
      FastDateFormat.getInstance(INDEX_COMPATIBLE_FORMAT);

  private OptionalDateFormatterForIndex() {

  }

  public static String format(Date date) {
    return FORMATTER.format(date);
  }
}