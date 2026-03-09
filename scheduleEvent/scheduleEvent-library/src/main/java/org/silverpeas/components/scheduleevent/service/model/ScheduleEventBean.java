package org.silverpeas.components.scheduleevent.service.model;

import org.silverpeas.components.scheduleevent.service.model.beans.Contributor;
import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.components.scheduleevent.service.model.beans.Response;

import java.util.Date;
import java.util.Set;

public interface ScheduleEventBean {

  String getId();

  void setId(String id);

  String getTitle();

  void setTitle(String title);

  String getDescription();

  void setDescription(String description);

  Date getCreationDate();

  void setCreationDate(Date creationDate);

  int getAuthor();

  void setAuthor(int author);

  Set<DateOption> getDates();

  Set<Contributor> getContributors();

  Set<Response> getResponses();

  int getStatus();

  void setStatus(int status);
}
