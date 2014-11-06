package com.megaeyes.regist.tasks;

import java.sql.Connection;

public interface IDoInConnection {
	public void execute(Connection conn);
}
