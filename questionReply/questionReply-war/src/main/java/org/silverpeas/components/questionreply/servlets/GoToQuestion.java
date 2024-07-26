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
package org.silverpeas.components.questionreply.servlets;

import org.silverpeas.core.web.util.servlet.GoTo;
import org.silverpeas.components.questionreply.service.QuestionManagerProvider;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.core.util.URLUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

public class GoToQuestion extends GoTo {

  private static final long serialVersionUID = 8074965533055941265L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req, HttpServletResponse res)
      throws Exception {
    Question question = QuestionManagerProvider.getQuestionManager().getQuestion(Long.parseLong(
        objectId));
    String componentId = question.getInstanceId();

    String gotoURL = URLUtil.getURL(null, componentId) + question.getURL();
    return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
  }
}