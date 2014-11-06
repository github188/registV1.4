function realInit() {
	$('#realList1a').accordion();
	$("#platformTree").treeview({
		control : "#treecontrol",
		persist : "cookie",
		cookieId : "treeview-black"
	});
}

// 平台搜索
function queryPlatform() {
	var platformName = $("#platformName").val();
	var platforms = $("#platformTree .organLi");
	platforms.each(function() {
		if ($(this).html().indexOf(platformName) != -1) {
			$(this).css("color", "red");
		}
	});
}

function setOrganId(form, method, id, name) {
	$("#" + form).ajaxSubmit({
		type : "post",
		url : ctxpath + "/organ/" + method,
		success : function(msg) {
			if (msg == "no") {
				$("#" + id).val("");
				$("#" + name).val("所有机构");
			} else {
				$("#" + id).val(msg);
			}
		}
	});
}

var currentTab = "";

function mouseOnTab(id) {
	setCurrentTabCss(id);
}
function mouseOutTab(id) {
	if (currentTab != id) {
		setTabCss(id);
	}
}

function getTabHtml(title) {
	return "<div class='tabLeft'></div>" + "<div class='tabCenter'>" + title
			+ "</div>" + "<div class='tabRight'></div>";
}



function setTabsCss(currentTabId) {
	$("ul.navbar-nav >li.classic-menu-dropdown").removeClass("active");
	$("#"+currentTabId).addClass("active");
	sharedResource = null;
	$("#queryGBDevice").hide();
}

function grantedManage() {
	setTabsCss('grantedManage');
	$.ajax({
			type : "GET",
			url : ctxpath
					+ "/granted/getGrantedList",
			data:{
				"page.currentPage" : 1,
				"page.recordCount" : 0,
				"page.position":"userList"
			},
			success : function(msg) {
				$("#content").html(msg);
			}
		});
}

function getGrantedList(){
	if($("[name='grantedType']", "#grantedForm").val()=='USER'){
		setSelected("roleList",selectedRole);
		getUserList();
	}else{
		setSelected("userList",selectedUser);
		getRoleList();
	}
}

function getUserOrganTree(){
	var formName = "#grantedForm";
	$('#userOrganTree').bind("select_node.jstree",function(e, data) {
				$("[name=organId]", formName).val(data.node.a_attr['organId']);
				$("[name=organName]", formName).val(data.node.text);
				$("#userOrganTreeModal .close").click();
				getGrantedList();
	        }).jstree(true);
	$("#userOrganTreeModalEntry").click();
}

function getResourcePlatformTree(){
	var formName = "#resourceForm";
	$('#resourcePlatformTree').bind("select_node.jstree",function(e, data) {
		var platformId = data.node.id=="0"?"":data.node.id;
		$("[name=platformId]", formName).val(platformId);
		$("[name=cmsName]", formName).val(data.node.text);
		$("#resourcePlatformTreeModal .close").click();
		getResourceList();
	}).jstree(true);
	$("#resourcePlatformTreeModalEntry").click();
}

function getSelectedGrantedList(){
	if($("[name='grantedType']", "#selectedGrantedForm").val()=='USER'){
		getSelectedUserList();
	}else{
		getSelectedRoleList();
	}
}

function getSelectedUserList(page, count){
	if (!page) {
		page = 1;
	}
	if (!count) {
		count = 0;
	}
	$("#selectedGrantedForm").ajaxSubmit({
		type : 'post',
		url : ctxpath + "/granted/getSelectedUserList",
		data : {
			"page.currentPage" : page,
			"page.recordCount" : count,
			"page.position":"selectedUserList",
			"userIds":getSimpleJSON(selectedUser),
		},
		success : function(msg) {
				$("#selectedGrantedList").html(msg);
		}
	});
}

function getSelectedRoleList(page,count){
	if (!page) {
		page = 1;
	}
	if (!count) {
		count = 0;
	}
	$("#selectedGrantedForm").ajaxSubmit({
		type : 'post',
		url : ctxpath + "/granted/getSelectedRoleList",
		data : {
			"page.currentPage" : page,
			"page.recordCount" : count,
			"page.position":"selectedRoleList",
			"roleIds":getSimpleJSON(selectedRole),
		},
		success : function(msg) {
				$("#selectedGrantedList").html(msg);
		}
	});
}

function getRoleList(page, count){
	setSelected("roleList",selectedRole);
	if (!page) {
		page = 1;
	}
	if (!count) {
		count = 0;
	}
	$("#grantedForm").ajaxSubmit({
		type : 'post',
		url : ctxpath + "/granted/getRoleList",
		data : {
			"page.currentPage" : page,
			"page.recordCount" : count,
			"page.position":"roleList",
		},
		success : function(msg) {
				$("#grantedList").html(msg);
				$("#roleList input:checkbox").each(function(idx, data) {
					if ($.inArray($(this).val(), selectedRole) != -1) {
						$(this).attr("checked",true);
					}
                    
				});
				jQuery.uniform.update();
		}
	});
}

var selectedUser = [];
var selectedRole = [];
var selectedOrgan = [];
var selectedDevice = [];

function getUserList(page, count, organId){
	setSelected("userList",selectedUser);
	if (!page) {
		page = 1;
	}
	if (!count) {
		count = 0;
	}
	if (organId) {
		$("[name='organId']", "#grantedForm").val(organId);
	}
	$("#grantedForm").ajaxSubmit({
		type : 'post',
		url : ctxpath + "/granted/getUserList",
		data : {
			"page.currentPage" : page,
			"page.recordCount" : count,
			"page.position":"userList",
		},
		success : function(msg) {
				$("#grantedList").html(msg);
				$("#userList input:checkbox").each(function(idx, data) {
					if ($.inArray($(this).val(), selectedUser) != -1) {
						$(this).attr("checked",true);
					}
				});
				jQuery.uniform.update();
		}
	});
	
}

function setSelected(id,selectedArr){
	$('#'+id+' :checked').each(function() {
		if($.inArray($(this).val(),selectedArr)==-1){
			selectedArr.push($(this).val());
		}
	});
	
	$("#"+id+" input:checkbox:not(:checked)").each(function() {
		var idx = $.inArray($(this).val(),selectedArr);
		if(idx != -1){
			selectedArr.splice(idx,1);
		}
	});
}


function showHome(){
	$.each($("ul.navbar-nav >li.classic-menu-dropdown"),function(idx,data){
		$(this).click();
		return false;
	});
}

function getResourceList(){
	var resourceType = $("[name='resourceType']", "#resourceForm").val();
	if(resourceType == 'platform' || resourceType == 'organ' || resourceType=='root'){
		setSelected("deviceList",selectedDevice);
		getOrganList();
	}else{
		setSelected("organList",selectedOrgan);
		getDeviceList();
	}
}

function getDeviceList(page, count){
	setSelected("deviceList",selectedDevice);
	if (!page) {
		page = 1;
	}
	if (!count) {
		count = 0;
	}
	var resourceType=$("[name='resourceType']", "#resourceForm").val();
	$("#resourceForm").ajaxSubmit({
		type : 'post',
		url : ctxpath + "/granted/getDeviceList",
		data : {
			"page.currentPage" : page,
			"page.recordCount" : count,
			"page.position":"deviceList",
			"type":resourceType=='device'?"":resourceType
		},
		success : function(msg) {
				$("#resourceList").html(msg);
				$("#deviceList input:checkbox").each(function(idx, data) {
					if ($.inArray($(this).val(), selectedDevice) != -1) {
						$(this).attr("checked",true);
					}
				});
				jQuery.uniform.update();
		}
	});
}

function getOrganList(page, count){
	setSelected("organList",selectedOrgan);
	if (!page) {
		page = 1;
	}
	if (!count) {
		count = 0;
	}
	$("#resourceForm").ajaxSubmit({
		type : 'post',
		url : ctxpath + "/granted/getOrganList",
		data : {
			"page.currentPage" : page,
			"page.recordCount" : count,
			"page.position":"organList",
			"sourceType":$("[name='resourceType']", "#resourceForm").val(),
		},
		success : function(msg) {
				$("#resourceList").html(msg);
				$("#organList input:checkbox").each(function(idx, data) {
					if ($.inArray($(this).val(), selectedOrgan) != -1) {
						$(this).attr("checked",true);
					}
				});
				jQuery.uniform.update();
		}
	});
}

function confirmAuthorization(){
	$.ajax({
		type:'post',
		url:ctxpath +"/granted/confirmAuthorization",
		data:{
			"organIds" :getSimpleJSON(selectedOrgan)
		},
		success:function(msg){
			$("#tab3").html(msg);
		}
	});
}
var permissionArr =['实时','历史','云台','抓拍','预置位'];
function authorization(){
	var bit = $("#select_permission").val().split(",");
	var permission = [];
	$.each(permissionArr,function(idx,data){
		if($.inArray(data,bit)!=-1){
			permission.push('1');
		}else{
			permission.push('0');
		}
	});
	$.ajax({
		type:'post',
		url:ctxpath +"/granted/authorization",
		data:{
			"organIds" :getSimpleJSON(selectedOrgan),
			"deviceIds" :getSimpleJSON(selectedDevice),
			"userIds" :getSimpleJSON(selectedUser),
			"roleIds" :getSimpleJSON(selectedRole),
			"permission" :permission.join(''),
		},
		success:function(msg){
			if(msg=='success'){
            	jSuccess("操作成功！");
            	selectedUser = [];
            	selectedRole =[];
            	selectedOrgan =[];
            	selectedDevice= [];
            	showHome();
			}else{
				jError("操作失败！");
			}
		}
	});
}

function getSelectedResourceList(){
	var resourceType = $("[name='resourceType']", "#selectedResourceForm").val();
	if(resourceType == 'root' ||resourceType =='platform' || resourceType == 'organ'){
		getSelectedOrganList();
	}else{
		getSelectedDeviceList();
	}
}


function getSelectedOrganList(page, count){
	if (!page) {
		page = 1;
	}
	if (!count) {
		count = 0;
	}
	$("#selectedResourceForm").ajaxSubmit({
		type : 'post',
		url : ctxpath + "/granted/getSelectedOrganList",
		data : {
			"page.currentPage" : page,
			"page.recordCount" : count,
			"page.position":"selectedOrganList",
			"sourceType":$("[name='resourceType']", "#selectedResourceForm").val(),
			"organIds":getSimpleJSON(selectedOrgan),
		},
		success : function(msg) {
				$("#selectedResourceList").html(msg);
		}
	});
}

function getSelectedDeviceList(page, count){
	if (!page) {
		page = 1;
	}
	if (!count) {
		count = 0;
	}
	var resourceType=$("[name='resourceType']", "#selectedResourceForm").val();
	$("#selectedResourceForm").ajaxSubmit({
		type : 'post',
		url : ctxpath + "/granted/getSelectedDeviceList",
		data : {
			"page.currentPage" : page,
			"page.recordCount" : count,
			"page.position":"selectedDeviceList",
			"deviceIds":getSimpleJSON(selectedDevice),
			"type":resourceType=='device'?"":resourceType
		},
		success : function(msg) {
			$("#selectedResourceList").html(msg);
		}
	});
}

function getSimpleJSON(selected){
	var parent = new Object();
	parent.id="0";
	parent.children =[];
	$.each(selected,function(idx,id){
		var child = Object();
		child.id=id;
		parent.children[idx] = child;
	});
	return JSON.stringify([parent]);
}

function getPlatformTree() {
	var formName = "#createParentPlatformForm";
	$('#platformTree').bind("select_node.jstree",function(e, data) {
				$("[name=platformId]", formName).val(data.node.id);
				$("[name=platformName]", formName).val(data.node.text);
				$("[name='platform.childCmsId']", formName).val(data.node.a_attr['gbPlatformCmsId']);
				if(data.node.a_attr['gbPlatformCmsId']!=""){
					$("[name='platform.childCmsId']", formName).attr("readonly","true");
				}else{
					$("[name='platform.childCmsId']", formName).attr("readonly",false);
				}
				$("#platformTreeModal .close").click();
	        }).jstree(true);
	$("#platformTreeModalEntry").click();
}
function getPlatformTreeForChild() {
	var formName = "#createChildPlatformForm";
	$('#platformTree').bind("select_node.jstree",function(e, data) {
		$("[name=platformId]", formName).val(data.node.id);
		$("[name=platformName]", formName).val(data.node.text);
		$("[name='gbPlatformCmsId']", formName).val(data.node.a_attr['gbPlatformCmsId']);
		if(data.node.a_attr['gbPlatformCmsId']!=""){
			$("[name='gbPlatformCmsId']", formName).attr("readonly","true");
		}else{
			$("[name='gbPlatformCmsId']", formName).attr("readonly",false);
		}
		$("#platformTreeModal .close").click();
	}).jstree(true);
	$("#platformTreeModalEntry").click();
}

function setPlatformTreeForGB(treeId) {
	$('#' + treeId).jstree({
		"core" : {
			"animation" : 0,
			"check_callback" : true,
			"themes" : {
				"stripes" : true
			},
			'data' : {
				'type' : 'POST',
				'dataType' : 'JSON',
				'url' : function(node) {
					return ctxpath+'/organTree/getPlatformTreeForGB';
				},
				'data' : function(node) {
					return {
						"id" : node.id
					};
				}
			}
		},
		"types" : {
			"#" : {
				"max_children" : 1,
				"max_depth" : 4,
				"valid_children" : [ "root" ]
			},
			"platform" : {
				"icon" : "fa fa-sitemap",
				"valid_children" : [ "platform","organ" ]
			},
			"organ" : {
				"icon" : "fa fa-th-list",
				"valid_children" : [ "organ" ]
			},
		},
		"plugins" : [ "types", "wholerow" ]
	});
}
function setUserOrganTree(treeId) {
	$('#' + treeId).jstree({
		"core" : {
			"animation" : 0,
			"check_callback" : true,
			"themes" : {
				"stripes" : true
			},
			'data' : {
				'type' : 'POST',
				'dataType' : 'JSON',
				'url' : function(node) {
					return ctxpath+'/granted/getUserOrganTree';
				},
				'data' : function(node) {
					return {
						"id" : node.id
					};
				}
			}
		},
		"types" : {
			"#" : {
				"max_children" : 1,
				"max_depth" : 4,
				"valid_children" : [ "root" ]
			},
			"platform" : {
				"icon" : "fa fa-sitemap",
				"valid_children" : [ "platform","organ" ]
			},
			"organ" : {
				"icon" : "fa fa-th-list",
				"valid_children" : [ "organ" ]
			},
		},
		"plugins" : [ "types", "wholerow" ]
	});
}

function setResourcePlatformTree(treeId){
	$('#' + treeId).jstree({
		"core" : {
			"animation" : 0,
			"check_callback" : true,
			"themes" : {
				"stripes" : true
			},
			'data' : {
				'type' : 'POST',
				'dataType' : 'JSON',
				'url' : function(node) {
					return ctxpath+'/granted/getResourePlatformTree';
				},
				'data' : function(node) {
					return {
						"id" : node.id
					};
				},
				'success':function(msg){
				}
			}
		},
		"types" : {
			"#" : {
				"max_children" : 1,
				"max_depth" : 4,
				"valid_children" : [ "root" ]
			},
			"platform" : {
				"icon" : "fa fa-sitemap",
				"valid_children" : [ "platform","organ" ]
			},
			"organ" : {
				"icon" : "fa fa-th-list",
				"valid_children" : [ "organ" ]
			},
		},
		"plugins" : [ "types", "wholerow" ]
	});
}

function roleMain() {
	$.ajax({
		type : "GET",
		url : ctxpath + "/role/forRoleMain",
		success : function(msg) {
			$("#roleMain").html(msg);
		}
	});
}



function queryRoleList() {
	$("#roleForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/role/getRoleList",
		success : function(msg) {
			$("#roleList").html(msg);
		}
	});
}

// 共享平台,资源共享
function resourceShare() {
	if (interval) {
		window.clearInterval(interval);
	}
	if (onlineCheck) {
		window.clearInterval(onlineCheck);
	}
	setTabsCss("tab5");
	$.ajax({
		type : "get",
		url : ctxpath + "/share/getResource",
		success : function(msg) {
			$("#tab5").html(getTabHtml("资源共享"));
			setCurrentTabCss("tab5");
			$("#content").html(msg);
			shareProcess(1);
		}
	});
}

// 共享平台,资源回收
function resourceManage(cmsId) {
	if (interval) {
		window.clearInterval(interval);
	}
	if (onlineCheck) {
		window.clearInterval(onlineCheck);
	}
	setTabsCss("tab6");
	$
			.ajax({
				type : "get",
				url : ctxpath
						+ "/share/getOuterPlatforms?r.first=true&r.original=true&r.cmsId="
						+ cmsId,
				success : function(msg) {
					$("#tab6").html(getTabHtml("资源回收"));
					setCurrentTabCss("tab6");
					$("#content").html(msg);
					$("#shareOne").css("color", "#5CC1ED");
				}
			});
}

/**
 * 平台管理主页
 * 
 * @param cmsId
 * @return
 */
var onlineCheck;
function platformManage() {
	setTabsCss('platformManage');
	$.ajax({
		type : "GET",
		url : ctxpath + "/gbOuterDevice/showPlatformList",
		success : function(msg) {
			$("#content").html(msg);
// onlineCheck = window.setInterval("checkPlatformStatus()", 6000);
			if(receiveStatus){
				showReceiveDeviceDetail(receiveStatus);
			}
		}
	});
}

// 角色创建
function createRole() {
	$("#createRoleForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/role/createRole",
		beforeSubmit : function() {
			if ($.trim($("#roleName").val()) == "") {
				jError("角色名称不能为空");
				return false;
			}
		},
		success : function(msg) {
			$("#createRoleModal .close").click();
			if (msg == "success") {
				jSuccess("操作成功");
				setTimeout(function(){roleMain();},500);
			} else if (msg == "name.has.exist") {
				jError("该角色名称已经存在");
			}
			
		}
	});
}

// 删除角色
function deleteRole(roleId) {
	if (confirm("确定要删除该角色吗?")) {
		$.ajax({
			type : "post",
			url : ctxpath + "/role/deleteRole?id=" + roleId,
			success : function(msg) {
				if (msg == "success") {
					queryRoleList();
				}
			}
		});
	}
}

// 切换机构查询用户
function queryGrantedList(action) {
	$("#first").val("false");
	$("#grantedForm").ajaxSubmit({
		type : "post",
		url : ctxpath + action,
		success : function(msg) {
			$("#resourceList").html(msg);
		}
	});
}

function queryGranted() {
	$("#grantedForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/granted/queryGranted",
		success : function(msg) {
			$("#resourceList").html(msg);
		}
	});
}

function queryResource(flag) {
	queryDevices(flag);
}

function querySelectedResourceByChangeOrgan() {
	if ($("#resourceType").val() == "ORGAN") {
		$("#theirOrgan").hide();
		querySelectedOrgans();
	} else {
		$("#theirOrgan").show();
		AT2QuerySelectedResource('resourceList', 'resourceType', 'true');
	}
}

function showSelectedResource() {
	process("t", 2);
	$("#grantedForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/device/querySelectedResource",
		success : function(msg) {
			$("#grantedArea").html(msg);
		}
	});
}

function queryOrgans() {
	$("#grantedForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/organ/queryOrgans",
		success : function(msg) {
			$("#resourceList").html(msg);
		}
	});
}

function getContent(logonName, organName, name, sex) {
	if (sex == "man") {
		sex = "男";
	} else {
		sex = "女";
	}
	return "<div>登录名:" + logonName + "</div><div>真实姓名:" + name
			+ "</div><div>性别:" + sex + "</div><div>所属机构:" + organName
			+ "</div>";
}

// 选中用户,设置用户id,name
function selectedUser(id, logonName) {
	$("#logonName").val(logonName);
	$("#userId").val(id);
	$("#options").hide();

}

function showMsg(msg, success, error, parentClass) {
	var combineClass = ".ui-widget";
	if (parentClass != null) {
		combineClass = "." + parentClass + " " + ".ui-widget";

	}
	if (msg == "success") {
		$(combineClass).css("visibility", "visible");
		$(combineClass + " .ui-corner-all").removeClass("ui-state-error")
				.addClass("ui-state-highlight").css("visibility", "visible");
		$(combineClass + " .ui-corner-all .ui-icon").removeClass(
				"ui-icon-alert").addClass("ui-icon-info");
		$(combineClass + " .ui-corner-all .message").html(success);
	} else {
		$(combineClass).css("visibility", "visible");
		$(combineClass + " .ui-corner-all").removeClass("ui-state-highlight")
				.addClass("ui-state-error").css("visibility", "visible");
		$(combineClass + " .ui-corner-all .ui-icon")
				.removeClass("ui-icon-info").addClass("ui-icon-alert");
		$(combineClass + " .ui-corner-all .message").html(error);
	}

}

function alertWrang(id, msgs, left, width) {
	$("#" + id).html(getAlert(left, width));
	showMsg("wrong", msgs, msgs, id);
	setTimeout(function() {
		$("#" + id).html("");
	}, 1500);
}

function alertSuccess(id, msgs, left, width) {
	$("#" + id).html(getAlert(left, width));
	showMsg("success", msgs, msgs, id);
	setTimeout(function() {
		$("#" + id).html("");
	}, 1500);
}

function getAlert(marginLeft, width) {
	if (marginLeft == null) {
		marginLeft = 20;
	}
	if (width == null) {
		width = 200;
	}
	var msg = "<div class='ui-widget' style='border:0px red solid;text-align:left;'>";
	msg = msg
			+ "<div class='ui-corner-all' style='margin-top: 5px;margin-left:"
			+ marginLeft + "px;padding:10px 13px;width:" + width + "px'>";
	msg = msg
			+ "<span class='ui-icon' style='float:left; margin-right: .3em;'></span>";
	msg = msg + "<span class='message'></span></div>";
	return msg;
}

function showOptions(name, f, form) {
	if (f == "organOptions") {
		if ($("#cmsId").val() == "") {
			alertWrang("alertMsg", "请选择所在平台", 10, 150);
			return false;
		}
	}
	$("#options").show();
	var divPos = document.getElementById(name);
	var position = findPos(name);
	var x = position.x;
	var y = position.y;
	$("#options").css("left", x + "px");
	$("#options").css("top", (y + 20) + "px");
	if ($("#" + name).val() == "所有机构") {
		setOptions(f, "", form);
	} else {
		setOptions(f, $("#" + name).val(), form);
	}
}

function setOptions(f, name, form) {
	if (f == "userOptions") {
		$.ajax({
			type : "get",
			url : ctxpath + "/user/getUserByName?name=" + name,
			success : function(msg) {
				$("#options").html(msg);
				setTimeout(autoHideOption, 100000);
			}
		});
	} else if (f == "ownerOrganOptions") {
		owner_organName_input_id = "ownerOrganName";
		owner_organId_input_id = "ownerOrganId";
		$("#ownerOrganName").val(name);
		$("#" + form).ajaxSubmit({
			type : "post",
			url : ctxpath + "/organ/getOwnerOrganByName",
			success : function(msg) {
				$("#options").html(msg);
			}
		});
	} else if (f == "organOptions") {
		organName_input_id = "organName";
		organId_input_id = "organId";
		$("#organName").val(name);
		$("#" + form).ajaxSubmit({
			type : "post",
			url : ctxpath + "/organ/getOrganByName",
			success : function(msg) {
				$("#options").html(msg);
			}
		});
	}
}

function selectOrgan(id, name, isOwner) {
	if (isOwner) {
		$("#" + owner_organName_input_id).val(name);
		$("#" + owner_organId_input_id).val(id);
	} else {
		$("#" + organName_input_id).val(name);
		$("#" + organId_input_id).val(id);
	}

	$("#options").hide();
	if ($("#selectOrganFuncton").val() == "queryResource") {
		selected('resourceList', 'currentResourceType');
		queryResource(false);
	} else if ($("#selectOrganFuncton").val() == "querySelectedResource") {
		querySelectedResourceByChangeOrgan();
	} else if ($("#selectOrganFuncton").val() == "queryGranted") {
		selected("resourceList", "grantedType");
		queryGrantedList("/granted/getGrantedList");
	} else if ($("#selectOrganFuncton").val() == "querySelectedGranted") {
		selected("resourceList", "grantedType");
		AT1QuerySelectedGranteds('false');
	} else if ($("#selectOrganFuncton").val() == "queryUsersByOrgan") {
		queryUser();
	} else if ($("#selectOrganFuncton").val() == "getResourcesForShare") {
		selected('resourceList', 'currentResourceType');
		getResourcesForShare('false');
	} else if ($("#selectOrganFuncton").val() == "QuerySelectedResourceForShare") {
		selected('resourceList', 'currentResourceType');
		QuerySelectedResourceForShare('resourceList', 'currentResourceType',
				'false');
	} else if ($("#selectOrganFuncton").val() == "getResourceByOuterPlatformViaPost") {
		getResourceByOuterPlatformViaPost();
	} else if ($("#selectOrganFuncton").val() == "obtainQueryGranted") {
		selected('resourceList', 'grantedType');
		obtainQueryGrantedList();
	} else if ($("#selectOrganFuncton").val() == "queryResourceForDistribution") {
		selected('resourceList', 'currentResourceType');
		redistributionQueryResource();
	}
}

function hideOptions(event) {
	if (!isInVideoTree(event, "options")) {
		$("#options").hide();
	}
}

function autoHideOption() {
	currentUserOption = 0;
	$("#options").hide();
}

var currentUserOption = 0;
var lastLogonName = "";
function keyDownEvent(e, form) {
	if ($("#logonName").val() != lastLogonName) {
		lastLogonName = $("#logonName").val();
		showOptions('logonName', 'userOptions', form);
	}
	var e = window.event || e;
	if (e.keyCode == 40) {
		$("#user" + currentUserOption).css("background-color", "#ffffff");
		currentUserOption++;
		$("#user" + currentUserOption).css("background-color", "DBF3FF");
	}
	if (e.keyCode == 38 && currentUserOption > -1) {
		if (currentUserOption == 0) {
			$("#user" + currentUserOption).css("background-color", "DBF3FF");
		} else {
			$("#user" + currentUserOption).css("background-color", "#ffffff");
			currentUserOption--;
			$("#user" + currentUserOption).css("background-color", "DBF3FF");
		}

	}
	if (e.keyCode == 13 && currentUserOption > -1) {
		$("#user" + currentUserOption).click();
		currentUserOption = 0;
	}
}

var currentOrganOption = 0;
function organKeyDownEvent(e, nameId, f, form) {
	if ($("#" + nameId).val() != $("#lastOrganName").val()) {
		$("#lastOrganName").val($("#" + nameId).val());
		currentOrganOption = 0;
		showOptions(nameId, f, form);
	}
	var e = window.event || e;
	if (e.keyCode == 40) {
		$("#organ" + currentOrganOption).css("background-color", "#ffffff");
		currentOrganOption++;
		$("#organ" + currentOrganOption).css("background-color", "DBF3FF");
	}
	if (e.keyCode == 38 && currentOrganOption > -1) {
		if (currentOrganOption == 0) {
			$("#organ" + currentOrganOption).css("background-color", "DBF3FF");
		} else {
			$("#organ" + currentOrganOption).css("background-color", "#ffffff");
			currentOrganOption--;
			$("#organ" + currentOrganOption).css("background-color", "DBF3FF");
		}

	}
	if (e.keyCode == 13 && currentOrganOption > -1) {
		$("#organ" + currentOrganOption).click();
		currentOrganOption = 0;
	}
}

var my_cmsId = "";
var my_cmsName = "";
// 下级平台树
function showPlatforms(cmsId, cmsName, platformTreeArea) {
	my_cmsId = cmsId;
	my_cmsName = cmsName;
	$("#" + platformTreeArea).show();
	var divPos = document.getElementById(cmsName);
	var position = findPos(cmsName);
	var x = position.x;
	var y = position.y;
	$("#" + platformTreeArea).css("left", x + "px");
	$("#" + platformTreeArea).css("top", (y + 20) + "px");
}

// get方式获取已经分配给外平台的资源
function getResourceByOuterPlatform(platformCmsId, resourceCmsId, platformName) {
	$
			.ajax({
				type : "get",
				url : ctxpath
						+ "/share/getResourceByOuterPlatform?r.resourceType=VIC&r.first=true&r.organId=all&r.original=true&r.platformCmsId="
						+ platformCmsId + "&r.cmsId=" + resourceCmsId,
				success : function(msg) {
					$("#grantedRight").html(msg);
					$("#platformName").html("平台: " + platformName + " 资源列表");
				}
			});
}
// post方式获取已经分配给外平台的资源
function getResourceByOuterPlatformViaPost(position, type) {
	permissionSelected(position, type);
	$("#userPermissionForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/share/getResourceByOuterPlatform",
		success : function(msg) {
			$("#resourceList").html(msg);
		}
	});
}

// 获取外平台进行资源共享
function getPlatforms() {
	shareProcess(1);
	$("#first").val("true");
	$("#shareForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/share/getPlatforms",
		success : function(msg) {
			$("#shareArea").html(msg);
		}
	});
}

// 获取本平台设备用于共享
function getResourcesForShare(flag) {
	if ($("#platformIds").val() == "") {
		alertWrang("alertMsg", "至少选择一个平台", 10, 150);
		return false;
	}
	shareProcess(2);
	$("#first").val(flag);
	$("#shareForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/share/getResourcesForShare",
		success : function(msg) {
			if (flag == "true") {
				$("#shareArea").html(msg);
			} else {
				$("#resourceList").html(msg);
			}
		}
	});
}

// 确认共享
function confirmShare() {
	if ($("#vicIds").val() == "" && $("#ipvicIds").val() == ""
			&& $("#aicIds").val() == "" && $("#organIds").val() == "") {
		alertWrang("alertMsg", "至少选择一个资源", 10, 150);
		return false;
	}
	if ($("#platformIds").val() == "") {
		alertWrang("alertMsg", "至少选择一个平台", 10, 150);
		return false;
	}
	shareProcess(4);
	$("#shareForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/share/shareResource",
		success : function(msg) {
			alertSuccess("alertMsg", "操作成功", 10, 150);
			clearIds();
			clearChecked("resourceList");
		}
	});

}

// 共享,查看已选中的资源
function QuerySelectedResourceForShare(flag) {
	if ($("#vicIds").val() == "" && $("#ipvicIds").val() == ""
			&& $("#aicIds").val() == "" && $("#organIds").val() == "") {
		alertWrang("alertMsg", "至少选择一个资源", 10, 150);
		return false;
	}
	shareProcess(3);
	$("#first").val(flag);
	$("#shareForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/share/querySelectedResource",
		success : function(msg) {
			if (flag == "true") {
				$("#shareArea").html(msg);
			} else {
				$("#resourceList").html(msg);
			}
		}
	});
}

// 共享平台主页
function shareHome() {
	$.ajax({
		type : "get",
		url : ctxpath + "/platform/shareDeviceToPlatforms",
		success : function(msg) {
			$("#center").html(msg);
		}
	});
}
// 下级平台主页
function childrenPlatformsHome() {
	$.ajax({
		type : "get",
		url : ctxpath + "/platform/childrenPlatformsHome",
		success : function(msg) {
			$("#center").html(msg);
		}
	});
}
// 互联平台主页
function obtainHome() {
	$.ajax({
		type : "get",
		url : ctxpath + "/platform/getDeviceFromPlatforms",
		success : function(msg) {
			$("#center").html(msg);
			$("#tab1").click();
		}
	});
}


function getPlatformsByDataSource(grantedId, grantedType) {
	$.ajax({
		type : "get",
		url : ctxpath + "/authorization/getPlatformsByDataSource?grantedId="
				+ grantedId + "&grantedType=" + grantedType + "&dataSource="
				+ $("#dataSource").val(),
		success : function(msg) {
			$("#platform").html(msg);
			$("#resourceList").html("");
		}
	});
}

// 本地搜索平台
function queryPlatformLocal() {
	var name = $("#cmsName").val();
	var spans = document.getElementById("userList")
			.getElementsByTagName("span");
	for ( var i = 0; i < spans.length; i++) {
		if ($("#" + spans[i].id).html().indexOf(name) != -1) {
			$("#" + spans[i].id).css("color", "red");
		} else {
			$("#" + spans[i].id).css("color", "black");
		}
	}
}

function queryOuterPlatforms() {
	$("#grantedForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/share/getOuterPlatforms",
		success : function(msg) {
			$("#userList").html(msg);
		}
	});
}

function getContentForShowTips(content, organId, platformId, platformName) {
	var organs = content.split("__", 10);
	var path = "";
	for ( var i = 0; organs[i] && organs[i] != ""; i++) {
		var idName = organs[i].split("||", 10);
		path = path
				+ "<span style='color:blue;cursor:pointer;' onclick='setOrganIdForQuery(\""
				+ platformId + "\",\"" + platformName + "\",\"" + idName[1]
				+ "\",\"" + idName[0] + "\")'>" + idName[0] + "</span>/";
	}
	return "/" + path;
}

function setOrganIdForQuery(platformId, platformName, organId, organName) {
	if ($("#cmsId").val() == "") {
		$("#cmsId").val(platformId);
		$("#cmsName").val(platformName);
	}
	$("#organId").val(organId);
	$("#organName").val(organName);
	$("#query").click();
}

/**
 * 创建上级国标平台
 * 
 * @param cmsId
 */
function forCreateParentPlatform() {
	$.ajax({
		type : "GET",
		url : ctxpath + "/gbOuterDevice/forCreateParentPlatform",
		success : function(msg) {
			$("#platformTitle").html('<i class="fa fa-list"></i> 创建上级国标平台');
			$("#platformDetail").html(msg);
		}
	});
}

/**
 * 创建下级国标平台
 * 
 * @param cmsId
 */
function forCreateChildPlatform() {
	$.ajax({
		type : "GET",
		url : ctxpath + "/gbOuterDevice/forCreateChildPlatform",
		success : function(msg) {
			$("#platformTitle").html('<i class="fa fa-list"></i> 创建下级国标平台');
			$("#platformDetail").html(msg);
		}
	});
}

function validPlatformForm(form, callback) {
	var form1 = $("#" + form);
	var error1 = $('.alert-danger', form1);
	var success1 = $('.alert-success', form1);
	form1.validate({
		errorElement : 'span', // default input error message container
		errorClass : 'help-block', // default input error message class
		focusInvalid : false, // do not focus the last invalid input
		ignore : "",
		rules : {
			"platform.cmsId": {
				minlength : 20,
				maxlength :20,
				required : true,
				remote : {
					url : ctxpath+"/gbOuterDevice/validGbPlatformCmsId",
					type : "post",
					data :{
						"cmsId" : function(){
							return $("[name='platform.cmsId']",form1).val();
							}
						}
					}
			},
			"platform.name" : {
				minlength : 2,
				required : true,
			},
			"platform.password" : {
				required : true
			},
			"platformId":{
				required:true,
			},
			"platform.childCmsId":{
				required:true,
				remote : {
					url : ctxpath+"/gbOuterDevice/validChildCmsId",
					type : "post",
					data :{
						"childCmsId" :function(){
							return $("[name='platform.childCmsId']",form1).val();
						},
						"platformId" :function(){
							return $("[name=platformId]",form1).val();
						}
					}
				}
			}
		},
		messages : {
			"platform.cmsId" : {
				required : "上级平台编号不能为空！",
				minlength: "上级国标平台编号必需是20位",
				maxlength: "上级国标平台编号必需是20位",
				remote:"该上级国标平台编号已经存在！"
			},
			"platform.name":{
				required:"上级平台名称不能为空"
			},
			"platform.password":{
				required:"密码不能为空"
			},
			"platform.childCmsId":{
				required:"平台国标编号不能为空",
				remote:"该编号已经被占用",
			},
			"platformId":{
				required:"平台编号不能为空",
			}
			
		},
		
// invalidHandler : function(event, validator) { // display error alert
//			
// success1.hide();
// error1.show();
// App.scrollTo(error1, -200);
// },
//		
		highlight : function(element) { // hightlight error inputs
			$(element).closest('.form-group').addClass('has-error'); // set
		},
		
		unhighlight : function(element) { // revert the change done by
			// hightlight
			$(element).closest('.form-group').removeClass('has-error'); // set
		},
		success : function(label) {
			label.closest('.form-group').removeClass('has-error'); // set
		},
		submitHandler : function(form) {
			callback();
		}
	});
}
function validCreateChildPlatformForm(form, callback) {
	var form1 = $("#" + form);
	var error1 = $('.alert-danger', form1);
	var success1 = $('.alert-success', form1);
	form1.validate({
		errorElement : 'span', // default input error message container
		errorClass : 'help-block', // default input error message class
		focusInvalid : false, // do not focus the last invalid input
		ignore : "",
		rules : {
			"platform.cmsId": {
				minlength : 20,
				maxlength :20,
				required : true,
				remote : {
					url : ctxpath+"/gbOuterDevice/validGbPlatformCmsId",
					type : "post",
					data :{
						"cmsId" : function(){
							return $("[name='platform.cmsId']",form1).val();
						}
					}
				}
			},
			"platform.name" : {
				minlength : 2,
				required : true,
			},
			"platform.password" : {
				required : true
			},
			"platformId":{
				required:true,
			},
			"platform.childCmsId":{
				required:true,
				remote : {
					url : ctxpath+"/gbOuterDevice/validChildCmsId",
					type : "post",
					data :{
						"childCmsId" :function(){
							return $("[name='platform.childCmsId']",form1).val();
						},
						"platformId" :function(){
							return $("[name=platformId]",form1).val();
						}
					}
				}
			}
		},
		messages : {
			"platform.cmsId" : {
				required : "下级平台编号不能为空！",
				minlength: "下级国标平台编号必需是20位",
				maxlength: "下级国标平台编号必需是20位",
				remote:"该下级国标平台编号已经存在！"
			},
			"platform.name":{
				required:"下级平台名称不能为空"
			},
			"platform.password":{
				required:"密码不能为空"
			},
			"gbPlatformCmsId":{
				required:"平台国标编号不能为空",
				remote:"该编号已经被占用",
			},
			"platformId":{
				required:"平台编号不能为空",
			}
			
		},
		
// invalidHandler : function(event, validator) { // display error alert
//			
// success1.hide();
// error1.show();
// App.scrollTo(error1, -200);
// },
//		
		highlight : function(element) { // hightlight error inputs
			$(element).closest('.form-group').addClass('has-error'); // set
		},
		
		unhighlight : function(element) { // revert the change done by
			// hightlight
			$(element).closest('.form-group').removeClass('has-error'); // set
		},
		success : function(label) {
			label.closest('.form-group').removeClass('has-error'); // set
		},
		submitHandler : function(form) {
			callback();
		}
	});
}



function setCmsIdByName() {
	$("#cmsId").val($("#cmsId").val());
}

function createParentPlatform() {
	$("#createParentPlatformForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/gbOuterDevice/createParentPlatform",
		success : function(msg) {
			if (msg == "success") {
				jSuccess("操作成功！");
				platformManage();
			}else{
				jError("操作失败！");
			}
		}
	});
}
function createChildPlatform() {
	$("#createChildPlatformForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/gbOuterDevice/createChildPlatform",
		success : function(msg) {
			if (msg == "success") {
				jSuccess("操作成功！");
				platformManage();
			}else{
				jError("操作失败！");
			}
		}
	});
}

function deleteParentPlatform(id){
	if(confirm("确定要删除吗")){
		$.ajax({
			type:"get",
			url:ctxpath+"/gbOuterDevice/deleteParentPlatform?id="+id,
			success:function(msg){
				if(msg == "success"){
					jSuccess("操作成功！");
					platformManage();
				}else{
					jError("操作失败");
				}
			}
		});
	}
}

var interval;
function getGBOuterDeviceBycmsId(cmsId, deviceId) {
	if (confirm("请勿多次点击,以免影响系统性能！需要查看设备接收情况，请点击刷新按钮")) {
		$("#getGBDevices_" + cmsId).attr("disabled", true);
		$.ajax({
				type : "post",
				url : ctxpath
						+ "/gbOuterDevice/queryOuterDevices?cmdType=Catalog&cmsId="
						+ cmsId + "&deviceId=" + deviceId,
				success : function(msg) {
					if (msg == "ERROR") {
						jError("检索失败，请检查sip服务器地址是否配置正确！");
					}else{
						 showReceiveDeviceDetail(cmsId);
						 flushDeviceList(msg,cmsId);
					}
				}
			});
	}
}

function showReceiveDeviceDetail(cmsId){
	$.ajax({
		type:'get',
		url:ctxpath+"/gbOuterDevice/receiveDeviceDetail?cmsId="+cmsId,
		success:function(detail){
			$("#platformTitle").html('<i class="fa fa-list"></i> 接收设备情况');
			$("#platformDetail").html(detail);
		}
	});
}

var receiveStatus;

function flushDeviceList(sn,cmsId){
	receiveStatus = cmsId;
	$("#queryGBDevice").show();
	$.ajax({
		type:'get',
		url:ctxpath+"/gbOuterDevice/waitFinish?sn="+sn,
		success:function(msg){
			var result = JSON.parse(msg);
			if(result[0]["result"]=="finish"){
				$("#queryGBDevice").hide();
				$("#queryGBDevice").html('<i class="fa fa-spinner fa-spin"></i>设备接收中...');
				receiveStatus = null;
				jSuccess("接收完成");
			}else{
				$("[name='organ']","#receiveDetailForm").val(result[0]['organCount']);
				$("[name='server']","#receiveDetailForm").val(result[0]['serverCount']);
				$("[name='vic']","#receiveDetailForm").val(result[0]['VIC']);
				$("[name='ipvic']","#receiveDetailForm").val(result[0]['IPVIC']);
				$("[name='aic']","#receiveDetailForm").val(result[0]['AIC']);
				$("[name='sumNum']","#receiveDetailForm").val(result[0]['sumNum']);
				$("[name='currentSumNum']","#receiveDetailForm").val(result[0]['currentSumNum']);
				$("[name='total']","#receiveDetailForm").val(result[0]['total']);
				if(result[0]['sumNum']!="0" && result[0]['sumNum']==result[0]['currentSumNum']){
					$("#queryGBDevice").html('<i class="fa fa-spinner fa-spin"></i>设备接收完毕，入库中...');
				}
				setTimeout(function(){flushDeviceList(sn,cmsId);},2000);
			}
		},
		error:function(flag){
			jError("接收异常结束！");
			$("#queryGBDevice").hide();
		}
	});
}

function getGBOuterDeviceByOrgan(cmsId, deviceId) {
	$.ajax({
			type : "post",
			url : ctxpath
					+ "/gbOuterDevice/queryOuterDevices?cmdType=Catalog&cmsId="
					+ cmsId + "&deviceId=" + deviceId,
			success : function(msg) {
				if (msg == "OK") {
					alertSuccess("alertMsg", "成功发送获取设备请求命令，正等待设备推送！",
							100, 150);
				}
			}
		});
}

function deleteGBOuterDeviceByOrgan(cmsId,organId){
	if (confirm("此操作将删除该机构下的所有子孙机构、设备、服务器以及相关的授权、共享数据，确定要删除吗？")) {
		$.ajax({
			type : "post",
			url : ctxpath + "/gbOuterDevice/deleteOrganResource?organId="
					+ organId+"&cmsId="+cmsId,
			success : function(msg) {
				if (msg == "success") {
					alertSuccess("alertMsg", "操作成功！", 100, 150);
					getResourcesByForm();
				} else {
					alertWrang("alertMsg", "操作失败！", 100, 150);
				}
			}
		});
	}
}

var recordPreTimes = "";
function stopInpect(count) {
	if (recordPreTimes.split("_").length > 1) {
		if (recordPreTimes.split("_")[0] == count) {
			if (parseInt(recordPreTimes.split("_")[1]) == 10) {
				// alert(count+"," +recordPreTimes);
				// alert(hasPreform+","+serversPreTime);
				recordPreTimes = "finish";
				window.clearInterval(interval);
			} else {
				recordPreTimes = count + "_"
						+ (parseInt(recordPreTimes.split("_")[1]) + 1);
			}
		} else {
			recordPreTimes = count + "_" + 0;
		}
	} else {
		recordPreTimes = count + "_" + 0;
	}
}

/**
 * 查看是否已经同步完了server,如果 已经同步完成,则开始查询server下的 摄像头
 */
var serversPreTime = "";
var hasPreform = false;
function getDeviceByServer(count, cmsId) {
	if (!hasPreform) {
		if (serversPreTime.split("_").length > 1) {
			if (serversPreTime.split("_")[0] == count) {
				if (parseInt(serversPreTime.split("_")[1]) == 5) {
					// 开始查询server下的摄像头
					hasPreform = true;
					serversPreTime = "";
					$
							.ajax({
								type : "post",
								url : ctxpath
										+ "/gbOuterDevice/queryOuterDevicesByServer?cmdType=Catalog&cmsId="
										+ cmsId,
								success : function(msg) {
								}
							});
				} else {
					serversPreTime = count + "_"
							+ (parseInt(serversPreTime.split("_")[1]) + 1);
				}
			} else {
				serversPreTime = count + "_" + 0;
			}
		} else {
			serversPreTime = count + "_" + 0;
		}
	}
}
var hasStartFlush = false;
function flushStatisticResourceByCmsId(cmsId) {
	$.ajax({
		type : "post",
		url : ctxpath + "/statistic/statisticResourceByCmsId?cmsId=" + cmsId
				+ "&type=remote",
		success : function(msg) {
			$("#flushResource_" + cmsId).html(
					'<input type="button"  onclick="stopFlushStatisticResource(\''
							+ cmsId + '\')" value="停止刷新"/>');
			$("#platformRight").html(msg);
			$("#alertMsg").html(getLoading("设备刷新中,请等待"));
			if (!hasStartFlush) {
				hasStartFlush = true;
				interval = window.setInterval("flushStatisticResourceByCmsId('"
						+ cmsId + "');", 5000);
			}

		}
	});
}

function stopFlushStatisticResource(cmsId, type) {
	if (interval) {
		window.clearInterval(interval);
		hasStartFlush = false;
		$("#flushResource_" + cmsId).html(
				'<input type="button"  onclick="flushStatisticResourceByCmsId(\''
						+ cmsId + '\')" value="刷新"/>');
		$("#alertMsg").html("");

	}
}

function statisticResourceByCmsId(cmsId) {
	$("#alertMsg").html(getLoading("设备刷新中,请等待"));
	window.clearInterval(interval);
	$.ajax({
		type : "post",
		url : ctxpath + "/statistic/statisticResourceByCmsId?type=local&cmsId="
				+ cmsId,
		success : function(msg) {
			$("#platformRight").html(msg);
			$("#alertMsg").html("");
		}
	});
}

function queryGBOuterDeviceBycmsId(cmsId) {
	$("#alertMsg").html(getLoading("查询中,请等待"));
	$.ajax({
		type : "post",
		url : ctxpath + "/gbOuterDevice/getResourceList?r.cmsId=" + cmsId
				+ "&r.organId=all&r.resourceType=VIC",
		success : function(msg) {
			$("#platformRight").html(msg);
		}
	});
}

function getGBOuterDevice() {
	$("#alertMsg").html(getLoading("查询中,请等待"));

	$("#manageDeivesForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/gbOuterDevice/getResourceList",
		success : function(msg) {
			$("#platformRight").html(msg);
		}
	});
}

function rebootVIC(naming) {
	$("#query_result").html("");
	if (confirm("确认要重启该设备吗？")) {
		alertWrang("alertMsg", getLoading("请求中,请等待"), 400, 400);
		$.ajax({
			type : "post",
			url : ctxpath + "/gbOuterDevice/rebootVIC?naming=" + naming,
			success : function(msg) {
				var retMessage = "";
				if (msg == "OK") {
					retMessage = "设备重启成功！";
				} else {
					retMessage = "设备重启失败！";
				}

				alertWrang("alertMsg", retMessage, 400, 400);
			}
		});
	}
}

var queryVicInfoInterval;
var reqTime = 0;

function queryStatus(naming) {
	reqTime = 0;
	window.clearInterval(queryVicInfoInterval);
	$("#alertMsg").html(getLoading("请求中,请等待"));
	$.ajax({
		type : "get",
		url : ctxpath + "/gbOuterDevice/queryVICStatus?naming=" + naming,
		success : function(msg) {
			showVicInfo(msg);
			queryVicInfoInterval = window.setInterval("showVicInfo('" + msg
					+ "');", 1000);
		}
	});
}

function queryVicInfo(naming) {
	reqTime = 0;
	window.clearInterval(queryVicInfoInterval);
	$("#alertMsg").html(getLoading("请求中,请等待"));
	$.ajax({
		type : "get",
		url : ctxpath + "/gbOuterDevice/queryVicDetail?naming=" + naming,
		success : function(msg) {
			showVicInfo(msg);
			queryVicInfoInterval = window.setInterval("showVicInfo('" + msg
					+ "');", 2000);
		}
	});
}

function showVicInfo(sn) {
	reqTime++;
	$.ajax({
		type : "get",
		url : ctxpath + "/gbOuterDevice/showResponseContent?sn=" + sn,
		success : function(msg) {
			if (msg != "") {
				reqTime = 0;
				window.clearInterval(queryVicInfoInterval);
				$("#alertMsg").html("");
				alert(msg);
			} else {
				if (reqTime > 5) {
					reqTime = 0;
					window.clearInterval(queryVicInfoInterval);
					$("#alertMsg").html("");
					alert("查询失败!");
				}
			}
		}
	});
}

// 报警布防和撤防相关指令
function setGuard(naming) {
	$("#alertMsg").html(getLoading("请求中,请等待"));
	$.ajax({
		type : "post",
		url : ctxpath + "/gbOuterDevice/setGuard?naming=" + naming,
		success : function(msg) {
			if (msg == "OK") {
				alert("布防成功！");
			} else {
				alert("布防不成功！");
			}
			;
		}
	});
}

function resetGuard(naming) {
	$("#alertMsg").html(getLoading("请求中,请等待"));
	$.ajax({
		type : "post",
		url : ctxpath + "/gbOuterDevice/resetGuard?naming=" + naming,
		success : function(msg) {
			if (msg == "OK") {
				alert("撤防成功！");
			} else {
				alert("撤防不成功！");
			}
			;
		}
	});
}

function resetAlarm(naming) {
	$("#alertMsg").html(getLoading("请求中,请等待"));
	$.ajax({
		type : "post",
		url : ctxpath + "/gbOuterDevice/resetAlarm?naming=" + naming,
		success : function(msg) {
			if (msg == "OK") {
				alert("报警复位成功！");
			} else {
				alert("报警复位不成功！");
			}
			;
		}
	});
}

function alarmByNaming(naming) {
	$("#alertMsg").html(getLoading("请求中,请等待"));
	$.ajax({
		type : "post",
		url : ctxpath + "/gbShareDevice/alarmByNaming?naming=" + naming,
		success : function(msg) {
			if (msg == "OK") {
				alert("报警发送成功！");
			} else {
				alert("报警发送不成功！");
			}
			;
		}
	});
}

function checkPlatformStatus() {
	// 更新平台列表
	$
			.ajax({
				type : "GET",
				url : ctxpath + "/gbOuterDevice/getPlatformStatus",
				success : function(msg) {
					var platformStatus = msg.split(",");
					for ( var i = 0; i < platformStatus.length; i++) {
						var temp = platformStatus[i].split("_");
						if (temp[1] == "false") {
							$("#" + temp[0]).html(
									"<span style='color:black'>离线</span>");
						} else {
							$("#" + temp[0]).html(
									"<span style='color:blue'>在线</span>");
						}

					}
				}
			});
}

function testDivRefresh(naming) {
	$("#alertMsg").html(getLoading("请求中,请等待"));
	$.ajax({
		type : "post",
		url : ctxpath + "/gbOuterDevice/testDivRefresh?naming=" + naming,
		success : function(msg) {
			$("#query_result").html(msg);
		}
	});
}

function testTalent() {
	alert("hi");
	$.ajax({
		type : "get",
		url : ctxpath + "/home/testMe",
		success : function(msg) {
			alert(msg);
		}
	});
}

function virtrueAlarm(cmsId) {
	setTabsCss("tab8");
	$.ajax({
		type : "GET",
		url : ctxpath + "/gbShareDevice/showDevices?first=true&cmsId=" + cmsId,
		success : function(msg) {
			$("#content").html(msg);
			$("#tab8").html(getTabHtml("模拟报警"));
			setCurrentTabCss("tab8");
		}
	});
}
function queryVirtrueAlarm() {
	$("#alarm-device-form").ajaxSubmit({
		type : "post",
		url : ctxpath + "/gbShareDevice/showDevices?first=false",
		success : function(msg) {
			$("#deviceList").html(msg);
		}
	});
}

function getPlatformsByType() {
	shareProcess(1);
	$("#first").val("true");
	$("#shareForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/share/getPlatformsByType",
		success : function(msg) {
			$("#resourceList").html(msg);
		}
	});
}

function queryOrgansForManage() {
	$("#organManageForm").ajaxSubmit({
		type : "post",
		url : ctxpath + "/organ/getOrganByCmsId",
		success : function(msg) {
			$("#organList").html(msg);
		}
	});
}

function subscribeGBOuterDeviceBycmsId(deviceId, cmsId, expirePeriod,
		ownerCmsId) {
	if (expirePeriod == "0") {
		$("#alertMsg").html(getLoading("取消中,请等待"));
	} else if(expirePeriod !="-1") {
		$("#alertMsg").html(getLoading("订阅中,请等待"));
	}
	if(expirePeriod == "-1"){
		if(confirm("确定要删除该订阅吗？")){
			$.ajax({
				type : "post",
				url : ctxpath + "/gbOuterDevice/sendSubscribe?deviceId=" + deviceId
						+ "&toPlatformId=" + cmsId + "&expirePeriod=" + expirePeriod,
				success : function(msg) {
					if (msg == "success") {
						alertSuccess("alertMsg", "操作成功！", 100, 150);
						showSubscribeList();
					} else {
						alertWrang("alertMsg", "操作失败！", 100, 150);
					}
				}
			});
		}
	}else{
		$.ajax({
			type : "post",
			url : ctxpath + "/gbOuterDevice/sendSubscribe?deviceId=" + deviceId
					+ "&toPlatformId=" + cmsId + "&expirePeriod=" + expirePeriod,
			success : function(msg) {
				if (msg == "success") {
					alertSuccess("alertMsg", "操作成功！", 100, 150);
					showSubscribeList();
				} else {
					alertWrang("alertMsg", "操作失败！", 100, 150);
				}
			}
		});
	}
	
}

// 删除国标平台的机构，设备，服务器数据
function deletePlatformResource(cmsId) {
	if (confirm("此操作将删除该平台下的所有机构、设备、服务器以及相关的授权、共享数据，确定要删除吗？")) {
		$.ajax({
			type : "post",
			url : ctxpath + "/gbOuterDevice/deletePlatformResource?cmsId="
					+ cmsId,
			success : function(msg) {
				if (msg == "success") {
					jSuccess("操作成功！");
				} else {
					jError("操作失败！");
				}
			}
		});
	}
}

function showSubscribeList() {
	$.ajax({
		type : "get",
		url : ctxpath + "/gbOuterDevice/getSubscribeList",
		success : function(msg) {
			$("#platformTitle").html('<i class="fa fa-list"></i> 订阅管理');
			$("#platformDetail").html(msg);
		}
	});
}

function forAddSubscribe() {
	$("#subscribeForm").css("visibility", "visible");
	$("#addSubscibeBtn").css("visibility", "hidden");
}

function addSubscribe(cmsId,period) {
	$.ajax({
		type : "post",
		url : ctxpath + "/gbOuterDevice/sendSubscribe",
		data:{
			"toPlatformId":cmsId,
			"period":period==null||period==undefined?null:period
		},
		success : function(msg) {
			if (msg == "success") {
				jSuccess("操作成功！");
				$.ajax({
					type : "GET",
					url : ctxpath + "/gbOuterDevice/showPlatformList",
					success : function(msg) {
						$("#content").html(msg);
					}
				});
			} else {
				jError("操作失败！");
			}
		}
	});
}

function getResourcesByCmsId(cmsId) {
	$("#tr_"+cmsId).css({"backgroundColor":"#C6EBFA"});
	$.ajax({
		type : "get",
		url : ctxpath
				+ "/gbPlatform/getResourcesByCmsId?cmsId="
				+ cmsId,
		success : function(msg) {
			$("#platformRight").html(msg);
		}
	});
}

function getResourcesByForm() {
	$("#queryResourceForm").ajaxSubmit({
		type : "post",
		url : ctxpath
				+ "/gbPlatform/getResourcesByCmsId",
		success : function(msg) {
			$("#platformRight").html(msg);
		}
	});
}

function showRootOrganForCheck(){
	if($("#resourceType").val() =='ORGAN'){
		$("#rootOrganTD").css("visibility","visible");
	}else{
		$("#rootOrganTD").css("visibility","hidden");
	}
}

function selectAll(id) {
	$.each($("[type='checkbox']", '#' + id), function(idx, data) {
		$(data).attr('checked', true);
	});
}

function unselect(id) {
	$.each($("[type='checkbox']", '#' + id), function(idx, data) {
		if ($(data).attr('checked')) {
			$(data).attr('checked', false);
		} else {
			$(data).attr('checked', true);
		}
	});
}

function setChecked(id) {
	$.each($("[type='checkbox']", '#' + id), function(idx, data) {
		if ($(event.target).attr('checked')) {
			$(data).attr('checked', true);
		} else {
			$(data).attr('checked', false);
		}
	});
}

function forwardOldRegist(tabId, cmsId, sessionId) {
	window.location.href = ctxpath + '/platform/getChildrenPlatforms?cmsId='
			+ cmsId + '&tabId=' + tabId + '&sessionId=' + sessionId;
}

var deleteNodes = [];
function setGbOrganTree() {
	var to = false;
	var treeId = 'gbOrganTree';
	$('#' + treeId)
			.jstree(
					{
						"core" : {
							"animation" : 0,
							"check_callback" : function(operation, node,
									parent, position) {
								if (operation == 'move_node'
										|| operation == 'copy_node') {
									if (this.get_type(node).indexOf('platform') > -1) {
										return false;
									}
									if (node.a_attr['cmsId'] != parent.a_attr['cmsId']) {
										return false;
									}

								}
								return true;
							},
							"themes" : {
								"stripes" : true
							},
							'data' : {
								'type' : 'POST',
								'dataType' : 'JSON',
								'url' : function(node) {
									return ctxpath + '/organTree/getOrgans';
								},
								'data' : function(node) {
									return {
										"id" : node.id,
									};
								},
								'error' : function(msg) {
									alert(msg);
								}

							}
						},
						"types" : {
							"#" : {
								"max_children" : 1,
								"max_depth" : 10,
								"valid_children" : [ "root" ]
							},
							"platform" : {
								"icon" : "fa fa-sitemap",
								"valid_children" : [ "organ"]
							},
							"organ" : {
								"icon" : "fa fa-th-list",
								"valid_children" : [ "organ","device" ]
							},
							"device" : {
								"icon" : "fa fa-video-camera",
								"valid_children" : []
							},

							"default" : {
								"icon" : "fa fa-folder-o",
								"valid_children" : [ "default", "file" ]
							},
						},
						"contextmenu" : {
							"items" : {
								"编辑" : {
									"separator_before" : false,
									"separator_after" : false,
									"label" : "编辑",
									"icon" : "fa fa-edit",
									"action" : function(data) {
										var ref = $.jstree
												.reference(data.reference), obj = ref.get_node(data.reference);
										if(ref.get_type(obj) != "organ"){
											jError("只支持编辑机构信息");
											return;
										}
										if (ref.is_selected(obj)) {
											$("[name='stdId']","#editGbOrganForm").val(ref.get_node(obj).a_attr['stdId']);
											$("[name='name']","#editGbOrganForm").val(ref.get_node(obj).text);
											$("[name='id']","#editGbOrganForm").val(ref.get_node(obj).a_attr['organId']);
											$("#editGbOrganModalEntry").click();
										}
									}
								},
								"剪切" : {
									"separator_before" : false,
									"separator_after" : false,
									"label" : "剪切",
									"icon" : "fa fa-cut",
									"action" : function(data) {
										var inst = $.jstree
										.reference(data.reference), obj = inst
										.get_node(data.reference);
										if (inst.is_selected(obj)) {
											inst.cut(inst.get_selected());
										} else {
											inst.cut(obj);
										}
									}
								},
								"粘贴" : {
									"separator_before" : false,
									"separator_after" : false,
									"label" : "粘贴",
									"icon" : "fa fa-paste",
									"action" : function(data) {
										var inst = $.jstree
												.reference(data.reference), obj = inst
												.get_node(data.reference);
										inst.paste(obj);
									}
								},
								"删除" : {
									"separator_before" : false,
									"separator_after" : false,
									"label" : "删除",
									"icon" : "fa fa-trash-o",
									"action" : function(data) {
											var ref = $.jstree.reference(data.reference), obj = ref.get_node(data.reference);
											var currentNodeType = ref.get_type(obj);
											var parentNodeType = ref.get_type(ref.get_parent(obj));
											
											if(currentNodeType == 'device'){
												deleteNodes.push(obj.id);
												ref.set_icon (obj, 'fa fa-times-circle');
												return;
											}
											
											var currentNodePositioin = 0;
											$.each(ref.get_children_dom(ref.get_parent(obj)),function(idx,data){
												if(data.id==obj.id){
													currentNodePositioin = idx;
												}
											});
											
											var target = true;
											ref.open_node(obj.id,function(data){
												$.each(ref.get_children_dom(obj),function(idx,data){
													var childNodeType = ref.get_type(data);
													if(childNodeType == 'device' && parentNodeType =='platform'){
														jError("该机构有设备，不能直挂载到平台下");
														ref.set_icon (obj, 'fa fa-th-list');
														target = false;
													}
												});
												if(target && currentNodeType =='organ'){
													$.each(ref.get_children_dom(obj),function(idx,data){
														ref.move_node(ref.get_node(data),ref.get_parent(obj),currentNodePositioin+idx);
													});
													deleteNodes.push(obj.id);
													ref.set_icon (obj, 'fa fa-times');
												}else{
													jError("不允许删除平台");
												}
											});
										}
								}
							}
						},
						"plugins" : [ "dnd", "search", "types",
								"wholerow", "contextmenu" ]

					}).bind("hover_node.jstree", function(e, data) {
				$("[title]").tooltip({
					placement : 'right',
					html : true
				});
			}).bind("rename_node.jstree", function(e, data) {
				// if (data.node.type.indexOf('organ') > -1) {
				// saveOrgan(data.node);
				// }
			}).bind("move_node.jstree", function(e, d) {
				// $.ajax({
				// type : 'post',
				// url : '/updatePersonOrgan',
				// data : {
				// "personId" : d.node.id,
				// "organId" : d.node.parent,
				// "oldOrganId" : d.old_parent,
				// "sessionId" : sessionId
				// },
				// });
			}).bind("paste.jstree", function(e, d) {
				// $.ajax({
				// type : 'post',
				// url : '/updatePersonOrgan',
				// data : {
				// "personId" : d.node[0].id,
				// "organId" : d.parent,
				// "sessionId" : sessionId
				// },
				// });
			}).bind("select_node.jstree", function(e, d) {
				// getPersonByOrgan(1, 0, d.node.id,
				// d.node.a_attr['permission'])
			}).bind("dblclick.jstree", function(event) {
				var ref = $.jstree.reference(event.target);
				var node = $(event.target).closest("li");
				$("#currentOrganName").html(ref.get_text(node));
				getDevices(1, 0, node[0].id);
			});
}

function getDevices(page, count, organId) {
	if (!page) {
		page = 1;
	}
	if (!count) {
		count = 0;
	}
	if (organId) {
		$("[name='organId']", "#organDeviceForm").val(organId);
	}
	$("#organDeviceForm").ajaxSubmit({
		type : 'post',
		url : ctxpath + '/organTree/getDevices',
		data : {
			"page.currentPage" : page,
			"page.recordCount" : count,
		},
		success : function(msg) {
			if ($("#deviceList").length > 0) {
				$(msg).find("tbody").each(function() {
					$("#deviceList").children('tbody').append($(this).html());
				});
			} else {
				$("#organDeviceList").html(msg);
			}

		}
	});
}

function resetGbOrganTree() {
	if(confirm("调整机构的层次结构后所有的国标共享信息将被回收,原机构组织结构将无法恢复,确认要调整吗？")){
		var nodes = $('#gbOrganTree').jstree(true).get_json();
		$.ajax({
			type : "post",
			url : ctxpath + "/organTree/resetGbOrganTree",
			data : {
				"organDatas" : JSON.stringify(nodes),
				"deleteNodeIds": getSimpleJSON(deleteNodes)
			},
			success : function(msg) {
				if (msg == 'success') {
					jSuccess('操作成功！');
					organReset();
				}
			}
		});
	}
}

function rectify() {
	$.ajax({
		type : "get",
		url : ctxpath + "/autoScanCtrl/rectify",
		success : function(msg) {
			if (msg == 'success') {
				jSuccess('操作成功！');
			}
		}
	});
}

function confirmGbShare() {
	var selectedNodes = $('#gbOrganTree').jstree(true).get_selected(true);
	var nodeArr = [];
	
	$.each(selectedNodes,function(idx,node){
		var share = new Object();
		share.id=node.id;
		share.path = node.a_attr['path'];
		share.children=[];
		nodeArr.push(share);
	});
	
	var share = new Object();
	share.id='#';
	share.path = '';
	share.children=nodeArr;
	
	var nodes = $('#gbOrganTree').jstree(true).get_json();

	$.ajax({
		url : ctxpath + "/organTree/confirmGbShare",
		type : "post",
		beforeSend : function() {
			if (gbPlatformId=="") {
				jError("请选择上级国标平台！");
				return false;
			}
			return true;
		},
		data : {
			"organIds" : JSON.stringify([share]),
			"gbPlatformId" : gbPlatformId,
			"nodes" : JSON.stringify(nodes)
		},
		success : function(msg) {
			if (msg == "success") {
				jSuccess("操作成功！");
			}else{
				jError("操作失败！");
			}
		}
	});
}

var sharedResource;
var gbPlatformId = "";
function showResourcesForPlatform(platformId) {
	gbPlatformId = platformId;
	$(".accordion-toggle").css("color", "black");
	$("#" + platformId + "_gbPlatform").css("color", "red");
	var ref = $('#gbOrganTree').jstree(true);
	ref.deselect_all();
	$.ajax({
		type:"get",
		url:ctxpath+"/organTree/showResourcesForPlatform",
		data:{
			"platformId":platformId
		},
		success:function(msg){
			sharedResource = JSON.parse(msg);
			$.each(sharedResource[0]['children'], function(key, value) {
				ref.select_node(value['id']);
			});
			$("#collapse_"+platformId+" .panel-body").html('<p style="cursor:pointer;" ondblclick="">共享机构总数：'+sharedResource[1]['organ']+'</p>'+
															'<p style="cursor:pointer;" ondblclick="">共享设备总数：'+sharedResource[1]['device']+'</p>');
		}
	});
}


function searchResource() {
	var to = false;
	var treeId = 'gbOrganTree';
	$('#organ_q').keydown(function() {
		if (event.keyCode == 13) {
			if (to) {
				clearTimeout(to);
			}
			to = setTimeout(function() {
				var v = $('#organ_q').val();
				var ref = $('#' + treeId).jstree(true);
				ref.close_all();
				$.ajax({
					type : "post",
					url : ctxpath + "/organTree/searchResource",
					data : {
						"value" : v
					},
					success : function(msg) {
						var paths = JSON.parse(msg);
						$.each(paths, function(idx, pathJson) {
							var ids = pathJson.path.split('/');
							ref._search_open(ids);
						});
					}
				});

			}, 250);
		}
	});
}

function setGbOrganTreeForShare() {
	var treeId = 'gbOrganTree';
	$('#' + treeId).jstree({
		"core" : {
			"animation" : 0,
			"check_callback" : function(operation, node, parent, position) {
				return false;
			},
			"themes" : {
				"stripes" : true
			},
			'data' : {
				'type' : 'POST',
				'dataType' : 'JSON',
				'url' : function(node) {
					return ctxpath + '/organTree/getResourceForShare';
				},
				'data' : function(node) {
					return {
						"id" : node.id
					};
				},
				'error' : function(msg) {
					alert(msg);
				}

			}
		},
		"types" : {
			"#" : {
				"max_children" : 1,
				"max_depth" : 10,
				"valid_children" : [ "root" ]
			},
			"platform" : {
				"icon" : "fa fa-sitemap",
				"valid_children" : [ "organ" ]
			},
			"organ" : {
				"icon" : "fa fa-th-list",
				"valid_children" : [ "organ" ]
			},
			"device" : {
				"icon" : "fa fa-video-camera",
				"valid_children" : []
			},

			"default" : {
				"icon" : "fa fa-folder-o",
				"valid_children" : [ "default", "file" ]
			},
		},
		"plugins" : [ "search", "types", "checkbox", "wholerow" ]

	}).bind("open_node.jstree", function(e, data) {
		if(sharedResource){
			$.each(sharedResource[0]['children'], function(key, value) {
				var ref = $.jstree.reference(e.target);
				ref.select_node(value['id']);
			});
		}
	}).bind("hover_node.jstree", function(e, data) {
		$("[title]").tooltip({
			placement : 'right',
			html : true
		});
	});
}

function organReset() {
	setTabsCss("organReset");
	$.ajax({
		type:"get",
		url:ctxpath+"/organTree/organReset",
		success:function(msg){
			$("#content").html(msg);
		}
	});
	
}

function forGbShare(cmsId) {
	setTabsCss("forGbShare");
	$.ajax({
		type:"get",
		url:ctxpath+"/organTree/toGbShare",
		data:{
			"cmsId":cmsId
		},
		success:function(msg){
			$("#content").html(msg);
		}
	});
}

function forConfigManage(cmsId) {
	window.open(ctxpath+"/initDB");
}

function validOrganForm(form,callback){
	var form1 = $("#" + form);
	var error1 = $('.alert-danger', form1);
	var success1 = $('.alert-success', form1);
	form1.validate({
		errorElement : 'span', // default input error message container
		errorClass : 'help-block', // default input error message class
		focusInvalid : false, // do not focus the last invalid input
		ignore : "",
		rules : {
			"stdId": {
				required : true,
				remote : {
					url : ctxpath+"/organTree/validOrganStdId",
					type : "post",
					data :{
						"stdId" : function(){
							return $("[name='id']",form1).val()+"__"+$("[name='stdId']",form1).val();
							}
						}
					}
			}
		},
			
		messages : {
			"stdId" : {
				required : "国标编号不能为空！",
				remote:"该国标编号已经被占用！"
			}
		},
		
		highlight : function(element) { // hightlight error inputs
			$(element).closest('.form-group').addClass('has-error'); // set
		},
		
		unhighlight : function(element) { // revert the change done by
			// hightlight
			$(element).closest('.form-group').removeClass('has-error'); // set
		},
		success : function(label) {
			label.closest('.form-group').removeClass('has-error'); // set
		},
		submitHandler : function(form) {
			callback();
		}
	});
}

function editOrgan(){
	$("#editGbOrganForm").ajaxSubmit({
		type:"post",
		url:ctxpath+"/organTree/editOrgan",
		success:function(msg){
			if(msg=='success'){
				$("#editGbOrganModal .close").click();
				jSuccess("操作成功！");
				setTimeout(function(){organReset();},2000);
			}
		}
	});
}
