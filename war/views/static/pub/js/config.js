var Config = {
	jdbcConf:function(){
		InitConfig.setTabsCss("configManage");
		InitConfig.setActiveSubMenu("jdbcConf");
		$.ajax({
			type : 'get',
			url : ctxpath + "/configManage/getJdbcConfig",
			success : function(msg) {
				$("#content").html(msg);
			}
		});
	},
	updateJdbcConf:function(){
		if(confirm("更改数据库配置需要重启目录服务器才生效，确定要更改吗？")){
			$("#updateJdbcConfForm").ajaxSubmit({
				type : 'post',
				url : ctxpath + "/configManage/updateJdbcConfig",
				success : function(msg) {
					if(msg=="success"){
						jSuccess("操作成功！");
					}else{
						jError("操作失败，请确保数据库用户名/密码，IP:PORT信息正确！");
					}
				}
			});
		}
	},
	registConf:function(){
		InitConfig.setTabsCss("configManage");
		InitConfig.setActiveSubMenu("registConf");
		$.ajax({
			type : 'get',
			url : ctxpath + "/configManage/getRegistConf",
			success : function(msg) {
				$("#content").html(msg);
			}
		});
	},
	updateRegistConf:function(){
		if(confirm("更改目录参数配置需要重启目录服务器才生效，确定要更改吗？")){
			$("#updateRegistConfForm").ajaxSubmit({
				type : 'post',
				url : ctxpath + "/configManage/updateRegistConf",
				success : function(msg) {
					if(msg=="success"){
						jSuccess("操作成功！");
					}
				}
			});
		}
	},
	updateTaskConf:function(){
		if(confirm("更改定时任务配置需要重启目录服务器才生效，确定要更改吗？")){
			$("#updateTaskConfForm").ajaxSubmit({
				type : 'post',
				url : ctxpath + "/configManage/updateTaskConf",
				success : function(msg) {
					if(msg=="success"){
						jSuccess("操作成功！");
					}
				}
			});
		}
	},
	taskConf:function(){
		InitConfig.setTabsCss("configManage");
		InitConfig.setActiveSubMenu("taskConf");
		$.ajax({
			type : 'get',
			url : ctxpath + "/configManage/getTaskConf",
			success : function(msg) {
				$("#content").html(msg);
			}
		});
	},
	log4jConf:function(){
		InitConfig.setTabsCss("configManage");
		InitConfig.setActiveSubMenu("log4jConf");
		$.ajax({
			type : 'get',
			url : ctxpath + "/configManage/getLog4jConf",
			success : function(msg) {
				$("#content").html(msg);
			}
		});
	},
	updateLog4jConf:function(){
		if(confirm("更改系统日志配置需要重启目录服务器才生效，确定要更改吗？")){
			$("#updateLog4jConfForm").ajaxSubmit({
				type : 'post',
				url : ctxpath + "/configManage/updateLog4jConf",
				success : function(msg) {
					if(msg=="success"){
						jSuccess("操作成功！");
					}
				}
			});
		}
	},
	developerConf:function(){
		InitConfig.setTabsCss("configManage");
		InitConfig.setActiveSubMenu("developerConf");
		$.ajax({
			type : 'get',
			url : ctxpath + "/configManage/getDeveloperConf",
			success : function(msg) {
				$("#content").html(msg);
			}
		});
	},
	updateDeveloperConf:function(){
		if(confirm("更改国标调试配置需要重启目录服务器才生效，确定要更改吗？")){
			$("#updateDeveloperConfForm").ajaxSubmit({
				type : 'post',
				url : ctxpath + "/configManage/updateDeveloperConf",
				success : function(msg) {
					if(msg=="success"){
						jSuccess("操作成功！");
					}
				}
			});
		}
	},
	exportJdbcConf:function(){
		var user =  $("[name='user']","#updateJdbcConfForm").val();
		var password =  $("[name='password']","#updateJdbcConfForm").val();
		var ipPort =  $("[name='ipPort']","#updateJdbcConfForm").val();
		var dbName =  $("[name='dbName']","#updateJdbcConfForm").val();
		location.href=ctxpath + "/configManage/exportJdbcConf?user="+user+"&password="+password+"&ipPort="+ipPort+"&dbName="+dbName;
	},
	forImportMyconf:function(){
		InitConfig.setTabsCss("configManage");
		InitConfig.setActiveSubMenu("forImportMyconf");
		$.ajax({
			type : 'get',
			url : ctxpath + "/configManage/forImportMyconf",
			success : function(msg) {
				$("#content").html(msg);
			}
		});
	},
	forExportMyconf:function(){
		InitConfig.setTabsCss("configManage");
		InitConfig.setActiveSubMenu("forExportMyconf");
		location.href=ctxpath + "/configManage/exportMyconf";
	}
};