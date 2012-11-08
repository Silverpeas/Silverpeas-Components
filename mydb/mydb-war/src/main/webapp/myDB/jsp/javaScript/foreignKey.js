var defaultDataType;
var defaultDataSize;
var keySeparator;

var form;
var columns = new Array();
var foreignColumns = new Array();
var indexInfoMaxColumnsCount;

function Column(name, type, size, defaultValue)
{
    this.name = name;
    this.type = type;
    this.size = size;
    this.defaultValue = defaultValue;
}

function getColumnType(list, name)
{
    var i;
    var column;
    for (i = 0; i < list.length; i++)
    {
        column = list[i];
        if (column.name == name)
        {
            return column.type;
        }
    }
    return defaultDataType;
}

function getColumnSize(list, name)        
{
    var i;
    var column;
    for (i = 0; i < list.length; i++)
    {
        column = list[i];
        if (column.name == name)
        {
            return column.size;
        }
    }
    return defaultDataSize;
}

function getColumnDefaultValue(list, name)
{
    var i;
    var column;
    for (i = 0; i < list.length; i++)
    {
        column = list[i];
        if (column.name == name)
        {
            return column.defaultValue;
        }
    }
    return "";
}

function getColumnIndex(list, name)        
{
    var i;
    var column;
    for (i = 0; i < list.length; i++)
    {
        column = list[i];
        if (column.name == name)
        {
            return i;
        }
    }
    return -1;
}

function cancelForeignKey()
{
    form.elements["command"].value = "";
    form.submit();
}

function updateForeignColumns()
{
    var select = form.elements["foreignColumnsNames"];
    while (select.options.length > 1)
    {
        select.options[select.options.length - 1] = null;
    }
    document.getElementById("refreshLink").style.visibility = "visible";
    if (foreignColumns.length > 0)
    {
        document.getElementById("foreignColumnsDiv").style.display = "none";
    }
}

function refreshForeignColumns()
{
    form.elements["command"].value = "validateFK";
    form.elements["refreshForeignColumns"].value = "true";
    form.submit();
}

function updateColumnsLists()
{
    var count = getColumnsCount();
    var i;
    for (i = 0; i < indexInfoMaxColumnsCount; i++)
    {
        if (i < count)
        {
            form.elements["column_" + i].style.display = "";
        }
        else
        {
            form.elements["column_" + i].style.display = "none";
            form.elements["column_" + i].value = "";
        }
    }
    if (foreignColumns.length > 0)
    {
        var div = document.getElementById("foreignColumnsDiv");
        var table = getChild(getChild(div, "TABLE"), "TABLE");
        if (table != null && table.rows.length > 2)
        {
            for (i = 2; i < table.rows.length; i++)
            {
                updateCellsStyle(table.rows[i], false);
            }
            var fColumns = getForeignColumns();
            for (i = 0; i < fColumns.length; i++)
            {
                updateCellsStyle(table.rows[getColumnIndex(foreignColumns, fColumns[i]) + 2], true);
            }
        }
    }
}

function updateCellsStyle(row, highlight)
{
    var style = (highlight ? "ArrayCell linkedColumn" : "ArrayCell");
    var i;
    for (i = 0; i < row.cells.length; i++)
    {
        row.cells[i].className = style;
    }
}

function getColumnsCount()
{
    return getForeignColumns().length;
}

function getForeignColumns()
{
    return form.elements["foreignColumnsNames"].value.split(keySeparator);
}

function processSubmitForm()
{
    form.elements["command"].value = "validateFK";
    var fColumnsNames = getForeignColumns();
    var fColumnName;
    for (i = 0; i < fColumnsNames.length; i++)
    {
        fColumnName = fColumnsNames[i];
        form.elements["foreignColumnType_" + i].value = "" + getColumnType(foreignColumns, fColumnName);
        form.elements["foreignColumnSize_" + i].value = "" + getColumnSize(foreignColumns, fColumnName);
    }
    form.submit();
}