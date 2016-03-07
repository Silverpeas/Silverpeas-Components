package org.silverpeas.components.scheduleevent.service.model;

import org.silverpeas.components.scheduleevent.service.model.beans.Contributor;
import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.components.scheduleevent.service.model.beans.Response;
import org.silverpeas.components.scheduleevent.service.model.beans.Contributor;
import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.components.scheduleevent.service.model.beans.Response;

import java.util.Date;
import java.util.Set;

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

  public Set<DateOption> getDates();

  public Set<Contributor> getContributors();

  public Set<Response> getResponses();

  public int getStatus();

  public void setStatus(int status);
}
