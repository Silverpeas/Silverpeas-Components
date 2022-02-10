/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.questionreply.service;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOImpl;
import org.silverpeas.core.test.extention.EnableSilverTestEnv;

import java.beans.FeatureDescriptor;
import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

/**
 * @author silveryocha
 */
@EnableSilverTestEnv
public class SilverpeasQuestionManagerReplayDaoTest {

  private SilverpeasBeanDAOImpl replyDao;

  private static final String[] EXPECTED_PROPERTIES =
      {"questionId", "title", "content", "creatorId", "creationDate", "publicReply",
          "privateReply"};

  @BeforeEach
  public void setup() throws IllegalAccessException {
    SilverpeasQuestionManager manager = new SilverpeasQuestionManager();
    replyDao = (SilverpeasBeanDAOImpl) FieldUtils.readDeclaredField(manager, "replyDao", true);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void verifyValidProperties() throws IllegalAccessException {
    List<String> replayDaoBeanPropertyNames =
        ((List<PropertyDescriptor>) FieldUtils.readDeclaredField(replyDao, "validProperties", true))
            .stream().map(FeatureDescriptor::getName).collect(Collectors.toList());
    assertThat(replayDaoBeanPropertyNames, containsInAnyOrder(EXPECTED_PROPERTIES));
  }
}