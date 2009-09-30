package com.stratelia.webactiv.kmax;

import java.util.Iterator;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.kmelia.control.KmeliaSessionController;
import com.stratelia.webactiv.util.attachment.control.AttachmentController;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;

public class KmaxIndexer implements ComponentIndexerInterface {

  private KmeliaSessionController scc = null;

  public void index(MainSessionController mainSessionCtrl,
      ComponentContext context) throws Exception {

    scc = new KmeliaSessionController(mainSessionCtrl, context);
    scc.indexKmax(scc.getComponentId());

    Iterator it = scc.getAllPublications().iterator();
    while (it.hasNext()) {
      PublicationDetail pd = (PublicationDetail) (it.next());
      AttachmentController.attachmentIndexer(pd.getPK());
    }
  }
}