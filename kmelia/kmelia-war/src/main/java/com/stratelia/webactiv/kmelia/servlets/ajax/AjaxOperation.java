package com.stratelia.webactiv.kmelia.servlets.ajax;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.BindToPubliHandler;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.DeleteHandler;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.EmptyTrashHandler;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.GetProfileHandler;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.GetTopicWysiwygHandler;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.IsSubscriberHandler;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.RenameTopicHandler;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.SortTopicsHandler;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.SubscribeHandler;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.UnbindToPubliHandler;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.UnsubscribeHandler;
import com.stratelia.webactiv.kmelia.servlets.ajax.handlers.UpdateTopicStatusHandler;

public enum AjaxOperation {
  Delete(new DeleteHandler()),
  GetProfile(new GetProfileHandler()),
  SortTopics(new SortTopicsHandler()),
  EmptyTrash(new EmptyTrashHandler()),
  UpdateTopicStatus(new UpdateTopicStatusHandler()),
  GetTopicWysiwyg(new GetTopicWysiwygHandler()),
  Rename(new RenameTopicHandler()),
  bindToPub(new BindToPubliHandler()),
  unbindToPub(new UnbindToPubliHandler()),
  Subscribe(new SubscribeHandler()),
  Unsubscribe(new UnsubscribeHandler()),
  IsSubscriber(new IsSubscriberHandler());

  private AjaxHandler handler;

  private AjaxOperation(AjaxHandler handler) {
    this.handler = handler;
  }

  public String handleRequest(HttpServletRequest request, ComponentSessionController controller) {
    return this.handler.handleRequest(request, controller);
  }
}
