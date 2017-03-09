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

package org.silverpeas.components.quizz;

import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerProvider;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The quizz implementation of ContentInterface.
 */
public class QuizzContentManager implements ContentInterface {

  @Inject
  private ContentManager contentManager;
  @Inject
  private QuestionContainerService questionContainerService;

  @Override
  public List<SilverContentInterface> getSilverContentById(List<Integer> ids, String instanceId,
      String userId) {
    return getHeaders(ContentManagerProvider.getContentManager().getResourcesMatchingContents(ids),
        instanceId);
  }

  /**
   * return a list of silverContent according to a list of publicationPK
   * @param ids a list of publicationPK
   * @return a list of publicationDetail
   */
  private List<SilverContentInterface> getHeaders(List<String> ids, String instanceId) {
    List<QuestionContainerPK> pks = ids.stream()
        .map(id -> new QuestionContainerPK(id, "useles", instanceId))
        .collect(Collectors.toList());
    ArrayList<QuestionContainerHeader> questionHeaders =
        new ArrayList<>(getQuestionContainerService().getQuestionContainerHeaders(pks));
    List headers = new ArrayList(questionHeaders.size());
    for (QuestionContainerHeader questionContainerHeader : questionHeaders) {
      questionContainerHeader.setIconUrl("quizzSmall.gif");
      headers.add(questionContainerHeader);
    }
    return headers;
  }

  private QuestionContainerService getQuestionContainerService() {
    return questionContainerService;
  }
}