package com.stratelia.webactiv.webSites;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.webSites.control.WebSiteSessionController;

public class WebSitesIndexer implements ComponentIndexerInterface {
     
    private WebSiteSessionController scc = null;
    
    public void index(MainSessionController mainSessionCtrl, ComponentContext context) throws Exception {
        scc = new WebSiteSessionController(mainSessionCtrl, context);
        scc.index();
    }
}