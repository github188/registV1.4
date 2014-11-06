package com.megaeyes.regist.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

import com.megaeyes.regist.utils.sendResource.ConfigUtil;

public class HttpclientUtils {
	private static String OWNER_URL = ConfigUtil.OWNER_PLATFORM_URL;
	private static String PARENT_REGIST_URL =ConfigUtil.PARENT_REGIST_URL.replace("http://", "")
			.replace("/services", "");
	private static HttpParams httpParams;
	private static ThreadSafeClientConnManager cm;
	static{
		httpParams = new BasicHttpParams(); 
		HttpConnectionParams.setConnectionTimeout(httpParams, 100000);
		HttpConnectionParams.setSoTimeout(httpParams, 100000); 
		HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", 80,PlainSocketFactory.getSocketFactory()));
		schemeRegistry.register(new Scheme("https",443, PlainSocketFactory.getSocketFactory())); 
		cm = new ThreadSafeClientConnManager(schemeRegistry);
		cm.setMaxTotal(200);
		cm.setDefaultMaxPerRoute(10);
	}
	
	public static HttpClient getClient(){
			HttpClient client = new DefaultHttpClient(cm, httpParams); 
			client.getParams().setParameter("http.conn-manager.timeout", 10000L); 
			client.getParams().setParameter("http.protocol.wait-for-continue", 10000L);
		return client;
	}
	public static HttpResponse getResponse(Map<String,String> map,HttpRequestBase req){
		try{
			return getClient().execute(req);
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static void execute(Map<String,String> map,HttpRequestBase req){
		HttpResponse response = null;
		try{
			response = getClient().execute(req);
		}catch(Exception e ){
			throw new RuntimeException(e);
		}finally{
			try{
				if(response != null){
					InputStream in =  response.getEntity().getContent();
					in.close();
				}
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	}

	public static HttpGet getHttpGet(Map<String, String> map, String resource) {
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			if (map != null) {
				for (String key : map.keySet()) {
					qparams.add(new BasicNameValuePair(key, map.get(key)));
				}
			}
			URI uri = URIUtils.createURI("http", OWNER_URL, -1, resource,
					URLEncodedUtils.format(qparams, "UTF-8"), null);
			HttpGet httpget = new HttpGet(uri);
			return httpget;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpPost getHttpPost(Map<String, String> map, String resource) {
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			if (map != null) {
				for (String key : map.keySet()) {
					qparams.add(new BasicNameValuePair(key, map.get(key)));
				}
			}
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(qparams,
					"UTF-8");
			HttpPost post = new HttpPost("http://" + OWNER_URL + resource);
			post.setEntity(entity);
			return post;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpGet getParenRegistHttpGet(Map<String, String> map,
			String resource) {
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			if (map != null) {
				for (String key : map.keySet()) {
					qparams.add(new BasicNameValuePair(key, map.get(key)));
				}
			}
			URI uri = URIUtils.createURI("http", PARENT_REGIST_URL, -1,
					resource, URLEncodedUtils.format(qparams, "UTF-8"), null);
			HttpGet httpget = new HttpGet(uri);
			return httpget;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpPost getParenRegistHttpPost(Map<String, String> map,
			String resource) {
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			if (map != null) {
				for (String key : map.keySet()) {
					qparams.add(new BasicNameValuePair(key, map.get(key)));
				}
			}
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(qparams,
					"UTF-8");
			
			HttpPost post = new HttpPost("http://" + PARENT_REGIST_URL
					+ resource);
			post.setEntity(entity);
			return post;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpPost getDynamicParenRegistHttpPost(HttpEntity entity,
			String resource) {
		try {
			HttpPost post = new HttpPost("http://" + PARENT_REGIST_URL
					+ resource);
			post.setEntity(entity);
			return post;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static HttpGet accessSelf(Map<String, String> map, String resource) {
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			if (map != null) {
				for (String key : map.keySet()) {
					qparams.add(new BasicNameValuePair(key, map.get(key)));
				}
			}
			URI uri = URIUtils.createURI("http",
					ConfigUtil.LOCAL_URL, -1, resource,
					URLEncodedUtils.format(qparams, "UTF-8"), null);
			System.out.println(uri);
			HttpGet httpget = new HttpGet(uri);
			return httpget;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static HttpPost accessSelfViaPost(Map<String, String> map, String resource) {
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			if (map != null) {
				for (String key : map.keySet()) {
					qparams.add(new BasicNameValuePair(key, map.get(key)));
				}
			}
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(qparams,
					"UTF-8");
			HttpPost post = new HttpPost("http://" + ConfigUtil.LOCAL_URL + resource);
			post.setEntity(entity);
			return post;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void accessVisGet(Map<String, String> map,String resource, String url){
		HttpResponse response = null;
		try{
			response = getClient().execute(accessPlatformGrade(map,resource,url));
		}catch(Exception e ){
			throw new RuntimeException(e);
		}finally{
			try{
				if(response != null){
					InputStream in =  response.getEntity().getContent();
					in.close();
				}
			}catch(Exception e){
				throw new RuntimeException(e);
			}
		}
	}
	

	public static HttpGet accessPlatformGrade(Map<String, String> map,
			String resource, String url) {
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			if (map != null) {
				for (String key : map.keySet()) {
					qparams.add(new BasicNameValuePair(key, map.get(key)));
				}
			}
			URI uri = URIUtils.createURI("http", url, -1, resource,
					URLEncodedUtils.format(qparams, "UTF-8"), null);
			HttpGet httpget = new HttpGet(uri);
			return httpget;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static HttpGet getOuterDeviceByBG(Map<String, String> map,
			String resource, String url) {
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			if (map != null) {
				for (String key : map.keySet()) {
					qparams.add(new BasicNameValuePair(key, map.get(key)));
				}
			}
			URI uri = URIUtils.createURI("http", url, -1, resource,
					URLEncodedUtils.format(qparams, "GBK"), null);
			HttpGet httpget = new HttpGet(uri);
			return httpget;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static HttpPost getOuterDeviceByBGPost(Map<String, String> map,
			String url,String resource) {
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			if (map != null) {
				for (String key : map.keySet()) {
					qparams.add(new BasicNameValuePair(key, map.get(key)));
				}
			}
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(qparams,
					"GBK");
			HttpPost post = new HttpPost(url+resource);
			post.setEntity(entity);
			return post;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void postJson() throws ClientProtocolException, IOException{
		 HttpPost post = new HttpPost("http://localhost:8888");
		 StringEntity entity = new StringEntity("{\"queryID\": \"650101gawwlmqsgawfri002012122612200423\", \"searchDistrict\":\"wlmqs\", \"userID\":\"0000000000200000000000000110000\",\"IDNumber\":\"110123456789012345\", \"name\":\"张三\", \"policeID\":\"\", \"deviceID\": \"65020200001310006001\", \"deviceName\": \"王府大街摄像机\", \"address\": \"王府大街11号\", \"startTime\": \"2012-01-01 16:20:26\", \"endTime\": \"2013-08-26 16:20:26\"}");
		 entity.setContentType("application/json");
		 entity.setContentEncoding("utf-8");
		 post.setEntity(entity);
		 getClient().execute(post);
	}
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		postJson();
	}
	

	public static HttpPost testPost(String url) {
		try {
			List<NameValuePair> qparams = new ArrayList<NameValuePair>();
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(qparams,
					"GBK");
			HttpPost post = new HttpPost(url);
			post.setEntity(entity);
			return post;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
