package com.silverpeas.scheduleevent.view;

public class DisagreeAvailability implements AvailableVO {
  private final static String HMTL_CLASS_ATTRIBUTE = "questionResults-Non";
  private final static DisagreeAvailability instance = new DisagreeAvailability();

  private DisagreeAvailability() {
  }

  @Override
  public String getMarkLabel() {
    return "&nbsp;";
  }

  @Override
  public String getHtmlClassAttribute() {
    return HMTL_CLASS_ATTRIBUTE;
  }

  @Override
  public void accept(AvailabilityVisitor visitor) {
    visitor.visit(this);
  }

  public static DisagreeAvailability getInstance() {
    return instance;
  }

}
