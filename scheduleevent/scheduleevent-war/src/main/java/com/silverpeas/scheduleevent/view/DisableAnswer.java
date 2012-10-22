package com.silverpeas.scheduleevent.view;;

public class DisableAnswer implements AnswerVO {
  private final static String DISABLE_LABEL = "-"; 
  private final static String HMTL_CLASS_ATTRIBUTE = "participation inactif";
  private final static DisableAnswer instance = new DisableAnswer();

  private DisableAnswer() {
  }

  @Override
  public String getPositiveAnswerPercentage() {
    return DISABLE_LABEL;
  }

  @Override
  public String getHtmlClassAttribute() {
    return HMTL_CLASS_ATTRIBUTE;
  }

  public static AnswerVO getInstance() {
    return instance;
  }

}
