package com.silverpeas.scheduleevent.view;

public class AgreeEditableAvailability implements
    AvailableEditableVO {
  private final static AgreeEditableAvailability instance = new AgreeEditableAvailability();
  
  private AgreeEditableAvailability() {
  }

  @Override
  public String getMarkLabel() {
    return "checked=\"checked\"";
  }

  @Override
  public String getHtmlClassAttribute() {
    return parentInstance().getHtmlClassAttribute();
  }

  @Override
  public void accept(AvailabilityVisitor visitor) {
    parentInstance().accept(visitor);
  }
  
  private AgreeAvailability parentInstance() {
    return AgreeAvailability.getInstance();
  }
  
  public static AgreeEditableAvailability getInstance() {
    return instance;
  }

  @Override
  public boolean isEditable() {
    return true;
  }
}