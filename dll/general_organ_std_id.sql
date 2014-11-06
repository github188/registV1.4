delimiter //
drop PROCEDURE if exists general_organ_std_id;
//
charset GBK //
CREATE  PROCEDURE general_organ_std_id()
begin
	DECLARE fetchCmsIdOk boolean; 
	DECLARE cmsId varchar(20);
	DECLARE fetchOrganCursor CURSOR FOR select cms_id from organ where length(cms_id)=6;
	DECLARE CONTINUE HANDLER FOR NOT FOUND set fetchCmsIdOk = true;
	SET fetchCmsIdOk = false;
	OPEN fetchOrganCursor;
		fetchCmsIdLoop:LOOP
		FETCH fetchOrganCursor INTO cmsId;
		IF fetchCmsIdOk THEN
			LEAVE fetchCmsIdLoop;
		ELSE
			create table temp like organ;
			insert into temp select child.* from organ child inner join organ parent on(child.parent_id = parent.id and parent.parent_id is null and parent.cms_id=cmsId);
			alter table temp add myid int not null auto_increment unique;
			update temp set std_id=concat(cms_id,repeat('0',2-length(myid)),myid);
			alter table temp drop column myid;
			update organ child inner join temp t on(t.id=child.id) set child.std_id=t.std_id;
			drop table temp;
		END IF;
		END LOOP;
	CLOSE fetchOrganCursor;
end 
//
delimiter ;



delimiter //
drop PROCEDURE if exists general_child_std_id;
//
charset GBK //
CREATE  PROCEDURE general_child_std_id()
begin
	DECLARE fetchCmsIdOk boolean; 
	DECLARE parent_id varchar(50);
	DECLARE parent_std_id varchar(20);
	DECLARE fetchOrganCursor CURSOR FOR select parent.id,parent.std_id  from organ parent where parent.parent_id is null  and length(parent.cms_id)=6 and substring(parent.cms_id,5,2)='00' and substring(parent.cms_id,3,2)!='00';
	DECLARE CONTINUE HANDLER FOR NOT FOUND set fetchCmsIdOk = true;
	SET fetchCmsIdOk = false;
	OPEN fetchOrganCursor;
		fetchCmsIdLoop:LOOP
		FETCH fetchOrganCursor INTO parent_id,parent_std_id;
		IF fetchCmsIdOk THEN
			LEAVE fetchCmsIdLoop;
		ELSE
			create table temp like organ;
			insert into temp select child.* from organ child where child.parent_id=parent_id;
			alter table temp add myid int not null auto_increment unique;
			update temp set std_id=concat(parent_std_id,repeat('0',2-length(myid)),myid);
			alter table temp drop column myid;
			update organ child inner join temp t on(t.id=child.id) set child.std_id=t.std_id;
			drop table temp;
		END IF;
		END LOOP;
	CLOSE fetchOrganCursor;
end 
//
delimiter ;


