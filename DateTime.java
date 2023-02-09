/**********************************************************\
|                                                          |
|                          hprose                          |
|                                                          |
| Official WebSite: http://www.hprose.com/                 |
|                   http://www.hprose.org/                 |
|                                                          |
\**********************************************************/
/**********************************************************\
 *                                                        *
 * DateTime.java                                          *
 *                                                        *
 * DateTime class for Java.                               *
 *                                                        *
 * LastModified: Jun 25, 2015                             *
 * Author: Ma Bingyao <andot@hprose.com>                  *
 *                                                        *
\**********************************************************/
package hprose.util;

import static hprose.io.HproseTags.TagPoint;
import static hprose.io.HproseTags.TagTime;
import static hprose.io.HproseTags.TagUTC;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import hprose.io.unserialize.Reader;

public class DateTime {
    public int year = 1970;
    public int month = 1;
    public int day = 1;
    public int hour = 0;
    public int minute = 0;
    public int second = 0;
    public int nanosecond = 0;
    public boolean utc = false;

    public final static Calendar toCalendar(java.util.Date date) {
        Calendar calendar = Calendar.getInstance(TimeZoneUtil.DefaultTZ);
        calendar.setTime(date);
        return calendar;
    }

    private void init(Calendar calendar) {
        TimeZone tz = calendar.getTimeZone();
        if (!(tz.hasSameRules(TimeZoneUtil.DefaultTZ) || tz.hasSameRules(TimeZoneUtil.UTC))) {
            tz = TimeZoneUtil.UTC;
            Calendar c = (Calendar) calendar.clone();
            c.setTimeZone(tz);
            calendar = c;
        }
        this.year = calendar.get(Calendar.YEAR);
        this.month = calendar.get(Calendar.MONTH) + 1;
        this.day = calendar.get(Calendar.DAY_OF_MONTH);
        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);
        this.nanosecond = calendar.get(Calendar.MILLISECOND) * 1000000;
        this.utc = tz.hasSameRules(TimeZoneUtil.UTC);
    }

    public DateTime() {}

    public DateTime(Calendar calendar) {
        init(calendar);
    }

    public DateTime(java.util.Date date) {
        init(toCalendar(date));
    }
//generalcostructor
    public DateTime(int year, int month, int day,int hour,int minute,int second,int nanosecond,boolean utc) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.nanosecond = nanosecond;
        this.utc = utc;
    }
   
  
//otherconstructors
     public DateTime(int year, int month, int day) {
       this(year,month,day,0,0,0,0,false);
    }
     public DateTime(int year, int month, int day,boolean utc) {
       this(year,month,day,0,0,0,0,utc);
    }
     public DateTime(int hour,int minute,int second,int nanosecond) {
       this(1970,1,1,hour,minute,second,nanosecond,false);
       }
    
     public DateTime(int hour,int minute,int second,int nanosecond,boolean utc) {
       this(1970,1,1,hour,minute,second,nanosecond,utc);
    }
     public DateTime(int year,int month,int day,int hour,int minute,int second) {
       this(year,month,day,hour,minute,second,0,false);
    }
     public DateTime(int year, int month, int day,int hour,int minute,int second,boolean utc) {
       this(year,month,day,hour,minute,second,0,utc);
    }
     public DateTime(int year, int month, int day,int hour,int minute,int second,int nanosecond) {
          this(year,month,day,hour,minute,second,nanosecond,false);
    }
     

     public final static int readTime(Reader reader, DateTime dt) throws IOException {
         InputStream stream = reader.stream;
         dt.hour = read2Digit(stream);
         dt.minute = read2Digit(stream);
         dt.second = read2Digit(stream);
         int tag = stream.read();
         if (tag == TagPoint) {
             dt.nanosecond = stream.read() - '0';
             dt.nanosecond = dt.nanosecond * 10 + (stream.read() - '0');
             dt.nanosecond = dt.nanosecond * 10 + (stream.read() - '0');
             dt.nanosecond = dt.nanosecond * 1000000;
             tag = stream.read();
             if (tag >= '0' && tag <= '9') {
                 dt.nanosecond += (tag - '0') * 100000;
                 dt.nanosecond += (stream.read() - '0') * 10000;
                 dt.nanosecond += (stream.read() - '0') * 1000;
                 tag = stream.read();
                 if (tag >= '0' && tag <= '9') {
                     dt.nanosecond += (tag - '0') * 100;
                     dt.nanosecond += (stream.read() - '0') * 10;
                     dt.nanosecond += stream.read() - '0';
                     tag = stream.read();
                 }
             }
         }
         return tag;
     }
     public final static DateTime readTime(Reader reader) throws IOException {
         DateTime dt = new DateTime();
         dt.utc = (readTime(reader, dt) == TagUTC);
         return dt;
     }
     public final static DateTime readDateTime(Reader reader) throws IOException {
         InputStream stream = reader.stream;
         DateTime dt = new DateTime();
         dt.year = read4Digit(stream);
         dt.month = read2Digit(stream);
         dt.day = read2Digit(stream);
         int tag = stream.read();
         if (tag == TagTime) {
             tag = readTime(reader, dt);
         }
         dt.utc = (tag == TagUTC);
         return dt;
     }

     private static int read4Digit(InputStream stream) throws IOException {
    	 int n = stream.read() - '0';
         n = n * 10 + stream.read() - '0';
         n = n * 10 + stream.read() - '0';
         return n * 10 + stream.read() - '0';
		
	}

	private static int read2Digit(InputStream stream) throws IOException {
         int n = stream.read() - '0';
         return n * 10 + stream.read() - '0';
     }

	@Override
    public String toString() {
        String s;
        if (year == 1970 && month == 1 && day == 1) {
            s = String.format("%02d:%02d:%02d", hour, minute, second);
            if (nanosecond != 0) s = s + String.format(".%09d", nanosecond);
        }
        else if (hour == 0 && minute == 0 && second == 0 && nanosecond == 0) {
            s = String.format("%04d-%02d-%02d", year, month, day);
        }
        else {
            s = String.format("%04d-%02d-%02dT%02d:%02d:%02d",
                year, month, day, hour, minute, second);
            if (nanosecond != 0) s = s + String.format(".%09d", nanosecond);
        }
        if (utc) s = s + (char)TagUTC;
        return s;
    }
//kai edo boro na koitaxo gia duplicated
    public StringBuilder toStringBuilder() {
        StringBuilder s = new StringBuilder();
        if (year == 1970 && month == 1 && day == 1) {
            s.append(String.format("%02d:%02d:%02d", hour, minute, second));
            if (nanosecond != 0) s.append(String.format(".%09d", nanosecond));
        }
        else if (hour == 0 && minute == 0 && second == 0 && nanosecond == 0) {
            s.append(String.format("%04d-%02d-%02d", year, month, day));
        }
        else {
            s.append(String.format("%04d-%02d-%02dT%02d:%02d:%02d",
                year, month, day, hour, minute, second));
            if (nanosecond != 0) s.append(String.format(".%09d", nanosecond));
        }
        if (utc) s.append((char)TagUTC);
        return s;
    }

    public StringBuffer toStringBuffer() {
        StringBuffer s = new StringBuffer();
        if (year == 1970 && month == 1 && day == 1) {
            s.append(String.format("%02d:%02d:%02d", hour, minute, second));
            if (nanosecond != 0) s.append(String.format(".%09d", nanosecond));
        }
        else if (hour == 0 && minute == 0 && second == 0 && nanosecond == 0) {
            s.append(String.format("%04d-%02d-%02d", year, month, day));
        }
        else {
            s.append(String.format("%04d-%02d-%02dT%02d:%02d:%02d",
                year, month, day, hour, minute, second));
            if (nanosecond != 0) s.append(String.format(".%09d", nanosecond));
        }
        if (utc) s.append((char)TagUTC);
        return s;
    }

    public Calendar toCalendar() {
        Calendar calendar = Calendar.getInstance(utc ?
                                       TimeZoneUtil.UTC :
                                       TimeZoneUtil.DefaultTZ);
        calendar.set(year, month - 1, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, nanosecond / 1000000);
        return calendar;
    }

    public Timestamp toTimestamp() {
        Timestamp timestamp = new Timestamp(toCalendar().getTimeInMillis());
        timestamp.setNanos(nanosecond);
        return timestamp;
    }

    public Date toDate() {
        return new Date(toCalendar().getTimeInMillis());
    }

    public Time toTime() {
        return new Time(toCalendar().getTimeInMillis());
    }

    public java.util.Date toDateTime() {
        return new java.util.Date(toCalendar().getTimeInMillis());
    }

    public BigInteger toBigInteger() {
        return BigInteger.valueOf(toCalendar().getTimeInMillis());
    }

    public long toLong() {
        return toCalendar().getTimeInMillis();
    }

}
