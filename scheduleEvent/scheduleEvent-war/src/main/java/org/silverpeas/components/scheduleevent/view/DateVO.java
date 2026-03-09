package org.silverpeas.components.scheduleevent.view;

import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.kernel.SilverpeasException;

import java.util.Date;
import java.util.List;

public interface DateVO {

  boolean hasSameDateAs(DateOption date);

  Date getDate();

  List<TimeVO> getTimes();

  Integer getTimesNumber();

  void addTime(DateOption date) throws SilverpeasException;
}
