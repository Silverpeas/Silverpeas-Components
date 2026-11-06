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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.repository;

import org.silverpeas.components.questionreply.model.Recipient;
import org.silverpeas.core.annotation.Repository;
import org.silverpeas.core.persistence.datasource.repository.jpa.BasicJpaEntityRepository;

import java.util.List;

/**
 * Repository of the users to whom the questions of the FAQs are addressed.
 */
@Repository
public class RecipientRepository extends BasicJpaEntityRepository<Recipient> {

  private static final String QUESTION_ID = "questionId";

  /**
   * Finds the users to whom the specified question is addressed.
   * @param questionId the unique identifier of the question.
   * @return the recipients of the question.
   */
  public List<Recipient> findByQuestion(long questionId) {
    return listFromNamedQuery("Recipient.findByQuestion",
        newNamedParameters().add(QUESTION_ID, questionId));
  }

  /**
   * Deletes all the recipients of the specified question.
   * @param questionId the unique identifier of the question.
   */
  public void deleteByQuestion(long questionId) {
    deleteFromNamedQuery("Recipient.deleteByQuestion",
        newNamedParameters().add(QUESTION_ID, questionId));
  }
}
