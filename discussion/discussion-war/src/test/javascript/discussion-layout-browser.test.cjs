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
const assets = path.join(__dirname, '../../main/webapp/discussion/jsp');
const rows = Array.from({length:100},(_,i)=>`<article class="discussion-message"><div class="discussion-message-content"><header>Author ${i}</header><p class="discussion-body">A long conversation message ${i}</p></div></article>`).join('');
const people = Array.from({length:50},(_,i)=>`<li class="discussion-person"><span class="discussion-person-name">Participant ${i}</span></li>`).join('');
async function open(browser,width,height) {
  const page = await browser.newPage({viewport:{width,height}});
  await page.setContent('<style>body{margin:0}nav{height:70px}iframe{display:block;width:100%;height:calc(100vh - 70px);border:0}</style><nav>Silverpeas navigation</nav><iframe></iframe>');
  const frame = page.frames()[1];
  await frame.setContent(`<!DOCTYPE html><style>body{margin:8px;font:16px Arial}.breadcrumb{height:62px}.tableFrame{padding:0 12px 12px;margin-bottom:8px}.milieuFrame{padding-bottom:6px}</style><div class="breadcrumb">Ressources &gt; Discussion</div><div class="tableFrame"><div class="milieuFrame"><main id="discussion"><header class="discussion-header"><div><h1>Discussion sur l'IA</h1><p class="discussion-subtitle">Projet IA dans Silverpeas</p></div><div class="discussion-reading">1 Messages non lus</div></header><div class="discussion-workspace"><div class="discussion-stream"><div id="discussion-scroll" class="discussion-scroll" tabindex="0"><section id="discussion-history">${rows}</section></div><form class="discussion-composer"><label>Écrire un message</label><textarea name="body">Brouillon conservé</textarea><div class="discussion-editor-tools">☺ @</div><div class="discussion-composer-footer"><span>2 000 caractères maximum</span><button type="button">Publier</button></div></form></div><aside class="discussion-sidebar"><div id="discussion-participants"><div class="discussion-participants-list"><h2>Auteurs de la discussion</h2><ul>${people}</ul></div></div></aside></div></main></div></div>`);
  await frame.addStyleTag({path:path.join(assets,'css/discussion.css')});
  await frame.addScriptTag({path:path.join(assets,'javaScript/discussion-layout.js')});
  await frame.evaluate(()=>document.dispatchEvent(new Event('DOMContentLoaded')));
  await frame.waitForFunction(()=>document.getElementById('discussion').style.getPropertyValue('--discussion-available-height'));
  return {page,frame};
}
async function check(frame) {
  const result=await frame.evaluate(()=>{
    const box=selector=>document.querySelector(selector).getBoundingClientRect();
    return {viewport:innerHeight,documentHeight:document.documentElement.scrollHeight,root:box('#discussion').bottom,button:box('.discussion-composer button').bottom,history:box('#discussion-scroll').height,authors:box('.discussion-sidebar h2').top,scrolls:document.getElementById('discussion-scroll').scrollHeight>document.getElementById('discussion-scroll').clientHeight};
  });
  assert.ok(result.documentHeight<=result.viewport+1,JSON.stringify(result));
  assert.ok(result.button<=result.viewport,JSON.stringify(result));
  assert.ok(result.history>40,JSON.stringify(result));
  assert.ok(result.scrolls);
}

test('an embedded discussion keeps heading, authors and publish button visible while messages scroll',async()=>{
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH||undefined,args:['--no-sandbox']});
  try {
    const {page,frame}=await open(browser,1440,900);
    await check(frame);
    const before=await frame.locator('.discussion-header').boundingBox();
    const authors=await frame.locator('.discussion-sidebar h2').boundingBox();
    await frame.locator('#discussion-scroll').evaluate(node=>{node.scrollTop=1800;});
    assert.deepEqual(await frame.locator('.discussion-header').boundingBox(),before);
    assert.deepEqual(await frame.locator('.discussion-sidebar h2').boundingBox(),authors);
    await frame.locator('.discussion-sidebar ul').evaluate(node=>{node.scrollTop=600;});
    assert.deepEqual(await frame.locator('.discussion-sidebar h2').boundingBox(),authors);
    assert.equal(await frame.locator('textarea').inputValue(),'Brouillon conservé');
    await page.setViewportSize({width:1100,height:600});
    await frame.waitForFunction(()=>document.getElementById('discussion').getBoundingClientRect().bottom<=innerHeight);
    await check(frame);
    await page.setViewportSize({width:390,height:844});
    await frame.waitForFunction(()=>getComputedStyle(document.querySelector('.discussion-sidebar')).gridRowStart==='1');
    await check(frame);
  } finally {await browser.close();}
});

test('frame padding and changing native navigation are included in the available height',async()=>{
  const browser=await chromium.launch({executablePath:process.env.CHROME_PATH||undefined,args:['--no-sandbox']});
  try {
    const {frame}=await open(browser,1280,800);
    const old=await frame.locator('#discussion').evaluate(node=>node.clientHeight);
    await frame.locator('.breadcrumb').evaluate(node=>{node.style.height='110px';});
    await frame.waitForFunction(old=>document.getElementById('discussion').clientHeight<old,old);
    await check(frame);
  } finally {await browser.close();}
});
