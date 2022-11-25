package com.febete.imagedownloader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    EditText txtUrl;
    ImageView imgView;
    Button btnDownload;

    //Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgView = findViewById(R.id.imgView);
        txtUrl = findViewById(R.id.txtURL);
        btnDownload = findViewById(R.id.btnDownload);
        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int permission = ActivityCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permission != PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
                }
               /* String fileName = "temp.jpg";
                String imagePath = (Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS)).toString()
                        + "/" + fileName;
*/
              /*  downloadFile(txtUrl.getText().toString(),imagePath);
                preview(imagePath);
*/
                else{
                 DownloadTask backgroundTask= new DownloadTask();
                 String[] urls = new String[1];
                 urls[0] = txtUrl.getText().toString();
                 backgroundTask.execute(urls);
                }


            }
        });
    }

    public void downloadFile(String strUrl, String imagePath){
        try {
            URL url =new URL(strUrl);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();
            Log.d("DownloadFile","reading and writing");

            InputStream inputStream=new BufferedInputStream(url.openStream(),8192);
            OutputStream outputStream= new FileOutputStream((imagePath));
            byte data[] = new byte[1024];

            int count;
            while ((count = inputStream.read(data))!=-1){
                outputStream.write(data,0,count);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void preview(String imagePath){
        Bitmap bitmapImage= BitmapFactory.decodeFile(imagePath);
        float w= bitmapImage.getWidth();
        float h= bitmapImage.getHeight();
        int W = 400;
        int H = (int) ( (h*W)/w);
        Bitmap bitmap = Bitmap.createScaledBitmap(bitmapImage,W,H,false);
        imgView.setImageBitmap((bitmap));
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            if (grantResults.length == 2 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                /*String fileName = "temp.jpg";
                String imagePath = (Environment.getExternalStoragePublicDirectory
                        (Environment.DIRECTORY_DOWNLOADS)).toString()
                        + "/" + fileName;
                downloadFile(txtUrl.getText().toString(), imagePath);
                preview(imagePath);*/
                DownloadTask task = new DownloadTask();
                String[] urls = new String[1];
                urls[0] = txtUrl.getText().toString();
                task.execute(urls);
            } else {
                Toast.makeText(this, "External Storage permission not granted",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    class DownloadTask extends AsyncTask<String, Integer,Bitmap>{
        @Override
        protected Bitmap doInBackground(String... strings) {

            String fileName = "temp.jpg";
            String imagePath = (Environment.getExternalStoragePublicDirectory
                    (Environment.DIRECTORY_DOWNLOADS)).toString();


            downloadFile(strings[0],imagePath+"/"+fileName);
            return scaleBitmap(imagePath+"/"+fileName);

        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            imgView.setImageBitmap(bitmap);
        }

    }

    public Bitmap scaleBitmap(String imagePath){
        Bitmap image= BitmapFactory.decodeFile(imagePath);
        float w= image.getWidth();
        float h= image.getHeight();
        int W = 400;
        int H = (int) ( (h*W)/w);
        Bitmap b = Bitmap.createScaledBitmap(image,W,H,false);
        return b;
    }

    class DownloadRunnable implements Runnable{

        String url;

        public DownloadRunnable(String url) {
            this.url = url;
        }


        @Override
        public void run() {
            String filename =  "temp.jpg";
            String imagePath = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)).toString();
            downloadFile(url,imagePath+"/"+filename);
            Bitmap bitmap = scaleBitmap(imagePath+"/"+filename);
            runOnUiThread(new UpdateBitmap(bitmap));
        }

        class UpdateBitmap implements Runnable{
            Bitmap bitmap;
            public UpdateBitmap(Bitmap bitmap){
                this.bitmap=bitmap;
            }
            @Override
            public void run() {
                imgView.setImageBitmap(bitmap);
            }
        }
    }
}