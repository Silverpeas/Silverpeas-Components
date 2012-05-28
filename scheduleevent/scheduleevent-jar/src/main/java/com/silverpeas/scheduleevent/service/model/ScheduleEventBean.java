package com.silverpeas.scheduleevent.service.model;

import java.util.Date;
import java.util.Set;
import java.util.SortedSet;

import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.DateOption;
import com.silverpeas.scheduleevent.service.model.beans.Response;

public interface ScheduleEventBean {
  public String getId();

  public void setId(String id);

  public String getTitle();

  public void setTitle(String title);

  public String getDescription();

  public void setDescription(String description);

  public Date getCreationDate();

  public void setCreationDate(Date creationDate);

  public int getAuthor();

  public void setAuthor(int author);

  public SortedSet<DateOption> getDates();

  public void setDates(SortedSet<DateOption> dates);

  public Set<Contributor> getContributors();

  public void setContributors(Set<Contributor> contributors);

  public Set<Response> getResponses();

  public void setResponses(Set<Response> responses);

  public int getStatus();

  public void setStatus(int status);
}
