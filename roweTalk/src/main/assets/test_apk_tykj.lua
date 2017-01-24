local shell = require('shell')
local math = require('math')

local function test_tykj()
	local package_name = 'com.eshore.ezone'
	local main_acitivity = 'com.eshore.ezone.StartActivity'
	local b = shell.checkApp(package_name)
	if not b then
		return false, 'package not exist'
	end
	shell.killApp(package_name)
	--shell.clearApp(package_name)
	shell.startApp(package_name, main_acitivity)
	shell.sleep(3)

    local w = 720
    local h = 1280
    local pts = {}
    local xstart = 0
    local ystart = 0
    local wstep = math.floor(w/4)
    local hstep = math.floor(h/16)
    local x = xstart+wstep
    local y = ystart+hstep

    for i=1,15,1 do
    	x = xstart+wstep
    	for j=1,3,1 do
    		table.insert(pts, x)
    		table.insert(pts, y)
    		x = x + wstep
    	end
    	y = y + hstep
    end

    local fail_count = 0
    local full_detect = true
    local bar_tap = 0
    local idle_loop = 0
    local click_count = 0

    print('test begin')

    while fail_count < 100 and idle_loop<5 do 
		local success, c1,  c2,  c3,  c4,  c5,  c6,  c7,  c8,  c9,  c10, 
                   c11, c12, c13, c14, c15, c16, c17, c18, c19, c20, 
                   c21, c22, c23, c24, c25, c26, c27, c28, c29, c30, 
                   c31, c32, c33, c34, c35, c36, c37, c38, c39, c40, 
                   c41, c42, c43, c44, c45
                   = getcolor(true, unpack(pts)) 
	    if not success then
			print('getColor failed: '..tostring(c1))
			fail_count = fail_count + 1
		elseif c34==4281637603 and c36==4281637603 and c38==4294967295 and 
			c41==4294967295 and c43==4294967295 and c45==4294967295 then
			-- connect confirm after intro page3
			shell.inputTap(168, 1168)
		elseif c1==4281637603 and c3==4281637603 and c5==4294967295 and
			c40==4294967295 and c42==4293980400 and c45==4284506208 then
			-- 'necessary popup window'
			shell.inputTap(636, 117)
		elseif c4==4294041269 and c6==4294041011 and c17==4294306494 and 
			c29==4294437568 and c37==4294566051 and c42==4293711781 then
		   -- intro page 1
		   print('detect intro1')
		   shell.inputSwipLeft()
		elseif c4==4294041269 and c6==4294041011 and c17==4294306494 and 
			c29==4294437568 and c37==4294566051 and c42==4293711781 then
		   -- intro page 2
		   print('detect intro2')
		   shell.inputSwipLeft()
		elseif c4==4294041269 and c6==4294041011 and c17==4294306494 and 
			c29==4294437568 and c37==4294566051 and c42==4293711781 then
		   -- intro page 3
		   print('detect intro3')
		   shell.inputTap(360, 1111)
		elseif c4==4294041269 and c6==4294041010 and c17==4294306494 and 
			c29==4294437568 and c37==4294566052 and c42==4293711781 then
		   -- popup window for app recommends
		   print('detect pop window: app recommends')
		   shell.inputTap(637, 117)
		elseif c7==4294967295 and c8==4293388263 and c9==4294967295 and
			c29==4294967295 and c43==4293980400 and c45==4293980400 then
			-- main menu recomment, tap category
		    print('detect mainmenu: recommend')
			shell.inputTap(219, 1165)
		elseif c1==4294967295 and  c2==4294967295 and c3==4294967295 and 
			 c43==4293980400 and  c45==4293980400 then
			 -- main menu category, tap toplist
		     print('detect mainmenu: others '..tostring(bar_tap))
			 if bar_tap == 0 then 
			 	shell.inputTap(219, 1165)
			 	bar_tap = bar_tap + 1
			 elseif bar_tap == 1 then
			 	shell.inputTap(360, 1165)
			 	bar_tap = bar_tap + 1
			 elseif bar_tap == 2 then
			 	shell.inputTap(504, 1165)
			 	bar_tap = bar_tap + 1
			 elseif bar_tap == 3 then
			 	shell.inputTap(650, 1165)
			 	bar_tap = bar_tap + 1
			 elseif bar_tap == 4 then
			 	shell.inputTap(75, 1165)
			 	bar_tap = 0
			 	click_count = click_count+1
			 	if click_count > 2 then 
			 		break
			 	end 
			 end
	    elseif getcolor(false, 178,516) == 0xffffffff and getcolor(false, 358,531) == 0xff282828 and
	    	   getcolor(false, 192,580) == 0xff33b5e5 and getcolor(false, 507,580) == 0xff33b5e5 and
	    	   getcolor(false, 219,688) == 0xff474747 and getcolor(false, 360,745) == 0xff474747 and
	    	   getcolor(false, 327,735) == 0xff282828 then
	    	   print('detect operator popup window')
			   shell.inputTap(472, 727)
		elseif getcolor(false, 312,514) == 0xff115d98 and getcolor(false, 553,528) == 0xff165d9e and
               getcolor(false, 180,504) == 0xfffa9a24 and getcolor(false, 657,1089) == 0xffffffff and
	    	   getcolor(false, 204,1138) == 0xff1067a8 then
	    	   print('detect preenter page')
			   shell.inputTap(657, 1090)
	    else
	    	-- do nothing
	    	print('detect nothing: '..tostring(idle_loop)) 
	    	idle_loop = idle_loop+1
		end
		shell.sleep(3)
	end
	print ('done')

	return true, 'test done'
end

return_status, return_result = test_tykj()


