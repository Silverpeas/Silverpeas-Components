package com.stratelia.webactiv.quickinfo;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.quickinfo.control.QuickInfoSessionController;

public class QuickinfoIndexer implements ComponentIndexerInterface {

  private QuickInfoSessionController quickinfo = null;

  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws Exception {
    quickinfo = new QuickInfoSessionController(mainSessionCtrl, context);

    quickinfo.index();
  }
}