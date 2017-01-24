
local json = require('json')
local http = require('http')
local function test()
	local url= "http://120.26.129.37:8060/misc/test"
	print('test url: '..url)
	local body, cookie, code, header = http.post(url)
	if not body then
		print('bad request: code='..code)
		return
	end
	--local info = json.decode(b)
	print('body: '..body)
	return false, 'kaka'
end
return_status, return_result = test()


