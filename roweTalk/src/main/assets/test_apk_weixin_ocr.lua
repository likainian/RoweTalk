
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

    --
    if status ~= 0 then
    	return true, 'signuped already.'
    end

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
    local success, fail_msg

    while fail_count < 10 and idle_loop<10 do 
		success, fail_msg = ocr(true) 
	    if not success then
			print('ocr failed: '..tostring(fail_msg))
			fail_count = fail_count + 1
		elseif ocr(false, 0xff00070d, 379, 165) and  ocr(false, 0xff94aabf, 363, 603) and 
			 ocr(false, 0xff000c15, 121, 1144) and ocr(false, 0xff000c15, 597, 1138) then	
			 -- splash earth screen
		     print('detect 微信地球 界面')
		elseif ocr(false, 0xff1aad19, 72,1128, true) and ocr(false, '登录', 94, 1125, 153, 1157)  then	
			-- signup or signin page
		    print('detect 登录 or 注册 选择界面')
		    if status == 0 then
			  shell.inputTap(597, 1138)
			else
			  shell.inputTap(120, 1138)
			end
		elseif ocr(false, 0xff393a3f,  336, 69) and ocr(false, '用手机号登录', 119, 56, 283, 89) then	
			-- signin page
			print('detect 登录 界面')
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
	    elseif ocr(false, 0xff1c1d1f,  336, 69) and  ocr(false, '谁在使用', 341, 582, 438, 610)  then
			 print('detect 登录后提示：看看谁在使用微信？ 提示框')
			 shell.inputTap(430, 710)

	    elseif ocr(false, 0xff393a3f,  336, 69) and ocr(false, '填写手机号', 88, 55, 230, 89) then	
			-- signup page
			print('detect 注册 界面')
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
			local success, number, code = recvsms('',60,'(腾讯科技)','([0-9]{6})')
			if success then
				print('recvsms: '..tostring(number)..','..tostring(code))
				sms_code = code
			end
		elseif ocr(false, 0xff393a3f,  336, 69) and ocr(false, '验证手机号', 148, 490, 300, 525) then
		     print('detect verify cell number screen')
        elseif ocr(false, 0xff393a3f,  336, 69) and ocr(false, '填写验证码', 89, 56, 229, 88) then
			-- input sms page
			if status == 1  then
				print('detect 登录过程的 填写验证码 界面 '..tostring(sms_code))
			else
				print('detect 注册过程的 填写验证码 界面')
			end
		 	if not sms_code then
		 		if status == 1 and ocr(false, '重发验证码', 291, 310, 428, 342) then
		 			shell.inputTap(370,330)
		 			local succ, number, code = recvsms('',60,'(腾讯科技)','([0-9]{6})')
		 			if succ then
						print('recvsms: '..tostring(number)..','..tostring(code))
						sms_code = code
					end
		 		else
					if succ then
						print('recvsms: '..tostring(number)..','..tostring(code))
						sms_code = code
					else 
						shell.inputKey(keycode.KEYCODE_BACK)
				        shell.inputTap(502,690)
					end
				end
		 	end
		 	if sms_code then
		 		if status == 1 then 
		 			shell.inputTap(360,240)
		 			shell.inputText(sms_code)
		 			shell.inputTap(670,74)
		 			sms_code = nil
		 		else 
			 		shell.inputTap(280,308)
			 		shell.inputText(sms_code)
			 		shell.inputTap(351,486)
		 		end
		 	end
		elseif ocr(false, 0xff1c1d1f,  336, 69) and ocr(false, '验证码不正确', 147, 541, 296, 570)  then
			-- 注册流程提示验证码不正确提示框
			 print('detect 注册流程验证码不正确 提示框')
			 shell.inputTap(528, 690)
			 shell.inputKey(keycode.KEYCODE_BACK)
			
		elseif ocr(false, 0xff393a3f,  336, 69) and  ocr(false, '不是我的', 236, 512, 348, 543)  then
			-- 是我的，立刻登陆； 不是我的，下一步
			print('detect 注册流程账号确认 界面')
			if ocr(false, nickName, 277, 277, 414, 309)  then
				-- 是我的，立刻登陆
                shell.inputTap(369, 439)
			else
				-- 不是我的，下一步
				shell.inputTap(358, 528)
			end
		elseif ocr(false, 0xff1c1d1f,  336, 69) and ocr(false, '注册失败', 149, 531, 271, 566)  then
	    	 -- 密码必须是8到16位数字，字符组合（不能是纯数字)
			 print('detect 注册失败 提示框')
			 shell.inputTap(525, 701)
			 shell.inputKey(keycode.KEYCODE_BACK)
		elseif ocr(false, 0xff1c1d1f,  336, 69) and ocr(false, '该账号长', 148, 541, 247, 570)  then
	    	 -- 该账号长期未登陆，为保护账号安全，系统自动将其置为保护状态，点击确定按钮可立即解除保护状态
			 print('detect 注册流程 账号被保护 提示框')
			 shell.inputTap(528, 690)

	    elseif ocr(false, 0xff1c1d1f,  336, 69) and ocr(false, '密码错误', 198, 542, 320, 570)  then
	    	 -- 账号或者密码错误，请重新填写
			 print('detect 登录输错密码 提示框')
			 shell.inputTap(530, 690)
			 done = false
			 break
		elseif ocr(false, 0xff393a3f,  336, 69) and  ocr(false, '手机不在', 265, 593, 375, 626)  then
	    	 -- 通过短信验证身份，手机不在身边？
			 print('detect 验证手机号 界面')
			 shell.inputTap(376, 522)
			 local success, number, code = recvsms('',60,'(腾讯科技)','([0-9]{6})')
			 if success then
				print('recvsms: '..tostring(number)..','..tostring(code))
				sms_code = code
			 end
	    elseif ocr(false, 0xff393a3f,  336, 69) and  ocr(false, '自助解封', 88, 53, 203, 90)  then
			 print('detect 自助解封 界面')
			 done = false
			 fail_msg = '账号被封锁'
			 break
		elseif ocr(false, 0xff393a3f,  336, 69) and  ocr(false, '找朋友', 88, 53, 175, 90)  then
			 print('detect 注册完成 界面')
			 shell.inputTap(525, 724)
			 done = true
			 shell.sleep(3)
			 break
	    elseif ocr(false, 0xff10aeff,  39, 498, true) and ocr(false, '钱包', 90, 485, 143, 512)  then
			 print('detect 微信主界面 我标签页')
			 shell.sleep(3)
			 shell.inputTap(85, 1162)
			 done = true
			 break
	    elseif ocr(false, 0xffffffff, 28, 73) and  ocr(false, 0xffffffff, 187, 73) and
			 ocr(false, 0xff45c01a, 136, 442)  and ocr(false, 0xfff1f1f1, 583, 529) then
			 print('detect confirm continue signup screen')
			 shell.sleep(3)
			 shell.inputTap(85, 1162)
			 done = true
			 break
	    elseif ocr(false, 0xff46c01b,  87, 1152, true) and ocr(false, '微信', 68, 1175, 109, 1200)  then
			 print('detect 微信主界面 微信标签页')
			 -- click first column
			 shell.inputTap(255, 151)
			 shell.sleep(3)
			 shell.inputKey(keycode.KEYCODE_BACK)
			 shell.sleep(1)
			 shell.inputTap(627, 1161)
			 done = true
			 break
	    elseif ocr(false, 0xff646464, 361, 175) and  ocr(false, 0xff646464, 352, 354) and
			 ocr(false, 0xfff86161, 403, 288)  and ocr(false, 0xffffffff, 406, 340) and 
			 ocr(false, 0xff45c01a, 363, 544)  and ocr(false, 0xfff1f1f1, 211, 583) then
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
	    elseif ocr(false, 0xff222222, 666, 78) and  ocr(false, '选择蓝牙', 40, 155, 128, 183)  then
			 print('detect RoweTalk screen, test app crashed, restart app')
			 shell.clearApp(package_name)
	         shell.startApp(package_name, main_acitivity)
	         shell.sleep(12)
	         fail_count = fail_count+1
	    else
	    	-- do nothing
	    	print('detect nothing: '..tostring(idle_loop)) 
	    	-- save ocr failed images
	    	ocr(false, false, package_name)
	    	idle_loop = idle_loop+1
		end
		shell.sleep(3)
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
	shell.startApp('com.example.rowetalk2', 'com.example.rowetalk.activity.MainActivity')
    
	return done,fail_msg
end

return_status, return_result = test_weixin()


