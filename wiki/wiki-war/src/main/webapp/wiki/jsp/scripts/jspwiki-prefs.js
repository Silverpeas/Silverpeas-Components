var WikiGroup={MembersID:"membersfield",GroupTltID:"grouptemplate",GroupID:"groupfield",NewGroupID:"newgroup",GroupInfoID:"groupinfo",CancelBtnID:"cancelButton",SaveBtnID:"saveButton",CreateBtnID:"createButton",DeleteBtnID:"deleteButton",groups:{"(new)":{members:"",groupInfo:""}},cursor:null,isEditOn:false,isCreateOn:false,putGroup:function(_1,_2,_3,_4){
this.groups[_1]={members:_2,groupInfo:_3};
var g=$(this.GroupTltID);
var gg=g.clone().setHTML(_1);
gg.id="";
g.parentNode.appendChild(gg);
$(gg).show();
if(_4||!this.cursor){
this.onMouseOverGroup(gg);
}
},onMouseOverGroup:function(_7){
if(this.isEditOn){
return;
}
this.setCursor(_7);
var g=this.groups[(_7.id==this.GroupID)?"(new)":_7.innerHTML];
$(this.MembersID).value=g.members;
$(this.GroupInfoID).innerHTML=g.groupInfo;
},setCursor:function(_9){
if(this.cursor){
$(this.cursor).removeClass("cursor");
}
this.cursor=$(_9).addClass("cursor");
},onClickNew:function(){
if(this.isEditOn){
return;
}
this.isCreateOn=true;
$(this.MembersID).value="";
this.toggle();
},toggle:function(){
this.isEditOn=!this.isEditOn;
$(this.MembersID).disabled=$(this.SaveBtnID).disabled=$(this.CreateBtnID).disabled=$(this.CancelBtnID).disabled=!this.isEditOn;
var _a=$(this.DeleteBtnID);
if(_a){
_a.disabled=this.isCreateOn||!this.isEditOn;
}
if(this.isCreateOn){
$(this.CreateBtnID).toggle();
$(this.SaveBtnID).toggle();
}
var _b=$(this.NewGroupID),_c=$(this.MembersID);
if(this.isEditOn){
_c.getParent().addClass("cursor");
_b.disabled=!this.isCreateOn;
if(this.isCreateOn){
_b.focus();
}else{
_c.focus();
}
}else{
_c.getParent().removeClass("cursor");
if(this.isCreateOn){
this.isCreateOn=false;
_b.value=_b.defaultValue;
_c.value="";
}
_b.blur();
_c.blur();
_b.disabled=false;
}
},onSubmitNew:function(_d,_e){
var _f=$(this.NewGroupID);
if(_f.value==_f.defaultValue){
alert("group.validName".localize());
_f.focus();
}else{
this.onSubmit(_d,_e);
}
},onSubmit:function(_10,_11){
if(!this.cursor){
return false;
}
var g=(this.cursor.id==this.GroupID)?$(this.NewGroupID).value:this.cursor.innerHTML;
_10.setAttribute("action",_11);
_10.group.value=g;
_10.members.value=$(this.MembersID).value;
_10.action.value="save";
Wiki.submitOnce(_10);
_10.submit();
}};

