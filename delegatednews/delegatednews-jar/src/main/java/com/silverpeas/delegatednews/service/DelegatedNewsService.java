/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.delegatednews.service;

import java.util.Date;
import java.util.List;

import com.silverpeas.delegatednews.model.DelegatedNews;

public interface DelegatedNewsService {

	public void addDelegatedNews(int pubId, String instanceId, String contributorId, Date validationDate, Date beginDate, Date endDate); 
	public DelegatedNews getDelegatedNews(int pubId); 
	public List<DelegatedNews> getAllDelegatedNews();
	public List<DelegatedNews> getAllValidDelegatedNews();
  public void validateDelegatedNews(int pubId, String validatorId);
  public void refuseDelegatedNews(int pubId, String validatorId);
  public void updateDateDelegatedNews(int pubId, Date dateHourBegin, Date dateHourEnd);
  public void notifyDelegatedNewsToValidate(String pubId, String pubName, String senderId, String senderName, String delegatednewsInstanceId);
  public void updateDelegatedNews(int pubId, String instanceId, String status, String updaterId, String validatorId, Date validationDate, Date dateHourBegin, Date dateHourEnd);
  public void deleteDelegatedNews(int pubId);
  public void notifyDelegatedNewsValid(String pubId, String pubName, String senderId, String senderName, String contributorId, String delegatednewsInstanceId);
  public void notifyDelegatedNewsRefused(String pubId, String pubName, String refusalMotive, String senderId, String senderName, String contributorId, String delegatednewsInstanceId);
}
