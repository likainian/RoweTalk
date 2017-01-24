package com.example.rowetalk.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class DateUtil {
	private static final String TAG = DateUtil.class.getName();
	
    public static final int SECOND = 1000;
    public static final int MINUTE = 60*SECOND;
    public static final int HOUR = 60*MINUTE;
    public static final int DAY = 24*HOUR;
	
	public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	public static final SimpleDateFormat simpleDateFormatLong = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static final SimpleDateFormat simpleDateFormatHour = new SimpleDateFormat("yyyy-MM-dd HH");
	
//	private static DateFormat dateFormat = DateFormat.getDateInstance();
	
	/**
	 * Timestamp转yyyyMMdd
	 */
	public static String timeConvertString(Timestamp time) {
		if (time == null) {
			return null;
		}
		SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyyMMdd");
		
		String str = simpleFormat.format(time);
		
		return str;
	}

	/**
	 * Date 转换成 yyyy-MM-dd形式
	 * @param time
	 * @return
	 */
	public static String convertString(Date time) {
		if (time == null) {
			return null;
		}
		SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		String str = simpleFormat.format(time);
		
		return str;
	}
	
	/**
	 * Timestamp 转换成 yyyy-MM-dd形式
	 * @param time
	 * @return
	 */
	public static String convertString(Timestamp time) {
		if (time == null) {
			return null;
		}
		SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		String str = simpleFormat.format(time);
		
		return str;
	}
	
	/**
	 * String 转换成 Timestamp
	 * @param time
	 * @return
	 */
	public static Timestamp convertTimestamp(String time) {
		if (time == null || "".equals(time)) {
			return null;
		}
		return Timestamp.valueOf(time);
	}
	/**
	 * 把yyyy-MM-dd HH:mm的日期形式转换为yyyyMMddHHmm
	 * @param time
	 * @return
	 */
	public static String convert2TightTimeFormat(String time){
		if(time == null || "".equals(time)){
			return null;
		}
		return time.replaceAll("[^\\d]","");
	}
	
	/**
	 * 把标准的日期形式转换为紧凑形式(eg:yyyy-MM-dd HH:mm To yyyyMMdd)
	 * @param time
	 * @return
	 */
	public static String convert2Day(String time){
		if(time == null || "".equals(time)){
			return null;
		}
		String t = time.substring(0, 10);
		return t.replaceAll("[^\\d]","");
	}
	/**
	 * yyyy-MM-dd转换为Date
	 * @param str
	 * @return
	 */
	public static Date convertStringToDate(String str){
		Date date = null;
		try {
			date = simpleDateFormat.parse(str);
		} catch (ParseException e) {
			Logger.e(TAG, str+" convert to datetime failed", e);
		}
		return date;
	}
	
	/**
	 * yyyy-MM-dd HH:mm:ss转换为Date
	 * @param str
	 * @return
	 */
	public static Date convertLongStringToDate(String str){
		Date date = null;
		try {
			date = simpleDateFormatLong.parse(str);
		} catch (ParseException e) {
			Logger.e(TAG, str+" convert to datetime failed", e);
		}
		return date;
	}
	
	/**
	 * 把紧凑的日期转为标准格式
	 * @param dateTime
	 * @return
	 */
	public static String convertTight2NormalFormat(String dateTime){
		StringBuilder builder = new StringBuilder();
		if(dateTime.length() == 8){
			builder.append( dateTime.substring(0, 4) ).append( "-" )
					.append( dateTime.substring(4, 6) ).append( "-" )
					.append( dateTime.substring(6) );
		}else{// if(dateTime.length() == 12){
			builder.append( dateTime.substring(0, 4) ).append( "-" )
			.append( dateTime.substring(4, 6) ).append( "-" )
			.append( dateTime.substring(6, 8) ).append( " " )
			.append( dateTime.substring(8, 10) ).append( ":" )
			.append( dateTime.substring(10) );
		}
		return builder.toString();
	}
	/**
	 * 返回指定之日后的第几天的yyyy-MM-dd的形式
	 * @param str
	 * @param nextCount
	 * @return
	 */
	public static String getNextNumDaysStr(Date date, int num){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, num);
		
		return simpleDateFormat.format( c.getTime() );
	}
	
	public static String getBeforeNumDaysStr(Date date, int num){
		if(num>0){
			num=num-num*2;
		}
		
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, num);
		
		return simpleDateFormat.format( c.getTime() );
	}
	
	/**
	 * 设置startTime、endTime的相差天数<=30
	 * @param paramMap
	 */
	public static void updateStartEndTimeLessThan30(StringBuilder startBuilder, StringBuilder endBuilder){
		String startTime = startBuilder.toString();
		String endTime = endBuilder.toString();
		
		try{
			//startTime、 endTime都为空
			if( stringIsNull(startTime) && stringIsNull(endTime) ){
				Date end = new Date();
				startBuilder.append(getNextNumDaysStr(end, -30));
				endBuilder.append( simpleDateFormat.format(end) );
				
			//startTime为空、 endTime不为空
			}else if( stringIsNull(startTime) && !stringIsNull(endTime) ){
				Date end = simpleDateFormat.parse(endTime);
				startBuilder.append( getNextNumDaysStr(end, -30) );
			//startTime不为空、 endTime为空
			}else if( !stringIsNull(startTime) && stringIsNull(endTime) ){
				Date start = simpleDateFormat.parse(startTime);
				endBuilder.append( getNextNumDaysStr(start, 30) );
			//startTime、 endTime都不为空
			}else{
				Date startDate = simpleDateFormat.parse(startTime);
				Date endDate = simpleDateFormat.parse(endTime);
				//两个日期相差天数
				int diffence = twoDayDiffence(startDate, endDate);
				//如果相差大于30天，则限制为30天
				if( diffence > 30 ){
					endBuilder.delete(0, endBuilder.length()).append( getNextNumDaysStr(startDate, 30) );
				}
			}
		}catch(Exception e){
			Logger.e(TAG, null,e);
		}

	}
	
	public static Date getTodaySinceHour(int hour){
		if(hour<0) hour = 0;
		if(hour>23) hour=23;
		String s = simpleDateFormatHour.format(new Date());
		s = s.substring(0, s.length()-2)+String.format("%02d", hour);
		try {
			return simpleDateFormatHour.parse(s);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return new Date();
		}	
	}
	
	public static long timeElapsed(Date date1){
		if( null==date1){
			return -1;
		}
		long l1 = new Date().getTime();
		long l2 = date1.getTime();
		if(l1 > l2){
			return (l1-l2);
		}else{
			return  0;
		}
	}
	public static long timeBetween(Date date1, Date date2){
		if( null==date1 || null == date2){
			return -1;
		}
		long l1 = date1.getTime();
		long l2 = date2.getTime();
		if(l1 > l2){
			return (l1-l2);
		}else{
			return (l2-l1);
		}
	}
	
	/**
	 * 比较两个日期的相差天数
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int daysBetween(Date early, Date late) { 
	     
        java.util.Calendar calst = java.util.Calendar.getInstance();   
        java.util.Calendar caled = java.util.Calendar.getInstance();   
        calst.setTime(early);   
         caled.setTime(late);   
         //设置时间为0时   
         calst.set(java.util.Calendar.HOUR_OF_DAY, 0);   
         calst.set(java.util.Calendar.MINUTE, 0);   
         calst.set(java.util.Calendar.SECOND, 0);   
         caled.set(java.util.Calendar.HOUR_OF_DAY, 0);   
         caled.set(java.util.Calendar.MINUTE, 0);   
         caled.set(java.util.Calendar.SECOND, 0);   
        //得到两个日期相差的天数   
         int days = ((int) (caled.getTime().getTime() / 1000) - (int) (calst   
                .getTime().getTime() / 1000)) / 3600 / 24;   
         
        return days;   
   }   
  
	
	public static int daysElapsed(Date date1){
		if( null==date1){
			return -1;
		}
		return daysBetween(date1, new Date());
	}
	
	/**
	 * 比较两个日期的相差天数
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int twoDayDiffence(Date date1, Date date2){
		if( null==date1 || null == date2){
			return -1;
		}
		long diffenceMill = date1.getTime() - date2.getTime();
		return (int)(diffenceMill / (24*60*60*1000));
	}
	
	/**
	 * 判断字符串是否为空
	 * @param str
	 * @return
	 */
	public static boolean stringIsNull(String str){
		if(str == null || str.equals("")){
			return true;
		}
		return false;
	}
	/**
	 * 把日期转换为yyyy-MM-dd的形式
	 * @param date
	 * @return
	 */
	public static String convertDateToStr(Date date){
		return simpleDateFormat.format(date);
	}
	/**
	 * 把日期转换为yyyy-MM-dd HH:mm:ss的形式
	 * @param date
	 * @return
	 */
	public static String convertDateToLongStr(Date date){
		return simpleDateFormatLong.format(date);
	}
	/**
	 * 把日期转换为yyyy-MM-dd HH的形式
	 * @param date
	 * @return
	 */
	public static String convertDateToHourStr(Date date){
		return simpleDateFormatHour.format(date);
	}
	
	public static String getCurJinCouDate(Date date){
		String str = simpleDateFormat.format(date);
		return str.replaceAll("[^\\d]", "");
	}
	
	/**
	 * 判断两个时间是否在同一周
	 * @param time1   时间戳1
	 * @param time2  时间戳2
	 * @return
	 */
	public static boolean isSameWeek(Date time1,Date time2){
		if(null==time1 || null==time2)return false;

		Calendar cal1 = convert(time1);
		Calendar cal2 = convert(time2);
		
		int subYear = cal1.get(Calendar.YEAR)-cal2.get(Calendar.YEAR);
		//同一年
		if(subYear==0){
			if(cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				return true;
		}else if(subYear==1 && cal2.get(Calendar.MONTH)==11){
			//例子:cal1是"2005-1-1"，cal2是"2004-12-25"
			//java对"2004-12-25"处理成第52周
			// "2004-12-26"它处理成了第1周，和"2005-1-1"相同了
			//说明:java的一月用"0"标识，那么12月用"11"
			if(cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				return true;
		}else if(subYear==-1 && cal1.get(Calendar.MONTH)==11){
			//例子:cal1是"2004-12-31"，cal2是"2005-1-1"
			if(cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
				return true;
		}
		return false;
	}
	
	/**
	 * 判断两个时间是否在同一月
	 * @param time1   时间戳1
	 * @param time2  时间戳2
	 * @return
	 */
	public static boolean isSameMonth(Date time1,Date time2){
		if(null==time1 || null==time2)return false;
		
		Calendar cal1 = convert(time1);
		Calendar cal2 = convert(time2);
		
		int count = cal1.get(Calendar.MONTH)-cal2.get(Calendar.MONTH);
		return (count==0);
	}
	
	/**
	 * 判断两个时间是否在同一天
	 * @param time1   时间戳1
	 * @param time2  时间戳2
	 * @return
	 */
	public static boolean isSameDay(Date date1,Date date2){
		if(null==date1 || null==date2)return false;
		
//		Calendar cal1 = convert(date1);
//		Calendar cal2 = convert(date2);
//		int count = cal1.get(Calendar.DAY_OF_YEAR)-cal2.get(Calendar.DAY_OF_YEAR);
//		return (count==0 && date1.getYear()==date2.getYear());
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
		System.out.println(sdf.format(date1));
		return sdf.format(date1).equals(sdf.format(date2));
	}
	
	/** 
     * 将日期转换为日历 
     * @param date 日期 
     * @return 日历 
     */  
	private static Calendar convert(Date date) {
        Calendar calendar = Calendar.getInstance();  
        calendar.setTime(date);  
        return calendar;  
    }
	
	/**
	 * 获取当前时间 传入所需的时间格式时间
	 * ： yyyyMMdd or yyyy-MM-dd
	 * @param geshi
	 * @return
	 */
	public static String getCurrentDate(String geshi)
	{
		SimpleDateFormat bartDateFormat =  new SimpleDateFormat(geshi);
		GregorianCalendar calendar = new GregorianCalendar();
		return  bartDateFormat.format(calendar.getTime());
	}
	
	/**
	 * 把字符串hh:mm:ss转换成秒
	 * @param time 【hh:mm:ss】
	 * @return 秒
	 */
	public static Integer timeToSecond(String time) {
		if(null==time || "".equals(time))return null;
		int second = 0;  
		String[] split = time.trim().split(":");
		if(split.length-1>=0)second += Integer.valueOf(split[split.length-1]);
		if(split.length-2>=0)second += Integer.valueOf(split[split.length-2])*60;
		if(split.length-3>=0)second += Integer.valueOf(split[split.length-3])*60*60;
		return second;
	}
	
	/**
	 * 把秒转换成字符串hh:mm:ss
	 * @param time 【秒】
	 * @return hh:mm:ss
	 */
	public static String secondToTime(int time) {  
        String timeStr = null;  
        int hour = 0;  
        int minute = 0;  
        int second = 0;  
        if (time <= 0)  
            return "00:00";  
        else {  
            minute = time / 60;  
            if (minute < 60) {  
                second = time % 60;  
                timeStr = unitFormat(minute) + ":" + unitFormat(second);  
            } else {  
                hour = minute / 60;  
                if (hour > 99)  
                    return "99:59:59";  
                minute = minute % 60;  
                second = time - hour * 3600 - minute * 60;  
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);  
            }  
        }  
        return timeStr;  
    }  
  
    private static String unitFormat(int i) {  
        String retStr = null;  
        if (i >= 0 && i < 10)  
            retStr = "0" + Integer.toString(i);  
        else  
            retStr = "" + i;  
        return retStr;  
    }
	
	
	public static void main(String[] args) throws ParseException {
//		StringBuilder builder1 = new StringBuilder();
//		StringBuilder builder2 = new StringBuilder("2011-04-03");
//		updateStartEndTimeLessThan30(builder1, builder2);
//		System.out.println( builder1 + "\t" + builder2);
//		
//		
//		SimpleDateFormat simpleDateFormatLong = new SimpleDateFormat("hh:mm:ss");
//		System.out.println(simpleDateFormatLong.parse("01:23:48").getTime());
//		
		
		String str = "01:23:48";
		
		System.out.println(timeToSecond(str));
		
		System.out.println(secondToTime(1278069));
		
		
		

		
	}
}
