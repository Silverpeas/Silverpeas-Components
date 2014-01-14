package com.silverpeas.scheduleevent.view;

public class DisabledAvailability implements AvailableVO {
  private final static String HMTL_CLASS_ATTRIBUTE = "displayUserName";
  private final static DisabledAvailability instance = new DisabledAvailability();
  
  private DisabledAvailability() {
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
  
  public static DisabledAvailability getInstance() {
    return instance;
  }
}
