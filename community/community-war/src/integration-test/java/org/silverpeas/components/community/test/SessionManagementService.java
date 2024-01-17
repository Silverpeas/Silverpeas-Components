/*
* Copyright (C) 2000 - 2024 Silverpeas
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as
* published by the Free Software Foundation, either version 3 of the
* License, or (at your option) any later version.
*
* As a special exception to the terms and conditions of version 3.0 of
* the GPL, you may redistribute this Program in connection with Free/Libre
* Open Source Software ("FLOSS") applications as described in Silverpeas's
* FLOSS exception.  You should have received a copy of the text describing
* the FLOSS exception, and it is also available here:
* "http://www.silverpeas.com/legal/licensing"
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.silverpeas.components.community.test;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionValidationContext;

import javax.annotation.Nonnull;
import javax.annotation.Priority;
import javax.enterprise.inject.Default;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

@Service
@Singleton
@Default
@Priority(APPLICATION + 10)
public class SessionManagementService implements SessionManagement {

  private final ConcurrentMap<String, SessionInfo> userDataSessions = new ConcurrentHashMap<>(100);

  @Override
  public Collection<SessionInfo> getConnectedUsersList() {
    return Collections.emptyList();
  }

  @Override
  public Collection<SessionInfo> getDistinctConnectedUsersList(final User user) {
    return Collections.emptyList();
  }

  @Override
  public int getNbConnectedUsersList(final User user) {
    return 0;
  }

  @Override
  @Nonnull
  public SessionInfo getSessionInfo(final String sessionId) {
    SessionInfo session = userDataSessions.get(sessionId);
    if (session == null) {
      if (UserDetail.getCurrentRequester() != null && UserDetail.getCurrentRequester()
          .isAnonymous()) {
        session = new MySessionInfo(sessionId, UserDetail.getAnonymousUser());
      } else {
        session = SessionInfo.NoneSession;
      }
    }
    return session;
  }

  @Override
  public boolean isUserConnected(final User user) {
    return false;
  }

  @Override
  public SessionInfo validateSession(final String sessionKey) {
    return validateSession(SessionValidationContext.withSessionKey(sessionKey));
  }

  @Override
  public SessionInfo validateSession(final SessionValidationContext context) {
    String sessionKey = context.getSessionKey();
    return getSessionInfo(sessionKey);
  }

  private SessionInfo openSession(final SessionInfo session) {
    userDataSessions.put(session.getSessionId(), session);
    return session;
  }

  @Override
  public SessionInfo openSession(final User user, final HttpServletRequest request) {
    HttpSession httpSession = request.getSession();
    SessionInfo session = new MySessionInfo(httpSession.getId(), user);
    return openSession(session);
  }

  @Override
  public SessionInfo openOneShotSession(User user, HttpServletRequest request) {
    HttpSession httpSession = request.getSession();
    return new MySessionInfo(httpSession.getId(), user);
  }

  @Override
  public SessionInfo openAnonymousSession(final HttpServletRequest httpServletRequest) {
    UserDetail anonymousUser = UserDetail.getAnonymousUser();
    if (anonymousUser != null) {
      HttpSession httpSession = httpServletRequest.getSession();
      return new MySessionInfo(httpSession.getId(), UserDetail.getAnonymousUser());
    }
    return SessionInfo.NoneSession;
  }

  @Override
  public void closeSession(final String sessionId) {
    SessionInfo si = userDataSessions.get(sessionId);
    if (si != null) {
      userDataSessions.remove(si.getSessionId());
      si.onClosed();
    }
  }

  private static class MySessionInfo extends SessionInfo {

    MySessionInfo(final String sessionId, final User user) {
      super(sessionId, user);
    }
  }
}