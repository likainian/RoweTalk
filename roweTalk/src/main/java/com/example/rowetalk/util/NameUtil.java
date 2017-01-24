package com.example.rowetalk.util;

import java.util.Random;
import java.util.regex.Pattern;

public class NameUtil {

	private static final String[] baijiaxing = new String[] {
			"赵","钱","孙","李","周","吴","郑","王",
			"冯","陈","楮","卫","蒋","沈","韩","杨",
			"朱","秦","尤","许","何","吕","施","张",
			"孔","曹","严","华","金","魏","陶","姜",
			"赵","钱","孙","李","周","吴","郑","王",
			"冯","陈","楮","卫","蒋","沈","韩","杨",
			"朱","秦","尤","许","何","吕","施","张",
			"孔","曹","严","华","金","魏","陶","姜",
			"戚","谢","邹","喻","柏","水","窦","章",
			"云","苏","潘","葛","奚","范","彭","郎",
			"鲁","韦","昌","马","苗","凤","花","方",
			
			"赵","钱","孙","李","周","吴","郑","王",
			"冯","陈","楮","卫","蒋","沈","韩","杨",
			"朱","秦","尤","许","何","吕","施","张",
			"孔","曹","严","华","金","魏","陶","姜",
			"赵","钱","孙","李","周","吴","郑","王",
			"冯","陈","楮","卫","蒋","沈","韩","杨",
			"朱","秦","尤","许","何","吕","施","张",
			"孔","曹","严","华","金","魏","陶","姜",
			"戚","谢","邹","喻","柏","水","窦","章",
			"云","苏","潘","葛","奚","范","彭","郎",
			"鲁","韦","昌","马","苗","凤","花","方",
			
			
			"赵","钱","孙","李","周","吴","郑","王",
			"冯","陈","楮","卫","蒋","沈","韩","杨",
			"朱","秦","尤","许","何","吕","施","张",
			"孔","曹","严","华","金","魏","陶","姜",
			"赵","钱","孙","李","周","吴","郑","王",
			"冯","陈","楮","卫","蒋","沈","韩","杨",
			"朱","秦","尤","许","何","吕","施","张",
			"孔","曹","严","华","金","魏","陶","姜",
			"戚","谢","邹","喻","柏","水","窦","章",
			"云","苏","潘","葛","奚","范","彭","郎",
			"鲁","韦","昌","马","苗","凤","花","方",
			
			
			"俞","任","袁","柳",/*"酆",*/"鲍","史","唐",
			"费","廉","岑","薛","雷","贺","倪","汤",
			/*"滕",*/"殷","罗","毕","郝","邬","安","常",
			"乐","于","时","傅","皮","卞","齐","康",
			"伍","余","元","卜","顾","孟","平","黄",
			"和","穆","萧","尹","姚","邵","湛","汪",
			"祁","毛","禹","狄","米","贝","明","臧",
			"计","伏","成","戴","谈","宋","茅","庞",
			"熊","纪","舒","屈","项","祝","董","梁",
			"杜","阮","蓝","闽","席","季","麻","强",
			"贾","路","娄","危","江","童","颜","郭",
			"梅","盛","林","刁",/*"锺",*/"徐","丘","骆",
			"高","夏","蔡","田","樊","胡","凌","霍",
			"虞","万","支","柯",/*"昝",*/"管","卢","莫",
			"经","房","裘","缪","干","解","应","宗",
			"丁","宣","贲","邓","郁","单","杭","洪",
			"包","诸","左","石","崔","吉","钮","龚",
			"程","嵇","邢","滑","裴","陆","荣","翁",
			"荀","羊","於","惠","甄",/*"麹",*/"家","封",
			"芮","羿","储","靳",/*"汲","邴","糜",*/"松",
			"井","段","富","巫","乌","焦","巴","弓",
			"牧",/*"隗",*/"山","谷","车","侯",/*"宓","蓬",*/
			"全",/*"郗",*/"班","仰","秋","仲","伊","宫",
			"宁","仇","栾","暴","甘","斜","厉","戎",
			"祖","武","符","刘","景","詹","束","龙",
			"叶","幸","司","韶","郜","黎","蓟","薄",
			"印","宿","白","怀","蒲","邰","从","鄂",
			"索","咸",/*"籍",*/"赖","卓","蔺","屠","蒙",
			"池","乔",/*"阴",*/"郁",/*"胥",*/"能","苍","双",
			"闻","莘","党","翟","谭","贡","劳","逄",
			"姬","申","扶","堵","冉","宰","郦","雍",
			"郤",/*"璩",*/"桑","桂","濮","牛","寿","通",
			"边","扈","燕","冀","郏","浦","尚","农",
			"温","别","庄","晏","柴","瞿","阎","充",
			"慕","连","茹","习","宦","艾","鱼","容",
			"向","古","易","慎","戈","廖","庾","终",
			/*"暨",*/"居","衡","步","都","耿","满","弘",
			"匡","国","文","寇","广","禄","阙","东",
			"欧",/*"殳",*/"沃","利","蔚","越",/*"夔",*/"隆",
			"师","巩",/*"厍",*/"聂","晁","勾","敖","融",
			"冷",/*"訾",*/"辛","阚","那","简","饶","空",
			"曾",/*"毋",*/"沙",/*"乜","养",*/"鞠","须","丰",
			"巢","关",/*"蒯",*/"相","查","后","荆","红",
			"游","竺","权",/*"逑",*/"盖","益","桓","公",
			"万俟","司马","上官","欧阳",
			"夏侯","诸葛","闻人","东方",
			"赫连","皇甫","尉迟","公羊",
			"澹台","公冶","宗政","濮阳",
			"淳于","单于","太叔","申屠",
			"公孙","仲孙","轩辕","令狐",
			/*"锺离",*/"宇文","长孙","慕容",
			"鲜于","闾丘","司徒","司空",
			/*"丌官","司寇","仉","督","子车",*/
			"颛孙","端木","巫马","公西",
			/*"漆雕",*/"乐正",/*"壤驷",*/"公良",
			"拓拔","夹谷","宰父","谷梁",
			"晋","楚","阎","法","汝",/*"鄢",*/"涂","钦",
			"段干","百里","东郭","南门",
			"呼延","归","海","羊舌","微生",
			"岳","帅",/*"缑",*/"亢","况","后","有","琴",
			"梁丘","左丘","东门","西门",
			"商","牟","佘","佴","伯","赏","南宫",
			"墨","哈",/*"谯","笪",*/"年","爱","阳","佟",
	};
	
	private static final String[] male_names = new String[] {
			"伟","伟","刚","勇","毅","俊","峰","强","军","平","东","文","辉","力","明","永","健","世","广","义",
			"兴","良","海","山","仁","波","宁","贵","福","生","龙","元","全","国","胜","学","祥","才","发利","清","飞","彬","富","顺","信","子","杰","涛","昌","成","康","星","光","天","达","安","岩",
			"林","有","坚","和","彪","博","诚","先","敬","震","振","壮","会","思","豪","心","邦","承","乐",
			"绍","功","松","厚","庆","磊","民","友","裕","河","哲","江","超","浩","亮","政","谦","亨","固",
			"轮","翰","朗","伯","宏","言","若","鸣","朋","斌","梁","栋","维","启","克","伦","翔","旭","皓",
			"晨","辰","士","建","家","致","炎","德","行","时","泰","盛","雄","琛","钧","冠","策","腾","弘",
			"志","武","中","榕","奇","鹏","楠","泽","风","茂","航。伍","亘","好","休","亦","次","守",
			"旭","宇","吉","兆","仰","向","至","共","仲","舟","再","存","先",
			"争","光","冲","丞","列","寺","旬","灰","旨","朱","任","艮","百","同","伊","州",
			"安","印","全","牟","朽","圳","江","汛","竹","如","仿","合","西","竹","曲","夷",
			"夙","灯","伎","伉","羽","后","名","回","因","多","帆"
	};
	private static final String[] female_names = new String[] {
			"仲媛","仲媛","湘怡","颜英","真文","怡丞","秀琴","滇萍","青蓉","妍羽","洁玲","雨蓉","胜红","慧琴","道芳","辰蓉",
			   "贞颖","莉萍","正妍","琳竣","梦丽","若美","娅清","舒玉","映蓉","长英","丽美","夕文","琳涵","燕星","善玲",
			   "宇芳","铭倩","琴子","筱雪","旭妍","蝾婷","怡婷","莉颖","芹悦","悦","芝蓉","娅庭","树艳","小霞","厦洁","卷玉",
			   "欣颖","榕嫣","晓悦","妍婷","帮琼","昱瑛","沂秀","祖萍","人萍","菊霞","雨琼","瑞娟","丽芳","丽瑶","国娟","友梅",
			   "曹文","玉颖","妍如","燕芷","思丽","翠玉","庆霞","塔娜","雪芬","秀秀","书怡","水琼","梦茹","国英","玉萍",
			   "新妹","银玲","文霞","浩文","小芳","秋丽","春英","大梅","羽洁","小洁","慧瑶","芝芳","美玲","长文","应艳",
			   "琳爰","君茹","莉娉","纯雪","洲裴","志丽","世悦","艳蓉","丹洁","玲娇","卓妍","杭英","玲丽","婷雯","梓悦",
			   "秀娟","梓婧","雯婧","瑶华","翠玲","锦婷","永茹","红琼","睿颖","炜琳","雨莹","炜婷","丽英","忠燕","瑞芬",
			   "馨婷","连英","秀媛","雨花","秀珊","欣怡","思艳","紫颖","芬璇","婧文","惠芳","炎琳","秀颖","翠萍","爱茹",
			   "艳霞","龙艳","嫣钰","志芳","芷茹","悦颖","红英","焱霞","煊悦","海萍","隅晖","金燕","薇颖","媛媛","丽霞",
			   "悦淇","安娜","媛娜","杰文","冬梅","晓燕","向莉","怡瑶","小芬","嘉怡","国琴","青霞","婷秀","惠玲","于娜",
			   "沁媛","诺瑶","梓燕","秀君","昊怡","小妍","悦张","秀芳","欣怡","晓梅","午瑶","亚萍","丽莉","桂霞","会霞",
			   "龙梅","琳敏","展文","羽莹","艺萍","月玲","歌玲","海燕","晶婧","尤文","仪琳","玉娟","钰洁","蓉","令红","梅霞",
			   "智琳","新芳","潜","英豪","科翰","千怡","春嫣","伟娜","园媛","成娥","宇丞","欣秀","锦洁","太文","胜怡","欣玉",
			   "美玲","雪娟","卿蓉","晓洁","家颖","安茹","湘媛","丹梅","薏冉","婷昱","怡可","琳淼","卓婷","瓶花","爱萍",
			   "雅莉","彩燕","思莹","桂燕","石英","腊梅","芬","莉娅","娟娣","艳红","棠莉","悦驰","婉婷","嘉洁","彩红","媛雪",
			   "美芳","束芳","淑琳","若瑶","莉轩","燕齐","昕妍","倡文","紊文","子婧","爱琴","维娟","思娜","振文","鸾瑶",
			   "玲丽","旦娅","苏娟","宝琳","群芳","羽洁","秋艳","建颖","泓茹","富霞","倩成","诗茹","欣瑶","曦秀","婷丽",
			   "莉娜","东玲","巧娜","佳艳","蓓莉","纪颖","艳红","有芳","专霞","巧茹","绪婷","石瑶","玉妍","佳洁","紫霞",
			   "雨茹","甫文","怡金","锐洁","悦书","粟梅","雄英","京艳","秀秀","新颖","依娜","欣瑶","梦洁","菁茹","泽芳",
			   "怡若","陈红","婧宁","美怡","悦帆","莹莹","莉绫","红丽","昕怡","德梅","燕萍","瑛蔓","鹤梅","蓉华","佳莉",
			   "蔡琳","婧妍","斯玉","恺玲","珂妍","小莉","成美","倩冰","巧玲","妃妍","小雪","柯洁","育梅","洁莹","云琼",
			   "睿瑶","商丽","亚玲","优美","长丽","瑜文","姝瑶","春颖","华琳","妍婷","欣琳","熙玉","梦洁","刖颖","慧玲",
			   "婧芸","光芳","婧玟","雯琴","红美","睿颖","葱娟","方玉","锦文","晓芬","雅丽","际红","悦玮","彦瑛","娉娟",
			   "燕艳","妍青","静美","禾悦","妍凌","洁","惋洁","一芳","立芬","平霞","琴芬","芳媛","芷秀","翠玲","贤琴","采艳",
			   "怡翎","裕梅","玲芬","宣艳","冠芳","勤琼","国萍","承文","雪芬","妍玲","晓霞","依婷","瑾琳","永琴","荇怡",
			   "惠娅","进芳","翰颖","婉燕","景文","赛玉","晓玉","晋玉","幸瑶","歆琳","曾燕","国政","彩娟","三婷","雪婧",
			   "义文","学英","英莉","尔悦","姝媛","经文","姝妍","开琼","诗芳","金梅","霞","菊芬","秀如","则悦","楚冉","桂芳",
			   "昱文","俊英","凤婷","雯丽","茹婷","雪萍","津文","琳秀","燕明","纯洁","素莉","艳玲","悦儿","春文","江红",
			   "李媛","木蓉","照红","鸿洁","宣蓉","娟","亚梅","泽英","筱雪","秀英","紫玲","崇芳","芸英","艾玲","红梅","小妹",
			   "惠琴","玲玉","文英","柏文","继红","娅彤","保哲","苑洁","瑞艳","玲玉","辰雪","瑜莉","兰娟","蓉洁","金文","伟娜",
			   "品怡","钊莹","嫦娥","路英","婉怡","冬雪","义茹","章文","茹芸","洁","俞莹","生文","八雪","涟颖","媛媛","燕平",
			   "恬萍","暖怡","晴芳","瑶琴","荣霞","梓婷","莹","韧颖","琬莹","知燕","婵","星颖","燕晓","杨梅","玉娟","泽瑛",
			   "妍彤","婧雯","奚瑶","天洁","滢莹","芸莹","倩愉","贝琳","秋怡","曼玲","林娜","依玲","常红","琴轩","景怡","琬玲",
			   "妍茜","影梅","光文","立婷","琳","悦婷","婷旭","心悦","肖倩","清茹","姗婷","春霞","超莹","宇玲","春梅","安娅",
			   "列琴","汉玲","泽琴","玲羽","燕肖","润洁","秉文","高妍","睿琳","悦营",
			"寄梅","代薇","南青","映冬","巧安","醉琴","幼儿","夏曼","幼彤","诗莲","青丹","若巧","山荷","惜旋","含芹","初云","笑丹",
			"含蓝","诗寒","芷容","亦旋","怀玉","惜蓉","春梅","冷巧","凌香","恨巧","访双","沛露","念云","梦蓉","醉蕊","友安","念露",
			"如萍","白蕾","映薇","春荷","绿秋","惜阳"," 寻翠","尔蕾","半真","寒香","寄桃","乐莲","之枫","晓玉","宛容","惜卉","元秋",
			"初彤","以风","醉云","惜荷","映蕾","雅菡","千凡","含双","乐丝","千儿","凝晴","天霜","谷梦"," 亦霜","诗白","问珍","水菡",
			"青文","语双","醉桃","访薇","秋菱","半旋","问香","冷菡","夜岚","春露","曼南","晓儿","白珍","安凡","灵菡","宛阳","涵蓉",
			"雨安","痴秋","雁槐"," 元芙","问彤","谷岚","代槐","迎芙","雅云","半凡","又文","春蕾","青风","芷柏","采海","雁萍","水枫",
			"雪珊","雅蕾","念兰","丹文","梦天","听容","绮旋","春安","秋槐","觅丝"," 恨柏","春易","雪阳","代青","夜萍","幼波","问阳",
			"涵丝","友筠","尔荷","凡柔","醉南","海晴","雪萍","涵香","宛霜","痴荷","盼灵","如凝","问雪","凌玉","映香","忆蝶","夜风",
			"代梅","凝蕾","迎菱","雁容","寄海","元南","曼槐","夏春","又旋","香旋","初灵","惜山","以卉","紫荷","凝荷","盼筠","妙蕊",
			"映兰","访枫","易夏","觅筠","惜雁","惜雪","芷白"," 迎芹","从晴","天亦","巧兰","醉海","书巧","绿青","水梅","水丹","恨兰",
			"梦丝","惜菱","新双","访烟","盼槐","冰枫","青槐","寻芹","雁蕊","冷蓝","飞香","凌蝶","雁凝","丹易"," 秋夏","绮寒","醉珊",
			"天蓝","寻翠","青晴","依夏","之文","傲云","雅柔","香儿","丹晴","幼曼","宛香","海槐","慕雪","碧容","凡阳","新之","思雪",
			"凌之","友凝","恨梦","绮丹","小雪","涵梦","问蕊","宛菱","之曼","沛阳","新丝","怜波"," 夜芹","易筠","碧白","白曼","千青",
			"冰枫","寻天","代翠","曼天","水玉","向绿","盼瑶","凝烟","紫彤","曼霜","听夏","碧柔","怀夏","翠兰","山冬","雨珍","靖灵",
			"翠露","尔蕾","千灵","绿珊","思山","凝露","如波","晓白","梦薇","采丹","孤曼","慕白","静云","春巧","访海","问易","孤彤",
			"山芹","静容","依儿","思蕾","妙蓉","寻丹","诗阳","友烟","盼翠","之荷","觅凡","灵文","涵枫","千儿","丹柏","觅梦","念海",
			"新竹","又凝","怀珊","梦巧","凡海","从雪","惜蕾","凝之","夜卉","绿晴","宛彤","山容","春风","映阳","夏柳","紫蓝","笑彤",
			"元蕾","翠柏","书桃","醉烟","友蕾","幼卉","念霜","新卉","惜儿","恨玉","安天","晓安","青兰","从波","孤寒","秋文","晓夏",
			"南薇","凝青","采蝶","之雪","谷风","碧薇","依彤","水琴","亦秋","绮萱","飞梦","傲珍","诗荷","靖芙","冰槐","雨蕾","山梅",
			"雪翠","孤云","凌卉","半巧","寒风","笑柔","飞梦","静蝶","紫凝","冬卉","绿烟","凝彤","元易","寻亦","恨晴","千凝","觅珊",
			"曼珍","映柔","初白","夜云","海天","巧春","冷亦","冰筠","涵天","寒竹","亦蝶","之芙","丹雪","香枫","紫蓝","念槐","迎香",
			"水柔","念波","青枫","巧灵","孤凡","笑露","傲蓉","映蕊","白蕊","灵曼","友云","孤香","忆芹","天菡","曼丝","慕菱","思凝",
			"绿青","晓荷","紫易","采珍","采丝","芷香","白天","觅安","盼梅","灵松","冰松","南露","问香","碧萍","紫筠","安凡","友晴",
			"亦春","凌荷","青之","笑梦","半雪","绮绿","青烟","迎珍","之凡","晓萱","寒梅","如云","翠南","怜蕾","惜霜","迎真","思薇",
			"之柏","冷云","恨芹","碧白","听寒","幻天","谷柔","初卉","水青","问蕊","灵双","寒萱","乐露","芷菱","又露","灵芹","易柔",
			"千寒","元芹","巧南","寄云","妙海","向菱","幻霜","觅凝","笑蕊","翠波","念寒","迎寒","元薇","雅南","涵海","冷玉","谷蓉","以晴",
			"怜丝","从丹","如巧","忆风","醉蝶","秋儿","亦秋","春萍","芷琴","问旋","青露","傲云","幼蝶","水荷","乐蓉","语珍","恨安","冷柔",
			"映之","妙冬","梦柔","怀丝","痴柳","乐凝","丹卉","初梅","凝柳","迎荷","小彤","尔蓉","梦岚","冬亦","飞亦","雅旋","代珍","忆亦",
			"翠彤","千晴","念蕊","易玉","涵玉","初彤","之柳","谷曼","飞桃","平莲","春亦","映荷","雅玉","怜卉","晓薇","尔筠","怜雁","雨天",
			"代冬","飞柏","安曼","小兰","谷蕊","紫雁","白筠","慕萱","恨兰","雪白","若南","静阳","南萱","念蕊","又巧","碧瑶","慕山","绮蕾",
			"紫夏","盼筠","绮芹","寄云","半柔","春彤","靖玉","采山","寒蓉","觅香","笑枫","寻凡","傲南","盼柳","凝云","初晴","怀冬","水蓉",
			"恨雁","山蕊","山云","冰梅","白双","南雪","亦雁","香云","忆蓉","代萱","之亦","雨萱","小之","思凡","向菱","夏槐","水卉","如玉",
			"如双","寒青","代夏","思蝶","元丝","依松","安珍","南荷","又松","妙雪","若香","凡莲","痴珍","半柔","恨槐","宛寒","碧桃","翠风","梦蝶","飞南",
	};	
	
	private static final String other[] = {
			"甲","乙","丙","丁","戊","己","庚","辛","壬","癸",
			"子","丑","寅","卯","辰","巳","午","未","申","酉","戌","亥",
			"金","木","水","火","土"
	};
	
	private static final String net_name[] = {
			//"★文正太浩★",
			"主宰、别样苍茫",
			"蝴蝶为谁开~",
			"爱情的经纬线",
			"无缘的缘分",
			"打开灯光照照暖",
			"逆倒尘光",
			"寻找我（你）的天堂",
			"爱被打了一巴掌",
			"夺爱水果刀",
			"N个网名用不完",
			"斑点水玛线",
			"擦干你那为爱留下的泪",
			"放下你的手￥",
			"扯不断的红尘",
			"找寻你的足迹∞",
			"想你时的孤独。",
			"冷月妖娆 ヽ由命",
			"︶ㄣ那个撕心裂肺的叫声",
			"我坚信，他们不会走远。",
			"月光歌夜美人戏赤木花开",
			"陈年豆蔻，谁许谁地老天荒",
			"情歌谱成一曲思念",
			"那抹温柔演绎的酣畅淋漓",
			"没有温度的路灯提醒着涐",
			"因为抱的太久反而难以承受",
			"涐知道涐只是一厢的情愿。",
			"都说好下个路口就分手。",
			"美人如此多娇",
			"沵永远不知涐是多么的爱沵",
			"若我离去，后会无期。",
			"我算个 what ︶￣",
			"爱后殇ㄋ臫巳",
			"茈籹孓丹 犯贱",
			"、用烟火驱散一季的阴霾",
			"我比任何人都爱自己。",
			"- 扯蛋 丶",
			"// 沵想过我旳感受没有",
			"我只跟自己比",
			"非典式、想你。",
			"#沵旳高傲刺伤了硪旳灵魂",
			"゛我要怎么相信你旳谎言",
			"花落 , 因为花开过",
			"我的爱、消失不见",
			"爱到沸腾，痛到冰点。",
			"泪洒黄河畔，情欲消眉上。",
			"Grace Junk",
			"丿沩凊丶葰涃",
			"丿为情丶所困",
			"想太多我会难过",
			"ヾ 残花冷月情",
			"我的改变,拜您所赐。",
			"时间一直去,回忆真美丽",
			"其实说好只是玩玩而已旳,",
			"繁花似锦，只剩青春旳忧伤",
			"-﹋原来承诺真旳很无力",
			"?也许是爱旳太大没有把握",
			"把一切送沵丶 黄昏余涐#",
			"▽ 总错把期待当成希望",
			"疼了就再也不碰爱情好了",
			"人有绝交，才有至交。",
			"我爱你这件事不需要解释。",
			"多么希望你，是我独家的记",
			"对你的爱,泛滥成灾.",
			"用微笑掩盖了落寞",
			"醉后知酒浓、爱后知情伤",
			"下一站，幸福",
			"下一站，在哪？",
			"其实、卜过是一场错爱。",
			"最终、回到了原点。",
			"叙述、他和她的故事°",
			"淡ㄋ的情、怎么继续",
			"ッ深冬、心已死亡。",
			"心情因你的离开而不美丽.",
			"想恨又不能恨~",
			"向日葵、没有太阳照样耀眼",
			"爱断了线该如何撕守-",
			"# 怀念,那只是优柔寡断",
			"誓言只不过是拆散的谎言。",
			"重蹈覆辙成了一种瘾",
			"天空灰的像哭过 擦干了泪",
			"你拆了城墙 让我去流浪",
			"一霸道自私的男r3n╮",
			"你的心角处一丝想念",
			"_/~↘浅唱、那囙忆",
			"╭ァ吢死丶情灭。",
			"☆想要★§留住你☆",
			"窒息-你的笑~",
			"い奈何心善╮",
			"狠爱狠爱倪",
			"真的有来生吗",
			"Wo丶好期待",
			"℡往事随风丶",
			"╰烟消云散、",
			"你无辜的眼神",
			"感性艺术 Ⅱ",
			"这无指望的爱▲",
			"没脾气了男人-",
			"无法坦诚相对",
			"想要留住你゜",
			"我用尽了力气",
			"听世界的喧闹",
			"心却寂寥゜",
			"梦丶支离破碎",
			"_无奈的看透",
			"曾经迩的味道",
			"终究是甜言蜜语",
			"罂粟毒花ㄟ",
			"小腼腆□不是错",
			"小伙稳住架，",
			"为你独唱偏爱ゆ",
			"脸庞灿烂的笑容",
			"月半小夜曲。",
			"情殇。无悔",
			"眼泪笑我愚昧，",
			"省略了对白。",
			"沵过嘚恏嬷？",
			"童话里没有你",
			"誓言扑朔迷离",
			"祢给的警告",
			"即使我在乎.",
			"逆光的少年丶",
			"可悲的小孩、",
			"用心去等待╭",
			"祢 °爱了玛",
			"记忆°Blank丶",
			"只是忘记而已",
			"谁都不欠谁",
			"小小の妖·娆",
			"有你→美梦",
			"ㄣ抒情的歌曲",
			"╮拿命爱自己",
			"心慌心淡心碎",
			"结局后才明白,",
			"爱情的自私フ",
			"好像忘了呢/",
			"单身却不孤单",
			"孤独的路灯",
			"有你的曾经",
			//"伤感网名",
			"轻描淡写诉人生",
			"丶眼瞳中仅剩苍白",
			"爱那么短、遗忘那么长",
			"相濡以沫、不如两两相忘ヽ",
			"回忆着回忆不起的回忆",
			"撕心裂肺的心痛蓦然的升起。",
			"情话再甜只是种敷衍 っ",
			"能触摸到菂゛就是幸福旳ゝ",
			"能不能借个安心的微笑ミ",
			"涐爱上了沵住过旳 city。",
			"破碎的诺言 湿了双眼",
			"¤ 隐匿 一张纸的空白。",
			"-以后旳以后,拿命爱自己 ♯",
			"）你沉重的爱、我受不起",
			"－心脏被冷漠霸占。て.",
			"我看不见、永远有多远つ",
			"‘那些誓言、荒废流年ゝ",
			"流年、撕扯着不堪っ",
			"指甲上、画满对你的思念",
			"ヅ不同的角色不同的结局ヅ",
			"骑着小毛驴带你环游世界丶",
			"抹不掉的痛,又有谁会懂",
			"没有句号╮也没法继续",
			"痛恨你给过、却转身要走",
			"对你的好_是别人给不起的",
			"因为你、一切都显得黯淡",
			"∝ 〃俄的幸福,必须有你",
			"其实硪不想为了你而流泪ヽ",
			"我们暧昧，却不属于彼此╮",
			"轻声描述爱情只是折子戏ㄟ",
			"记忆死在丶那年离别的夏天",
			"有沵的时候,我与世隔绝ゞ",
			"你花言巧语 我不言不语ら",
			"至始至终丶思念在深夜",
			"苏格拉没有底丶已忘却悲伤",
			"想念你的呼吸、变成氧气つ",
			"俄忘了是俄一个人当主角つ",
			"_想牵着你的手、不放开",
			"想你丶是我每天的必修课",
			"给不了你一生我给你一世╮",
			"烟花耀眼灼伤我旳回忆//",
			"眼泪释怀丶那无奈的伤 づ",
			"﹏散在空气中的小记忆づ",
			"放手゛不代表我不再爱你",
			"生活※一半记忆一半继续",
			"黑黑黑的夜ヽ却有一抹阳光",
			"つ风吹起如花般细碎的流年",
			"黑色的房间回忆就像默片﹌",
			"思念在记忆中朦胧的回忆",
			"就是心痛罢了、又没什么",
			"空城住着颗 空心。",
			"゛躲角落ヽ拥抱自己的心痛",
			"你的路途 看不到我的苍老",
			"疯疯癫癫、只为幸福",
			"谁说回忆都是、快乐的つ",
			"。゛我把心寄错了地址 |",
			"╰如果有如果ˋ结局会怎样",
			"℡若即若离的小纠结-",
			"满口永远的孩子、慢慢懂事",
			"别调戏我丶小心我非礼你",
			"女人不花、何谈貌美如花",
			"流年跌破承诺终究消逝づ",
			"爱旳信仰早已被风熄灭╰つ",
			"你的幸福幸福只是奢侈’",
			"时间总嘲笑涐的痴心妄想°",
			"涐不坚强没人替涐勇敢°",
			"谁真心对我好ゝ我很清楚",
			"痛丶只有我自己懂”",
			"淡淡的青春，纯纯的爱づ",
			"歌的旋律、释怀心的情绪",
			"荒季莫秋╰祭奠往日情",
			"つ眼瞳里只剩下苍白的记忆",
			"我习惯了ゝ你旳不在乎",
			"情侣网名",
			"(_灬左手づ゜|;(_灬右づ手",
			"->?尐爷、|;->?尛姐、",
			"ωǒ哊点坏.|;ωǒ哊点乖.",
			"-夫|;-妻",
			"潴公|;潴婆",
			"|.男主角||;|.女主角|",
			"整个夏天，|;悲伤蔓延。",
			"ノ禸躰娇贵ヽ|;ノ禸躰娇贵丶",
			"伦8|;J伦",
			"[爱-。|;-。被爱]",
			"?潴‘宀呗，|;?潴‘吆唲，",
			"-﹥海贼王.|;-﹥海贼婆.",
			"（他德笑。|;（硪德泪。",
			".╲圊楼娚孓|;.╲圊楼钕孓",
			"牵扯,|;羁绊。",
			"峩,{魂丢ㄋ}|;峩,{吢丢ㄋ}",
			"の鈊肝T|;の寳贝C",
			"嫉妒︷﹖|;菰独゛.",
			"[?¨橙子|;[尐¨橘子",
			"-迩睋相爱，|;-緈福开场，",
			"?-[鸭疍。|;?-[鸡疍。",
			"╄→丘比特荖厷℅|;╄→丘比特荖嘙℅",
			"(り’茬这裏！|;(り’类迲了?",
			"-[潘]?嫖,|;-[胡]尛妓,",
			"?尛鬼暘暘丶|;?尛妖嫣嫣丶",
			"我那什么丶|;整死你我的爱人",
			"背影斑驳你的旧时光，思念装饰你的旧心情",
			//"英文网名",
			"BAEK°",
			"along",
			"KING。",
			"crazyMaybe",
			"BlaVer",
			"Fairy°Story°",
			"star |",
			"Crazy゜",
			"Summer*",
			"Demon、微光",
			"Angle、微眸",
			"Aries°气质",
			"Abandon丶",
			"Sadness。",
			"—— end °",
			"Sunny°刺眼",
			"Sunny°刺眼",
			"Moment °superman",
			"Sam| 绝情△",
			"Black 黑色",
			"Devil·心碎",
			"玩心少年、boy",
			"Death、－宁",
			"Sily°小晴天",
			"So what、",
			"One day *",
			".·　Alone°",
			"Story！剧终°baby",
			"Fairy°",
			"Tears",
			"Cut love！",
			"Shoulderˆ",
			"HEY1 贵人诱惑",
			"Amour · 旧爱",
			"Sunshine °",
			"· 迷情queen°",
			"- superman",
			"starry.",
			"凉兮* Armani",
			"暮夏-Gentle°",
			"我心有BIGBANG",
			"hate you 彡",
			"心动则痛 Oath°",
			"Sandm ° 旧颜",
			"经年°reminis",
			"Aomr゛心渃相依゜",
			"肆虐 *Raging",
			"黛文 Make-up",
			"潮牌Pet miss",
			"浮夸生 Easonら",
			"Redundant°",
			"安分°　Moon",
			"Freedom",
			"Let me go °",
			"肆虐ヽ Ragingヽ"
	};
	
	private static final String[] girl_names = new String[] {
			"Sophia",
			"Emma",
			"Olivia",
			"Ava",
			"Isabella",
			"Mia",
			"Zoe",
			"Lily",
			"Emily",
			"Madelyn",
			"Madison",
			"Chloe",
			"Charlotte",
			"Aubrey",
			"Avery",
			"Abigail",
			"Kaylee",
			"Layla",
			"Harper",
			"Ella",
			"Amelia",
			"Arianna",
			"Riley",
			"Aria",
			"Hailey",
			"Hannah",
			"Aaliyah",
			"Evelyn",
			"Addison",
			"Mackenzie",
			"Adalyn",
			"Ellie",
			"Brooklyn",
			"Nora",
			"Scarlett",
			"Grace",
			"Anna",
			"Isabelle",
			"Natalie",
			"Kaitlyn",
			"Lillian",
			"Sarah",
			"Audrey",
			"Elizabeth",
			"Leah",
			"Annabelle",
			"Kylie",
			"Mila",
			"Claire",
			"Victoria",
			"Maya",
			"Lila",
			"Elena",
			"Lucy",
			"Savannah",
			"Gabriella",
			"Callie",
			"Alaina",
			"Sophie",
			"Makayla",
			"Kennedy",
			"Sadie",
			"Skyler",
			"Allison",
			"Caroline",
			"Charlie",
			"Penelope",
			"Alyssa",
			"Peyton",
			"Samantha",
			"Liliana",
			"Bailey",
			"Maria",
			"Reagan",
			"Violet",
			"Eliana",
			"Adeline",
			"Eva",
			"Stella",
			"Keira",
			"Katherine",
			"Vivian",
			"Alice",
			"Alexandra",
			"Camilla",
			"Kayla",
			"Alexis",
			"Sydney",
			"Kaelyn",
			"Jasmine",
			"Julia",
			"Cora",
			"Lauren",
			"Piper",
			"Gianna",
			"Paisley",
			"Bella",
			"London",
			"Clara",
			"Cadence"	
	};
	private static final String[] boy_names = new String[] {
			"Jackson",
			"Aiden",
			"Liam",
			"Lucas",
			"Noah",
			"Mason",
			"Jayden",
			"Ethan",
			"Jacob",
			"Jack",
			"Caden",
			"Logan",
			"Benjamin",
			"Michael",
			"Caleb",
			"Ryan",
			"Alexander",
			"Elijah",
			"James",
			"William",
			"Oliver",
			"Connor",
			"Matthew",
			"Daniel",
			"Luke",
			"Brayden",
			"Jayce",
			"Henry",
			"Carter",
			"Dylan",
			"Gabriel",
			"Joshua",
			"Nicholas",
			"Isaac",
			"Owen",
			"Nathan",
			"Grayson",
			"Eli",
			"Landon",
			"Andrew",
			"Max",
			"Samuel",
			"Gavin",
			"Wyatt",
			"Christian",
			"Hunter",
			"Cameron",
			"Evan",
			"Charlie",
			"David",
			"Sebastian",
			"Joseph",
			"Dominic",
			"Anthony",
			"Colton",
			"John",
			"Tyler",
			"Zachary",
			"Thomas",
			"Julian",
			"Levi",
			"Adam",
			"Isaiah",
			"Alex",
			"Aaron",
			"Parker",
			"Cooper",
			"Miles",
			"Chase",
			"Muhammad",
			"Christopher",
			"Blake",
			"Austin",
			"Jordan",
			"Leo",
			"Jonathan",
			"Adrian",
			"Colin",
			"Hudson",
			"Ian",
			"Xavier",
			"Camden",
			"Tristan",
			"Carson",
			"Jason",
			"Nolan",
			"Riley",
			"Lincoln",
			"Brody",
			"Bentley",
			"Nathaniel",
			"Josiah",
			"Declan",
			"Jake",
			"Asher",
			"Jeremiah",
			"Cole",
			"Mateo",
			"Micah",
			"Elliot",
	};
	private static final byte[] birth_month = {
			1,2,3,4,5,6,7,8,9,10,11,12
	};
	private static final byte[] birth_day = {
			1,2,3,4,5,6,7,8,9,10,
			11,12,13,14,15,16,17,18,19,20,
			21,22,23,24,25,26,27,28,29,30,
			31
	};
	
	private static final String[] china_mailbox = new String[] {
			"163.com",
			"sina.cn",
			"qq.com",
			"sina.com",
			"126.com",
			"sohu.com",
			"yeah.net",
			"tom.com",
			"263.net",
			"citiz.net",
			"21cn.com"
	};
	private static final String[] global_mailbox = new String[] {
			"hotmail.com",
			"gmail.com",
			"outlook.com",
			"yahoo.com",
			"alibaba.com"
	};
	private static final String digitString="0123456789";
	private static final String upperCaseString="ABCDEFGHJKMNPQRSTUVWXYZ"; //ILO
	private static final String lowerCaseString="abcdefghjkmnpqrstuvwxyz"; //ilo
	private static final String symbolString="@#$%&-+*\"':;!?,_/.";
	
	
	
	public static String getRandomNetName() {
		Random r = new Random();
		return net_name[r.nextInt(net_name.length)];
	}
	public static String getRandomChineseName(Random r, int gender) {
		boolean is_boy = false;
		if(gender == 0){
			is_boy = r.nextBoolean();
		}else{
			is_boy = gender==1;
		}
		String name = "";
		if((r.nextInt()&0x1)==0){
			// has 
			name += baijiaxing[r.nextInt(baijiaxing.length)];
		}
		if(is_boy){
			if((r.nextInt()&0x1)==0){
				name += male_names[r.nextInt(male_names.length)];
			}
			name += male_names[r.nextInt(male_names.length)];
		}else{
			name += female_names[r.nextInt(female_names.length)];
		}
		if(name.length()==1){
			name += other[r.nextInt(other.length)];
		}
		
		int i = r.nextInt(100);
		if(i<50){
			name += 1970+r.nextInt(30);
			name += String.format("%02d", 1+r.nextInt(12));
			name += String.format("%02d", 1+r.nextInt(30));
		}else if(i<30){
			name += r.nextInt(10000);
		}else {
			name += getRandomAscii(6);
		}
		
		return name;
	}
	
	public static String getRandomAscii(int min_len) {
		Random r = new Random();
		int len = r.nextInt(5)+min_len;
		String string = "";
		for(int i=0; i<len;i++){
			string+=getRandomAlpha(r.nextInt(2)==1);
		}
		return string;
	}
	
	public static String getRandomEnglishName(Random r, int gender) {
		boolean is_boy = false;
		if(gender == 0) is_boy =r.nextBoolean();
		else is_boy = gender==1;
		
		String s = null;
		
		if(is_boy){
			s =  boy_names[r.nextInt(boy_names.length)];
		}else{
			s = girl_names[r.nextInt(girl_names.length)];
		}
		int i = r.nextInt(100);
		if(i<50){
			s += 1970+r.nextInt(30);
			s += String.format("%02d", 1+r.nextInt(12));
			s += String.format("%02d", 1+r.nextInt(30));
		}else if(i<30){
			s += r.nextInt(10000);
		}else {
			s += getRandomAscii(6);
		}
		return s;
	}
	
	public static String getRandomAlpha(boolean uppercase){
		Random r = new Random();
		if(uppercase){
			return ""+upperCaseString.charAt(r.nextInt(upperCaseString.length()));
		}else{
			return ""+lowerCaseString.charAt(r.nextInt(lowerCaseString.length()));
		}
	}
	public static int getRandomMonth(){
		Random r = new Random();
		return birth_month[r.nextInt(birth_month.length)];
	}
	
	public static String getRandomEmail(boolean in_china) {
		Random r = new Random();
		if(in_china){
			return china_mailbox[r.nextInt(china_mailbox.length)];
		}else{
			return global_mailbox[r.nextInt(global_mailbox.length)];
		}
	}
	public static int getRandomDay(int m){
		Random r = new Random();
		byte d = 1;
		if(m == 1 || m == 3 || m == 5 || m==7 || m==8 || m==10 || m==12){
			d = birth_day[r.nextInt(birth_month.length)];
		}else if( m == 2){
			d = birth_day[r.nextInt(birth_month.length-3)];
		}else if(m == 4||m==6||m==9||m==11){
			d = birth_day[r.nextInt(birth_month.length-1)];
		}
		return d;
	}
	
	public static String getRandomBirthday() {
		Random r = new Random();
		byte m = birth_month[r.nextInt(birth_month.length)];
		byte d = 1;
		if(m == 1 || m == 3 || m == 5 || m==7 || m==8 || m==10 || m==12){
			d = birth_day[r.nextInt(birth_month.length)];
		}else if( m == 2){
			d = birth_day[r.nextInt(birth_month.length-3)];
		}else{
			d = birth_day[r.nextInt(birth_month.length-1)];
		}
		return String.format("%02d%02d", m, d);
	}
	public static String getRandomPassword(Random r, int min_len, int max_len, String extra_chars, 
			boolean require_asc, boolean require_both_upper_and_lower) {
		String pass = "";
		int diff = max_len-min_len;
		int sel = r.nextInt(100);
		if(sel<90&& diff >= 8) diff = diff/2;
		int passlen = (min_len + diff>0?r.nextInt(diff):0)/3;
		for(int i=0;i<passlen;i++) {
			int i1 = r.nextInt(100);
			if(i1 < 10){
				int num = digitString.charAt(r.nextInt(digitString.length()))-'0';
				pass += String.format("%d%d", num, num+1);
			}else if(i1 <20 ){
				int num = digitString.charAt(r.nextInt(digitString.length()))-'0';
				pass += String.format("%d%d%d", num, num+1, num+2);
			}else if(i1 < 30) {
				int num = digitString.charAt(r.nextInt(digitString.length()))-'0';
				pass += String.format("%d%d", num, num);
			}else if(i1 < 40) {
				int num = digitString.charAt(r.nextInt(digitString.length()))-'0';
				pass += String.format("%d%d", num, num>0?num-1:0);
			}else if(i1 < 50) {
				int num = digitString.charAt(r.nextInt(digitString.length()))-'0';
				pass += String.format("%d%d%d", num, num>1?num-1:0, num>2?num-2:num);
			}else if(i1 < 60) {
				boolean b = r.nextBoolean();
				String s = b?lowerCaseString:upperCaseString;
				char ch = s.charAt(r.nextInt(s.length()));
				pass += ch+ch;
			}else if(i1 < 70) {
				boolean b = r.nextBoolean();
				String s = b?lowerCaseString:upperCaseString;
				int index = r.nextInt(s.length());
				int next_index = index+1;
				if(index == s.length()-1){
					next_index = index-1;
				}
				pass += s.charAt(index)+s.charAt(next_index);
			}else if(i1 < 80) {
				boolean b = r.nextBoolean();
				String s = b?lowerCaseString:upperCaseString;
				int index = r.nextInt(s.length());
				int next_index = index+1;
				int nn_index = index+2;
				if(index >= s.length()-2){
					next_index = index-1;
					nn_index = index-2;
				}
				pass += s.charAt(index)+s.charAt(next_index)+s.charAt(nn_index);
			}else if(i1 < 90) {
				boolean b = r.nextBoolean();
				String s = b?lowerCaseString:upperCaseString;
				char ch = s.charAt(r.nextInt(s.length()));
				pass += ch+ch;
			}else {
				if(!StringUtil.isEmpty(extra_chars)) {
					pass += extra_chars.charAt(r.nextInt(extra_chars.length()));
				}
			}

			if(pass.length()>max_len) {
				pass = pass.substring(0, max_len);
				break;
			}
		}
		
		if(pass.length()< min_len){
			for(int i=pass.length();i<min_len;i++){
				int i1 = r.nextInt(3);
				if(i1 == 0){
					pass += digitString.charAt(r.nextInt(digitString.length()));
				}else if(i1==2){
					pass += lowerCaseString.charAt(r.nextInt(lowerCaseString.length()));
				}else{
					pass += upperCaseString.charAt(r.nextInt(upperCaseString.length()));
				}
			}
		}
		boolean no_digits = true; 
		for(int i=0; i<pass.length();i++){
			char ch = pass.charAt(i);
			if(ch >= '0' && ch <= '9'){
				no_digits = false;
				break;
			}
		}
		if(no_digits){ // nodigits
			pass = pass.replace(pass.charAt(r.nextInt(pass.length())),
					digitString.charAt(r.nextInt(digitString.length())));
		}
		
		no_digits = true; 
		boolean no_ascii= true;
		boolean no_upper_asc = true;
		boolean no_lower_asc = true;
		int asc_count = 0;
		int digit_count= 0;
		
		for(int i=0; i<pass.length();i++){
			char ch = pass.charAt(i);
			if(ch >= '0' && ch <= '9'){
				no_digits = false;
				digit_count++;
			}else if(ch >='a' && ch <= 'z'){
				no_lower_asc = false;
				no_ascii = false;
				asc_count++;
			}else if(ch >='A' && ch <= 'Z'){
				no_upper_asc = false;
				no_ascii = false;
				asc_count++;
			}
		}
		
		
		if(no_ascii){
			if(require_asc){
				if(require_both_upper_and_lower) {
					int index = r.nextInt(pass.length());
					int next_index = r.nextInt(pass.length());
					if(next_index == index){
						next_index = index + 1;
						if(next_index>=pass.length()-1)
							next_index = index-1;
					}
					pass = pass.replace(pass.charAt(index), lowerCaseString.charAt(r.nextInt(lowerCaseString.length())));
					pass = pass.replace(pass.charAt(next_index), upperCaseString.charAt(r.nextInt(upperCaseString.length())));
				}else{
					boolean b = r.nextBoolean();
					String s = b?lowerCaseString:upperCaseString;
					int index = r.nextInt(pass.length());
					pass = pass.replace(pass.charAt(index), s.charAt(r.nextInt(s.length())));
				}
			}
		}else if(no_upper_asc&&require_both_upper_and_lower){
			int index = r.nextInt(pass.length());
			char ch = pass.charAt(index);
			if((ch>='a'&&ch<='z' && asc_count==1)||(ch>='0'&&ch<='9'&&digit_count==1)){
				index++;
				if(index >= pass.length()) index = 0;
			}
			pass = pass.replace(pass.charAt(index), upperCaseString.charAt(r.nextInt(upperCaseString.length())));
			
		}else if(no_lower_asc&&require_both_upper_and_lower){
			int index = r.nextInt(pass.length());
			char ch = pass.charAt(index);
			if((ch>='A'&&ch<='Z' && asc_count==1)||(ch>='0'&&ch<='9'&&digit_count==1)){
				index++;
				if(index >= pass.length()) index = 0;
			}
			pass = pass.replace(pass.charAt(index), lowerCaseString.charAt(r.nextInt(lowerCaseString.length())));
		}
		
		
		return pass;
	}
	public static String getRandomUserName(Random r, int min_len, int max_len, int gender) {
		// TODO Auto-generated method stub
		String s = getRandomEnglishName(r, gender);
		if(s.length()>max_len){
			s = s.substring(0, max_len);
		}
		return s;
	}
	public static String getRandomNickName(Random r, boolean chinese, int gender) {
		// TODO Auto-generated method stub
		if(chinese){
			return getRandomChineseName(r, gender);
		}else{
			return getRandomEnglishName(r, gender);
		}
		
	}
}
