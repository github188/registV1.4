package com.megaeyes.regist.other;

import com.megaeyes.regist.domain.Device;
import com.megaeyes.regist.domain.DeviceServer;
import com.megaeyes.regist.domain.Organ;

public enum ResourceType {
	DEVICE {
		@Override
		public String getDesc() {
			return "DEVICE";
		}
		@Override
		public Class<?> getResourceClass() {
			return Device.class;
		}
		@Override
		public String getSuffix() {
			return "";
		}
	},
	ORGAN {
		@Override
		public String getDesc() {
			return "ORGAN";
		}
		@Override
		public Class<?> getResourceClass() {
			return Organ.class;
		}
		@Override
		public String getSuffix() {
			return "@organ";
		}
	},
	VIC{
		@Override
		public String getDesc() {
			return "VIC";
		}
		@Override
		public Class<?> getResourceClass() {
			return Device.class;
		}
		@Override
		public String getSuffix() {
			return "";
		}
	},
	IPVIC{
		@Override
		public String getDesc() {
			return "IPVIC";
		}
		@Override
		public Class<?> getResourceClass() {
			return Device.class;
		}
		@Override
		public String getSuffix() {
			return "@organ";
		}
	},
	AIC{
		@Override
		public String getDesc() {
			return "AIC";
		}
		@Override
		public Class<?> getResourceClass() {
			return Device.class;
		}
		@Override
		public String getSuffix() {
			return "";
		}
	},
	VIS{
		@Override
		public String getDesc() {
			return "VIS";
		}
		@Override
		public Class<?> getResourceClass() {
			return DeviceServer.class;
		}
		@Override
		public String getSuffix() {
			return "";
		}
	},
	DVR{
		@Override
		public String getDesc() {
			return "DVR";
		}
		@Override
		public Class<?> getResourceClass() {
			return DeviceServer.class;
		}
		@Override
		public String getSuffix() {
			return "";
		}
	};
	public static ResourceType getInstance(String type){
		for(ResourceType resourceType : ResourceType.values()){
			if(resourceType.getDesc().equals(type)){
				return resourceType;
			}
		}
		return VIC;
	}
	public abstract String getDesc();
	public abstract Class<?> getResourceClass();
	public abstract String getSuffix();
}
