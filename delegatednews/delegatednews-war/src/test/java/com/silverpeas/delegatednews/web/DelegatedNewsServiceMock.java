/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.delegatednews.web;

import java.util.Date;
import java.util.List;

import javax.inject.Named;

import com.silverpeas.delegatednews.model.DelegatedNews;
import com.silverpeas.delegatednews.service.DelegatedNewsService;

@Named
public class DelegatedNewsServiceMock implements DelegatedNewsService {

  private DelegatedNewsService delegatedNewsService;

  void setDelegatedNewsService(DelegatedNewsService delegatedNewsService) {
    this.delegatedNewsService = delegatedNewsService;
  }

  public DelegatedNewsService getDelegatedNewsService() {
    return this.delegatedNewsService;
  }

  @Override
  public void addDelegatedNews(int pubId, String instanceId, String contributorId,
      Date validationDate, Date beginDate, Date endDate) {
    this.delegatedNewsService.addDelegatedNews(pubId, instanceId, contributorId, validationDate, beginDate, endDate);
  }

  @Override
  public DelegatedNews getDelegatedNews(int pubId) {
    return this.delegatedNewsService.getDelegatedNews(pubId);
  }

  @Override
  public List<DelegatedNews> getAllDelegatedNews() {
    return this.delegatedNewsService.getAllDelegatedNews();
  }

  @Override
  public List<DelegatedNews> getAllValidDelegatedNews() {
    return this.delegatedNewsService.getAllValidDelegatedNews();
  }

  @Override
  public void validateDelegatedNews(int pubId, String validatorId) {
    this.delegatedNewsService.validateDelegatedNews(pubId, validatorId);
  }

  @Override
  public void refuseDelegatedNews(int pubId, String validatorId) {
    this.delegatedNewsService.refuseDelegatedNews(pubId, validatorId);
  }

  @Override
  public void updateDateDelegatedNews(int pubId, Date dateHourBegin, Date dateHourEnd) {
    this.delegatedNewsService.updateDateDelegatedNews(pubId, dateHourBegin, dateHourEnd);
    
  }

  @Override
  public void notifyDelegatedNewsToValidate(String pubId, String pubName, String senderId,
      String senderName, String delegatednewsInstanceId) {
    this.delegatedNewsService.notifyDelegatedNewsToValidate(pubId, pubName, senderId, senderName, delegatednewsInstanceId);
  }

  @Override
  public void updateDelegatedNews(int pubId, String instanceId, String status, String updaterId,
      String validatorId, Date validationDate, Date dateHourBegin, Date dateHourEnd) {
    this.delegatedNewsService.updateDelegatedNews(pubId, instanceId, status, updaterId, validatorId, validationDate, dateHourBegin, dateHourEnd);    
  }

  @Override
  public void deleteDelegatedNews(int pubId) {
    this.delegatedNewsService.deleteDelegatedNews(pubId);
  }

  @Override
  public void notifyDelegatedNewsValid(String pubId, String pubName, String senderId,
      String senderName, String contributorId, String delegatednewsInstanceId) {
      this.delegatedNewsService.notifyDelegatedNewsValid(pubId, pubName, senderId, senderName, contributorId, delegatednewsInstanceId); 
  }

  @Override
  public void notifyDelegatedNewsRefused(String pubId, String pubName, String refusalMotive,
      String senderId, String senderName, String contributorId, String delegatednewsInstanceId) {
      this.delegatedNewsService.notifyDelegatedNewsRefused(pubId, pubName, refusalMotive, senderId, senderName, contributorId, delegatednewsInstanceId);
  }

  @Override
  public DelegatedNews updateOrderDelegatedNews(int pubId, int newsOrder) {
      return this.delegatedNewsService.updateOrderDelegatedNews(pubId, newsOrder);
  }
  
}