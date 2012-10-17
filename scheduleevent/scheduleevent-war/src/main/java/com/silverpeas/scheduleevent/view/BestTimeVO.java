package com.silverpeas.scheduleevent.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BestTimeVO {
  private List<TimeVO> times = new ArrayList<TimeVO>();
  private AvailabilityVisitorPresenceCounter bestPresence =
      new AvailabilityVisitorPresenceCounter();

  public BestTimeVO(List<TimeVO> proposedTimes) {
    for (TimeVO time : proposedTimes) {
      AvailabilityVisitorPresenceCounter presence = time.getPresentsCount();
      if (hasMoreParticipationFor(presence)) {
        replacedBestTimeBy(time, presence);
      } else if (hasSameParticipationFor(presence)) {
        add(time);
      }
    }
  }

  private boolean hasMoreParticipationFor(AvailabilityVisitorPresenceCounter presence) {
    return presence.compareTo(bestPresence) > 0;
  }

  private void replacedBestTimeBy(TimeVO time, AvailabilityVisitorPresenceCounter presence) {
    times.clear();
    add(time);
    bestPresence = presence;
  }

  private boolean hasSameParticipationFor(AvailabilityVisitorPresenceCounter presence) {
    return bestPresence.compareTo(presence) == 0;
  }

  private void add(TimeVO time) {
    times.add(time);
  }

  public boolean isBestDateExists() {
    return bestPresence.count() > 0;
  }

  public String getMultilangLabel() {
    return "scheduleevent.form.bestdates";
  }
  
  public int getPresentCount() {
    return bestPresence.count();
  }

  public List<TimeVO> getTimes() {
    return Collections.unmodifiableList(times);
  }
  
  public int getDatesNumber() {
    return getTimes().size();
  }
}
