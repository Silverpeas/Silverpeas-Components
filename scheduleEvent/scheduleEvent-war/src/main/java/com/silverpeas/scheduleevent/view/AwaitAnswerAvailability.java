package com.silverpeas.scheduleevent.view;

public class AwaitAnswerAvailability implements AvailableVO {
  private final static String HMTL_CLASS_ATTRIBUTE = "questionResults-NC";
  private final static AwaitAnswerAvailability instance = new AwaitAnswerAvailability();

  private AwaitAnswerAvailability() {
  }

  @Override
  public String getMarkLabel() {
    return "-";
  }

  @Override
  public String getHtmlClassAttribute() {
    return HMTL_CLASS_ATTRIBUTE;
  }

  @Override
  public void accept(AvailabilityVisitor visitor) {
    visitor.visit(this);
  }

  public static AwaitAnswerAvailability getInstance() {
    return instance;
  }
}
