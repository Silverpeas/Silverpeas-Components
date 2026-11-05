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
const contentTypes = {'.js':'application/javascript', '.svg':'image/svg+xml', '.css':'text/css'};
const row = (scope, name, count, archived=false) => `<li data-channel-scope="${scope}" data-unread="${count}" data-archived="${archived}"><a class="discussion-channel-link" href="/channels/${scope}/Main"># ${name}</a><button class="discussion-icon-button discussion-channel-settings" type="button" title="Gérer ce canal" aria-label="Gérer ce canal" hidden><svg class="discussion-icon" aria-hidden="true"><use href="/assets/icons/discussion-icons.svg#settings"/></svg></button></li>`;
const rows = (count = 3, archived=true) => `<div class="discussion-channel-groups"><ul class="discussion-channel-list">${row('main','Général',0)}${row('projects','Projets',count)}${archived?'':row('old','Ancien projet',0)}</ul>${archived?`<details id="discussion-channel-archives" class="discussion-channel-archives"><summary>Archives <span>1</span></summary><ul class="discussion-channel-list">${row('old','Ancien projet',9,true)}</ul></details>`:''}</div>`;
const fixture = scope => `<!doctype html><html><head><meta charset="utf-8"><link rel="stylesheet" href="/assets/css/discussion.css"><style>body{margin:0;font:16px Arial}</style><script>
window.handlers=new Map();window.DiscussionContext={embedded:false,eventId:name=>name};
window.spServerEventSource={isEnabled:()=>true,addEventListener:(name,fn,id)=>handlers.set(id,{name,fn}),removeEventListener:()=>{}};
window.calls=[];window.sp={ajaxRequest:url=>({send:async()=>{calls.push(url);return {responseText:window.newRows||${JSON.stringify(rows())}};}})};
</script><script src="/assets/javaScript/discussion-view-state.js" defer></script><script src="/assets/javaScript/discussion-channels.js" defer></script><script src="/assets/javaScript/discussion-layout.js" defer></script></head><body>
<main id="discussion" data-instance-id="discussion1" data-user-id="2" data-channel-scope="${scope}" data-channels="/channels">
<header class="discussion-header"><button id="discussion-channels-toggle" aria-expanded="false" aria-controls="discussion-channels" title="Afficher les canaux" aria-label="Afficher les canaux"><svg class="discussion-icon" aria-hidden="true"><use href="/assets/icons/discussion-icons.svg#menu"/></svg><span id="discussion-channels-unread" hidden></span></button><div class="discussion-heading"><h1>Discussion</h1><p id="discussion-channel-title">${scope}</p></div></header>
<nav id="discussion-channels" class="discussion-channels" hidden><button id="discussion-channels-close" aria-label="Replier la liste des canaux"><svg><use href="/assets/icons/discussion-icons.svg#panel-collapse"/></svg></button><div id="discussion-channel-list">${rows()}</div><button id="discussion-channel-create" aria-label="Créer un canal" title="Créer un canal"><svg class="discussion-icon" aria-hidden="true"><use href="/assets/icons/discussion-icons.svg#plus"/></svg></button><p id="discussion-channels-error" hidden>Erreur</p></nav>
<dialog id="discussion-channel-dialog"><form data-create="/channels/create" data-update="/channels/${scope}/settings"><input name="name" data-current="${scope}" required maxlength="80"><textarea name="description" data-current="Description"></textarea><label><input name="archive" type="checkbox" data-main="${scope==='main'}" data-current="${scope==='old'}">Archiver</label><input name="archived" type="hidden" value="false"><button type="button" class="discussion-channel-cancel">Annuler</button></form></dialog>
<div class="discussion-workspace"><div class="discussion-stream"><div class="discussion-scroll"><section id="discussion-history"><article>Un message de ${scope}</article></section></div><form class="discussion-composer"><textarea id="discussion-body" name="body"></textarea><input type="file" name="images" multiple hidden><input type="file" name="files" multiple hidden></form></div><aside class="discussion-sidebar">Participants</aside></div></main></body></html>`;
async function setup(width=1280) {
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  const page=await browser.newPage({viewport:{width,height:850}});
  page.setDefaultTimeout(5000);
  await page.route('http://discussion.test/**',route=>{
    const url=new URL(route.request().url());
    if(url.pathname.startsWith('/assets/'))return route.fulfill({contentType:contentTypes[path.extname(url.pathname)],body:fs.readFileSync(path.join(assets,url.pathname.slice(8)))});
    if(url.pathname==='/')return route.fulfill({contentType:'text/html',body:'<iframe id="app" style="width:100%;height:800px;border:0" src="/channels/main/Main"></iframe>'});
    return route.fulfill({contentType:'text/html',body:fixture(url.pathname.split('/')[2])});
  });
  await page.goto('http://discussion.test/');
  const frame=()=>page.frames().find(f=>f.url().includes('/channels/'));
  await page.frameLocator('#app').locator('#discussion-channels-toggle').waitFor();
  return {browser,page,frame};
}
test('channel drawer starts open and shifts the desktop conversation without horizontal overflow',async()=>{
  const {browser,frame}=await setup();
  try {
    const app=frame();
    assert.equal(await app.locator('#discussion-channels').isVisible(),true);
    assert.equal(await app.locator('#discussion-channels-unread').textContent(),'3');
    const after=await app.locator('.discussion-stream').boundingBox();
    await app.locator('#discussion-channels-close').click();
    const before=await app.locator('.discussion-stream').boundingBox();
    assert.ok(after.width < before.width-150);
    await app.locator('#discussion-channels-toggle').click();
    assert.equal(await app.locator('a[aria-current="page"]').textContent(),'# Général');
    assert.equal(await app.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true);
    await app.locator('#discussion-channels-close').click();
    assert.equal(await app.locator('#discussion-channels-toggle').getAttribute('aria-expanded'),'false');
  } finally {await browser.close();}
});
test('switching channels in the native iframe restores its text and attachment draft',async()=>{
  const {browser,page,frame}=await setup();
  try {
    await frame().locator('#discussion-body').fill('Brouillon général');
    await frame().locator('input[name=files]').setInputFiles({name:'draft.txt',mimeType:'text/plain',buffer:Buffer.from('draft')});
    await frame().locator('a[href="/channels/projects/Main"]').click();
    await page.waitForFunction(()=>document.getElementById('app').contentWindow.location.pathname.includes('/projects/'));
    await frame().locator('#discussion-body').fill('Brouillon projets');
    await frame().locator('a[href="/channels/main/Main"]').click();
    await page.waitForFunction(()=>document.getElementById('app').contentWindow.location.pathname.includes('/main/'));
    await frame().waitForFunction(()=>document.getElementById('discussion-body').value==='Brouillon général');
    await frame().waitForFunction(()=>document.querySelector('input[name=files]').files.length===1);
    assert.equal(await frame().locator('input[name=files]').evaluate(n=>n.files[0].name),'draft.txt');
    assert.equal(await frame().locator('#discussion-channels').isVisible(),true);
  } finally {await browser.close();}
});
test('mobile drawer overlays the conversation, closes on escape and keeps the viewport width',async()=>{
  const {browser,frame}=await setup(390);
  try {
    const app=frame();
    assert.equal(await app.locator('.discussion-workspace').evaluate(n=>n.inert),true);
    assert.equal(await app.evaluate(()=>document.documentElement.scrollWidth<=innerWidth),true);
    await app.locator('#discussion-channels-close').focus();
    await app.evaluate(()=>document.dispatchEvent(new KeyboardEvent('keydown',{key:'Escape',bubbles:true})));
    assert.equal(await app.locator('#discussion-channels').isVisible(),false);
    assert.equal(await app.locator('.discussion-workspace').evaluate(n=>n.inert),false);
  } finally {await browser.close();}
});
test('native change events refresh channel unread counters and management preserves general channel',async()=>{
  const {browser,frame}=await setup();
  try {
    const app=frame();
    await app.evaluate(html=>{window.newRows=html;for(const h of handlers.values()){if(h.name==='DISCUSSION_CHANGED')h.fn({data:JSON.stringify({instanceId:'discussion1'})});}},rows(8));
    await app.waitForFunction(()=>document.getElementById('discussion-channels-unread').textContent==='8');
    await app.locator('.discussion-channel-settings:visible').click();
    assert.equal(await app.locator('input[name=archive]').isDisabled(),true);
    await app.locator('.discussion-channel-cancel').click();
    await app.locator('#discussion-channel-create').click();
    assert.equal(await app.locator('#discussion-channel-dialog form').getAttribute('action'),'/channels/create');
    assert.equal(await app.locator('input[name=name]').inputValue(),'');
  } finally {await browser.close();}
});

test('archives start collapsed, do not inflate active unread counts and retain their open state on live refresh', async () => {
  const {browser, frame}=await setup();
  try {
    const app=frame();
    assert.equal(await app.locator('#discussion-channel-archives').evaluate(n=>n.open),false);
    assert.equal(await app.locator('a[href="/channels/old/Main"]').isVisible(),false);
    assert.equal(await app.locator('#discussion-channels-unread').textContent(),'3');
    assert.equal((await app.locator('#discussion-channels-toggle').textContent()).trim(),'3');
    assert.equal(await app.locator('#discussion-channels-toggle').getAttribute('aria-label'),'Replier la liste des canaux');
    assert.equal(await app.locator('#discussion-channel-create').textContent(),'');
    assert.equal(await app.locator('.discussion-channel-settings:visible').count(),1);
    assert.equal(await app.locator('.discussion-channel-settings:visible').textContent(),'');
    await app.locator('#discussion-channel-archives > summary').click();
    await app.evaluate(html=>{window.newRows=html;for(const h of handlers.values()){if(h.name==='DISCUSSION_CHANGED')h.fn({data:JSON.stringify({instanceId:'discussion1'})});}},rows(6));
    await app.waitForFunction(()=>document.getElementById('discussion-channels-unread').textContent==='6');
    assert.equal(await app.locator('#discussion-channel-archives').evaluate(n=>n.open),true);
    assert.equal(await app.locator('a[href="/channels/old/Main"]').isVisible(),true);
    await app.locator('.discussion-channel-settings:visible').click();
    assert.equal(await app.locator('#discussion-channel-dialog').evaluate(n=>n.open),true);
  } finally { await browser.close(); }
});

test('an archived channel can be selected and reactivated from its own gear', async () => {
  const {browser,page,frame}=await setup();
  try {
    await frame().locator('#discussion-channel-archives > summary').click();
    await frame().locator('a[href="/channels/old/Main"]').click();
    await page.waitForFunction(()=>document.getElementById('app').contentWindow.location.pathname.includes('/old/'));
    const app=frame();
    assert.equal(await app.locator('#discussion-channel-archives').evaluate(n=>n.open),false);
    await app.locator('#discussion-channel-archives > summary').click();
    await app.locator('.discussion-channel-settings:visible').click();
    assert.equal(await app.locator('input[name=archive]').isChecked(),true);
    assert.equal(await app.locator('input[name=archive]').isDisabled(),false);
    assert.equal(await app.locator('#discussion-channel-dialog form').getAttribute('action'),'/channels/old/settings');
    await app.locator('input[name=archive]').uncheck();
    assert.equal(await app.locator('input[name=archived]').inputValue(),'false');
    await app.locator('.discussion-channel-cancel').click();
    await app.evaluate(html=>{window.newRows=html;for(const h of handlers.values()){if(h.name==='DISCUSSION_CHANGED')h.fn({data:JSON.stringify({instanceId:'discussion1'})});}},rows(5,false));
    await app.waitForFunction(()=>!document.getElementById('discussion-channel-archives'));
    assert.equal(await app.locator('a[href="/channels/old/Main"]').isVisible(),true);
    assert.equal(await app.locator('a[href="/channels/old/Main"]').getAttribute('aria-current'),'page');
  } finally { await browser.close(); }
});


test('embedded replies retain the full workspace width without a channel drawer', async () => {
  const {browser,frame}=await setup();
  try {
    const app=frame();
    await app.evaluate(()=>document.documentElement.classList.add('discussion-embedded'));
    assert.equal(await app.locator('#discussion-channels').isVisible(),false);
    assert.equal(await app.locator('.discussion-workspace').evaluate(n=>getComputedStyle(n).marginLeft),'0px');
  } finally {await browser.close();}
});


test('channel drawer preference survives reloads and channel changes with matching accessibility and layout state', async () => {
  const {browser, frame} = await setup();
  try {
    let app = frame();
    await app.locator('#discussion-channels-close').click();
    await app.goto('http://discussion.test/channels/projects/Main');
    await app.locator('#discussion-channels-toggle').waitFor();
    assert.equal(await app.locator('#discussion-channels').isVisible(), false);
    assert.equal(await app.locator('#discussion-channels-toggle').getAttribute('aria-expanded'), 'false');
    assert.equal(await app.locator('#discussion').evaluate(node => node.classList.contains('discussion-channels-open')), false);
    await app.locator('#discussion-channels-toggle').click();
    await app.goto('http://discussion.test/channels/main/Main');
    await app.locator('#discussion-channels-toggle').waitFor();
    assert.equal(await app.locator('#discussion-channels').isVisible(), true);
    assert.equal(await app.locator('#discussion-channels-toggle').getAttribute('aria-expanded'), 'true');
  } finally {await browser.close();}
});
