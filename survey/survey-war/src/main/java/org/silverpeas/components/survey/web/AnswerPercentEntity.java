/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.survey.web;

import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.web.rs.WebEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * This is a representation of an answer percent against other answer percents of a question.
 * @author silveryocha
 */
public class AnswerPercentEntity implements WebEntity {
  private static final long serialVersionUID = 4859866182709279376L;

  private AnswerEntity answer;
  private BigDecimal percent;

  protected AnswerPercentEntity() {
  }

  /**
   * Gets the given collection of answers to a question into a list of
   * {@link AnswerPercentEntity} instances.
   * @param answers answers to a question.
   * @param nbVoters the total number of survey voters.
   * @param isUsers false to shut down the percent computation, true otherwise.
   * @return a list of {@link AnswerPercentEntity} instances.
   */
  public static List<AnswerPercentEntity> asWebEntities(Collection<Answer> answers, int nbVoters,
      boolean isUsers) {
    return answers.stream().map(a -> {
      final BigDecimal answerPercent;
      if (isUsers && nbVoters > 0) {
        answerPercent = a.getPercent(nbVoters);
      } else {
        answerPercent = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_DOWN);
      }
      final AnswerPercentEntity entity = new AnswerPercentEntity();
      entity.answer = AnswerEntity.asWebEntity(a);
      entity.percent = answerPercent;
      return entity;
    }).collect(toList());
  }

  /**
   * The WEB entity representation of an {@link Answer}.
   * @return an {@link AnswerEntity} instance.
   */
  public AnswerEntity getAnswer() {
    return answer;
  }

  public void setAnswer(final AnswerEntity answer) {
    this.answer = answer;
  }

  /**
   * The percent representation of the answer against the others for a question.
   * @return a {@link BigDecimal} instance.
   */
  public BigDecimal getPercent() {
    return percent;
  }

  public void setPercent(final BigDecimal percent) {
    this.percent = percent;
  }

  @Override
  public URI getURI() {
    return null;
  }
}
