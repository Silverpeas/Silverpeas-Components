package com.silverpeas.scheduleevent.view;

import java.util.Date;
import java.util.List;

import com.silverpeas.scheduleevent.service.model.beans.DateOption;

public interface DateVO {

  boolean hasSameDateAs(DateOption date);

  Date getDate();

  List<TimeVO> getTimes();
  
  Integer getTimesNumber();

  void addTime(DateOption date) throws Exception;
}
