var InitConfig = {
	initDB : function() {
		if(confirm("数据库初始化前，请备份好数据库！以免造成数据丢失！初始化需要1到2分钟的时间，确定要初始化吗")){
			this.setTabsCss("dataManage");
			this.setActiveSubMenu("initDB");
			$("#content").html("");
			$.ajax({
				type : 'get',
				url : ctxpath + "/initDB/initAll",
				success : function(msg) {
					jSuccess(msg);
				}
			});
		}
	},
	rectify : function() {
		this.setTabsCss("dataManage");
		this.setActiveSubMenu("rectifyData");
		$("#content").html("");
		$.ajax({
			type : 'get',
			url : ctxpath + "/autoScanCtrl/rectify",
			success : function(msg) {
				if(msg == "success"){
					jSuccess("数据纠正成功！");
				}
			}
		});
	},
	pullData:function(){
		this.setTabsCss("dataManage");
		this.setActiveSubMenu("pullData");
		$.ajax({
			type : 'get',
			url : ctxpath + "/initDB/showOracleMsgList",
			success : function(msg) {
				$("#content").html(msg);
			}
		});
	},
	importData:function(){
		this.setTabsCss("dataManage");
		this.setActiveSubMenu("importData");
		$.ajax({
			type : 'get',
			url : ctxpath + "/initDB/forImportData",
			success : function(msg) {
				$("#content").html(msg);
			}
		});
	},
	updateOracleConf:function(cmsId){
		$("#oracleForm_"+cmsId).ajaxSubmit({
			type : 'post',
			url : ctxpath + "/initDB/updateOracleConf",
			success : function(msg) {
				if(msg=="success"){
					jSuccess("操作成功！");
					InitConfig.pullData();
				}
			}
		});
	},
	deleteOralceConf:function(cmsId){
		if(confirm("确定要删除该配置吗")){
			$.ajax({
				type : 'get',
				url : ctxpath + "/initDB/deleteOracleConf?cmsId="+cmsId,
				success : function(msg) {
					if(msg=="success"){
						jSuccess("操作成功！");
						InitConfig.pullData();
					}
				}
			});
		}
	},
	addOracleConf:function(){
		$("#addOracleForm").ajaxSubmit({
			type : 'post',
			url : ctxpath + "/initDB/addOracleConf",
			success : function(msg) {
				if(msg=="success"){
					$("#addOracleModal .close").click();
					jSuccess("操作成功！");
					setTimeout(function(){InitConfig.pullData();},2000);
				}else{
					$("#cmsIdValid").show();
				}
			}
		});
	},
	testOracleConf:function(){
		$("#addOracleForm").ajaxSubmit({
			type : 'post',
			url : ctxpath + "/initDB/testOracleConf",
			success : function(msg) {
				if(msg=="success"){
					$("#testOracle").html("连接成功！");
				}else{
					$("#testOracle").html("连接失败，请检查配置是否正确！");
				}
				$("#testOracle").show();
			}
		});
	},
	executePull:function(cmsId){
		$("#oracleForm_"+cmsId).ajaxSubmit({
			type : 'post',
			url : ctxpath + "/initDB/executePull?cmsId="+cmsId,
			success : function(msg) {
				var result = JSON.parse(msg);
				$("#pullResult_"+cmsId).html(result['result']);
				$("#pullResult_"+cmsId).show();
			}
		});
	},
	executePullAll:function(){
		$.ajax({
			type : 'get',
			url : ctxpath + "/initDB/executeAllPull",
			success : function(msg) {
				var results = JSON.parse(msg);
				$.each(results,function(idx,result){
					if(result['cmsId'] == 'all'){
						jSuccess(result['result']);
					}else{
						$("#pullResult_"+result['cmsId']).html(result['result']);
						$("#pullResult_"+result['cmsId']).show();
					}
				});
			}
		});
	},
	showSyncStatus:function(cmsId){
		$("#syncExecMsg").hide();
		$("#device_1").html(0);
		$("#device_0").html(0);
		$("#organ_0").html(0);
		$("#organ_1").html(0);
		$("#vis_0").html(0);
		$("#vis_1").html(0);
		$.ajax({
			type : 'get',
			url : ctxpath + "/initDB/getSyncStatus?cmsId="+cmsId,
			success : function(msg) {
				var result = JSON.parse(msg);
				$.each(result, function(key, value){
				   $("#"+key).html(value);
				});
				$("#cmsId").val(cmsId);
				$("#syscStatusModalEntry").click();
			}
		});
	},
	clearSyncStatus:function(){
		$("#syncExecMsg").hide();
		if(confirm("确定要清除状态表device_status,organ_status,vis_status数据吗,清除后只能通过点击 触发数据推送 按钮才能恢复状态数据，请确定？")){
			$.ajax({
				type : 'post',
				url : ctxpath + "/initDB/clearSyncStatus?cmsId="+$("#cmsId").val(),
				success : function(msg) {
					if(msg == "success"){
						$("#device_1").html(0);
						$("#device_0").html(0);
						$("#organ_0").html(0);
						$("#organ_1").html(0);
						$("#vis_0").html(0);
						$("#vis_1").html(0);
						$("#syncExecMsg").html("清除成功！");
						$("#syncExecMsg").show();
					}
				}
			});
		}
	},
	triggerPush:function(){
		$("#syncExecMsg").hide();
		if(confirm("触发平台的share.war推送数据给所配置的目录服务器，确定吗？")){
			$.ajax({
				type : 'post',
				url : ctxpath + "/initDB/triggerPush?cmsId="+$("#cmsId").val(),
				success : function(msg) {
					if(msg == "success"){
						$("#syncExecMsg").html("已经成功触发，结果请稍后查看平台状态数据！");
						$("#syncExecMsg").show();
					}
				}
			});
		}
	},
	setActiveSubMenu:function(currentItem){
		$("div .sub-menu >li").removeClass("active");
		$("#"+currentItem).addClass("active");
	},
	setTabsCss: function(currentTabId) {
		$("div .page-sidebar .classic-menu-dropdown").removeClass("active");
		$("#"+currentTabId).addClass("active");
	},
};