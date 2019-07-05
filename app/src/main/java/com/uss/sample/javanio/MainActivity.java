package com.uss.sample.javanio;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String DIRECTORY_TO_WRITE = Environment.getExternalStorageDirectory().getAbsolutePath() +
            File.separator +"javanio/";

    /** the maximum size of each file "chunk" generated, in bytes */
    public static long CHUNK_SIZE = (long)(1.4 * 1024 * 1024);  // 1.4 MB size of chunk

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ).withListener(new MultiplePermissionsListener() {
            @Override public void onPermissionsChecked(MultiplePermissionsReport report) {


                 // read the file from sd card
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        File.separator + "DCIM/Camera/VID_29.3gp";

                splitNew(filePath);



            }
            @Override public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                token.continuePermissionRequest();
            }
        }).check();



    }

    public void splitNew(String fileName){

        // open the file and read the bytes
        try(BufferedInputStream inputStream =
                    new BufferedInputStream(new FileInputStream(fileName))){

            // get the file length
            File file = new File(fileName);
            long fileSize = file.length();

            long numOfSplit = fileSize / CHUNK_SIZE;
            long remainingBytes = fileSize % CHUNK_SIZE;


            // loop for each full chunk
            int subFile = 0;  // subfile will be increased and after the end of for loop we can use the latest value
            for(; subFile < numOfSplit; subFile++){

                File mDirectory = new File(DIRECTORY_TO_WRITE);
                if (! mDirectory.exists()){
                    mDirectory.mkdir();
                }

                String partFileName = DIRECTORY_TO_WRITE + UUID.randomUUID();

                // open the output file with try-with-resource so we don't need to bother to close the file
                // creates a file output stream to write to a File object.

                try(BufferedOutputStream outputStream =
                            new BufferedOutputStream(new FileOutputStream(partFileName))){

                    // write the right amount of bytes
                    for (int currentByte = 0; currentByte < CHUNK_SIZE; currentByte++)
                    {
                        // load one byte from the input file and write it to the output file
                        outputStream.write(inputStream.read());
                    }
                }
            }

            // loop for the last chunk (which may be smaller than the chunk size)
            if (remainingBytes > 0)
            {
                String partFileName = DIRECTORY_TO_WRITE + UUID.randomUUID();
                // open the output file with try-with-resource so we don't need to bother to close the file
                try(BufferedOutputStream outputStream =
                            new BufferedOutputStream(new FileOutputStream(partFileName))){

                    // write the rest of the file
                    int b;
                    while ((b = inputStream.read()) != -1)
                        outputStream.write(b);
                }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /*public void joinFiles(File[] files) throws Exception {
        int maxReadBufferSize = 8 * 1024;

        BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream(INPUT_FILE + "_Splits\\fullJoin"
                + FILE_SUFFIX));

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
    }*/



    public void join(String baseFilename) throws IOException
    {
        int numberParts = getNumberParts(baseFilename);

        // now, assume that the files are correctly numbered in order (that some joker didn't delete any part)
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(baseFilename));
        for (int part = 0; part < numberParts; part++)
        {
            BufferedInputStream in = new BufferedInputStream(new FileInputStream(baseFilename + "." + part));

            int b;
            while ( (b = in.read()) != -1 )
                out.write(b);

            in.close();
        }
        out.close();
    }


    private int getNumberParts(String baseFilename) throws IOException
    {
        // list all files in the same directory
        File directory = new File(baseFilename).getAbsoluteFile().getParentFile();
        final String justFilename = new File(baseFilename).getName();
        String[] matchingFiles = directory.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.startsWith(justFilename) && name.substring(justFilename.length()).matches("^\\.\\d+$");
            }
        });
        return matchingFiles.length;
    }

}
