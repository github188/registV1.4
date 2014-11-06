package com.megaeyes.regist.enump;

public enum UseType {
	USETYPE {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "";
		}
	},PUBLIC_ORDER {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "治安";
		}
	},TRAFFIC {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "交通";
		}
	},IMPORTANCE {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "重点";
		}
	};
	public abstract String getComment();
	
	public static UseType getInstance(int ordernal){
		switch (ordernal) {
		case 1:
			return PUBLIC_ORDER;
		case 2:
			return TRAFFIC;
		case 3:
			return IMPORTANCE;

		default:
			return USETYPE;
		}
	}
}
