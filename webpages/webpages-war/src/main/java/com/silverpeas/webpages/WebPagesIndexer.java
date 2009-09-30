/*
 * Created on 19 avr. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.silverpeas.webpages;

import com.silverpeas.webpages.control.WebPagesSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;

/**
 * @author sdevolder
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class WebPagesIndexer implements ComponentIndexerInterface {

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
    // TODO Auto-generated method stub
    WebPagesSessionController wePagesScc = new WebPagesSessionController(
        mainSessionCtrl, context);
    wePagesScc.index();
  }

}
