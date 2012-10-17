/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.kmelia.control;

import java.rmi.RemoteException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import com.silverpeas.comment.service.CommentService;
import com.silverpeas.comment.service.notification.CommentUserNotificationService;
import com.stratelia.webactiv.kmelia.model.KmeliaRuntimeException;
import com.stratelia.webactiv.util.EJBUtilitaire;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.webactiv.util.exception.SilverpeasRuntimeException;
import com.stratelia.webactiv.util.publication.control.PublicationBm;
import com.stratelia.webactiv.util.publication.control.PublicationBmHome;
import com.stratelia.webactiv.util.publication.model.PublicationDetail;
import com.stratelia.webactiv.util.publication.model.PublicationPK;


/**
 * Default implementation of the services provided by the Blog component.
 * It is managed by the underlying IoC container.
 * At initialization by the IoC container, it registers itself among different services for which
 * it is interested.
 */
@Named("kmeliaService")
public class DefaultKmeliaService implements KmeliaService  {
  
  public static final String COMPONENT_NAME = "kmelia";
  private static final String MESSAGES_PATH = "com.stratelia.webactiv.kmelia.multilang.kmeliaBundle";
  private static final String SETTINGS_PATH = "com.stratelia.webactiv.kmelia.settings.kmeliaSettings";
  private static final ResourceLocator settings = new ResourceLocator(SETTINGS_PATH, "");
  
  @Inject
  private CommentUserNotificationService commentUserNotificationService;
  
  @Inject
  private CommentService commentService;
  
  /**
   * Initializes this service by registering itself among Silverpeas core services as interested
   * by events.
   */
  @PostConstruct
  public void initialize() {
    commentUserNotificationService.register(COMPONENT_NAME, this);
  }
  
  /**
   * Releases all the resources required by this service. For instance, it unregisters from the
   * Silverpeas core services.
   */
  @PreDestroy
  public void release() {
    commentUserNotificationService.unregister(COMPONENT_NAME);
  }

  @Override
  public PublicationDetail getContentById(String contentId) {
    PublicationDetail publication;
    try {
      publication = getPublicationBm().getDetail(new PublicationPK(contentId));
    } catch (RemoteException e) {
      throw new KmeliaRuntimeException(getClass().getSimpleName() + ".getContentById()",
              SilverpeasRuntimeException.ERROR, "root.EX_RECORD_NOT_FOUND", "id = " + contentId, e);
    }
    return publication;
  }

  @Override
  public ResourceLocator getComponentSettings() {
    return settings;
  }

  @Override
  public ResourceLocator getComponentMessages(String language) {
    return new ResourceLocator(MESSAGES_PATH, language);
  }
  
  private PublicationBm getPublicationBm() {
    PublicationBm publicationBm = null;
    try {
      PublicationBmHome publicationBmHome = EJBUtilitaire.getEJBObjectRef(
              JNDINames.PUBLICATIONBM_EJBHOME, PublicationBmHome.class);
      publicationBm = publicationBmHome.create();
    } catch (Exception e) {
      throw new KmeliaRuntimeException(getClass().getSimpleName()+".getPublicationBm()",
              SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
    return publicationBm;
  }

  /**
   * Gets a DefaultCommentService instance.
   * @return a DefaultCommentService instance.
   */
  protected CommentService getCommentService() {
    return commentService;
  }
  
}
