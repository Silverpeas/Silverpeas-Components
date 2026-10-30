/* Copyright (C) 2000 - 2026 Silverpeas. AGPL v3 with Silverpeas FLOSS exception. */
const test = require('node:test');
const assert = require('node:assert/strict');
const path = require('node:path');
const {chromium} = require('playwright');

const rows = (start, end, unreadAt = 0) => Array.from({length: end-start+1}, (_,index) => {
  const sequence = start+index;
  return (sequence === unreadAt ? '<div class="discussion-unread-divider">Unread</div>' : '') +
    `<article id="message-${sequence}" class="discussion-message" data-sequence="${sequence}">Message ${sequence}</article>`;
}).join('');

async function open(browser, unreadAt = 0, hash = '') {
  const page = await browser.newPage();
  await page.setContent(`<!DOCTYPE html><style>.discussion-message{height:72px}.discussion-unread-divider{height:24px}#discussion #discussion-scroll{height:250px;min-height:0;overflow:auto;overflow-anchor:none}</style>
    <main id="discussion" data-history="/history"><div id="discussion-scroll">
      <button id="discussion-older">Older</button><p id="discussion-error" hidden>Error</p>
      <section id="discussion-history"><div class="discussion-page" data-before="31" data-more="true">${rows(31,60,unreadAt)}</div></section>
    </div><textarea id="draft">Draft being written</textarea></main>`);
  await page.addStyleTag({path:path.join(__dirname,'../../main/webapp/discussion/jsp/css/discussion.css')});
  await page.evaluate(hash => {
    if(hash) window.location.hash = hash;
    window.historyRequests = [];
    window.sp = {ajaxRequest: () => ({withParam: (name,value) => ({send: () => {
      window.historyRequests.push(value);
      return new Promise(resolve => { window.releaseHistory = text => resolve({responseText: text}); });
    }})})};
  }, hash);
  await page.addScriptTag({path: path.join(__dirname,'../../main/webapp/discussion/jsp/javaScript/discussion.js')});
  await page.evaluate(() => document.dispatchEvent(new Event('DOMContentLoaded')));
  await page.evaluate(() => new Promise(resolve => requestAnimationFrame(resolve)));
  return page;
}

test('older messages prepend chronologically while preserving the visible position and draft', async () => {
  const browser = await chromium.launch({executablePath:process.env.CHROME_PATH || undefined,headless:true,args:['--no-sandbox']});
  try {
    const page = await open(browser);
    assert.equal(await page.evaluate(() => document.getElementById('discussion-scroll').scrollTop + 250 >= document.getElementById('discussion-scroll').scrollHeight),true);
    assert.deepEqual(await page.evaluate(() => window.historyRequests),[]);
    await page.evaluate(() => { document.getElementById('discussion-scroll').scrollTop=0; });
    await page.waitForFunction(() => !!window.releaseHistory);
    await page.evaluate(() => { document.getElementById('discussion-scroll').scrollTop=120; });
    const before = (await page.locator('#message-35').boundingBox()).y;
    await page.evaluate(fragment => window.releaseHistory(fragment), `<div class="discussion-page" data-before="1" data-more="false">${rows(1,30)}</div>`);
    await page.waitForSelector('#message-1');
    const after = (await page.locator('#message-35').boundingBox()).y;
    assert.ok(Math.abs(before-after)<2,`Anchor stayed at ${before} / ${after}`);
    const sequences = await page.locator('.discussion-message').evaluateAll(nodes=>nodes.map(n=>Number(n.dataset.sequence)));
    assert.deepEqual(sequences, Array.from({length:60},(_,i)=>i+1));
    assert.equal(await page.locator('#discussion-older').isVisible(),false);
    assert.equal(await page.locator('#draft').inputValue(),'Draft being written');
    assert.deepEqual(await page.evaluate(()=>window.historyRequests),['31']);
  } finally { await browser.close(); }
});

test('opening prioritizes the unread boundary, while a message deep link takes precedence', async () => {
  const browser = await chromium.launch({executablePath:process.env.CHROME_PATH || undefined,headless:true,args:['--no-sandbox']});
  try {
    const page = await open(browser,45);
    const boundary = await page.locator('.discussion-unread-divider').boundingBox();
    const viewport = await page.locator('#discussion-scroll').boundingBox();
    assert.ok(Math.abs(boundary.y-viewport.y-8)<2);
    await page.close();
    const linked = await open(browser,45,'message-60');
    const target = await linked.locator('#message-60').boundingBox();
    const visible = await linked.locator('#discussion-scroll').boundingBox();
    assert.ok(target.y>=visible.y && target.y+target.height<=visible.y+visible.height+1);
  } finally { await browser.close(); }
});
