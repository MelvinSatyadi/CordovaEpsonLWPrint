/*
 * Logger.java
 *
 * Project: LW-Print SDK
 *
 * Contains: Logger class
 *
 * Copyright (C) 2013-2019 SEIKO EPSON CORPORATION. All Rights Reserved.
 */
package com.melvinsatyadi.cordova.plugin;

import android.os.Debug;
import android.util.Log;

public class Logger {

	private static final String TAG = "LWPrintSimpleSample";
	private static final boolean debug = true;

	public static final void d(String msg) {
		if (debug) {
			Log.d(TAG, msg);
		}
	}

	public static final void d(String msg, Throwable tr) {
		if (debug) {
			Log.d(TAG, msg, tr);
		}
	}

	public static final void d(String tag, String msg) {
		if (debug) {
			Log.d(tag, msg);
		}
	}

	public static final void d(String tag, String msg, Throwable tr) {
		if (debug) {
			Log.d(tag, msg, tr);
		}
	}

	public static final void e(String msg) {
		if (debug) {
			Log.e(TAG, msg);
		}
	}

	public static final void e(String msg, Throwable tr) {
		if (debug) {
			Log.e(TAG, msg, tr);
		}
	}

	public static final void e(String tag, String msg) {
		if (debug) {
			Log.e(tag, msg);
		}
	}

	public static final void e(String tag, String msg, Throwable tr) {
		if (debug) {
			Log.e(tag, msg, tr);
		}
	}

	public static final void i(String msg) {
		if (debug) {
			Log.i(TAG, msg);
		}
	}

	public static final void i(String msg, Throwable tr) {
		if (debug) {
			Log.i(TAG, msg, tr);
		}
	}

	public static final void i(String tag, String msg) {
		if (debug) {
			Log.i(tag, msg);
		}
	}

	public static final void i(String tag, String msg, Throwable tr) {
		if (debug) {
			Log.i(tag, msg, tr);
		}
	}

	public static final void v(String msg) {
		if (debug) {
			Log.v(TAG, msg);
		}
	}

	public static final void v(String msg, Throwable tr) {
		if (debug) {
			Log.v(TAG, msg, tr);
		}
	}

	public static final void v(String tag, String msg) {
		if (debug) {
			Log.v(tag, msg);
		}
	}

	public static final void v(String tag, String msg, Throwable tr) {
		if (debug) {
			Log.v(tag, msg, tr);
		}
	}

	public static final void w(String msg) {
		if (debug) {
			Log.w(TAG, msg);
		}
	}

	public static final void w(String msg, Throwable tr) {
		if (debug) {
			Log.w(TAG, msg, tr);
		}
	}

	public static final void w(String tag, String msg) {
		if (debug) {
			Log.w(tag, msg);
		}
	}

	public static final void w(String tag, String msg, Throwable tr) {
		if (debug) {
			Log.w(tag, msg, tr);
		}
	}

	public static final void heap() {
		heap(TAG);
	}

	public static final void heap(String tag) {
		if (debug) {
			String msg = "heap : Free="
					+ Long.toString(Debug.getNativeHeapFreeSize() / 1024)
					+ "kb" + ", Allocated="
					+ Long.toString(Debug.getNativeHeapAllocatedSize() / 1024)
					+ "kb" + ", Size="
					+ Long.toString(Debug.getNativeHeapSize() / 1024) + "kb";

			Log.v(tag, msg);
		}
	}

}
