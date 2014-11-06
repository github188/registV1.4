package com.megaeyes.regist.controllers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.List;

import net.hight.performance.annotation.Param;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.mega.jdom.Document;
import com.mega.jdom.Element;
import com.mega.jdom.input.SAXBuilder;
import com.megaeyes.regist.enump.DeviceIdType;
import com.megaeyes.utils.Invocation;

@Component("analysis")
public class AnalysisController {
	@Value("${recordSendLog.dir}")
	private String recordSendLogDir;

	@SuppressWarnings("unchecked")
	public String analysisSendResult(Invocation inv,
			@Param("platformCmsId") String platformCmsId, @Param("sn") String sn) {
		InputStream in = null;
		BufferedReader readerIn = null;
		try {
			in = new BufferedInputStream(new FileInputStream(new File(
					recordSendLogDir + "/send" + platformCmsId + "_" + sn
							+ ".txt")));
			String result = IOUtils.toString(in, "GBK");
			in.close();
			StringBuilder sb = new StringBuilder(result.replaceAll(
					"\\<\\?xml version=\"1.0\" encoding=\"GBK\"\\?\\>", ""));
			sb.insert(0, "<?xml version=\"1.0\" encoding=\"GBK\"?><Result>");
			sb.append("</Result>");
			SAXBuilder builder = new SAXBuilder();
			readerIn = new BufferedReader(new StringReader(sb.toString()));
			Document doc = builder.build(readerIn);
			readerIn.close();
			Element root = doc.getRootElement();
			List<Element> children = root.getChildren();
			int sum = 0;
			int totalOrgan = 0;
			int totalDVR = 0;
			int totalVIS = 0;
			int totalAIC = 0;
			int totalVIC = 0;
			int totalIPVIC = 0;
			int other = 0;
			for (Element child : children) {
				if (sum == 0) {
					sum = Integer.parseInt(child.getChildText("SumNum"));
				}

				List<Element> items = child.getChild("DeviceList").getChildren(
						"Item");
				for (Element item : items) {
					if (DeviceIdType.isVIC(item.getChildText("DeviceID"))) {
						totalVIC++;
					} else if (DeviceIdType.isIPVIC(item
							.getChildText("DeviceID"))) {
						totalIPVIC++;
					} else if (DeviceIdType
							.isAIC(item.getChildText("DeviceID"))) {
						totalAIC++;
					} else if (DeviceIdType
							.isDVR(item.getChildText("DeviceID"))) {
						totalDVR++;
					} else if (DeviceIdType.isOrgan(item
							.getChildText("DeviceID"))) {
						totalOrgan++;
					} else if (DeviceIdType
							.isVIS(item.getChildText("DeviceID"))) {
						totalVIS++;
					} else {
						System.out.println(item.getChildText("DeviceID"));
						other++;
					}
				}

			}
			inv.addModel("totalOrgan", totalOrgan);
			inv.addModel("totalDVR", totalDVR);
			inv.addModel("totalVIS", totalVIS);
			inv.addModel("totalAIC", totalAIC);
			inv.addModel("totalVIC", totalVIC);
			inv.addModel("totalIPVIC", totalIPVIC);
			inv.addModel("sum", sum);
			inv.addModel("other", other);
			inv.addModel("realSum", totalOrgan + totalDVR + totalVIS + totalAIC
					+ totalVIC + totalIPVIC + other);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				if(readerIn != null){
					readerIn.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "/GB/analysis-send-result";
	}

}
