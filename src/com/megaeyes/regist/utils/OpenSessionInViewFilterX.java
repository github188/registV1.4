package com.megaeyes.regist.utils;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;

public class OpenSessionInViewFilterX extends OpenSessionInViewFilter{
	@Override
	protected Session getSession(SessionFactory sessionFactory)
			throws DataAccessResourceFailureException {
		Session session = super.getSession(sessionFactory);
		session.setFlushMode(FlushMode.AUTO);
		return session;
	}
	@Override
	protected void closeSession(Session session, SessionFactory sessionFactory) {
		// TODO Auto-generated method stub
		session.flush();
		super.closeSession(session, sessionFactory);
	}
	
}
