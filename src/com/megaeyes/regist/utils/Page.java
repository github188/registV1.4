package com.megaeyes.regist.utils;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;

import com.megaeyes.utils.Ar;

public class Page {
	private int pageSize = 100;
	private int currentPage;
	private long recordCount;
	private int pageCount;
	private String position ="";

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getCurrentPage() {
		if (currentPage == 0) {
			currentPage = 1;
		}
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public long getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(long recordCount) {
		this.recordCount = recordCount;
	}

	public int getPageCount() {
		pageCount = (int) ((recordCount + pageSize - 1) / pageSize);
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getFirstResult() {
		return (getCurrentPage() - 1) * getPageSize();
	}

	public void setRecordCount(DetachedCriteria criteria) {
		criteria.setProjection(Projections.rowCount());
		long count = Ar.one(criteria);
		this.setRecordCount(count);
		criteria.setProjection(null);
		criteria.setResultTransformer(DetachedCriteria.ROOT_ENTITY);
	}

	public void setLastRecordCount(DetachedCriteria criteria) {
		if (this.getRecordCount() == 0) {
			setRecordCount(criteria);
		}
	}
}
