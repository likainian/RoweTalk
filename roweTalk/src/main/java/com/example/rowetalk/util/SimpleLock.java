package com.example.rowetalk.util;

import java.io.Serializable;

public class SimpleLock implements Serializable {
	public String name;
	public int result;
	public Object obj;
	
	public SimpleLock(){
		this.name = "";
		this.result = -1;
		this.obj = null;
	}
}
