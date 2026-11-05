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
/* Native image selection and clipboard paste share attachment validation and previews. */
(() => {
  'use strict';
  document.addEventListener('DOMContentLoaded', () => {
    const input = document.getElementById('discussion-images');
    const preview = document.getElementById('discussion-image-preview');
    const error = document.getElementById('discussion-image-error');
    const textarea = document.getElementById('discussion-body');
    if (!input || !preview || !error || !textarea) {
      return;
    }
    const add = document.querySelector('.discussion-add-image');
    if (add) {
      add.addEventListener('click', () => input.click());
      document.querySelector('.discussion-composer .discussion-editor-tools')?.append(add);
    }
    const dialog = document.createElement('dialog');
    dialog.className = 'discussion-image-dialog';
    const enlarged = document.createElement('img');
    const close = document.createElement('button');
    close.type = 'button';
    close.textContent = '×';
    close.setAttribute('aria-label', document.getElementById('discussion').dataset.closeImage);
    close.addEventListener('click', () => dialog.close());
    dialog.addEventListener('click', event => { if (event.target === dialog) { dialog.close(); } });
    dialog.addEventListener('close', () => enlarged.removeAttribute('src'));
    dialog.append(close, enlarged);
    document.body.append(dialog);
    document.getElementById('discussion').addEventListener('click', event => {
      const link = event.target.closest('.discussion-message-images a');
      if (!link || event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey
          || typeof dialog.showModal !== 'function') {
        return;
      }
      event.preventDefault();
      enlarged.src = link.href;
      enlarged.alt = link.querySelector('img').alt;
      dialog.setAttribute('aria-label', link.getAttribute('aria-label'));
      dialog.showModal();
    });
    const maxCount = Number(input.dataset.maxCount || 4);
    const maxBytes = Number(input.dataset.maxBytes || 5 * 1024 * 1024);
    let files = [];
    let urls = [];
    const release = () => { urls.forEach(url => URL.revokeObjectURL(url)); urls = []; };
    const render = () => {
      release();
      preview.replaceChildren();
      const transfer = new DataTransfer();
      files.forEach((file, index) => {
        transfer.items.add(file);
        const figure = document.createElement('figure');
        const image = document.createElement('img');
        const url = URL.createObjectURL(file);
        urls.push(url);
        image.src = url;
        image.alt = file.name;
        const remove = document.createElement('button');
        remove.type = 'button';
        remove.textContent = '×';
        remove.title = input.dataset.remove;
        remove.setAttribute('aria-label', input.dataset.remove + ' : ' + file.name);
        remove.addEventListener('click', () => {
          files.splice(index, 1);
          error.hidden = true;
          render();
        });
        figure.append(image, remove);
        preview.append(figure);
      });
      input.files = transfer.files;
      textarea.required = files.length === 0 && !document.getElementById('discussion-files')?.files.length;
      document.dispatchEvent(new Event('discussion:attachments'));
    };
    const addFiles = incoming => {
      const next = [...files, ...incoming];
      if (next.length > maxCount || next.some(file => file.size <= 0 || file.size > maxBytes
          || !['image/jpeg', 'image/png', 'image/gif'].includes(file.type))) {
        error.textContent = input.dataset.error;
        error.hidden = false;
      } else {
        files = next;
        error.hidden = true;
      }
      render();
    };
    input.addEventListener('change', () => addFiles([...input.files]));
    textarea.addEventListener('paste', event => {
      const clipboard = event.clipboardData;
      if (!clipboard) { return; }
      const images = [...(clipboard.items || [])]
          .filter(item => item.kind === 'file' && item.type.startsWith('image/'))
          .map(item => item.getAsFile()).filter(Boolean);
      if (!images.length) { return; }
      // The textarea keeps the browser's plain-text paste when the clipboard also contains text.
      if (!clipboard.getData('text/plain')) { event.preventDefault(); }
      addFiles(images);
    });
    window.addEventListener('pagehide', release);
    window.addEventListener('pageshow', event => { if (event.persisted) { render(); } });
  });
})();
