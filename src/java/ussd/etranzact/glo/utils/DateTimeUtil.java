/*
 * Crown Interactive. Proprietary.
 */
package ussd.etranzact.glo.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Utility class for date and time manipulation.
 *
 * @author remi.adekanmbi
 */
public class DateTimeUtil {

   private static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
   private static final Logger L = Logger.getLogger(DateTimeUtil.class);

   private DateTimeUtil() {
   }

   public static Date convertTimestamp(Timestamp tmp) {
      long time = tmp.getTime();
      Date derivedDate = DateTimeUtil.getDateFor(time);
      return derivedDate;
   }

   public static java.sql.Date getCurrentSQlDate(Date date) {
      return new java.sql.Date(date.getTime());
   }

   public static Date getCurrentDate() {
      Date currentDate;
      try {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
         String format = dateFormat.format(new Date());
         currentDate = dateFormat.parse(format);
      } catch (ParseException ex) {
         currentDate = new Date();
      }
      return currentDate;
   }

   public static Date getCurrentDate(Date date) {
      Date currentDate;
      try {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
         String format = dateFormat.format(date);
         currentDate = dateFormat.parse(format);
      } catch (ParseException ex) {
         currentDate = new Date();
      }
      return currentDate;
   }

   public static Date getCurrentDate(String date) {
      Date currentDate;
      try {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
         String format = dateFormat.format(date);
         currentDate = dateFormat.parse(format);
      } catch (ParseException ex) {
         currentDate = new Date();
      }
      return currentDate;
   }

   public static Date addHoursToDate(Date date, int hours) {
      int addedMillis = hours * 60000 * 60;
      //L.info("Calculated milliseconds is " + addedMillis);
      long time = date.getTime() + addedMillis;
      return getDateFor(time);
   }

   public static Date addMinutesToDate(Date date, int minutes) {
      int addedMillis = minutes * 60000;
      //L.info("Calculated milliseconds is " + addedMillis);
      long time = date.getTime() + addedMillis;
      return getDateFor(time);
   }

   public static Date getLockedOutUntil(Date date, int elapse, String elapseType) {
      Date retVal = null;
      if (elapseType.equals("MINUTES")) {
         //L.info("Adding minutes to date!!!!");
         retVal = addMinutesToDate(date, elapse);
         //L.info("Date retrieved is : " + retVal);
      } else if (elapseType.equals("HOURS")) {
         retVal = addHoursToDate(date, elapse);
      }
      return retVal;
   }

   /**
    * Distance between the two dates in millis.
    */
   public static long distance(Date start, Date end) {
      return Math.abs(start.getTime() - end.getTime());
   }

   public static long distanceFromNow(Date date) {
      return distance(date, getCurrentDate());
   }

   public static long daysToMillis(int days) {
      return days * 24 * 60 * 60 * 1000;
   }

   public static long hoursToMillis(int hours) {
      return hours * 60 * 60 * 1000;
   }

   public static long minutesToMillis(int minutes) {
      return minutes * 60 * 1000;
   }

   public static String toPrettyString(Date date) {
      return DATE_FORMATTER.format(date);
   }

   //TODO(oluwasayo): REVISIT THIS IMPLEMENTATION. TAKE PROPER CARE OF TIMEZONES.
   public static Date getDateFor(long millis) {
      Date date = null;
      try {
         String format = DATE_FORMATTER.format(new Date(millis));
         date = DATE_FORMATTER.parse(format);
      } catch (ParseException ex) {

      }
      return date;
   }

   public static Date addHoursToDate(int hours) {
      return addHoursToDate(new Date(), hours);
   }

   public static Date addMillisToDate(Date date, long millis) {
      return new Date(date.getTime() + millis);
   }

   public static Date subtractMillisFromDate(Date date, long millis) {
      return new Date(date.getTime() - millis);
   }

   public static Date addMillisToDate(long millis) {
      return addMillisToDate(new Date(), millis);
   }

   public static List<Date> datesBetween(Date d1, Date d2, int style) {
      List<Date> ret = new ArrayList<Date>();
      Calendar c = Calendar.getInstance();
      c.setTime(d1);
      while (c.getTimeInMillis() < d2.getTime()) {
         c.add(style, 1);
         String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").format(c.getTime());
         //System.out.println(format);
         Date parse = null;
         try {
            parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z").parse(format);
         } catch (ParseException ex) {

         }
         //This is to ensure that only the days preceeding the endDate are in the search
         long m = c.getTimeInMillis() - d2.getTime();
         if (m < 0) {
            ret.add(parse);
         }
      }
      return ret;
   }

   public static String getMonthForInt(int num) {
      String month = "wrong";
      DateFormatSymbols dfs = new DateFormatSymbols();
      String[] months = dfs.getShortMonths();
      if (num >= 0 && num <= 11) {
         month = months[num];
      }
      return month;
   }

   public static List<String> getMonths(List<Date> dates) {
      List<String> resp = new ArrayList<String>();
      DateFormatSymbols dfs = new DateFormatSymbols();
      String[] months = dfs.getShortMonths();
      for (Date date : dates) {
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         int num = cal.get(Calendar.MONTH);
         if (num >= 0 && num <= 11) {
            resp.add(months[num]);
            System.out.println(months[num]);
         }
      }
      return resp;
   }

   public static List<String> getDays(List<Date> dates) {
      List<String> resp = new ArrayList<String>();
      DateFormatSymbols dfs = new DateFormatSymbols();
      String[] days = dfs.getShortWeekdays();
      for (Date date : dates) {
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         int num = cal.get(Calendar.DAY_OF_WEEK);
         if (num >= 0 && num <= 11) {
            resp.add(days[num]);
            System.out.println(days[num]);
         }
      }
      return resp;
   }

   public static List<Date> datesBetween(Date d1, Date d2) {
      List<Date> ret = new ArrayList<Date>();
      Calendar c = Calendar.getInstance();
      c.setTime(d1);
      while (c.getTimeInMillis() < d2.getTime()) {
         c.add(Calendar.MONTH, 1);
         String format = DATE_FORMATTER.format(c.getTime());
         try {
            ret.add(DATE_FORMATTER.parse(format));
         } catch (ParseException ex) {

         }
      }
      return ret;
   }

   public static Date getStartOfToday() {
      Date startofTodayDate;
      Calendar instance1 = Calendar.getInstance();
      instance1.setTime(new Date());
      Calendar instance2 = Calendar.getInstance();
      instance2.set(
              instance1.get(Calendar.YEAR),
              instance1.get(Calendar.MONTH),
              instance1.get(Calendar.DAY_OF_MONTH), 0, 0);

      Date time = instance2.getTime();
      startofTodayDate = getCurrentDate(time);
      return startofTodayDate;
   }

   public static Date getStartOfDate(Date date) {
      Date startofDate;
      Calendar instance1 = Calendar.getInstance();
      instance1.setTime(date);
      Calendar instance2 = Calendar.getInstance();
      instance2.set(
              instance1.get(Calendar.YEAR),
              instance1.get(Calendar.MONTH),
              instance1.get(Calendar.DAY_OF_MONTH), 0, 0);

      Date time = instance2.getTime();
      startofDate = getCurrentDate(time);
      return startofDate;
   }

   public static Date getEndOfDate(Date date) {
      Date startofDate;
      Calendar instance1 = Calendar.getInstance();
      instance1.setTime(date);
      Calendar instance2 = Calendar.getInstance();
      instance2.set(
              instance1.get(Calendar.YEAR),
              instance1.get(Calendar.MONTH),
              instance1.get(Calendar.DAY_OF_MONTH), 23, 59);

      Date time = instance2.getTime();
      startofDate = getCurrentDate(time);
      return startofDate;
   }

   public static Date getShortDate() {
      Date parse = null;
      try {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
         String format = sdf.format(new Date());
         parse = sdf.parse(format);
      } catch (ParseException ex) {

      }
      return parse;
   }

   public static String getShortDate(Date date) {
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      return sdf.format(date);
   }

   public static Date createDate(int year, int month, int day) {
      Calendar cal = Calendar.getInstance();
      cal.set(Calendar.YEAR, year);
      cal.set(Calendar.MONTH, month - 1);
      cal.set(Calendar.DAY_OF_MONTH, day);
      return cal.getTime();
   }
    public static void main(String[] args) {
        String in = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        System.out.println("out :::: "+in);
    }
}
