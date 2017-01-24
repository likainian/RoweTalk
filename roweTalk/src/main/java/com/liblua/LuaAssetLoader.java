package com.liblua;

import java.io.IOException;

import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;

public class LuaAssetLoader extends JavaFunction
{

	private LuaState L;
	private LuaEnv Env;

	public LuaAssetLoader(LuaEnv Env, LuaState L)
	{
		super(L);
		this.L = L;
		this.Env = Env;
	}

	@Override
	public int execute() throws LuaException
	{
		String name = L.toString(-1);
		name = name.replace('.', '/') + ".lua";
		try
		{
			byte[] bytes = Env.readAsset(name);
			int ok=L.LloadBuffer(bytes, name);
			if (ok != 0)
				L.pushString("\n\t" + L.toString(-1));
			return 1;
		}
		catch (IOException e)
		{
			L.pushString("\n\tno file \'/assets/" + name + "\'");
			return 1;
		}
	}

}

