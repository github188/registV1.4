package com.megaeyes.regist.enump;

public enum DircetionType {
	Dircetion_Type0 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "";
		}
	},
	Dircetion_Type1 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "东";
		}
	},
	Dircetion_Type2 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "西";
		}
	},
	Dircetion_Type3 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "南";
		}
	},
	Dircetion_Type4 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "北";
		}
	},
	Dircetion_Type5 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "东南";
		}
	},
	Dircetion_Type6 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "东北";
		}
	},
	Dircetion_Type7 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "西南";
		}
	},
	Dircetion_Type8 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "西北";
		}
	};
	public abstract String getComment();
	
	public static DircetionType getInstance(int ordernal){
		switch (ordernal) {
		case 1:
			
			return Dircetion_Type1;
		case 2:
			
			return Dircetion_Type2;
		case 3:
			
			return Dircetion_Type3;
		case 4:
			
			return Dircetion_Type4;
		case 5:
			
			return Dircetion_Type5;
		case 6:
			
			return Dircetion_Type6;
		case 7:
			
			return Dircetion_Type7;
		case 8:
			
			return Dircetion_Type8;

		default:
			return Dircetion_Type0;
		}
	}
}
