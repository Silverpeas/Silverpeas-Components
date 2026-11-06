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
/* Quoting stays in the current conversation; creating a thread remains a separate action. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    const textarea = discussion?.querySelector('.discussion-composer textarea');
    if (!discussion || !textarea) { return; }
    discussion.addEventListener('click', event => {
      const trigger = event.target.closest('.discussion-quote');
      if (!trigger) { return; }
      const message = trigger.closest('.discussion-message');
      const body = message.querySelector('.discussion-body');
      let excerpt = body?.innerText.trim();
      if (!excerpt) {
        excerpt = [...message.querySelectorAll('.discussion-message-files a[download], .discussion-message-images img')]
            .map(node => node.textContent.trim() || node.alt).join(' · ');
      }
      if (!excerpt) { return; }
      // Use visible text, never canonical @mention identifiers, so quoting does not notify them again.
      const maximum = Number(discussion.dataset.quoteExcerptLength) || 300;
      excerpt = [...excerpt].slice(0, maximum).join('') + ([...excerpt].length > maximum ? '…' : '');
      const url = new URL(trigger.dataset.quoteUrl, document.baseURI);
      if (url.origin !== location.origin || !['http:','https:'].includes(url.protocol)) { return; }
      window.DiscussionRichEditor?.quote(textarea, {author:trigger.dataset.quoteAuthor, excerpt, url:url.href});
    });
  });
})();
