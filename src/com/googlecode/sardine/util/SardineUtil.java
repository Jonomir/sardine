package com.googlecode.sardine.util;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;

/**
 * Basic utility code. I borrowed some code from the webdavlib for
 * parsing dates.
 *
 * @author jonstevens
 */
public class SardineUtil
{
	/** cached version of getResources() webdav xml GET request */
	private static StringEntity GET_RESOURCES = null;

	/**
	 * Date formats using for Date parsing.
	 */
	static final SimpleDateFormat formats[] =
	{
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
		new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US),
		new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
		new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
		new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
		new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US)
	};

	/**
	 * GMT timezone.
	 */
	final static TimeZone gmtZone = TimeZone.getTimeZone("GMT");

	static
	{
		for (SimpleDateFormat format : formats)
		{
			format.setTimeZone(gmtZone);
		}
	}

	/**
	 * Hides the irritating declared exception.
	 */
	public static String encode(String value)
	{
		try
		{
			return URLEncoder.encode(value, "utf-8");
		}
		catch (UnsupportedEncodingException ex) { throw new RuntimeException(ex); }
	}

	/**
	 * Hides the irritating declared exception.
	 * @return null if there is an IllegalArgumentException
	 * @throws RuntimeException if there is an UnsupportedEncodingException
	 */
	public static String decode(String value)
	{
		try
		{
			return URLDecoder.decode(value, "utf-8");
		}
		catch (UnsupportedEncodingException ex) { throw new RuntimeException(ex); }
		catch (IllegalArgumentException ex) { return null; }
	}

	/**
	 * Loops over all the possible date formats and tries to find the right one.
	 * @param dateValue
	 */
	public static Date parseDate(String dateValue)
	{
		Date date = null;
		for (int i = 0; (date == null) && (i < formats.length); i++)
		{
			try
			{
				synchronized (formats[i])
				{
					date = formats[i].parse(dateValue);
				}
			}
			catch (ParseException e)
			{
			}
		}

		return date;
	}

	/**
	 * Simple class for making propfind a bit easier to deal with.
	 */
	public static class HttpPropFind extends HttpEntityEnclosingRequestBase
	{
		public HttpPropFind(String url)
		{
			super();
			this.setDepth(1);
			this.setURI(URI.create(url));
		}

		@Override
		public String getMethod()
		{
			return "PROPFIND";
		}

		public void setDepth(int val)
		{
			this.setHeader("Depth", String.valueOf(val));
		}
	}

	/**
	 * Is the status code 2xx
	 */
	public static boolean isGoodResponse(int statusCode)
	{
		return (statusCode >= 200 && statusCode <= 299);
	}

	/**
	 * Stupid wrapper cause it needs to be in a try/catch
	 */
	public static StringEntity getResourcesEntity()
	{
		if (GET_RESOURCES == null)
		{
			try
			{
				GET_RESOURCES = new StringEntity("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
													"<propfind xmlns=\"DAV:\">\n" +
													"	<allprop/>\n" +
													"</propfind>", "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				// Ignored
			}
		}

		return GET_RESOURCES;
	}
}