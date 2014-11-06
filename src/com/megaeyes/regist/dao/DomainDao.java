package com.megaeyes.regist.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.megaeyes.utils.OurNamingStrategy;

@Repository
public class DomainDao {
	@Autowired
	private JdbcTemplate jdbc;
	public <T> List<T> find(Class<T> clazz){
		StringBuilder sql = new StringBuilder("select * from ");
		sql.append(namingStrategy.tableName(clazz.getSimpleName()));
		List<Map<String,Object>> rows = jdbc.queryForList(sql.toString());
		List<T> list = new ArrayList<T>();
		for(Map<String,Object> row : rows){
			try {
				T object = clazz.newInstance();
				setRowMpper(object, row);
				list.add(object);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public <T> List<T> find(Class<T> clazz,String sql,Object ...args){
		List<Map<String,Object>> rows = jdbc.queryForList(sql.toString(),args);
		List<T> list = new ArrayList<T>();
		for(Map<String,Object> row : rows){
			try {
				T object = clazz.newInstance();
				setRowMpper(object, row);
				list.add(object);
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return list;
	}
	
	public Map<Object,Object> findForMap(String sql,String key,String value){
		List<Map<String,Object>> rows = jdbc.queryForList(sql);
		Map<Object,Object> result = new HashMap<Object,Object>();
		for(Map<String,Object> row : rows){
			result.put(row.get(key), row.get(value));
		}
		return result;
	}
	
	public <T> T one(Class<T> clazz){
		StringBuilder sql = new StringBuilder("select * from ");
		sql.append(namingStrategy.tableName(clazz.getSimpleName()));
		sql.append(" limit 1");
		try {
			Map<String,Object> row = jdbc.queryForMap(sql.toString());
			if(row !=null){
				T object = clazz.newInstance();
				setRowMpper(object, row);
				return object;
			}
		} catch(EmptyResultDataAccessException e){
			return null;
		}catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public <T> T one(Class<T> clazz,String sql,Object ...args){
		try {
			Map<String,Object> row = jdbc.queryForMap(sql,args);
			if(row !=null){
				T object = clazz.newInstance();
				setRowMpper(object, row);
				return object;
			}
		}catch(EmptyResultDataAccessException e){
			return null;
		}catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	OurNamingStrategy namingStrategy = OurNamingStrategy.INSTANCE;
	public void setRowMpper(Object object,Map<String,Object> row){
		Field[] fields = object.getClass().getDeclaredFields();
		for(Field field : fields){
			String columnName = namingStrategy.columnName(field.getName());
			if(row.containsKey(columnName)){
				field.setAccessible(true);
				try {
					if(field.getType().isEnum()){
						Field myField = object.getClass().getDeclaredField("I"+field.getName());
						myField.setAccessible(true);
						myField.set(object, row.get(columnName));
					}else {
						field.set(object, row.get(columnName));
					}
				} catch (IllegalArgumentException e) {
					
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
