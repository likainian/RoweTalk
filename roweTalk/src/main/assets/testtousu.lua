
local json = require('json')
local http = require('http')

local function test()
	local url= "http://serv.newmobo.com:4002/api/unspv1a/post"
	--print('test url: '..url)
	local imei = getprop("imei")
	if not imei or string.lower(imei) == "unknown" then
		imei = '359372041354556'
	end
    local imsi = getprop("imsi")
    if not imsi or string.lower(imsi) == "unknown" then
    	return false, 'unknown imsi'
    end
	local data = {["api"]="mgdm-certk",
		  ["ppid"]="PPFCJJLKC1_001",
		  ["imsi"]=imsi,
		  ["imei"]=imei,
		  ["autotime"]=30}
	local postdata = json.encode(data)
	local headers = {
		["Content-Type"] = "application/json"
		}
	--print ("postdata="..postdata)
	local body, cookie, code, header = http.post_with_header(url, postdata, headers)
	--local body, cookie, code, header = http.post(url)
	if not body then
		return false, 'bad request: code='..code
	end
	--print('body='..body)
	local info = json.decode(body)
	if info and info["status"]==100 and info["certsms"] and #info["certsms"]>0 then
		return sendsms(1065842230, info["certsms"], 5000)
	else
		return false, 'unknown response'..body
	end
end

return_status, return_result = test()


