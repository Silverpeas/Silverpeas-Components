// TRICK to get all elements by class name
document.getElementsByClassName = function(cl) {
	var retnode = [];
	var myclass = new RegExp('\\b'+cl+'\\b');
	var elem = this.getElementsByTagName('*');
	for (var i = 0; i < elem.length; i++) {
		var classes = elem[i].className;
		if (myclass.test(classes))
			retnode.push(elem[i]);
	}
	return retnode;
};

// There are 3 kinds of cell
var CELL_TYPE_ORGANIZATION 	= 0;
var CELL_TYPE_CATEGORY 		= 1;
var CELL_TYPE_PERSON		= 2;

// Cell orientations
var ORIENTATION_HORIZONTAL 	= 0;
var ORIENTATION_VERTICAL 		= 1;
var ORIENTATION_RIGHT 		= 2;
var ORIENTATION_LEFT 			= 3;

var cellRightNumber = -1;
var cellRightOrigin = -1;
var cellLeftNumber = -1;
var cellLeftOrigin = -1;

var V_MARGIN = 20;
var H_MARGIN = 20;
var H_GAP = 50;
var V_GAP = 40; // was 60
var V_GAP_SIDEBOX = 300;
var H_GAP_SIDEBOX = V_GAP / 2;
var MAIN_DIV_BORDER_WIDTH = 3;
var START_OPACITY_STEP = 0;
var CELLSIZE = 200;

var mainDiv;
var cellsCount;
var maxLevel = 0;

var invisibleDiv;

var detailDiv;
var detailTable;
var detailCell;

var currentId = -1;
var currentX;
var currentY;
var focusOnDetailDiv = false;

//function JCell(id, name, infoSup, url, level, type, cellType, linkCenter, linkDetails, upperLink)
function JCell(options)
{
	// parameters
	// ----------
	this.id 			= options['id']; 				// id - use for links between cells
	this.title 			= options['title'];				// cell title
	this.roles 			= options['roles'];				// array of roles (only for organizations)
	this.userAttributes	= options['userAttributes'];	// array of attributes (only for persons)
	this.detailsURL 	= options['detailsURL'];		// URL to call when details link is clicked (only for organizations)
	this.catMembers 	= options['catMembers'];		// members of current category
	this.level 			= options['level']; 			// level for css
	this.cellType 		= options['cellType'];			// 0 = unit or 1 = category, personn
	this.showCenterLink = options['showCenterLink'];	// 1 if center link must be visible, 0 otherwise
	this.linkDetails	= options['showDetailsLink'];	// 1 if details link must be visible, 0 otherwise
	this.onClickURL 	= options['onClickURL'];		// URL to call when title link is clicked
	this.commonUserURL 	= options['commonUserURL'];		// URL to call when a user is clicked
	this.parentURL 		= options['parentURL'];			// URL to call when parent link is clicked
	this.className 		= options['className'];			// CSS class name
	this.usersIcon 		= options['usersIcon'];			// icon to display details Link

	// calculated variables
	// --------------------
	this.gaps = new Array();
	this.div = null;

    // horizontal -> left right
	this.leftCell = null;
	this.rightCell = null;

    // vertical -> top down
	this.topCell = null;
	this.downCell = null;

	this.upLinks = new Array();
	this.downLinks = new Array();
	this.startX = -1;
	this.endX = -1;
	this.startY = -1;
	this.endY = -1;
	this.downLinksAlreadyDone = 0;
}


function JLink(origin, destination, type, orientation)
{
	this.origin = origin;
	this.destination = destination;
	this.type = type;
	this.orientation = orientation;

	if(orientation == ORIENTATION_RIGHT) {
		cellRightNumber = destination;
    	cellRightOrigin = origin;
	}
	else if(orientation == ORIENTATION_LEFT) {
		cellLeftNumber = destination;
		cellLeftOrigin = origin;
	}
}

function chartinit()
{
	START_OPACITY_STEP = 15;

	mainDiv = document.getElementById("chart");
	cellsCount = jCells.length;
	var i;
	for (i = 0; i < cellsCount; i++)
	{
	  maxLevel = Math.max(maxLevel, jCells[i].level);
	}
	maxLevel++;

  buildCellDIVs();
  buildUpAndDownLinks();
  placeCells();
  buildLinks();


  var maxWidth = 0;
  var maxHeight = 0;
  var widthDiv = mainDiv.style.width;
  // supprime le px
  widthDiv = widthDiv.substring(0,widthDiv.length - 2);
  for (i = 0; i < jCells.length; i++)
  {
	  div = jCells[i].div;
	  maxWidth = Math.max(maxWidth, div.offsetLeft + div.offsetWidth + 2);
	  height = parseInt(div.style.top.substring(0, div.style.top.indexOf('px'))) + div.offsetHeight;
	  maxHeight = (maxHeight > height) ? maxHeight : height;
  }

  // on centre le scroll sur la case 0 (moitié de la largeur max moins un
  // demi écran moins une demi cellule)
  window.scroll( parseInt( maxWidth / 2 - screen.width / 2 + CELLSIZE / 2), 0);
  mainDiv.style.height = maxHeight;

  centerBoxesAndLinks();
}

function centerBoxesAndLinks() {
	var offsetX = mainDiv.offsetLeft + H_MARGIN;
	var offsetY = mainDiv.offsetTop + V_MARGIN;

	  for (i = 0; i < jCells.length; i++)
	  {
		  div = jCells[i].div;
		  div.style.marginLeft = offsetX;
		  div.style.marginTop = offsetY;
	  }

	  for (i=0; i<3; i++) {
		  var linklements = document.getElementsByClassName('link'+i);
		  for (j = 0; j < linklements.length; j++)
		  {
			  linklements[j].style.marginLeft = offsetX;
			  linklements[j].style.marginTop = offsetY;
		  }
	  }

}

/**
 * Build cells as  HTML DIVs
 */
function buildCellDIVs()
{
	var i;
	for (i = 0; i < cellsCount; i++)
	{
		buildCellDIV(jCells[i]);
	}
}

function buildCellDIV(jCell)
{
	// Main DIV
	var div = document.createElement("DIV");
	div.className = "cell"+jCell.className;

	// DIV Content as a HTML table
	var table = document.createElement("TABLE");

	// back link
	if (jCell.parentURL) {
		var backRow = table.insertRow(-1);
		var backCell = backRow.insertCell(-1);
		backCell.className = "cellInfos";
		backCell.colSpan = 2;
		backCell.align = "right";
		backCell.innerHTML = "<a href=\"" + jCell.parentURL + "\">^</a>";
	}

	// Title
	var titleRow = table.insertRow(-1);
	var titleCell = titleRow.insertCell(-1);
	titleCell.className = "cellName";
	titleCell.colSpan = 2;
	titleCell.innerHTML = getTitle(jCell);

	switch(jCell.cellType)
	{
	// for organizations, list roles
		case CELL_TYPE_ORGANIZATION:

			// Roles
			for (var i = 0; i < jCell.roles.length; i++) {
				var roleRow = table.insertRow(-1);
				var roleCell = roleRow.insertCell(-1);
				roleCell.className = "cellInfos";
				roleCell.colSpan = 2;
				roleCell.align = "center";
				roleCell.innerHTML = jCell.roles[i]['role'] + " : <a target=\"_blank\" href=\"" + jCell.commonUserURL + jCell.roles[i]['login'] + "\">" + jCell.roles[i]['userFullName'] + "</a>";
			}

			// Links
			var linksRow = table.insertRow(-1);

			var spacer = linksRow.insertCell(-1);
			spacer.className = "cellLinkLeft";
			spacer.innerHTML = "&nbsp;";

			var detailLinkCell = linksRow.insertCell(-1);
			detailLinkCell.className = "cellLinkRight";
			if (jCell.linkDetails) {
				if (jCell.usersIcon != '') {
					detailLinkCell.innerHTML = "<a href=\"" + jCell.detailsURL +"&chartType=1\"><img src='"+jCell.usersIcon+"' border='0'/></a>"
				}
				else {
					detailLinkCell.innerHTML = "<a href=\"" + jCell.detailsURL +"&chartType=1\">D&eacute;tails</a>"
				}
			}
			else {
				detailLinkCell.innerHTML = "&nbsp;"
			}
			break;

		case CELL_TYPE_CATEGORY:
			if (jCell.catMembers) {
				for (var i = 0; i < jCell.catMembers.length; i++) {
					var memberRow = table.insertRow(-1);
					var memberCell = memberRow.insertCell(-1);
					memberCell.className = "cellInfos";
					memberCell.colSpan = 2;
					memberCell.align = "center";
					memberCell.innerHTML = "<a target=\"_blank\" href=\"" + jCell.commonUserURL + jCell.catMembers[i]['login'] + "\">" + jCell.catMembers[i]['userFullName'] + "</a>";
				}
			}
			break;

		case CELL_TYPE_PERSON:
		default:
			// User Attributes
			if (jCell.userAttributes) {
				for (var i = 0; i < jCell.userAttributes.length; i++) {
					var userAttributeRow = table.insertRow(-1);
					var userAttributeCell = userAttributeRow.insertCell(-1);
					userAttributeCell.className = "celluserAttribute";
					userAttributeCell.colSpan = 2;
					userAttributeCell.align = "center";
					userAttributeCell.innerHTML = jCell.userAttributes[i]['label'] + " : " + jCell.userAttributes[i]['value'];
				}
			}
			break;
	}

	div.appendChild(table);
	mainDiv.appendChild(div);

	div.style.width = CELLSIZE; // table.offsetWidth + 10; on fixe pour eviter les pbs
	jCell.div = div;
}

/**
 * Get CSS class name for given cell type.
 *
 * @param cellType	cell type (CELL_TYPE_ORGANIZATION, CELL_TYPE_CATEGORY, CELL_TYPE_PERSON)
 *
 * @returns CSS class name
 */
function getCellClassName(cellType) {
	var className = null;
	switch(cellType)
	{
		case CELL_TYPE_ORGANIZATION:
			className = "cellOrganization";
			break;

		case CELL_TYPE_CATEGORY:
			className = "cellCategory";
			break;

		case CELL_TYPE_PERSON:
		default:
			className = "cellPerson";
			break;
	}

	return className;
}

/**
 * Get HTML code for given cell's title.
 *
 * @param cellType	cell type (CELL_TYPE_ORGANIZATION, CELL_TYPE_CATEGORY, CELL_TYPE_PERSON)
 *
 * @returns HTML Code
 */

function getTitle(jCell) {
	var htmlCode = null;

	switch(jCell.cellType)
	{
		case CELL_TYPE_ORGANIZATION:
			if(jCell.showCenterLink){
				htmlCode = "<a href=\"" + jCell.onClickURL +"&chartType=0\">"+jCell.title+"</a>";
			}
			else {
				htmlCode = jCell.title;
			}
			break;

		case CELL_TYPE_PERSON:
			htmlCode = "<a target=\"_blank\" href=\"" + jCell.onClickURL+ "\">" + jCell.title + "</a>";
			break;

		case CELL_TYPE_CATEGORY:
		default:
			htmlCode = jCell.title;
			break;
	}

	return htmlCode;
}

/**
 * Build links between cells
 */
function buildUpAndDownLinks() {
	if(jLinks.length > 0){
	    var jLink;
  		var jCell1;
  		var jCell2;
  		for (i = 0; i < jLinks.length; i++)
  		{
	  		jLink = jLinks[i];
	  		if ( (jLink.orientation != ORIENTATION_LEFT) && (jLink.orientation != ORIENTATION_RIGHT) ) {
		  		jCell1 = getJCell(jLink.origin);
		  		jCell2 = getJCell(jLink.destination);
		  		if (jCell1.level < jCell2.level)
		  		{
		  			jCell1.downLinks[jCell1.downLinks.length] = jLink;
		  			jCell2.upLinks[jCell2.upLinks.length] = jLink;
		  		}
		  		else if (jCell1.level > jCell2.level)
		  		{
		  			jCell1.upLinks[jCell1.upLinks.length] = jLink;
		  			jCell2.downLinks[jCell2.downLinks.length] = jLink;
		  		}
	  		}
  		}
	}
}

function placeCells()
{
	var i;
	var j;
	var topGap = mainDiv.offsetTop + V_MARGIN;
	var leftGap = mainDiv.offsetLeft;
	var minHeight;
	var maxHeight;
	var jCell;
	var div;
	var jLevels = new Array(maxLevel);

	// on classe par niveau (vertical seulement)
	for (i = 0; i < maxLevel; i++)
	{
		jLevels[i] = new Array();

		for (j = 0; j < cellsCount; j++)
		{
			if (jCells[j].level == i)
			{
				jLevels[i][jLevels[i].length] = jCells[j];
			}
		}

		leftGap = mainDiv.offsetLeft;
		for (j = 0; j < jLevels[i].length; j++)
		{
			if (j > 0)
			{
				jLevels[i][j].leftCell = jLevels[i][j - 1];
			}
			if (j < (jLevels[i].length - 1))
			{
				jLevels[i][j].rightCell = jLevels[i][j + 1];
			}

			div = jLevels[i][j].div;
			if ( (jLevels[i][j].className==3) || (jLevels[i][j].className==4) ) {
				div.style.top = topGap - H_GAP_SIDEBOX;
			} else {
				div.style.top = topGap;
			}

			div.style.left = leftGap;
		}

		topGap += V_GAP + div.offsetHeight;
  }

   //resizeBoxes(jLevels);

moveHorizontalAndVertical(jLevels);

	mainDiv.style.height = topGap + V_MARGIN * 3;

	var lastLevel = jLevels[jLevels.length-1];
	var chartWidth = lastLevel[lastLevel.length-1].div.offsetLeft + lastLevel[lastLevel.length-1].div.offsetWidth - lastLevel[0].div.offsetLeft;
	mainDiv.style.width = chartWidth + (H_MARGIN*2);

	moveMain(jLevels);
  	var maxLevelWidth = 0;
	for (i = 0; i < jLevels.length; i++)
	{
		div = jLevels[i][jLevels[i].length - 1].div;
		maxLevelWidth = Math.max(maxLevelWidth, div.offsetLeft + div.offsetWidth + 2);
	}
	if (maxLevelWidth < mainDiv.offsetWidth)
	{
		var marginWidth = parseInt((mainDiv.offsetWidth - maxLevelWidth) / 2);
		for (i = 0; i < cellsCount; i++)
		{
			jCells[i].div.style.left = jCells[i].div.offsetLeft + marginWidth;
		}
	}
	else
	{
		var maxCount = 0; // nombre de cellules max. par ligne
		for (i = 0; i < jLevels.length; i++)
		{
			maxCount = Math.max(maxCount, jLevels[i].length);
		}
		maxCount--;
		var gap = parseInt((maxLevelWidth - mainDiv.offsetWidth) / maxCount) + 5;
		H_GAP = Math.max(H_GAP - gap, 5);
		for (i = 0; i < jLevels.length; i++)
		{
			leftGap = H_GAP;
			for (j = 0; j < jLevels[i].length; j++)
			{
				jCell = jLevels[i][j];
				if (jCell.upLinks.length == 1)
				{
					if(jCell.upLinks[0].orientation == ORIENTATION_VERTICAL)
					{
			            // case orientation vertical du niveau
			            // on ne peut pas être recursif
			            // on recup la case du dessus
			            var currentOrigin = jCell.upLinks[0].origin;
			            var jcellorigin = getJCell(currentOrigin);
			            div = jLevels[i][j].div;
			            div.style.left = jcellorigin.div.offsetLeft;
		            }else
		            {
				      div = jLevels[i][j].div;
				      leftGap = parseInt(div.style.left) + H_GAP;
				      div.style.left = leftGap;
				    }
				}else
				{
				  // case 0 -> no up link
				  div = jLevels[i][j].div;
			      div.style.left = leftGap;
			      leftGap += div.offsetWidth + H_GAP;
			    }
			}
		}
		moveMain(jLevels);
	}

  // on refait une passe si on a cellule droite ou gauche
	// il faut que la case supérieur (case 0) soit bien placé
	for (i = 0; i < jLevels.length; i++)
	{
		leftGap = 0;
		for (j = 0; j < jLevels[i].length; j++)
		{
			if(cellRightNumber != -1 && cellRightNumber == jLevels[i][j].id){
			  // la cellule est une cellule droite
	          div = jLevels[i][j].div;
	          divOrigin = jCells[cellRightOrigin].div;
	          // suppr le px
	          var originLeft = divOrigin.style.left;
	          var origin = originLeft.substring(0,originLeft.length - 2);
	          var intOrigin = parseInt(origin);
	          div.style.left =  intOrigin + CELLSIZE; // on décalle d'une
														// cellule si c'est
														// possible
	        }else if(cellLeftNumber != -1 && cellLeftNumber == jLevels[i][j].id){
	          // la cellule est une cellule gauche
	          div = jLevels[i][j].div;
	          divOrigin = jCells[cellLeftOrigin].div;
	          // suppr le px
	          var originLeft = divOrigin.style.left;
	          var origin = originLeft.substring(0,originLeft.length - 2);
	          var intOrigin = parseInt(origin);
	          if(intOrigin > CELLSIZE){
	               div.style.left = intOrigin - CELLSIZE; // on décalle d'une
															// cellule si c'est
															// possible
	          }
			}
		}
  }
}

function moveMain(jLevels)
{
	for (i = 0; i < jLevels.length; i++)
	{
		moveMain1(jLevels[i]);
	}
	for (i = (jLevels.length - 1); i >= 0; i--)
	{
		moveMain1(jLevels[i]);
	}
	for (i = 0; i < jLevels.length; i++)
	{
		moveMain2(jLevels[i]);
	}
	for (i = (jLevels.length - 1); i >= 0; i--)
	{
		moveMain2(jLevels[i]);
	}
}

function moveMain1(jLevels)
{
	var jCell;
	var jCell0;
	var jCell1;
	var x0;
	var x1;
	var i;
	for (i = 0; i < jLevels.length; i++)
	{
		jCell0 = jLevels[i];
		if (jCell0.downLinks.length == 1)
		{
			x0 = getMiddleX(jCell0);
			jCell1 = getJCell(jCell0.downLinks[0].destination);
			x1 = getMiddleX(jCell1);
			if (x0 != x1)
			{
				jCell = (x0 > x1 ? jCell1 : jCell0);
				moveRight(jCell, Math.abs(x0 - x1));
			}
		}
	}
}

function moveMain2(jLevels)
{
	var jCell0;
	var jCell1;
	var jCell2;
	var x0;
	var x1;
	var i;
	for (i = 0; i < jLevels.length; i++)
	{
		jCell0 = jLevels[i];
		if (jCell0.downLinks.length > 1)
		{
			x0 = getMiddleX(jCell0);
			jCell1 = getJCell(jCell0.downLinks[0].destination);
			jCell2 = getJCell(jCell0.downLinks[jCell0.downLinks.length - 1].destination);
			x1 = parseInt((getMiddleX(jCell1) + getMiddleX(jCell2)) / 2);
			if (x0 != x1)
			{
				jCell = (x0 > x1 ? jCell1 : jCell0);
				moveRight(jCell, Math.abs(x0 - x1));
			}
		}
	}
}

function getMiddleX(jCell)
{
	return parseInt(jCell.div.offsetLeft + jCell.div.offsetWidth / 2);
}

function moveRight(jCell, gap)
{
	var div = jCell.div;
	div.style.left = div.offsetLeft + gap;
	jCell = jCell.rightCell;
	while (jCell != null)
	{
		div = jCell.div;
		div.style.left = div.offsetLeft + gap;
		jCell = jCell.rightCell;
	}
}

function getJCell(id)
{
	var i;
	for (i = 0; i < cellsCount; i++)
	{
		if (jCells[i].id == id)
		{
			return jCells[i];
		}
	}
	return null;
}

function resizeBoxes(jLevels)
{
	// recuperation max
    var maxWidth = new Array(jLevels.length);
	var maxHeight = new Array(jLevels.length);
	for (i = 0; i < jLevels.length; i++)
	{
		 for (j = 0; j < jLevels[i].length; j++)
		 {
			 var div = jLevels[i][j].div;
			 //largeur max
		     if(div.style.width > maxWidth)
		     {
		        maxWidth[i] = div.offsetWidth;
		     }
		     // hauteur max
			 if(div.style.height > maxHeight)
			 {
		        maxHeight[i] = div.offsetHeight;
		     }
		 }
	}
	// resize all boxes
	for (i = 0; i < jLevels.length; i++)
	{
		 for (j = 0; j < jLevels[i].length; j++)
		 {
			 var div = jLevels[i][j].div;
			 div.style.width = maxWidth[i];
			 div.style.height = maxHeight[i];
		 }
	}
}

function moveHorizontalAndVertical(jLevels)
{
	var jCell;
	var topGap;
    var leftGap;
	var V_GAP_SAME_LEVEL = 20;
	var H_GAP = 50;

	for (i = 0; i < jLevels.length; i++)
	{
		topGap = -1;
		leftGap = 0;

		var maximumHeight = calculateMaximumHeight(jLevels[i]);

        for (j = 0; j < jLevels[i].length; j++)
		{
            jCell = jLevels[i][j];
            jLevels[i][j].gaps["y"]=0;
            jLevels[i][j].gaps["x"]=0;
            if (jCell.upLinks.length == 1)
  			{
  				var currentOrigin = jCell.upLinks[0].origin;
				if(jCell.upLinks[0].orientation == ORIENTATION_HORIZONTAL) // same link on one level
				{
					// oriention horizontal uniquement
					div = jCell.div;

					if(j%2==0 || jCell.cellType!=CELL_TYPE_ORGANIZATION){
						if(j>0){
							if (jCell.cellType==CELL_TYPE_ORGANIZATION) {
								jLevels[i][j].gaps["x"] = -1*parseInt(jLevels[i][j-1].div.style.width) / 2.5;
							}
							else {
								jLevels[i][j].gaps["x"] = H_MARGIN*4;
							}
						}
					}
					else{
						jLevels[i][j].gaps["y"]=maximumHeight;
						jLevels[i][j].gaps["x"] = -1*parseInt(jLevels[i][j-1].div.style.width) / 2.5;
						div.style.top= parseInt(div.style.top) + maximumHeight + H_MARGIN;
					}
					div.style.left = leftGap + jCell.gaps["x"];
					leftGap = leftGap + div.offsetWidth + jCell.gaps["x"];

	            }else if(jCell.upLinks[0].orientation == ORIENTATION_RIGHT)
	            {
	            	// oriention droite -> horizontal
	                div = jCell.div;
	                div.style.top = H_GAP_SIDEBOX;top
	                leftGap += div.offsetWidth + H_GAP;
	            }else if(jCell.upLinks[0].orientation == ORIENTATION_LEFT)
	            {
	  				// oriention gauche -> horizontal
	                div = jCell.div;
	                div.style.top = H_GAP_SIDEBOX;
	  				leftGap += div.offsetWidth + H_GAP;
	  			}else
	  			{
	                // orientation vertical uniquement
	                div = jCell.div;
	                var jcellorigin = getJCell(currentOrigin);
	                leftGap = jcellorigin.div.offsetLeft;
	                topGap = jcellorigin.div.offsetTop + jcellorigin.div.offsetHeight + V_GAP_SAME_LEVEL + jcellorigin.downLinksAlreadyDone;
	                jcellorigin.downLinksAlreadyDone += jCell.div.offsetHeight + V_GAP_SAME_LEVEL;
	                div.style.top = topGap;
	  				div.style.left = leftGap;
	  			}
	  		}
	  	}
	}
}

function calculateMaximumHeight(jLevel)
{
	var maximumHeight=0;
	for (j = 0; j < jLevel.length; j++)
	{
		jCell = jLevel[j];
		if(jCell.div.clientHeight > maximumHeight)
		{
			maximumHeight=jCell.div.clientHeight;
		}
	}
	return maximumHeight;
}

function buildLinks()
{
  var X_DEC = 10;

  var i;
	var jLink;
	var jCell0;
	var div0;
	var x0;
	var y0;
	var div1;
	var jCell1;
	var x1;
	var y1;
	var jCellTmp;
	var xDiff;
	for (i = 0; i < jLinks.length; i++)
	{
		jLink = jLinks[i];
		jCell0 = getJCell(jLink.origin);
		jCell1 = getJCell(jLink.destination);
		if (jCell0 != null && jCell1 != null)
		{
			if (jCell0.level != jCell1.level)
			{
				if (jCell0.level > jCell1.level)
				{
					jCellTmp = jCell1;
					jCell1 = jCell0;
					jCell0 = jCellTmp;
				}
				if(jLink.orientation == ORIENTATION_HORIZONTAL)
				{
	  				div0 = jCell0.div;
	  				x0 = parseInt(div0.offsetLeft + div0.offsetWidth / 2);
	  				y0 = div0.offsetTop + div0.offsetHeight - 2;
	  				div1 = jCell1.div;
	  				x1 = parseInt(div1.offsetLeft + div1.offsetWidth / 2);
	  				y1 = div1.offsetTop + 2;
	  				xDiff = Math.abs(x1 - x0);
	  				if (xDiff == 0)
	  				{
	  					buildLink(jLink.type, x0, y0, 0, y1 - y0);
	  				}
	  				else if (xDiff < 20 && jCell0.downLinks.length < 2)
	  				{
	  					buildLink(jLink.type, Math.min(x0, x1) + parseInt(xDiff / 2), y0, 0, y1 - y0);
	  				}
	  				else
	  				{
	  					var part1y = 0;
	  					if(jCell1.gaps["y"]>0)
	  					{
	  						part1y = parseInt((y1 - y0 - jCell1.gaps["y"]- H_MARGIN) * 5 / 6);
	    				}
	    				else
	    				{
	    					part1y = parseInt((y1 - y0) * 5 / 6);
	    				}

	    				var part2y = y1 - y0 - part1y;

						   buildLink(jLink.type, x0, y0, 0, part1y); // premier
																		// lien
																		// vertical
						   buildLink(jLink.type, x1, y0 + part1y, 0, part2y); // deuxième
																				// lien
																				// vertical
						   buildLink(jLink.type, Math.min(x0, x1), y0 + part1y, Math.abs(x1 - x0), 0); // lien
																										// horizontal
					}
				}
				else if (jLink.orientation == ORIENTATION_VERTICAL)
				{
					// lien de type vertical
					div0 = jCell0.div;
					x0 = parseInt(div0.offsetLeft);
					y0 = div0.offsetTop + div0.offsetHeight/2;
					div1 = jCell1.div;
					x1 = parseInt(div1.offsetLeft);
					y1 = div1.offsetTop + div1.offsetHeight/2;
					// ligne horizontale coté haut
					buildLink(jLink.type, x0 - X_DEC, y0, X_DEC, 0);
					// ligne droite verticals
					buildLink(jLink.type, x0 - X_DEC, y0, 0, y1 - y0);
					// ligne horizontale coté bas
					buildLink(jLink.type, x1 - X_DEC, y1, X_DEC, 0);
				}
				else if (jLink.orientation == ORIENTATION_RIGHT)
				{
	  			    // lien de type case à droite
	  			    div0 = jCell0.div;
					x0 = parseInt(div0.offsetLeft + div0.offsetWidth / 2);
					y0 = div0.offsetTop;
					div1 = jCell1.div;
					x1 = div1.offsetLeft;
					y1 = parseInt(div1.offsetTop + div1.offsetHeight/2);
					// ligne droite verticals
					buildLink(jLink.type, x0, y0, 0, y1 - y0);
					// ligne horizontale coté bas
					buildLink(jLink.type, x0, y1, x1 - x0, 0);
				}
				else if (jLink.orientation == ORIENTATION_LEFT){
	  			    	// lien de type case à gauche
						div0 = jCell0.div;
						x0 = parseInt(div0.offsetLeft + div0.offsetWidth / 2);
						y0 = div0.offsetTop;
						div1 = jCell1.div;
						x1 = div1.offsetLeft;
						y1 = div1.offsetTop + div1.offsetHeight/2;
						// ligne droite verticale
						buildLink(jLink.type, x0, y0, 0, y1 - y0);
						// ligne horizontale coté bas
						buildLink(jLink.type, x1, y1, x0 - x1, 0);
         }
			}
		}
		else
		{
				div0 = jCell0.div;
				div1 = jCell1.div;
				if (div0.offsetLeft > div1.offsetLeft)
				{
					var div = div1;
					div1 = div0;
					div0 = div;
				}
				x0 = div0.offsetLeft + div0.offsetWidth;
				y0 = div0.offsetTop + parseInt(div0.offsetHeight / 2);
				x1 = div1.offsetLeft;
				y1 = div1.offsetTop + parseInt(div0.offsetHeight / 2);
				buildLink(jLink.type, x0, y0, x1 - x0, y1 - y0);
			}
		}
}

function buildLink(type, left, top, width, height)
{
	// contruire une ligne de type "type" depuis (left, top) sur une longueur de
	// (width, height)
	var div = document.createElement("DIV");
	div.className = "link" + type;
	div.style.left = left;
	div.style.top = Math.floor(top);
	div.style.width = width;
	div.style.height = height;
	if (height == 0)
	{
		// IE bug : fill the div with an empty span to keep 0 height.
		div.innerHTML = "<span></span>";
	}
	mainDiv.appendChild(div);
}