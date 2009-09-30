/*
 * SurveyInstanciator.java
 *
 * Created on 12 decembre 2000, 17:20
 */

package com.stratelia.webactiv.survey;

import java.sql.Connection;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.instance.control.ComponentsInstanciatorIntf;
import com.stratelia.webactiv.beans.admin.instance.control.InstanciationException;
import com.stratelia.webactiv.question.QuestionInstanciator;
import com.stratelia.webactiv.questionContainer.QuestionContainerInstanciator;

/**
 * 
 * @author Nicolas EYSSERIC
 * @date 12/12/2000
 * @version 1 update by the Sébastien Antonio - Externalisation of the SQL
 *          request
 */
public class SurveyInstanciator extends Object implements
    ComponentsInstanciatorIntf {

  /** Creates new SurveyInstanciator */
  public SurveyInstanciator() {
  }

  public void create(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("Survey", "SurveyInstanciator.create",
        "Survey.MSG_ENTRY_METHOD");
    // create question component
    QuestionInstanciator questionInst = new QuestionInstanciator(
        "com.stratelia.webactiv.survey");
    questionInst.create(con, spaceId, componentId, userId);
    // create questionContainer component
    QuestionContainerInstanciator questionContainerInst = new QuestionContainerInstanciator(
        "com.stratelia.webactiv.survey");
    questionContainerInst.create(con, spaceId, componentId, userId);
  }

  public void delete(Connection con, String spaceId, String componentId,
      String userId) throws InstanciationException {
    SilverTrace.info("Survey", "SurveyInstanciator.delete",
        "Survey.MSG_ENTRY_METHOD");

    // delete question component
    QuestionInstanciator questionInst = new QuestionInstanciator(
        "com.stratelia.webactiv.survey");
    questionInst.delete(con, spaceId, componentId, userId);

    // delete questionContainer component
    QuestionContainerInstanciator questionContainerInst = new QuestionContainerInstanciator(
        "com.stratelia.webactiv.survey");
    questionContainerInst.delete(con, spaceId, componentId, userId);
  }

}