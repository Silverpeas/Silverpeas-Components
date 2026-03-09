package org.silverpeas.components.scheduleevent.view;

import static org.silverpeas.components.scheduleevent.view.ScheduleEventRessources.formatInPercent;

public class PresentAnswer implements AnswerVO {
  private static final String HTML_CLASS_ATTRIBUTE = "participation";

  private final int presents;
  private final int contributors;

  public PresentAnswer(AvailabilityVisitorPresenceCounter presenceCounter) {
    this.presents = presenceCounter.count();
    this.contributors = presenceCounter.answers();
  }

  @Override
  public String getPositiveAnswerPercentage() {
    return getPresencePercentage();
  }

  private String getPresencePercentage() {
    return formatInPercent(getPresenceRate());
  }

  private double getPresenceRate() {
    return contributors > 0 ? 1.0 * presents / contributors : 0.0;
  }

  @Override
  public String getHtmlClassAttribute() {
    return HTML_CLASS_ATTRIBUTE;
  }

}
