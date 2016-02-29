package com.silverpeas.scheduleevent.view;

public class AvailabilityVisitorPresenceCounter extends AbstractAvailabilityVisitor implements
    AvailabilityVisitor {

  private static final int LOWER_THAN_CRRENT = -1;
  private static final int EQUAL_TO_CURRENT = 0;
  private static final int UPPER_THAN_CURRENT = 1;
  private int present = 0;
  private int answers = 0;

  @Override
  public void visit(AgreeAvailability availability) {
    super.visit(availability);
    ++present;
    ++answers;
  }

  @Override
  public void visit(DisagreeAvailability availability) {
    super.visit(availability);
    ++answers;
  }

  public int count() {
    return present;
  }

  public int answers() {
    return answers;
  }

  public int compareTo(AvailabilityVisitorPresenceCounter presenceCounter) {
    assert count() <= answers() : "Subscribers can't be greater than answers";
    assert presenceCounter.count() <= presenceCounter.answers() : "Subscribers can't be greater than answers";
    if (presenceCounter.answers() == 0) {
      return answers() == 0 ? EQUAL_TO_CURRENT : UPPER_THAN_CURRENT;
    } else if (answers() == 0) {
      return LOWER_THAN_CRRENT;
    } else {
    // Use cross rate to make comparison in integer and avoid divide by zero
    return count() * presenceCounter.answers() - presenceCounter.count() * answers();
    }
  }
}
