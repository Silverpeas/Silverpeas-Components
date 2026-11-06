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
const assets = path.join(__dirname, '../../main/webapp/discussion/jsp');
const messages = fs.readFileSync(path.join(assets, 'messages.jsp'), 'utf8');
const files = fs.readFileSync(path.join(assets, 'files.jsp'), 'utf8');
const editor = fs.readFileSync(path.join(assets, 'file-editor.jsp'), 'utf8').match(/<button[\s\S]*?<\/button>/)[0];
const escape = text => text.replaceAll('&', '&amp;').replaceAll('<', '&lt;').replaceAll('"', '&quot;');
function render(source, name) {
  return source.replace(/<jsp:include page="file-editor.jsp"\/>/g, editor)
    .replace(/<c:out value=['"]\$\{file\.(name|url|displaySize)}['"]\/>/g,
      (_, property) => escape({name, url:'/attached/file', displaySize:'37 KB'}[property]))
    .replace(/<fmt:message key=['"]([^'"]+)['"]\/>/g, (_, key) => key)
    .replace(/\$\{[^}]+}/g, expression => expression.includes('name') ? escape(name) : 'file1')
    .replace(/<c:(?:if|set|remove)\b[^>]*>/g, '').replace(/<\/c:if>/g, '');
}
for (const sidebar of [false, true]) {
  test('file actions stay on one row with a wrapping filename in ' + (sidebar ? 'the sidebar' : 'a message'), async () => {
    const browser = await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
    try {
      const page = await browser.newPage();
      const row = sidebar ? files.match(/<li>[\s\S]*?<\/li>/)[0]
        : messages.match(/<li><a href=[\s\S]*?<\/li>/)[0];
      const name = 'Trame_visite_entreprise_' + 'nom_de_document_tres_long_'.repeat(5) + '.docx';
      await page.setContent('<main id="discussion"><ul class="' + (sidebar ? 'discussion-files-list' : 'discussion-message-files') + '">' + render(row, name) + '</ul></main>');
      await page.addStyleTag({path:path.join(assets, 'css/discussion.css')});
      // Reproduce the native theme rule which can place each action on its own line.
      await page.addStyleTag({content:'body{margin:0}button{display:block;margin:4px 0}#discussion{width:100%;padding:8px;box-sizing:border-box}'});
      for (const width of sidebar ? [250,190] : [1100,640,320]) {
        await page.setViewportSize({width,height:600});
        const bounds = await page.locator('.discussion-file-actions button').evaluateAll(buttons => buttons.map(button => {
          const {x,y,width,height} = button.getBoundingClientRect(); return {x,y,width,height};
        }));
        assert.equal(bounds.length, 2);
        assert.ok(Math.abs(bounds[0].y + bounds[0].height / 2 - bounds[1].y - bounds[1].height / 2) < 1, 'same vertical alignment at ' + width);
        assert.ok(bounds[1].x >= bounds[0].x + bounds[0].width, 'no overlapping controls');
        assert.ok(bounds.every(box => box.width > 0 && box.x >= 0 && box.x + box.width <= width), 'controls fit at ' + width);
        assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true, 'no horizontal overflow at ' + width);
        assert.equal(await page.locator('a[download]').textContent().then(value => value.trim()), name);
      }
      await page.locator('.discussion-view-file').focus();
      await page.keyboard.press('Tab');
      assert.equal(await page.locator('.discussion-edit-file').evaluate(button => button === document.activeElement), true);
    } finally {await browser.close();}
  });
}
