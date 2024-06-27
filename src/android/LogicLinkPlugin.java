package com.mba3.cordofileopen;


import androidx.core.content.FileProvider;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.net.Uri;
import android.content.Intent;
import android.webkit.MimeTypeMap;
import android.content.ActivityNotFoundException;
import android.os.Build;

import java.io.File;

/**
 * This class starts an activity for an intent to view files
 */
public class LogicLinkPlugin extends CordovaPlugin {


   public static final String OPEN_ACTION = "open";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(OPEN_ACTION)) {
            String path = args.getString(0);
            this.chooseIntent(path, callbackContext);
            return true;
        }
        return false;
    }

    /**
     * Returns the MIME type of the file.
     *
     * @param path
     * @return
     */
    private static String getMimeType(String path) {
        String mimeType = null;

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            mimeType = mime.getMimeTypeFromExtension(extension.toLowerCase());
        }

        System.out.println("Mime type: " + mimeType);

        return mimeType;
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
                String mime = getMimeType(path);
                Intent fileIntent = new Intent(Intent.ACTION_VIEW);

                // Check SDK version for handling URI
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Context context = cordova.getActivity().getApplicationContext();
                    File file = new File(uri.getPath());
                    Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                    fileIntent.setDataAndTypeAndNormalize(fileUri, mime);
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    fileIntent.setDataAndType(uri, mime);
                }

                fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                cordova.getActivity().startActivity(fileIntent);

                callbackContext.success();
            } catch (ActivityNotFoundException e) {
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
}