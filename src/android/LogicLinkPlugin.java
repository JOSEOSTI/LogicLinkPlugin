package org.example.logiclinkplugin;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.webkit.MimeTypeMap;

import androidx.core.content.FileProvider;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
public class LogicLinkPlugin extends CordovaPlugin {

    public static final String OPEN_ACTION = "open";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(OPEN_ACTION)) {
            String path = args.getString(0);
            this.openFile(path, callbackContext);
            return true;
        }
        return false;
    }

    private void openFile(String path, CallbackContext callbackContext) {
        if (path != null && path.length() > 0) {
            try {
                Uri uri = Uri.parse(path);
                String mime = getMimeType(path);
                Intent fileIntent = new Intent(Intent.ACTION_VIEW);

                Context context = cordova.getActivity().getApplicationContext();
                File file = new File(uri.getPath());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    // Use FileProvider to get content URI for file
                    Uri fileUri = FileProvider.getUriForFile(context,
                            context.getApplicationContext().getPackageName() + ".provider",
                            file);
                    fileIntent.setDataAndType(fileUri, mime);
                    fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    fileIntent.setDataAndType(uri, mime);
                }

                fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (fileIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(fileIntent);
                    callbackContext.success();
                } else {
                    throw new ActivityNotFoundException();
                }

            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                callbackContext.error("Activity not found to handle this file type.");
            } catch (Exception e) {
                e.printStackTrace();
                callbackContext.error("Error opening file: " + e.getMessage());
            }
        } else {
            callbackContext.error("File path is empty or null.");
        }
    }

    private String getMimeType(String path) {
        String mimeType = null;

        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        if (extension != null) {
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        }

        return mimeType;
    }
}
