package com.silverpeas.scheduleevent.view;

public class AgreeAvailability implements AvailableVO {
  private final static String HMTL_CLASS_ATTRIBUTE = "questionResults-Oui";
  private final static AgreeAvailability instance = new AgreeAvailability();

  private AgreeAvailability() {
  }
  
  @Override
  public String getMarkLabel() {
    return "x";
  }

  @Override
  public String getHtmlClassAttribute() {
    return HMTL_CLASS_ATTRIBUTE;
  }
  
  @Override
  public void accept(AvailabilityVisitor visitor) {
    visitor.visit(this);
  }
  
  public static AgreeAvailability getInstance() {
    return instance;
  }
}
