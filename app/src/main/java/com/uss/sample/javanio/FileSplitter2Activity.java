package com.uss.sample.javanio;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


/**
 * I didn't load the entire file - a small part is just saved in a buffer,
 * writes it in a new file, and then moves to the next file.
 * The key is that the 'maxReadBufferSize' should be small.
 * It will take more time, but the application will use less memory.
 */
public class FileSplitter2Activity extends AppCompatActivity {

    private static final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator +"javanio/";
    private static final String suffix = ".splitPart";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_splitter1);


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {

                // Split the file into subfiles
                Single<String> pathList = getSplitFile();


                // after getting the split option ok then we will merge the file
                // this is actually the case of Making 2 requests where the second depends on the first
                Single<String> dummies = pathList.flatMap(s -> {
                       return joinSubFile(5);
                });

                dummies.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(s -> Toast.makeText(getApplicationContext(), "Merge Done!", Toast.LENGTH_LONG).show());




            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    //  RxJava 2 Observable from a Java List:
    public Single<String> getSplitFile(){

        String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "DCIM/Camera/IMG_07.jpg";
        List<String> path = null;
        try {
            path = split(filePath, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Single.just("Hello");

    }


    public Single<String> getDummyData(){
        return Single.just("Dum 1");
    }

    public Single<String> joinSubFile(int totalNumOfSplits){

        File[] files = new File[totalNumOfSplits];
        for (int i = 1; i <= totalNumOfSplits; i++) {
            files[i-1] = new File(dir + "part" + (i-1) + suffix);
        }

        try {
            joinFiles(files);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Single.just("Merging done");
    }



    public void joinFiles(File[] files) throws Exception {
        int maxReadBufferSize = 8 * 1024;

        BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(dir + "FullJoin"
                + ".jpg"));

        RandomAccessFile raf = null;

        for (File file : files) {
            raf = new RandomAccessFile(file, "r");
            long numReads = raf.length() / maxReadBufferSize;
            long numRemainingRead = raf.length() % maxReadBufferSize;
            for (int i = 0; i < numReads; i++) {
                readWrite(raf, bw, maxReadBufferSize);
            }
            if (numRemainingRead > 0) {
                readWrite(raf, bw, numRemainingRead);
            }
            raf.close();

        }
        bw.close();
    }


    public List<String> split(String fileName, int mBperSplit) throws IOException {

        if (mBperSplit <= 0) {
            throw new IllegalArgumentException("mBperSplit must be more than zero");
        }

        List<String> partFiles = new ArrayList<>();
        final long sourceSize = new File(fileName).length();
        int bytesPerSplit = 1024 * 1024 * mBperSplit;
        long numSplits = sourceSize / bytesPerSplit;
        int remainingBytes = (int) sourceSize % bytesPerSplit;

        RandomAccessFile raf = new RandomAccessFile(fileName, "r");
        int maxReadBufferSize = 8* 1024; //8 kB

        int partNum = 0;
        for (; partNum < numSplits; partNum++) {

            BufferedOutputStream bw = newWriteBuffer(partNum, partFiles);

            if (bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit / maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for (int i = 0; i < numReads; i++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if (numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            } else {
                readWrite(raf, bw, bytesPerSplit);
            }
            bw.close();
        }
        if (remainingBytes > 0) {
            BufferedOutputStream bw = newWriteBuffer(partNum, partFiles);
            readWrite(raf, bw, remainingBytes);
            bw.close();
        }
        raf.close();
        return partFiles;
    }

    private static BufferedOutputStream newWriteBuffer(int partNum, List<String> partFiles) throws IOException{

        File mDirectory = new File(dir);
        if (! mDirectory.exists()){
            mDirectory.mkdir();
        }
        String partFileName = dir + "part" + partNum + suffix;
        partFiles.add(partFileName);
        return new BufferedOutputStream(new FileOutputStream(partFileName));
    }

    private static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if (val != -1) {
            bw.write(buf);
        }
    }






}
