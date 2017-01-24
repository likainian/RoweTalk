
local shell = require('shell')
local math = require('math')
local keycode = require('keycode')
local json = require('json')
local http = require('http')

local function test_weixin()
	local package_name = 'com.tencent.mm' 
	local main_acitivity = 'com.tencent.mm.ui.LauncherUI'
	local b = shell.checkApp(package_name)
	if not b then
		return false, 'package not exist'
	end

    -- get data from server
	local url= "http://120.26.129.37:8060/misc/allocwxdata"
    local imsi = getprop("imsi")
    if not imsi or string.lower(imsi) == "unknown" then
    	return false, 'unknown imsi'
    end
	print('request url='..url)
	local body, cookie, code, header = http.post(url, 'imsi='..imsi)
	if not body or #body == 0 then
		return false, 'http.post allocwxdata failed: code='..code
	end
	print('body='..body)
	local info = json.decode(body)
	if not info or not info['errorNo'] or info['errorNo'] ~= 0 or not info['data']  then
		return false, 'http.post allocwxdata failed: body='..body
	end
	local data = info['data']['weixin_account']
	--print ('data='..data)
	local weixin = json.decode(data)
	if not weixin then
		print('not found weixin_account')
		return false, 'not found weixin_account'
	end
	local weixin_id = weixin['eId']
	local cell_number = weixin['number']
	local status = weixin['status']
    local nickName = weixin['nickName']
    local nickPinyin = weixin['nickPinyin']
    local password = weixin['password']

    print('nickName='..nickName)
    print('nickPinyin='..nickPinyin)
    print('password='..password)
    print('status='..status)

    -- restart weixin app
	
	shell.clearApp(package_name)

	shell.startApp(package_name, main_acitivity)
	shell.sleep(12)

    print('test app begin')

    local fail_count = 0
    local idle_loop = 0
    local sms_code = nil
    local done = false
    local i = 0

    while fail_count < 10 and idle_loop<10 do 
		local success, fail_msg = getcolor(true) 
	    if not success then
			print('getColor failed: '..tostring(fail_msg))
			fail_count = fail_count + 1
		elseif getcolor(false, 379, 165)==0xff00070d and  getcolor(false, 363, 603)==0xff94aabf and 
			 getcolor(false, 121, 1144)==0xff000c15 and getcolor(false, 597, 1138)==0xff000c15 then	
			 -- splash earth screen
		     print('detect splash earth screen, do nothing')
		elseif getcolor(false, 379, 165)==0xff00070d and  getcolor(false, 363, 603)==0xff94aabf and 
			  getcolor(false, 121, 1144)==0xff45c01a and getcolor(false, 597, 1138)==0xfff1f1f1  then	
			-- signup or signin page
		    print('detect signup or signin select screen')
		    if status == 0 then
			  shell.inputTap(597, 1138)
			else
			  shell.inputTap(120, 1138)
			end
		elseif getcolor(false, 19, 168)==0xffffffff and  getcolor(false, 40, 168)==0xffcacaca and 
			getcolor(false, 678, 168)==0xff5ac734 and getcolor(false, 55, 241)==0xff909090 and 
			getcolor(false, 39, 312)==0xff353535 and getcolor(false, 627, 414)==0xffa3dea3 then	
			-- signin page
			print('detect signin screen')
			shell.inputTap(175, 241)
			success, fail_msg = shell.inputVirutalKeypadNumber(cell_number)
			if not success then
				print('inputVirutalKeypadNumber '..tostring(cell_number)..' failed: '..tostring(fail_msg))
				break
			end 
			shell.inputKey(keycode.KEYCODE_ENTER)
			shell.inputTap(192, 313)
			shell.sleep(2)
			success, fail_msg = shell.inputVirutalKeypadText(password)
			if not success then
				print('inputVirutalKeypadText '..tostring(password)..' failed: '..tostring(fail_msg))
				break
			end
			shell.sleep(1)
			shell.inputTap(361, 420)

	    elseif getcolor(false, 28, 73)==0xffffffff and  getcolor(false, 618, 69)==0xff393a3f and 
			getcolor(false, 42, 181)==0xffeeeeee and getcolor(false, 696, 217)==0xffaaaaaa and 
			getcolor(false, 654, 252)==0xff45c01a and getcolor(false, 675, 404)==0xffd9d9d9 then	
			-- signup page
			print('detect signup screen')
			-- shell.inputText(nickPinyin)
			--shell.inputVirutalKeypadText(nickPinyin)
			success, fail_msg = shell.inputImeText(nickName, true)
			if not success then
				print('inputImeText '..tostring(nickName)..' failed: '..tostring(fail_msg))
				break
			end 
			--shell.inputKey(keycode.KEYCODE_ENTER)
			success, fail_msg = shell.inputVirutalKeypadNumber(cell_number)
			if not success then
				print('inputVirutalKeypadNumber '..tostring(cell_number)..' failed: '..tostring(fail_msg))
				break
			end 
			shell.inputKey(keycode.KEYCODE_ENTER)
			shell.inputTap(174, 397)
			shell.sleep(2)
			success, fail_msg = shell.inputVirutalKeypadText(password)
			if not success then
				print('inputVirutalKeypadText '..tostring(password)..' failed: '..tostring(fail_msg))
				break
			end
			shell.inputTap(670, 402)
			shell.sleep(2)
			shell.inputTap(360, 510)
			shell.sleep(1)
			shell.inputTap(526, 699)
			local success, number, code = recvsms('',60,'(腾讯科技)','验证码([0-9]{6})')
			if success then
				print('recvsms: '..tostring(number)..','..tostring(code))
				sms_code = code
			end
		elseif getcolor(false, 160, 498)==0xff7f7f7f and  getcolor(false, 571, 490)==0xffffffff and 
			 getcolor(false, 157, 726)==0xff45c01a and  getcolor(false, 535, 768)==0xffffffff and 
			 getcolor(false, 202, 664)==0xff353535 and getcolor(false, 555, 724)==0xfffbfbfb then 
			 -- cell number page
		     print('detect verify cell number screen')
        elseif getcolor(false, 28, 73)==0xffffffff and  getcolor(false, 72, 70)==0xff2e2e32 and 
			getcolor(false, 126, 69)==0xffffffff and  getcolor(false, 192, 72)==0xffffffff and 
			getcolor(false, 118, 72)==0xff393a3f and  getcolor(false, 222, 79)==0xffc3c3c5 then 
			-- input sms page
			print('detect input sms code screen')
		 	if not sms_code then
		 		local succ, number, code = recvsms('',60,'(腾讯科技)','验证码([0-9]{6})')
				if succ then
					print('recvsms: '..tostring(number)..','..tostring(code))
					sms_code = code
				else
					shell.inputKey(keycode.KEYCODE_BACK)
			        shell.inputTap(502,690)
				end
		 	else
		 		shell.inputTap(280,308)
		 		shell.inputText(sms_code)
		 		shell.inputTap(351,486)
		 	end
		elseif getcolor(false, 85, 579)==0xff7f7f7f and  getcolor(false, 649, 547)==0xff7f7f7f and 
			 getcolor(false, 346, 781)==0xff7f7f7f and getcolor(false, 184, 691)==0xffffffff then

			if getcolor(false, 232, 552)==0xff353535 and  getcolor(false, 406, 555)==0xff9e9e9e and 
			  getcolor(false, 523, 688)==0xff1aad19 then
			  print('detect incorrect smscode screen')

        	elseif getcolor(false, 159, 565)==0xff363636 and  getcolor(false, 481, 553)==0xff4c4c4c and 
        	 getcolor(false, 523, 688)==0xff1aad19 then
			  print('detect smscode timeout screen')
			  shell.inputKey(keycode.KEYCODE_BACK)
			  shell.inputTap(502,690)
			end
		elseif getcolor(false, 361, 454)==0xfff3f3f3 and  getcolor(false, 262, 808)==0xfff3f3f3 and
			 getcolor(false, 543, 732)==0xffffffff  and getcolor(false, 195, 729)==0xffffffff and 
			 getcolor(false, 586, 792)==0xff44b91b  then
			 print('detect signup finish screen')
			 shell.inputTap(525, 724)
			 done = true
			 shell.sleep(3)
			 break
	    elseif getcolor(false, 43, 334)==0xff10aeff and  getcolor(false, 46, 391)==0xff3cb2ef and
			 getcolor(false, 42, 498)==0xff10aeff  and getcolor(false, 46, 592)==0xffffca00 then
			 print('detect me tab screen')
			 shell.sleep(3)
			 shell.inputTap(85, 1162)
			 done = true
			 break
	    elseif getcolor(false, 28, 73)==0xffffffff and  getcolor(false, 187, 73)==0xffffffff and
			 getcolor(false, 136, 442)==0xff45c01a  and getcolor(false, 583, 529)==0xfff1f1f1 then
			 print('detect confirm continue signup screen')
			 shell.sleep(3)
			 shell.inputTap(85, 1162)
			 done = true
			 break
	    elseif getcolor(false, 357, 1102)==0xffffffff and  getcolor(false, 88, 1150)==0xff45c01a and
			 getcolor(false, 264, 1158)==0xff999999  and getcolor(false, 451, 1150)==0xff999999 and 
			 getcolor(false, 585, 1161)==0xfffcfcfc  then
			 print('detect weixin tab screen')
			 -- click first column
			 shell.inputTap(255, 151)
			 shell.sleep(3)
			 shell.inputKey(keycode.KEYCODE_BACK)
			 shell.sleep(1)
			 shell.inputTap(627, 1161)
			 done = true
			 break
	    elseif getcolor(false, 361, 175)==0xff646464 and  getcolor(false, 352, 354)==0xff646464 and
			 getcolor(false, 403, 288)==0xfff86161  and getcolor(false, 406, 340)==0xffffffff and 
			 getcolor(false, 363, 544)==0xff45c01a  and getcolor(false, 211, 583)==0xfff1f1f1 then
			 print('detect after signin require sms verify on new phone screen')
			 -- click first column
			 shell.inputTap(363, 525)
			 shell.sleep(3)
			 for i=1,3 do
			 	local success, number, code = recvsms('',60,'(腾讯科技)','验证码([0-9]{6})')
			 	if success then
					 print('recvsms: '..tostring(number)..','..tostring(code))
					 shell.inputTap(360,250)
			 		 shell.inputText(code)
			 		 shell.inputTap(670,72)
			 		 shell.sleep(8)
			 		 shell.inputTap(432,712)
			 		 break
			 	else
                     shell.sleep(10)
                     shell.inputTap(358,322)
				end
			 end
			 done = true
			 break
	    else
	    	-- do nothing
	    	print('detect nothing: '..tostring(idle_loop)) 
	    	idle_loop = idle_loop+1
		end
		shell.sleep(2)
	end
	
    -- notify server signup finished
	if done then 
		url= "http://120.26.129.37:8060/misc/sumitwxdata"
		local param = 'imsi='..imsi..'&weixin_id='..weixin_id
		if status == 0 then
            param = param..'&type=1'
		else
            param = param..'&type=2'
		end
		body, cookie, code, header = http.post(url, param)
		if not body then
			return false, 'http.post sumitwxdata failed: code='..code
		end
		info = json.decode(body)
		if not info or not info['errorNo'] or info['errorNo'] ~= 0  then
			return false, 'http.post sumitwxdata failed: body='..body
		end

		-- quit weixin, click me
		shell.inputTap(631, 1164)
		shell.sleep(1)
		-- click setup
		shell.inputTap(141, 700)
		shell.sleep(1)
		-- click setup->exit
		shell.inputTap(133, 708)
		shell.sleep(1)
		-- click setup->exit->confirm exit account
		shell.inputTap(307, 580)
		shell.sleep(1)
		-- click setup->exit->confirm exit account->confirm again
		shell.inputTap(530, 690)
		shell.sleep(3)
	end

	print ('done='..tostring(done))
	shell.startApp('com.example.autobot', '.activity.MainActivity')
    
	return done
end

return_status, return_result = test_weixin()


