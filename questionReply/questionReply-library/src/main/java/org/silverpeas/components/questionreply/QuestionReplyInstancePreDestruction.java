/*
 * Copyright (C) 2000 - 2022 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply;

import org.silverpeas.core.admin.component.ComponentInstancePreDestruction;
import org.silverpeas.core.persistence.jdbc.DBUtil;

import javax.inject.Named;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Deletes all the questions and their answers that are managed by the QuestionReply instance that
 * is being deleted.
 * @author mmoquillon
 */
@Named
public class QuestionReplyInstancePreDestruction implements ComponentInstancePreDestruction {

  private static final String RECIPIENT_DELETION =
      "DELETE FROM SC_QuestionReply_Recipient WHERE questionId in (SELECT id FROM " +
          "SC_QuestionReply_Question WHERE instanceId = ?)";
  private static final String ANSWER_DELETION =
      "DELETE FROM SC_QuestionReply_Reply WHERE questionId in (SELECT id FROM " +
          "SC_QuestionReply_Question WHERE instanceId = ?)";
  private static final String QUESTION_DELETION =
      "DELETE FROM SC_QuestionReply_Question WHERE instanceId = ?";

  /**
   * Performs pre destruction tasks in the behalf of the specified component instance.
   * @param componentInstanceId the unique identifier of the component instance.
   */
  @Transactional
  @Override
  public void preDestroy(final String componentInstanceId) {
    try (Connection connection = DBUtil.openConnection()) {
      for (String sql : new String[]{RECIPIENT_DELETION, ANSWER_DELETION, QUESTION_DELETION}) {
        try (PreparedStatement deletion = connection.prepareStatement(sql)) {
          deletion.setString(1, componentInstanceId);
          deletion.execute();
        }
      }
    } catch(SQLException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
