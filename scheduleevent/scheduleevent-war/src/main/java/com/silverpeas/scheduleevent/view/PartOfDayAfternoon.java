package com.silverpeas.scheduleevent.view;

public class PartOfDayAfternoon implements PartOfDay {
  private static PartOfDayAfternoon instance = new PartOfDayAfternoon();

  private PartOfDayAfternoon() {}

  public static PartOfDayAfternoon getInstance() {
    return instance;
  }

  @Override
  public String getMultilangLabel() {
    return "scheduleevent.form.hour.columnpm";
  }

  @Override
  public String getPrefixId() {
    return "PM";
  }

}
