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
/* Keep the native details state while displaying its editor below the message toolbar. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const discussion = document.getElementById('discussion');
    if (!discussion) { return; }
    const states = new WeakMap();
    discussion.addEventListener('toggle', event => {
      const details = event.target;
      if (!details.matches('.discussion-edit')) { return; }
      if (details.open && !states.has(details)) {
        const content = details.closest('.discussion-message-content');
        const form = details.querySelector('form');
        if (!content || !form) { return; }
        const placeholder = document.createComment('Message edit action');
        const body = form.elements.body.value;
        details.before(placeholder);
        states.set(details, {placeholder, body});
        details.classList.add('discussion-edit--inline');
        content.append(details);
        form.querySelector('textarea').focus();
      } else if (!details.open && states.has(details)) {
        const {placeholder} = states.get(details);
        placeholder.replaceWith(details);
        states.delete(details);
        details.classList.remove('discussion-edit--inline');
        details.querySelector('summary').focus();
      }
    }, true);
    const cancel = details => {
      const state = states.get(details);
      if (!state) { return; }
      const textarea = details.querySelector('textarea');
      if (window.DiscussionEditor?.reset) { window.DiscussionEditor.reset(textarea, state.body); }
      else { textarea.value = state.body; textarea.dispatchEvent(new Event('input', {bubbles:true})); }
      details.open = false;
    };
    discussion.addEventListener('click', event => {
      const button = event.target.closest('.discussion-edit-cancel');
      if (button) { cancel(button.closest('.discussion-edit')); }
    });
    discussion.addEventListener('keydown', event => {
      if (event.key === 'Escape' && !event.defaultPrevented) {
        const details = event.target.closest('.discussion-edit[open]');
        if (details) { event.preventDefault(); cancel(details); }
      }
    });
  });
})();
