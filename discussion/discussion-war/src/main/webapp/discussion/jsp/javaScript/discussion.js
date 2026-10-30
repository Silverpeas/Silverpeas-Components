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
/* Discussion history uses the platform AJAX helper so security tokens are preserved. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    const history = document.getElementById('discussion-history');
    const older = document.getElementById('discussion-older');
    const error = document.getElementById('discussion-error');
    const participants = document.getElementById('discussion-participants');
    const presenceError = document.getElementById('discussion-presence-error');
    let presenceLoading = false;
    const refreshPresence = async () => {
      if (!participants || document.hidden || presenceLoading) {
        return;
      }
      presenceLoading = true;
      try {
        const response = await sp.ajaxRequest(discussion.dataset.participants).send();
        const fragment = new DOMParser().parseFromString(response.responseText, 'text/html');
        const list = fragment.querySelector('.discussion-participants-list');
        if (!list) {
          throw new Error('Invalid participant response');
        }
        participants.replaceChildren(document.importNode(list, true));
        participants.classList.remove('is-stale');
        presenceError.hidden = true;
      } catch (failure) {
        participants.classList.add('is-stale');
        presenceError.hidden = false;
      } finally {
        presenceLoading = false;
      }
    };
    if (participants) {
      const presenceTimer = window.setInterval(refreshPresence, 30000);
      window.addEventListener('pagehide', () => window.clearInterval(presenceTimer), { once: true });
      document.addEventListener('visibilitychange', refreshPresence);
      document.addEventListener('discussion:changed', refreshPresence);
    }
    const scroller = window.DiscussionScroll || document.getElementById('discussion-scroll');
    let loading = false;
    const firstPage = () => history.querySelector('.discussion-page:first-child');
    const updateButton = () => {
      older.hidden = firstPage().dataset.more !== 'true';
    };
    const normalizeUnreadDivider = () => {
      history.querySelectorAll('.discussion-unread-divider').forEach((divider, index) => {
        if (index > 0) {
          divider.remove();
        }
      });
    };
    const loadOlder = async () => {
      if (loading || older.hidden) {
        return;
      }
      loading = true;
      older.disabled = true;
      error.hidden = true;
      try {
        const response = await sp.ajaxRequest(discussion.dataset.history)
            .withParam('before', firstPage().dataset.before).send();
        const fragment = new DOMParser().parseFromString(response.responseText, 'text/html');
        const page = fragment.querySelector('.discussion-page');
        if (!page) {
          throw new Error('Invalid history response');
        }
        // Measure immediately before insertion, even if the user moved while the request ran.
        const height = scroller.scrollHeight;
        const top = scroller.scrollTop;
        history.prepend(document.importNode(page, true));
        normalizeUnreadDivider();
        if (typeof applyTokenSecurity === 'function') {
          applyTokenSecurity(history);
        }
        if (window.DiscussionEditor) {
          window.DiscussionEditor.enhance(history);
        }
        updateButton();
        scroller.scrollTop = top + scroller.scrollHeight - height;
      } catch (failure) {
        error.hidden = false;
      } finally {
        loading = false;
        older.disabled = false;
      }
    };
    older.addEventListener('click', loadOlder);
    normalizeUnreadDivider();
    updateButton();
    const observer = new IntersectionObserver(entries => {
      if (entries.some(entry => entry.isIntersecting) && error.hidden) {
        loadOlder();
      }
    }, {root: window.DiscussionScroll ? null : scroller});
    scroller.addEventListener('scroll', () => {
      if (scroller.scrollTop <= 64 && error.hidden) {
        loadOlder();
      }
    });
    window.requestAnimationFrame(() => {
      const hash = window.location.hash.slice(1);
      const target = hash.startsWith('message-') ? document.getElementById(hash) : null;
      const unread = history.querySelector('.discussion-unread-divider');
      const initialTarget = target && history.contains(target) ? target : unread;
      if (initialTarget) {
        scroller.scrollTop += initialTarget.getBoundingClientRect().top - scroller.getBoundingClientRect().top - 8;
      } else {
        scroller.scrollTop = scroller.scrollHeight;
      }
      observer.observe(older);
    });
    window.addEventListener('pagehide', () => observer.disconnect(), {once: true});
  });
})();
