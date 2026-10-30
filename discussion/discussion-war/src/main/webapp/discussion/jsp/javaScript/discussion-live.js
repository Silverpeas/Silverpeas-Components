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
/* Synchronize only loaded messages, plus a bounded batch of new messages. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    if (!discussion?.dataset.updates) {
      return;
    }
    const history = document.getElementById('discussion-history');
    const scroller = document.getElementById('discussion-scroll');
    const failure = document.getElementById('discussion-live-error');
    const articles = () => Array.from(history.querySelectorAll('.discussion-message'));
    const latest = () => Math.max(0, ...articles().map(row => Number(row.dataset.sequence)));
    const fingerprints = new Map();
    let loading = false;
    let stopped = false;

    const merge = page => {
      // Anchor the current row rather than the total height: edits can resize older messages.
      const bottom = scroller.scrollHeight - scroller.scrollTop - scroller.clientHeight < 48;
      const anchor = articles().find(row => row.getBoundingClientRect().bottom > scroller.getBoundingClientRect().top);
      const anchorTop = anchor?.getBoundingClientRect().top;
      page.querySelectorAll('.discussion-message').forEach(row => {
        const existing = document.getElementById(row.id);
        const fingerprint = row.outerHTML;
        // Keep an open editor and in-flight reactions intact; the next poll will reconcile them.
        if (existing?.querySelector('details[open], .discussion-like-button:disabled') ||
            existing?.contains(document.activeElement) ||
            existing?.contains(window.getSelection()?.anchorNode) ||
            (existing && fingerprints.get(row.id) === fingerprint)) {
          return;
        }
        fingerprints.set(row.id, fingerprint);
        const fresh = document.importNode(row, true);
        if (existing && history.contains(existing)) {
          existing.replaceWith(fresh);
        } else {
          history.querySelector('.discussion-page:last-child').append(fresh);
        }
      });
      if (articles().length) {
        history.querySelector('.discussion-empty')?.remove();
      }
      const divider = page.querySelector('.discussion-unread-divider') || history.querySelector('.discussion-unread-divider');
      history.querySelectorAll('.discussion-unread-divider').forEach(node => node.remove());
      const firstUnread = history.querySelector('.discussion-message--unread');
      if (firstUnread && divider) {
        firstUnread.before(document.importNode(divider, true));
      }
      const count = discussion.querySelector('.discussion-unread-count');
      count.textContent = page.dataset.unreadCount;
      const read = discussion.querySelector('.discussion-read-form');
      read.hidden = Number(page.dataset.unreadCount) === 0;
      read.querySelector('[name="sequence"]').value = latest();
      if (typeof applyTokenSecurity === 'function') {
        applyTokenSecurity(history);
      }
      window.DiscussionEditor?.enhance(history);
      if (bottom) {
        scroller.scrollTop = scroller.scrollHeight;
      } else if (anchor) {
        const current = document.getElementById(anchor.id);
        scroller.scrollTop += current.getBoundingClientRect().top - anchorTop;
      }
    };

    const refresh = async () => {
      if (document.hidden || loading || stopped) {
        return;
      }
      loading = true;
      try {
        const ids = articles().map(row => row.id.slice('message-'.length));
        // Refresh all loaded pages without ever sending an unbounded request.
        for (let offset = 0; offset < Math.max(1, ids.length); offset += 100) {
          const response = await sp.ajaxRequest(discussion.dataset.updates)
              .withParam('after', latest()).withParam('ids', ids.slice(offset, offset + 100).join(',')).send();
          if (stopped) {
            return;
          }
          const fragment = new DOMParser().parseFromString(response.responseText, 'text/html');
          const page = fragment.querySelector('.discussion-page');
          if (!page || !page.hasAttribute('data-unread-count')) {
            throw new Error('Invalid discussion update');
          }
          merge(page);
        }
        failure.hidden = true;
      } catch (error) {
        failure.hidden = false;
      } finally {
        loading = false;
      }
    };
    let timer = window.setInterval(refresh, 3000);
    document.addEventListener('visibilitychange', refresh);
    window.addEventListener('focus', refresh);
    window.addEventListener('pagehide', () => {
      stopped = true;
      window.clearInterval(timer);
    });
    window.addEventListener('pageshow', () => {
      if (stopped) {
        stopped = false;
        timer = window.setInterval(refresh, 3000);
        refresh();
      }
    });
  });
})();
