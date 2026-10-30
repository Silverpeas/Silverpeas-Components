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
const test=require('node:test');
const assert=require('node:assert/strict');
const path=require('node:path');
const {chromium}=require('playwright');
const launch=()=>chromium.launch({executablePath:process.env.CHROME_PATH||undefined,args:['--no-sandbox']});
async function open(browser,focused=true) {
  const page=await browser.newPage({viewport:{width:900,height:500}});
  page.setDefaultTimeout(5000);
  await page.setContent(`<style>body{margin:0}header{position:fixed;top:0;height:60px;background:white;width:100%}.discussion-composer{margin:0;position:fixed;bottom:0;height:80px;background:white;width:100%}.discussion-message{height:100px}#discussion-history{padding:60px 0 80px}.gap{height:1200px}</style><main id="discussion" data-read="/read"><header class="discussion-header">Discussion <span class="discussion-unread-count">2</span></header><section id="discussion-history"><article id="message-old" class="discussion-message discussion-message--unread">Older unseen</article><div class="gap"></div><article id="message-new" class="discussion-message discussion-message--unread"><span class="discussion-unread-badge">Unread</span>Newly displayed</article></section><form class="discussion-composer">Composer</form></main>`);
  await page.evaluate(focused=>{
    window.focused=focused;
    document.hasFocus=()=>window.focused;
    window.calls=[];
    window.sp={ajaxRequest:()=>({byPostMethod:()=>({withParam:(name,ids)=>({send:async()=>{
      window.calls.push(ids);
      return {responseText:'<span class="discussion-read-state" data-unread-count="1"></span>'};
    }})})})};
    window.scrollTo(0,document.body.scrollHeight);
  },focused);
  await page.addScriptTag({path:path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript/discussion-read.js')});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  return page;
}

test('sustained visibility acknowledges only the displayed message, leaving skipped older messages unread',async()=>{
  const browser=await launch();
  try {
    const page=await open(browser);
    await page.waitForFunction(()=>window.calls.length===1);
    assert.deepEqual(await page.evaluate(()=>window.calls),['new']);
    assert.equal(await page.locator('#message-old').evaluate(n=>n.classList.contains('discussion-message--unread')),true);
    assert.equal(await page.locator('#message-new').evaluate(n=>n.classList.contains('discussion-message--unread')),false);
    assert.equal(await page.locator('.discussion-unread-count').textContent(),'1');
    await page.waitForTimeout(1200);
    assert.equal(await page.evaluate(()=>window.calls.length),1);
  } finally {await browser.close();}
});

test('unfocused tabs and quickly passing messages are not acknowledged',async()=>{
  const browser=await launch();
  try {
    const page=await open(browser,false);
    await page.waitForTimeout(1200);
    assert.deepEqual(await page.evaluate(()=>window.calls),[]);
    await page.evaluate(()=>{window.focused=true;window.dispatchEvent(new Event('focus'));});
    await page.waitForTimeout(350);
    await page.evaluate(()=>window.scrollTo(0,0));
    await page.waitForTimeout(750);
    assert.deepEqual(await page.evaluate(()=>window.calls),[]);
    await page.waitForFunction(()=>window.calls.length===1);
    assert.deepEqual(await page.evaluate(()=>window.calls),['old']);
  } finally {await browser.close();}
});

test('messages inserted by live updates are observed without acknowledging content behind the composer',async()=>{
  const browser=await launch();
  try {
    const page=await open(browser,false);
    await page.evaluate(()=>{
      const row=document.getElementById('message-new');
      row.style.position='fixed';row.style.top='450px';row.style.height='30px';
      window.focused=true;window.dispatchEvent(new Event('focus'));
    });
    await page.waitForTimeout(1200);
    assert.deepEqual(await page.evaluate(()=>window.calls),[]);
    await page.evaluate(()=>{
      const row=document.createElement('article');
      row.id='message-live';row.className='discussion-message discussion-message--unread';
      row.style.position='fixed';row.style.top='100px';row.textContent='Live message';
      document.getElementById('discussion-history').append(row);
    });
    await page.waitForFunction(()=>window.calls.includes('live'));
    assert.deepEqual(await page.evaluate(()=>window.calls),['live']);
  } finally {await browser.close();}
});
