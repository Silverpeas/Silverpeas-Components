package org.silverpeas.components.scheduleevent.view;

public interface AvailabilityVisitor {
  void visit(DisabledAvailability availability);

  void visit(AgreeAvailability availability);

  void visit(DisagreeAvailability availability);

  void visit(AwaitAnswerAvailability availability);
}
