package com.melvinsatyadi.cordova.plugin;

//Epson libs
import com.epson.lwprint.sdk.LWPrintDiscoverPrinter;
import com.epson.lwprint.sdk.LWPrintDiscoverPrinterCallback;

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


public class EpsonLWPrint extends CordovaPlugin {

	List<String> dataList = new ArrayList<String>();
	List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();
	

  @Override
  public boolean execute(String action, JSONArray args,
    CallbackContext callbackContext) {
      // Verify that the user sent a 'show' action
      if (action.equals("startDiscover")) {
        startDiscover(callbackContext);
        callbackContext.success("Discover Started");
        return true;
      }
      else if (action.equals("stopDiscover")){
        callbackContext.success("\"" + action + "\" not implemented yet.");
        return true;
      }
      else if (action.equals("getDeviceList")){
        getDeviceList(callbackContext);
        return true;
      }
      else{
        callbackContext.error("\"" + action + "\" is not a recognized action.");
        return false;
      }
  }

  boolean startDiscover(CallbackContext callbackContext){
    EnumSet<LWPrintDiscoverConnectionType> flag = EnumSet.of(LWPrintDiscoverConnectionType.ConnectionTypeBluetooth);
		lpPrintDiscoverPrinter = new LWPrintDiscoverPrinter(null, null, flag);

		// Sets the callback
		lpPrintDiscoverPrinter.setCallback(listener = new ServiceCallback());
		// Starts discovery
		try{
			lpPrintDiscoverPrinter.startDiscover(this);
		}
		catch (Exception e) {
			callbackContext.error("Error starting discovery! ");
		}
		callbackContext.success();
  }

  boolean getDeviceList(CallbackContext callbackContext){
    JSONArray json = new JSONArray();
    for (DeviceInfo info : deviceList){
      jsonObj = new JSONObject();
      jsonObj.put(name,info.getName());
      jsonObj.put(host,info.getHost());
      jsonObj.put(mac,info.getMacaddress());
      json.add(jsonObj);
    }
    callbackContext.success(json);
  }

  class ServiceCallback implements LWPrintDiscoverPrinterCallback {

		@Override
		public void onFindPrinter(LWPrintDiscoverPrinter discoverPrinter,
				Map<String, String> printer) {
			// Called when printers are detected

            for (DeviceInfo info : deviceList) {
            	if (info.getName().equals(printer.get(LWPrintDiscoverPrinter.PRINTER_INFO_NAME))
                 && info.getHost().equals(printer.get(LWPrintDiscoverPrinter.PRINTER_INFO_HOST))
            	 && info.getMacaddress().equals(printer.get(LWPrintDiscoverPrinter.PRINTER_INFO_SERIAL_NUMBER))) {
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
				notifyAdd((String) printer
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
					notifyAdd((String) printer
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
					notifyAdd((String) printer
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
				notifyRemove(index);
				deviceList.remove(index);
			}
		}

	}
  private void notifyAdd(final String name) {
		handler.postDelayed(new Runnable() {
			public void run() {
				dataList.add(name);
				//adapter.notifyDataSetChanged();
			}
		}, 1);
	}

	private void notifyUpdate(final int index, final String name) {
		handler.postDelayed(new Runnable() {
			public void run() {
				dataList.set(index, name);
				//adapter.notifyDataSetChanged();
			}
		}, 1);
	}

	private void notifyRemove(final int index) {
		handler.postDelayed(new Runnable() {
			public void run() {
				dataList.remove(index);
				adapter.notifyDataSetChanged();
			}
		}, 1);
	}
}