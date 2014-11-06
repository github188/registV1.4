package com.megaeyes.regist.controllers;

import java.math.BigInteger;
import java.util.List;

import org.springframework.stereotype.Component;

import net.hight.performance.annotation.Param;

import com.megaeyes.regist.bean.PlatformResourceStatisticBean;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.other.Status;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

@Component("statistic")
public class StatisticController {
	public String statisticResourceByCmsId(Invocation inv,
			@Param("cmsId") String cmsId,@Param("type")String type) {
		Platform platform = Ar.of(Platform.class).get(cmsId);
		List<Object[]> deviceStatistic = Ar
				.sql(
						"select count(*), type from device where cms_id=? and status!=? group by type",
						cmsId, Status.delete.toString());
		PlatformResourceStatisticBean bean = new PlatformResourceStatisticBean();
		for (Object[] row : deviceStatistic) {
			if (row[1].equals("VIC")) {
				bean.setVics((BigInteger) row[0]);
			} else if (row[1].equals("AIC")) {
				bean.setAics((BigInteger) row[0]);
			} else if (row[1].equals("IPVIC")) {
				bean.setIpvics((BigInteger) row[0]);
			}
		}
		List organs = Ar.sql(
				"select count(*) from organ where cms_id=? and status!=?",
				cmsId, Status.delete.toString());
		
		List servers = Ar.sql("select count(*) from device_server where cms_id=? and status!=?", cmsId,Status.delete.toString()); 
		bean.setOrgans((BigInteger) organs.get(0));
		bean.setServers((BigInteger) servers.get(0));
		inv.addModel("statisticBean", bean);
		inv.addModel("platformName",platform.getName());
		inv.addModel("platformId",platform.getId());
		return "platform-resource-statistic";
	}
}
