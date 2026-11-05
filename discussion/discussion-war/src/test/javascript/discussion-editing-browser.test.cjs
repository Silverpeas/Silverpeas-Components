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
const sharedEditor = require('./shared-editor-assets.cjs');
const test=require('node:test');
const assert=require('node:assert/strict');
const path=require('node:path');
const fs=require('node:fs');
const {chromium}=require('playwright');
const assets=path.join(__dirname,'../../main/webapp/discussion/jsp');
const props=fs.readFileSync(path.join(__dirname,'../../../../discussion-configuration/src/main/config/properties/org/silverpeas/components/discussion/multilang/DiscussionBundle_fr.properties'),'utf8');
const labels=Object.fromEntries(props.split('\n').filter(s=>s.startsWith('discussion.')&&s.includes('=')).map(s=>{const i=s.indexOf('=');return [s.slice(0,i).trim(),s.slice(i+1).trim()];}));
labels['GML.cancel']='Annuler';
const source=fs.readFileSync(path.join(assets,'messages.jsp'),'utf8');
const details=source.match(/<details class="discussion-edit">[\s\S]*?<\/details>/)[0]
    .replace(/<c:if test="\$\{not empty messageImages\[message.id\]}">[\s\S]*?<\/c:if>/g, '')
    .replace(/<fmt:message key=['"]([^'"]+)['"]\s*\/>/g,(_,key)=>labels[key])
    .replace(/<c:out value="\$\{message.body}"\s*\/>/g,'Bonjour @[Alice](2)')
    .replace(/\$\{([^}]+)}/g,(_,key)=>key==='message.version'?'1':key.includes('editLabel')?'Modifier':key==='messageActionBase'?'/messages/':key==='message.id'?'m1':key==='discussionIcons'?'/icons.svg':'false');
const article=id=>`<article class="discussion-message" id="${id}"><span class="discussion-avatar">A</span><div class="discussion-message-content"><div class="discussion-message-header">Alice</div><div class="discussion-body">Bonjour @[Alice](2)</div><div class="discussion-message-footer"><a href="#">Répondre</a><div class="discussion-actions">${details}<button type="button">Supprimer</button></div><form class="discussion-pin-form"><button type="button">Épingler</button></form><span class="discussion-readonly-likes">👍 0</span></div></div></article>`;
async function setup() {
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH,args:['--no-sandbox']});
  const page=await browser.newPage({viewport:{width:1100,height:850}});
  await page.setContent(`<base href="https://discussion.test/"><main id="discussion" data-emoji-label="Emoji" data-mention-label="Mention" data-icons="/icons.svg" data-profile-base="/profile"><div id="history">${article('message1')}</div><section id="later"></section></main>`);
  await page.evaluate(labels=>{
    for(const [key,value]of Object.entries(labels)){if(key.startsWith('discussion.format'))document.getElementById('discussion').dataset[key.slice(11)]=value;}
    window.posts=[];document.addEventListener('submit',event=>{event.preventDefault();posts.push({action:event.target.action,body:Object.fromEntries(new FormData(event.target))});});
  },labels);
  await page.addStyleTag({content:'body{margin:0;font:16px Arial}'+fs.readFileSync(path.join(assets,'css/discussion.css'),'utf8')});
  await sharedEditor.load(page);
  for(const file of ['vendor/markdown-it-15.0.3.min.js','discussion-markdown.js','discussion-editor.js','discussion-editing.js'])await page.addScriptTag({path:(file.startsWith('vendor/') ? sharedEditor.asset(file) : path.join(assets,'javaScript',file))});
  await page.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  return {browser,page};
}
async function open(page,selector='#message1') {
  await page.locator(selector).hover();
  await page.locator(selector+' .discussion-edit summary').click();
  await page.waitForFunction(selector=>document.querySelector(selector+' .discussion-edit').classList.contains('discussion-edit--inline'),selector);
}

test('the real edit form replaces the text at full content width and fits narrow frames',async()=>{
  const {browser,page}=await setup();
  try {
    await open(page);
    assert.equal(await page.locator('.discussion-edit > summary').isVisible(),false);
    assert.equal(await page.locator('.discussion-edit textarea').evaluate(n=>n===document.activeElement),true);
    for(const width of [1100,390,320]) {
      await page.setViewportSize({width,height:850});
      const content=await page.locator('.discussion-message-content').boundingBox();
      const form=await page.locator('.discussion-edit form').boundingBox();
      const header=await page.locator('.discussion-message-header').boundingBox();
      assert.ok(Math.abs(content.width-form.width)<2);
      assert.ok(Math.abs(content.x-form.x)<2);
      assert.ok(form.y>=header.y+header.height);
      assert.equal(await page.locator('.discussion-message-footer').isVisible(),false);
      assert.equal(await page.locator('.discussion-body').isVisible(),false);
      assert.equal(await page.locator('.discussion-message-footer .discussion-edit form').count(),0);
      assert.equal(await page.locator('.discussion-edit form').evaluate(n=>n.scrollWidth<=n.clientWidth),true);
    }
  } finally {await browser.close();}
});

test('cancel and Escape restore Markdown and canonical mention recipients without submitting',async()=>{
  const {browser,page}=await setup();
  try {
    await open(page);
    await page.locator('textarea').fill('Changement abandonné');
    await page.locator('.discussion-edit-cancel').click();
    await page.waitForFunction(()=>!document.querySelector('.discussion-edit').classList.contains('discussion-edit--inline'));
    assert.equal(await page.locator('.discussion-actions .discussion-edit').count(),1);
    assert.equal(await page.locator('input[name=body]').inputValue(),'Bonjour @[Alice](2)');
    assert.equal(await page.locator('summary').evaluate(n=>n===document.activeElement),true);
    await open(page);
    assert.equal(await page.locator('textarea').inputValue(),'Bonjour @Alice');
    await page.locator('textarea').fill('Autre changement');
    await page.keyboard.press('Escape');
    await page.waitForFunction(()=>!document.querySelector('.discussion-edit').classList.contains('discussion-edit--inline'));
    assert.equal(await page.locator('input[name=body]').inputValue(),'Bonjour @[Alice](2)');
    assert.equal(await page.evaluate(()=>posts.length),0);
  } finally {await browser.close();}
});

test('saving uses the unchanged native edit endpoint, version and formatted mention body',async()=>{
  const {browser,page}=await setup();
  try {
    await open(page);
    await page.locator('textarea').selectText();
    await page.keyboard.press('Control+b');
    await page.locator('.discussion-edit button[type=submit]').click();
    assert.equal(await page.evaluate(()=>posts[0].action),'https://discussion.test/messages/m1/edit');
    assert.equal(await page.evaluate(()=>posts[0].body.version),'1');
    assert.equal(await page.evaluate(()=>posts[0].body.body),'**Bonjour @[Alice](2)**');
  } finally {await browser.close();}
});

test('loaded history rows get the same inline editor and closing formatting alone does not cancel editing',async()=>{
  const {browser,page}=await setup();
  try {
    await page.locator('#later').evaluate((n,html)=>{n.innerHTML=html;DiscussionEditor.enhance(n);},article('message2'));
    await open(page,'#message2');
    await page.locator('#message2 .discussion-format-toggle').click();
    await page.locator('#message2 .discussion-format-bold').focus();
    await page.keyboard.press('Escape');
    assert.equal(await page.locator('#message2 .discussion-edit').evaluate(n=>n.open),true);
    assert.equal(await page.locator('#message2 .discussion-format-tools').isVisible(),false);
  } finally {await browser.close();}
});
