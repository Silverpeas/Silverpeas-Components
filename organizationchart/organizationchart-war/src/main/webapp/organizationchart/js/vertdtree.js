/*--------------------------------------------------|
| dTree 2.05 | www.destroydrop.com/javascript/tree/ |
|---------------------------------------------------|
| Copyright (c) 2002-2003 Geir Landr�               |

| This script can be used freely as long as all     |
| copyright messages are intact.                    |

| Updated: 28.06.2006 by Mahmoud Tahoon           
|--------------------------------------------------*/

// Node object
function Node(id, pid, name, url, title, target, icon, iconOpen, open, firstLevel, style) {
	this.id = id;
	this.pid = pid;
	this.name = name;
	this.url = url;
	this.title = title;
	this.target = target;
	this.icon = icon;
	this.iconOpen = iconOpen;
	this.firstLevel = firstLevel;
	this.style = style;
	this._io = open || false;
	this._is = false; // if this node is selected or not
	this._ls = false; // is this node is the last sibling
	this._hc = false; // if this node have children or not
	this._ai = 0;
	this._p;            // the parent node of this node
	this._children = 0; // Number of children this node have
	this._childrenFL = 0; // Number of first level children this node have
	this._isLeft =  false; // Is this node is a very left child
	this._isRight = false; // Is this node is a very right child
	this._isOnly = false;  // Is this node is the only child for its parent
	this._nodeIndex = 0; // the index of this node in the childs array of its parent
};

// Tree object
function dTree(objName) {
	this.config = {
		target			: null,
		folderLinks		: true,
		useSelection	: false,
		useCookies		: true,
		useLines		: true,
		useIcons		: true,
		useStatusText	: false,
		closeSameLevel	: false,
		inOrder			: false
	};

	this.icon = {
		root			: organizationchartPath + 'img/bigboss.png',
		folder			: organizationchartPath + 'img/service.png',
		folderOpen		: organizationchartPath + 'img/boss.png',
		node			: organizationchartPath + 'img/homme.png',
		empty			: organizationchartPath + 'img/empty.gif',
		line			: organizationchartPath + 'img/line.gif',
		join			: organizationchartPath + 'img/join.gif',
		joinBottom		: organizationchartPath + 'img/joinbottom.gif',
		plus			: organizationchartPath + 'img/plus.gif',
		plusBottom		: organizationchartPath + 'img/plusbottom.gif',
		minus			: organizationchartPath + 'img/minus.gif',
		minusBottom		: organizationchartPath + 'img/minusbottom.gif',
		nlPlus			: organizationchartPath + 'img/nolines_plus.gif',
		nlMinus			: organizationchartPath + 'img/nolines_minus.gif',
		middleLine		: organizationchartPath + 'img/myline2.gif',
		leftLine		: organizationchartPath + 'img/myline2.gif',
		smallLine		: organizationchartPath + 'img/smallLine.gif',
		rightLine		: organizationchartPath + 'img/myline2.gif'
	};

	this.obj = objName;
	this.aNodes = [];
	this.aIndent = [];
	this.root = new Node(-1);
	this.selectedNode = null;
	this.selectedFound = false;
	this.completed = false;
};

// Adds a new node to the node array
dTree.prototype.add = function(id, pid, name, url, title, target, icon, iconOpen, open, firstLevel, style) {
	this.aNodes[this.aNodes.length] = new Node(id, pid, '&nbsp;'+name+'&nbsp;', url, title, target, 
			icon, iconOpen, open, firstLevel, style);
};

// Open/close all nodes
dTree.prototype.openAll = function() {
	this.oAll(true);
};

dTree.prototype.closeAll = function() {
	this.oAll(false);
};

// Outputs the tree to the page
dTree.prototype.toString = function() {
	var str = '<div class="dtree" id="' + this.obj + '">\n';
	if (document.getElementById) {
		if (this.config.useCookies) this.selectedNode = this.getSelected();
		str += this.addNode(this.root);
	} else str += 'Browser not supported.';
	str += '</div>';

	if (!this.selectedFound) this.selectedNode = null;
	this.completed = true;
	return str;
};

// resize the tree on a node
dTree.prototype.resizeon = function(index) {
	//-2 pour revenir à la racine
	if(index == -2) {
		document.getElementById(this.obj).innerHTML = this.addNode(this.root);
	} else {
		var curNode = this.aNodes[index];
		var str = '<table border=0 cellpadding=0 cellspacing=0 >';
		str += '	<tr>';
		str += '	<td valign=top align=center >';
		str += this.node(curNode, index);
		str += '	</td>';
		str += '	</tr>';
		str += '	</table>';
		document.getElementById(this.obj).innerHTML = str;
	}
}

// Creates the tree structure
dTree.prototype.addNode = function(pNode) {
	var str = '';
	str += '<table border=0 cellpadding=0 cellspacing=0 >';
	str += '	<tr>';
	var n=0;

	if (this.config.inOrder) n = pNode._ai;
	//alert("NODES LENGTH: "+this.aNodes.length+" N="+n+" NAME: "+pNode.name);
	var childsIndex =0;
	if(pNode._childrenFL > 0) {
		str += '<td align="center" colspan="7"><table><tr>';
	for (n; n<this.aNodes.length; n++) {
		if (this.aNodes[n].pid == pNode.id && this.aNodes[n].firstLevel) {
			var curNode = this.aNodes[n];
			curNode._p = pNode;
			curNode._ai = n;
			this.setCS(curNode); // Checks if a node has any children and if it is the last sibling
			curNode._childIndex = childsIndex++;
			if(childsIndex > 6 && (childsIndex % 6) == 1 ) {
				//on force le retour à la ligne tous les 6 enfants
				str += '</tr><tr>';
			}
			if(pNode._childrenFL == 1) {
				if(pNode._children > 1)
					curNode._isLeft=true;
				else 
					curNode._isOnly=true;
			}
			else {
				if(curNode._childIndex == 0 || (childsIndex % 6) == 1)
					curNode._isLeft=true;
				if(curNode._childIndex == curNode._p._childrenFL-1 || (childsIndex % 6) == 0)
					curNode._isRight=true;
			}

			if (!curNode.target && this.config.target) curNode.target = this.config.target;
			if (curNode._hc && !curNode._io && this.config.useCookies) curNode._io = this.isOpen(curNode.id);
			if (!this.config.folderLinks && curNode._hc) curNode.url = null;
			if (this.config.useSelection && curNode.id == this.selectedNode && !this.selectedFound) {
				curNode._is = true;
				this.selectedNode = n;
				this.selectedFound = true;
			}

			if((childsIndex % 6) == 4 ) {
				var numLigne = Math.ceil(childsIndex/6);
				if( curNode._p._childrenFL > 6*numLigne) {
					str += '	<td valign=top align=center width="1%" ';
					str += 'style="background-image:url('+this.icon.smallLine +
						');background-repeat:repeat-y;background-position:middle">&nbsp;</td>';
				}
				else if(numLigne > 1 && pNode._children > 0) {
					str += '	<td valign=top align=center width="1%" ';
					str += 'style="background-image:url('+this.icon.smallLine +
						');background-repeat:repeat-y;background-position:middle">&nbsp;</td>';
				}
			}
			
			if(pNode._childrenFL > 1 && pNode._childrenFL < 7 && pNode._children > 0) {
				if((pNode._childrenFL % 2) == 0) {
					if(childsIndex == pNode._childrenFL / 2 + 1) {
						str += '	<td valign=top align=center width="1%" ';
						str += 'style="background-image:url('+this.icon.smallLine +
							');background-repeat:repeat-y;background-position:middle">&nbsp;</td>';
					}
				} else {
					if(childsIndex == (pNode._childrenFL +1) / 2 + 1) {
						str += '	<td valign=top align=center width="1%" ';
						str += 'style="background-image:url('+this.icon.smallLine +
							');background-repeat:repeat-y;background-position:middle">&nbsp;</td>';
					}
				}
			}
			
			str += '	<td valign=top align=center ';
			if(pNode._childrenFL ==1)
				str += 'width="50%"';
			str += '>';
			str += this.node(curNode, n);
			str += '	</td>';
			if(pNode._childrenFL ==1 && pNode._children > 1) {
				str += '	<td valign=top align=center width="1%" ';
				str += 'style="background-image:url('+this.icon.smallLine +
					');background-repeat:repeat-y;background-position:middle">&nbsp;</td>';
				str += '<td width="50%" ></td>'
			}
			
			if (curNode._p._childrenFL == childsIndex) {
				if(childsIndex > 6) {
					if((childsIndex % 6) == 1)
						str += ' <td style="border-top-width:1px;border-top-style:dotted;border-top-color:Gray;">&nbsp;</td>';
					if((childsIndex % 6) == 1 || (childsIndex % 6) == 2) {
						str += ' <td style="border-top-width:1px;border-top-style:dotted;border-top-color:Gray;">&nbsp;</td>';
						str += '	<td valign=top align=center width="1%" ';
						str += 'style="background-image:url('+this.icon.smallLine +
						');background-repeat:repeat-y;background-position:middle">&nbsp;</td>';
					}
				}
				break;
			}
		}
	}
	str += '</tr></table></td></tr><tr>';
	}
	childsIndex =0;n=0;
	if (this.config.inOrder) n = pNode._ai;
	for (n; n<this.aNodes.length; n++) {
		if (this.aNodes[n].pid == pNode.id && !this.aNodes[n].firstLevel) {
			var curNode = this.aNodes[n];
			curNode._p = pNode;
			curNode._ai = n;
			this.setCS(curNode); // Checks if a node has any children and if it is the last sibling
			curNode._childIndex = childsIndex++;
			if(childsIndex > 6 && (childsIndex % 6) == 1 ) {
				//on force le retour à la ligne tous les 6 enfants
				str += '</tr><tr>';
			}
			if(curNode._p._children <= 1)
				curNode._isOnly=true;
			else {
				if(curNode._childIndex == 0 || (childsIndex % 6) == 1)
					curNode._isLeft=true;
				if((curNode._childIndex == curNode._p._children-1 && (curNode._p._children < 7 || childsIndex % 6 > 3)) || (childsIndex % 6) == 0)
					curNode._isRight=true;
			}

			if (!curNode.target && this.config.target) curNode.target = this.config.target;
			if (curNode._hc && !curNode._io && this.config.useCookies) curNode._io = this.isOpen(curNode.id);
			if (!this.config.folderLinks && curNode._hc) curNode.url = null;
			if (this.config.useSelection && curNode.id == this.selectedNode && !this.selectedFound) {
				curNode._is = true;
				this.selectedNode = n;
				this.selectedFound = true;
			}

			if((childsIndex % 6) == 4 ) {
				var numLigne = Math.ceil(childsIndex/6);
				if( curNode._p._children > 6*numLigne) {
					str += '	<td valign=top align=center width="1%" '
					str += 'style="background-image:url('+this.icon.smallLine +
						');background-repeat:repeat-y;background-position:middle">&nbsp;</td>';
				}
				else if(numLigne > 1) {
					str += '<td width="1%" style="border-top-width:1px;border-top-style:dotted;border-top-color:Gray;"></td>';
				}
			}
			
			str += '	<td valign=top align=center >';
			str += this.node(curNode, n);
			str += '	</td>';
			if (curNode._ls) {
				if(childsIndex > 6) {
					if((childsIndex % 6) == 1 || (childsIndex % 6) == 2)
						str += ' <td style="border-top-width:1px;border-top-style:dotted;border-top-color:Gray;">&nbsp;</td>';
					if((childsIndex % 6) == 1)
						str += ' <td style="border-top-width:1px;border-top-style:dotted;border-top-color:Gray;">&nbsp;</td>';
				}
				break;
			}
		}
	}
	str += '	</tr>';
	str += '</table>';
	return str;
};

// Creates the node icon, url and text
dTree.prototype.node = function(node, nodeId) {
	var str = '<div class="dTreeNode" style="white-space:nowrap">';
	str += '<table border="0" cellpadding="0" cellspacing="0" width="100%" >';
	str += '<tr>';
	str += '	<td align="center" width="52%" ';
	if (this.root.id != node.pid) {
		if(this.config.useLines) {
			if(node._isOnly) {
				str += '';	
			} else if(node._isLeft) {
				str += '';
			} else if(node._isRight) {
				str += ' style="border-top-width:1px;border-top-style:dotted;border-top-color:Gray;" ';
			} else {
				str += ' style="border-top-width:1px;border-top-style:dotted;border-top-color:Gray;" ';
			}
		}
	}
	str += ' >&nbsp; ';
	str += '	</td>';
	str += '	<td valign="top" style="padding-top:0px;" align="center" width="1%" >';
	
	if (this.root.id != node.pid) {/// if this node isn't a first lever node
		str += '<img src="';
		if(this.config.useLines) {
			if(node._isOnly)
				str += this.icon.line;
			else if(node._isLeft)
				str += this.icon.leftLine;
			else if(node._isRight)
				str += this.icon.rightLine;
			else
				str += this.icon.middleLine;
		} else {
			str += this.icon.empty;
		}
		str += '" alt="" />';
	}

	str += '	</td>';
	str += '	<td align="center" width="52%" ';

	if (this.root.id != node.pid) {
		if(this.config.useLines) {
			if(node._isOnly)
				str += ' ';
			else if(node._isLeft)
				str += ' style="border-top-width:1px;border-top-style:dotted;border-top-color:Gray;" ';
			else if(node._isRight)
				str += '';
			else
				str += ' style="border-top-width:1px;border-top-style:dotted;border-top-color:Gray;" ';
		}
	}
	
	str += ' >&nbsp; ';
	str += '	</td>';
	str += '</tr>';
	str += '<tr>';
	str += '	<td align="center" colspan="3"> <table><tr>';
	str += '	<td align="center" style="border-width:1px;border-style:solid;border-color:Gray;" >';
	str += '<table border=0 bordercolor=blue cellpadding="0" cellspacing="0" class="'+node.style +'">';
	str += '<tr><td valign=top align=center>' + this.indent(node, nodeId);

	if (this.config.useIcons) {
		if (!node.icon) node.icon = (this.root.id == node.pid) ? this.icon.root : ((node._hc) ? this.icon.folder : this.icon.node);
		if (!node.iconOpen) node.iconOpen = (node._hc) ? this.icon.folderOpen : this.icon.node;
		if (this.root.id == node.pid) {
			node.icon = this.icon.root;
			node.iconOpen = this.icon.root;
		}
		if(node._hc) {
			str += '<a href="javascript: ' + this.obj + '.resizeon(' + nodeId + ');" title="Centrer dessus">';
			str += '<img  id="i' + this.obj + nodeId + '" src="' + ((node._io) ? node.iconOpen : node.icon) + '" />';
			str += '</a>';
		} else 
			str += '<img id="i' + this.obj + nodeId + '" src="' + ((node._io) ? node.iconOpen : node.icon) + '" alt="" />';
	}
	str+="</td></tr><tr><td valign=top align=center style='white-space:nowrap'>";
	if (node.url) {
		str += '<a id="s' + this.obj + nodeId + '" class="' + 
			((this.config.useSelection) ? ((node._is ? 'nodeSel' : 'node')) : 'node') + '" href="' + node.url + '"';
		if (node.title) str += ' title="' + node.title + '"';
		if (node.target) { 
			if(node.target == "popup")
				str += ' onclick="var w=window.open(this.href,\'popupWindow\',\'width=450,height=300,left=320,top=150\');w.focus();return false;"';
			else str += ' target="' + node.target + '"';
		}
		if (this.config.useStatusText) str += ' onmouseover="window.status=\'' + node.name + 
			'\';return true;" onmouseout="window.status=\'\';return true;" ';
		if (this.config.useSelection && ((node._hc && this.config.folderLinks) || !node._hc))
			str += ' onclick="javascript: ' + this.obj + '.s(' + nodeId + ');"';
		str += '>';
	}
	//<a onclick=">&nbsp;</a>
	else if ((!this.config.folderLinks || !node.url) && node._hc && node.pid != this.root.id)
		str += '<a href="javascript: ' + this.obj + '.o(' + nodeId + ');" class="node">';

	str += node.name;
	if (node.url) str += '</a>';
	//|| ((!this.config.folderLinks || !node.url) && node._hc)
	str += '</td></tr></table>';
	str += '</td></tr></table>';
	str += '</td></tr></table>';
	str += '</div>';

	if (node._hc) {
		str += '<div id="d' + this.obj + nodeId + '" class="clip" style="display:' + ((node._io) ? 'block' : 'none') + ';">';
		str += '<table border=0 cellpadding=0 cellspacing=0>';
		str += '<tr><td height="9" align="center"><img src="'+this.icon.smallLine+'" alt="" border=0></td></tr>';
		str += '<tr><td>';
		str += this.addNode(node);
		str += '</td></tr></table>';
		str += '</div>';
	}

	this.aIndent.pop();
	return str;
};

// Adds the empty and line icons
dTree.prototype.indent = function(node, nodeId) {
	var str = '';
	(node._ls) ? this.aIndent.push(0) : this.aIndent.push(1);
	if (node._hc) {
		str += '<a href="javascript: ' + this.obj + '.o(' + nodeId + ');"><img id="j' + this.obj + nodeId + '" src="';
		str += (node._io) ? this.icon.nlMinus : this.icon.nlPlus;
		str += '" alt="" /></a>';
	}
	return str;
};

// Checks if a node has any children and if it is the last sibling
dTree.prototype.setCS = function(node) {
	var lastId;
	var children =0;
	var firstChildren = 0;
	for (var n=0; n<this.aNodes.length; n++) {
		if (this.aNodes[n].pid == node.id)
		{
		 node._hc = true;
		 if(this.aNodes[n].firstLevel)
			 firstChildren++;
		 else
			 children ++;
		}
		if (this.aNodes[n].pid == node.pid) lastId = this.aNodes[n].id;
	}
	node._children = children;
	node._childrenFL = firstChildren;
	if (lastId==node.id) node._ls = true;
};

// Returns the selected node
dTree.prototype.getSelected = function() {
	var sn = this.getCookie('cs' + this.obj);
	return (sn) ? sn : null;
};

// Highlights the selected node
dTree.prototype.s = function(id) {
	if (!this.config.useSelection) return;
	var cn = this.aNodes[id];
	if (cn._hc && !this.config.folderLinks) return;
	if (this.selectedNode != id) {
		if (this.selectedNode || this.selectedNode==0) {
			eOld = document.getElementById("s" + this.obj + this.selectedNode);
			eOld.className = "node";
		}
		eNew = document.getElementById("s" + this.obj + id);
		eNew.className = "nodeSel";
		this.selectedNode = id;
		if (this.config.useCookies) this.setCookie('cs' + this.obj, cn.id);
	}
};

// Toggle Open or close
dTree.prototype.o = function(id) {
	var cn = this.aNodes[id];
	this.nodeStatus(!cn._io, id, cn._ls);
	cn._io = !cn._io;
	if (this.config.closeSameLevel) this.closeLevel(cn);
	if (this.config.useCookies) this.updateCookie();
};

// Open or close all nodes
dTree.prototype.oAll = function(status) {
	for (var n=0; n<this.aNodes.length; n++) {
		if (this.aNodes[n]._hc && this.aNodes[n].pid != this.root.id) {
			this.nodeStatus(status, n, this.aNodes[n]._ls);
			this.aNodes[n]._io = status;
		}
	}
	if (this.config.useCookies) this.updateCookie();
};

// Opens the tree to a specific node
dTree.prototype.openTo = function(nId, bSelect, bFirst) {
	if (!bFirst) {
		for (var n=0; n<this.aNodes.length; n++) {
			if (this.aNodes[n].id == nId) {
				nId=n;
				break;
			}
		}
	}
	var cn=this.aNodes[nId];
	if (cn.pid==this.root.id || !cn._p) return;
	cn._io = true;
	cn._is = bSelect;
	if (this.completed && cn._hc) this.nodeStatus(true, cn._ai, cn._ls);
	if (this.completed && bSelect) this.s(cn._ai);
	else if (bSelect) this._sn=cn._ai;
	this.openTo(cn._p._ai, false, true);
};

// Closes all nodes on the same level as certain node
dTree.prototype.closeLevel = function(node) {
	for (var n=0; n<this.aNodes.length; n++) {
		if (this.aNodes[n].pid == node.pid && this.aNodes[n].id != node.id && this.aNodes[n]._hc) {
			this.nodeStatus(false, n, this.aNodes[n]._ls);
			this.aNodes[n]._io = false;
			this.closeAllChildren(this.aNodes[n]);
		}
	}
};

// Closes all children of a node
dTree.prototype.closeAllChildren = function(node) {
	for (var n=0; n<this.aNodes.length; n++) {
		if (this.aNodes[n].pid == node.id && this.aNodes[n]._hc) {
			if (this.aNodes[n]._io) this.nodeStatus(false, n, this.aNodes[n]._ls);
			this.aNodes[n]._io = false;
			this.closeAllChildren(this.aNodes[n]);
		}
	}
};

// Change the status of a node(open or closed)
dTree.prototype.nodeStatus = function(status, id, bottom) {
	eDiv	= document.getElementById('d' + this.obj + id);
	eJoin	= document.getElementById('j' + this.obj + id);
	if (this.config.useIcons) {
		eIcon	= document.getElementById('i' + this.obj + id);
		eIcon.src = (status) ? this.aNodes[id].iconOpen : this.aNodes[id].icon;
	}
	eJoin.src = ((status)?this.icon.nlMinus:this.icon.nlPlus);
	eDiv.style.display = (status) ? 'block': 'none';
};

// [Cookie] Clears a cookie
dTree.prototype.clearCookie = function() {
	var now = new Date();
	var yesterday = new Date(now.getTime() - 1000 * 60 * 60 * 24);
	this.setCookie('co'+this.obj, 'cookieValue', yesterday);
	this.setCookie('cs'+this.obj, 'cookieValue', yesterday);
};

// [Cookie] Sets value in a cookie
dTree.prototype.setCookie = function(cookieName, cookieValue, expires, path, domain, secure) {
	document.cookie = escape(cookieName) + '=' + escape(cookieValue)
		+ (expires ? '; expires=' + expires.toGMTString() : '')
		+ (path ? '; path=' + path : '')
		+ (domain ? '; domain=' + domain : '')
		+ (secure ? '; secure' : '');
};

// [Cookie] Gets a value from a cookie
dTree.prototype.getCookie = function(cookieName) {
	var cookieValue = '';
	var posName = document.cookie.indexOf(escape(cookieName) + '=');
	if (posName != -1) {
		var posValue = posName + (escape(cookieName) + '=').length;
		var endPos = document.cookie.indexOf(';', posValue);
		if (endPos != -1) cookieValue = unescape(document.cookie.substring(posValue, endPos));
		else cookieValue = unescape(document.cookie.substring(posValue));
	}
	return (cookieValue);
};

// [Cookie] Returns ids of open nodes as a string
dTree.prototype.updateCookie = function() {
	var str = '';
	for (var n=0; n<this.aNodes.length; n++) {
		if (this.aNodes[n]._io && this.aNodes[n].pid != this.root.id) {
			if (str) str += '.';
			str += this.aNodes[n].id;
		}
	}
	this.setCookie('co' + this.obj, str);
};

// [Cookie] Checks if a node id is in a cookie
dTree.prototype.isOpen = function(id) {
	var aOpen = this.getCookie('co' + this.obj).split('.');
	for (var n=0; n<aOpen.length; n++)
		if (aOpen[n] == id) return true;
	return false;
};

// If Push and pop is not implemented by the browser
if (!Array.prototype.push) {
	Array.prototype.push = function array_push() {
		for(var i=0;i<arguments.length;i++)
			this[this.length]=arguments[i];
		return this.length;
	};
};

if (!Array.prototype.pop) {
	Array.prototype.pop = function array_pop() {
		lastElement = this[this.length-1];
		this.length = Math.max(this.length-1,0);
		return lastElement;
	};
};
