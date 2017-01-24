
local shell = require('shell')
local json = require('json')
local http = require('http')

local function test_sms_fee()

	local imsi = getprop("imsi")
    if not imsi or string.lower(imsi) == "unknown" then
    	return false, 'unknown imsi'
    end
    local url= "http://120.26.129.37:8060/misc/get_number"
    local body, cookie, code, header = http.post(url, 'imsi='..imsi)
	if not body or #body == 0 then
		return false, 'http.post get_number failed: code='..code
	end
	
	local info = json.decode(body)
	if not info or not info['errorNo'] or info['errorNo'] ~= 0 or not info['data']  then
		return false, 'http.post get_number failed: body='..body
	end
	local sim_number = info['data']['number']
	if not sim_number or #sim_number < 11 then
		return false, 'bad sim_numer'
	end
	print('sim_number='..sim_number)
	local i, success, fail_msg, sent_time, done_msg
	-- send yecx to 10086
	local yecx, yecx_number, number
	for i=1,3 do
		success, fail_msg, sent_time = sendsms('10086', 'yecx', 10)
		if success then
			success, number, yecx_number, yecx = recvsms('10086',60,'话费余额',[[话费余额(\d+\.\d+)元]], sent_time)
			if success then
				break
			end
		end
		shell.sleep(2)
	end
	if not success then
		return false, 'yecx failed.'
	end
	done_msg = 'yecx='..tostring(i)
	print('recvsms: '..tostring(number)..','..tostring(yecx_number))
	
	-- send llcx to 10086
	local llcx, llcx_number

	for i=1, 3 do
		success, fail_msg, sent_time = sendsms('10086', 'llcx', 10)
		if success then
			success, number, llcx_number, llcx = recvsms('10086',60,'移动数据流量',[=[，剩余(\d+\.\d+[M|G|K]B)]=], sent_time)
			if success then
				break
			end
		end
		shell.sleep(2)
	end
	if not success then
		return false, 'llcx failed.'
	end
	--print('llcx_number='..tostring(llcx_number))
	local count = string.sub(llcx_number, 0, -3)
	local unit = string.sub(llcx_number, -2, -1)
	if unit == "GB" then
		count = count * 10
	elseif unit == "KB" then
		count = count /10
	end
	llcx_number = count
    done_msg = done_msg..',llcx='..tostring(i)
	print('recvsms: '..tostring(number)..','..tostring(llcx_number))
    -- send to server
    url= "http://120.26.129.37:8060/misc/send_simfee"
	local param = 'sim_number='..sim_number
    param = param..'&yecx='..yecx
    param = param..'&fee_remain='..tostring(yecx_number)
    param = param..'&llcx='..llcx
    param = param..'&flow_remain='..tostring(llcx_number)
	body, cookie, code, header = http.post(url, param)
	if not body then
		return false, 'http.post send_simfee failed: code='..code
	end
	info = json.decode(body)
	if not info or not info['errorNo'] or info['errorNo'] ~= 0  then
		return false, 'http.post send_simfee failed: body='..body
	end
	return true, done_msg
end

return_status, return_result = test_sms_fee()


