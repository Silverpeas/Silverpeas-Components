var updateTableAction;
var form;

var columnWindow = null;
var keysImpacts = new Array();

function cancelUpdate()
{
    form.elements["command"].value = "cancel";
    form.submit();
}

function modifyColumn(name, type, value)
{
    var fkErrorForm = document.forms["fkErrorForm"];
    fkErrorForm.elements["columnName"].value = name;
    fkErrorForm.elements["type"].value = type;
    fkErrorForm.elements["correctedValue"].value = value;
    fkErrorForm.submit();
}

function openColumnWindow(url)
{
    closeColumnWindow();
    columnWindow = SP_openWindow(url, "NewColumn", "600", "400", "toolbar=no, directories=no, menubar=no, locationbar=no ,resizable, scrollbars");
    columnWindow.focus();
}

function openErrorColumnWindow(errorIndex)
{
    var url = updateTableAction + "?command=displayColumn&index=" + errorIndex + "&errorColumn=true";
    openColumnWindow(url);
}

function closeColumnWindow()
{
    if (columnWindow != null && !columnWindow.closed)
    {
        columnWindow.close();
    }
}

function validateColumn()
{
    var columnForm = columnWindow.document.forms[0];
    form.elements["command"].value = "validateColumn";
    var params = "";
    var i;
    var value;
    for (i = 0; i < columnForm.elements.length; i++)
    {
        value = columnForm.elements[i].value;
        if (value != "")
        {
            params += (params.length == 0 ? "?" : "&") + columnForm.elements[i].name + "=" + value;
        }
    }
    form.action = updateTableAction + params;
    columnWindow.close();
    form.submit();
}

function updatePrimaryKey(update)
{
    form.elements["command"].value = (update ? "displayPK" : "removePK");
    form.submit();
}

function updateUnicityKey(index, update)
{
    form.elements["command"].value = (update ? "displayUK" : "removeUK");
    form.elements["index"].value = index;
    form.submit();
}

function updateForeignKey(index, update)
{
    form.elements["command"].value = (update ? "displayFK" : "removeFK");
    form.elements["index"].value = index;
    form.submit();
}