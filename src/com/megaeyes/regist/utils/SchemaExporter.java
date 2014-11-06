package com.megaeyes.regist.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import com.megaeyes.utils.OurNamingStrategy;

/**
 * 
 * 
 * 
 */
public class SchemaExporter {

	private static Map<String, String> dialects = new HashMap<String, String>();
	static {
		dialects.put("mysql", "org.hibernate.dialect.MySQLDialect");
		dialects.put("mysql5", "org.hibernate.dialect.MySQL5InnoDBDialect");
		dialects.put("oracle", "org.hibernate.dialect.Oracle9Dialect");
	}

	/**
	 * <pre>
	 * example:
	 * $ java net.paoding.ar.practice.SchemaExporter mysql5 com.xiaonei.blog.domain
	 * 
	 * <pre>
	 * 
	 * &#064;param args
	 *            [0]=dialect[mysql|mysql5|oralce] [1]=packages&lt;br&gt;
	 * 
	 * &#064;throws IOException
	 * @throws Exception
	 */
	public static void main(String[] args) throws IOException, Exception {
		Configuration config = new Configuration();
		config.setNamingStrategy(OurNamingStrategy.INSTANCE);
		String dialect = dialects.get("mysql5");
		config.setProperty("hibernate.dialect", dialect == null ? args[0]
				: dialect);
		setDomain(config,"com.megaeyes.regist.domain");
		SchemaExport se = new SchemaExport(config);
		se.setDelimiter(";");
		se.setFormat(true);
		se.create(true, false);
	}

	@SuppressWarnings("unchecked")
	private static void setDomain(Configuration config, String packageName) {
		try{
		Class[] clazzes = getClasses(packageName);
		for(Class clazz : clazzes){
			if(clazz.getAnnotation(Entity.class) != null){
				config.addAnnotatedClass(clazz);
			}
		}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	
	/**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
	 * @throws URISyntaxException 
     */
	private static <T> Class[] getClasses(String packageName)
            throws ClassNotFoundException, IOException, URISyntaxException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            URI outputURI = new URI(("file:///"+ resource.getFile().replaceAll(" ", "%20"))); 
            dirs.add(new File(outputURI));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
	private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

}
