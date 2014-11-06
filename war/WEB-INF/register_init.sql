    drop table if exists authorization;

    drop table if exists device;

    drop table if exists device_server;

    drop table if exists device_status;

    drop table if exists gb_device;

    drop table if exists gb_organ;

    drop table if exists gb_platform;

    drop table if exists gb_share;

    drop table if exists organ;

    drop table if exists organ_server;

    drop table if exists organ_status;

    drop table if exists outer_device_alarm;

    drop table if exists platform;

    drop table if exists platform_organ;

    drop table if exists platform_status;

    drop table if exists r_user_role;

    drop table if exists role;

    drop table if exists server_status;

    drop table if exists share;

    drop table if exists subscribe;

    drop table if exists subscribe_event;

    drop table if exists task_status;

    drop table if exists user;

    create table authorization (
        id int(20) not null auto_increment,
        cms_id int(20),
        granted_id int(20),
        granted_type int(20),
        item varchar(255),
        resource_cms_id int(20),
        resource_id int(20),
        resource_path varchar(255),
        resource_type varchar(255),
        primary key (id),
        unique (granted_id, granted_type, resource_id, resource_type)
    ) ENGINE=InnoDB default charset=gbk;

    create table device (
        id int(20) not null auto_increment,
        alarm_reset varchar(255),
        alarm_statu varchar(255),
        allocated bit not null,
        change_time datetime,
        cms_id varchar(255),
        device_id varchar(255),
        dircetion_type int(20),
        dispatcher_platform_id varchar(255),
        gps_z varchar(255),
        latitude varchar(255),
        location varchar(255),
        longitude varchar(255),
        name varchar(255),
        naming varchar(255),
        nanosecond int(20) not null,
        online varchar(255),
        outer_platforms varchar(255),
        owner_id varchar(255),
        path varchar(255),
        permission varchar(255),
        position_type int(20),
        ptz_type int(20),
        record_count bigint not null,
        room_type int(20),
        server_id varchar(255),
        status varchar(255),
        std_id varchar(255),
        supply_light_type int(20),
        support_scheme bit not null,
        sync bit not null,
        type varchar(255),
        use_type int(20),
        organ_id int(20),
        platform_id int(20),
        primary key (id),
        unique (device_id, owner_id, cms_id)
    ) ENGINE=InnoDB default charset=gbk;

    create table device_server (
        id int(20) not null auto_increment,
        ip varchar(255),
        change_time datetime,
        children_status bit not null,
        cms_id varchar(255),
        inner_device bit not null,
        location varchar(255),
        manufacturer varchar(255),
        model varchar(255),
        name varchar(255),
        naming varchar(255),
        nanosecond int(20) not null,
        online varchar(255),
        organ_id int(20),
        server_id varchar(255),
        status varchar(255),
        std_id varchar(255),
        stream_support varchar(255),
        sync bit not null,
        type varchar(255),
        primary key (id),
        unique (server_id, cms_id)
    ) ENGINE=InnoDB default charset=gbk;

    create table device_status (
        id int(20) not null auto_increment,
        base_notify bit not null,
        base_notify_sign int(20) not null,
        change_time datetime,
        device_id int(20),
        location varchar(255),
        name varchar(255),
        nanosecond int(20) not null,
        online varchar(255),
        online_change_time datetime,
        online_nanosecond int(20) not null,
        online_notify bit not null,
        online_notify_sign int(20) not null,
        status varchar(255),
        subscribe_event_id int(20),
        platform_id int(20),
        primary key (id),
        unique (device_id, platform_id)
    ) ENGINE=InnoDB default charset=gbk;

    create table gb_device (
        id int(20) not null auto_increment,
        path varchar(255),
        suspend bit default false,
        device_id int(20),
        organ_id int(20),
        original_id int(20),
        primary key (id),
        unique (device_id, original_id)
    ) ENGINE=InnoDB default charset=gbk;

    create table gb_organ (
        id int(20) not null auto_increment,
        path varchar(255),
        source_id int(20),
        source_type varchar(255),
        suspend bit default false,
        parent_id int(20),
        primary key (id),
        unique (source_id, source_type)
    ) ENGINE=InnoDB default charset=gbk;

    create table gb_platform (
        id int(20) not null auto_increment,
        auto_push bit not null,
        child_cms_id varchar(255),
        cms_id varchar(255),
        contain bit not null,
        has_query bit not null,
        manufacturer varchar(255),
        name varchar(255),
        page_size int(20),
        password varchar(255),
        protocol varchar(255),
        send_type varchar(255),
        share_type varchar(255),
        sip_server varchar(255),
        standard_type varchar(255),
        type int(20),
        primary key (id),
        unique (cms_id)
    ) ENGINE=InnoDB default charset=gbk;

    create table gb_share (
        id int(20) not null auto_increment,
        platform_id int(20) not null,
        resource_id int(20),
        resource_path varchar(255),
        resource_type varchar(255),
        primary key (id),
        unique (platform_id, resource_id, resource_type)
    ) ENGINE=InnoDB default charset=gbk;

    create table organ (
        id int(20) not null auto_increment,
        block varchar(255),
        change_time datetime,
        cms_id varchar(255),
        name varchar(255),
        nanosecond int(20) not null,
        organ_id varchar(255),
        parent_organ_id varchar(255),
        parent_organ_name varchar(255),
        path varchar(255),
        status varchar(255),
        std_id varchar(255),
        sync bit not null,
        type varchar(255),
        parent_id int(20),
        platform_id int(20),
        primary key (id),
        unique (organ_id, cms_id)
    ) ENGINE=InnoDB default charset=gbk;

    create table organ_server (
        id int(20) not null auto_increment,
        cms_id varchar(255),
        organ_id varchar(255),
        path varchar(255),
        server_id varchar(255),
        status int(20),
        primary key (id)
    ) ENGINE=InnoDB default charset=gbk;

    create table organ_status (
        id int(20) not null auto_increment,
        base_notify bit not null,
        base_notify_sign int(20) not null,
        block varchar(255),
        change_time datetime,
        name varchar(255),
        nanosecond int(20) not null,
        organ_id int(20),
        status varchar(255),
        subscribe_event_id int(20),
        platform_id int(20),
        primary key (id),
        unique (organ_id, platform_id)
    ) ENGINE=InnoDB default charset=gbk;

    create table outer_device_alarm (
        id varchar(31) not null,
        cms_id varchar(6),
        device_id varchar(31),
        device_naming varchar(100),
        device_type varchar(50),
        scheme_id varchar(31),
        primary key (id)
    ) ENGINE=InnoDB default charset=gbk;

    create table platform (
        id int(20) not null auto_increment,
        cms_id varchar(255),
        event_server_ip varchar(255),
        event_server_port int(20),
        gb_platform_cms_id varchar(255),
        name varchar(255),
        owner bit not null,
        parent_cms_id varchar(255),
        password varchar(255),
        service_url varchar(255),
        status int(20),
        sync bit not null,
        parent_id int(20),
        primary key (id),
        unique (cms_id)
    ) ENGINE=InnoDB default charset=gbk;

    create table platform_organ (
        id int(20) not null auto_increment,
        name varchar(255),
        organ_id varchar(255),
        path varchar(255),
        platform_id int(20),
        source_id int(20),
        source_type varchar(255),
        parent_id int(20),
        primary key (id),
        unique (source_id, source_type)
    ) ENGINE=InnoDB default charset=gbk;

    create table platform_status (
        id int(20) not null auto_increment,
        description varchar(255),
        error_code varchar(255),
        heart_beat_cycle int(20),
        heartbeat_time datetime,
        online bit not null,
        gb_platform_id int(20),
        primary key (id),
        unique (gb_platform_id)
    ) ENGINE=InnoDB default charset=gbk;

    create table r_user_role (
        role_id int(20) not null,
        user_id int(20) not null,
        primary key (role_id, user_id)
    ) ENGINE=InnoDB default charset=gbk;

    create table role (
        id int(20) not null auto_increment,
        cms_id varchar(255),
        name varchar(20),
        note varchar(255),
        primary key (id)
    ) ENGINE=InnoDB default charset=gbk;

    create table server_status (
        id int(20) not null auto_increment,
        base_notify bit not null,
        change_time datetime,
        location varchar(255),
        name varchar(255),
        nanosecond int(20) not null,
        online varchar(255),
        online_change_time datetime,
        online_nanosecond int(20) not null,
        online_notify bit not null,
        server_id int(20),
        status varchar(255),
        platform_id int(20),
        primary key (id)
    ) ENGINE=InnoDB default charset=gbk;

    create table share (
        id int(20) not null auto_increment,
        item varchar(255),
        platform_cms_id varchar(255),
        resource_cms_id varchar(255),
        resource_id int(20),
        resource_path varchar(255),
        resource_type int(20),
        primary key (id),
        unique (platform_cms_id, resource_id, resource_type)
    ) ENGINE=InnoDB default charset=gbk;

    create table subscribe (
        id int(20) not null auto_increment,
        device_id varchar(255),
        from_platform_id varchar(255),
        period int(20),
        status int(20),
        subscribe_time datetime,
        to_platform_id varchar(255),
        primary key (id)
    ) ENGINE=InnoDB default charset=gbk;

    create table subscribe_event (
        id int(20) not null auto_increment,
        device_id varchar(255),
        expire_date datetime,
        organ_id int(20),
        path varchar(255),
        platform_id int(20),
        subscribe_id varchar(255),
        primary key (id)
    ) ENGINE=InnoDB default charset=gbk;

    create table task_status (
        id int(20) not null auto_increment,
        change_time datetime,
        nanosecond int(20) not null,
        object_id int(20) not null,
        object_type varchar(255),
        platform_id int(20) not null,
        sn varchar(255),
        primary key (id)
    ) ENGINE=InnoDB default charset=gbk;

    create table user (
        id int(20) not null auto_increment,
        change_time datetime,
        cms_id varchar(255),
        logon_name varchar(255),
        name varchar(255),
        naming varchar(255),
        nanosecond int(20) not null,
        organ_id varchar(255),
        password varchar(255),
        sex varchar(255),
        status varchar(255),
        sync bit not null,
        user_id varchar(255),
        primary key (id)
    ) ENGINE=InnoDB default charset=gbk;

    alter table device 
        add index FKB06B1E56AC90B1FB (organ_id), 
        add constraint FKB06B1E56AC90B1FB 
        foreign key (organ_id) 
        references organ (id);

    alter table device 
        add index FKB06B1E5647881B59 (platform_id), 
        add constraint FKB06B1E5647881B59 
        foreign key (platform_id) 
        references platform (id);

    alter table device_status 
        add index FKC65A01FBB9D2DD94 (platform_id), 
        add constraint FKC65A01FBB9D2DD94 
        foreign key (platform_id) 
        references gb_platform (id);

    alter table gb_device 
        add index FKBD96F23ACB9091A0 (organ_id), 
        add constraint FKBD96F23ACB9091A0 
        foreign key (organ_id) 
        references gb_organ (id);

    alter table gb_device 
        add index FKBD96F23AE44F81E0 (original_id), 
        add constraint FKBD96F23AE44F81E0 
        foreign key (original_id) 
        references gb_organ (id);

    alter table gb_device 
        add index FKBD96F23A533BC039 (device_id), 
        add constraint FKBD96F23A533BC039 
        foreign key (device_id) 
        references device (id);

    alter table gb_organ 
        add index FKB429B1ADF880E867 (parent_id), 
        add constraint FKB429B1ADF880E867 
        foreign key (parent_id) 
        references gb_organ (id);

    alter table organ 
        add index FK651921147881B59 (platform_id), 
        add constraint FK651921147881B59 
        foreign key (platform_id) 
        references platform (id);

    alter table organ 
        add index FK6519211D98108C2 (parent_id), 
        add constraint FK6519211D98108C2 
        foreign key (parent_id) 
        references organ (id);

    alter table organ_status 
        add index FK2A57F5E0B9D2DD94 (platform_id), 
        add constraint FK2A57F5E0B9D2DD94 
        foreign key (platform_id) 
        references gb_platform (id);

    alter table platform 
        add index FK6FBD6873784F7BC2 (parent_id), 
        add constraint FK6FBD6873784F7BC2 
        foreign key (parent_id) 
        references platform (id);

    alter table platform_organ 
        add index FKE1E9FB45A3EF8F0F (parent_id), 
        add constraint FKE1E9FB45A3EF8F0F 
        foreign key (parent_id) 
        references platform_organ (id);

    alter table platform_status 
        add index FK62428D3EC323D830 (gb_platform_id), 
        add constraint FK62428D3EC323D830 
        foreign key (gb_platform_id) 
        references gb_platform (id);

    alter table r_user_role 
        add index FK95488DD1B4D8FF9 (role_id), 
        add constraint FK95488DD1B4D8FF9 
        foreign key (role_id) 
        references role (id);

    alter table r_user_role 
        add index FK95488DDC07853D9 (user_id), 
        add constraint FK95488DDC07853D9 
        foreign key (user_id) 
        references user (id);

    alter table server_status 
        add index FK43277C6EB9D2DD94 (platform_id), 
        add constraint FK43277C6EB9D2DD94 
        foreign key (platform_id) 
        references gb_platform (id);
