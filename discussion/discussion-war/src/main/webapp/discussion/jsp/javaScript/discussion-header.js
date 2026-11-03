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
/* Keep the compact header in sync with native read receipts and live message updates. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    if (!discussion) { return; }
    const reading = discussion.querySelector('.discussion-reading');
    const count = reading?.querySelector('.discussion-unread-count');
    if (count) {
      const renderUnread = () => {
        const value = count.textContent.trim();
        reading.hidden = !/^\d+$/.test(value) || Number(value) === 0;
        const label = reading.dataset.unreadLabel.replace('{0}', value);
        reading.title = label;
        reading.setAttribute('aria-label', label);
      };
      const observer = new MutationObserver(renderUnread);
      const observe = () => { observer.observe(count, {childList:true, characterData:true, subtree:true}); renderUnread(); };
      observe();
      window.addEventListener('pagehide', () => observer.disconnect());
      window.addEventListener('pageshow', observe);
    }
    const scope = document.getElementById('discussion-search-scope');
    if (!scope) { return; }
    const summary = scope.querySelector('summary');
    const renderScope = () => {
      const selected = scope.querySelector('input:checked');
      const label = scope.dataset.label.replace('{0}', selected.dataset.label);
      summary.title = label;
      summary.setAttribute('aria-label', label);
      scope.classList.toggle('is-all-channels', selected.value === 'true');
    };
    renderScope();
    scope.addEventListener('change', () => { renderScope(); scope.open = false; summary.focus(); });
    document.addEventListener('click', event => { if (!scope.contains(event.target)) { scope.open = false; } });
    document.addEventListener('keydown', event => {
      if (event.key === 'Escape' && scope.open) {
        scope.open = false;
        summary.focus();
        event.preventDefault();
      }
    });
    scope.closest('form').addEventListener('submit', () => { scope.open = false; });
    window.addEventListener('pageshow', renderScope);
  });
})();
