/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.quizz;

import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.contribution.contentcontainer.content.AbstractSilverpeasContentManager;
import org.silverpeas.core.contribution.model.Contribution;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The quizz implementation of SilverpeasContentManager.
 */
@Service
public class QuizzContentManager  extends AbstractSilverpeasContentManager {

  private static final String CONTENT_ICON_FILE_NAME = "quizzSmall.gif";

  @Inject
  private QuestionContainerService questionContainerService;

  /**
   * Hidden constructor as this implementation must be GET by CDI mechanism.
   */
  protected QuizzContentManager() {
  }

  @Override
  protected String getContentIconFileName(final String componentInstanceId) {
    return CONTENT_ICON_FILE_NAME;
  }

  @Override
  protected Optional<Contribution> getContribution(final String resourceId,
      final String componentInstanceId) {
    return Optional.ofNullable(getQuestionContainerService().getQuestionContainerHeader(
        new QuestionContainerPK(resourceId, "useless", componentInstanceId)));
  }

  @Override
  protected List<Contribution> getAccessibleContributions(
      final List<ResourceReference> resourceReferences, final String currentUserId) {
    List<QuestionContainerPK> pks = resourceReferences.stream()
        .map(r -> new QuestionContainerPK(r.getLocalId(), "useless", r.getComponentInstanceId()))
        .collect(Collectors.toList());
    return new ArrayList<>(getQuestionContainerService().getQuestionContainerHeaders(pks));
  }

  private QuestionContainerService getQuestionContainerService() {
    return questionContainerService;
  }
}