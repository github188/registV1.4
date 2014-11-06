SET FOREIGN_KEY_CHECKS=0;

DROP DATABASE IF EXISTS  regist;

CREATE DATABASE regist
    CHARACTER SET GBK
    COLLATE GBK_chinese_ci;
USE regist;
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
    )engine=InnoDb default charset=gbk;

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
    )engine=InnoDb default charset=gbk;

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
    )engine=InnoDb default charset=gbk;

    create table outer_device_alarm (
        id varchar(40) not null,
        cms_id varchar(6),
        device_id varchar(31),
        device_naming varchar(100),
        device_type varchar(50),
        scheme_id varchar(31),
        primary key (id)
    )engine=InnoDb default charset=gbk;

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
    )engine=InnoDb default charset=gbk;

    create table r_user_role (
        role_id varchar(50) not null,
        user_id varchar(50) not null,
        primary key (role_id, user_id)
    )engine=InnoDb default charset=gbk;

    create table regist_server (
        id integer not null auto_increment,
        url varchar(255),
        city_server bit not null,
        password varchar(255),
        regist_server_code varchar(255),
        parent_id integer,
        primary key (id)
    )engine=InnoDb default charset=gbk;

    create table role (
        id varchar(30) not null,
        cms_id varchar(255),
        name varchar(20),
        note varchar(255),
        primary key (id),
        unique(cms_id,name)
    )engine=InnoDb default charset=gbk;

    create table share (
        id varchar(255) not null,
        item varchar(255),
        platform_cms_id varchar(255),
        resource_cms_id varchar(255),
        resource_id varchar(255),
        resource_path varchar(255),
        resource_type integer,
        primary key (id)
    )engine=InnoDb default charset=gbk;

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
    )engine=InnoDb default charset=gbk;
    
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
    )engine=InnoDb default charset=gbk;

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
   ## 2011-11-22 ����authorization��device�������,�ӿ��ѯ�ٶ� ##
   alter table authorization modify column granted_id varchar(50);
   alter table authorization modify column resource_id varchar(50);
   alter table authorization add index(cms_id,granted_id,resource_cms_id,resource_type);
   alter table device modify column cms_id char(6);
   alter table device add index(cms_id,owner_id);
   create table static_file (
        user_id varchar(50) not null,
        primary key (user_id)
    );
    
   ## 2011-12-18 PM device����serverId��stdId,platform������access_server,device_server����manufacturer,model,status,stdIdʵ�ֹ��� ##
   alter table device add column server_id varchar(50);
   alter table device add column std_id varchar(50);
   alter table device add column dispatcher_platform_id varchar(50);
   alter table device_server add column manufacturer varchar(255);
   alter table device_server add column model varchar(255);
   alter table device_server add column status varchar(20);
   alter table device_server add column std_id varchar(50);
   alter table device_server add column children_status bit(1);
   alter table platform add column access_server varchar(100);
   
   ## 2011-12-18 PM �����ɳ����� , �豸��ص�dispatcher_platform_id##
   create table dispatcher_platform (
        id varchar(255) not null,
        location varchar(255),
        name varchar(255),
        note varchar(255),
        platform_id varchar(255),
        primary key (id)
    )engine=InnoDb default charset=gbk;
     alter table dispatcher_platform 
        add index FKA00E8B0B47881B59 (platform_id), 
        add constraint FKA00E8B0B47881B59 
        foreign key (platform_id) 
        references platform (id);
        
   ## 2012-03-09 AM ���ӹ���ƽ̨״̬��
    create table platform_status (
        id varchar(255) not null,
        description varchar(255),
        error_code varchar(255),
        heart_beat_cycle integer,
        heartbeat_time datetime,
        online bit,
        platform_id varchar(255),
        primary key (id)
    )engine=InnoDb default charset=gbk;
    alter table platform_status 
        add index FK62428D3E47881B59 (platform_id), 
        add constraint FK62428D3E47881B59 
        foreign key (platform_id) 
        references platform (id);
      
    ## 2012-03-21 AM �����ϼ�ƽ̨�ķ��ͷ�ʽ,send_typeΪterminalʱ,ֻ���Ͷ����豸,
    ## send_typeΪserver��nullʱ,�ȷ���һ���豸,�ٷ��Ͷ����豸.platform��Ϊ�����ϼ�ʱ,
    ## ��Ӧ��child_platform_id ��Ϊ�����¼�ƽ̨��ƽ̨ID.  
    alter table platform add column send_type varchar(10);
    alter table platform add column child_platform_id varchar(31);
    alter table platform add column page_size int;
    
    ##2012-06-15 PM 
    alter table device add sync bit default false;
    alter table device add change_time timestamp;
    alter table device add nanosecond mediumint default 0;
    alter table organ add sync bit default false;
    alter table organ add change_time  timestamp;
    alter table organ add nanosecond mediumint default 0;
    alter table user add sync bit default false;
    alter table user add change_time  timestamp;
    alter table user add status varchar(20);
    alter table user add nanosecond mediumint default 0;
    alter table device_server add sync bit default false;
    alter table device_server add change_time  timestamp;
    alter table device_server add nanosecond mediumint default 0; 
    
    ##2012-06-19 PM ����monitor��,��������̰߳�ȫ����
    create table monitor (
        id integer not null auto_increment,
        cms_id varchar(255),
        monitored varchar(255),
        primary key (id),
        unique (monitored,cms_id)
    )engine=InnoDb default charset=gbk;
  
    
##2012-07-12�ϲ�zyq ��Ƶ������״̬���¼�ƽ̨������
DROP TABLE IF EXISTS device_server_status;
CREATE TABLE device_server_status (
  id varchar(255) NOT NULL,
  online bit(1) DEFAULT NULL,
  status varchar(10) DEFAULT NULL,
  encode varchar(10) DEFAULT NULL,
  record varchar(10) DEFAULT NULL,
  device_time varchar(10) DEFAULT NULL,
  std_id  varchar(200) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=gbk;

## 2012-07-12�ϲ�zyq  Device��ı����豸״̬�ͱ�����λ
alter table device add column alarm_statu varchar(10) DEFAULT NULL;
alter table device add column alarm_reset varchar(10) DEFAULT NULL;

## 2012-07-18ʵ�ֹ�����ּ�ƽ̨������ͨ
create table gb_platform (
        id integer not null auto_increment,
        child_cms_id varchar(255),
        cms_id varchar(255),
        contain bit not null,
        type integer,
        name varchar(255),
        page_size integer,
        password varchar(255),
        send_type varchar(255),
        sip_server varchar(255),
        primary key (id)
    )ENGINE=InnoDB DEFAULT CHARSET=gbk;
delete from platform_status;
alter table platform_status drop foreign key FK62428D3E47881B59;
alter table platform_status drop column platform_id;
alter table platform_status add column  gb_platform_id integer;
alter table platform_status add index FK62428D3EC323D830 (gb_platform_id), add constraint FK62428D3EC323D830 
	foreign key (gb_platform_id)  references gb_platform (id);
alter table platform drop column outer_platform_cms_ids;
alter table platform drop column outer_platform;
alter table platform drop column access_server;
alter table platform drop column send_type;
alter table platform drop column child_platform_id;
alter table platform drop column page_size;

## 2012-07-20 �����std_id����˾��ǿ����������
alter table organ add column std_id varchar(20);
## 2012-08-13 ����platform��gb_platform�Ĺ�����ϵ
alter table platform add column gb_platform_cms_id varchar(20);
update platform set gb_platform_cms_id=id where id in(select cms_id from gb_platform);

## 2012-08-22 ȡ��monitored��Ψһ������
alter table monitor drop key monitored;

## 2012-09-28 ���Ա���ʱ����online�Ķ��岻��ȷ,Ӧ��Ϊvarchar����
alter table device_server_status modify column online varchar(10);

## 2012-10-15 ������Ƶ������naming�ֶ�
alter table device_server add column naming varchar(100);

## 2013-01-06 ����cms_id�䳤,��ص��ֶ�Ҳ�ñ䳤
alter table device modify column id varchar(255);
alter table device modify column server_id varchar(255);
alter table device modify column cms_id varchar(100);
alter table authorization modify column cms_id varchar(100);
alter table authorization modify column resource_id varchar(255);
alter table authorization modify column resource_cms_id varchar(255);
alter table static_file modify column user_id varchar(255);

## 2013-04-10 share������platform_type�ֶΣ���ʾ�豸������Ӧƽ̨�����ͣ�Ŀǰ�ǹ���ƽ̨����ͨƽ̨����
alter table share add column platform_type integer(2) default 0;
update gb_platform set send_type='OST' where send_type='organ';
update gb_platform set send_type='ST' where send_type='server';
update gb_platform set send_type='T' where send_type='terminal';

## 2013-04-17 gb_platform������manufacturer�ֶΣ���ʾ�ԽӵĹ��곧�̱�ʶ YS��ʾһ����H3C��ʾ������FH��ʾ���
alter table gb_platform add column manufacturer varchar(20) default '';

## 2013-04-22 ��¼��Դ�ٷ�����Ϣ���Ա��ڷ�������Ϣ׷�٣���������һ����
    create table redistribution (
        id integer not null auto_increment,
        resource_id varchar(255),
        resource_type integer,
        source_cms_id varchar(255),
        source_path varchar(255),
        std_id varchar(255),
        target_cms_id varchar(255),
        target_path varchar(255),
        target_resource_id varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

## 2013-06-08 ����nanosecondĬ��ֵ
alter table device modify nanosecond mediumint default 0;
alter table organ modify nanosecond mediumint default 0;
alter table device_server modify nanosecond mediumint default 0;

    create table subscribe_event (
        id integer not null auto_increment,
        cms_id varchar(255),
        device_id varchar(255),
        expire_date datetime,
        from_platform_id varchar(255),
        path varchar(255),
        subscribe_id varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;
    
    create table device_status (
        id integer not null auto_increment,
        base_notify bit not null,
        change_time datetime,
        cms_id varchar(255),
        device_id varchar(255),
        name varchar(255),
        nanosecond mediumint default 0,
        online varchar(255),
        online_change_time datetime,
        online_nanosecond  mediumint default 0,
        online_notify bit not null,
        path varchar(255),
        server_id varchar(255),
        status integer,
        std_id varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;
    
## 2013-07-24 subscribe_event�����organ_std_id��ԭʼ���ĵ�device_id���ֿ���
alter table subscribe_event add column organ_std_id varchar(255) not null;

## 2013-11-14 
 alter table gb_platform engine=INNODB;
 alter table organ engine=INNODB;
 alter table platform engine=INNODB;
 alter table device_server engine=INNODB;
 alter table device engine=INNODB;
 drop table device_status;
 alter table gb_platform add column standard_type varchar(255);
 alter table gb_platform add column share_type varchar(255) default 'PART';
 alter table gb_platform add column auto_push bit not null default 0;
 alter table gb_platform add column has_query bit not null default 0;
 create table device_status (
        id integer not null auto_increment,
        base_notify bit not null,
        change_time datetime,
        nanosecond integer not null,
        online varchar(255),
        online_change_time datetime,
        online_nanosecond integer not null,
        online_notify bit not null,
        status integer,
        device_id varchar(255),
        platform_id integer,
        primary key (id)
    ) ENGINE=InnoDB;
    
     create table organ_status (
        id integer not null auto_increment,
        base_notify bit not null,
        change_time datetime,
        nanosecond integer not null,
        status integer,
        organ_id varchar(255),
        platform_id integer,
        primary key (id)
    ) ENGINE=InnoDB;
    
    create table server_status (
        id integer not null auto_increment,
        base_notify bit not null,
        change_time datetime,
        nanosecond integer not null,
        online varchar(255),
        online_change_time datetime,
        online_nanosecond integer not null,
        online_notify bit not null,
        status integer,
        platform_id integer,
        server_id varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;
    
    alter table device_status 
        add index FKC65A01FB533BC039 (device_id), 
        add constraint FKC65A01FB533BC039 
        foreign key (device_id) 
        references device (id);

    alter table device_status 
        add index FKC65A01FBB9D2DD94 (platform_id), 
        add constraint FKC65A01FBB9D2DD94 
        foreign key (platform_id) 
        references gb_platform (id);
        
     alter table organ_status 
        add index FK2A57F5E0AC90B1FB (organ_id), 
        add constraint FK2A57F5E0AC90B1FB 
        foreign key (organ_id) 
        references organ (id);

    alter table organ_status 
        add index FK2A57F5E0B9D2DD94 (platform_id), 
        add constraint FK2A57F5E0B9D2DD94 
        foreign key (platform_id) 
        references gb_platform (id);
    alter table server_status 
        add index FK43277C6E27BF03CF (server_id), 
        add constraint FK43277C6E27BF03CF 
        foreign key (server_id) 
        references device_server (id);

    alter table server_status 
        add index FK43277C6EB9D2DD94 (platform_id), 
        add constraint FK43277C6EB9D2DD94 
        foreign key (platform_id) 
        references gb_platform (id);
        
    create table task_status (
        id integer not null auto_increment,
        change_time datetime,
        nanosecond integer not null,
        object_id integer not null,
        object_type varchar(255),
        platform_id integer not null,
        sn varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;
    
 ## 2013-11-25 �½�Ҫ�����ӵ��ֶ�
    alter table device add column position_type integer;
    alter table device add column ptz_type integer;
    alter table device add column supply_light_type integer;
    alter table device add column use_type integer;
    alter table device add column dircetion_type integer;
    alter table device add column room_type integer;
    
 ##2013-11-25 �½�Ҫ�����ӵ��ֶΣ�����˾û��һ���Ը�ȫ��Ҫ���ӵ��ֶΣ�������Ҫ���ӵĹ�������������1/7������1/2,���ߣ�
   alter table device add column block varchar(255);
   alter table device_server add column block varchar(255);
   
 ##2013-11-29 �½�Ҫ�����ӵ��ֶ�
   alter table device drop column block;
   alter table device_server drop column block;
   alter table organ add column block varchar(255);
   
 ##2013-12-04 device����online�У���Ҫ���ڱ�ʾ������豸����״̬
   alter table device add column online varchar(10) default 'OFF';
   alter table device_server add column online varchar(10) default 'OFF';
   ##���ڼ�¼�ϼ��ڱ���������Ϣ
   create table subscribe (
        id integer not null auto_increment,
        device_id varchar(255),
        from_platform_id varchar(255),
        status integer,
        subscribe_time datetime,
        to_platform_id varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;
    
##2014-01-28 ȥ��monitor,dispatcher_platform���ñ����Ӹ���Դ״̬����Ҫ��ע������ֶΣ��ſ�״̬����Դ��ǿ�Թ���
drop table monitor;
drop table dispatcher_platform;
alter table platform add column sync bit default true;

alter table device_status drop foreign key FKC65A01FB533BC039;
alter table server_status drop foreign key FK43277C6E27BF03CF;
alter table organ_status drop foreign key FK2A57F5E0AC90B1FB;

alter table device_status add column name varchar(255);
alter table device_status add column path varchar(1000);

alter table server_status add column name varchar(255);
alter table server_status add column location varchar(255);

alter table organ_status add column path varchar(1000);
alter table organ_status add column name varchar(255);
alter table organ_status add column block varchar(255);

##2014-02-25 subscribe���Ӷ��ĳ�ʱ��period
alter table subscribe add column period integer;

##2014-03-15 �����û�Ȩ�ޱ�
update authorization  au inner join organ o on (au.resource_type=1 and au.resource_id=o.id) set au.resource_path=o.path;
update authorization  au inner join device d on (au.resource_type in(2,3,4) and au.resource_id=d.id) inner join organ o on(o.id=concat(d.owner_id,'_',d.cms_id))set au.resource_path=o.path;

update device set std_id=concat(cms_id,'00001310',substr(device_id,-6)) where type='VIC' and std_id is null;
update device set std_id=concat(cms_id,'00001320',substr(device_id,-6)) where type='IPVIC' and std_id is null;
update device set std_id=concat(cms_id,'00001340',substr(device_id,-6)) where type='AIC' and std_id is null;
update organ set std_id=concat(cms_id,'00002000',substr(organ_id,-6)) where std_id is null;
update device_server set std_id=concat(cms_id,'00001110',substr(id,26,6)) where type='VIS' and std_id is null;

##2014-03-22 ����organ_server�����ڱ�ʾorgan��device_server�Ĺ�����ϵ��֮ǰ��ͨ��device����ȷ���˹�ϵ
alter table subscribe_event add column organ_id varchar(100);
create table organ_server (
        id integer not null auto_increment,
        organ_id varchar(255),
        path varchar(255),
        server_id varchar(255),
        cms_id varchar(255),
        unique index(organ_id, server_id),
        primary key (id)
    ) ENGINE=InnoDB;
insert into organ_server (organ_id,server_id,cms_id,path) select t.o_id,t.s_id,t.o_cms_id,t.o_path from (select distinct(concat(o.id,s.id)),o.id as o_id,s.id as s_id,o.cms_id as o_cms_id,o.path as o_path from device d inner join organ o on(o.id=concat(d.owner_id,'_',d.cms_id)) inner join device_server s on(s.id=d.server_id))t;

##2014-03-25
alter table device_status add column location varchar(255);

##2014-03-27
alter table device add column gps_x varchar(255);
alter table device add column gps_y varchar(255);
alter table device add column gps_z varchar(255);

##2014-03-28
alter table organ_server add column status integer default 3;
alter table device drop column gps_x;
alter table device drop column gps_y;

##2014-03-31
alter table device modify column longitude varchar(255);
alter table device modify column latitude varchar(255);
alter table subscribe_event modify column path varchar(1000);

##2014-04-05
alter table platform add column plan_device_nums int default 10000;

##2014-04-16
 create table online (
        server_id varchar(255) not null,
        change_time datetime,
        nanosecond integer not null,
        primary key (server_id)
    ) ENGINE=InnoDB;
    
##2014-04-18
alter table gb_platform add column protocol varchar(10) default 'udp';

##2014-05-08 ������ǩ����ʵ�ֿ���ģ������
 create table label (id integer not null auto_increment,device_id varchar(255) not null,type varchar(20),title varchar(255),primary key (id)) ENGINE=InnoDB;

##2014-06-01 typeֵΪorgan,server,device,�ֱ��ʾ���ĵ�����Ϊ�������ģ����������ģ��ն�����ͷ����
alter table subscribe_event add column type varchar(10);

##2014-07-17 device_server�����inner_device�ֶΣ���ʾ�Ƿ��ǻ����ڲ����豸������ȷ���Ƿ���Ҫ��ȡ���豸������״̬��
##��Ϊͨ���ּ��ӿ������Ĺ����¼�ƽ̨����gb_platform���޷�ȷ�����豸�Ƿ��ǹ����¼�ƽ̨��
alter table device_server add column inner_device bit(1);
