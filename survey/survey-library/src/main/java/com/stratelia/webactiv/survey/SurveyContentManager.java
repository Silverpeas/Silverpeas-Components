/*
 * Copyright (C) 2000 - 2015 Silverpeas
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
package com.stratelia.webactiv.survey;

import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The survey implementation of ContentInterface
 */
@Singleton
public class SurveyContentManager implements ContentInterface {

  @Inject
  private ContentManager contentManager;
  @Inject
  private QuestionContainerService questionContainerService;

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected SurveyContentManager() {
  }

  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param instanceId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @return a List of SilverContent
   */
  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> ids, String instanceId, String userId) {
    return getHeaders(contentManager.getResourcesMatchingContents(ids), instanceId);
  }

  /**
   * return a list of silverContent according to a list of publicationPK
   * @param instanceId the id of the instance
   * @param ids a list of question container identifiers.
   * @return a list of {@link QuestionContainerHeader} instance.
   */
  private List<SilverContentInterface> getHeaders(List<String> ids, String instanceId) {
    List<QuestionContainerPK> pks = ids.stream()
        .map(id -> new QuestionContainerPK(id, "useles", instanceId))
        .collect(Collectors.toList());
    Collection<QuestionContainerHeader> questionContainerHeaders =
        getQuestionContainerService().getQuestionContainerHeaders(pks);
    List<SilverContentInterface> headers = new ArrayList<>(questionContainerHeaders.size());
    for (QuestionContainerHeader qC : questionContainerHeaders) {
      qC.setIconUrl("surveySmall.gif");
      if (qC.getPK().getInstanceId().startsWith("pollingStation")) {
        qC.setIconUrl("pollingStationSmall.gif");
      }
      headers.add(qC);
    }
    return headers;
  }

  private QuestionContainerService getQuestionContainerService() {
    return questionContainerService;
  }
}