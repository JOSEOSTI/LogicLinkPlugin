package org.example.logiclinkplugin;

import org.apache.cordova.CordovaPlugin;

import java.io.File;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.CallbackContext;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import androidx.core.content.FileProvider;

import java.io.File;
/**
 * This class echoes a string called from JavaScript.
 */
public class LogicLinkPlugin extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        }else if(action.equals("saludarMethod")){
            String message = args.getString(0);
            this.saludarMethod(message, callbackContext);
            return true;
        }else  if (action.equals("openFile")) {
            String filePath = args.getString(0);
            openFile(filePath, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    
    private void saludarMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success("hola " +message );
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void openFile(String path, CallbackContext callbackContext) {
        if (path != null && path.length() > 0) {
            try {
                Context context = cordova.getActivity().getApplicationContext();
                File file = new File(path);
                Uri uri;
                String mime = getMimeType(file.getName());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // Utilizar FileProvider para generar la URI del archivo
                    uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
                } else {
                    // Si es una versión anterior a Android Nougat, utilizar Uri.fromFile()
                    uri = Uri.fromFile(file);
                }

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, mime);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                cordova.getActivity().startActivity(intent);
                callbackContext.success();
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                callbackContext.error("No application available to open this file.");
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error("Error opening file: " + e.getMessage());
            }
        } else {
            callbackContext.error("File path is empty or invalid.");
        }
    }

    private String getMimeType(String url) {
        String mimeType;
        if (url.lastIndexOf(".") != -1) {
            String extension = url.substring(url.lastIndexOf(".") + 1);
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        } else {
            mimeType = "application/octet-stream";
        }
        return mimeType;
    }

    
}
