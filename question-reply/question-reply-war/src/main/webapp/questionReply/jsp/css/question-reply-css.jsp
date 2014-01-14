<%@page contentType="text/css; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

.hautFrame form > ul,
ul.questions, 
ul.questions > li, 
li.category, 
.answers > ul, 
li.answer, 
.questionTitle > h4, 
.categoryTitle > h3, 
h5.answerTitle{
  margin:0;
  padding:0;
  list-style-type:none;
}

li.category {
  margin-top:10px;
}

.categoryTitle{
  position:relative;
  padding:10px 5px 5px 30px;
  min-height:19px;
  cursor:pointer;
  background:#EAEAEA url("<c:url value="/util/icons/arrow/open.gif" />") 5px center no-repeat;
  border:1px solid #EAEAEA;
  border-radius:10px;
}

.select .categoryTitle {
  background:#DDDDDD url() 5px  center no-repeat;
  border:1px solid #DDDDDD;
  border-radius:10px;
  padding-left:10px;
  cursor:default;
}

.hover .categoryTitle {
  background-color:#DDDDDD;
  border:1px solid #DDDDDD;
  border-radius:10px;
}

.categoryTitle .categoryTitle {
  padding:0;
  background-image:none;
  border:0;
}

.categoryTitle h3 {
  font-size: 11px;
  font-weight: bold;
}

.action {
  position:absolute;
  top:0px;
  right:10px;
}
.action img{
  margin:5px 5px 0;
}
.status {
  position:absolute;
  top:4px;
  left:4px;
}

div.question{	
  background-color:#EFF8E3;	
  position:relative;
  border:1px solid #CFCFCF;
  margin: 10px 5px 0;
}

.question .action{
  background-color:#EAEAEA;
  border-left:1px solid #CFCFCF;
  height:100%;
  width:142px;
  right:0px;
}

.question .action a {
  position:absolute;
}
.question .action img {
  position:relative;
}

.question .action .reply {	top:5px;	left:2px;	}
.question .action .open {	top:3px;	left:31px;	}
.question .action .update {	top:3px;	left:60px;	}
.question .action .delete {	top:3px;	left:89px;	}
.question .action .checkbox {position:absolute; top:7px;	left:118px;}

.question .permalink {
  margin-right: 5px;
}

.question .permalink img {
  vertical-align: middle;
}

.questionTitle {
  line-height: 140%;
  margin: 6px 142px 5px 25px;
  cursor:pointer;	/* Cursor is like a hand when someone rolls the mouse over the question */
}

.questionTitle h4 {
  font-weight:100;
}

li.answer {
  position:relative;    
  color: #333333;
  padding: 4px 0 10px 2px;
  border-radius: 5px;
  -moz-border-radius: 5px;
  -webkit-border-radius: 5px;
  box-shadow:5px 5px 10px #cccccc;
  -moz-box-shadow:5px 5px 10px #cccccc;
  -webkit-box-shadow:5px 5px 10px #cccccc;
  background-color:#FFF;
  margin-left: auto;
  margin-right: auto;
  margin-bottom: 10px;
}

.answers{ 
  background-color: #EDEDED;
  border:1px solid #B3BFD1;
  border-top:0;
  border-bottom:0;
  margin: 0 10px;
  padding: 0 10px  10px;
}

.answers p {
  margin:0;
  padding:10px 0;
}

h5.answerTitle {
  font-size:100%;
  padding:5px 50px 10px 24px;
}

.answerContent {
  padding:0px 10px 8px 24px;
  overflow:auto;
}

/* pour que les listes de l'éditeur est le bon comportement */

.answerContent  ul 			{ list-style-type:disc;}
.answerContent  ul ul		{ list-style-type:circle;}
.answerContent  ul ul ul	{ list-style-type:square;}

.answerContent table.tableBoard {
  width:auto !important;
  float:right;
  margin:10px;}

.answerAuthor {
  display:block;
  padding:5px 15px 10px 24px;
  text-align:right;
  font-style:italic;
  font-size:11px;
}

.attachments {
 float:right;
 overflow:hidden;
 display:inline;
}

* html .attachments  {
 width:30%;
}

#portlet-faq .cellBrowseBar,
#portlet-faq .cellOperation {
  display: none;
}

#portlet-faq .questionTitle {
  margin: 6px 25px 5px 25px;
}

.updateR input{
	width:70%;
	maw-width:500px;
}