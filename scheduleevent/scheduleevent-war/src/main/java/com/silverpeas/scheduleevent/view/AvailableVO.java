package com.silverpeas.scheduleevent.view;

public interface AvailableVO {
  String getMarkLabel();
  String getHtmlClassAttribute();
  void accept(AvailabilityVisitor visitor);
}
