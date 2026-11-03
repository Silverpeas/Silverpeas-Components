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

import java.util.List;
import java.util.Locale;

/** One question, two to ten distinct plain-text choices. */
public record DiscussionVoteInput(String instanceId, String question, List<String> answers, boolean multiple, boolean anonymous) {
  public DiscussionVoteInput {
    if (instanceId == null || !instanceId.matches("pollingStation[0-9]+")
        || question == null || question.isBlank() || question.strip().length() > 160
        || answers == null || answers.size() < 2 || answers.size() > 10) {
      throw new IllegalArgumentException("Invalid vote");
    }
    question = question.strip().replace('\r', ' ').replace('\n', ' ');
    if (answers.stream().anyMatch(answer -> answer == null || answer.isBlank() || answer.strip().length() > 200
        || answer.contains("\n") || answer.contains("\r"))) {
      throw new IllegalArgumentException("Invalid vote answers");
    }
    answers = answers.stream().map(String::strip).toList();
    if (answers.stream().map(answer -> answer.toLowerCase(Locale.ROOT)).distinct().count() != answers.size()) {
      throw new IllegalArgumentException("Duplicate vote answers");
    }
  }
}
