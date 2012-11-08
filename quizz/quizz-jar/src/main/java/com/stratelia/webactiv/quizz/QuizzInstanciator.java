/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.webactiv.quizz;

import com.silverpeas.admin.components.ComponentsInstanciatorIntf;
import com.silverpeas.admin.components.InstanciationException;
import java.sql.Connection;

import com.stratelia.webactiv.question.QuestionInstanciator;
import com.stratelia.webactiv.questionContainer.QuestionContainerInstanciator;

/**
 * @author David LESIMPLE
 * @date 06/04/2001
 */
public class QuizzInstanciator extends Object implements ComponentsInstanciatorIntf {

  /** Creates new QuizzInstanciator */
  public QuizzInstanciator() {
  }

  @Override
  public void create(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    QuestionInstanciator questionInst = new QuestionInstanciator("com.stratelia.webactiv.quizz");
    questionInst.create(con, spaceId, componentId, userId);
    QuestionContainerInstanciator questionContainerInst = new QuestionContainerInstanciator(
        "com.stratelia.webactiv.quizz");
    questionContainerInst.create(con, spaceId, componentId, userId);
  }

  @Override
  public void delete(Connection con, String spaceId, String componentId, String userId) throws
      InstanciationException {
    QuestionInstanciator questionInst = new QuestionInstanciator("com.stratelia.webactiv.quizz");
    questionInst.delete(con, spaceId, componentId, userId);
    QuestionContainerInstanciator questionContainerInst = new QuestionContainerInstanciator(
        "com.stratelia.webactiv.quizz");
    questionContainerInst.delete(con, spaceId, componentId, userId);

  }
}