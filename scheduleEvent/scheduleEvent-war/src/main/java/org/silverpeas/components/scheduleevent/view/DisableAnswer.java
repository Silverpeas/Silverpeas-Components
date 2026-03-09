package org.silverpeas.components.scheduleevent.view;

public class DisableAnswer implements AnswerVO {
  private static final String DISABLE_LABEL = "-";
  private static final String HTML_CLASS_ATTRIBUTE = "participation inactif";
  private static final DisableAnswer instance = new DisableAnswer();

  private DisableAnswer() {
  }

  @Override
  public String getPositiveAnswerPercentage() {
    return DISABLE_LABEL;
  }

  @Override
  public String getHtmlClassAttribute() {
    return HTML_CLASS_ATTRIBUTE;
  }

  public static AnswerVO getInstance() {
    return instance;
  }

}
