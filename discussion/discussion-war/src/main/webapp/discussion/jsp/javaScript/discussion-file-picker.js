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
    const input = document.getElementById('discussion-files');
    const preview = document.getElementById('discussion-file-preview');
    const error = document.getElementById('discussion-file-error');
    const textarea = document.getElementById('discussion-body');
    const add = document.querySelector('.discussion-add-file');
    const images = document.getElementById('discussion-images');
    if (!input || !preview || !textarea || !error || !add) {
      return;
    }
    add.addEventListener('click', () => input.click());
    document.querySelector('.discussion-composer .discussion-editor-tools')?.append(add);
    let files = [];
    const render = () => {
      preview.replaceChildren();
      const transfer = new DataTransfer();
      files.forEach((file, index) => {
        transfer.items.add(file);
        const item = document.createElement('span');
        item.className = 'discussion-selected-file';
        const name = document.createElement('span');
        name.textContent = file.name;
        const remove = document.createElement('button');
        remove.type = 'button';
        remove.textContent = '×';
        remove.setAttribute('aria-label', input.dataset.remove + ' : ' + file.name);
        remove.addEventListener('click', () => { files.splice(index,1); error.hidden = true; render(); });
        item.append(name, remove);
        preview.append(item);
      });
      input.files = transfer.files;
      textarea.required = files.length === 0 && !images?.files.length;
      document.dispatchEvent(new Event('discussion:attachments'));
    };
    input.addEventListener('change', () => {
      const selected = [...files, ...input.files];
      if (selected.length > 4 || selected.some(file => file.size <= 0 || file.size > 20 * 1024 * 1024)) {
        error.textContent = input.dataset.error;
        error.hidden = false;
      } else {
        files = selected;
        error.hidden = true;
      }
      render();
    });
    document.addEventListener('discussion:attachments', () => {
      textarea.required = files.length === 0 && !images?.files.length;
    });
  });
})();
