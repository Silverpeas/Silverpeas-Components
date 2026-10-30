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
const path = require('node:path');
const { chromium } = require('playwright');

test('safe rendering, emoji, keyboard mentions, and reversible likes work in the browser', async () => {
  const browser = await chromium.launch({executablePath:process.env.CHROME_PATH || undefined,headless:true,args:['--no-sandbox']});
  try {
    const page = await browser.newPage();
    await page.setContent(`<!DOCTYPE html><main id="discussion" data-icons="/icons.svg" data-profile-base="/Rprofil/jsp/Main" data-emoji-label="Emoji" data-mention-label="Mention">
      <template id="discussion-mention-directory"><span data-user-id="2" data-name="Alice"></span></template>
      <p class="discussion-body"></p><form><textarea name="body" maxlength="2000"></textarea></form>
      <form class="discussion-like-form" action="https://example.test/like" data-like-label="Like" data-unlike-label="Unlike">
        <input name="liked" value="true"><button type="submit" aria-pressed="false"><span class="discussion-like-count">0</span></button>
        <span class="discussion-like-error" hidden>Error</span>
      </form></main>`);
    await page.evaluate(() => {
      document.querySelector('.discussion-body').textContent = '<img src=x onerror=alert(1)> @[Alice](2) 😀 https://example.org';
      window.likeRequests = [];
      window.sp = {ajaxRequest: () => ({byPostMethod: () => ({withParam: (name,value) => ({send: async () => {
        window.likeRequests.push(value);
        return {responseText:`<span class="discussion-like-state" data-count="${value === 'true' ? 1 : 0}" data-liked="${value}"></span>`};
      }})})})};
    });
    await page.addScriptTag({path:path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript/discussion-editor.js')});
    await page.evaluate(() => document.dispatchEvent(new Event('DOMContentLoaded')));
    assert.equal(await page.locator('.discussion-body img, .discussion-body script').count(),0);
    assert.equal(await page.locator('.discussion-mention').getAttribute('href'),'/Rprofil/jsp/Main?userId=2');
    assert.equal(await page.locator('.discussion-body a[target="_blank"]').getAttribute('rel'),'noopener noreferrer');
    const editor = page.locator('textarea');
    await editor.fill('Bonjour @Al');
    await editor.press('Enter');
    assert.equal(await editor.inputValue(),'Bonjour @Alice ');
    await page.getByRole('button',{name:'Emoji',exact:true}).click();
    await page.getByRole('button',{name:'😀',exact:true}).click();
    assert.ok((await editor.inputValue()).endsWith('😀'));
    assert.ok((await page.locator('input[name="body"]').inputValue()).includes('@[Alice](2)'));
    assert.equal(await page.evaluate(()=>new FormData(document.querySelector('textarea').form).get('body')),await page.locator('input[name="body"]').inputValue());
    const like = page.locator('.discussion-like-form button');
    await like.click();
    assert.equal(await like.getAttribute('aria-pressed'),'true');
    assert.equal(await page.locator('.discussion-like-count').textContent(),'1');
    await like.click();
    assert.equal(await like.getAttribute('aria-pressed'),'false');
    assert.deepEqual(await page.evaluate(() => window.likeRequests),['true','false']);
    assert.ok((await editor.inputValue()).includes('@Alice'), 'Reacting preserves the draft');
    await browser.close();
  } finally {
    if(browser.isConnected()) await browser.close();
  }
});


test('editing saved mentions hides identifiers while preserving the submitted recipients and length limit', async () => {
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH || undefined,args:['--no-sandbox']});
  try {
    const page=await browser.newPage();
    await page.setContent('<main id="discussion" data-icons="/icons.svg" data-emoji-label="Emoji" data-mention-label="Mention"><form><textarea name="body" maxlength="2000">@[Bernard Lemoine](2) et @[Bernard Lemoine](3)</textarea></form></main>');
    await page.addScriptTag({path:path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript/discussion-editor.js')});
    await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
    const editor=page.locator('textarea');
    const display='@Bernard Lemoine et @Bernard Lemoine';
    assert.equal(await editor.inputValue(),display);
    await editor.fill('Salut '+display);
    assert.equal(await page.locator('input[name="body"]').inputValue(),'Salut @[Bernard Lemoine](2) et @[Bernard Lemoine](3)');
    const visible='Salut '+display;
    await editor.fill(visible+'x'.repeat(2000-visible.length));
    assert.equal(await editor.evaluate(node=>node.checkValidity()),false);
    assert.equal((await editor.inputValue()).includes('(2)'),false);
  } finally {await browser.close();}
});


test('personalized message avatars cover initials and failed portraits fall back to the initial', async () => {
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH || undefined,args:['--no-sandbox']});
  try {
    const page=await browser.newPage();
    await page.route('https://discussion.test/**',route=>{
      if(route.request().url().endsWith('/custom.png')) {
        return route.fulfill({contentType:'image/png',body:Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jRZkAAAAASUVORK5CYII=','base64')});
      }
      return route.fulfill({status:404,body:''});
    });
    await page.setContent('<base href="https://discussion.test/"><main id="discussion"><div class="discussion-avatar" id="custom"><span>A</span><img src="/custom.png" alt="" width="30" height="30"></div><div class="discussion-avatar" id="default"><span>B</span></div><div class="discussion-avatar" id="broken"><span>C</span><img src="/missing.png" alt=""></div></main>');
    await page.addStyleTag({path:path.join(__dirname,'../../main/webapp/discussion/jsp/css/discussion.css')});
    await page.addScriptTag({path:path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript/discussion-editor.js')});
    await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
    await page.waitForFunction(()=>document.querySelector('#custom img')?.naturalWidth>0);
    assert.equal(await page.locator('#default img').count(),0);
    assert.equal(await page.locator('#default span').textContent(),'B');
    await page.waitForFunction(()=>!document.querySelector('#broken img'));
    assert.equal(await page.locator('#broken span').isVisible(),true);
    assert.equal(await page.locator('#custom img').evaluate(node=>getComputedStyle(node).objectFit),'cover');
    await page.evaluate(()=>{
      const avatar=document.createElement('div');
      avatar.id='dynamic';avatar.className='discussion-avatar';
      avatar.innerHTML='<span>D</span><img src="/missing-later.png" alt="">';
      document.getElementById('discussion').append(avatar);
      window.DiscussionEditor.enhance(avatar);
    });
    await page.waitForFunction(()=>!document.querySelector('#dynamic img'));
    assert.equal(await page.locator('#dynamic span').textContent(),'D');
  } finally {await browser.close();}
});
