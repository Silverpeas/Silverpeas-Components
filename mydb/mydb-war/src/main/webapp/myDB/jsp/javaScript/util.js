function removeAccents(element)
{
    var value = element.value;
    value = value.replace(/[באגהד]/g, "a");
    value = value.replace(/[יטךכ]/g, "e");
    value = value.replace(/[םלמן]/g, "i");
    value = value.replace(/[ףעפצץ]/g, "o");
    value = value.replace(/[תשûü]/g, "u");
    value = value.replace(/[ ]/g, "");
    element.value = value;
}

function isSqlValidName(value)
{
    var re = /^([a-zA-Z]+([a-zA-Z0-9_]*[a-zA-Z0-9])*)$/;
    return re.test(value);
}

function getChild(element, tagName)
{
    if (element != null)
    {
        var i = 0;
        var child;
        for (i = 0; i < element.childNodes.length; i++)
        {
            child = element.childNodes[i];
            if (child.tagName == tagName)
            {
                return child;
            }
        }
        for (i = 0; i < element.childNodes.length; i++)
        {
            child = getChild(element.childNodes[i], tagName);
            if (child != null)
            {
                return child;
            }
        }
    }
    return null;
}

function getParent(element, tagName)
{
    if (element != null)
    {
        var i = 0;
        var parent = element.parentNode;
        while (parent != null)
        {
            if (parent.tagName == tagName)
            {
                return parent;
            }
            parent = parent.parentNode;
        }
    }
    return null;
}

function getOffsets(element)
{
    var offsetTop = element.offsetTop;
    var offsetLeft = element.offsetLeft;
    element = element.offsetParent;
    while (element != null)
    {
        offsetTop += element.offsetTop;
        offsetLeft += element.offsetLeft;
        element = element.offsetParent;
    }
    return new Array(offsetTop, offsetLeft);
}

function Warning(startLabel, endLabel)
{
    this.startLabel = startLabel;
    this.detail = new Array();
    this.endLabel = endLabel;
}

function addWarningDetail(warning, detail)
{
    warning.detail[warning.detail.length] = detail;
}

function displayWarning(warning)
{
    var result = warning.startLabel + " :";
    var i;
    for (i = 0; i < warning.detail.length; i++)
    {
        result += "\n\n - " + warning.detail[i];
    }
    result += "\n\n" + warning.endLabel;
    return result;
}