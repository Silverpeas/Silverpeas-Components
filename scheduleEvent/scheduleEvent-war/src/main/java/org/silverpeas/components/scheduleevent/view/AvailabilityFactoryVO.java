package org.silverpeas.components.scheduleevent.view;

public interface AvailabilityFactoryVO {
  enum Availability {
    DISABLE, AGREE, DISAGREE, AWAIT_ANSWER
  }

  AvailableVO makeAvailablity(Availability availability);
}
