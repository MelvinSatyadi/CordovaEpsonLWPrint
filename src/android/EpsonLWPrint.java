package com.melvinsatyadi.cordova.plugin;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

//Epson libs
import com.epson.lwprint.sdk.LWPrintDiscoverPrinter;
import com.epson.lwprint.sdk.LWPrintDiscoverPrinterCallback;
import com.epson.lwprint.sdk.LWPrintDiscoverConnectionType;

// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//java imports
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.EnumSet;

//android imports
import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;

public class EpsonLWPrint extends CordovaPlugin {

	public static final int REQUEST_BLUETOOTH_PERMISSION = 1;

	private static final String SEP = System.getProperty("line.separator");
	private String type = "_pdl-datastream._tcp.local.";

	List<String> dataList = new ArrayList<String>();
	List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();

	ServiceCallback listener;
	LWPrintDiscoverPrinter lpPrintDiscoverPrinter;

	android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());

	Context myDiscoverContext;
	CallbackContext myDiscoverCallbackContext;


	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) {
		// Verify that the user sent a 'show' action
		if (action.equals("startDiscover")) {
			startDiscover(callbackContext);
			callbackContext.success("Discover Started");
			return true;
		} else if (action.equals("stopDiscover")) {
			callbackContext.success("\"" + action + "\" not implemented yet.");
			return true;
		} else if (action.equals("getDeviceList")) {
			getDeviceList(callbackContext);
			return true;
			
		} 
		else if (action.equals("checkPermissions")) {
			if (PermissionChecker.checkSelfPermission(this.cordova.getContext(), android.Manifest.permission.BLUETOOTH_SCAN) != PermissionChecker.PERMISSION_GRANTED) {  
				ActivityCompat.requestPermissions(
					this.cordova.getActivity(),    
					new String[] { android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT },
					REQUEST_BLUETOOTH_PERMISSION
				);
			}
			callbackContext.success("True");
			return true;
		}
		else {
			callbackContext.error("\"" + action + "\" is not a recognized action.");
			return false;
		}
	}

	void startDiscover(CallbackContext callbackContext) {
		myDiscoverCallbackContext = callbackContext;
		List<String> typeList = new ArrayList<String>();
		typeList.add(type);

		EnumSet<LWPrintDiscoverConnectionType> flag = EnumSet.of(LWPrintDiscoverConnectionType.ConnectionTypeBluetooth);
		lpPrintDiscoverPrinter = new LWPrintDiscoverPrinter(null, null, flag);

		// lpPrintDiscoverPrinter = new LWPrintDiscoverPrinter(typeList);

		// Sets the callback
		lpPrintDiscoverPrinter.setCallback(listener = new ServiceCallback());
		// Starts discovery
		try {
			myDiscoverContext = this.cordova.getActivity().getApplicationContext();
			lpPrintDiscoverPrinter.startDiscover(myDiscoverContext);
		} catch (Exception e) {
			callbackContext.error("Error starting discovery! ");
		}
		callbackContext.success();
	}

	void getDeviceList(CallbackContext callbackContext) {
		JSONArray json = new JSONArray();
		for (DeviceInfo info : deviceList) {
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put("name", info.getName());
				jsonObj.put("host", info.getHost());
				jsonObj.put("mac", info.getMacaddress());
			} catch (JSONException e) {

			}
			json.put(jsonObj);
		}
		callbackContext.success(json.toString());
	}

	class ServiceCallback implements LWPrintDiscoverPrinterCallback {

		@Override
		public void onFindPrinter(LWPrintDiscoverPrinter discoverPrinter,
				Map<String, String> printer) {
			// Called when printers are detected

			for (DeviceInfo info : deviceList) {
				if (info.getName().equals(printer.get(LWPrintDiscoverPrinter.PRINTER_INFO_NAME))
						&& info.getHost().equals(printer.get(LWPrintDiscoverPrinter.PRINTER_INFO_HOST))
						&& info.getMacaddress()
								.equals(printer.get(LWPrintDiscoverPrinter.PRINTER_INFO_SERIAL_NUMBER))) {
					return;
				}
			}

			String type = (String) printer.get(LWPrintDiscoverPrinter.PRINTER_INFO_TYPE);
			String status = (String) printer.get(LWPrintDiscoverPrinter.PRINTER_INFO_DEVICE_STATUS);

			DeviceInfo obj = new DeviceInfo();
			obj.setName((String) printer
					.get(LWPrintDiscoverPrinter.PRINTER_INFO_NAME));
			obj.setProduct((String) printer
					.get(LWPrintDiscoverPrinter.PRINTER_INFO_PRODUCT));
			obj.setUsbmdl((String) printer
					.get(LWPrintDiscoverPrinter.PRINTER_INFO_USBMDL));
			obj.setHost((String) printer
					.get(LWPrintDiscoverPrinter.PRINTER_INFO_HOST));
			obj.setPort((String) printer
					.get(LWPrintDiscoverPrinter.PRINTER_INFO_PORT));
			obj.setType(type);
			obj.setDomain((String) printer
					.get(LWPrintDiscoverPrinter.PRINTER_INFO_DOMAIN));
			obj.setMacaddress((String) printer
					.get(LWPrintDiscoverPrinter.PRINTER_INFO_SERIAL_NUMBER));
			obj.setDeviceClass((String) printer
					.get(LWPrintDiscoverPrinter.PRINTER_INFO_DEVICE_CLASS));
			obj.setDeviceStatus(status);

			deviceList.add(obj);

			if (TextUtils.isEmpty(obj.getMacaddress())) {
				// Wi-Fi
				dataList.add((String) printer
						.get(LWPrintDiscoverPrinter.PRINTER_INFO_NAME)
						+ SEP
						+ (String) printer
								.get(LWPrintDiscoverPrinter.PRINTER_INFO_HOST)
						+ SEP
						+ (String) printer
								.get(LWPrintDiscoverPrinter.PRINTER_INFO_TYPE));
			} else {
				if (TextUtils.isEmpty(status)) {
					// Bluetooth
					dataList.add((String) printer
							.get(LWPrintDiscoverPrinter.PRINTER_INFO_NAME)
							+ SEP
							+ (String) printer
									.get(LWPrintDiscoverPrinter.PRINTER_INFO_SERIAL_NUMBER)
							+ SEP
							+ (String) printer
									.get(LWPrintDiscoverPrinter.PRINTER_INFO_DEVICE_CLASS));
				} else {
					// Wi-Fi Direct
					int deviceStatus = -1;
					try {
						deviceStatus = Integer.parseInt(status);
					} catch (NumberFormatException e) {
					}
					dataList.add((String) printer
							.get(LWPrintDiscoverPrinter.PRINTER_INFO_NAME)
							+ SEP
							+ (String) printer
									.get(LWPrintDiscoverPrinter.PRINTER_INFO_SERIAL_NUMBER)
							+ SEP
							+ getDeviceStatusForWifiDirect(deviceStatus));
				}
			}
		}

		private String getDeviceStatusForWifiDirect(int deviceStatus) {
			switch (deviceStatus) {
				case 0:
					return "Connected";
				case 1:
					return "Invited";
				case 2:
					return "Failed";
				case 3:
					return "Available";
				case 4:
					return "Unavailable";
				default:
					return "Unknown";
			}
		}

		@Override
		public void onRemovePrinter(LWPrintDiscoverPrinter discoverPrinter,
				Map<String, String> printer) {
			// Called when printers have been deleted

			String name = (String) printer
					.get(LWPrintDiscoverPrinter.PRINTER_INFO_NAME);
			int index = -1;
			for (int i = 0; i < deviceList.size(); i++) {
				DeviceInfo info = deviceList.get(i);
				if (name.equals(info.getName())) {
					index = i;
					break;
				}
			}
			if (index >= 0) {
				deviceList.remove(index);
			}
		}
		
	}
}