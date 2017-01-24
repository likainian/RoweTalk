
local json = require('json')
local http = require('http')
function test()
	--print('sendsms: number: 10086, content: LLCX')
	sendsms(10086, 'LLCX', 5000)

end
test()


