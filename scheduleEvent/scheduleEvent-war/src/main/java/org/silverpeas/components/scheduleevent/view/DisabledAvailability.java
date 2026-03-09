package org.silverpeas.components.scheduleevent.view;

public class DisabledAvailability implements AvailableVO {
  private static final String HTML_CLASS_ATTRIBUTE = "displayUserName";
  private static final DisabledAvailability instance = new DisabledAvailability();

  private DisabledAvailability() {
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

  public static DisabledAvailability getInstance() {
    return instance;
  }
}
