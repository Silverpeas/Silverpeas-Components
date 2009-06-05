package com.silverpeas.mailinglist.service.model.beans;

import java.util.List;

public class MailingListActivity {
  private List<Message> messages;

  private List<Activity> activities;

  public MailingListActivity(List<Message> messages, List<Activity> activities) {
    this.messages = messages;
    this.activities = activities;
  }

  public List<Message> getMessages() {
    return messages;
  }

  public List<Activity> getActivities() {
    return activities;
  }

}
