package org.silverpeas.components.scheduleevent.view;

import org.silverpeas.components.scheduleevent.service.model.ScheduleEventBean;
import org.silverpeas.components.scheduleevent.service.model.beans.Contributor;
import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.components.scheduleevent.service.model.beans.Response;
import org.silverpeas.components.scheduleevent.service.model.beans.ScheduleEvent;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.kernel.SilverpeasException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ScheduleEventDetailVO {
  private static final String USER_CONTRIBUTOR_HTML_CLASS_ATTRIBUTE = "userVote";

  int answers;
  private final List<DateVO> dates;
  private String id;
  private String title;
  private String description;
  private boolean closed;
  private boolean allowedToChange;
  private ContributorVO currentUser;
  private final List<ContributorVO> otherSubscribers;
  private final List<ContributorVO> contributors;
  private BestTimeVO selectionTime;

  public ScheduleEventDetailVO(AbstractComponentSessionController sessionController,
      ScheduleEventBean currentEvent) throws SilverpeasException {
    dates = new ArrayList<>();
    otherSubscribers = new ArrayList<>();
    contributors = new ArrayList<>();
    answers = 0;

    setId(currentEvent);
    setTitle(currentEvent);
    setDescription(currentEvent);
    setStatus(currentEvent);
    setModificationStatus(sessionController, currentEvent);
    setDates(currentEvent);
    setContributors(sessionController, currentEvent);
    setEachContributorAvailabilities(currentEvent, getContributors());
    setBestSelectionTime(getTimes());
  }

  private void setId(ScheduleEventBean currentEvent) {
    id = currentEvent.getId();
  }

  private void setTitle(ScheduleEventBean currentEvent) {
    title = currentEvent.getTitle();
  }

  private void setDescription(ScheduleEventBean currentEvent) {
    description = currentEvent.getDescription();
  }

  private void setStatus(ScheduleEventBean currentEvent) {
    closed = currentEvent.getStatus() == 0;
  }

  private void setModificationStatus(AbstractComponentSessionController sessionController,
      ScheduleEventBean currentEvent) {
    try {
      allowedToChange = currentEvent.getAuthor() == Integer.parseInt(sessionController.getUserId());
    } catch (Exception e) {
      allowedToChange = false;
    }
  }

  private void setDates(ScheduleEventBean currentEvent) throws SilverpeasException {
    for (DateOption date : currentEvent.getDates()) {
      updateOrAddDate(date);
    }
  }

  private void updateOrAddDate(DateOption date) throws SilverpeasException {
    DateVO dateVO = find(date);
    if (dateVO != null) {
      dateVO.addTime(date);
    } else {
      addDateFrom(date);
    }
  }

  private void addDateFrom(DateOption date) throws SilverpeasException {
    DateVO dateVO = createdDateFrom(date);
    dateVO.addTime(date);
    dates.add(dateVO);
  }

  private DateVO find(DateOption searchedDate) {
    for (DateVO date : dates) {
      if (date.hasSameDateAs(searchedDate)) {
        return date;
      }
    }
    return null;
  }

  private DateVO createdDateFrom(DateOption searchedDate) {
    return new HalfDayDateVO(searchedDate.getDay());
  }

  private void setContributors(AbstractComponentSessionController sessionController,
      ScheduleEventBean currentEvent) {
    for (Contributor contributor : currentEvent.getContributors()) {
      makeAndSetContributor(sessionController, contributor);
    }
  }

  private void makeAndSetContributor(AbstractComponentSessionController sessionController,
      Contributor contributor) {
    ContributorVO contributorVO = makeAndSetContributorByRole(sessionController, contributor);
    contributors.add(contributorVO);
  }

  private ContributorVO makeAndSetContributorByRole(
      AbstractComponentSessionController sessionController, Contributor contributor) {
    ContributorVO contributorVO;
    String name = getContributorDisplayedName(sessionController, contributor);
    if (isCurrentUser(sessionController.getUserId(), contributor)) {
      contributorVO = makeCurrentUser(name, contributor);
      setCurrentUser(contributorVO);
    } else {
      contributorVO = makeContributor(name, contributor);
      otherSubscribers.add(contributorVO);
    }
    return contributorVO;
  }

  private String getContributorDisplayedName(AbstractComponentSessionController sessionController,
      Contributor contributor) {
    String userId = String.valueOf(contributor.getUserId());
    UserDetail userDetail = sessionController.getUserDetail(userId);
    return userDetail == null ? userId : userDetail.getDisplayedName();
  }

  private boolean isCurrentUser(String userId, Contributor contributor) {
    try {
      return contributor.getUserId() == Integer.parseInt(userId);
    } catch (Exception e) {
      return false;
    }
  }

  private ContributorVO makeCurrentUser(String name, Contributor contributor) {
    if (isClosed()) {
      return makeContributor(name, contributor);
    } else {
      ContributorImplVO contributorVO = new ContributorImplVO(name, contributor, AvailabilityUserFactory.getInstance());
      contributorVO.setHtmlClassAttribute(USER_CONTRIBUTOR_HTML_CLASS_ATTRIBUTE);
      return contributorVO;
    }
  }

  private ContributorVO makeContributor(String name, Contributor contributor) {
    return new ContributorImplVO(name, contributor, AvailabilityContributorFactory.getInstance());
  }

  private void setEachContributorAvailabilities(ScheduleEventBean currentEvent,
      List<ContributorVO> contributors) {
    for (ContributorVO contributor : contributors) {
      if (contributor.hasAnswered()) {
        incrementAnswers();
        addContributorAvailabilities(contributor,
            getContributorResponses(currentEvent, contributor));
      } else {
        addWaitingContributorAvailabilities(contributor);
      }
    }
  }

  private Set<Response> getContributorResponses(ScheduleEventBean currentEvent,
      ContributorVO contributor) {
    return contributor.match(currentEvent.getResponses());
  }

  private void incrementAnswers() {
    ++answers;
  }

  private void addContributorAvailabilities(ContributorVO contributor, Set<Response> responses) {
    for (TimeVO time : getTimes()) {
      Set<Response> availabilities = time.match(responses);
      addAvailabilities(contributor, time, availabilities);
    }
  }

  private List<TimeVO> getTimes() {
    List<TimeVO> times = new ArrayList<>();
    for (DateVO date : getDates()) {
      times.addAll(date.getTimes());
    }
    return times;
  }

  public List<DateVO> getDates() {
    return dates;
  }

  private void addAvailabilities(ContributorVO contributor, TimeVO time, Set<Response> responses) {
    AvailableVO availability =
        responses.isEmpty() ? contributor.makeAvailability(AvailabilityFactoryVO.Availability.DISAGREE) : contributor
            .makeAvailability(AvailabilityFactoryVO.Availability.AGREE);
    time.addAvailability(contributor, availability);
  }

  private void addWaitingContributorAvailabilities(ContributorVO contributor) {
    for (TimeVO time : getTimes()) {
      AvailableVO availability = contributor.makeAvailability(AvailabilityFactoryVO.Availability.AWAIT_ANSWER);
      time.addAvailability(contributor, availability);
    }
  }

  private void setBestSelectionTime(List<TimeVO> times) {
    selectionTime = new BestTimeVO(times);
  }

  public BestTimeVO getBestTimes() {
    return selectionTime;
  }

  public String getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public ContributorVO getCurrentUser() {
    return currentUser;
  }

  private void setCurrentUser(ContributorVO currentUser) {
    this.currentUser = currentUser;
  }

  public Integer getSubscribersCount() {
    return getContributors().size();
  }

  private List<ContributorVO> getContributors() {
    return contributors;
  }

  public String getPresentParticipationPercentageRate() {
    return ScheduleEventRessources.formatInPercent(getPresentParticipationRate());
  }

  private double getPresentParticipationRate() {
    return getSubscribersRate(selectionTime.getPresentCount());
  }

  public String getParticipationPercentageRate() {
    return ScheduleEventRessources.formatInPercent(getParticipationRate());
  }

  private double getParticipationRate() {
    return getSubscribersRate(answers);
  }

  private double getSubscribersRate(int answers) {
    int subscribers = getSubscribersCount();
    return subscribers > 0 ? 1.0 * answers / subscribers : 0.0;
  }

  public boolean isCurrentUserDefinedAsSubscriber() {
    return currentUser != null;
  }

  public List<ContributorVO> getOtherSubscribers() {
    return Collections.unmodifiableList(otherSubscribers);
  }

  public boolean isClosed() {
    return closed;
  }

  public boolean isAllowedToChange() {
    return allowedToChange;
  }

  public String getResourceType() {
    return ScheduleEvent.getResourceType();
  }
}
