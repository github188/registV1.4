package com.megaeyes.regist.bean;

import java.math.BigInteger;

public class PlatformResourceStatisticBean {
	private BigInteger organs = new BigInteger("0");
	private BigInteger vics = new BigInteger("0");
	private BigInteger aics = new BigInteger("0");
	private BigInteger ipvics = new BigInteger("0");
	private BigInteger servers = new BigInteger("0");
	public BigInteger getServers() {
		return servers;
	}

	public void setServers(BigInteger servers) {
		this.servers = servers;
	}

	private Integer all = 0 ;
	public BigInteger getOrgans() {
		return organs;
	}

	public void setOrgans(BigInteger organs) {
		this.organs = organs;
	}

	public BigInteger getVics() {
		return vics;
	}

	public void setVics(BigInteger vics) {
		this.vics = vics;
	}

	public BigInteger getAics() {
		return aics;
	}

	public void setAics(BigInteger aics) {
		this.aics = aics;
	}

	public BigInteger getIpvics() {
		return ipvics;
	}

	public void setIpvics(BigInteger ipvics) {
		this.ipvics = ipvics;
	}

	public Integer getAll() {
		this.all = Integer.parseInt(this.organs.toString())
				+ Integer.parseInt(this.vics.toString())
				+ Integer.parseInt(this.ipvics.toString())
				+ Integer.parseInt(this.aics.toString())
				+ Integer.parseInt(this.servers.toString());
		return this.all;
	}
}
