/*
 * QuizzInstanciator.java
 *
 * Created on 2001, April, 06, 17:20
 */
package com.stratelia.webactiv.quizz;

import java.sql.Connection;

import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.question.QuestionInstanciator;
import com.stratelia.webactiv.questionContainer.QuestionContainerInstanciator;

/**
 * 
 * @author David LESIMPLE
 * @date 06/04/2001
 * @version 1 update by the Sébastien Antonio - Externalisation of the SQL
 *          request
 */
public class QuizzInstanciator extends Object implements
    ComponentsInstanciatorIntf {

  /** Creates new QuizzInstanciator */
  public QuizzInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {

    // create question component
    QuestionInstanciator questionInst = new QuestionInstanciator(
        "com.stratelia.webactiv.quizz");
    questionInst.create(con, spaceId, componentId, userId);

    // create questionContainer component
    QuestionContainerInstanciator questionContainerInst = new QuestionContainerInstanciator(
        "com.stratelia.webactiv.quizz");
    questionContainerInst.create(con, spaceId, componentId, userId);
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {

    // create question component
    QuestionInstanciator questionInst = new QuestionInstanciator(
        "com.stratelia.webactiv.quizz");
    questionInst.delete(con, spaceId, componentId, userId);

    // delete questionContainer component
    QuestionContainerInstanciator questionContainerInst = new QuestionContainerInstanciator(
        "com.stratelia.webactiv.quizz");
    questionContainerInst.delete(con, spaceId, componentId, userId);

  }

}