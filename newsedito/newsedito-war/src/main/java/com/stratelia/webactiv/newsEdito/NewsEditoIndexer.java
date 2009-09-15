package com.stratelia.webactiv.newsEdito;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.webactiv.applicationIndexer.control.ComponentIndexerInterface;
import com.stratelia.webactiv.newsEdito.control.NewsEditoSessionController;

public class NewsEditoIndexer implements ComponentIndexerInterface {
     
    private NewsEditoSessionController newsEdito = null;
    
    public void index(MainSessionController mainSessionCtrl, ComponentContext context) throws Exception {
        newsEdito = new NewsEditoSessionController(mainSessionCtrl, context);

		newsEdito.index();
    }
}