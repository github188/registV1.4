var FormWizard = function () {


    return {
        init: function () {
            if (!jQuery().bootstrapWizard) {
                return;
            }
            var handleTitle = function(tab, navigation, index) {
                var total = navigation.find('li').length;
                var current = index + 1;
                // set wizard title
                $('.step-title', $('#form_wizard_1')).text('步骤 ' + (index + 1) + ' / ' + total);
                // set done steps
                jQuery('li', $('#form_wizard_1')).removeClass("done");
                var li_list = navigation.find('li');
                for (var i = 0; i < index; i++) {
                    jQuery(li_list[i]).addClass("done");
                }

                if (current == 1) {
                    $('#form_wizard_1').find('.button-previous').hide();
                } else {
                    $('#form_wizard_1').find('.button-previous').show();
                }

                if (current >= total) {
                    $('#form_wizard_1').find('.button-next').hide();
                    $('#form_wizard_1').find('.button-submit').show();
                    displayConfirm();
                } else {
                    $('#form_wizard_1').find('.button-next').show();
                    $('#form_wizard_1').find('.button-submit').hide();
                }
                App.scrollTo($('.page-title'));
            };

            // default form wizard
            $('#form_wizard_1').bootstrapWizard({
                'nextSelector': '.button-next',
                'previousSelector': '.button-previous',
                onTabClick: function (tab, navigation, index, clickedIndex) {
                    if (clickedIndex>0){
                    	setSelected("userList",selectedUser);
                    	setSelected("roleList",selectedRole);
                    	if(selectedUser.length==0 && selectedRole.length==0) {
                    		jError("请选择用户或角色");
                            return false;
                    	}
                    }
                    
                    if(clickedIndex>1){
                    	setSelected("organList",selectedOrgan);
                    	setSelected("deviceList",selectedDevice);
                    	if(selectedOrgan.length==0 && selectedDevice.length==0) {
                    		jError("请选择机构或设备");
                            return false;
                    	}
                    }
                    
                    if(clickedIndex == 2){
                    	confirmAuthorization();
                    }
                    
                    handleTitle(tab, navigation, clickedIndex);
                    if(clickedIndex==1 && $("#resourceList").html()==""){
                    	getOrganList();
                    }
                    
                },
                onNext: function (tab, navigation, index) {

                    handleTitle(tab, navigation, index);
                },
                onPrevious: function (tab, navigation, index) {
                    handleTitle(tab, navigation, index);
                },
                onTabShow: function (tab, navigation, index) {
                    var total = navigation.find('li').length;
                    var current = index + 1;
                    var $percent = (current / total) * 100;
                    $('#form_wizard_1').find('.progress-bar').css({
                        width: $percent + '%'
                    });
                }
            });
        }

    };

}();