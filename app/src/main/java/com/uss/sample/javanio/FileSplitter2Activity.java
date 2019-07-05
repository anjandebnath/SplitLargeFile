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





                // read the file from sd card and split

                /*String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        File.separator + "DCIM/Camera/IMG_07.jpg";

                try {
                    List path = split(filePath, 1);
                    for (int i= 0; i<path.size(); i++){
                        System.out.println("Splitted into:"+ path.get(i));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }*/


                // read the files from sdcard and join
                // here after getting the split files number we need to set the value
                File[] files = new File[5];
                for (int i = 1; i <= 5; i++) {
                    files[i-1] = new File(dir + "part" + (i-1) + suffix);
                }

                try {
                    joinFiles(files);
                } catch (Exception e) {
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

    private static BufferedOutputStream newWriteBuffer(int partNum, List partFiles) throws IOException{

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

    public void join(String FilePath) {
        long leninfile = 0, leng = 0;
        int count = 1, data = 0;
        try {
            File filename = new File(FilePath);

            OutputStream outfile = new BufferedOutputStream(new FileOutputStream(filename));
            while (true) {
                filename = new File(FilePath + count + ".sp");
                if (filename.exists()) {

                    InputStream infile = new BufferedInputStream(new FileInputStream(filename));
                    data = infile.read();
                    while (data != -1) {
                        outfile.write(data);
                        data = infile.read();
                    }
                    leng++;
                    infile.close();
                    count++;
                } else {
                    break;
                }
            }
            outfile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
