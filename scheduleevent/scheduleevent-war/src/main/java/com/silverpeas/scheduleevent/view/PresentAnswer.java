package com.silverpeas.scheduleevent.view;

import static com.silverpeas.scheduleevent.view.ScheduleEventRessources.formatInPercent;

public class PresentAnswer implements AnswerVO {
  private final static String HMTL_CLASS_ATTRIBUTE = "participation";
  
  private int presents;
  private int contributors;

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
    return HMTL_CLASS_ATTRIBUTE;
  }

}
