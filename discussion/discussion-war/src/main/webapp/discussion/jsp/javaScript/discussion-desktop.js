/*
 * Copyright (C) 2000 - 2026 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/* One listener per Silverpeas shell, including when no Discussion iframe is open. */
(() => {
  'use strict';
  const root = window.top;
  if (root.DiscussionDesktop) {
    return;
  }
  const namespace = 'silverpeas.discussion.' + (root.webContext || '');
  const views = new Map();
  const seen = new Set();
  const queries = new Map();
  const channel = typeof root.BroadcastChannel === 'function'
      ? new root.BroadcastChannel(namespace + '.desktop') : null;
  const token = () => String(Date.now()) + '-' + Math.random().toString(36).slice(2);
  const viewing = message => [...views.values()].some(view => view.instanceId === message.instanceId
      && view.userId === message.userId && view.active());
  if (channel) {
    channel.onmessage = ({data}) => {
      if (data.kind === 'query' && viewing(data)) {
        channel.postMessage({kind:'viewing', token:data.token});
      }
      if (data.kind === 'viewing') {
        queries.get(data.token)?.();
      }
    };
  }
  const anyView = message => {
    if (viewing(message)) {
      return Promise.resolve(true);
    }
    if (!channel) {
      return Promise.resolve(false);
    }
    return new Promise(resolve => {
      const id = token();
      const finish = result => { queries.delete(id); resolve(result); };
      const timer = root.setTimeout(() => finish(false), 150);
      queries.set(id, () => { root.clearTimeout(timer); finish(true); });
      channel.postMessage({kind:'query', token:id, instanceId:message.instanceId, userId:message.userId});
    });
  };
  const claim = id => {
    if (seen.has(id)) {
      return false;
    }
    seen.add(id);
    if (seen.size > 500) {
      seen.delete(seen.values().next().value);
    }
    try {
      const key = namespace + '.desktop.seen';
      const stored = JSON.parse(root.localStorage.getItem(key) || '[]');
      if (stored.includes(id)) {
        return false;
      }
      root.localStorage.setItem(key, JSON.stringify([...stored, id].slice(-200)));
    } catch (error) { /* Tags and the per-window set still deduplicate if storage is unavailable. */ }
    return true;
  };
  const receive = async event => {
    let message;
    try {
      message = JSON.parse(event.data);
      const url = new URL(message.url, root.location.href);
      if (url.origin !== root.location.origin || !url.pathname.endsWith('/Rdiscussion/' + message.instanceId + '/messages/' + message.messageId)) {
        return;
      }
    } catch (error) { return; }
    if (!root.Notification || root.Notification.permission !== 'granted' || await anyView(message)) {
      return;
    }
    const show = () => {
      if (viewing(message) || !claim(message.userId + ':' + message.messageId)) {
        return;
      }
      const options = {body:message.text, tag:'discussion-' + message.messageId, renotify:false,
        icon:(root.webContext || '') + '/util/icons/desktop-user-notification.png'};
      const clickable = notification => {
        notification.onclick = () => {
          if (root.spWindow) {
            root.spWindow.loadLink(message.url);
          } else {
            root.location.assign(message.url);
          }
          root.focus();
          notification.close();
        };
      };
      if (root.spUserNotification) {
        root.spUserNotification.notifyOnDesktop(message.title, options, clickable);
      } else {
        clickable(new root.Notification(message.title, options));
      }
    };
    if (root.navigator.locks) {
      await root.navigator.locks.request(namespace + '-desktop-' + message.messageId, show);
    } else {
      show();
    }
  };
  root.DiscussionDesktop = {
    register(instanceId, userId, active) {
      const id = token();
      views.set(id, {instanceId, userId, active});
      return () => views.delete(id);
    },
    requestPermission: () => root.Notification?.requestPermission()
  };
  const connect = () => {
    if (root.spServerEventSource?.isEnabled()) {
      root.spServerEventSource.addEventListener('DISCUSSION_NEW_MESSAGE', receive, 'discussionDesktop');
    }
  };
  if (typeof whenSilverpeasReady === 'function') {
    whenSilverpeasReady(connect);
  } else {
    document.addEventListener('DOMContentLoaded', connect, {once:true});
  }
})();
