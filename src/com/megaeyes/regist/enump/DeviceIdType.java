package com.megaeyes.regist.enump;

import com.megaeyes.regist.domain.Organ;
import com.megaeyes.regist.domain.Platform;
import com.megaeyes.utils.Ar;

public enum DeviceIdType {
	Mega {
		@Override
		public String sample() {
			return "0000000000000000000000000000000";
		}
	},
	Cms {
		@Override
		public String sample() {
			return "000000";
		}
		
		
	},
	Organ {
		@Override
		public String sample() {
			return "00000000";
		}
	},
	DVR {
		@Override
		public String sample() {
			return "111";
		}
	},
	VIS {
		@Override
		public String sample() {
			return "112";
		}
	},
	VIC {
		@Override
		public String sample() {
			return "131";
		}
	},
	IPVIC {
		@Override
		public String sample() {
			return "132";
		}
	},
	AIC {
		@Override
		public String sample() {
			return "134";
		}
	},
	CMS_DEVICE{
		@Override
		public String sample() {
			return "2";
		}
	},
	NVR{
		@Override
		public String sample() {
			return "118";
		}
	},
	UNKWON_TYPE{
		@Override
		public String sample() {
			return "involid";
		}
	};
	public abstract String sample();
	public String fullSample(){
		return "00000000000000";
	}
	
	/**
	 * 判断id是否是平台的编号
	 * @param id
	 * @return
	 */
	public static boolean isCmsId(String id) {
//		if(id.length()<=Cms.sample().length()){
//			return true;
//		}
//		if(id.length()>6 && id.substring(6).equals(Cms.fullSample())){
//			return true;
//		}
		return isGBPlatformCmsId(id);
	}
	
	public static boolean isGBPlatformCmsId(String id){
		Platform platform = Ar.of(Platform.class).one("gbPlatformCmsId",id);
		if(platform != null){
			return true;
		}
		return false;
	}
	
	public static String hasGBPlatformCmsId(String id){
		Platform platform = Ar.of(Platform.class).one("gbPlatformCmsId",id);
		if(platform != null){
			return platform.getCmsId();
		}
		return "";
	}
	
	public static boolean isCmsDevice(String id) {
		return id.length()>10 && id.substring(10, 11).equals("2");
	}
	
	public static boolean isOrgan(String id) {
//		return id.length()==Organ.sample().length() || (id.length()==20 && id.substring(10, 13).equals("200")) ||isDbOrganStdId(id);
		return id.length()==2 || id.length()==4 || id.length()==6 || id.length()==8 ||isDbOrganStdId(id) || isVirtrueOrgan(id) || isBusinessOrgan(id);
	}
	
	public static String getParentOrganId(String id,String cmsId){
		if(id.endsWith("000000")){
			id = id.substring(0,id.length()-6);
		}else if(id.endsWith("0000")){
			id = id.substring(0,id.length()-4);
		}else if(id.endsWith("00")){
			id = id.substring(0, id.length()-2);
		}
		if(id.length()==2){
			return null;
		}else if(id.length() == 4){
			return getMegaId(fullOrganId(id.substring(0,2)));
		}else if(id.length() == 6){
			return getMegaId(fullOrganId(id.substring(0,4)));
		}else if(id.length() == 8){
			return getMegaId(fullOrganId(id.substring(0,6)));
		}
		return null;
	}
	
	public static boolean isVirtrueOrgan(String id) {
		return  id.length()==20 && id.substring(10, 13).equals("216");
	}
	
	public static boolean isBusinessOrgan(String id) {
		return  id.length()==20 && id.substring(10, 13).equals("215");
	}
	
	private static boolean isDbOrganStdId(String stdId) {
		Organ organ = Ar.of(Organ.class).one("stdId",stdId);
		if(organ != null){
			return true;
		}
		return false;
	}
//	public static String getType(String id){
//		return id.substring(10, 13);
//	}

	public static boolean isDVR(String id) {
		return id.length()==20 && id.substring(10, 13).equals(DeviceIdType.DVR.sample());
	}

	public static boolean isVIS(String id) {
		return id.length()==20 &&  id.substring(10, 13).equals(DeviceIdType.VIS.sample());
	}

	public static boolean isIPVIC(String id) {
		return id.length()==20 && id.substring(10, 13).equals(DeviceIdType.IPVIC.sample());
	}

	public static boolean isVIC(String id) {
		return id.length()==20 && id.substring(10, 13).equals(DeviceIdType.VIC.sample());
	}

	public static boolean isAIC(String id) {
		return id.length()==20 && id.substring(10, 13).equals(DeviceIdType.AIC.sample());
	}
	
	public static boolean isNVR(String id){
		return id.length()==20 && id.substring(10, 13).equals(DeviceIdType.NVR.sample());
	}

	public static boolean isTerminal(String id) {
//		return id.length()==20 && TerminalType.getTypes().contains(id.substring(10, 13));
		return isVIC(id) || isIPVIC(id) || isAIC(id);
	}

	public static boolean isServer(String id) {
//		return id.length()==20 && ServerType.getTypes().contains(id.substring(10, 13));
		return isVIS(id) || isDVR(id) || isNVR(id);
	}

	public static DeviceIdType getType(String id) {
		if (isVIC(id)) {
			return VIC;
		} else if (isAIC(id)) {
			return AIC;
		} else if (isIPVIC(id)) {
			return IPVIC;
		} else if (isOrgan(id)) {
			return Organ;
		} else if (isVIS(id)) {
			return VIS;
		} else if (isDVR(id)) {
			return DVR;
		} else if (isCmsId(id)) {
			return Cms;
		} else if(isNVR(id)){
			return NVR;
		}
		System.out.println("#########################wrong deviceId:"+id);
		return null;
	}
	
	public static String getTypeCode(String type){
		if(type.equals(VIC.name())){
			return "131";
		}else if(type.equals(IPVIC.name())){
			return "132";
		}else if(type.equals(AIC.name())){
			return "134";
		}else if(type.equals(VIS.name())){
			return "112";
		}else if(type.equals(DVR.name())){
			return "111";
		}else if(type.equals(NVR.name())){
			return "118";
		}
		return "";
	}

	public static String getCmsId(String id) {
		String cmsId = "";
		if(id.length()==6){
			return id;
		}else if(!"".equals(cmsId = hasGBPlatformCmsId(id))){
			return cmsId;
		}
		return id
				+ DeviceIdType.Cms.sample().substring(0,
						Cms.sample().length() - id.length());
	}
	
	public static String getMegaId(String id){
		return DeviceIdType.Mega.sample().substring(0, 31 - id.length())
		+ id;
	}
	
	public static String fullOrganId(String id){
		if(id.length()>=6){
			return id;
		}
		return id
				+ DeviceIdType.Cms.sample().substring(0,
						Cms.sample().length() - id.length());
	}
	
	public static String getBusinessOrganParentId(String deviceId){
		String fullId = deviceId.substring(0,8);
		if (fullId.endsWith("00")){
			return fullId.substring(0,6);
		}else{
			return fullId;
		}
	}
	
	public static String getCivilCodeByDeviceId(String deviceId){
		String fullId = "";
		if(deviceId.length()>=8){
			fullId = deviceId.substring(0,8);
		}else{
			fullId = deviceId;
		}
		if (fullId.endsWith("000000")){
			return fullId.substring(0,2);
		}else if(fullId.endsWith("0000")){
			return fullId.substring(0,4);
		}else if(fullId.endsWith("00")){
			return fullId.substring(0,6);
		}else{
			return fullId;
		}
	}
}
