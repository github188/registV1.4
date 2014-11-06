package com.megaeyes.regist.other;

public enum DeviceType {
	VIC{
		public String getType(){
			return "video_input_channel";
		}
	},
	AIC{
		@Override
		public String getType() {
			return "alarm_input_channel";
		}
	},
	AOC{
		@Override
		public String getType() {
			return "alarm_output_channel";
		}
		
	},
	IPVIC{
		@Override
		public String getType() {
			return "video_input_channel";
		}
		
	},
	UNKOWN{
		@Override
		public String getType() {
			return null;
		}
	};
	
	public abstract  String getType();
}

