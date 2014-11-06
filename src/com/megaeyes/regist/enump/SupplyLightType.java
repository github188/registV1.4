package com.megaeyes.regist.enump;

public enum SupplyLightType {
	Supply_Light_Type0 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "";
		}
	},
	Supply_Light_Type1 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "无补光";
		}
	},
	Supply_Light_Type2 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "红外补光";
		}
	},
	Supply_Light_Type3 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "白光补光";
		}
	};
	public abstract String getComment();
	
	public static SupplyLightType getInstance(int ordernal){
		switch (ordernal) {
		case 1:
			return Supply_Light_Type1;
		case 2:
			return Supply_Light_Type2;
		case 3:
			return Supply_Light_Type3;
		default:
			return Supply_Light_Type0;
		}
	}
}
