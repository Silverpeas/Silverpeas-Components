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
/* A read receipt requires sustained visibility in the focused document. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    if (!discussion?.dataset.read) {
      return;
    }
    const history = document.getElementById('discussion-history');
    const visibleSince = new Map();
    const pending = new Set();
    const observed = new Set();
    let intersecting = new WeakSet();
    let timer;
    let retry;
    let sending = false;
    let stopped = false;
    const active = () => {
      if (document.hidden || stopped) {
        return false;
      }
      try { return document.hasFocus() || window.top.document.hasFocus(); }
      catch (error) { return document.hasFocus(); }
    };
    const visible = row => {
      const header = discussion.querySelector('.discussion-header').getBoundingClientRect();
      const composer = discussion.querySelector('.discussion-composer')?.getBoundingClientRect();
      const sidebar = discussion.querySelector('.discussion-sidebar')?.getBoundingClientRect();
      const top = Math.max(0, header.bottom,
          window.innerWidth <= 760 && sidebar ? sidebar.bottom : 0);
      const bottom = Math.min(window.innerHeight, composer?.top ?? window.innerHeight);
      const box = row.getBoundingClientRect();
      const height = Math.min(box.bottom, bottom) - Math.max(box.top, top);
      return intersecting.has(row) && box.width > 0 && box.height > 0 && height > 0 && height >= Math.min(box.height, 120, (bottom - top) * 0.6)
          && bottom > top;
    };
    const clearReadMarkers = ids => {
      const divider = history.querySelector('.discussion-unread-divider');
      ids.forEach(id => {
        const row = document.getElementById('message-' + id);
        row?.classList.remove('discussion-message--unread');
        row?.querySelector('.discussion-unread-badge')?.remove();
      });
      if (divider) {
        const next = history.querySelector('.discussion-message--unread');
        if (next) {
          next.before(divider);
        } else {
          divider.remove();
        }
      }
    };
    const send = async () => {
      if (!active() || sending || !pending.size) {
        return;
      }
      window.clearTimeout(retry);
      retry = undefined;
      sending = true;
      const ids = [...pending].slice(0, 100);
      try {
        const response = await sp.ajaxRequest(discussion.dataset.read).byPostMethod()
            .withParam('ids', ids.join(',')).send();
        if (stopped) {
          return;
        }
        const parsed = new DOMParser().parseFromString(response.responseText, 'text/html');
        const state = parsed.querySelector('.discussion-read-state');
        if (!state || !/^\d+$/.test(state.dataset.unreadCount)) {
          throw new Error('Invalid read receipt');
        }
        ids.forEach(id => pending.delete(id));
        clearReadMarkers(ids);
        discussion.querySelector('.discussion-unread-count').textContent = state.dataset.unreadCount;
        document.dispatchEvent(new Event('discussion:read'));
      } catch (error) {
        retry = window.setTimeout(send, 2000);
      } finally {
        sending = false;
      }
      if (pending.size && !retry) {
        send();
      }
    };
    const scan = () => {
      window.clearTimeout(timer);
      if (!active()) {
        visibleSince.clear();
        return;
      }
      const now = Date.now();
      const present = new Set();
      let wait = Infinity;
      history.querySelectorAll('.discussion-message--unread').forEach(row => {
        const id = row.id.slice('message-'.length);
        if (pending.has(id) || !visible(row)) {
          return;
        }
        present.add(id);
        const start = visibleSince.get(id) ?? now;
        visibleSince.set(id, start);
        if (now - start >= 1000) {
          pending.add(id);
        } else {
          wait = Math.min(wait, 1000 - (now - start));
        }
      });
      visibleSince.forEach((value, id) => {
        if (!present.has(id)) {
          visibleSince.delete(id);
        }
      });
      if (wait !== Infinity) {
        timer = window.setTimeout(scan, wait + 10);
      }
      send();
    };
    const observer = new IntersectionObserver(entries => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          intersecting.add(entry.target);
        } else {
          intersecting.delete(entry.target);
        }
      });
      scan();
    });
    const observe = () => {
      observed.forEach(row => {
        if (!history.contains(row) || !row.classList.contains('discussion-message--unread')) {
          observer.unobserve(row);
          observed.delete(row);
          intersecting.delete(row);
        }
      });
      history.querySelectorAll('.discussion-message--unread').forEach(row => {
        if (!observed.has(row)) {
          observed.add(row);
          observer.observe(row);
        }
      });
      scan();
    };
    const mutations = new MutationObserver(observe);
    const resume = () => {
      stopped = false;
      mutations.observe(history, {childList: true, subtree: true, attributes: true, attributeFilter: ['class']});
      observe();
    };
    window.addEventListener('scroll', scan, {passive: true});
    window.addEventListener('resize', scan);
    discussion.addEventListener('toggle', scan, true);
    window.addEventListener('focus', scan);
    window.addEventListener('blur', scan);
    document.addEventListener('visibilitychange', scan);
    window.addEventListener('pagehide', () => {
      stopped = true;
      window.clearTimeout(timer);
      window.clearTimeout(retry);
      observer.disconnect();
      observed.clear();
      intersecting = new WeakSet();
      mutations.disconnect();
      visibleSince.clear();
    });
    window.addEventListener('pageshow', resume);
    resume();
  });
})();
