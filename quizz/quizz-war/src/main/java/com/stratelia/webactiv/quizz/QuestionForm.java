/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.stratelia.webactiv.quizz;

/**
 *
 * @author ehugonnet
 */
public class QuestionForm {

  public int getAttachmentSuffix() {
    return attachmentSuffix;
  }

  public void setAttachmentSuffix(int attachmentSuffix) {
    this.attachmentSuffix = attachmentSuffix;
  }

  public boolean isFile() {
    return file;
  }

  public void setFile(boolean file) {
    this.file = file;
  }
  private boolean file = false;
  private int attachmentSuffix = 0;

  public QuestionForm(boolean file, int attachmentSuffix) {
    this.file = file;
    this.attachmentSuffix = attachmentSuffix;
  }
}
