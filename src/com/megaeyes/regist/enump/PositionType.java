package com.megaeyes.regist.enump;

public enum PositionType {
	POSITION_TYPE0 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "";
		}
	},
	POSITION_TYPE1 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "省际检查站";
		}
	},
	POSITION_TYPE2 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "党政机关";
		}
	},
	POSITION_TYPE3 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "车站码头";
		}
	},
	POSITION_TYPE4 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "中心广场";
		}
	},
	POSITION_TYPE5 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "体育场";
		}
	},
	POSITION_TYPE6 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "商业中心";
		}
	},
	POSITION_TYPE7 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "宗教场所";
		}
	},
	POSITION_TYPE8 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "校园周边";
		}
	},
	POSITION_TYPE9 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "治安复杂区域";
		}
	},
	POSITION_TYPE10 {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "交通干线";
		}
	};
	public abstract String getComment();

	public static PositionType getInstance(int ordernal) {
		switch (ordernal) {
		case 1:

			return POSITION_TYPE1;
		case 2:

			return POSITION_TYPE2;
		case 3:

			return POSITION_TYPE3;
		case 4:

			return POSITION_TYPE4;
		case 5:

			return POSITION_TYPE5;
		case 6:

			return POSITION_TYPE6;
		case 7:

			return POSITION_TYPE7;
		case 8:

			return POSITION_TYPE8;
		case 9:

			return POSITION_TYPE9;
		case 10:

			return POSITION_TYPE10;

		default:
			return POSITION_TYPE0;
		}
	}
}
