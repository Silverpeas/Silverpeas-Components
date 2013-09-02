package com.silverpeas.scheduleevent.view;

public class DisagreeEditableAvailability implements
    AvailableEditableVO {
  private final static DisagreeEditableAvailability instance = new DisagreeEditableAvailability();
  
  private DisagreeEditableAvailability() {
  }

  @Override
  public String getMarkLabel() {
    return "";
  }

  @Override
  public String getHtmlClassAttribute() {
    return parentInstance().getHtmlClassAttribute();
  }

  @Override
  public void accept(AvailabilityVisitor visitor) {
    parentInstance().accept(visitor);
  }
  
  private DisagreeAvailability parentInstance() {
    return DisagreeAvailability.getInstance();
  }
  
  public static DisagreeEditableAvailability getInstance() {
    return instance;
  }

  @Override
  public boolean isEditable() {
    return true;
  }
}