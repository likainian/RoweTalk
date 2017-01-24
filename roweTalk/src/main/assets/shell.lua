module("shell", package.seeall)


local string = require("string")
local table = require("table")
local math = require('math')

--[[
local Runtime=luajava.bindClass("java.lang.Runtime")
local DataOutputStream=luajava.bindClass("java.io.DataOutputStream")
local DataInputStream=luajava.bindClass("java.io.DataInputStream")
function exec(cmd)
   if not cmd then
      return false, 'bad cmd'
   end
   --local process = Runtime.getRuntime().exec("sh")  -- 系统签名，push到app目录下用这个
    local process = Runtime.getRuntime().exec("sh")
    local dos = DataOutputStream(process.getOutputStream());
    local dis = DataInputStream(process.getInputStream());
    local v= "("..cmd..") && echo ^OK$ || echo ^FAIL$"
     dos.writeBytes(v.."\n");
     dos.flush();
     print('exec: '..cmd)
    dos.writeBytes("exit\n");
    dos.flush();
    --process.waitFor();
    local r=""
    local t={}
    local b = false
    local loopCount = 0
    while loopCount<30 do
        if dis.available() > 0 then
          r=dis.readLine()
          if not r then
              break
          elseif r == "^OK$" then
              b = true
              print('exec => OK')
              break
          elseif r == "^FAIL$" then
              b = false
              print('exec => FAIL')
              break
          else 
              table.insert(t,r)
          end
        else
          thread_sleep(1000)
          loopCount = loopCount + 1
        end
    end
    dos.close()
    dis.close()
    return b, table.concat(t,"\n")
end
]]
-- try to solve exception: java.io.IOException: Error running exec(). Command: [sh] Working Directory: null Environment: null
function exec(cmd, ret)
   return java_exec(cmd, ret)
end

function inputKey(key_code)
   return exec('input keyevent '..tostring(key_code))
end

function inputTap(x, y, wait)
   local b = exec('input tap '..tostring(x)..' '..tostring(y))
   if b and wait then
      sleep(1)
   end 
   return b
end

function inputSwip(x1, y1, x2, y2)
   return exec('input swipe '..tostring(x1)..' '..tostring(y1)..' '..tostring(x2)..' '..tostring(y2))
end

function inputSwipLeft()
  return shell.inputSwip(650, 800, 300, 800)
end

function inputSwipUp()
  return shell.inputSwip(358, 500, 358, 800)
end

function inputSwipDown()
  return shell.inputSwip(358, 800, 358, 500)
end


function inputSwipRight()
  return shell.inputSwip(300, 800, 650, 800)
end

function inputText(text)
   return exec('input text '..text)
end

function installApp(apk_path, package_name)
  if not package_name or string.len(package_name) == 0 then
    return exec('pm install -r -f '..apk_path)
  else 
   return exec('pm install -r -f -i '..package_name..' '..apk_path)
  end
end

function uninstallApp(package_name)
   return exec('pm uninstall '..package_name)
end

function startApp(package_name, main_activity)
   return exec('am start -n '..package_name..'/'..main_activity)
end

function killApp(package_name)
   return exec('am force-stop '..package_name)
end

function checkApp(package_name)
   return exec('pm list packages | grep '..package_name)
end

function clearApp(package_name)
   return exec('pm clear '..package_name)
end

function rmDir(file_path)
   return exec("rm -ff "..file_path)
end

function rmFile(file_path)
   return exec("rm -f "..file_path)
end

function checkFile(file_path)
   return exec('ls '..file_path)
end

function sleep(seconds)
    if seconds >0 then
	-- return exec('sleep '..tostring(seconds))
	print('sleep '..tostring(seconds))
	return thread_sleep(seconds*1000)
    else
	return false
    end
end

function inputVirutalKeypadNumber(text)
	if string.len(text) == 0 then
		return false, 'text is empty'
	end
	print('inputVirutalKeypadNumber: '..text)
	local i=0
	local success, fail_msg
	local detect_nunmber_keypad = false
	for i=1,3 do
		local result = exec('ime set com.android.inputmethod.latin/.LatinIME')
		if result then 
			sleep(3)
			success, fail_msg = ocr(true) 
			if success and ocr(false, 0xff37474f, 78, 819) and ocr(false, 0xff37474f, 276, 814) and 
				ocr(false, 0xff37474f, 460, 813)  and ocr(false, 0xffeceff1, 69, 813)  and 
				ocr(false, 0xffeceff1, 267, 799) and ocr(false, 0xffeceff1, 462, 828)  then
				detect_nunmber_keypad = true
				break
			end
		end 
		sleep(2)
	end
	if detect_nunmber_keypad then
		print('detect number keypad')
	else
		return false, 'not detect number keypad'
	end
  for i=1,#text, 1 do 
    local ch = string.sub(text, i,i)
    if ch=='0' then
      inputTap(283,1146)
    elseif ch=='1' then
      inputTap(75,813)
    elseif ch=='2' then
      inputTap(270,811)
    elseif ch=='3' then
      inputTap(469,814)
    elseif ch=='4' then
      inputTap(70,924)
    elseif ch=='5' then
      inputTap(282,930)
    elseif ch=='6' then
      inputTap(469,930)
    elseif ch=='7' then
      inputTap(82,1035)
    elseif ch=='8' then
      inputTap(274,1038)
    elseif ch=='9' then
      inputTap(465,1027)
    end
  end
  return true, 'success'
end

function inputVirutalKeypadText(text)
	if string.len(text) == 0 then
		return false, 'text is empty'
	end
	print('inputVirutalKeypadText: '..text)
	local i
	local success, fail_msg
	local detect_keypad = false
	for i=1,3 do
		local result = exec('ime set com.android.inputmethod.latin/.LatinIME')
		if result then 
			sleep(3)
			success, fail_msg = ocr(true) 
			if success and ocr(false, 0xff7f8b8f, 658, 1035) and ocr(false, 0xffebeef0, 672, 1033)  and 
		       ocr(false, 0xffd1d6d9, 411, 1143)  and ocr(false, 0xff37474f, 576, 1158)  and 
		       ocr(false, 0xff4db6ac, 666, 1137)  and ocr(false, 0xfffefefe, 658, 1149)  then
				detect_keypad = true
				break
			end
		end 
		sleep(2)
	end

	if detect_keypad then
		print('detect virual keypad')
	else
		return false, 'virtual keypad not detected.'
	end

	for i=1,#text, 1 do 
      local ch = string.sub(text, i,i)
      local is_digit = false
      local is_lowercase = false
      local is_char = false
      if ch>='0' and ch <='9' then
        is_digit = true
      elseif ch >='a' and ch <='z' then
        is_char = true
        is_lowercase = true
      elseif ch >='A' and ch <='Z' then
        is_char = true
        is_lowercase = false
      end
      print('input '..tostring(ch)..' is_digit='..tostring(is_digit)..', is_char='..tostring(is_char)..',is_lowercase='..tostring(is_lowercase))
      if is_digit then 
        inputTap(58,1144)
        if ch=='0' then
          inputTap(682,814)
        elseif ch=='1' then
          inputTap(37,814)
        elseif ch=='2' then
          inputTap(109,814)
        elseif ch=='3' then
          inputTap(178,814)
        elseif ch=='4' then
          inputTap(252,814)
        elseif ch=='5' then
          inputTap(324,814)
        elseif ch=='6' then
          inputTap(396,814)
        elseif ch=='7' then
          inputTap(466,814)
        elseif ch=='8' then
          inputTap(541,814)
        elseif ch=='9' then
          inputTap(613,814)
        end
        inputTap(58,1144)
      elseif is_char then
        if not is_lowercase then
          inputTap(50,1039)
        end
        ch = string.lower(ch)
        if ch=='q' then
          inputTap(36,820)
        elseif ch=='w' then
          inputTap(106,820)
        elseif ch=='e' then
          inputTap(178,820)
        elseif ch=='r' then
          inputTap(250,820)
        elseif ch=='t' then
          inputTap(322,820)
        elseif ch=='y' then
          inputTap(396,820)
        elseif ch=='u' then
          inputTap(466,820)
        elseif ch=='i' then
          inputTap(541,820)
        elseif ch=='o' then
          inputTap(610,820)
        elseif ch=='p' then
          inputTap(684,820)
        elseif ch=='a' then
          inputTap(70,933)
        elseif ch=='s' then
          inputTap(145,933)
        elseif ch=='d' then
          inputTap(216,933)
        elseif ch=='f' then
          inputTap(286,933)
        elseif ch=='g' then
          inputTap(360,933)
        elseif ch=='h' then
          inputTap(432,933)
        elseif ch=='j' then
          inputTap(504,933)
        elseif ch=='k' then
          inputTap(576,933)
        elseif ch=='l' then
          inputTap(648,933)
        elseif ch=='z' then
          inputTap(144,1038)
        elseif ch=='x' then
          inputTap(216,1038)
        elseif ch=='c' then
          inputTap(286,1038)
        elseif ch=='v' then
          inputTap(358,1038)
        elseif ch=='b' then
          inputTap(430,1038)
        elseif ch=='n' then
          inputTap(502,1038)
        elseif ch=='m' then
          inputTap(578,1038)
        end
        if not is_lowercase then
          --inputTap(50,1039)
          sleep(1)
        end
      else
        --input chars
        inputTap(58,1144)
        if ch == '@' then
          inputTap(73,930)
        elseif ch == '#' then
          inputTap(142,930)
        elseif ch == '$' then
          inputTap(216,930)
        elseif ch == '%' then
          inputTap(286,930)
        elseif ch == '&' then
          inputTap(358,930)
        elseif ch == '-' then
          inputTap(430,930)
        elseif ch == '+' then
          inputTap(502,930)
        elseif ch == '(' then
          inputTap(576,930)
        elseif ch == ')' then
          inputTap(648,930)
        elseif ch == '*' then
          inputTap(144,1029)
        elseif ch == '"' then
          inputTap(216,1029)
        elseif ch == '\'' then
          inputTap(286,1029)
        elseif ch == ':' then
          inputTap(360,1029)
        elseif ch == ';' then
          inputTap(432,1029)
        elseif ch == '!' then
          inputTap(502,1029)
        elseif ch == '?' then
          inputTap(576,1029)
        elseif ch == ',' then
          inputTap(144,1155)
        elseif ch == '_' then
          inputTap(216,1155)
        elseif ch == '/' then
          inputTap(504,1155)
        elseif ch == '.' then
          inputTap(576,1155)
        else
          return false, 'unsupport char '..tostring(ch)
        end
        inputTap(58,1144)
      end
	end 
	return true, 'success'
end

--[[
function Utf8to32(utf8str)
  --assert(type(utf8str) == "string")
  local res, seq, val = {}, 0, nil
  for i = 1, #utf8str do
    local c = string.byte(utf8str, i)
    if seq == 0 then
      table.insert(res, val)
      seq = c < 0x80 and 1 or c < 0xE0 and 2 or c < 0xF0 and 3 or
            c < 0xF8 and 4 or --c < 0xFC and 5 or c < 0xFE and 6 or
          error("invalid UTF-8 character sequence")
      val = bit32.band(c, 2^(8-seq) - 1)
    else
      val = bit32.bor(bit32.lshift(val, 6), bit32.band(c, 0x3F))
    end
    seq = seq - 1
  end
  table.insert(res, val)
  table.insert(res, 0)
  return res
end
]]

function setImeLatin() 
  return exec('ime set com.android.inputmethod.latin/.LatinIME')
end

function setImeTy() 
  local b = exe('is_tyime_present')
  if not b then
      return exec('ime set com.ty.ime/.LatinIME')
  else
      return true
  end
end

function inputImeText(text, go, auto_wait)
  if not text or #text == 0 then
    print('inputImeText failed: text is empyt')
    return false, 'bad text'
  end
  print('inputImeText: '..text)
  
  return exe('input_chinese', text, go, auto_wait)
end

function checkTyIme() 
  return exec('ime list -s  | grep com.ty.ime')
end

function get_rgb(color)
    local r = (color & 0xff0000) >> 16
  local g = (color & 0xff00) >> 8
  local b = color & 0xff
  return r,g,b
end

function is_color_similiar(c1, c2)
  local r1, g1, b1 = get_rgb(c1)
  local r2, g2, b2 = get_rgb(c2)
  local dr = r1 -r2
  local dg = g1 -g2
  local db = b1 -b2
  local dd = math.sqrt(dr*dr + dg*dg + db* db)
  --print('test_color: dd='..tostring(dd))
  return (dd<150)
end


function openUrl(url)
  print('openUrl: '..url)
  return exec('am start -a android.intent.action.VIEW -d '..url)
end



