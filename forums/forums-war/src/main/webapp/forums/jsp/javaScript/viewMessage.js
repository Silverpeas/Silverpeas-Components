var currentMessageId = -1;
var parentMessageId = -1;

function replyMessage(messageId)
{
	currentMessageId = messageId;
    var divList = document.getElementsByTagName("DIV");
    var div;
    var divId;
    var i;
    var refDivId = "msgContent" + messageId;
    for (i = 0; i < divList.length; i++)
    {
    	div = divList[i];
    	divId = div.id;
        if (divId != null && divId.length > 10 && divId.substring(0, 10) == "msgContent"
            && divId != refDivId)
        {
            div.style.display = "none";
        }
    }
    var responseTable = document.getElementById("responseTable");
    if (responseTable != null)
    {
    	responseTable.style.display = "block";
    	initCKeditor();
    }
    var backButton = document.getElementById("backButton");
    if (backButton != null)
    {
    	backButton.style.display = "none";
    }
    scrollMessageList(messageId);
    callResizeFrame();
}

function cancelMessage()
{
    var messageId = currentMessageId;
	currentMessageId = -1;
    var divList = document.getElementsByTagName("DIV");
    var div;
    var divId;
    var i;
    for (i = 0; i < divList.length; i++)
    {
        div = divList[i];
        divId = div.id;
        if (divId != null && divId.length > 10 && divId.substring(0, 10) == "msgContent")
        {
            div.style.display = "block";
        }
    }
    var responseTable = document.getElementById("responseTable");
    if (responseTable != null)
    {
    	resetText();
        responseTable.style.display = "none";
    }
    var backButton = document.getElementById("backButton");
    if (backButton != null)
    {
        backButton.style.display = "block";
    }
    scrollMessage(messageId);
    callResizeFrame();
}

function scrollMessageList(messageId)
{
	document.getElementById("msgLine" + parentMessageId).className = (messageId == parentMessageId ? "msgLine" : "");
    var msgDiv = document.getElementById("msgDiv");
    if (msgDiv != null)
    {
    	var msgTable = document.getElementById("msgTable");
        if (msgDiv.offsetHeight > msgTable.scrollHeight)
        {
        	msgDiv.style.height = msgTable.scrollHeight;
        }

        var i;
        var row;
        var searchedId = "msgLine" + messageId;
        if (msgDiv.scrollHeight > msgDiv.offsetHeight)
        {
            for (i = 0; i < msgTable.rows.length; i++)
            {
                row = msgTable.rows[i];
                if (row.id == searchedId)
                {
                	msgDiv.scrollTop = row.offsetTop;
                }
            }
        }
        for (i = 0; i < msgTable.rows.length; i++)
        {
            row = msgTable.rows[i];
            row.className = (row.id == searchedId ? "msgLine" : "");
        }
    }
}


function scrollMessage(messageId)
{
    if (currentMessageId != -1)
    {
    	cancelMessage();
    }
	document.location.href = "#msg" + messageId;
	scrollMessageList(messageId);
}

function scrollTop()
{
	document.location.href = "#topPage";
}