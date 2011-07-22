package com.silverpeas.scheduleevent.view;

public class DisabledEditableAvailability implements
    AvailableEditableVO {
  private final static DisabledEditableAvailability instance = new DisabledEditableAvailability();
  
  private DisabledEditableAvailability() {
  }

  @Override
  public String getMarkLabel() {
    return parentInstance().getMarkLabel();
  }

  @Override
  public String getHtmlClassAttribute() {
    return parentInstance().getHtmlClassAttribute();
  }

  @Override
  public void accept(AvailabilityVisitor visitor) {
    parentInstance().accept(visitor);
  }
  
  private DisabledAvailability parentInstance() {
    return DisabledAvailability.getInstance();
  }
  
  public static DisabledEditableAvailability getInstance() {
    return instance;
  }

  @Override
  public boolean isEditable() {
    return false;
  }
}
