greet = {}
function greet.hello(name)
	local imei = getprop("imei")
	print('imei:'..imei)
	local imsi = getprop("imsi")
	print('imsi:'..imsi)
	--print('Hello '..name..'!')
end
