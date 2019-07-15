/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.notification.user.builder.helper.UserNotificationHelper;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Singleton
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

  /**
   * Add new delegated news
   */
  @Override
  public void submitNews(String id, SilverpeasContent news, String lastUpdaterId, Period visibilityPeriod, String userId) {
    DelegatedNews delegatedNews =
      new DelegatedNews(Integer.parseInt(id), news.getComponentInstanceId(), lastUpdaterId, new Date(),
          visibilityPeriod.getBeginDate(), visibilityPeriod.getEndDate());

    notifyDelegatedNewsToValidate(delegatedNews, userId);

    dao.saveAndFlush(delegatedNews);
  }

  /**
   * Récupère une actualité déléguée correspondant à la publication Theme Tracker passée en
   * paramètre
   * @param pubId : l'id de la publication de Theme Tracker
   * @return DelegatedNews : l'objet correspondant à l'actualité déléguée ou null si elle n'existe
   * pas
   */
  @Override
  public DelegatedNews getDelegatedNews(int pubId) {
    return dao.getById(Integer.toString(pubId));
  }

  @Override
  public List<DelegatedNews> getDelegatedNews(final Collection<String> pubIds) {
    return dao.getById(pubIds);
  }

  /**
   * Retrieve all delegated news from Theme Tracker applications
   * @return List<DelegatedNews> : list of delegated news
   */
  @Override
  public List<DelegatedNews> getAllDelegatedNews() {
    return dao.findAllOrderedNews();
  }

  /**
   * Retrieve all valid delegated news
   * @return List<DelegatedNews> : list of valid delegated news
   */
  @Override
  public List<DelegatedNews> getAllValidDelegatedNews() {
    return dao.findByStatus(DelegatedNews.NEWS_VALID);
  }

  /**
   * Valide l'actualité déléguée passée en paramètre
   */
  @Override
  public void validateDelegatedNews(int pubId, String validatorId) {
    DelegatedNews delegatedNews = dao.getById(Integer.toString(pubId));
    if (delegatedNews != null) {
      delegatedNews.setStatus(DelegatedNews.NEWS_VALID);
      delegatedNews.setValidatorId(validatorId);
      delegatedNews.setValidationDate(new Date());
      dao.saveAndFlush(delegatedNews);

      notifyValidation(delegatedNews, validatorId);
    }
  }

  /**
   * Refuse l'actualité déléguée passée en paramètre
   */
  @Override
  public void refuseDelegatedNews(int pubId, String validatorId, String refusalMotive) {
    DelegatedNews delegatedNews = dao.getById(Integer.toString(pubId));
    if (delegatedNews != null) {
      delegatedNews.setStatus(DelegatedNews.NEWS_REFUSED);
      delegatedNews.setValidatorId(validatorId);
      delegatedNews.setValidationDate(new Date());
      dao.saveAndFlush(delegatedNews);

      notifyDelegatedNewsRefused(delegatedNews, refusalMotive, validatorId);
    }
  }

  /**
   * Met à jour les dates de visibilité de l'actualité déléguée passée en paramètre
   */
  @Override
  public void updateDateDelegatedNews(int pubId, Date dateHourBegin, Date dateHourEnd) {
    DelegatedNews delegatedNews = dao.getById(Integer.toString(pubId));
    if (delegatedNews != null) {
      delegatedNews.setBeginDate(dateHourBegin);
      delegatedNews.setEndDate(dateHourEnd);
      dao.saveAndFlush(delegatedNews);
    }
  }

  /**
   * @return
   */
  private OrganizationController getOrganisationController() {
    return organizationController;
  }

  private String getAppId() {
    String[] instanceIds = getOrganisationController().getCompoId("delegatednews");
    if (ArrayUtil.isNotEmpty(instanceIds)) {
      return "delegatednews"+instanceIds[0];
    }
    SilverLogger.getLogger(this).warn("No instance of 'DelegatedNews' found !");
    return null;
  }

  /**
   * Notifie l'Equipe éditoriale d'une actualité à valider
   */
  private void notifyDelegatedNewsToValidate(DelegatedNews news, String senderId) {
    String delegatednewsInstanceId = getAppId();
    // Notification des membres de l'équipe éditoriale
    if (delegatednewsInstanceId != null) {
      List<String> roles = new ArrayList<>();
      roles.add("admin");
      String[] editors = getOrganisationController().getUsersIdsByRoleNames(
          delegatednewsInstanceId, roles);

      UserNotificationHelper.buildAndSend(
          new DelegatedNewsToValidateNotification(news, User.getById(senderId), editors,
              delegatednewsInstanceId));
    }
  }

  /**
   * Update delegated news identified by id
   * @param id delegated news identifier
   * @param news the news content
   * @param updaterId updater identifier
   * @param visibilityPeriod the visibility period to update
   */
  @Override
  public void updateDelegatedNews(String id, SilverpeasContent news, String updaterId, Period visibilityPeriod) {
    DelegatedNews delegatedNews = dao.getById(id);
    if (delegatedNews != null) {
      delegatedNews.setInstanceId(news.getComponentInstanceId());
      delegatedNews.setStatus(DelegatedNews.NEWS_TO_VALIDATE);
      delegatedNews.setContributorId(updaterId);
      delegatedNews.setValidatorId(null);
      delegatedNews.setValidationDate(new Date());
      delegatedNews.setBeginDate(visibilityPeriod.getBeginDate());
      delegatedNews.setEndDate(visibilityPeriod.getEndDate());
      dao.saveAndFlush(delegatedNews);

      notifyDelegatedNewsToValidate(delegatedNews, updaterId);
    }
  }

  /**
   * Delete delegated news identified by pubId
   * @param pubId the delegated news identifier to delete
   */
  @Override
  public void deleteDelegatedNews(int pubId) {
    DelegatedNews delegatedNews = dao.getById(Integer.toString(pubId));
    if (delegatedNews != null) {
      dao.delete(delegatedNews);
    }
  }

  /**
   * Notifie le dernier contributeur que l'actualité est validée
   */
  private void notifyValidation(DelegatedNews news, String senderId) {
    String delegatednewsInstanceId = getAppId();
    // Notification du dernier contributeur
    if (delegatednewsInstanceId != null) {
      UserNotificationHelper
          .buildAndSend(new DelegatedNewsValidationNotification(news, User.getById(senderId)));
    }
  }

  /**
   * Notifie le dernier contributeur que l'actualité est refusée
   */
  private void notifyDelegatedNewsRefused(DelegatedNews news, String refusalMotive, String userId) {
    String delegatednewsInstanceId = getAppId();
    // Notification du dernier contributeur
    if (delegatednewsInstanceId != null) {
      UserNotificationHelper.buildAndSend(
          new DelegatedNewsDeniedNotification(news, User.getById(userId), refusalMotive));
    }
  }

  /**
   * Met à jour l'ordre de l'actualité déléguée passée en paramètre
   */
  @Override
  public DelegatedNews updateOrderDelegatedNews(int pubId, int newsOrder) {
    DelegatedNews delegatedNews = dao.getById(Integer.toString(pubId));
    delegatedNews.setNewsOrder(newsOrder);
    return dao.saveAndFlush(delegatedNews);
  }
}