package org.mfri.bbcworldservicenewshourdownloader;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DeleteOldPodcastsService extends IntentService {

    /**
     * @deprecated
     */
    public DeleteOldPodcastsService() {
        super("DownloadService");
        try {
            Class.forName("android.os.AsyncTask");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d("HANDLE_INTENT", "DeleteOldPodcastsService: onHandleIntent start");
        synchronized (intent) {
            BBCWorldServiceDownloaderUtils utils = BBCWorldServiceDownloaderUtils.getInstance();
            try {
                ArrayList<DownloadItem> list = utils.getDownloadedPodcastsList(this);
                if (list != null) {
                    for(int i=0; i<list.size();i++){
                        DownloadItem item = list.get(i);
                        Calendar calendarDateMin30 = Calendar.getInstance(); // this would default to now
                        calendarDateMin30.add(Calendar.DAY_OF_MONTH, -30);
                        if(item.compareDate.getTime() < calendarDateMin30.getTime().getTime()){
                           String message = deletePodcast(item.fileName);
                            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }



    private String deletePodcast(String fileName) throws IOException {

        String root = PreferenceManager.getDefaultSharedPreferences(this).getString("dl_dir_root", Environment.getExternalStorageDirectory().toString());

        File myDir = new File(root);
        BBCWorldServiceDownloaderUtils.checkDir(myDir, this);


        File file = new File(myDir, fileName);
        if (file.exists()) {
          if(file.delete()==true)
            return  "Deleted file "+fileName;
          else
              return  "Delete file failed: "+fileName;
        }
        return  "File not found "+fileName;

    }


}
