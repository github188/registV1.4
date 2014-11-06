CREATE TABLE `outer_device_alarm` (
  `id` varchar(40) NOT NULL DEFAULT '',
  `cms_id` varchar(6) DEFAULT NULL,
  `device_id` varchar(31) DEFAULT NULL,
  `device_naming` varchar(100) DEFAULT NULL,
  `device_type` varchar(50) DEFAULT NULL,
  `scheme_id` varchar(31) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=gbk;

alter table permission  add permission varchar(20);
alter table permission  add organ_device_id varchar(31);
alter table permission  add resource_outer  bit(1) default 0;
alter table permission  add status varchar(20);
alter table permission  add child_resource  bit(1) default 1;
alter table permission  add support_scheme  bit(1) default 0;

alter table platform add event_server_ip varchar(255);
alter table platform add event_server_port varchar(255);
alter table platform add outer_platform bit(1) default 0;
alter table permission rename old_permission;

	alter table platform rename old_platform;
	alter table r_user_role rename old_r_user_role;
	alter table role rename old_role;
	alter table user rename old_user;
	alter table outer_device_alarm rename old_outer_device_alarm;
	alter table platform_organ rename old_organ;
	alter table regist_server rename old_regist_server;
	alter table r_role_permission rename old_r_role_permission;
	alter table r_user_permission rename old_r_user_permission;
	create table authorization (
        id varchar(31) not null,
        cms_id varchar(6),
        granted_id varchar(255),
        granted_type integer,
        item varchar(255),
        resource_cms_id varchar(255),
        resource_id varchar(255),
        resource_path varchar(255),
        resource_type integer,
        primary key (id)
    )ENGINE=INNODB DEFAULT CHARSET=gbk;

    create table device (
        id varchar(255) not null,
        allocated bit not null,
        cms_id varchar(255),
        device_id varchar(255),
        location varchar(255),
        name varchar(255),
        naming varchar(255),
        organ_id varchar(255),
        outer_platforms varchar(255),
        owner_id varchar(255),
        path varchar(1000),
        permission varchar(255),
        record_count bigint not null,
        status varchar(255),
        support_scheme bit not null,
        type varchar(255),
        longitude char(10),
        latitude char(10),
        primary key (id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;

    create table organ (
        id varchar(255) not null,
        cms_id varchar(255),
        name varchar(255),
        organ_id varchar(255),
        parent_organ_id varchar(255),
        parent_organ_name varchar(255),
        path varchar(1000),
        status varchar(255),
        type varchar(255) default 'ORGAN',
        parent_id varchar(255),
        primary key (id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;

    create table outer_device_alarm (
        id varchar(40) not null,
        cms_id varchar(6),
        device_id varchar(31),
        device_naming varchar(100),
        device_type varchar(50),
        scheme_id varchar(31),
        primary key (id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;

    create table platform (
        id varchar(255) not null,
        event_server_ip varchar(255),
        event_server_port integer,
        name varchar(255),
        outer_platform bit not null,
        outer_platform_cms_ids varchar(480),
        owner bit not null,
        parent_cms_id varchar(255),
        password varchar(255),
        service_url varchar(255),
        status integer,
        parent_id varchar(255),
        primary key (id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;

    create table r_user_role (
        role_id varchar(50) not null,
        user_id varchar(50) not null,
        primary key (role_id, user_id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;

    create table regist_server (
        id integer not null auto_increment,
        url varchar(255),
        city_server bit not null,
        password varchar(255),
        regist_server_code varchar(255),
        parent_id integer,
        primary key (id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;

    create table role (
        id varchar(30) not null,
        cms_id varchar(255),
        name varchar(20),
        note varchar(255),
        primary key (id),
        unique(cms_id,name)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;

    create table share (
        id varchar(255) not null,
        item varchar(255),
        platform_cms_id varchar(255),
        resource_cms_id varchar(255),
        resource_id varchar(255),
        resource_path varchar(255),
        resource_type integer,
        primary key (id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;

    create table user (
        id varchar(50) not null,
        cms_id varchar(255),
        logon_name varchar(255),
        name varchar(255),
        naming varchar(255),
        organ_id varchar(255),
        password varchar(255),
        sex varchar(255),
        primary key (id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;
    
     create table device_server (
        id varchar(255) not null,
        ip varchar(255),
        cms_id varchar(255),
        location varchar(255),
        name varchar(255),
        organ_id varchar(255),
        stream_support varchar(255),
        type varchar(255),
        primary key (id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;

    alter table organ 
        add index FK65192112DF8BFC1 (parent_id), 
        add constraint FK65192112DF8BFC1 
        foreign key (parent_id) 
        references organ (id);

    alter table platform 
        add index FK6FBD6873FA7F063 (parent_id), 
        add constraint FK6FBD6873FA7F063 
        foreign key (parent_id) 
        references platform (id);

    alter table r_user_role 
        add index FK95488DD4F937D1A (role_id), 
        add constraint FK95488DD4F937D1A 
        foreign key (role_id) 
        references role (id);

    alter table r_user_role 
        add index FK95488DDF4BE40FA (user_id), 
        add constraint FK95488DDF4BE40FA 
        foreign key (user_id) 
        references user (id);

    alter table regist_server 
        add index FK71CAD42CF8BF4A49 (parent_id), 
        add constraint FK71CAD42CF8BF4A49 
        foreign key (parent_id) 
        references regist_server (id);
   ## 2011-11-22 增加authorization和device表的索引,加快查询速度 ##
   alter table authorization modify column granted_id varchar(50);
   alter table authorization modify column resource_id varchar(50);
   alter table authorization add index(cms_id,granted_id,resource_cms_id,resource_type);
   alter table device modify column cms_id char(6);
   alter table device add index(cms_id,owner_id);
   create table static_file (
        user_id varchar(50) not null,
        primary key (user_id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;
    
   ## 2011-12-18 PM device增加serverId和stdId,platform表增加access_server,device_server增加manufacturer,model,status,stdId实现国标 ##
   alter table device add column server_id varchar(50);
   alter table device add column std_id varchar(50);
   alter table device add column dispatcher_platform_id varchar(50);
   alter table device_server add column manufacturer varchar(255);
   alter table device_server add column model varchar(255);
   alter table device_server add column status varchar(20);
   alter table device_server add column std_id varchar(50);
   alter table platform add column access_server varchar(100);
   
   ## 2011-12-18 PM 增加派出所表 , 设备相关的dispatcher_platform_id##
   create table dispatcher_platform (
        id varchar(255) not null,
        location varchar(255),
        name varchar(255),
        note varchar(255),
        platform_id varchar(255),
        primary key (id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;
     alter table dispatcher_platform 
        add index FKA00E8B0B47881B59 (platform_id), 
        add constraint FKA00E8B0B47881B59 
        foreign key (platform_id) 
        references platform (id);
        
   ## 2012-03-09 AM 增加国标平台状态表
    create table platform_status (
        id varchar(255) not null,
        description varchar(255),
        error_code varchar(255),
        heart_beat_cycle integer,
        heartbeat_time datetime,
        online bit,
        platform_id varchar(255),
        primary key (id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;;
    alter table platform_status 
        add index FK62428D3E47881B59 (platform_id), 
        add constraint FK62428D3E47881B59 
        foreign key (platform_id) 
        references platform (id);
      
    ## 2012-03-21 AM 增加上级平台的发送方式,send_type为terminal时,只发送二级设备,
    ## send_type为server或null时,先发送一级设备,再发送二级设备.platform作为国标上级时,
    ## 相应的child_platform_id 作为国标下级平台的平台ID.  
    alter table platform add column send_type varchar(10);
    alter table platform add column child_platform_id varchar(31);
    alter table platform add column page_size int;
   

