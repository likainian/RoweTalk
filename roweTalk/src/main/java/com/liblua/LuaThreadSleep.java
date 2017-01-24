package com.liblua;

import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;

public class LuaThreadSleep extends JavaFunction
{
	private static final String TAG = LuaThreadSleep.class.getName();
	private LuaState L;
	private LuaEnv Env;
	
	public LuaThreadSleep(LuaEnv Env, LuaState L)
	{
		super(L);
		this.L = L;
		this.Env = Env;
	}

	@Override
	public int execute() throws LuaException
	{
		if (L.getTop() < 2)
		{
			Env.print("bad parameter count");
			L.pushBoolean(false);
			return 1;
		}
		// get prop
		long ms = L.toInteger(2);
		L.pushBoolean(Env.sleep(ms));
		return 1;
	}
}

