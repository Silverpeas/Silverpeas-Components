/**
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.quizz.servlets;

import com.silverpeas.peasUtil.GoTo;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.questionContainer.control.QuestionContainerService;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerHeader;
import com.stratelia.webactiv.questionContainer.model.QuestionContainerPK;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;

public class GoToQuizz extends GoTo {

  private static final long serialVersionUID = -25530750219633841L;

  @Override
  public String getDestination(String objectId, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    QuestionContainerPK questionContainerPK = new QuestionContainerPK(objectId);
    QuestionContainerHeader quizz = getQuestionContainerBm()
        .getQuestionContainerHeader(questionContainerPK);

    if (quizz != null) {
      String componentId = quizz.getInstanceId();

      SilverTrace.info("quizz", "GoToQuizz.getDestination",
          "root.MSG_GEN_PARAM_VALUE", "quizz = " + quizz.getId()
          + "componentId = " + componentId);

      String gotoURL = URLManager.getURL(null, componentId) + quizz.getURL();

      return "goto=" + URLEncoder.encode(gotoURL, "UTF-8");
    }
    return null;
  }

  private QuestionContainerService getQuestionContainerBm() {
    return QuestionContainerService.getInstance();
  }
}