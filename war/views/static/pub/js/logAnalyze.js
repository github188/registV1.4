var LogAnalyze = {
	log4jList:function(){
		InitConfig.setTabsCss("logAnalyze");
		InitConfig.setActiveSubMenu("log4jList");
		location.href = ctxpath + "/logAnalyze/getLog4j";
	},
	gbLogList:function(){
		InitConfig.setTabsCss("logAnalyze");
		InitConfig.setActiveSubMenu("gbLogList");
		$.ajax({
			type : 'get',
			url : ctxpath + "/logAnalyze/getGbLogList",
			success : function(msg) {
				$("#content").html(msg);
			}
		});
	},
	deleteLogFile:function(fileName){
		if(confirm("确认要删除吗？")){
			$.ajax({
				type : 'post',
				url : ctxpath + "/logAnalyze/deleteLogFile",
				data:{
					"fileName":fileName
				},
				success : function(msg) {
					if(msg=='success'){
						jSuccess("操作成功！");
						LogAnalyze.gbLogList();
					}
				}
			});
		}
	},
	
};