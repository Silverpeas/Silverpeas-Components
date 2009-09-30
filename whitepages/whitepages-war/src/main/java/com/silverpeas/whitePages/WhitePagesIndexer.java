/*
 * Created on 13 avr. 2005
 *
 */
package com.silverpeas.whitePages;

import com.silverpeas.whitePages.control.WhitePagesSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;

/**
 * @author neysseri
 * 
 */
public class WhitePagesIndexer implements ComponentIndexerInterface {

  WhitePagesSessionController sc = null;

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface
   * #index(com.stratelia.silverpeas.peasCore.MainSessionController,
   * com.stratelia.silverpeas.peasCore.ComponentContext)
   */
  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws Exception {
    sc = new WhitePagesSessionController(mainSessionCtrl, context, null, null);

    sc.indexVisibleCards();
  }
}