delimiter //
drop PROCEDURE if exists general_std_id;
//
charset GBK //
CREATE  PROCEDURE general_std_id()
begin
	DECLARE fetchCmsIdOk boolean; 
	DECLARE cmsId varchar(20);
	DECLARE fetchCmsIdCursor CURSOR FOR select cms_id from device group by cms_id having(count(std_id)=0);
	DECLARE fetchServerCursor CURSOR FOR select cms_id from device_server group by cms_id having(count(std_id)=0);
	DECLARE fetchOrganCursor CURSOR FOR select cms_id from organ group by cms_id having(count(std_id)=0);
	DECLARE CONTINUE HANDLER FOR NOT FOUND set fetchCmsIdOk = true;
	SET fetchCmsIdOk = false;
	
	OPEN fetchCmsIdCursor;
		fetchCmsIdLoop:LOOP
		FETCH fetchCmsIdCursor INTO cmsId;
		IF fetchCmsIdOk THEN
			LEAVE fetchCmsIdLoop;
		ELSE
			drop table if exists temp;
			create table temp like device;
			insert into temp select * from device where cms_id=cmsId;
			alter table temp add myid int not null auto_increment unique;
			update temp set std_id=concat(cms_id,'00001310',repeat('0',6-length(myid)),myid) where type='VIC';
			update temp set std_id=concat(cms_id,'00001320',repeat('0',6-length(myid)),myid) where type='IPVIC';
			update temp set std_id=concat(cms_id,'00001340',repeat('0',6-length(myid)),myid) where type='AIC';
			update temp t1 inner join temp t2 on(t1.device_id=t2.device_id) set t2.std_id=t1.std_id;
			alter table temp drop column myid;
			delete from device where cms_id=cmsId;
			insert into device select * from temp;
			drop table temp;
		END IF;
		END LOOP;
	CLOSE fetchCmsIdCursor;
	SET fetchCmsIdOk = false;
	OPEN fetchServerCursor;
		fetchCmsIdLoop:LOOP
		FETCH fetchServerCursor INTO cmsId;
		IF fetchCmsIdOk THEN
			LEAVE fetchCmsIdLoop;
		ELSE
			create table temp like device_server;
			insert into temp select * from device_server where cms_id=cmsId;
			alter table temp add myid int not null auto_increment unique;
			update temp set std_id=concat(cms_id,'00001110',repeat('0',6-length(myid)),myid),type='VIS';
			alter table temp drop column myid;
			delete from device_server where cms_id=cmsId;
			insert into device_server select * from temp;
			drop table temp;
		END IF;
		END LOOP;
	CLOSE fetchServerCursor;
	SET fetchCmsIdOk = false;
	OPEN fetchOrganCursor;
		fetchCmsIdLoop:LOOP
		FETCH fetchOrganCursor INTO cmsId;
		IF fetchCmsIdOk THEN
			LEAVE fetchCmsIdLoop;
		ELSE
			create table temp like organ;
			insert into temp select * from organ where cms_id=cmsId;
			alter table temp add myid int not null auto_increment unique;
			update temp set std_id=concat(cms_id,'00002000',repeat('0',6-length(myid)),myid);
			alter table temp drop column myid;
			delete from organ where cms_id=cmsId;
			insert into organ select * from temp;
			drop table temp;
		END IF;
		END LOOP;
	CLOSE fetchOrganCursor;
end 
//
delimiter ;


