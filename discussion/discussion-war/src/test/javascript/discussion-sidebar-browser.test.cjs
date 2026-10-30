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
const {chromium} = require('playwright');
const launch = () => chromium.launch({executablePath:process.env.CHROME_PATH || undefined,args:['--no-sandbox']});
const fragment = (count, title='Projet IA') => `<div class="discussion-publications-list" data-count="${count}"><ul>${count ? '<li><a href="/silverpeas/Publication/12" target="_blank" rel="noopener noreferrer">'+title+'</a></li>' : ''}</ul></div>`;
async function open(browser) {
  const page = await browser.newPage();
  page.setDefaultTimeout(5000);
  await page.setContent('<main id="discussion" data-publications="/publications"><aside class="discussion-sidebar"><details id="participants" class="discussion-sidebar-section" name="discussion-sidebar" open><summary><h2>Participants <span>2</span></h2></summary><div class="discussion-sidebar-content"><p>Alice, Bob</p></div></details><details id="discussion-publications-section" class="discussion-sidebar-section" name="discussion-sidebar"><summary><h2>Publications <span id="discussion-publication-count">0</span></h2></summary><div class="discussion-sidebar-content"><div id="discussion-publications"></div><p id="discussion-publications-error" hidden>Error</p></div></details></aside></main>');
  await page.addStyleTag({path:path.join(__dirname,'../../main/webapp/discussion/jsp/css/discussion.css')});
  await page.evaluate(() => {
    window.calls=0;
    window.listeners={};
    window.spServerEventSource={isEnabled:()=>true,addEventListener:(name,fn)=>{window.listeners[name]=fn;},removeEventListener:name=>{delete window.listeners[name];}};
    window.sp={ajaxRequest:()=>({send:()=>{
      window.calls++;
      return new Promise((resolve,reject)=>{window.release=html=>resolve({responseText:html});window.fail=reject;});
    }})};
  });
  await page.addScriptTag({path:path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript/discussion-sidebar.js')});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  return page;
}

test('native accordion opens publications without losing state during refresh and removal',async()=>{
  const browser=await launch();
  try {
    const page=await open(browser);
    await page.evaluate(html=>window.release(html),fragment(1));
    await page.waitForFunction(()=>document.getElementById('discussion-publication-count').textContent==='1');
    await page.locator('#discussion-publications-section summary').click();
    await page.waitForFunction(()=>window.calls===2);
    await page.evaluate(html=>window.release(html),fragment(2,'Architecture &amp; IA'));
    await page.waitForFunction(()=>document.getElementById('discussion-publication-count').textContent==='2');
    assert.equal(await page.locator('#participants').getAttribute('open'),null);
    assert.equal(await page.locator('#discussion-publications-section').evaluate(node=>node.open),true);
    assert.equal(await page.locator('#discussion-publications a').textContent(),'Architecture & IA');
    assert.equal(await page.locator('#discussion-publications a').getAttribute('href'),'/silverpeas/Publication/12');
    await page.evaluate(()=>document.dispatchEvent(new Event('discussion:changed')));
    await page.waitForFunction(()=>window.calls===3);
    await page.evaluate(html=>window.release(html),fragment(0));
    await page.waitForFunction(()=>document.getElementById('discussion-publication-count').textContent==='0');
    assert.equal(await page.locator('#discussion-publications a').count(),0);
    assert.equal(await page.locator('#discussion-publications-section').evaluate(node=>node.open),true);
  } finally {await browser.close();}
});

test('citation updates coalesce in-flight changes, recover on reconnect and detach without closing the shared source',async()=>{
  const browser=await launch();
  try {
    const page=await open(browser);
    await page.evaluate(()=>{
      document.dispatchEvent(new Event('discussion:changed'));
      document.dispatchEvent(new Event('discussion:changed'));
    });
    assert.equal(await page.evaluate(()=>window.calls),1);
    await page.evaluate(html=>window.release(html),fragment(1));
    await page.waitForFunction(()=>window.calls===2);
    await page.evaluate(()=>window.fail(new Error('offline')));
    await page.waitForFunction(()=>!document.getElementById('discussion-publications-error').hidden);
    await page.evaluate(()=>{window.listeners.OPEN_EVENT_SOURCE();});
    await page.waitForFunction(()=>window.calls===3);
    await page.evaluate(html=>window.release(html),fragment(2));
    await page.waitForFunction(()=>document.getElementById('discussion-publications-error').hidden);
    await page.evaluate(()=>window.dispatchEvent(new Event('pagehide')));
    assert.deepEqual(await page.evaluate(()=>Object.keys(window.listeners)),[]);
    await page.evaluate(()=>document.dispatchEvent(new Event('discussion:changed')));
    assert.equal(await page.evaluate(()=>window.calls),3);
  } finally {await browser.close();}
});
