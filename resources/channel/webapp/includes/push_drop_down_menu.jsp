<script>

function highlightMenuItem(menuItem,state) {
	menuItem.style.color = (state) ? "#FFFFFF" : "";
	menuItem.style.backgroundColor = (state) ? "#000099" : "";
}

function hightlightMenu(menuCell,color,forceOff) {
	if(!forceOff) {
		var menuCellID = menuCell.id;
		var rootName = menuCellID.substring(0,menuCellID.indexOf("_"))		
		var menuDivID = rootName + "_MenuDiv";
		var state = (menuCell.className == "buttonMenu") ? true : false;
		
		var menuCellTop = getTrueTop(menuCell);			
		var menuCellHeight = menuCell.offsetHeight;
		var menuCellLeft = getTrueLeft(menuCell);
		var menuCellBottom = menuCellTop + menuCellHeight;
		 
	}
	else {
		var state = false;
		menuDivId = "";
		menuCellID = "";
	}
	var allTDs = document.getElementsByTagName("td"); //depends on it being a <td>
	for(i=0; i<allTDs.length; i++) {		
		var thisTD = allTDs[i];
		
		if(thisTD.id != null && thisTD.id.indexOf("_MenuTitle") > 0) { //if this is a menu cell then change the style to tableRowActionsMenu
			thisTD.className = "buttonMenu"
		}
		
		if(thisTD.id == menuCellID && state) { //if this cell is a menu cell and if this cell has a class of tableRowActionsMenu
			thisTD.className = "buttonMenuActive";
		}
	}
	var allDIVs = document.getElementsByTagName("div");
	for(j=0; j<allDIVs.length; j++) {
		var thisDIV = allDIVs[j];
		if(thisDIV.id != null && thisDIV.id.indexOf("_MenuDiv") > 0) {
			thisDIV.style.visibility = "hidden";			
		}
		if(thisDIV.id == menuDivID && state) {
			thisDIV.style.left = menuCellLeft;
			thisDIV.style.top = menuCellBottom;
			thisDIV.style.visibility = "visible";
		}
	}
}


document.onmouseup = function() {
	hightlightMenu(null,null,true);
}
</script>