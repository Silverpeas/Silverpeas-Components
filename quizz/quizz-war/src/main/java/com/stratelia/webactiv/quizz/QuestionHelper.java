/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.quizz;

import com.silverpeas.util.StringUtil;
import com.silverpeas.util.web.servlet.FileUploadUtil;
import com.stratelia.webactiv.quizz.control.QuizzSessionController;
import com.stratelia.webactiv.util.FileRepositoryManager;
import com.stratelia.webactiv.util.answer.model.Answer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.fileupload.FileItem;

/**
 *
 * @author ehugonnet
 */
public class QuestionHelper {

  public static boolean isCorrectFile(FileItem filePart) {
    String fileName = FileUploadUtil.getFileName(filePart);
    boolean correctFile = false;
    if (fileName != null) {
      String logicalName = fileName.trim();
      if ((logicalName != null) && (logicalName.length() >= 3) && (logicalName.indexOf(".") != -1)) {
        String type = logicalName.substring(logicalName.indexOf(".") + 1, logicalName.length());
        if (type.length() >= 3) {
          correctFile = true;
        }
      }
    }
    return correctFile;
  }

  public static List<Answer> extractAnswer(List<FileItem> items,
      QuestionForm form, String componentId, String subdir) throws IOException {
    List<Answer> answers = new ArrayList<Answer>();
    Iterator iter = items.iterator();
    while (iter.hasNext()) {
      FileItem item = (FileItem) iter.next();
      String mpName = item.getFieldName();
      if (item.isFormField() && mpName.startsWith("answer")) {
        String answerInput = FileUploadUtil.getOldParameter(items, mpName, "");
        Answer answer = new Answer(null, null, answerInput, 0, false, null,
            0, false, null, null);
        String id = mpName.substring("answer".length());
        String nbPoints = FileUploadUtil.getOldParameter(items, "nbPoints" + id, "0");
        answer.setNbPoints(new Integer(nbPoints).intValue());
        if (new Integer(nbPoints).intValue() > 0) {
          answer.setIsSolution(true);
        }
        String comment = FileUploadUtil.getOldParameter(items, "comment" + id, "");
        answer.setComment(comment);
        String value = FileUploadUtil.getOldParameter(items, "valueImageGallery" + id, "");
        if (StringUtil.isDefined(value)) {
          // traiter les images venant de la gallery si pas d'image externe
          if (!form.isFile()) {
            answer.setImage(value);
          }
        }
        FileItem image = FileUploadUtil.getFile(items, "image" + id);
        if (image != null) {
          addImageToAnswer(answer, image, form, componentId, subdir);
        }
        answers.add(answer);
      }
    }
    return answers;
  }

  protected static void addImageToAnswer(Answer answer, FileItem item,
      QuestionForm form, String componentId, String subdir) throws IOException {
    // it's a file part
    if (QuestionHelper.isCorrectFile(item)) {
      // the part actually contained a file
      String logicalName = FileUploadUtil.getFileName(item);
      String type = logicalName.substring(logicalName.indexOf(".") + 1, logicalName.length());
      String physicalName = new Long(new Date().getTime()).toString() + form.getAttachmentSuffix() + "." + type;
      form.setAttachmentSuffix(form.getAttachmentSuffix() + 1);
      File dir = new File(FileRepositoryManager.getAbsolutePath(componentId) + subdir + File.separator + physicalName);
      long size = item.getSize();
      FileUploadUtil.saveToFile(dir, item);
      if (size > 0) {
        answer.setImage(physicalName);
        form.setFile(true);
      }
    }
  }
}
