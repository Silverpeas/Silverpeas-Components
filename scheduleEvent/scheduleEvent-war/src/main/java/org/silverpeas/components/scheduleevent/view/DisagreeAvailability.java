package org.silverpeas.components.scheduleevent.view;

public class DisagreeAvailability implements AvailableVO {
  private static final String HTML_CLASS_ATTRIBUTE = "questionResults-Non";
  private static final DisagreeAvailability instance = new DisagreeAvailability();

  private DisagreeAvailability() {
  }

  @Override
  public String getMarkLabel() {
    return "&nbsp;";
  }

  @Override
  public String getHtmlClassAttribute() {
    return HTML_CLASS_ATTRIBUTE;
  }

  @Override
  public void accept(AvailabilityVisitor visitor) {
    visitor.visit(this);
  }

  public static DisagreeAvailability getInstance() {
    return instance;
  }

}
