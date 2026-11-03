/*
 * Copyright (C) 2000 - 2026 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.discussion.service;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.net.URI;
import java.util.Optional;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerHeader;
import org.silverpeas.core.questioncontainer.container.model.QuestionContainerPK;
import org.silverpeas.core.questioncontainer.container.service.QuestionContainerService;
import org.silverpeas.core.security.authorization.AccessControlContext;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.util.URLUtil;

/** Native Vote management rights; never accepts a Survey instance. */
@Service
@Transactional(Transactional.TxType.MANDATORY)
public class DiscussionLinkedVoteService {
  @Inject private QuestionContainerService containers;
  @Inject private ComponentAccessController componentAccess;

  public DiscussionMessageDeletion describe(String body) {
    return resolve(body).map(vote -> {
      boolean visible = componentAccess.isUserAuthorized(User.getCurrentRequester().getId(), vote.getInstanceId());
      return new DiscussionMessageDeletion("vote", canDelete(vote), visible ? vote.getTitle() : "", false);
    }).orElseGet(DiscussionMessageDeletion::messageOnly);
  }

  public void delete(String body) {
    resolve(body).ifPresent(vote -> {
      if (!canDelete(vote)) { throw new SecurityException("Vote deletion denied"); }
      containers.deleteQuestionContainer(vote.getPK());
    });
  }

  private boolean canDelete(QuestionContainerHeader vote) {
    var user = User.getCurrentRequester();
    if (!componentAccess.isUserAuthorized(user.getId(), vote.getInstanceId())) { return false; }
    var role = SilverpeasRole.getHighestFrom(componentAccess.getUserRoles(user.getId(), vote.getInstanceId(), AccessControlContext.init()));
    return role != null && role.isGreaterThanOrEquals(SilverpeasRole.PUBLISHER);
  }

  private Optional<QuestionContainerHeader> resolve(String body) {
    return DiscussionContributionReference.find(body, URI.create(URLUtil.getAbsoluteApplicationURL()))
        .filter(reference -> "QuestionContainer".equals(reference.getType()) && reference.getComponentInstanceId().startsWith("pollingStation"))
        .flatMap(reference -> Optional.ofNullable(containers.getQuestionContainerHeader(new QuestionContainerPK(reference.getLocalId(), null, reference.getComponentInstanceId())))
            .filter(vote -> reference.getComponentInstanceId().equals(vote.getInstanceId())));
  }
}
