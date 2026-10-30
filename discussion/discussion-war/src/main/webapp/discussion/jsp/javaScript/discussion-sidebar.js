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
/* Refresh derived citation content without replacing the native accordion. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    const content = document.getElementById('discussion-publications');
    if (!discussion?.dataset.publications || !content) {
      return;
    }
    const count = document.getElementById('discussion-publication-count');
    const error = document.getElementById('discussion-publications-error');
    let loading = false;
    let dirty = false;
    let stopped = false;
    let source;
    const refresh = async () => {
      dirty = true;
      if (loading || document.hidden || stopped) {
        return;
      }
      loading = true;
      try {
        do {
          dirty = false;
          const response = await sp.ajaxRequest(discussion.dataset.publications).send();
          if (stopped) {
            return;
          }
          const page = new DOMParser().parseFromString(response.responseText, 'text/html');
          const list = page.querySelector('.discussion-publications-list');
          if (!list || !/^\d+$/.test(list.dataset.count)) {
            throw new Error('Invalid citation response');
          }
          content.replaceChildren(document.importNode(list, true));
          count.textContent = list.dataset.count;
          error.hidden = true;
        } while (dirty && !document.hidden);
      } catch (failure) {
        error.hidden = false;
      } finally {
        loading = false;
      }
    };
    const connect = () => {
      if (stopped || source) {
        return;
      }
      source = window.top.spServerEventSource || window.spServerEventSource;
      if (source?.isEnabled()) {
        source.addEventListener('OPEN_EVENT_SOURCE', refresh);
      }
      refresh();
    };
    if (typeof whenSilverpeasReady === 'function') {
      whenSilverpeasReady(connect);
    } else {
      connect();
    }
    document.addEventListener('discussion:changed', refresh);
    document.addEventListener('visibilitychange', () => { if (!document.hidden) { refresh(); } });
    window.addEventListener('focus', refresh);
    document.getElementById('discussion-publications-section').addEventListener('toggle', event => {
      if (event.target.open) {
        refresh();
      }
    });
    window.addEventListener('pagehide', () => {
      stopped = true;
      source?.removeEventListener('OPEN_EVENT_SOURCE', refresh);
      source = undefined;
    });
    window.addEventListener('pageshow', () => {
      if (stopped) {
        stopped = false;
        connect();
      }
    });
  });
})();
