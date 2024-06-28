package com.mba3.cordofileopen;


import androidx.core.content.FileProvider;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaResourceApi;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.util.List;
/**
 * This class starts an activity for an intent to view files
 */
public class LogicLinkPlugin extends CordovaPlugin {


    
	/**
	 * Executes the request and returns a boolean.
	 *
	 * @param action
	 *            The action to execute.
	 * @param args
	 *            JSONArry of arguments for the plugin.
	 * @param callbackContext
	 *            The callback context used when calling back into JavaScript.
	 * @return boolean.
	 */

   public static final String OPEN_ACTION = "open";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(OPEN_ACTION)) {
            String path = args.getString(0);
            this.chooseIntent(path, callbackContext);
            return true;
        }else if (action.equals("open2")) {
			String fileUrl = args.getString(0);
			String contentType = args.getString(1);
			Boolean openWithDefault = true;
			if(args.length() > 2){
				openWithDefault = args.getBoolean(2);
			}
			this._open(fileUrl, contentType, openWithDefault, callbackContext);
		}else {
			JSONObject errorObj = new JSONObject();
			errorObj.put("status", PluginResult.Status.INVALID_ACTION.ordinal());
			errorObj.put("message", "Invalid action");
			callbackContext.error(errorObj);
		}
        return false;
    }


    /**
     * Creates an intent for the data of mime type
     *
     * @param path
     * @param callbackContext
     */
    private void chooseIntent(String path, CallbackContext callbackContext) {
        if (path != null && path.length() > 0) {
            try {
                Uri uri = Uri.parse(path);
                String mime = _getMimeType(path);
                Intent fileIntent = new Intent(Intent.ACTION_VIEW);

                // Check SDK version for handling URI
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Context context = cordova.getActivity().getApplicationContext();
                    File file = new File(uri.getPath());
                    Uri fileUri = FileProvider.getUriForFile(context, cordova.getActivity().getPackageName()  + ".provider", file);
                    fileIntent.setDataAndTypeAndNormalize(fileUri, mime);
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    fileIntent.setDataAndType(uri, mime);
                }

                fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                cordova.getActivity().startActivity(fileIntent);

                callbackContext.success();
            } catch (android.content.ActivityNotFoundException e) {
                e.printStackTrace();
                callbackContext.error(1);
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error(2);
            }
        } else {
            callbackContext.error(3);
        }
    }
	private void _open(String fileArg, String contentType, Boolean openWithDefault, CallbackContext callbackContext) throws JSONException {
		String fileName = "";
		try {
			CordovaResourceApi resourceApi = webView.getResourceApi();
			Uri fileUri = resourceApi.remapUri(Uri.parse(fileArg));
			fileName = fileUri.getPath();
		} catch (Exception e) {
			fileName = fileArg;
		}
		File file = new File(fileName);
		if (file.exists()) {
			try {
				if (contentType == null || contentType.trim().equals("")) {
				    contentType = _getMimeType(fileName);
				}

				Intent intent;
				if (contentType.equals("application/vnd.android.package-archive")) {
					// https://stackoverflow.com/questions/9637629/can-we-install-an-apk-from-a-contentprovider/9672282#9672282
					intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
					Uri path;
					if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
						path = Uri.fromFile(file);
					} else {
						Context context = cordova.getActivity().getApplicationContext();
						path = FileProvider.getUriForFile(context, cordova.getActivity().getPackageName() + ".provider", file);
					}
					intent.setDataAndType(path, contentType);
					intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

				} else {
					intent = new Intent(Intent.ACTION_VIEW);
					Context context = cordova.getActivity().getApplicationContext();
					Uri path = FileProvider.getUriForFile(context, cordova.getActivity().getPackageName() + ".provider", file);
					intent.setDataAndType(path, contentType);
					intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

				}

				/*
				 * @see
				 * http://stackoverflow.com/questions/14321376/open-an-activity-from-a-cordovaplugin
				 */
				 if(openWithDefault){
					 cordova.getActivity().startActivity(intent);
				 }
				 else{
					 cordova.getActivity().startActivity(Intent.createChooser(intent, "Open File in..."));
				 }

				callbackContext.success();
			} catch (android.content.ActivityNotFoundException e) {
				JSONObject errorObj = new JSONObject();
				errorObj.put("status", PluginResult.Status.ERROR.ordinal());
				errorObj.put("message", "Activity not found: " + e.getMessage());
				callbackContext.error(errorObj);
			}
		} else {
			JSONObject errorObj = new JSONObject();
			errorObj.put("status", PluginResult.Status.ERROR.ordinal());
			errorObj.put("message", "File not found");
			callbackContext.error(errorObj);
		}
	}


    	private String _getMimeType(String url) {
	    String mimeType = "*/*";
	    int extensionIndex = url.lastIndexOf('.');
	    if (extensionIndex > 0) {
		String extMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(url.substring(extensionIndex+1));
		if (extMimeType != null) {
		    mimeType = extMimeType;
		}
	    }
	    return mimeType;
	}
}