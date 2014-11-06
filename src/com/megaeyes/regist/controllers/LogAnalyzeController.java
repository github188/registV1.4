package com.megaeyes.regist.controllers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.hight.performance.annotation.Param;
import net.hight.performance.filter.HPFilter;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.springframework.stereotype.Component;

import com.megaeyes.utils.Invocation;

@Component("logAnalyze")
public class LogAnalyzeController {
	public void getLog4j(Invocation inv) throws ConfigurationException {
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/log4j.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		File logFile = new File(
				(String) config.getProperty("log4j.appender.logfile.File"));
		try {
			inv.getResponse().setContentType("application/force-download");
			inv.getResponse().setHeader("Content-Disposition",
					"attachment; filename=" + logFile.getName());
			InputStream is = new BufferedInputStream(new FileInputStream(
					logFile));
			org.apache.commons.io.IOUtils.copy(is, inv.getResponse()
					.getOutputStream());
			inv.getResponse().flushBuffer();
		} catch (IOException ex) {
			throw new RuntimeException("IOError writing file to output stream");
		}
	}

	public String getGbLogList(Invocation inv) throws ConfigurationException{
		File file = new File(HPFilter.getFc().getServletContext()
				.getRealPath("WEB-INF/config.properties"));
		PropertiesConfiguration config = new PropertiesConfiguration(file);
		List<File> logFiles = new ArrayList<File>();
		listf((String) config.getProperty("recordSendLog.dir"), logFiles);
		List<LogFile> logs = new ArrayList<LogFile>();
		for(File logFile:logFiles){
			if(logFile.getName().startsWith(".")){
				continue;
			}
			LogFile  log = new LogFile();
			log.setLastModified(new Date(logFile.lastModified()));
			log.setLength(new DecimalFormat("###,###,###.##").format(new Double(logFile.length())/(1000*1000))+"M");
			log.setName(logFile.getName());
			log.setPath(logFile.getAbsolutePath());
			logs.add(log);
		}
		inv.addModel("logFiles", logs);
		return "init/logAnalyze/gb-log-list";
	}
	
	public void download(Invocation inv,@Param("fileName")String fileName){
		File logFile = new File(fileName);
		InputStream is = null;
		try {
			inv.getResponse().setContentType("application/force-download");
			inv.getResponse().setHeader("Content-Disposition",
					"attachment; filename=" + logFile.getName());
			is = new BufferedInputStream(new FileInputStream(
					logFile));
			org.apache.commons.io.IOUtils.copy(is, inv.getResponse()
					.getOutputStream());
			inv.getResponse().flushBuffer();
		} catch (IOException ex) {
			throw new RuntimeException("IOError writing file to output stream");
		}finally{
			if(is != null){
				try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public String deleteLogFile(Invocation inv,@Param("fileName")String fileName){
		File logFile = new File(fileName);
		logFile.delete();
		return "@success";
	}

	public class LogFile {
		private String name;
		private String path;
		private String length;
		private Date lastModified;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getPath() {
			return path;
		}

		public void setPath(String path) {
			this.path = path;
		}

		public String getLength() {
			return length;
		}

		public void setLength(String length) {
			this.length = length;
		}

		public Date getLastModified() {
			return lastModified;
		}

		public void setLastModified(Date lastModified) {
			this.lastModified = lastModified;
		}

	}

	private void listf(String directoryName, List<File> files) {
		File directory = new File(directoryName);
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				files.add(file);
			} else if (file.isDirectory()) {
				listf(file.getAbsolutePath(), files);
			}
		}
	}
}
