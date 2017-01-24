package com.liblua;

import com.example.rowetalk.util.RootShellCmd;
import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;

public class LuaJavaExec extends JavaFunction
{
	private static final String TAG = LuaJavaExec.class.getName();
	private LuaState L;
	private LuaEnv Env;
	
	public LuaJavaExec(LuaEnv Env, LuaState L)
	{
		super(L);
		this.L = L;
		this.Env = Env;
	}

	@Override
	public int execute() throws LuaException
	{
		int count = L.getTop();
		if (count < 2)
		{
			Env.print("bad parameter count");
			L.pushBoolean(false);
			L.pushString("bad parameter count");
			return 2;
		}
		boolean ret = false;
		// get prop
		String cmd = L.toString(2);
		if(count>2){
			ret = L.toBoolean(3);
		}
		StringBuilder sb= null;
		if(ret){
			sb = new StringBuilder();
		}
		try {
			boolean b = RootShellCmd.getInstance().exec(cmd, sb);
			L.pushBoolean(b);
			L.pushString(sb!=null?sb.toString():"");
		}catch(Exception e){
			e.printStackTrace();
			L.pushBoolean(false);
			L.pushString("");
		}
		return 2;
	}
}

