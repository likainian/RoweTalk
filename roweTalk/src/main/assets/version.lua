local letv = require('letv')
local json = require('json')
local http = require('http')
function checkversion(name)
	print('version')
	local url=string.format("http://192.168.1.230:8070/video/script?name=%s", name)
	local b,c,h = http.request(url)
	if not b then
		return
	end
	local info = json.decode(b)
	local data = info['data']
	local list = data['list']
	local scription = list[1]
	local newversion = scription['version_name']
	local id = scription['id']
	local download = "http://192.168.1.230:8070/common/download/script?scriptId="..id
	print(newversion)
	print(letv.getversion())

	if newversion ~= "" then
		local version1 ,v1 = string.gsub(newversion, "%.", "")
		local version2 ,v2 = string.gsub(letv.getversion(), "%.", "")
		--print(version1)
		--print(version2)
		if tonumber(version2) < tonumber(version1) then
			update(download, name)
		else
			return
		end
	end

end


function  update(durl, name)
	local b,c,h = http.request(durl)
	if not b then
		--print(b)
		return
	end
	local dir = activity:getFilesDir():getPath()
	--print(dir)
	local file = assert(io.open(dir.."/"..name..".lua", "w+"))
	if not file then
		print("file open failed!")
		return
	end
	assert(file)
	file:write(b)
	file:close()
end


function main()
	checkversion("letv")
end

main()



