package com.carl.lucene.enums;

public enum IndexEnum {
	SF((byte)1,"string field"),
	TF((byte)2,"text field"),
	F((byte)3,"field");
	private Byte code;
	private String desc;
	private IndexEnum(Byte code,String desc) {
		this.code = code;
		this.desc = desc;
	}
	public Byte getCode() {
		return code;
	}
	public void setCode(Byte code) {
		this.code = code;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
}
