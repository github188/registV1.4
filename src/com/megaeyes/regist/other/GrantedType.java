package com.megaeyes.regist.other;

import com.megaeyes.regist.domain.Role;
import com.megaeyes.regist.domain.User;


public enum GrantedType {
	USER {
		@Override
		public String getDesc() {
			return "USER";
		}
		@Override
		public Class<?> getResourceClass() {
			return User.class;
		}
	},
	ROLE {
		@Override
		public String getDesc() {
			return "ROLE";
		}
		@Override
		public Class<?> getResourceClass() {
			return Role.class;
		}
	};
	
	public static GrantedType getGrantedType(String type){
		for(GrantedType grantedType : GrantedType.values()){
			if(grantedType.name().equals(type)){
				return grantedType;
			}
		}
		return null;
	}

	public abstract String getDesc();

	public abstract Class<?> getResourceClass();
}
