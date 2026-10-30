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
/* Activity signals never include the textarea value. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    const textarea = discussion?.querySelector('.discussion-composer textarea');
    if (!discussion?.dataset.typing || !textarea) {
      return;
    }
    const root = window.top;
    const indicator = document.getElementById('discussion-typing');
    const permission = document.getElementById('discussion-desktop-enable');
    const typers = new Map();
    const revisions = new Map();
    const tab = Date.now().toString(36) + '_' + Math.random().toString(36).slice(2);
    let source;
    let unregister;
    let stopped = false;
    let stopTimer;
    let expiryTimer;
    let lastSent = 0;
    let typing = false;
    let sending = Promise.resolve();
    const active = () => {
      if (window.closed || window !== root && !window.frameElement) {
        return false;
      }
      const frame = window.frameElement;
      const box = frame?.getBoundingClientRect();
      return !document.hidden && !root.document.hidden && root.document.hasFocus()
          && (!box || box.width > 0 && box.height > 0 && box.bottom > 0 && box.top < root.innerHeight);
    };
    const signal = value => {
      sending = sending.catch(() => {}).then(() => sp.ajaxRequest(discussion.dataset.typing).byPostMethod()
          .withParam('active', String(value)).withParam('tab', tab).send()).catch(() => {});
    };
    const stop = () => {
      window.clearTimeout(stopTimer);
      if (typing) {
        signal(false);
      }
      typing = false;
      lastSent = 0;
    };
    const input = () => {
      if (stopped || !active() || !textarea.value.trim()) {
        stop();
        return;
      }
      const now = Date.now();
      if (!typing || now - lastSent >= 2500) {
        signal(true);
        lastSent = now;
      }
      typing = true;
      window.clearTimeout(stopTimer);
      stopTimer = window.setTimeout(stop, 5000);
    };
    const render = () => {
      window.clearTimeout(expiryTimer);
      const now = Date.now();
      typers.forEach((value, id) => {
        if (value.expires <= now) {
          typers.delete(id);
        }
      });
      const names = [...typers.values()].map(value => value.name);
      indicator.hidden = names.length === 0;
      const shown = names.slice(0,2).join(', ') + (names.length > 2 ? ' + ' + (names.length - 2) : '');
      const text = (names.length === 1 ? discussion.dataset.typingOne : discussion.dataset.typingMany).replace('{0}', shown);
      if (indicator.textContent !== text) {
        indicator.textContent = text;
      }
      if (typers.size) {
        expiryTimer = window.setTimeout(render, Math.max(1, Math.min(...[...typers.values()].map(value => value.expires - now))));
      }
    };
    const receive = event => {
      try {
        const data = JSON.parse(event.data);
        if (data.instanceId !== discussion.dataset.instanceId || data.userId === discussion.dataset.userId
            || !Number.isFinite(data.revision) || !Number.isFinite(data.ttl)) {
          return;
        }
        if ((revisions.get(data.userId) ?? -1) >= data.revision) {
          return;
        }
        revisions.set(data.userId, data.revision);
        if (data.ttl > 0) {
          typers.set(data.userId, {name:data.name, expires:Date.now() + Math.min(data.ttl, 8000)});
        } else {
          typers.delete(data.userId);
        }
        render();
      } catch (error) { /* An invalid signal must not affect the composer. */ }
    };
    const reset = () => { typers.clear(); revisions.clear(); render(); };
    const connect = () => {
      if (stopped || source) {
        return;
      }
      unregister = root.DiscussionDesktop?.register(discussion.dataset.instanceId, discussion.dataset.userId, active);
      source = root.spServerEventSource || window.spServerEventSource;
      if (source?.isEnabled()) {
        source.addEventListener('DISCUSSION_TYPING', receive);
        source.addEventListener('OPEN_EVENT_SOURCE', reset);
      }
      permission.hidden = !root.Notification || root.Notification.permission !== 'default';
    };
    textarea.addEventListener('input', input);
    textarea.form?.addEventListener('submit', stop);
    // Keep the short-lived signal when focus changes: the observer may be in another window.
    // Sending, clearing the draft, leaving the page, or the idle timer ends it.
    permission.addEventListener('click', async () => {
      await root.DiscussionDesktop?.requestPermission();
      permission.hidden = !root.Notification || root.Notification.permission !== 'default';
    });
    window.addEventListener('pagehide', () => {
      stop(); stopped = true;
      window.clearTimeout(expiryTimer);
      unregister?.();
      source?.removeEventListener('DISCUSSION_TYPING', receive);
      source?.removeEventListener('OPEN_EVENT_SOURCE', reset);
      source = undefined;
      reset();
    });
    window.addEventListener('pageshow', () => {
      if (stopped) {
        stopped = false;
        connect();
      }
    });
    if (typeof whenSilverpeasReady === 'function') {
      whenSilverpeasReady(connect);
    } else {
      connect();
    }
  });
})();
