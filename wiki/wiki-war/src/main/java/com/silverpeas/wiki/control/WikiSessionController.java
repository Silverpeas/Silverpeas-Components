package com.silverpeas.wiki.control;

import com.silverpeas.wiki.control.model.PageDetail;
import com.stratelia.silverpeas.peasCore.AbstractComponentSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;

public class WikiSessionController extends AbstractComponentSessionController {
  WikiPageDAO dao = new WikiPageDAO();

  /**
   * Standard Session Controller Constructeur
   * 
   * 
   * @param mainSessionCtrl
   *          The user's profile
   * @param componentContext
   *          The component's profile
   * 
   * @see
   */
  public WikiSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext,
        "com.silverpeas.wiki.multilang.wikiBundle",
        "com.silverpeas.wiki.settings.wikiIcons");
  }

  public PageDetail getPageFromId(int pageId) throws WikiException {
    return dao.getPage(pageId, getComponentId());
  }
}