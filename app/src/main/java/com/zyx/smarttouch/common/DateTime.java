package com.zyx.smarttouch.common;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.TimeZone;
import java.util.Timer;

public class DateTime
{
	
	public final static String FORMAT_SLASH_DATE  = "MM/dd/yyyy";
	public final static String FORMAT_CROSS_DATE  = "yyyy-MM-dd";
	
	public static final long TicksPerSecond = 1000L;
	public static final long TicksPerMinute = 60000L;
	public static final long TicksPerHour = 3600000L;
	public static final long TicksPerDay = 86400000L;
	private Date date;
	private TimeZone zeroTimeZone = new SimpleTimeZone(0, "13256");
	private Calendar cal;
	private SimpleDateFormat dateFormat;
	private SimpleDateFormat timeFormat;
	private SimpleDateFormat dateTimeFormat;
	private SimpleDateFormat dateTimeFormatPic;
	private SimpleDateFormat dateTimeFormatEvent;
	private SimpleDateFormat dateTimeFormatChat;
	private TimeZone timeZone;

	public DateTime()
	{
		
		this.cal = Calendar.getInstance(Locale.US);
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.US);
		this.dateFormat.setLenient(false);
		this.timeFormat = new SimpleDateFormat("HH:mm",Locale.US);
		this.timeFormat.setLenient(false);
		this.dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US);
		this.dateTimeFormatEvent = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.US);
		this.dateTimeFormatPic = new SimpleDateFormat("d MMM, yyyy h.mm a",Locale.US);//("yyyy-MM-dd HH:mm",Locale.US);
		this.dateTimeFormatChat = new SimpleDateFormat("d MMM, yyyy h.mm a",Locale.US);
		this.dateTimeFormat.setLenient(false);
		this.date = new Date(0L);
		this.timeZone = TimeZone.getDefault();
	}
	
	public long getNow()
	{
		return System.currentTimeMillis();
	}

	public String Date(long Ticks)
	{
		date.setTime(Ticks);
		return dateFormat.format(date);
	}

	public String Time(long Ticks)
	{
		date.setTime(Ticks);
		return timeFormat.format(date);
	}
	
	public String parseDateTime(long Ticks)
	{
		date.setTime(Ticks);
		return dateTimeFormat.format(date);
	}
	
	public String parseDateTimeForChat(long Ticks)
	{
		date.setTime(Ticks);
		return dateTimeFormatChat.format(date);
	}
	
	public String parseDateTimePic(long Ticks)
	{
		date.setTime(Ticks);
		return dateTimeFormatPic.format(date);
	}

	public String getTimeFormat()
	{
		return timeFormat.toPattern();
	}

	public void setTimeFormat(String Pattern)
	{
		timeFormat.applyPattern(Pattern);
	}
	
	public long DateParse(String Date) throws ParseException
	{
		return dateFormat.parse(Date).getTime();
	}
	
	public long DateTimeParse(String Date) throws ParseException
	{
		return dateTimeFormat.parse(Date).getTime();
	}
	
	public String getDateFormat()
	{
		return dateFormat.toPattern();
	}

	public void setDateFormat(String Pattern)
	{
		dateFormat.applyPattern(Pattern);
	}
	
	public String getDeviceDefaultDateFormat()
	{
		SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getDateInstance();
		return sdf.toPattern();
	}

	public String getDeviceDefaultTimeFormat()
	{
		SimpleDateFormat sdf = (SimpleDateFormat) DateFormat.getTimeInstance();
		return sdf.toPattern();
	}

	public long TimeParse(String Time) throws ParseException
	{
		timeFormat.setTimeZone(zeroTimeZone);
		long time = timeFormat.parse(Time).getTime();
		timeFormat.setTimeZone(timeZone);
		long offsetInMinutes = Math.round(getTimeZoneOffset() * 60.0D);
		long dayStartInUserTimeZone = System.currentTimeMillis()+ offsetInMinutes * 60000L;
		dayStartInUserTimeZone -= dayStartInUserTimeZone % 86400000L;
		dayStartInUserTimeZone -= offsetInMinutes * 60000L;
		return dayStartInUserTimeZone + time % 86400000L;
	}

	public long DateTimeParse(String Date, String Time)
	{
		SimpleDateFormat df = dateFormat;
		SimpleDateFormat tf = timeFormat;
		df.setTimeZone(zeroTimeZone);
		tf.setTimeZone(zeroTimeZone);
		try
		{
			long dd = DateParse(Date);
			long tt = tf.parse(Time).getTime();
			long total = dd + tt;

			int endShift = (int) (GetTimeZoneOffsetAt(total) * 3600000.0D);
			total -= endShift;
			int startShift = (int) (GetTimeZoneOffsetAt(total) * 3600000.0D);
			System.out.println(endShift - startShift);
			total += endShift - startShift;
			return total;
		} 
		catch (ParseException e)
		{
			e.printStackTrace();
		} 
		finally
		{
			tf.setTimeZone(timeZone);
			df.setTimeZone(timeZone);
		}
		return 0;

	}

	public void SetTimeZone(double OffsetHours)
	{
		setTimeZoneInternal(new SimpleTimeZone((int) Math.round(OffsetHours * 3600.0D * 1000.0D), ""));
	}

	public void SetTimeZone(int OffsetHours)
	{
		setTimeZoneInternal(new SimpleTimeZone(OffsetHours * 3600 * 1000, ""));
	}

	private void setTimeZoneInternal(TimeZone tz)
	{
		timeZone = tz;
		cal.setTimeZone(timeZone);
		dateFormat.setTimeZone(timeZone);
		timeFormat.setTimeZone(timeZone);
	}

	public double getTimeZoneOffset()
	{
		return timeZone.getOffset(System.currentTimeMillis()) / 3600000.0D;
	}

	public double GetTimeZoneOffsetAt(long Date)
	{
		double d = timeZone.getOffset(Date) / 3600000.0D;
		return d;
	}

	public int GetYear(long Ticks)
	{
		cal.setTimeInMillis(Ticks);
		return cal.get(Calendar.YEAR);
	}

	public int GetMonth(long Ticks)
	{
		cal.setTimeInMillis(Ticks);
		return cal.get(Calendar.MONTH) + 1;
	}

	public int GetDayOfMonth(long Ticks)
	{
		cal.setTimeInMillis(Ticks);
		return cal.get(Calendar.DAY_OF_MONTH);
	}

	public int GetDayOfYear(long Ticks)
	{
		cal.setTimeInMillis(Ticks);
		return cal.get(Calendar.DAY_OF_YEAR);
	}

	public int GetDayOfWeek(long Ticks)
	{
		cal.setTimeInMillis(Ticks);
		return cal.get(Calendar.DAY_OF_WEEK);
	}

	public int GetHour(long Ticks)
	{
		cal.setTimeInMillis(Ticks);
		return cal.get(Calendar.HOUR_OF_DAY);
	}

	public int GetSecond(long Ticks)
	{
		cal.setTimeInMillis(Ticks);
		return cal.get(Calendar.SECOND);
	}

	public int GetMinute(long Ticks)
	{
		cal.setTimeInMillis(Ticks);
		return cal.get(Calendar.MINUTE);
	}

	public long Add(long Ticks, int Years, int Months, int Days)
	{
		Calendar c = cal;
		c.setTimeInMillis(Ticks);
		c.add(1, Years);
		c.add(2, Months);
		c.add(6, Days);
		return c.getTimeInMillis();
	}
	
	
	public long DateTimeEventParse(String Date) throws ParseException
	{
		return dateTimeFormatEvent.parse(Date).getTime();
	}
	
	public long DateTimeEventParseUtc(String Date) throws ParseException
	{
		long lTime =  dateTimeFormatEvent.parse(Date).getTime();
		
		lTime += timeZone.getRawOffset();
		
		return lTime;
	}
}
