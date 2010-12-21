/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.questionReply.control;

import com.silverpeas.attachment.importExport.AttachmentImportExport;
import com.silverpeas.questionReply.QuestionReplyException;
import com.silverpeas.questionReply.model.Question;
import com.silverpeas.questionReply.model.Reply;
import com.silverpeas.util.EncodeHelper;
import com.stratelia.silverpeas.util.ResourcesWrapper;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.util.attachment.model.AttachmentDetail;
import java.io.File;
import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;

/**
 *
 * @author ehugonnet
 */
public class QuestionReplyExport {

  private File file;
  private ResourcesWrapper resource;

  public QuestionReplyExport(ResourcesWrapper resource, File file) {
    this.file = file;
    this.resource = resource;
  }

  public void exportQuestion(Question question, StringBuilder sb, QuestionReplySessionController scc)
      throws QuestionReplyException, ParseException {
    String questionId = question.getPK().getId();
    String qId = "q" + questionId;
    sb.append(
        "<table class=\"question\" width=\"98%\" align=\"center\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">\n");
    sb.append("<tr>\n");
    sb.append("<td>\n");
    sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"2\">\n");
    sb.append("<tr>\n");
    sb.append("<td></td>\n");
    sb.append("<td class=\"titreQuestionReponse\" width=\"100%\">\n");
    sb.append("<div id=").append(qId).append(" class=\"question\">");
    sb.append(EncodeHelper.javaStringToHtmlParagraphe(question.getTitle()));
    sb.append("</div>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("<tr>\n");
    sb.append("<td colspan=\"2\">\n");
    sb.append("<span class=\"txtBaseline\">");
    sb.append("Question de").append(question.readCreatorName()).append(" - ").append(
        resource.getOutputDate(question.getCreationDate()));
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
      sb.append(EncodeHelper.javaStringToHtmlParagraphe(question.getContent()));
      sb.append("</td>\n");
      sb.append("</tr>\n");
      sb.append("</table>\n");
    }
    while (itR.hasNext()) {
      Reply reply = itR.next();
      if (isReplyVisible(question, reply, scc)) {
        reply.getPK().setComponentName(question.getInstanceId());
        exportReply(reply, sb);
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

  protected void exportReply(Reply reply, StringBuilder sb) throws QuestionReplyException,
      ParseException {
    sb.append("<br>\n");
    sb.append("<center>\n");
    sb.append(
        "<table class=\"tableBoard\" width=\"98%\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">\n");
    sb.append("<tr>\n");
    sb.append("<td nowrap=\"nowrap\">\n");
    sb.append("<table width=\"100%\" cellpadding=\"0\" cellspacing=\"2\">\n");
    sb.append("<tr>\n");
    sb.append("<td class=\"titreQuestionReponse\" width=\"100%\">\n");
    sb.append(" <span class=\"titreQuestionReponse\">").append(
        EncodeHelper.javaStringToHtmlParagraphe(reply.getTitle())).append(
        "</span>\n");
    sb.append("</td>\n");
    sb.append("</tr>\n");
    sb.append("</table>\n");
    sb.append("<br>\n");
    sb.append("<table>\n");
    sb.append("<tr>\n");
    sb.append("<td width=\"90%\">");
    sb.append(EncodeHelper.javaStringToHtmlParagraphe(reply.getContent()));
    sb.append("</td>\n");
    // récupération des fichiers joints : copie de ces fichiers dans le dossier "files"
    AttachmentImportExport attachmentIE = new AttachmentImportExport();
    Collection<AttachmentDetail> attachments = null;
    try {
      String filePath = file.getParentFile().getPath() + File.separator + "files";
      String relativeFilePath = file.getParentFile().getPath();
      attachments = attachmentIE.getAttachments(reply.getPK(), filePath, relativeFilePath, null);
    } catch (Exception ex) {
      // En cas d"objet non trouvé: pas d'exception gérée par le système
      throw new QuestionReplyException("QuestionReplySessioncontroller.export()", 0,
          "root.EX_CANT_GET_ATTACHMENTS", ex);
    }

    if (attachments != null && attachments.size() > 0) {
      // les fichiers joints : création du lien dans la page
      sb.append("<td valign=\"top\" align=\"left\">\n");
      sb.append("<a name=\"attachments\"></a>\n");
      sb.append("<td valign=\"top\" align=\"left\">\n");
      sb.append("<center>\n");
      sb.append(
          "<table class=\"tableBoard\" width=\"98%\" border=\"0\" cellpadding=\"5\" cellspacing=\"0\">\n");
      sb.append("<tr>\n");
      sb.append("<td nowrap=\"nowrap\">\n");
      sb.append("<table width=\"150\">\n");

      Iterator<AttachmentDetail> it = attachments.iterator();
      // pour chaque fichier
      while (it.hasNext()) {
        AttachmentDetail attachment = it.next();
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
    sb.append("Réponse de ").append(reply.readCreatorName()).append(" - ").append(
        resource.getOutputDate(reply.getCreationDate()));
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
    sb.append(attachment.getAttachmentFileSize(attachment.getLanguage())).append("  ");
    sb.append(attachment.getAttachmentDownloadEstimation(attachment.getLanguage()));
    sb.append("</td>\n");
    sb.append("</tr>\n");
  }
  
  public boolean  isReplyVisible(Question question, Reply reply, QuestionReplySessionController scc) {
    return isReplyVisible(question, reply, scc.getUserRole(), scc.getUserId()) ;
  }
  
  public static boolean isReplyVisible(Question question, Reply reply, SilverpeasRole role, String userId) {
    boolean isPrivate = reply.getPublicReply() == 0;
    boolean isPublisherQuestion = true;
    if (SilverpeasRole.publisher == role && isPrivate) {
      isPublisherQuestion = question.getCreatorId().equals(userId);
    }
    if (isPrivate && SilverpeasRole.user == role || !isPublisherQuestion) {
      return false;
    }
    return true;
  }
}
