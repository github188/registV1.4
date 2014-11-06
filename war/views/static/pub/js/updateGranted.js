var UpdateGranted = {

checkAuthorizations:"",
granted:{
	id:0,
	type:0
},

getGrantedList:function(page,count){
	if($("[name='grantedType']", "#updateGrantedForm").val()=='USER'){
		UpdateGranted.clear(null, 0);
		$("#authorizationMain .grantedTitle").html('<div class="caption"><i class="icon-user"></i>用户</div>');
		$("#authorizationMain .userRoleTitle").html('<h4>角色列表</h4>');
		if (!page) {
			page = 1;
		}
		if (!count) {
			count = 0;
		}
		$("#updateGrantedForm").ajaxSubmit({
			type : 'post',
			url : ctxpath + "/updateGranted/getUserList",
			data : {
				"page.currentPage" : page,
				"page.recordCount" : count,
				"page.position":"updateUserList",
			},
			success : function(msg) {
				$("#authorizationMain .grantedList").html(msg);
			}
		});
		$.ajax({
			type : 'get',
			url : ctxpath + "/updateGranted/getAllRoles",
			success : function(msg) {
				$("#userRoleList").html(msg);
			}
		});
	}else{
		UpdateGranted.clear(null, 1);
		$("#authorizationMain .grantedTitle").html('<div class="caption"><i class="icon-user"></i>角色</div>');
		$("#authorizationMain .userRoleTitle").html('<h4>用户列表</h4>');
		if (!page) {
			page = 1;
		}
		if (!count) {
			count = 0;
		}
		$("#updateGrantedForm").ajaxSubmit({
			type : 'post',
			url : ctxpath + "/updateGranted/getRoleList",
			data : {
				"page.currentPage" : page,
				"page.recordCount" : count,
				"page.position":"updateRoleList",
			},
			success : function(msg) {
				$("#authorizationMain .grantedList").html(msg);
			}
		});
		
		$.ajax({
			type : 'get',
			url : ctxpath + "/updateGranted/getAllUsers",
			success : function(msg) {
				$("#userRoleList").html(msg);
			}
		});
	}
},
showAuthorizationMain:function(){
	UpdateGranted.checkAuthorizations="";
	$.ajax({
		type : "GET",
		url : ctxpath + "/updateGranted/showAuthorizationMain",
		success : function(msg) {
			$("#authorizationMain").html(msg);
		}
	});
},
setOrganTreeForGranted:function(treeId){
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
						return ctxpath + '/updateGranted/getResourceForGranted';
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
			var ref = $.jstree.reference(e.target);
			if(UpdateGranted.checkAuthorizations){
				$.each(UpdateGranted.checkAuthorizations[2]['children'], function(key, value) {
					if(value['resourceType']=="0"){
						ref.select_node("device__"+value['resourceId']);
					}else{
						ref.select_node(value['resourceId']);
					}
				});
			}
		}).bind("hover_node.jstree", function(e, data) {
				 $("[title]").tooltip({
				 placement : 'right',
				 html : true,
				 });
		});
},
showUserResource:function(userId){
	UpdateGranted.clear(userId, 0);
	$.ajax({
		type:'get',
		url:ctxpath+"/updateGranted/getResourceByUserId",
		data:{
			"userId":userId
		},
		success:function(msg){
			UpdateGranted.checkAuthorizations = JSON.parse(msg);
			var ref = $('#organTreeForGranted').jstree(true);
			$.each(UpdateGranted.checkAuthorizations[2]['children'], function(key, value) {
				if(value['resourceType']=="device"){
					ref.select_node("device__"+value['resourceId']);
				}else{
					ref.select_node(value['resourceId']);
				}
			});
			$.each(UpdateGranted.checkAuthorizations[0]['children'], function(key, value) {
				$("[name="+value['id']+"]","#userRoleList").attr("checked",true);
			});
			jQuery.uniform.update();
		}
	});
},
showRoleResource:function(roleId){
	UpdateGranted.clear(roleId, 1);
	$.ajax({
		type:'get',
		url:ctxpath+"/updateGranted/getResourceByRoleId",
		data:{
			"roleId":roleId
		},
		success:function(msg){
			UpdateGranted.checkAuthorizations = JSON.parse(msg);
			var ref = $('#organTreeForGranted').jstree(true);
			$.each(UpdateGranted.checkAuthorizations[2]['children'], function(key, value) {
				if(value['resourceType']=="0"){
					ref.select_node("device__"+value['resourceId']);
				}else{
					ref.select_node(value['resourceId']);
				}
			});
			$.each(UpdateGranted.checkAuthorizations[1]['children'], function(key, value) {
				$("[name="+value['id']+"]","#userRoleList").attr("checked",true);
			});
			jQuery.uniform.update();
		}
	});
},

authorization:function(){
	var userRoleList = [];
	if(UpdateGranted.granted.id==0){
		jError("请选中需要更新权限的用户或角色！");
		return ;
	}
	$('#userRoleList :checked').each(function() {
		if($(this).val()!='on'){
			userRoleList.push($(this).val());
		}
	});
	
	var selectedNodes = $('#organTreeForGranted').jstree(true).get_selected(true);
	var nodeArr = [];
	$.each(selectedNodes,function(idx,node){
		var resource = new Object();
		resource.id=node.id;
		resource.path = node.a_attr['path'];
		resource.platformId = node.a_attr['platformId'];
		resource.children=[];
		nodeArr.push(resource);
	});
	var resource = new Object();
	resource.id='#';
	resource.path = '';
	resource.children=nodeArr;
	var nodes = $('#organTreeForGranted').jstree(true).get_json();
	
	$.ajax({
		url : ctxpath + "/updateGranted/confirmAuthorization",
		type : "post",
		data : {
			"organIds" : JSON.stringify([resource]),
			"grantedId" : UpdateGranted.granted.id,
			"grantedType" : UpdateGranted.granted.type,
			"nodes" : JSON.stringify(nodes),
			"userRoleList":getSimpleJSON(userRoleList)
		},
		success : function(msg) {
			if (msg == "success") {
				jSuccess("操作成功！");
			}else{
				jError("操作失败！");
			}
		}
	});
	
},
clear:function(grantedId,grantedType){
	if(grantedId){
		var type ="role_";
		var listId = "#updateRoleList";
		if(grantedType==0){
			type="user_";
			listId = "#updateUserList";
		}
		$(listId+" .gradeX").css("color", "black");
		$("#"+type + grantedId).css("color", "red");
		UpdateGranted.granted.id=grantedId;
		UpdateGranted.granted.type=grantedType;
	}
	$.each($("[type=checkbox],#userRoleList"), function(key, value) {
		$(this).attr("checked",false);
	});
	var ref = $('#organTreeForGranted').jstree(true);
	ref.deselect_all();
},

};
