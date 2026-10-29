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
    let loading = false;
    const lastPage = () => history.querySelector('.discussion-page:last-child');
    const updateButton = () => {
      older.hidden = lastPage().dataset.more !== 'true';
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
            .withParam('before', lastPage().dataset.before).send();
        const fragment = new DOMParser().parseFromString(response.responseText, 'text/html');
        const page = fragment.querySelector('.discussion-page');
        if (!page) {
          throw new Error('Invalid history response');
        }
        history.appendChild(document.importNode(page, true));
        if (typeof applyTokenSecurity === 'function') {
          applyTokenSecurity(history);
        }
        updateButton();
      } catch (failure) {
        error.hidden = false;
      } finally {
        loading = false;
        older.disabled = false;
      }
    };
    older.addEventListener('click', loadOlder);
    updateButton();
    const observer = new IntersectionObserver(entries => {
      if (entries.some(entry => entry.isIntersecting) && error.hidden) {
        loadOlder();
      }
    });
    observer.observe(older);
  });
})();
