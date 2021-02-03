/*
 * Copyright (C) 2000 - 2021 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.delegatednews.service;

import org.silverpeas.components.delegatednews.dao.DelegatedNewsRepository;
import org.silverpeas.components.delegatednews.model.DelegatedNews;
import org.silverpeas.components.delegatednews.notification.DelegatedNewsDeniedNotification;
import org.silverpeas.components.delegatednews.notification.DelegatedNewsToValidateNotification;
import org.silverpeas.components.delegatednews.notification.DelegatedNewsValidationNotification;
import org.silverpeas.core.admin.component.ComponentInstanceDeletion;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.date.Period;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper.buildAndSend;

@Service
@Transactional
public class DelegatedNewsServiceImpl implements DelegatedNewsService, ComponentInstanceDeletion {

  @Inject
  private DelegatedNewsRepository dao;
  @Inject
  private OrganizationController organizationController;

  @Override
  public void delete(final String componentInstanceId) {
    dao.deleteByComponentInstanceId(componentInstanceId);
  }

  @Override
  public void submitNews(Contribution contribution, Period visibilityPeriod, String userId) {
    DelegatedNews delegatedNews = new DelegatedNews(contribution.getContributionId(),
        contribution.getLastModifier().getId(), new Date(), visibilityPeriod);
    notifyDelegatedNewsToValidate(delegatedNews, userId);
    dao.saveAndFlush(delegatedNews);
  }

  @Override
  public DelegatedNews getDelegatedNews(String contributionId) {
    return dao.getById(contributionId);
  }

  @Override
  public List<DelegatedNews> getDelegatedNews(final Collection<String> contributionIds) {
    return dao.getById(contributionIds);
  }

  @Override
  public List<DelegatedNews> getAllDelegatedNews() {
    return dao.findAllOrderedNews();
  }

  @Override
  public List<DelegatedNews> getAllValidDelegatedNews() {
    return dao.findByStatus(DelegatedNews.NEWS_VALID);
  }

  @Override
  public void validateDelegatedNews(String contributionId, String validatorId) {
    DelegatedNews delegatedNews = dao.getById(contributionId);
    if (delegatedNews != null) {
      delegatedNews.setStatus(DelegatedNews.NEWS_VALID);
      delegatedNews.setValidatorId(validatorId);
      delegatedNews.setValidationDate(new Date());
      dao.saveAndFlush(delegatedNews);

      notifyValidation(delegatedNews, validatorId);
    }
  }

  @Override
  public void refuseDelegatedNews(String contributionId, String validatorId, String refusalMotive) {
    DelegatedNews delegatedNews = dao.getById(contributionId);
    if (delegatedNews != null) {
      delegatedNews.setStatus(DelegatedNews.NEWS_REFUSED);
      delegatedNews.setValidatorId(validatorId);
      delegatedNews.setValidationDate(new Date());
      dao.saveAndFlush(delegatedNews);

      notifyDelegatedNewsRefused(delegatedNews, refusalMotive, validatorId);
    }
  }

  @Override
  public void updateDateDelegatedNews(String contributionId, Period visibilityPeriod) {
    DelegatedNews delegatedNews = dao.getById(contributionId);
    if (delegatedNews != null) {
      delegatedNews.setVisibilityPeriod(visibilityPeriod);
      dao.saveAndFlush(delegatedNews);
    }
  }

  private OrganizationController getOrganisationController() {
    return organizationController;
  }

  private String getAppId() {
    final String componentName = "delegatednews";
    return Stream.of(getOrganisationController().getCompoId(componentName))
        .map(i -> componentName + i)
        .findFirst()
        .orElseGet(() -> {
          SilverLogger.getLogger(this).warn("No instance of 'DelegatedNews' found !");
          return null;
        });
  }

  /**
   * Notify the editorial team about a news to validate.
   */
  private void notifyDelegatedNewsToValidate(DelegatedNews news, String senderId) {
    final String delegatedNewsInstanceId = getAppId();
    if (delegatedNewsInstanceId != null) {
      final String[] editors = getOrganisationController()
          .getUsersIdsByRoleNames(delegatedNewsInstanceId, singletonList("admin"));
      buildAndSend(new DelegatedNewsToValidateNotification(news, User.getById(senderId), editors,
          delegatedNewsInstanceId));
    }
  }

  @Override
  public void updateDelegatedNews(ContributionIdentifier id, String updaterId,
      Period visibilityPeriod) {
    final DelegatedNews delegatedNews = dao.getById(id.getLocalId());
    if (delegatedNews != null) {
      delegatedNews.setInstanceId(id.getComponentInstanceId());
      delegatedNews.setStatus(DelegatedNews.NEWS_TO_VALIDATE);
      delegatedNews.setContributorId(updaterId);
      delegatedNews.setValidatorId(null);
      delegatedNews.setValidationDate(new Date());
      delegatedNews.setVisibilityPeriod(visibilityPeriod);
      dao.saveAndFlush(delegatedNews);
      notifyDelegatedNewsToValidate(delegatedNews, updaterId);
    }
  }

  @Override
  public void deleteDelegatedNews(String contributionId) {
    DelegatedNews delegatedNews = dao.getById(contributionId);
    if (delegatedNews != null) {
      dao.delete(delegatedNews);
    }
  }

  private void notifyValidation(DelegatedNews news, String senderId) {
    String delegatedNewsInstanceId = getAppId();
    // Notification of the last contributor
    if (delegatedNewsInstanceId != null) {
      buildAndSend(new DelegatedNewsValidationNotification(news, User.getById(senderId)));
    }
  }

  private void notifyDelegatedNewsRefused(DelegatedNews news, String refusalMotive, String userId) {
    String delegatedNewsInstanceId = getAppId();
    // Notification of the last contributor
    if (delegatedNewsInstanceId != null) {
      buildAndSend(new DelegatedNewsDeniedNotification(news, User.getById(userId), refusalMotive));
    }
  }

  @Override
  public DelegatedNews updateOrderDelegatedNews(String contributionId, int newsOrder) {
    DelegatedNews delegatedNews = dao.getById(contributionId);
    delegatedNews.setNewsOrder(newsOrder);
    return dao.saveAndFlush(delegatedNews);
  }
}