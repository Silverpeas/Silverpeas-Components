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
(function(root) {
  'use strict';
  const TOKEN = /@\[([^\]\r\n]{1,300})\]\(([0-9]{1,40})\)|https?:\/\/[^\s<>]+/gi;
  const EMOJI = ['😀', '😊', '😂', '😍', '🤔', '😮', '😢', '👍', '👎', '👏', '🙌', '❤️', '🎉', '🚀', '✅', '🙏', '💡', '👀'];
  const fold = text => text.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase();

  function tokenize(text) {
    const tokens = [];
    let offset = 0;
    for (const match of text.matchAll(TOKEN)) {
      if (match.index > offset) {
        tokens.push({type: 'text', text: text.slice(offset, match.index)});
      }
      if (match[1]) {
        tokens.push({type: 'mention', text: '@' + match[1], id: match[2]});
      } else {
        let urlText = match[0].replace(/[.,!?;:]+$/, '');
        while (urlText.endsWith(')') && urlText.split(')').length > urlText.split('(').length) {
          urlText = urlText.slice(0, -1);
        }
        try {
          const url = new URL(urlText);
          if (url.protocol !== 'http:' && url.protocol !== 'https:') {
            throw new Error('Unsupported URL');
          }
          tokens.push({type: 'link', text: urlText, href: url.href});
          if (urlText.length < match[0].length) {
            tokens.push({type: 'text', text: match[0].slice(urlText.length)});
          }
        } catch (failure) {
          tokens.push({type: 'text', text: match[0]});
        }
      }
      offset = match.index + match[0].length;
    }
    if (offset < text.length) {
      tokens.push({type: 'text', text: text.slice(offset)});
    }
    return tokens;
  }

  function decodeMentions(value) {
    const mentions = [];
    let removed = 0;
    const text = value.replace(/@\[([^\]\r\n]{1,300})\]\(([0-9]{1,40})\)/g, (match, name, id, offset) => {
      const start = offset - removed;
      const label = '@' + name;
      removed += match.length - label.length;
      mentions.push({start, end: start + label.length, name, id});
      return label;
    });
    return {text, mentions};
  }
  function moveMentions(previous, value, mentions) {
    let start = 0;
    while (start < previous.length && start < value.length && previous[start] === value[start]) {
      start++;
    }
    let end = previous.length;
    let nextEnd = value.length;
    while (end > start && nextEnd > start && previous[end - 1] === value[nextEnd - 1]) { end--; nextEnd--; }
    const delta = nextEnd - end;
    return mentions.flatMap(mention => {
      if (mention.end <= start) {
        return [mention];
      }
      if (mention.start >= end) {
        return [{...mention, start: mention.start + delta, end: mention.end + delta}];
      }
      return [];
    });
  }
  function encodeMentions(value, mentions) {
    let result = value;
    [...mentions].sort((a, b) => b.start - a.start).forEach(mention => {
      if (value.slice(mention.start, mention.end) === '@' + mention.name) {
        result = result.slice(0, mention.start) + '@[' + mention.name + '](' + mention.id + ')' + result.slice(mention.end);
      }
    });
    return result;
  }

  function mentionQuery(value, caret) {
    const match = value.slice(0, caret).match(/(?:^|\s)@([^\s@\[\]]*)$/u);
    return match ? {query: match[1], start: caret - match[1].length - 1, end: caret} : null;
  }

  function filterPeople(people, query) {
    const search = fold(query);
    return people.filter(person => fold(person.name).includes(search)).slice(0, 8);
  }

  if (typeof module !== 'undefined' && module.exports) {
    module.exports = {tokenize, mentionQuery, filterPeople, decodeMentions, moveMentions, encodeMentions};
  }
  if (typeof document === 'undefined') {
    return;
  }

  let discussion;
  let people = [];
  let listNumber = 0;
  const element = (tag, className) => {
    const node = document.createElement(tag);
    node.className = className;
    return node;
  };
  function icon(name) {
    const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
    svg.setAttribute('class', 'discussion-icon');
    svg.setAttribute('aria-hidden', 'true');
    const use = document.createElementNS('http://www.w3.org/2000/svg', 'use');
    use.setAttribute('href', discussion.dataset.icons + '#' + name);
    svg.appendChild(use);
    return svg;
  }
  function tool(label, name) {
    const button = element('button', 'discussion-icon-button');
    button.type = 'button';
    button.title = label;
    button.setAttribute('aria-label', label);
    button.appendChild(icon(name));
    return button;
  }
  function renderBody(body) {
    if (body.dataset.rendered) {
      return;
    }
    const content = document.createDocumentFragment();
    for (const token of tokenize(body.textContent)) {
      if (token.type === 'text') {
        content.appendChild(document.createTextNode(token.text));
      } else {
        const link = document.createElement('a');
        link.textContent = token.text;
        if (token.type === 'mention') {
          link.href = discussion.dataset.profileBase + '?userId=' + encodeURIComponent(token.id);
          link.className = 'discussion-mention';
        } else {
          link.href = token.href;
          link.target = '_blank';
          link.rel = 'noopener noreferrer';
        }
        content.appendChild(link);
      }
    }
    body.replaceChildren(content);
    body.dataset.rendered = 'true';
  }
  function insert(textarea, text, start = textarea.selectionStart, end = textarea.selectionEnd) {
    if (textarea.value.length - (end - start) + text.length > textarea.maxLength) {
      return false;
    }
    textarea.setRangeText(text, start, end, 'end');
    textarea.focus();
    textarea.dispatchEvent(new Event('input', {bubbles: true}));
    return true;
  }
  function enhanceEditor(textarea) {
    if (textarea.dataset.enhanced) {
      return;
    }
    textarea.dataset.enhanced = 'true';
    const decoded = decodeMentions(textarea.value);
    let mentions = decoded.mentions;
    let previous = decoded.text;
    textarea.value = previous;
    const bodyInput = document.createElement('input');
    bodyInput.type = 'hidden';
    bodyInput.name = 'body';
    textarea.name = 'bodyDisplay';
    textarea.after(bodyInput);
    const synchronize = () => {
      mentions = moveMentions(previous, textarea.value, mentions);
      previous = textarea.value;
      bodyInput.value = encodeMentions(previous, mentions);
      textarea.setCustomValidity(bodyInput.value.length > textarea.maxLength
          ? discussion.dataset.lengthError || 'Message too long' : '');
    };
    textarea.addEventListener('input', synchronize);
    textarea.form?.addEventListener('submit', synchronize);
    synchronize();
    const tools = element('div', 'discussion-editor-tools');
    const emojiButton = tool(discussion.dataset.emojiLabel, 'emoji');
    const mentionButton = tool(discussion.dataset.mentionLabel, 'mention');
    const picker = element('div', 'discussion-emoji-picker');
    picker.hidden = true;
    picker.setAttribute('role', 'group');
    picker.setAttribute('aria-label', discussion.dataset.emojiLabel);
    emojiButton.setAttribute('aria-expanded', 'false');
    let selection = [textarea.selectionStart, textarea.selectionEnd];
    const saveSelection = () => { selection = [textarea.selectionStart, textarea.selectionEnd]; };
    textarea.addEventListener('select', saveSelection);
    textarea.addEventListener('keyup', saveSelection);
    textarea.addEventListener('click', saveSelection);
    textarea.addEventListener('input', saveSelection);
    for (const emoji of EMOJI) {
      const button = document.createElement('button');
      button.type = 'button';
      button.textContent = emoji;
      button.setAttribute('aria-label', emoji);
      button.addEventListener('click', () => {
        insert(textarea, emoji, selection[0], selection[1]);
        picker.hidden = true;
        emojiButton.setAttribute('aria-expanded', 'false');
      });
      picker.appendChild(button);
    }
    emojiButton.addEventListener('click', () => {
      picker.hidden = !picker.hidden;
      emojiButton.setAttribute('aria-expanded', String(!picker.hidden));
    });
    mentionButton.addEventListener('click', () => insert(textarea, '@', selection[0], selection[1]));
    tools.append(emojiButton, mentionButton, picker);
    textarea.after(tools);

    const list = element('div', 'discussion-mention-list');
    list.id = 'discussion-mentions-' + (++listNumber);
    list.hidden = true;
    list.setAttribute('role', 'listbox');
    list.setAttribute('aria-label', discussion.dataset.mentionLabel);
    textarea.setAttribute('aria-controls', list.id);
    textarea.setAttribute('aria-autocomplete', 'list');
    textarea.setAttribute('aria-expanded', 'false');
    tools.appendChild(list);
    let active = 0;
    let buttons = [];
    let query;
    const close = () => {
      list.hidden = true;
      textarea.setAttribute('aria-expanded', 'false');
      textarea.removeAttribute('aria-activedescendant');
    };
    const highlight = () => {
      buttons.forEach((button, index) => button.setAttribute('aria-selected', String(index === active)));
      if (buttons[active]) {
        textarea.setAttribute('aria-activedescendant', buttons[active].id);
      }
    };
    const update = () => {
      query = mentionQuery(textarea.value, textarea.selectionStart);
      list.replaceChildren();
      buttons = [];
      if (!query) {
        close();
        return;
      }
      for (const person of filterPeople(people, query.query)) {
        const button = element('button', 'discussion-mention-option');
        button.id = list.id + '-' + buttons.length;
        button.type = 'button';
        button.setAttribute('role', 'option');
        button.textContent = '@' + person.name;
        button.addEventListener('mousedown', event => event.preventDefault());
        button.addEventListener('click', () => {
          const name = person.name.replace(/[\[\]\r\n]/g, ' ');
          const start = query.start;
          if (insert(textarea, '@' + name + ' ', start, query.end)) {
            mentions.push({start, end: start + name.length + 1, name, id: person.id});
            synchronize();
          }
          close();
        });
        list.appendChild(button);
        buttons.push(button);
      }
      active = 0;
      list.hidden = buttons.length === 0;
      textarea.setAttribute('aria-expanded', String(!list.hidden));
      highlight();
    };
    textarea.addEventListener('input', update);
    textarea.addEventListener('keydown', event => {
      if (event.key === 'Escape') {
        close();
        picker.hidden = true;
        emojiButton.setAttribute('aria-expanded', 'false');
      } else if (!list.hidden && ['ArrowDown', 'ArrowUp', 'Enter'].includes(event.key)) {
        event.preventDefault();
        if (event.key === 'Enter') {
          buttons[active].click();
        } else {
          active = (active + (event.key === 'ArrowDown' ? 1 : -1) + buttons.length) % buttons.length;
          highlight();
        }
      }
    });
    textarea.addEventListener('blur', close);
  }
  function enhance(container) {
    container.querySelectorAll('.discussion-avatar img').forEach(image => {
      if (image.complete && image.naturalWidth === 0) {
        image.remove();
      }
    });
    container.querySelectorAll('.discussion-body').forEach(renderBody);
    container.querySelectorAll('textarea[name="body"]').forEach(enhanceEditor);
  }
  root.DiscussionEditor = {enhance};
  document.addEventListener('DOMContentLoaded', () => {
    discussion = document.getElementById('discussion');
    if (!discussion) {
      return;
    }
    const directory = document.getElementById('discussion-mention-directory');
    if (directory) {
      people = Array.from(directory.content.querySelectorAll('[data-user-id]'), node => ({id: node.dataset.userId, name: node.dataset.name}));
    }
    document.addEventListener('error', event => {
      if (event.target.matches?.('#discussion .discussion-avatar img')) {
        event.target.remove();
      }
    }, true);
    enhance(discussion);
    discussion.addEventListener('submit', async event => {
      const form = event.target;
      if (!form.matches('.discussion-like-form')) {
        return;
      }
      event.preventDefault();
      const button = form.querySelector('button');
      if (button.disabled) {
        return;
      }
      const input = form.querySelector('input[name="liked"]');
      const error = form.querySelector('.discussion-like-error');
      button.disabled = true;
      error.hidden = true;
      try {
        const response = await sp.ajaxRequest(form.action).byPostMethod().withParam('liked', input.value).send();
        const parsed = new DOMParser().parseFromString(response.responseText, 'text/html');
        const state = parsed.querySelector('.discussion-like-state');
        if (!state || !['true', 'false'].includes(state.dataset.liked) || !/^\d+$/.test(state.dataset.count)) {
          throw new Error('Invalid like response');
        }
        const liked = state.dataset.liked === 'true';
        button.setAttribute('aria-pressed', String(liked));
        const label = liked ? form.dataset.unlikeLabel : form.dataset.likeLabel;
        button.setAttribute('aria-label', label);
        button.title = label;
        button.querySelector('.discussion-like-count').textContent = state.dataset.count;
        input.value = String(!liked);
      } catch (failure) {
        error.hidden = false;
      } finally {
        button.disabled = false;
        form.dispatchEvent(new CustomEvent('discussion-reaction-settled', {bubbles: true}));
      }
    });
  });
})(globalThis);
