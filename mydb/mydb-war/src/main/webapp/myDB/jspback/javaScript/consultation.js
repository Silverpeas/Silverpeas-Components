  $(document).ready(function() {
    init();
     // By suppling no content attribute, the library uses each elements title attribute by default
     $('#fieldValue[title]').qtip({
    content: {
      text: false
    },
    style: {
      tip: true,
      classes: "qtip-shadow qtip-green"
    },
    position: {
      adjust: {
        method: "flip flip"
      },
      at: "top right",
      my: "bottom left",
      viewport: $(window)
    }
     });
  });

var tableDiv = null;

function detail(index, command)
{
    var form = document.forms["processForm"];
    form.elements["index"].value = index;
    form.elements["command"].value = command;
    form.action = "UpdateData";
    form.submit();
}

function filterData()
{
    document.forms["filterForm"].submit();
}

function init()
{
    tableDiv = document.getElementById("tableDiv");
    if (tableDiv != null)
    {
        window.onresize = resizeDiv;
        resizeDiv();
    }
}

function resizeDiv()
{
    tableDiv.style.display = "none";
    
    var table = getParent(tableDiv, "TABLE");
    if (table) {
        tableDiv.style.width = table.offsetWidth - 10;
    }
    
    tableDiv.style.display = "";

    var center = getChild(tableDiv, "CENTER");
    table = getChild(center, "TABLE");
    if (table) {
        var height = table.offsetHeight;
        if (height > 0)
        {
            tableDiv.style.height = height + 15;
        }
    }
}