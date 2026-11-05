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
const test = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const {chromium} = require('playwright');
const sharedEditor = require('./shared-editor-assets.cjs');
const assets = path.join(__dirname, '../../main/webapp/discussion/jsp');
const source = fs.readFileSync(path.join(assets, 'messages.jsp'), 'utf8');
const bundle = fs.readFileSync(path.join(__dirname, '../../../../discussion-configuration/src/main/config/properties/org/silverpeas/components/discussion/multilang/DiscussionBundle_fr.properties'), 'utf8');
const labels = Object.fromEntries(bundle.split('\n').filter(line => line.startsWith('discussion.') && line.includes('='))
  .map(line => [line.slice(0, line.indexOf('=')).trim(), line.slice(line.indexOf('=') + 1).trim()]));
labels['GML.cancel'] = 'Annuler';
const pixel = 'data:image/gif;base64,R0lGODlhAQABAIAAAAAAAP///yH5BAEAAAAALAAAAAABAAEAAAIBRAA7';
function form(images, body, files) {
  let template = source.match(/<details class="discussion-edit">[\s\S]*?<\/details>/)[0];
  const block = /<c:if test="\$\{not empty messageImages\[message.id\]}">[\s\S]*?<\/c:if>/;
  const figure = template.match(/<figure class="discussion-edit-image">[\s\S]*?<\/figure>/)[0];
  template = template.replace(block, '<div class="discussion-edit-images">' + images.map(id => figure
    .replace(/<c:out value='\$\{image.url}'\/>/g, pixel)
    .replace(/\$\{fn:escapeXml\(image.id\)}/g, id)).join('') + '</div>');
  return template.replace(/<fmt:message key=['"]([^'"]+)['"]\s*\/>/g, (_, key) => labels[key])
    .replace(/<c:out value="\$\{message.body}"\s*\/>/g, body)
    .replace(/\$\{([^}]+)}/g, (_, key) => {
      if (key === 'not empty messageFiles[message.id]') { return String(files); }
      if (key.includes('not empty messageImages')) { return String(images.length > 0 || files); }
      return {'message.version':'3', 'message.id':'m1', 'messageActionBase':'/messages/', 'discussionIcons':'/icons.svg'}[key] || 'Modifier';
    });
}
async function setup({images = ['photo1', 'photo2'], body = 'Message', files = false} = {}) {
  const browser = await chromium.launch({executablePath:process.env.CHROME_PATH, args:['--no-sandbox']});
  try {
    const page = await browser.newPage({viewport:{width:800, height:850}});
    await page.setContent('<base href="https://discussion.test/"><main id="discussion" data-emoji-label="Emoji" data-mention-label="Mention" data-icons="/icons.svg" data-profile-base="/profile"><article class="discussion-message"><div class="discussion-message-content"><div class="discussion-body">' + body + '</div><div class="discussion-message-images"><img src="' + pixel + '" alt="original"/></div><div class="discussion-message-footer"><div class="discussion-actions">' + form(images, body, files) + '</div></div></div></article><section id="later"></section></main>');
    await page.evaluate(labels => {
      window.posts = [];
      document.addEventListener('submit', event => {
        event.preventDefault();
        const values = new FormData(event.target);
        posts.push({removed:values.getAll('removedImageIds'), version:values.get('version'), body:values.get('body')});
      });
      for (const [key,value] of Object.entries(labels)) {
        if (key.startsWith('discussion.format')) { document.getElementById('discussion').dataset[key.slice(11)] = value; }
      }
    }, labels);
    await page.addStyleTag({path:path.join(assets, 'css/discussion.css')});
    await sharedEditor.load(page);
    for (const file of ['discussion-markdown.js','discussion-rich-editor.js','discussion-editor.js','discussion-editing.js']) {
      await page.addScriptTag({path:path.join(assets, 'javaScript', file)});
    }
    await page.evaluate(() => document.dispatchEvent(new Event('DOMContentLoaded')));
    await page.locator('.discussion-message').hover();
    await page.locator('.discussion-edit summary').click();
    await page.locator('.discussion-edit .tiptap').waitFor();
    return {browser, page};
  } catch (error) {await browser.close(); throw error;}
}

test('image removal is reversible and only selected IDs are submitted when saving', async () => {
  const {browser, page} = await setup();
  try {
    assert.equal(await page.locator('.discussion-message-images').isVisible(), false);
    assert.deepEqual(await page.evaluate(() => posts), []);
    const remove = page.getByRole('button', {name:'Retirer cette image', exact:true});
    await remove.first().click();
    assert.equal(await page.locator('.discussion-edit-image--removed').count(), 1);
    assert.deepEqual(await page.locator('.discussion-edit form').evaluate(form => new FormData(form).getAll('removedImageIds')), ['photo1']);
    await page.getByRole('button', {name:'Conserver cette image', exact:true}).click();
    assert.deepEqual(await page.locator('.discussion-edit form').evaluate(form => new FormData(form).getAll('removedImageIds')), []);
    await remove.last().click();
    await page.getByRole('button', {name:labels['discussion.save'], exact:true}).click();
    assert.deepEqual(await page.evaluate(() => posts), [{removed:['photo2'], version:'3', body:'Message'}]);
  } finally {await browser.close();}
});

test('cancel restores the text and every image without submitting a removal', async () => {
  const {browser, page} = await setup();
  try {
    await page.getByRole('button', {name:'Retirer cette image', exact:true}).first().click();
    await page.locator('.tiptap').fill('Edited');
    await page.getByRole('button', {name:'Annuler', exact:true}).click();
    await page.locator('.discussion-message-images').waitFor({state:'visible'});
    assert.equal(await page.locator('.discussion-message-images').isVisible(), true);
    await page.locator('.discussion-message').hover();
    await page.locator('.discussion-edit summary').click();
    assert.equal(await page.locator('.tiptap').textContent(), 'Message');
    assert.equal(await page.locator('.discussion-edit-image--removed').count(), 0);
    assert.deepEqual(await page.locator('.discussion-edit form').evaluate(form => new FormData(form).getAll('removedImageIds')), []);
    assert.deepEqual(await page.evaluate(() => posts), []);
  } finally {await browser.close();}
});

test('the last image cannot leave an empty message but remaining files allow removal', async () => {
  for (const files of [false, true]) {
    const {browser, page} = await setup({images:['photo1'], body:' ', files});
    try {
      await page.getByRole('button', {name:'Retirer cette image', exact:true}).click();
      await page.getByRole('button', {name:labels['discussion.save'], exact:true}).click();
      assert.equal(await page.evaluate(() => posts.length), files ? 1 : 0);
      if (!files) {
        await page.getByRole('button', {name:'Conserver cette image', exact:true}).click();
        await page.getByRole('button', {name:labels['discussion.save'], exact:true}).click();
        assert.equal(await page.evaluate(() => posts.length), 1);
      }
    } finally {await browser.close();}
  }
});
