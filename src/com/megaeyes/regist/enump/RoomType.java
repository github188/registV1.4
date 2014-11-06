package com.megaeyes.regist.enump;

public enum RoomType {
	ROOM_TYPE {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "";
		}
	},
	OUTER {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "室外";
		}
	},
	INNER {
		@Override
		public String getComment() {
			// TODO Auto-generated method stub
			return "室内";
		}
	};
	public abstract String getComment();

	public static RoomType getInstance(int ordernal) {
		switch (ordernal) {
		case 1:
			return OUTER;
		case 2:
			return INNER;
		default:
			return ROOM_TYPE;
		}
	}
}
