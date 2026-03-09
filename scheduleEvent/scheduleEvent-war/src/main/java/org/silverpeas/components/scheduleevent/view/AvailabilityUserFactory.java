package org.silverpeas.components.scheduleevent.view;

public class AvailabilityUserFactory implements AvailabilityFactoryVO {

  private static final AvailabilityUserFactory instance = new AvailabilityUserFactory();

  private AvailabilityUserFactory() {
  }

  @Override
  public AvailableEditableVO makeAvailability(Availability availability) {
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
