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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.silverpeas.core.ResourceReference;
import org.silverpeas.core.questioncontainer.answer.model.Answer;
import org.silverpeas.core.questioncontainer.answer.model.AnswerPK;
import org.silverpeas.core.security.html.HtmlSanitizer;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;
import org.silverpeas.core.test.extention.TestManagedMock;
import org.silverpeas.core.util.JSONCodec;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.silverpeas.components.survey.web.AnswerEntity.asWebEntity;
import static org.silverpeas.components.survey.web.AnswerPercentEntity.asWebEntities;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
class WebEntityTest {

  private static final Answer AN_ANSWER = new Answer(new AnswerPK("7"),
      new ResourceReference("26", "survey38"), "A survey label", 5, false, "comment", 5, false,
      null, null);
  private static final String AN_ANSWER_AS_JSON =
       "{\"id\":\"7\","
      + "\"instanceId\":\"survey38\","
      + "\"questionId\":\"26\","
      + "\"label\":\"A survey label\","
      + "\"opened\":false,"
      + "\"nbVoters\":5}";

  @TestManagedMock
  private HtmlSanitizer sanitizer;

  @BeforeEach
  void setup() {
    when(sanitizer.sanitize(anyString())).thenAnswer((i) -> i.getArgument(0));
  }

  @Test
  void answerEntityAsJson() {
    final AnswerEntity webEntity = asWebEntity(AN_ANSWER);
    final String json = JSONCodec.encode(webEntity);
    assertThat(json, is(AN_ANSWER_AS_JSON));
  }

  @Test
  void answerEntitiesAsJson() {
    final List<AnswerPercentEntity> webEntities = asWebEntities(List.of(AN_ANSWER), 5, true);
    final String json = JSONCodec.encode(webEntities);
    assertThat(json, is("[{\"answer\":" + AN_ANSWER_AS_JSON + ",\"percent\":100.00}]"));
  }
}