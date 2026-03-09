package org.silverpeas.components.scheduleevent.view;

public class AvailabilityContributorFactory implements AvailabilityFactoryVO {
  private static final AvailabilityContributorFactory instance = new AvailabilityContributorFactory();

  private AvailabilityContributorFactory() {
  }

  @Override
  public AvailableVO makeAvailability(Availability availability) {
    switch (availability) {
      case AGREE:
        return AgreeAvailability.getInstance();
      case DISAGREE:
        return DisagreeAvailability.getInstance();
      case AWAIT_ANSWER:
        return AwaitAnswerAvailability.getInstance();
      case DISABLE:
      default:
        return DisabledAvailability.getInstance();
    }
  }

  public static AvailabilityContributorFactory getInstance() {
    return instance;
  }

}
