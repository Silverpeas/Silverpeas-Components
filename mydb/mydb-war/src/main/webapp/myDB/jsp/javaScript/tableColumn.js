var form;
var dataTypes = new Array();

function DataType(name, type, sizeEnabled)
{
    this.name = name;
    this.type = type;
    this.sizeEnabled = sizeEnabled;
}

function updateFields()
{
    var columnType = form.elements["columnType"].value;
    var i = 0;
    var dataType = getDataType(columnType);
    if (dataType.sizeEnabled)
    {
        form.elements["columnSize"].disabled = false;
    }
    else
    {
        form.elements["columnSize"].value = "";
        form.elements["columnSize"].disabled = true;
    }
}

function getDataType(type)
{
    var i;
    for (i = 0; i < dataTypes.length; i++)
    {
        if (dataTypes[i].type == type)
        {
            return dataTypes[i];
        }
    }
    return null;
}