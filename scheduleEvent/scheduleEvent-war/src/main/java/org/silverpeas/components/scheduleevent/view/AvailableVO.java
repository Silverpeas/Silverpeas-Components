package org.silverpeas.components.scheduleevent.view;

public interface AvailableVO {
  String getMarkLabel();
  String getHtmlClassAttribute();
  void accept(AvailabilityVisitor visitor);
}
