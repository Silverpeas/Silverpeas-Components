/*
 * Copyright (C) 2000 - 2011 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */



function loadTask(taskId, componentId) {
  // This method load the children of a tasks
  $.ajax({
    url: getContext()+'/RAjaxProjectManagerServlet/dummy', 
    data:{ Action:'loadTask',
      TaskId:taskId,
      ComponentId:componentId
    },
    success: function(data){
      /*if($("#spaceMenuDivId").isMasked()) {
        $("#spaceMenuDivId").unmask();
      }
      //alert("Success Data Loaded: data=" + data);*/
      displayTask(taskId, data);
      collapseTreeNodeDisplay(taskId);
      updateNumbering();
      highlightResponsible();
    },
    error : function() {
      //alert("XMLHttpRequest error ");
      $("#ajaxLoadError").dialog();
    },
    dataType: 'json'
  });
}

/**
 * Display a list of tasks from JSON response
 * @param taskId the current opened task identifier
 * @param data the json reponse data
 * @build the list of sub tasks of current tasks
 */
function displayTask(taskId, data) {
  var tasksSize = data.tasks.length - 1;
  //alert('number of tasks =' + tasksSize);
  $.each(data.tasks, function(i,item) {
    // First try to create a new table row by cloning another one and modifying this row
    //var newTaskRow = $("#taskRow" + taskId).clone();
    var newTaskId = 'taskRow' + item.id;
    //$(newTaskRow).attr("id", 'taskRow' + item.id);
    var taskClass = "";
    var statusName = "";
    switch (item.status) {
    case 0:
      taskClass = "in_progress";
      statusName = $("#hiddenInProgressId").val();
      break;
    case 1:
      taskClass = "frozen";
      statusName = $("#hiddenFrozenId").val();
      break;
    case 2: 
      taskClass = "lost";
      statusName = $("#hiddenCancelId").val();
      break;
    case 3:
      taskClass = "done";
      statusName = $("#hiddenDoneId").val();
      break;
    case 4:
      taskClass = "warning";
      statusName = $("#hiddenWarningId").val();
      break;
    case 5:
    default:
      taskClass = "not_started";
      statusName = $("#hiddenNotStartedId").val();
      break;
    }
    if (item.level > 1) {
      taskClass += " under_task level" + (item.level - 1);
      if (i == tasksSize) {
        taskClass += " last_child";
      }
    } else {
      taskClass += " task_row";
    }
    //$(newTaskRow).attr("class", taskClass);
    //$(newTaskRow).children(".task_wording").html('<div>' + item.name + '</div>');
    //newTaskRow.insertAfter("#taskRow" + taskId);
    var trHtml = '<tr id="' + newTaskId + '" class="' + taskClass + '" title="childOf_">';
    trHtml += '<td class="numerotation"></td>';
    trHtml += '<td class="task_wording"><div>';
    if (item.containsSubTask == 1) {
      // add link to load under tasks
      trHtml += '<a href="javascript:loadTask(\'' + item.id +'\', \'' + data.componentId + '\');" class="linkSee"  id="taskLink' + item.id + '">';
      trHtml += '<img border="0" alt="+" src="' + $("#hiddenExpandTreeImgId").val() + '" id="taskLinkImg' + item.id + '"></img>&#160;</a>';
    }
    trHtml += '<a title="' + $("#hiddenResponsibleId").val() + ' : ' + item.manager + '" href="ViewTask?Id=' + item.id + '" class="">' + item.name + '</a></div></td>';
    trHtml += '<td class="state"><p>' + statusName + '</p></td>';
    trHtml += '<td class="percentage"><p>' + item.percentage + ' %</p></td>';
    //trHtml += '<td colspan="31">&nbsp;</td>';
    
    //Initialize loop variable
    var startDay = item.startDate;
    var endDay = item.endDate;
    
    $('#emptyDays td').each(function(index) {
      // Compute day column class
      var isTaskDay = false;
      var curDay = $(this).text();
      var classDay = $(this).attr("class");
      if (curDay == startDay && curDay == endDay) {
        classDay += " lenght_oneDay task_start";
        isTaskDay = true;
      } else if (curDay == startDay) {
        classDay += " task_start";
        isTaskDay = true;
      } else if (curDay == endDay) {
        classDay += " task_end";
        isTaskDay = true;
      } else if (curDay > startDay && curDay < endDay) {
        classDay += " task";
        isTaskDay = true;
      }

      if (isTaskDay) {
        for (keyVar in listHolidays) {
          if (listHolidays[keyVar] == curDay) {
            classDay += " day_unworked";
            break;
          }
        }
      }
      
      trHtml += '<td class="'+ classDay + '">';
      // Accessibility need: don't remove the code below
      if (isTaskDay) {
        trHtml += "<div>&nbsp;<div><span>x</span></div></div>";
      }
      trHtml += '</td>';
    });
    
    trHtml += '</tr>';
    var newTaskRow = $('' + trHtml);
    //newTaskRow.inserAfter();
    $("#taskRow" + taskId).after(newTaskRow);
    //increment loop variable
    taskId = item.id;
  });

}

/**
 * void method which updates all the tasks number in the first field
 */
function updateNumbering() {
  $("td .numerotation").each(function(index) {
    $(this).text((index + 1) + '.');
  });
}

/**
 * @param taskId
 * @display a collapsable image and change src action on link
 */
function collapseTreeNodeDisplay(taskId) {
  $("#taskLink" + taskId).attr("href", "javascript:collapseTask('" + taskId +"','" + $("#hiddenComponentId").val() + "');");
  $("#taskLinkImg" + taskId).attr("src", $("#hiddenCollapseTreeImgId").val());
  $("#taskLinkImg" + taskId).attr("alt", "-");
}

function collapseTask(taskId, componentId) {
  // This method remove the children of a tasks
  $.ajax({
    url: getContext()+'/RAjaxProjectManagerServlet/dummy', 
    data:{ Action:'collapseTask',
      TaskId:taskId,
      ComponentId:componentId
    },
    success: function(data){
      /*if($("#spaceMenuDivId").isMasked()) {
        $("#spaceMenuDivId").unmask();
      }
      //alert("Success Data Loaded: data=" + data);*/
      removeTask(data);
      expandTreeNodeDisplay(taskId);
    },
    error : function() {
      //alert("XMLHttpRequest error ");
      $("#ajaxLoadError").dialog();
    },
    dataType: 'json'
  });
}


/**
 * @param taskId
 * @display a collapsable image and change src action on link
 */
function expandTreeNodeDisplay(taskId) {
  $("#taskLink" + taskId).attr("href", "javascript:loadTask('" + taskId +"','" + $("#hiddenComponentId").val() + "');");
  $("#taskLinkImg" + taskId).attr("src", $("#hiddenExpandTreeImgId").val());
  $("#taskLinkImg" + taskId).attr("alt", "+");
}

/**
 * Remove the table row which display the tasks
 * @param data : list of JSON tasks parameter to remove
 */
function removeTask(data) {
  $.each(data.tasks, function(i,item) {
    // Remove each task that are given in JSON parameter
    //alert("Remove element : taskRow" + item.id);
    $('#taskRow' + item.id).remove();
  });
}
