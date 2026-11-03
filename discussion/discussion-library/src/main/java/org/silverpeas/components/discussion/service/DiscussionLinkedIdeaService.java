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
import org.silverpeas.components.suggestionbox.model.Suggestion;
import org.silverpeas.components.suggestionbox.model.SuggestionBoxService;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.authorization.ComponentAccessController;
import org.silverpeas.core.util.URLUtil;

/** Mirrors native SuggestionBox deletion rules and uses its collection cleanup lifecycle. */
@Service
@Transactional(Transactional.TxType.MANDATORY)
public class DiscussionLinkedIdeaService {
  @Inject private SuggestionBoxService boxes;
  @Inject private ComponentAccessController componentAccess;

  public DiscussionMessageDeletion describe(String body) {
    return resolve(body).map(idea -> {
      var user = User.getCurrentRequester();
      boolean allowed = canDelete(idea, user);
      boolean visible = allowed || componentAccess.isUserAuthorized(user.getId(), idea.getComponentInstanceId())
          && (idea.getValidation().isValidated() || user.getId().equals(idea.getCreatorId()));
      return new DiscussionMessageDeletion("idea", allowed, visible ? idea.getTitle() : "", false);
    }).orElseGet(DiscussionMessageDeletion::messageOnly);
  }

  public void delete(String body) {
    resolve(body).ifPresent(idea -> {
      if (!canDelete(idea, User.getCurrentRequester())) {
        throw new SecurityException("Suggestion deletion denied");
      }
      idea.getSuggestionBox().getSuggestions().remove(idea);
    });
  }

  private boolean canDelete(Suggestion idea, User user) {
    if (!componentAccess.isUserAuthorized(user.getId(), idea.getComponentInstanceId())) { return false; }
    boolean manager = user.isAccessAdmin() || idea.getSuggestionBox().getHighestUserRole(user) == SilverpeasRole.ADMIN;
    var validation = idea.getValidation();
    if (validation.isInDraft() || validation.isRefused()) {
      return manager || user.getId().equals(idea.getCreatorId());
    }
    return manager && (validation.isPendingValidation() || validation.isValidated());
  }

  private Optional<Suggestion> resolve(String body) {
    return DiscussionContributionReference.find(body, URI.create(URLUtil.getAbsoluteApplicationURL()))
        .filter(reference -> "Suggestion".equals(reference.getType()))
        .flatMap(reference -> boxes.getContributionById(reference)
            .filter(idea -> idea.isDefined() && reference.getComponentInstanceId().equals(idea.getComponentInstanceId())));
  }
}
