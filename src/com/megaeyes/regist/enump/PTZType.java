package com.megaeyes.regist.enump;

public enum PTZType {
	ptz {
		@Override
		public String getComment() {
			return "";
		}
	},
	BALL {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "球机";
		}
	},
	HEMISPHERE {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "半球";
		}
	},
	FIXED_GUN {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "固定枪机";
		}
	},
	TELECONTROL_GUN {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "遥控枪机";
		}
	};
	public abstract String getComment();

	public static PTZType getInstance(int ordernal) {
		switch (ordernal) {
		case 1:
			return BALL;
		case 2:
			return HEMISPHERE;
		case 3:
			return FIXED_GUN;
		case 4:
			return TELECONTROL_GUN;

		default:
			return ptz;
		}
	}
}
