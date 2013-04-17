package com.silverpeas.scheduleevent.view;

import com.silverpeas.scheduleevent.service.model.ScheduleEventBean;
import com.silverpeas.scheduleevent.service.model.beans.Contributor;
import com.silverpeas.scheduleevent.service.model.beans.ContributorComparator;
import com.silverpeas.scheduleevent.service.model.beans.DateOption;
import com.silverpeas.scheduleevent.service.model.beans.Response;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ScheduleEventVO implements ScheduleEventBean {

  private static final int MORNING_HOUR = 8;
  private static final int AFTERNOON_HOUR = 14;
  private ScheduleEventBean event;

  public ScheduleEventVO(ScheduleEventBean current) {
    this.event = current;
  }

  @Override
  public String getId() {
    return event.getId();
  }

  @Override
  public void setId(String id) {
    event.setId(id);
  }

  @Override
  public String getTitle() {
    return event.getTitle();
  }

  @Override
  public void setTitle(String title) {
    event.setTitle(title);
  }

  @Override
  public String getDescription() {
    return event.getDescription();
  }

  @Override
  public void setDescription(String description) {
    event.setDescription(description);
  }

  @Override
  public Date getCreationDate() {
    return event.getCreationDate();
  }

  @Override
  public void setCreationDate(Date creationDate) {
    event.setCreationDate(creationDate);
  }

  @Override
  public int getAuthor() {
    return event.getAuthor();
  }

  @Override
  public void setAuthor(int author) {
    event.setAuthor(author);
  }

  @Override
  public Set<DateOption> getDates() {
    return event.getDates();
  }

  @Override
  public Set<Contributor> getContributors() {
    //return event.getContributors();
    SortedSet<Contributor> contributors = new TreeSet<Contributor>(new ContributorComparator());
    for (Contributor contributor : event.getContributors()) {
      contributors.add(contributor);
    }
    return contributors;
  }

  public Integer getSubscribersCount() {
    return event.getContributors().size();
  }

  @Override
  public Set<Response> getResponses() {
    return event.getResponses();
  }

  @Override
  public int getStatus() {
    return event.getStatus();
  }

  @Override
  public void setStatus(int status) {
    event.setStatus(status);
  }

  public Set<OptionDateVO> getOptionalDateIndexes() throws Exception {
    SortedSet<OptionDateVO> dates = new TreeSet<OptionDateVO>();
//    Set<OptionDateVO> dates = new HashSet<OptionDateVO>();
    for (DateOption dateOption : event.getDates()) {
      addPartOfDayOrCreate(dates, dateOption);
    }
    return dates;
  }

  private void addPartOfDayOrCreate(Set<OptionDateVO> dates, DateOption dateOption)
      throws Exception {
    for (OptionDateVO date : dates) {
      if (date.isSameDateAs(dateOption)) {
        date.setPartOfDayFromHour(dateOption);
        return;
      }
    }
    addOptionDateVO(dates, dateOption);
  }

  private void addOptionDateVO(Set<OptionDateVO> dates, DateOption dateOption)
      throws Exception {
    OptionDateVO date = new OptionDateVO(dateOption.getDay());
    date.setPartOfDayFromHour(dateOption);
    dates.add(date);
  }

  public void setScheduleEventWith(Set<OptionDateVO> optionalDates) {
    Set<DateOption> dates = event.getDates();
    dates.clear();
    for (OptionDateVO date : optionalDates) {
      addMorningDateIfSelected(dates, date);
      addAfertnoonDateIfSelected(dates, date);
    }
  }

  private void addMorningDateIfSelected(Set<DateOption> dates, OptionDateVO date) {
    if (date.isMorning()) {
      dates.add(makeMorningDateOption(date));
    }
  }

  private static DateOption makeMorningDateOption(OptionDateVO date) {
    DateOption dateOption = new DateOption();
    dateOption.setDay(date.getDate());
    dateOption.setHour(MORNING_HOUR);
    return dateOption;
  }

  private void addAfertnoonDateIfSelected(Set<DateOption> dates, OptionDateVO date) {
    if (date.isAfternoon()) {
      dates.add(makeAfternoonDateOption(date));
    }
  }

  private static DateOption makeAfternoonDateOption(OptionDateVO date) {
    DateOption dateOption = new DateOption();
    dateOption.setDay(date.getDate());
    dateOption.setHour(AFTERNOON_HOUR);
    return dateOption;
  }

  public void deleteDate(String dateIndexFormat) throws Exception {
    OptionDateVO searchedDate = findDate(dateIndexFormat);
    deleteCorrespondingDateOptions(searchedDate);
  }

  private OptionDateVO findDate(String dateIndexFormat) throws Exception {
    for (OptionDateVO date : getOptionalDateIndexes()) {
      if (date.getIndexFormat().equals(dateIndexFormat)) {
        return date;
      }
    }
    return null;
  }

  private void deleteCorrespondingDateOptions(OptionDateVO searchedDate) {
    if (searchedDate != null) {
      Set<DateOption> dates = getCorrespondingDateOptionsTo(searchedDate);
      getDates().removeAll(dates);
    }
  }

  private Set<DateOption> getCorrespondingDateOptionsTo(OptionDateVO searchedDate) {
    Set<DateOption> result = new HashSet<DateOption>();
    for (DateOption date : getDates()) {
      if (areCorrespondingDates(searchedDate, date)) {
        result.add(date);
      }
    }
    return result;
  }

  private boolean areCorrespondingDates(OptionDateVO searchedDate, DateOption date) {
    return searchedDate.isSameDateAs(date);
  }
}
