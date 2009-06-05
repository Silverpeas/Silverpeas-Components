package com.silverpeas.mailinglist.service.model.dao;

import java.util.List;

import com.silverpeas.mailinglist.service.model.beans.MailingList;

public interface MailingListDao {
  
  public String createMailingList(MailingList mailingList);
  
  public void updateMailingList(MailingList mailingList);
  
  public void deleteMailingList(MailingList mailingList);
  
  public MailingList findById(String id);
  
  public MailingList findByComponentId(String componentId);
  
  public List<MailingList> listMailingLists();
}
