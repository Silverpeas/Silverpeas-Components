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
/* Refresh derived pin content without replacing the native accordion. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('.discussion-composer');
    const input = document.getElementById('discussion-body');
    const send = form?.querySelector('.discussion-send');
    const images = document.getElementById('discussion-images');
    const files = document.getElementById('discussion-files');
    if (!form || !input || !send) {
      return;
    }
    const update = () => {
      input.style.removeProperty('--discussion-input-height');
      input.style.setProperty('--discussion-input-height', input.scrollHeight + 'px');
      send.disabled = !input.value.trim() && !images?.files.length && !files?.files.length;
    };
    input.addEventListener('input', update);
    images?.addEventListener('change', update);
    files?.addEventListener('change', update);
    document.addEventListener('discussion:attachments', update);
    const preview = document.getElementById('discussion-image-preview');
    const observer = new MutationObserver(update);
    if (preview) {
      observer.observe(preview, {childList:true});
    }
    window.addEventListener('resize', update);
    window.addEventListener('pagehide', () => observer.disconnect());
    window.addEventListener('pageshow', () => {
      if (preview) {
        observer.observe(preview, {childList:true});
      }
      update();
    });
    update();
  });
})();
