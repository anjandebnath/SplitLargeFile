package com.uss.sample.javanio;

import android.Manifest;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;


/*
* Instead of using InputStreams and OutputStream,
* we will use channels. Imagine a channel is like a file pointer.
* We set the pointer to the beginning of where we want to read or write, in this case, we start to read from the original file.
* */
public class FileSplitter3Activity extends AppCompatActivity {

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


                // read the file from sd card
                /*String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        File.separator + "DCIM/Camera/VID_201.3gp"; // 8,463,665 Bytes*/


                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        File.separator + "DCIM/Camera/20190703_185746.mp4"; // 25,301,859 Bytes
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
     * Split a file into multiples files.
     *
     * @param fileName   Name of file to be split.
     * @param mBperSplit maximum number of MB per file.
     * @throws IOException
     */
    public static List<Path> split(final String fileName, final int mBperSplit) throws IOException {

        if (mBperSplit <= 0) {
            throw new IllegalArgumentException("mBperSplit must be more than zero");
        }

        List<Path> partFiles = new ArrayList<>();
        final long sourceSize = Files.size(Paths.get(fileName));
        final long bytesPerSplit = 1024L * 1024L * mBperSplit;
        final long numSplits = sourceSize / bytesPerSplit;
        final long remainingBytes = sourceSize % bytesPerSplit;
        int position = 0;

        // try-with -resource so we don't need to bother about closing the file
        try (RandomAccessFile sourceFile = new RandomAccessFile(fileName, "r");
             FileChannel sourceChannel = sourceFile.getChannel()) {

            for (; position < numSplits; position++) {
                //write multipart files.
                writePartToFile(bytesPerSplit, position * bytesPerSplit, sourceChannel, partFiles);
            }

            if (remainingBytes > 0) {
                writePartToFile(remainingBytes, position * bytesPerSplit, sourceChannel, partFiles);
            }
        }
        return partFiles;
    }

    private static void writePartToFile(long byteSize, long position, FileChannel sourceChannel, List<Path> partFiles) throws IOException {
        File mDirectory = new File(dir);
        if (! mDirectory.exists()){
            mDirectory.mkdir();
        }
        Path fileName = Paths.get(dir + UUID.randomUUID() + suffix);
        try (RandomAccessFile toFile = new RandomAccessFile(fileName.toFile(), "rw");
             FileChannel toChannel = toFile.getChannel()) {
            sourceChannel.position(position);
            toChannel.transferFrom(sourceChannel, 0, byteSize);
        }
        partFiles.add(fileName);
    }

    public static Stream<String> convertFileToStream(String location) throws IOException {
        return Files.lines(Paths.get(location));
    }

    public static void convertStreamToFile(Stream<String> data, Path path) throws IOException {
        Files.write(path, (Iterable<String>) data::iterator);
    }

}
