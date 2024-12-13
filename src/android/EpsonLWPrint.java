package com.melvinsatyadi.cordova.plugin;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

//Epson libs
import com.epson.lwprint.sdk.LWPrint;
import com.epson.lwprint.sdk.LWPrintDiscoverPrinter;
import com.epson.lwprint.sdk.LWPrintDiscoverPrinterCallback;
import com.epson.lwprint.sdk.LWPrintDiscoverConnectionType;
import com.epson.lwprint.sdk.LWPrintCallback;
import com.epson.lwprint.sdk.LWPrintConnectionStatus;

import com.epson.lwprint.sdk.LWPrintParameterKey;
import com.epson.lwprint.sdk.LWPrintPrintingPhase;
import com.epson.lwprint.sdk.LWPrintStatusError;
import com.epson.lwprint.sdk.LWPrintTapeCut;
import com.epson.lwprint.sdk.LWPrintTapeWidth;
import com.epson.lwprint.sdk.LWPrintDataProvider;

// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//java imports
import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//import java.util.logging.Handler;
import java.util.EnumSet;
import java.util.HashMap;

//android imports
import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.bluetooth.BluetoothAdapter;
import android.content.res.AssetManager;
import android.util.Base64;

public class EpsonLWPrint extends CordovaPlugin {

	

	Map<String, String> printerInfo = null;
	Map<String, Integer> lwStatus = null;

	public static final int REQUEST_BLUETOOTH_PERMISSION = 1;
	public static final int REQUEST_LOCATION = 1;

	private static final String SEP = System.getProperty("line.separator");
	private String type = "_pdl-datastream._tcp.local.";

	

	List<String> dataList = new ArrayList<String>();
	List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>();

	LWPrint lwprint;
	ServiceCallback listener;
	LWPrintDiscoverPrinter lpPrintDiscoverPrinter;

	

	android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());

	Context myDiscoverContext;
	CallbackContext myDiscoverCallbackContext;
	
	public EpsonLWPrint(){
		initialize();
	}
	

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		// Verify that the user sent a 'show' action
		if (action.equals("initialize")) {
			initialize();
			callbackContext.success("Initialized");
			return true;
		}
		else if(action.equals("startDiscover")) {
			startDiscover(callbackContext);
			callbackContext.success("Discover Started");
			return true;
		} else if (action.equals("stopDiscover")) {
			stopDiscover(callbackContext);
			//callbackContext.success("\"" + action + "\" not implemented yet.");
			return true;
		} else if (action.equals("debugLog")) {
			Logger.d(args.getString(0));
			callbackContext.success();
			return true;
		} else if (action.equals("getDeviceList")) {
			getDeviceList(callbackContext);
			return true;

		} else if (action.equals("checkPermissions")) {
			if (PermissionChecker.checkSelfPermission(this.cordova.getContext(),
					android.Manifest.permission.BLUETOOTH_SCAN) != PermissionChecker.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(
						this.cordova.getActivity(),
						new String[] { android.Manifest.permission.BLUETOOTH_SCAN,
								android.Manifest.permission.BLUETOOTH_CONNECT },
						REQUEST_BLUETOOTH_PERMISSION);
				callbackContext.success("Permission requested.");
			} else {
				callbackContext.success("Already granted!");
			}

			if (PermissionChecker.checkSelfPermission(this.cordova.getContext(),
			android.Manifest.permission.ACCESS_FINE_LOCATION) != PermissionChecker.PERMISSION_GRANTED) {
		ActivityCompat.requestPermissions(
				this.cordova.getActivity(),
				new String[] { android.Manifest.permission.ACCESS_FINE_LOCATION },
				REQUEST_LOCATION);
		callbackContext.success("Permission requested.");
	} else {
		callbackContext.success("Already granted!");
	}

		

			return true;
		} else if (action.equals("printImage")) {
			Logger.d("Called printImage");
			String base64 = args.getString(0);
			printImage(callbackContext, base64);
			return true;
		} else if (action.equals("printText")) {
			String text = args.getString(0);
			printText(callbackContext, text);
			return true;
		} 
		else if (action.equals("setPrinterInfo")) {
			String infoJSON = args.getString(0);
			setPrinterInfo(callbackContext, infoJSON);
			return true;
		}
		else {
			callbackContext.error("\"" + action + "\" is not a recognized action.");
			return false;
		}
	}

	void initialize() {
		Logger.d("Initialize library start");
		final Context self = this.cordova.getContext();
		lwprint = new LWPrint(self);
		PrintCallback printListener = new PrintCallback();

		lwprint.setCallback(printListener);

		EnumSet<LWPrintDiscoverConnectionType> flag = EnumSet.of(LWPrintDiscoverConnectionType.ConnectionTypeBluetooth);
		lpPrintDiscoverPrinter = new LWPrintDiscoverPrinter(null, null, flag);
		Logger.d("Initialize library stop");
	}

	void startDiscover(CallbackContext callbackContext) {
		myDiscoverCallbackContext = callbackContext;
		List<String> typeList = new ArrayList<String>();
		typeList.add(type);

		//EnumSet<LWPrintDiscoverConnectionType> flag = EnumSet.of(LWPrintDiscoverConnectionType.ConnectionTypeBluetooth);
		//lpPrintDiscoverPrinter = new LWPrintDiscoverPrinter(null, null, flag);

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

	void stopDiscover(CallbackContext callbackContext) {
		try {
			Logger.d("stopping discovery");
			lpPrintDiscoverPrinter.stopDiscover();
			Logger.d("Discovery stopped");
		} catch (Exception e) {
			Logger.d("Error stopping discovery");
			callbackContext.error("Error stopping discovery! ");
		}
		callbackContext.success("Discovery stopped");
	}

	void getDeviceList(CallbackContext callbackContext) {
		JSONArray json = new JSONArray();
		for (DeviceInfo info : deviceList) {
			
			JSONObject jsonObj = new JSONObject();
			try {
				jsonObj.put(LWPrintDiscoverPrinter.PRINTER_INFO_NAME, info.getName());
				jsonObj.put(LWPrintDiscoverPrinter.PRINTER_INFO_HOST, info.getHost());
				jsonObj.put(LWPrintDiscoverPrinter.PRINTER_INFO_SERIAL_NUMBER, info.getMacaddress());
				jsonObj.put(LWPrintDiscoverPrinter.PRINTER_INFO_PRODUCT, info.getProduct());
				jsonObj.put(LWPrintDiscoverPrinter.PRINTER_INFO_USBMDL, info.getUsbmdl());
				jsonObj.put(LWPrintDiscoverPrinter.PRINTER_INFO_PORT, info.getPort());
				jsonObj.put(LWPrintDiscoverPrinter.PRINTER_INFO_TYPE, info.getType());
				jsonObj.put(LWPrintDiscoverPrinter.PRINTER_INFO_DOMAIN, info.getDomain());
				jsonObj.put(LWPrintDiscoverPrinter.PRINTER_INFO_DEVICE_CLASS, info.getDeviceClass());
				jsonObj.put(LWPrintDiscoverPrinter.PRINTER_INFO_DEVICE_STATUS, info.getDeviceStatus());
			} catch (JSONException e) {

			}
			json.put(jsonObj);
		}
		callbackContext.success(json.toString());
	}

	void setPrinterInfo(CallbackContext callbackContext, String printerInfoJSON) {
		if (printerInfo != null) {
			printerInfo.clear();
			printerInfo = null;
		}

		String printerInfoJSONClean = printerInfoJSON.replace("{", "");
		printerInfoJSONClean = printerInfoJSONClean.replace("}", "");
		printerInfoJSONClean = printerInfoJSONClean.replace("[", "");
		printerInfoJSONClean = printerInfoJSONClean.replace("]", "");

		printerInfo = new HashMap<String, String>();
		String[] pairs = printerInfoJSONClean.split(",");
		for (int i = 0; i < pairs.length; i++){
			String pair = pairs[i];
			String[] keyValue = pair.split(":");
			printerInfo.put(keyValue[0], keyValue[1].replace("\\\\", ":"));
		}

		callbackContext.success("Printer info is set! " + printerInfo.toString());
	}

	void printText(CallbackContext callbackContext, String textToPrint) {
		if (printerInfo == null) {
			callbackContext.error("Printer info not set!");
			return;
		}
	

		SampleDataProvider sampleDataProvider = new SampleDataProvider();

	
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Boolean printResult;

				// Set printing information
				lwprint.setPrinterInformation(printerInfo);

				// Obtain printing status
				lwStatus = lwprint.fetchPrinterStatus();
				int deviceError = lwprint.getDeviceErrorFromStatus(lwStatus);
				if (lwStatus.isEmpty() || (deviceError == LWPrintStatusError.ConnectionFailed)) {
					printResult = false;
				} else {
					// Make a print parameter
					int tapeWidth = lwprint.getTapeWidthFromStatus(lwStatus);

					Map<String, Object> printParameter = new HashMap<String, Object>();
					// Number of copies(1 ... 99)
					printParameter.put(LWPrintParameterKey.Copies, 1);
					// Tape cut method(LWPrintTapeCut)
					printParameter.put(LWPrintParameterKey.TapeCut, LWPrintTapeCut.EachLabel);
					// Set half cut (true:half cut on)
					printParameter.put(LWPrintParameterKey.HalfCut, lwprint.isSupportHalfCut());
					// Low speed print setting (true:low speed print on)
					printParameter.put(LWPrintParameterKey.PrintSpeed, false);
					// Print density(-5 ... 5)
					printParameter.put(LWPrintParameterKey.Density, 0);
					// Tape width(LWPrintTapeWidth)
					printParameter.put(LWPrintParameterKey.TapeWidth, tapeWidth);

					sampleDataProvider.setFormType(FormType.String);
					sampleDataProvider.setStringData(textToPrint);
					lwprint.doPrint(sampleDataProvider, printParameter);

					printResult = true;
				}

				final Boolean result = printResult;
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						if (result == false) {
							// setProcessing(false);

							String message = "Can't get printer status.";
							callbackContext.error(message);
							// alertAbortOperation("Error", message);
						} else {
							callbackContext.success("Print success " + textToPrint + " " + lwStatus.toString());
						}
					}
				});
			}
		});
		// callbackContext.success("");
	}

	void printImage(CallbackContext callbackContext, String imageBase64) {
		Logger.d("Running printImage");
		
		Logger.d("Image Base64 snippet is : " + imageBase64.substring(0, 50));

		final byte[] decodedBytes = Base64.decode(imageBase64, Base64.DEFAULT);
		Bitmap imageToPrint = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);

		Logger.d(imageToPrint.toString());

		if (printerInfo == null) {
			callbackContext.error("Printer info not set!");
			return;
		}


		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Boolean printResult;

				// Set printing information
				Logger.d("Setting printer information : " + printerInfo.toString());
				lwprint.setPrinterInformation(printerInfo);

				// Obtain printing status
				lwStatus = lwprint.fetchPrinterStatus();
				Logger.d("Get printer status : " + lwStatus.toString());
				int deviceError = lwprint.getDeviceErrorFromStatus(lwStatus);
				Logger.d("Get printer error : " + deviceError);
				if (lwStatus.isEmpty() || (deviceError == LWPrintStatusError.ConnectionFailed)) {
					printResult = false;
				} else {
					// Make a print parameter
					int tapeWidth = lwprint.getTapeWidthFromStatus(lwStatus); //Q
					Logger.d("Tape width : " + tapeWidth);
					Map<String, Object> printParameter = new HashMap<String, Object>();
					// Number of copies(1 ... 99)
					printParameter.put(LWPrintParameterKey.Copies, 1);
					// Tape cut method(LWPrintTapeCut)
					printParameter.put(LWPrintParameterKey.TapeCut, LWPrintTapeCut.EachLabel);
					// Set half cut (true:half cut on)
					printParameter.put(LWPrintParameterKey.HalfCut, lwprint.isSupportHalfCut());
					// Low speed print setting (true:low speed print on)
					printParameter.put(LWPrintParameterKey.PrintSpeed, false);
					// Print density(-5 ... 5)
					printParameter.put(LWPrintParameterKey.Density, 0);
					// Tape width(LWPrintTapeWidth)
					printParameter.put(LWPrintParameterKey.TapeWidth, tapeWidth);
					
					Logger.d("Print parameter : " + printParameter.toString());
					if (imageToPrint != null) {
						lwprint.doPrintImage(imageToPrint, printParameter);
					}
					/*
					 * String item = spinnerData.getSelectedItem().toString();
					 * 
					 * switch (item) {
					 * case "Text":
					 * sampleDataProvider.setFormType(FormType.String);
					 * lwprint.doPrint(sampleDataProvider, printParameter);
					 * break;
					 * case "QRCode":
					 * sampleDataProvider.setFormType(FormType.QRCode);
					 * lwprint.doPrint(sampleDataProvider, printParameter);
					 * break;
					 * case "Img1":
					 * Bitmap bitmap1 = createBitmap("Hello Tape:1");
					 * lwprint.doPrint(bitmap1, printParameter);
					 * break;
					 * case "Img2":
					 * Bitmap bitmap2 = createBitmap("Hello Tape:2");
					 * lwprint.doPrint(bitmap2, printParameter);
					 * break;
					 * case "Imgs":
					 * Bitmap bitmap3 = createBitmap("Hello Tape:1");
					 * Bitmap bitmap4 = createBitmap("Hello Tape:2");
					 * 
					 * if (bitmap3 != null && bitmap4 != null) {
					 * ArrayList<Bitmap> bitmaps = new ArrayList<Bitmap>(
					 * Arrays.asList(bitmap3, bitmap4)
					 * );
					 * lwprint.doPrint(bitmaps, printParameter);
					 * }
					 * break;
					 * default:
					 * break;
					 * }
					 */
					printResult = true;
				}

				final Boolean result = printResult;
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					@Override
					public void run() {
						if (result == false) {
							// setProcessing(false);

							String message = "Can't get printer status.";
							callbackContext.error(message);
							// alertAbortOperation("Error", message);
						} else {
							callbackContext.success("Print success");
						}
					}
				});
			}
		});
		// callbackContext.success("");
	}
/* 
	public static Bitmap decodeBase64(String input) {
		byte[] decodedBytes = Base64.getDecoder().decode(input.getBytes());;
		return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
	}
*/
	class ServiceCallback implements LWPrintDiscoverPrinterCallback {

		@Override
		public void onFindPrinter(LWPrintDiscoverPrinter discoverPrinter,
				Map<String, String> printer) {
			// Called when printers are detected
					Logger.d(printer.toString());
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
			//lpPrintDiscoverPrinter.stopDiscover();
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

	public enum FormType {
		String,
		QRCode;
	}

	class SampleDataProvider implements LWPrintDataProvider {

		private static final String FORM_DATA_STRING = "FormDataString.plist";
		private static final String FORM_DATA_QRCODE = "FormDataQRCode.plist";

		private FormType formType = FormType.String;
		private String stringData = "String";
		private String qrCodeData = "QRCode";

		InputStream formDataStringInputStream;
		InputStream formDataQRCodeInputStream;

		public FormType getFormType() {
			return formType;
		}

		public void setFormType(FormType formType) {
			this.formType = formType;
		}

		public String getStringData() {
			return stringData;
		}

		public void setStringData(String stringData) {
			this.stringData = stringData;
		}

		public String getQrCodeData() {
			return qrCodeData;
		}

		public void setQrCodeData(String qrCodeData) {
			this.qrCodeData = qrCodeData;
		}

		public void closeStreams() {
			if (formDataStringInputStream != null) {
				try {
					formDataStringInputStream.close();
				} catch (IOException e) {
					// Logger.e(e.toString(), e);
				}
				formDataStringInputStream = null;
			}
			if (formDataQRCodeInputStream != null) {
				try {
					formDataQRCodeInputStream.close();
				} catch (IOException e) {
					// Logger.e(e.toString(), e);
				}
				formDataQRCodeInputStream = null;
			}
		}

		@Override
		public void startOfPrint() {
			// It is called only once when printing started
			Logger.d("startOfPrint");
		}

		@Override
		public void endOfPrint() {
			// It is called only once when printing finished
			Logger.d("endOfPrint");
		}

		@Override
		public void startPage() {
			// It is called when starting a page
			Logger.d("startPage");
		}

		@Override
		public void endPage() {
			// It is called when finishing a page
			Logger.d("endPage");
		}

		@Override
		public int getNumberOfPages() {
			// Return all pages printed
			Logger.d("getNumberOfPages");

			return 1;
		}

		@Override
		public InputStream getFormDataForPage(int pageIndex) {
			InputStream formData = null;
			Logger.d("getFormDataForPage : Something should be happening here");
			/* 
			// Return the form data for pageIndex page
			Logger.d("getFormDataForPage: pageIndex=" + pageIndex);
			

			switch (formType) {
			case String:
				Logger.d("Stinrg: pageIndex=" + pageIndex);
				if (formDataStringInputStream != null) {
					try {
						formDataStringInputStream.close();
					} catch (IOException e) {
						Logger.e(e.toString(), e);
					}
					formDataStringInputStream = null;
				}
				try {
					AssetManager as = getResources().getAssets();
					formDataStringInputStream = as.open(FORM_DATA_STRING);
					formData = formDataStringInputStream;
					Logger.d("getFormDataForPage: " + FORM_DATA_STRING + "=" + formDataStringInputStream.available());
				} catch (IOException e) {
					Logger.e(e.toString(), e);
				}
				break;
			case QRCode:
				Logger.d("QRCode: pageIndex=" + pageIndex);
				if (formDataQRCodeInputStream != null) {
					try {
						formDataQRCodeInputStream.close();
					} catch (IOException e) {
						Logger.e(e.toString(), e);
					}
					formDataQRCodeInputStream = null;
				}
				try {
					AssetManager as = getResources().getAssets();
					formDataQRCodeInputStream = as.open(FORM_DATA_QRCODE);
					formData = formDataQRCodeInputStream;
					Logger.d("getFormDataForPage: " + FORM_DATA_QRCODE + "=" + formDataStringInputStream.available());
				} catch (IOException e) {
					Logger.e(e.toString(), e);
				}
				break;
			}
			*/
			return formData;
		}

		@Override
		public Bitmap getBitmapContentData(String contentName, int pageIndex) {
			// Return the data for the contentName of the pageIndex page
			Logger.d("getBitmapContentData: contentName=" + contentName
							+ ", pageIndex=" + pageIndex);

			return null;
		}

		@Override
		public String getStringContentData(String contentName, int pageIndex) {
			// Return the data for the contentName of the pageIndex page
			Logger.d("getStringContentData: contentName=" + contentName
							+ ", pageIndex=" + pageIndex);

			if ("String".equals(contentName)) {
				return stringData;
			} else if ("QRCode".equals(contentName)) {
				return qrCodeData;
			}

			return null;
		}

	}

	
	class PrintCallback implements LWPrintCallback {

		@Override
		public void onChangePrintOperationPhase(LWPrint lWPrint, int phase) {
			// Report the change of a printing phase
			Logger.d("onChangePrintOperationPhase: phase=" + phase);
			String jobPhase = "";
			switch (phase) {
			case LWPrintPrintingPhase.Prepare:
				jobPhase = "PrintingPhasePrepare";
				break;
			case LWPrintPrintingPhase.Processing:
				jobPhase = "PrintingPhaseProcessing";
				break;
			case LWPrintPrintingPhase.WaitingForPrint:
				jobPhase = "PrintingPhaseWaitingForPrint"; //Q
				break;
			case LWPrintPrintingPhase.Complete:
				jobPhase = "PrintingPhaseComplete";
				//printComplete(LWPrintConnectionStatus.NoError, LWPrintStatusError.NoError, false);
				//setProcessing(false);
				break;
			default:
				//setProcessing(false);
				break;
			}
			Logger.d("phase=" + jobPhase);
		}

		@Override
		public void onChangeTapeFeedOperationPhase(LWPrint lWPrint, int phase) {
			// Called when tape feed and tape cutting state transitions
			Logger.d("onChangeTapeFeedOperationPhase: phase=" + phase);
		}

		@Override
		public void onAbortPrintOperation(LWPrint lWPrint, int errorStatus,
				int deviceStatus) {
			// It is called when undergoing a transition to the printing cancel operation due to a printing error
			Logger.d("onAbortPrintOperation: errorStatus=" + errorStatus
							+ ", deviceStatus=" + deviceStatus);

			//printComplete(errorStatus, deviceStatus, false);

			
			//setProcessing(false);

			String message = "Error Status : " + errorStatus
					+ "\nDevice Status : " + Integer.toHexString(deviceStatus);
			//alertAbortOperation("Print Error!", message);
		}

		@Override
		public void onSuspendPrintOperation(LWPrint lWPrint, int errorStatus,
				int deviceStatus) {
			// It is called when undergoing a transition to the printing restart operation due to a printing error
			Logger.d("onSuspendPrintOperation: errorStatus=" + errorStatus
							+ ", deviceStatus=" + deviceStatus);

			//printComplete(errorStatus, deviceStatus, true);

			String message = "Error Status : " + errorStatus
					+ "\nDevice Status : " + Integer.toHexString(deviceStatus);
			//alertSuspendPrintOperation("Print Error! re-print ?", message);
		}

		@Override
		public void onAbortTapeFeedOperation(LWPrint lWPrint, int errorStatus,
				int deviceStatus) {
			// Called when tape feed and tape cutting stops due to an error
			Logger.d("errorStatus=" + errorStatus + ", deviceStatus=" + deviceStatus);
		}

	}

}