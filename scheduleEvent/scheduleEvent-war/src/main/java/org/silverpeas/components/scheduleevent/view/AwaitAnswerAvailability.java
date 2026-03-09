package org.silverpeas.components.scheduleevent.view;

public class AwaitAnswerAvailability implements AvailableVO {
  private static final String HTML_CLASS_ATTRIBUTE = "questionResults-NC";
  private static final AwaitAnswerAvailability instance = new AwaitAnswerAvailability();

  private AwaitAnswerAvailability() {
  }

  @Override
  public String getMarkLabel() {
    return "-";
  }

  @Override
  public String getHtmlClassAttribute() {
    return HTML_CLASS_ATTRIBUTE;
  }

  @Override
  public void accept(AvailabilityVisitor visitor) {
    visitor.visit(this);
  }

  public static AwaitAnswerAvailability getInstance() {
    return instance;
  }
}
