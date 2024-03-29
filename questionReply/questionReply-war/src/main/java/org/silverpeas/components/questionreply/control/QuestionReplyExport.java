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
package org.silverpeas.components.questionreply.control;

import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.admin.user.model.SilverpeasRole;
import org.silverpeas.core.importexport.attachment.AttachmentDetail;
import org.silverpeas.core.importexport.attachment.AttachmentImportExport;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.UnitUtil;
import org.silverpeas.core.util.WebEncodeHelper;

import java.io.File;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author ehugonnet
 */
public class QuestionReplyExport {

  private File file;
  private MultiSilverpeasBundle resource;

  public QuestionReplyExport(MultiSilverpeasBundle resource, File file) {
    this.file = file;
    this.resource = resource;
  }

  public void exportQuestion(Question question, StringBuilder sb,
      QuestionReplySessionController scc) throws QuestionReplyException, ParseException {
    String questionId = question.getPK().getId();
    String qId = "q" + questionId;
    sb.append(
        "<table class=\"question\" width=\"98%\" align=\"center\" border=\"0\" cellpadding=\"0\" " +
            "cellspacing=\"0\">\n");
    sb.append("<tr>\n");
    sb.append("<td>\n");
    sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"2\">\n");
    sb.append("<tr>\n");
    sb.append("<td></td>\n");
    sb.append("<td class=\"titreQuestionReponse\" width=\"100%\">\n");
    sb.append("<div id=").append(qId).append(" class=\"question\">");
    sb.append(WebEncodeHelper.javaStringToHtmlParagraphe(question.getTitle()));
    sb.append("</div>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("<tr>\n");
    sb.append("<td colspan=\"2\">\n");
    sb.append("<span class=\"txtBaseline\">");
    sb.append(question.readCreatorName()).append(" - ")
        .append(resource.getOutputDate(question.getCreationDate()));
    sb.append("</span>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</table>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</table>\n");

    // affichage des réponses
    String aId = "a" + questionId;
    Collection<Reply> replies = question.readReplies();
    Iterator<Reply> itR = replies.iterator();
    boolean existe = false;
    if (itR.hasNext()) {
      existe = true;
    }
    existe = true;
    if (existe) {
      sb.append("<table width=\"98%\" align=\"center\" cellpadding=\"0\" cellspacing=\"0\">\n");
      sb.append("<tr>\n");
      sb.append("<td class=\"answers\">\n");
      sb.append("<div id=").append(aId).append(" class=\"answer\">\n");
      // contenu de la question
      sb.append("<table>\n");
      sb.append("<tr>\n");
      sb.append("<td>");
      sb.append(WebEncodeHelper.javaStringToHtmlParagraphe(question.getContent()));
      sb.append("</td>\n");
      sb.append("</tr>\n");
      sb.append("</table>\n");
    }
    while (itR.hasNext()) {
      Reply reply = itR.next();
      if (isReplyVisible(question, reply, scc)) {
        reply.getPK().setComponentName(question.getInstanceId());
        exportReply(scc, reply, sb);
      }
    }
    if (existe) {
      sb.append("<br>\n");
      sb.append("</div>\n");
      sb.append("</td>\n");
      sb.append("</tr>\n");
      sb.append("</table>\n");
    }

  }

  protected void exportReply(final QuestionReplySessionController qRSC, Reply reply,
      StringBuilder sb) throws QuestionReplyException, ParseException {
    sb.append("<br>\n");
    sb.append("<center>\n");
    sb.append(
        "<table class=\"tableBoard\" width=\"98%\" border=\"0\" cellpadding=\"5\" " +
            "cellspacing=\"0\">\n");
    sb.append("<tr>\n");
    sb.append("<td nowrap=\"nowrap\">\n");
    sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"2\">\n");
    sb.append("<tr>\n");
    sb.append("<td class=\"titreQuestionReponse\" width=\"100%\">\n");
    sb.append(" <span class=\"titreQuestionReponse\">")
        .append(WebEncodeHelper.javaStringToHtmlParagraphe(reply.getTitle())).append("</span>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</table>\n");
    sb.append("<br>\n");
    sb.append("<table>\n");
    sb.append("<tr>\n");
    sb.append("<td width=\"90%\">");
    sb.append(reply.readCurrentWysiwygContent());
    sb.append("</td>\n");
    // récupération des fichiers joints : copie de ces fichiers dans le dossier "files"
    AttachmentImportExport attachmentIE = new AttachmentImportExport(qRSC.getUserDetail());
    Collection<AttachmentDetail> attachments = null;
    try {
      String filePath = file.getParentFile().getPath() + File.separator + "files";
      String relativeFilePath = file.getParentFile().getPath();
      attachments = attachmentIE.getAttachments(reply.getPK().toResourceReference(), filePath,
          relativeFilePath, null);
    } catch (Exception ex) {
      // En cas d"objet non trouvé: pas d'exception gérée par le système
      throw new QuestionReplyException(ex);
    }

    if (attachments != null && !attachments.isEmpty()) {
      // les fichiers joints : création du lien dans la page
      sb.append("<td valign=\"top\" align=\"left\">\n");
      sb.append("<a name=\"attachments\"></a>\n");
      sb.append("<td valign=\"top\" align=\"left\">\n");
      sb.append("<center>\n");
      sb.append(
          "<table class=\"tableBoard\" width=\"98%\" border=\"0\" cellpadding=\"5\" " +
              "cellspacing=\"0\">\n");
      sb.append("<tr>\n");
      sb.append("<td nowrap=\"nowrap\">\n");
      sb.append("<table width=\"150\">\n");

      // pour chaque fichier
      for (AttachmentDetail attachment : attachments) {
        exportAttachment(attachment, sb);
      }
      sb.append("</table>\n");
      sb.append("</td>\n");
      sb.append("</tr>\n");
      sb.append("</table>\n");
      sb.append("</center>\n");
      sb.append("</td>\n");
    }
    sb.append("</tr>\n");
    sb.append("</table>\n");
    sb.append("<br>\n");
    sb.append("<span class=\"txtBaseline\">");
    sb.append(resource.getString("questionReply.replyOf")).append(" ")
        .append(reply.readCreatorName());
    sb.append(" - ").append(resource.getOutputDate(reply.getCreationDateAsString()));
    sb.append("</span>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</table>\n");
    sb.append("</center>\n");
  }

  protected void exportAttachment(AttachmentDetail attachment, StringBuilder sb) {
    // attachment
    sb.append("<tr>\n");
    sb.append("<td align=\"center\"></td>\n");
    sb.append("</tr>\n");
    sb.append("<tr>\n");
    sb.append("<td valign=\"top\">\n");
    sb.append("<nobr>\n");
    sb.append("<a href=\"files/");
    sb.append(attachment.getLogicalName());
    sb.append("\" target=\"_blank\">");
    sb.append(attachment.getLogicalName());
    sb.append("</a>\n");
    sb.append("</nobr>\n");
    sb.append("<br>\n");
    sb.append(UnitUtil.formatMemSize(attachment.getSize()));
    sb.append("</td>\n");
    sb.append("</tr>\n");
  }

  public boolean isReplyVisible(Question question, Reply reply,
      QuestionReplySessionController scc) {
    return isReplyVisible(question, reply, scc.getUserRole(), scc.getUserId());
  }

  public static boolean isReplyVisible(Question question, Reply reply, SilverpeasRole role,
      String userId) {
    boolean isPrivate = reply.getPublicReply() == 0;
    boolean isPublisherQuestion = true;
    if (SilverpeasRole.PUBLISHER == role && isPrivate) {
      isPublisherQuestion = question.getCreatorId().equals(userId);
    }
    return !(isPrivate && SilverpeasRole.USER == role || !isPublisherQuestion);
  }
}
