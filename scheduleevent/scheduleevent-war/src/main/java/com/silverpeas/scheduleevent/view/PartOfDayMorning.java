package com.silverpeas.scheduleevent.view;

public class PartOfDayMorning implements PartOfDay {

  private static PartOfDayMorning instance = new PartOfDayMorning();

  private PartOfDayMorning() {}

  public static PartOfDayMorning getInstance() {
    return instance;
  }

  @Override
  public String getMultilangLabel() {
    return "scheduleevent.form.hour.columnam";
  }

  @Override
  public String getPrefixId() {
    return "AM";
  }

}
