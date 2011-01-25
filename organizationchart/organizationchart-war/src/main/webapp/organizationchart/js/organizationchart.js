function JCell(id, name, infoSup, url, level, type, cellType, linkCenter, linkDetails, upperLink)
{
	this.id = id; // id - use for links between cells
	this.name = name;
	this.infoSup = infoSup;
	this.url = url;
	this.level = level; // level for css
	this.type = type; // cell class type for css
	this.cellType = cellType; // 0 = unit or 1 = category, personn
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
	this.linkCenter = linkCenter;
	this.linkDetails = linkDetails;
	this.upperLink = upperLink;
	this.downLinksAlreadyDone = 0;
}

var cellRightNumber = -1;
var cellRightOrigin = -1;
var cellLeftNumber = -1;
var cellLeftOrigin = -1;

function JLink(origin, destination, type, orientation)
{
	this.origin = origin;
	this.destination = destination;
	this.type = type;
	this.orientation = orientation; // 0 horizontal - 1 vertical - 2 right - 3 left
	if(orientation == 2){
    // cas on a une cellule droite
		cellRightNumber = destination;
    	cellRightOrigin = origin;
	}else if(orientation == 3){
    // cas on a une cellule gauche
		cellLeftNumber = destination;
		cellLeftOrigin = origin;
	}
}

var V_MARGIN = 20;
var H_MARGIN = 20;
var H_GAP = 50;
var V_GAP = 100;
var V_GAP_SIDEBOX = 300;
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

  buildCells();
  placeCells();
  buildLinks();
  
  // update size div invisible pour le contour du td silverpeas
  invisibleDiv = document.getElementById("chartInvisible");
  invisibleDiv.style.height = mainDiv.style.height;
  invisibleDiv.style.width = mainDiv.style.width;
  
  var maxWidth = 0;
  var widthDiv = mainDiv.style.width;
  // supprime le px
  widthDiv = widthDiv.substring(0,widthDiv.length - 2);
  for (i = 0; i < jCells.length; i++)
  {
	  div = jCells[i].div;
	  maxWidth = Math.max(maxWidth, div.offsetLeft + div.offsetWidth + 2);
  }
	
  // on centre le scroll sur la case 0 (moitié de la largeur max moins un demi écran moins une demi cellule)
  mainDiv.scrollLeft= parseInt( maxWidth / 2 - widthDiv / 2 - CELLSIZE / 2 );
  mainDiv.style.width = "95%";
}

function buildCells()
{
	var i;
	for (i = 0; i < cellsCount; i++)
	{
    buildCell(jCells[i]);
	}
	
	if(jLinks.length > 0){
	  var jLink;
  	var jCell1;
  	var jCell2;
  	for (i = 0; i < jLinks.length; i++)
  	{
  		jLink = jLinks[i];
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

function buildCell(jCell)
{
	var div = document.createElement("DIV");
	div.className = "cell" + jCell.type;
	
	var table = document.createElement("TABLE");

	var tr1 = table.insertRow(-1);
	var td1 = tr1.insertCell(-1);
	td1.className = "cellName";
	td1.colSpan = 2;
	td1.align = "center";
	
	td1.innerHTML = jCell.name;
	if(jCell.linkCenter){
		td1.innerHTML = "<a href=\"" + jCell.url +"&chartType=0\">"+td1.innerHTML+"</a>";
	}
	if(jCell.upperLink != "")
	{
		td1.innerHTML = td1.innerHTML + "&nbsp;<a href=\"" + jCell.upperLink +"&chartType=0\">^</a>";
	}
	if(jCell.infoSup.length > 0){
	  for (i = 0; i < jCell.infoSup.length; i++)
	  {
  	  var tr2 = table.insertRow(-1);
  	  var td2 = tr2.insertCell(-1);
  	  td2.className = "cellInfos";
  	  td2.colSpan = 2;
  	  td2.align = "center";
  	  var i;
  	  td2.innerHTML = jCell.infoSup[i];
	  }
  }
	
	if(jCell.cellType == 0){
	  // unit cell
  	  var tr3 = table.insertRow(-1);
      var td3 = tr3.insertCell(-1);
      td3.className = "cellLinkLeft";
  	  var td4 = tr3.insertCell(-1);
  	  td4.className = "cellLinkRight";
  	  var centrer = "&nbsp;";
  	  td3.innerHTML = centrer;
      var detail = "&nbsp;";
  	  if(jCell.linkDetails){
  		  detail = "<a href=\"" + jCell.url +"&chartType=1\">D&eacute;tails</a>";
  		}
  		td4.innerHTML = detail;
  }
	
	div.appendChild(table);
	mainDiv.appendChild(div);

	div.style.width = CELLSIZE; //table.offsetWidth + 10; on fixe pour eviter les pbs
	jCell.div = div;
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
			
			div = jLevels[i][j].div
			div.style.top = topGap;
			div.style.left = leftGap;
		}
		
		topGap += V_GAP + div.offsetHeight;
  }
  
  //resizeBoxes(jLevels);
  
  moveHorizontalAndVertical(jLevels);
  
  mainDiv.style.height = topGap + V_MARGIN;
  mainDiv.style.width = 800;
  mainDiv.style.overflow="auto";
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
					if(jCell.upLinks[0].orientation == 1)
          {
            // case orientation vertical du niveau
            // on ne peut pas être recursif
            // on recup la case du dessus
            var currentOrigin = jCell.upLinks[0].origin;
            var jcellorigin = getJCell(currentOrigin);
            div = jLevels[i][j].div;
            div.style.left = jcellorigin.div.offsetLeft;
          }else{
			      div = jLevels[i][j].div;
			      div.style.left = leftGap;
			      leftGap += div.offsetWidth + H_GAP;
			    }
				}else{
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
	          div.style.left =  intOrigin + CELLSIZE; // on décalle d'une cellule si c'est possible
	        }else if(cellLeftNumber != -1 && cellLeftNumber == jLevels[i][j].id){
	          // la cellule est une cellule gauche
	          div = jLevels[i][j].div;
	          divOrigin = jCells[cellLeftOrigin].div;
	          // suppr le px
	          var originLeft = divOrigin.style.left;
	          var origin = originLeft.substring(0,originLeft.length - 2);
	          var intOrigin = parseInt(origin);
	          if(intOrigin > CELLSIZE){
	               div.style.left = intOrigin - CELLSIZE; // on décalle d'une cellule si c'est possible
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
			   // largeur max
         if(div.style.width > maxWidth){
            maxWidth[i] = div.offsetWidth;
         }
         // hauteur max
		     if(div.style.height > maxHeight){
            maxHeight[i] = div.offsetHeight;
         }
			 }
      }
      
      //resize all boxes
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
        
        for (j = 0; j < jLevels[i].length; j++)
				{
          jCell = jLevels[i][j];
          
          if (jCell.upLinks.length == 1)
  				{
  					var currentOrigin = jCell.upLinks[0].origin;
  					if(jCell.upLinks[0].orientation == 0) //same link on one level 
            {
  					    // oriention horizontal uniquement
                div = jCell.div;
  					    div.style.left = leftGap;
  					    leftGap += div.offsetWidth + V_GAP;
            }else if(jCell.upLinks[0].orientation == 2)
            {
  					    // oriention droite -> horizontal
                div = jCell.div;
                div.style.left = - V_GAP_SIDEBOX;
  					    leftGap += div.offsetWidth + H_GAP;
            }else if(jCell.upLinks[0].orientation == 3) 
            {
  					    // oriention gauche -> horizontal
                div = jCell.div;
  					    div.style.left = + V_GAP_SIDEBOX;
  					    leftGap += div.offsetWidth + H_GAP;
  					}else{
                // orientation vertical uniquement
                div = jCell.div;
                var jcellorigin = getJCell(currentOrigin);
                leftGap = jcellorigin.div.offsetLeft;
                topGap = jcellorigin.div.offsetTop + jcellorigin.div.offsetHeight + V_GAP_SAME_LEVEL + jcellorigin.downLinksAlreadyDone;
                jcellorigin.downLinksAlreadyDone += jcellorigin.div.offsetHeight + V_GAP_SAME_LEVEL;
                  
                div.style.top = topGap;
  					    div.style.left = leftGap;
  					}
  				}
  			}
			}
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
				if(jLink.orientation == 0){
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
  				}else{
    					
    				 var part1y = parseInt((y1 - y0) * 2 / 3);
    				 var part2y = parseInt((y1 - y0) * 1 / 3);
    				 
					   buildLink(jLink.type, x0, y0, 0, part1y); // premier lien vertical
					   buildLink(jLink.type, x1, y1 - part2y, 0, part2y); // deuxième lien vertical
					   buildLink(jLink.type, Math.min(x0, x1), y0 + part1y, Math.abs(x1 - x0), 0); // lien horizontal
					}
			 }else if (jLink.orientation == 1){
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
  			 }else if (jLink.orientation == 2){
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
				 }else if (jLink.orientation == 3){
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
	// contruire une ligne de type "type" depuis (left, top) sur une longueur de (width, height)
	var div = document.createElement("DIV");
	div.className = "link" + type;
	div.style.left = left;
	div.style.top = top;
	div.style.width = width;
	div.style.height = height;
	if (height == 0)
	{
		// IE bug : fill the div with an empty span to keep 0 height.
		div.innerHTML = "<span></span>";
	}
	mainDiv.appendChild(div);
}