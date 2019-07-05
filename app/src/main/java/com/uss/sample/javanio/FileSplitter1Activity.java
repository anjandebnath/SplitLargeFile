package com.uss.sample.javanio;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class FileSplitter1Activity extends AppCompatActivity {

    private static final String dir = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator +"my_data/";
    private static final String suffix = ".splitPart";


    //this approach is that we end up loading the entire file into the JVM.
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


                // read the file from sd card
                /*String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        File.separator + "DCIM/Camera/VID_201.3gp"; // 8,463,665 Bytes*/


                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        File.separator + "DCIM/Camera/JPEG_240.jpg"; // 4,773,342 Bytes
                try {
                    List path = split(filePath, 1);
                    for (int i= 0; i<path.size(); i++){
                        System.out.println("Splitted into:"+ path.get(i));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();
    }

    /**
     *
     * @param fileName name of file to be split.
     * @param mBperSplit number of MB per file.
     * @return Return a list of files.
     * @throws IOException
     */
    public List split(String fileName, int mBperSplit) throws IOException {

        if (mBperSplit <= 0) {
            throw new IllegalArgumentException("mBperSplit must be more than zero");
        }

        List partFiles = new ArrayList();
        final long sourceSize = new File(fileName).length();
        int bytesPerSplit = 1024 * 1024 * mBperSplit;
        long numSplits = sourceSize / bytesPerSplit;
        int remainingBytes = (int) sourceSize % bytesPerSplit;

        /// Copy arrays
        byte[] originalBytes = convertFileToBytes(fileName);
        int partNum = 0;
        while (partNum < numSplits) {
            //write bytes to a part file.
            copyBytesToPartFile(originalBytes, partFiles, partNum, bytesPerSplit, bytesPerSplit);
            ++partNum;
        }

        if (remainingBytes > 0) {
            copyBytesToPartFile(originalBytes, partFiles, partNum, bytesPerSplit, remainingBytes);
        }

        return partFiles;
    }


    private static byte[] convertFileToBytes(String location) throws IOException {
        RandomAccessFile f = new RandomAccessFile(location, "r");
        byte[] b = new byte[(int) f.length()];
        f.readFully(b);
        f.close();
        return b;
    }

    private static void writeBufferToFiles(byte[] buffer, String fileName) throws IOException {
        BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(fileName));
        bw.write(buffer);
        bw.close();
    }

    private void copyBytesToPartFile(byte[] originalBytes, List partFiles, int partNum, int bytesPerSplit, int bufferSize) throws IOException {

        File mDirectory = new File(dir);
        String partFileName = dir + "part" + partNum + suffix;

        if (! mDirectory.exists()){
            mDirectory.mkdir();
            Toast.makeText(FileSplitter1Activity.this, "Directory created" , Toast.LENGTH_LONG).show();
        }

        byte[] b = new byte[bufferSize];
        System.arraycopy(originalBytes, (partNum * bytesPerSplit), b, 0, bufferSize);
        writeBufferToFiles(b, partFileName);
        partFiles.add(partFileName);
    }



}
