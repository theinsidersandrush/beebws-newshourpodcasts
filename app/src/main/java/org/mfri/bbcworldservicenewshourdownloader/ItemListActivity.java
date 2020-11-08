package org.mfri.bbcworldservicenewshourdownloader;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

/**
 * An activity representing a list of Items. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,

 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ItemListActivity extends AppCompatActivity {


    private BBCWorldServiceDownloaderUtils utils = null;
    private TableLayout.LayoutParams rowParams = null;
    private TableRow.LayoutParams colParams = null;
    private ScrollView layMain;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("CREATE", "onCreate start");
        utils = new BBCWorldServiceDownloaderUtils();

        Bundle listBundle = this.getIntent().getExtras().getBundle("RESULT_LIST");

        //MFRI ne
        ItemList items = new ItemList(listBundle);
        setContentView(R.layout.activity_item_list);
        rowParams = new TableLayout.LayoutParams();
        rowParams.setMargins(0, 0, 0, 1);
        colParams = new TableRow.LayoutParams();
        colParams.setMargins(0, 0, 1, 0);
        colParams.width = 0;
        colParams.height = TableRow.LayoutParams.FILL_PARENT;
        TableLayout tableLayout = new TableLayout(this);
        TableLayout.LayoutParams tabLayoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.MATCH_PARENT);
        tableLayout.setLayoutParams(tabLayoutParams);
        tableLayout.setStretchAllColumns(true);
        tableLayout.setBackgroundColor(this.getResources().getColor(
                R.color.table_background));

        for(int i=0; i<items.ITEMS.size();i++){
            //add rows
           tableLayout.addView(addRow(items.ITEMS.get(i)));
        }

        //display the table
        layMain = (ScrollView)findViewById(R.id.table);
        layMain.removeAllViews();
        layMain.addView(tableLayout);
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("messageFromDownloadService"));
        Log.d("CREATE", "onCreate end");
    }



    public TableRow addRow(DownloadListItem item) {
        TableRow tr = new TableRow(this);
        tr.setBackgroundColor(this.getResources().getColor(R.color.table_background));

        tr.setLayoutParams(rowParams);

        for (int i = 0; i < 2; i++) {
            TextView tvCol = tvCol = new Button(this);
            switch (i)
            {
                case 0:
                    tvCol.setText(item.content);
                    setSubmitButtonOnClickListener((Button)tvCol, item);
                    tvCol.setBackgroundColor(this.getResources().getColor(R.color.row_background));
                    break;
                default:
                    tvCol.setClickable(false);
                    tvCol.setText(item.dateOfPublication);
                    tvCol.setBackgroundColor(this.getResources().getColor(R.color.row_background));
                    break;
            }
            //Set background color according to the download state
            if(item.url!=null&&item.url.equals("none")&&item.content!=null&&!item.content.equals("Content"))
                tvCol.setBackgroundColor(this.getResources().getColor(R.color.aqua_marine_downloaded));
            if(item.url!=null&&!item.url.equals("none")&&item.content!=null&&!item.content.equals("Content"))
                tvCol.setBackgroundColor(this.getResources().getColor(R.color.aqua_pink_downloaded));
            tvCol.setGravity(Gravity.CENTER | Gravity.CENTER);
            tvCol.setPadding(3, 3, 3, 3);
            tvCol.setTextColor(this.getResources().getColor(R.color.text_black));
            tvCol.setLayoutParams(colParams);

            tr.addView(tvCol);
        }

        return tr;
    }

    public void setSubmitButtonOnClickListener(Button button, final DownloadListItem item) {

        Log.d("ItemListActivity", "setSubmitButtonOnClickListener()start => URL: "+item.url);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("DOWNLOAD_ITEM", "onClick start");
                Intent theDownloadIntent = utils.prepareItemDownload(item,getApplicationContext(),true, false);
                utils.showNotification("BBC podcast download", "Downloading or retrieving: "+theDownloadIntent.getExtras().get("fileName"), false, null, getApplicationContext(), (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
                startService(theDownloadIntent);
            }
        });
    }


    private BroadcastReceiver bReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String fileName = intent.getExtras().getString("fileName");
            if(fileName!= null && !fileName.equals("")) {
                if(intent.getExtras().getBoolean("isStartedInBackground")!=true) {
                    Intent viewIntent = new Intent(Intent.ACTION_VIEW);
                    String root = Environment.getExternalStorageDirectory().toString();
                    File file = new File(root+"/"+BBCWorldServiceDownloaderStaticValues.BBC_PODCAST_DIR+"/"+fileName);
                    Uri fileURI = BBCWorldServicePodcastDownloaderFileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".provider", file);
                    viewIntent.setDataAndType(fileURI, "audio/*");
                    viewIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(viewIntent, fileName));
                    return;
                }
                String fileNameWithoutDir = intent.getExtras().getString("fileNameWithoutDir");
                utils.showNotification("BBC podcast download", "Podcast downloaded or retrieved: "+fileName, true, fileNameWithoutDir, context, (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
            }
        }
    };

    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(bReceiver, new IntentFilter("messageFromDownloadService"));
    }

    protected void onPause() {
        super.onPause();
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(bReceiver);
    }


}