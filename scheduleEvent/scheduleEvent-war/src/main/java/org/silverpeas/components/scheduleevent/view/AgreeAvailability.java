package org.silverpeas.components.scheduleevent.view;

public class AgreeAvailability implements AvailableVO {
  private static final String HTML_CLASS_ATTRIBUTE = "questionResults-Oui";
  private static final AgreeAvailability instance = new AgreeAvailability();

  private AgreeAvailability() {
  }

  @Override
  public String getMarkLabel() {
    return "x";
  }

  @Override
  public String getHtmlClassAttribute() {
    return HTML_CLASS_ATTRIBUTE;
  }

  @Override
  public void accept(AvailabilityVisitor visitor) {
    visitor.visit(this);
  }

  public static AgreeAvailability getInstance() {
    return instance;
  }
}
