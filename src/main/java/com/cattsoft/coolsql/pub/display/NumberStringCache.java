package com.cattsoft.coolsql.pub.display;

/**
 *
 */
public class NumberStringCache
{
	// As this is class is used to cache the String representation for 
	// Line numbers in the editor, caching 5000 numbers should suffice
	// for most cases. Any editor text larger than that will fall back
	// to the objects cached in overflowMap
	public static final int CACHE_SIZE = 5000;
	private static final String[] cache = new String[CACHE_SIZE];

	private static final String[] hexCache = new String[256];
	
	private NumberStringCache()
	{
	}

	public static String getHexString(int value)
	{
		if (value > 255 || value < 0) return Integer.toHexString(value);
		if (hexCache[value] == null)
		{
			if (value < 16)
			{
				hexCache[value] = "0" + Integer.toHexString(value);
			}
			else
			{
				hexCache[value] = Integer.toHexString(value);
			}
			
		}
		return hexCache[value];
	}
	
	public static String getNumberString(long lvalue)
	{
		if (lvalue < 0 || lvalue >= CACHE_SIZE) return Long.toString(lvalue);

		int value = (int)lvalue;
		// I'm not synchronizing this, because the worst that can 
		// happen is, that the same number is created two or three times
		// instead of exactly one time. 
		// And as this is most of the time called from Swing Event Thread
		// it is more or less a single-threaded access anyway.
		if (cache[value] == null)
		{
			cache[value] = Integer.toString(value);
		}
		return cache[value];
	}
	
}