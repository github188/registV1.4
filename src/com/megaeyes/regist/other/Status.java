package com.megaeyes.regist.other;

public enum Status {
	add {
		@Override
		public String getHNDBOperation() {
			return "ADD";
		}
	},
	update {
		@Override
		public String getHNDBOperation() {
			return "MOD";
		}
	},
	delete {
		@Override
		public String getHNDBOperation() {
			return "DEL";
		}
	},
	normal {
		@Override
		public String getHNDBOperation() {
			return "";
		}
	};
	public static Status getStatusByOrdinal(int ordinal) {
		if (add.ordinal() == ordinal) {
			return add;
		}
		if (update.ordinal() == ordinal) {
			return update;
		}
		if (delete.ordinal() == ordinal) {
			return delete;
		}
		return normal;
	}
	public abstract String getHNDBOperation();
}
