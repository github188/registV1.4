package com.megaeyes.regist.webservice;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.mega.jdom.Document;
import com.mega.jdom.Element;
import com.mega.jdom.input.SAXBuilder;
import com.megaeyes.regist.bean.Require;
import com.megaeyes.regist.bean.Resource;
import com.megaeyes.regist.dao.RegisterDao;
import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.GbPlatform;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.regist.domain.Share;
import com.megaeyes.regist.utils.GbPlatformType;
import com.megaeyes.regist.utils.HttpclientUtils;
import com.megaeyes.regist.utils.Page;
import com.megaeyes.utils.Ar;
import com.megaeyes.utils.Invocation;

public class CityRegistUtil implements Regist {
	private RegisterDao registerDao;

	public CityRegistUtil(RegisterDao registerDao) {
		this.registerDao = registerDao;
	}

	/**
	 * 如果本注册服务器不是顶级主册服务器,则继续向上级注册服务器注册
	 * 
	 * @param cityRegistPlatform
	 * @param platform
	 * @param status
	 */
	@Override
	public void parentPlatform() {
		Platform pltform = Ar.of(Platform.class).one(
				"from Platform where sync is false");
		if (pltform != null) {
			registerDao.platformRegist();
		}
	}

	@Override
	public void parentRegistDevice(String cmsId) {
		registerDao.deviceRegist(cmsId);
	}

	@Override
	public void parentRegistOrgan(String cmsId) {
		registerDao.organRegist(cmsId);
	}

	@Override
	public void parentRegistVIS(String cmsId) {
		registerDao.visRegist(cmsId);
	}

	@Override
	public String shareResource(Require r) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("r.platformIds", r.getPlatformIds());
		map.put("r.vicIds", r.getVicIds());
		map.put("r.ipvicIds", r.getIpvicIds());
		map.put("r.aicIds", r.getAicIds());
		map.put("r.organIds", r.getOrganIds());
		map.put("r.cmsId", r.getCmsId());
		HttpPost parentRegistPost = HttpclientUtils.getParenRegistHttpPost(map,
				"/share/shareResource");
		HttpClient httpclient = new DefaultHttpClient();
		try {
			HttpResponse response = httpclient.execute(parentRegistPost);
			return "" + response.getStatusLine().getStatusCode();
		} catch (Exception e) {

		}
		return "errro";
	}

	@Override
	public String getPlatforms(Invocation inv, String cmsId, String first,
			String name) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("r.cmsId", cmsId);
		map.put("r.first", "false");
		map.put("r.name", name);
		HttpPost parentRegistGet = HttpclientUtils.getParenRegistHttpPost(map,
				"/share/getPlatforms");
		try {
			HttpResponse response = HttpclientUtils.getResponse(map,
					parentRegistGet);
			List<Platform> platforms = new ArrayList<Platform>();
			List<String> cmsIds = Ar.of(GbPlatform.class).find(
					"select cmsId from GbPlatform where type=?",
					GbPlatformType.CHILD);
			paserPlatforms(response.getEntity(), platforms,cmsIds);
			inv.addModel("platforms", platforms);
		} catch (Exception e) {
			e.printStackTrace();
			// 取本注册服务器的所有平台
			registerDao.getPlatforms(inv, cmsId, name);
		}
		if ("true".equals(first)) {
			return PlatformUtils.getSharePath("platform-list");
		} else {
			return PlatformUtils.getSharePath("share-platform-xml");
		}
	}

	@SuppressWarnings("unchecked")
	private void paserPlatforms(HttpEntity entity, List<Platform> platforms,List<String> gbCmsIds) {
		InputStream in = null;
		try {
			SAXBuilder builder = new SAXBuilder();
			in = entity.getContent();
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			List<Element> children = root.getChildren();
			for (Element el : children) {
				if (!ownerCmsId.equals(el.getAttributeValue("id")) && !gbCmsIds.contains(el.getAttributeValue("id"))) {
					Platform platform = new Platform();
					platform.setCmsId(el.getAttributeValue("id"));
					platform.setName(el.getAttributeValue("name"));
					platform.setParentCmsId(el.getAttributeValue("parent"));
					platform.setEventServerIp(el
							.getAttributeValue("eventServerIp"));
					if (StringUtils.isNotBlank(el
							.getAttributeValue("eventServerPort"))) {
						platform.setEventServerPort(Integer.parseInt(el
								.getAttributeValue("eventServerPort")));
					}
					platforms.add(platform);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public String getSharePlatforms(Invocation inv, String obtainCmsId,
			String first) {
		List<Platform> platforms = registerDao.getSharePlatforms(inv,
				obtainCmsId);
		Map<String, String> map = new HashMap<String, String>();
		map.put("r.obtainCmsId", obtainCmsId);
		map.put("r.first", "false");
		HttpGet parentRegistGet = HttpclientUtils.getParenRegistHttpGet(map,
				"/obtain/getSharePlatforms");
		try {
			HttpResponse response = HttpclientUtils.getResponse(map,
					parentRegistGet);
			List<String> cmsIds = Ar.of(GbPlatform.class).find(
					"select cmsId from GbPlatform where type=?",
					GbPlatformType.CHILD);
			paserPlatforms(response.getEntity(), platforms,cmsIds);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if ("true".equals(first)) {
			inv.getRequest().getSession().setAttribute("platforms", platforms);
			return PlatformUtils.getObtainPath("share-platform");
		} else {
			inv.addModel("platforms", platforms);
			return PlatformUtils.getObtainPath("share-platform-xml");
		}
	}


	@SuppressWarnings("unchecked")
	private void paserResource(HttpEntity entity, Invocation inv, Page page) {
		InputStream in = null;
		try {
			List<Resource> resources = new ArrayList<Resource>();
			SAXBuilder builder = new SAXBuilder();
			in = entity.getContent();
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			List<Element> children = root.getChildren("resource");
			Map<String, String> permissionMap = new HashMap<String, String>();
			for (Element el : children) {
				Resource resource = getResource(el);
				resources.add(resource);
				permissionMap.put(resource.getId(), resource.getPermission());
			}
			inv.addModel("resources", resources);
			inv.addModel("permissionMap", permissionMap);
			Element pageEL = root.getChild("page");
			page.setRecordCount(Integer.parseInt(pageEL
					.getAttributeValue("recordCount")));
			inv.addModel("page", page);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void paserShareResource(HttpEntity entity, Invocation inv, Page page) {
		InputStream in = null;
		try {
			List<Resource> resources = new ArrayList<Resource>();
			SAXBuilder builder = new SAXBuilder();
			in = entity.getContent();
			Document doc = builder.build(in);
			Element root = doc.getRootElement();
			List<Element> children = root.getChildren("resource");
			Map<String, String> permissionMap = new HashMap<String, String>();
			Map<String, String> itemMap = new HashMap<String, String>();
			for (Element el : children) {
				Resource resource = getResource(el);
				resources.add(resource);
				permissionMap.put(resource.getId(), resource.getPermission());
				itemMap.put(resource.getId(), resource.getItem());
			}
			inv.addModel("resources", resources);
			inv.addModel("permissionMap", permissionMap);
			inv.addModel("itemMap", itemMap);
			Element pageEL = root.getChild("page");
			page.setRecordCount(Integer.parseInt(pageEL
					.getAttributeValue("recordCount")));
			inv.addModel("page", page);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Resource getResource(Element el) {
		Resource resource = new Resource();
		resource.setType(el.getAttributeValue("resourceType"));
		resource.setId(el.getAttributeValue("id"));
		resource.setName(el.getAttributeValue("name"));
		resource.setParentOrgan(el.getAttributeValue("parentOrgan"));
		resource.setCmsId(el.getAttributeValue("cmsId"));
		resource.setPermission(el.getAttributeValue("permission"));
		resource.setItem(el.getAttributeValue("item"));
		resource.setCmsName(el.getAttributeValue("cmsName"));
		resource.setOrganPath(el.getAttributeValue("organPath"));
		return resource;
	}
	

	@Override
	public String getOuterPlatformByCmsId(Invocation inv, String platformCmsId,
			String cmsId) {
		if (PlatformUtils.isSameRegist(platformCmsId, cmsId)) {
			return registerDao.getOuterPlatformByCmsId(platformCmsId, cmsId);
		} else {
			Map<String, String> map = new HashMap<String, String>();
			map.put("platformCmsId", platformCmsId);
			map.put("cmsId", cmsId);
			HttpPost parentRegistPost = HttpclientUtils.getParenRegistHttpPost(
					map, "/share/getOuterPlatformByCmsId");
			try {
				HttpEntity entity = HttpclientUtils.getResponse(map,
						parentRegistPost).getEntity();
				if (entity != null) {
					String resp = EntityUtils.toString(entity);
					return resp;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String getEventServerMsg(Invocation inv, String naming, String type) {
		String original = inv.getRequest().getParameter("original");
		List<Platform> platforms = new ArrayList<Platform>();
		platforms = registerDao.getEventServerMsg(platforms, naming, type);
		Map<String, String> map = new HashMap<String, String>();
		map.put("naming", naming);
		map.put("original", "false");
		map.put("type", type);
		HttpPost parentRegistPost = HttpclientUtils.getParenRegistHttpPost(map,
				"/home/getEventServerMsg");
		try {
			List<String> cmsIds = Ar.of(GbPlatform.class).find(
					"select cmsId from GbPlatform where type=?",
					GbPlatformType.CHILD);
			paserPlatforms(HttpclientUtils.getResponse(map, parentRegistPost)
					.getEntity(), platforms,cmsIds);
		} catch (Exception e) {
			e.printStackTrace();
		}
		inv.addModel("platforms", platforms);
		if (original != null) {
			return PlatformUtils.getSharePath("share-platform-xml");
		} else {
			return "event-server";
		}
	}

	@Override
	public String getPlatformUrlByCmsId(Invocation inv, String cmsId) {
		Platform platform = Ar.of(Platform.class).get(cmsId);
		String url = "";
		if (platform != null) {
			url = platform.getServiceUrl().replaceFirst("/platform_grade", "");
			url = url.replaceFirst("/share", "");
			return url;
		} else {
			Map<String, String> map = new HashMap<String, String>();
			map.put("cmsId", cmsId);
			HttpPost parentRegistPost = HttpclientUtils.getParenRegistHttpPost(
					map, "/home/getPlatformUrlByCmsId");
			try {
				HttpEntity entity = HttpclientUtils.getResponse(map,
						parentRegistPost).getEntity();
				if (entity != null) {
					return EntityUtils.toString(entity);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return url;
	}

	@Override
	public String getDeviceInfoByNaming(Invocation inv, String naming) {
		Device device = Ar.of(Device.class).one("naming", naming);
		if (device != null) {
			Platform platform = Ar.of(Platform.class).get(device.getCmsId());
			String url = "";
			if (platform != null && platform.getServiceUrl() != null) {
				url = platform.getServiceUrl();
				String deviceInfo = registerDao.getDeviceInfoByUrl(url,
						device.getCmsId(), device);
				if (StringUtils.isBlank(deviceInfo)) {
					return registerDao.getDeviceInfoByNaming(inv, naming);
				}
				return "@" + deviceInfo;
			} else if (platform != null && platform.getServiceUrl() == null) {
				// 国标的下级平台设备
				if (StringUtils.isBlank(url)) {
					return registerDao.getDeviceInfoByNaming(inv, naming);
				}
			}
		} else {
			Map<String, String> map = new HashMap<String, String>();
			map.put("naming", naming);
			HttpPost parentRegistPost = HttpclientUtils.getParenRegistHttpPost(
					map, "/home/getDeviceInfoByNaming");
			try {
				HttpEntity entity = HttpclientUtils.getResponse(map,
						parentRegistPost).getEntity();
				if (entity != null) {
					return "@" + EntityUtils.toString(entity);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return "@";
	}

	@Override
	public void outerDeviceRegist(String platformId, String accessIP,
			String parentCmsId, String resource) {
		Map<String, String> map = new HashMap<String, String>();
		map.put("PlatformID", platformId);
		map.put("AccessIP", accessIP);
		map.put("ParentCmsId", parentCmsId);
		map.put("resource", resource);
		HttpPost parentRegistPost = HttpclientUtils.getParenRegistHttpPost(map,
				"/outerDevice/index");
		try {
			HttpResponse response = HttpclientUtils.getResponse(map,
					parentRegistPost);
			System.out.println(EntityUtils.toString(response.getEntity()));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String hasShareResource(String cmsId) {
		Share share = Ar.of(Share.class).one("platformCmsId", cmsId);
		if (share != null) {
			return "yes";
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("cmsId", cmsId);
		HttpPost parentRegistPost = HttpclientUtils.getParenRegistHttpPost(map,
				"/platform/hasShareResource");
		try {
			HttpResponse response = HttpclientUtils.getResponse(map,
					parentRegistPost);
			return EntityUtils.toString(response.getEntity());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "no";
	}
	
	@Override
	public void parentRegistByDeviceId(Integer id) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void parentRegistByServerId(Integer id) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void parentRegistByOrganId(Integer id) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public String getDevicesByNaming(Invocation inv, List<Device> devices,
			String deviceNaming, String original) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getSharePlatformsByCmsIds(Invocation inv, String shareCmsIds,
			String original) {
		// TODO Auto-generated method stub
		return null;
	}

}
