package com.liblua;


import com.example.rowetalk.util.StringUtil;
import com.luajava.JavaFunction;
import com.luajava.LuaException;
import com.luajava.LuaState;

public class LuaPrint extends JavaFunction
{

	private LuaState L;
	private LuaEnv Env;
	
	public LuaPrint(LuaEnv Env, LuaState L)
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
			Env.print("");
			return 0;
		}
		StringBuilder output = new StringBuilder();
		for (int i = 2; i <= L.getTop(); i++)
		{
			int type = L.type(i);
			String val = null;
			String stype = L.typeName(type);
			if (stype.equals("userdata"))
			{
				Object obj = L.toJavaObject(i);
				if (obj != null)
					val = obj.toString();
			}
			else if (stype.equals("boolean"))
			{
				val = L.toBoolean(i) ? "true" : "false";
			}
			else
			{
				val = L.toString(i);
			}
			if (val == null)
				val = stype;						
			output.append("\t");
			output.append(val);
			output.append("\t");
		}
		String s = output.toString();
		if(!StringUtil.isEmptyOrNull(s))
			Env.print(s.substring(1, s.length() - 1));
		return 0;
	}


}

