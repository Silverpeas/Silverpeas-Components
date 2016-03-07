package org.silverpeas.components.scheduleevent.view;

import java.util.Date;
import java.util.List;

import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;

public interface DateVO {

  boolean hasSameDateAs(DateOption date);

  Date getDate();

  List<TimeVO> getTimes();

  Integer getTimesNumber();

  void addTime(DateOption date) throws Exception;
}
