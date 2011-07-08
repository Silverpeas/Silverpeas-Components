package com.silverpeas.scheduleevent.view;

public abstract class AbstractAvailabilityVisitor implements AvailabilityVisitor {

  @Override
  public void visit(DisabledAvailability availability) {
  }

  @Override
  public void visit(AgreeAvailability availability) {
  }

  @Override
  public void visit(DisagreeAvailability availability) {
  }

  @Override
  public void visit(AwaitAnswerAvailability availability) {
  }
}
