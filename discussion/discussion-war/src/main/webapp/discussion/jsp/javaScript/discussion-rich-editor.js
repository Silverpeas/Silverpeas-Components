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
/* Visual editing only; canonical Markdown remains in the existing native form. */
(() => {
  'use strict';
  const shared = window.SilverpeasRichEditor;
  window.DiscussionRichEditor = {
    ...shared,
    enhance:(textarea, options) => shared.enhance(textarea, {...options,
      integrated:true, labels:document.getElementById('discussion').dataset, classPrefix:'discussion', fieldName:'body',
      cancelOnEscape:field => !!field.closest('.discussion-edit[open]'), cancelEvent:'discussion:cancel-edit',
      quotation:{isQuotation:first => {
        const link = first?.firstChild?.firstChild?.marks.find(mark => mark.type.name === 'link');
        return first?.type.name === 'blockquote' && !!link
            && /\/Rdiscussion\/discussion[0-9]+\/(?:channels\/[a-zA-Z0-9-]{1,40}\/)?messages\/[a-zA-Z0-9-]{1,40}$/.test(link.attrs.href);
      }}
    })
  };
})();
