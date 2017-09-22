package com.hankora817.nightshift.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtil
{
	private static final String SEEK_VALUE = "seek_value";
	private static final String SCEDULE_SETTING = "scedule_setting";
	private static final String START_TIME = "start_time";
	private static final String END_TIME = "end_time";
	
	private static SharedPreferences _preference;
	
	
	public static SharedPreferences instance(Context context)
	{
		if (_preference == null)
			_preference = PreferenceManager.getDefaultSharedPreferences(context);
		return _preference;
	}
	
	
	public static void setSeekValue(int value, Context context)
	{
		put(SEEK_VALUE, value, context);
	}
	
	
	public static Integer getSeekValue(Context context)
	{
		return get(SEEK_VALUE, 0, context);
	}
	
	
	public static Boolean isSceduled(Context context)
	{
		return get(SCEDULE_SETTING, false, context);
	}
	
	
	public static void setSceduled(boolean isSceduled, Context context)
	{
		put(SCEDULE_SETTING, isSceduled, context);
	}
	
	
	public static void setStartTime(String time, Context context)
	{
		put(START_TIME, time, context);
	}
	
	
	public static String getStartTime(Context context)
	{
		return getWithNullToStart(START_TIME, context);
	}
	
	
	public static void setEndTime(String time, Context context)
	{
		put(END_TIME, time, context);
	}
	
	
	public static String getEndTime(Context context)
	{
		return getWithNullToEnd(END_TIME, context);
	}
	
	
	/**
	 * key 수동 설정
	 * 
	 * @param key
	 *            키 값
	 * @param value
	 *            내용
	 */
	public static void put(String key, String value, Context context)
	{
		SharedPreferences p = instance(context);
		SharedPreferences.Editor editor = p.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	
	/**
	 * String 값 가져오기
	 * 
	 * @param key
	 *            키 값
	 * @return String (기본값 null)
	 */
	public static String get(String key, Context context)
	{
		SharedPreferences p = instance(context);
		return p.getString(key, null);
	}
	
	
	/**
	 * String 값 가져오기
	 * 
	 * @param key
	 *            키 값
	 * @return String (기본값 "")
	 */
	public static String getWithNullToBlank(String key, Context context)
	{
		SharedPreferences p = instance(context);
		return p.getString(key, "");
	}
	
	
	public static String getWithNullToStart(String key, Context context)
	{
		SharedPreferences p = instance(context);
		return p.getString(key, "21:00");
	}
	
	
	public static String getWithNullToEnd(String key, Context context)
	{
		SharedPreferences p = instance(context);
		return p.getString(key, "07:00");
	}
	
	
	/**
	 * key 설정
	 * 
	 * @param key
	 *            키 값
	 * @param value
	 *            내용
	 */
	public static void put(String key, boolean value, Context context)
	{
		SharedPreferences p = instance(context);
		SharedPreferences.Editor editor = p.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	
	/**
	 * Boolean 값 가져오기
	 * 
	 * @param key
	 *            키 값
	 * @return Boolean
	 */
	public static boolean get(String key, boolean defaultValue, Context context)
	{
		SharedPreferences p = instance(context);
		return p.getBoolean(key, defaultValue);
	}
	
	
	/**
	 * key 설정
	 * 
	 * @param key
	 *            키 값
	 * @param value
	 *            내용
	 */
	public static void put(String key, int value, Context context)
	{
		SharedPreferences p = instance(context);
		SharedPreferences.Editor editor = p.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	
	/**
	 * int 값 가져오기
	 * 
	 * @param key
	 *            키 값
	 * @return int
	 */
	public static int get(String key, int defaultValue, Context context)
	{
		SharedPreferences p = instance(context);
		return p.getInt(key, defaultValue);
	}
}
