var LIST_MAX_ELEMENTS = 50;
var LIST_MAX_HEIGHT = 283;
var LIST_EVENT_MARGIN = 5;

var filterDiv = null;
var filterTable = null;
var tableNameInput = null;
var tableNameSelect = null;
var previousFilter = "";
var isInputFocus = false;
var selectedElementIndex = -1;
var elementsCount = 0;
var originOnChangeFunction;

function disableEnter(e)
{
    var evt = (window.event ? window.event : e);
    var key = parseInt(evt.keyCode);
    return (key != 13);
}

function init(listName)
{
    tableNameInput = document.forms["processForm"].elements[listName + "Input"];
    if (tableNameInput != null)
    {
        tableNameInput.setAttribute("autocomplete", "off");
        tableNameInput.onclick = displayFilterTable;
        tableNameInput.onkeyup = updateFilterTable;
        tableNameInput.onkeypress = disableEnter;
        tableNameInput.onfocus = inputFocus;
        tableNameInput.onblur = inputBlur;
        
        filterDiv = document.createElement("DIV");
        filterDiv.id = "filterDiv";
        filterDiv.className = "tableFrame";
        filterDiv.style.display = "none";
        
        filterTable = document.createElement("TABLE");
        filterTable.width = 150;
        filterTable.cellPadding = "0";
        filterTable.cellSpacing = "2";
        filterTable.border = "0";
        filterDiv.appendChild(filterTable);
        
        document.body.appendChild(filterDiv);
        
        tableNameSelect = document.forms["processForm"].elements[listName];
        originOnChangeFunction = tableNameSelect.onchange;
        tableNameSelect.onclick = clearTableNameInput;
        tableNameSelect.onchange = clearTableNameInput;
        
        window.onresize = updateFilterDivPosition;
        document.onclick = updateFilterDivDisplay;    
    }
}

function clearTableNameInput()
{
    tableNameInput.value = "";
    updateFilterTable(null);
    if (originOnChangeFunction != null)
    {
        originOnChangeFunction.call();
    }
}

function updateFilterTable(e)
{
    var filter = tableNameInput.value;
    if (filter != previousFilter)
    {
        selectedElementIndex = -1;
        while (filterTable.rows.length > 0)
        {
            filterTable.deleteRow(filterTable.rows.length - 1);
        }
        previousFilter = filter;
        if (filter != "")
        {
            filterDiv.style.display = "";
            var options = tableNameSelect.options;
            var i = 0;
            var stopDisplay = false;
            var value;
            while (i < options.length && !stopDisplay)
            {
                value = options[i].value;
                if (value.indexOf(filter) != -1)
                {
                    var row = filterTable.insertRow(-1);
                    var cell = row.insertCell(-1);
                    if (filterTable.rows.length < LIST_MAX_ELEMENTS)
                    {
                        var link = document.createElement("A");
                        link.href = "javascript:selectOption('" + value + "')";
                        link.appendChild(document.createTextNode(value));
                        cell.appendChild(link);
                    }
                    else
                    {
                        cell.innerHTML = "...";
                        stopDisplay = true;
                    }
                }
                i++;
            }
            elementsCount = filterTable.rows.length;
            if (stopDisplay)
            {
                elementsCount--;
            }
            if (filterTable.rows.length == 0)
            {
                hideFilterTable();
            }
            else
            {
                displayFilterTable();
            }
        }
        else
        {
            hideFilterTable();
        }
    }
    var evt = (window.event ? window.event : e);
    if (evt != null)
    {
        var key = parseInt(evt.keyCode);
        updateSelectedElement(key);
    }
}

function updateSelectedElement(key)
{
    if (filterTable.rows.length > 0)
    {
        if (key == 38 || key == 40)
        {
            if (filterDiv.style.display == "none")
            {
                filterDiv.style.display = "";
            }
            if (key == 38)
            {
                var value = tableNameInput.value;
                tableNameInput.value = "";
                tableNameInput.focus();
                tableNameInput.value = value;
                selectedElementIndex -= 1;
            }
            else
            {
                selectedElementIndex += 1;
            }
            selectedElementIndex = Math.max(selectedElementIndex, 0);
            selectedElementIndex = Math.min(selectedElementIndex, elementsCount - 1);
            selectElement();
        }
        else if (key == 13)
        {
            if (selectedElementIndex != -1)
            {
                var selectedValue = getChild(filterTable.rows[selectedElementIndex].cells[0], "A").innerHTML;
                selectOption(selectedValue);
            }
        }
    }
}

function selectElement()
{
    var index = Math.max(selectedElementIndex - 1, 0);
    selectRow(index, false);
    
    index = Math.min(selectedElementIndex + 1, elementsCount - 1);
    selectRow(index, false);
    
    var row = selectRow(selectedElementIndex, true);
    var gap = 2 * row.offsetHeight + 5;
    filterDiv.scrollTop = Math.max(row.offsetTop + gap - filterDiv.offsetHeight, 0);
}

function selectRow(index, select)
{
	var className = (select ? "ArrayColumn selectElt" : "");
    var row = filterTable.rows[index];
    row.className = className;
    getChild(row.cells[0], "A").className = className;
    return row;
}

function selectOption(value)
{
    tableNameSelect.value = value;
    hideFilterTable();
    if (originOnChangeFunction != null)
    {
        originOnChangeFunction.call();
    }
}

function displayFilterTable()
{
    if (filterTable.rows.length > 0)
    {
        var tableHeight = filterTable.offsetHeight;
        if (tableHeight > LIST_MAX_HEIGHT)
        {
            filterDiv.style.height = LIST_MAX_HEIGHT;
            filterDiv.style.width = filterTable.offsetWidth + 20;
            filterDiv.style.overflowY = "auto";
        }
        else
        {
            filterDiv.style.height = tableHeight;
            filterDiv.style.width = filterTable.offsetWidth + 10;
            filterDiv.style.overflowY = "hidden";
        }
        updateFilterDivPosition();
    }
}

function hideFilterTable()
{
    filterDiv.style.display = "none";
    if (selectedElementIndex != -1 && filterTable.rows.length > 0)
    {
        selectRow(selectedElementIndex, false);
        selectedElementIndex = -1;
    }
}

function updateFilterDivPosition()
{
    var offsets = getOffsets(tableNameInput);
    filterDiv.style.top = offsets[0] + tableNameInput.offsetHeight + 1;
    filterDiv.style.left = offsets[1];
    filterDiv.style.display = "";
}

function inputFocus()
{
    isInputFocus = true;
    displayFilterTable();
}

function inputBlur()
{
    isInputFocus = false;
}

function updateFilterDivDisplay(e)
{
    if (filterDiv.style.display == "" && !isInputFocus)
    {
        var evt = (window.event ? window.event : e);
        var x;
        if (evt.pageX)
        {
            x = evt.pageX;
        }
        else if (evt.clientX)
        {
            x = evt.clientX + (document.documentElement.scrollLeft
                ? document.documentElement.scrollLeft : document.body.scrollLeft);
        }
        else
        {
            x = null;
        }
        if (x != null)
        {
            var minX = parseInt(filterDiv.style.left) - LIST_EVENT_MARGIN;
            if (x < minX)
            {
                hideFilterTable();
            }
            else
            {
                var maxX = parseInt(filterDiv.style.left) + parseInt(filterDiv.style.width) + LIST_EVENT_MARGIN;
                if (x > maxX)
                {
                    hideFilterTable();
                }
                else
                {
                    var y;
                    if (evt.pageY)
                    {
                        y = evt.pageY;
                    }
                    else if (evt.clientY)
                    {
                        y = evt.clientY + (document.documentElement.scrollTop
                            ? document.documentElement.scrollTop : document.body.scrollTop);
                    }
                    else
                    {
                        y = null;
                    }
                    if (y != null)
                    {
                        var minY = parseInt(filterDiv.style.top) - LIST_EVENT_MARGIN;
                        if (y < minY)
                        {
                            hideFilterTable();
                        }
                        else
                        {
                            var maxY = parseInt(filterDiv.style.top) + parseInt(filterDiv.style.height) + LIST_EVENT_MARGIN;
                            if (y > maxY)
                            {
                                hideFilterTable();
                            }
                        }
                    }
                }
            }
        }
    }
}