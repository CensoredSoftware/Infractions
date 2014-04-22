package com.censoredsoftware.library.util;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Times
{
	/**
	 * Return the seconds since the provided time.
	 *
	 * @param time The provided time.
	 * @return Seconds.
	 */
	public static double getSeconds(long time)
	{
		return (double) TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time);
	}

	/**
	 * Return the minutes since the provided time.
	 *
	 * @param time The provided time.
	 * @return Minutes.
	 */
	public static double getMinutes(long time)
	{
		return (double) TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - time);
	}

	/**
	 * Return the hours since the provided time.
	 *
	 * @param time The provided time.
	 * @return Hours.
	 */
	public static double getHours(long time)
	{
		return (double) TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - time);
	}

	/**
	 * Return a readable String of the amount of time since a provided time.
	 *
	 * @param time  The provided time.
	 * @param round Round to an int.
	 * @return The readable String.
	 */
	public static String getTimeTagged(long time, boolean round)
	{
		DecimalFormat format = round ? new DecimalFormat("#") : new DecimalFormat("#.##");
		if(getHours(time) >= 1) return format.format(getHours(time)) + "h";
		else if(Double.valueOf(format.format(getMinutes(time))) >= 1) return format.format(getMinutes(time)) + "m";
		else return format.format(getSeconds(time)) + "s";
	}

	public static String prettyDate(long time)
	{
		long diff = (System.currentTimeMillis() - time) / 1000;
		double day_diff = Math.floor(diff / 86400);

		if(day_diff == 0 && diff < 60) return "just now";
		if(diff < 120) return "1 minute ago";
		if(diff < 3600) return Math.floor(diff / 60) + " minutes ago";
		if(diff < 7200) return "1 hour ago";
		if(diff < 86400) return Math.floor(diff / 3600) + " hours ago";
		if(day_diff == 1) return "Yesterday";
		if(day_diff < 7) return day_diff + " days ago";
		if(day_diff < 31) return Math.ceil(day_diff / 7) + " weeks ago";

		return new SimpleDateFormat("yyyy-MM-dd").format(new Date(time));
	}
}
