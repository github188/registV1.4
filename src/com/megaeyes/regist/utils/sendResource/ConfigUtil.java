package com.megaeyes.regist.utils.sendResource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration
public class ConfigUtil {

	public static String PARENT_REGIST_URL;

	public static String IS_CITY_REGIST;

	public static String LOCAL_URL;

	public static String OWNER_PLATFORM_URL;

	public static String OWNER_PLATFORM_CMS_ID;

	public static String STATIC_PATH;

	public static String SEND_TO_REGIST_PAGESIZE;

	public static String GB_CMSID_EXPRESSION;
	


	@Autowired(required = true)
	public void setPARENT_REGIST_URL(
			@Value("#{config[parent_regist_url]}") String vlaue) {
		PARENT_REGIST_URL = vlaue;
	}

	@Autowired(required = true)
	public void setIS_CITY_REGIST(
			@Value("#{config[is_city_regist]}") String vlaue) {
		IS_CITY_REGIST = vlaue;
	}

	@Autowired(required = true)
	public void setLOCAL_URL(@Value("#{config[local_url]}") String vlaue) {
		LOCAL_URL = vlaue;
	}

	@Autowired(required = true)
	public void setOWNER_PLATFORM_URL(
			@Value("#{config[owner_platform_url]}") String vlaue) {
		OWNER_PLATFORM_URL = vlaue;
	}

	@Autowired(required = true)
	public void setOWNER_PLATFORM_CMS_ID(
			@Value("#{config[owner_platform_cmsId]}") String vlaue) {
		OWNER_PLATFORM_CMS_ID = vlaue;
	}

	@Autowired(required = true)
	public void setSTATIC_PATH(@Value("#{config[static_path]}") String vlaue) {
		STATIC_PATH = vlaue;
	}

	@Autowired(required = true)
	public void setSEND_TO_REGIST_PAGESIZE(
			@Value("#{config[send_to_regist_pageSize]}") String vlaue) {
		SEND_TO_REGIST_PAGESIZE = vlaue;
	}
	
	@Autowired(required = true)
	public void setGB_CMSID_EXPRESSION(
			@Value("#{config[gb_cmsid_expression]}") String value) {
		GB_CMSID_EXPRESSION = value;
	}
}
