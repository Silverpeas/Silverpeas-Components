package com.silverpeas.scheduleevent.view;

public class AvailabilityUserFactory implements AvailabilityFactoryVO {

private final static AvailabilityUserFactory instance = new AvailabilityUserFactory();
  
  private AvailabilityUserFactory() {
  }
  
  @Override
  public AvailableEditableVO makeAvailablity(Availability availability) {
    switch (availability) {
      case AGREE:
        return AgreeEditableAvailability.getInstance();
      case AWAIT_ANSWER:
        // In edition mode it is synonym to disagree 
      case DISAGREE:
        return DisagreeEditableAvailability.getInstance();
      case DISABLE:
      default:
        return DisabledEditableAvailability.getInstance();
    }
  }

  public static AvailabilityUserFactory getInstance() {
    return instance;
  }

}
