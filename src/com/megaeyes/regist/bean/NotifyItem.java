package com.megaeyes.regist.bean;

import com.mega.jdom.Element;
import com.megaeyes.regist.bean.sendResource.Item;
import com.megaeyes.regist.bean.sendResource.NotifyEntity;

public class NotifyItem extends Item{
	private String event;

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}
	
	private NotifyEntity notifyEntity;
	

	public NotifyEntity getNotifyEntity() {
		return notifyEntity;
	}

	public void setNotifyEntity(NotifyEntity notifyEntity) {
		this.notifyEntity = notifyEntity;
	}

	public void initItem(Element el){
		super.initItem(el);
		this.setEvent(el.getChildText("Event"));
	}
}
