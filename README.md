## important links


- https://stackoverflow.com/questions/37761543/how-i-can-split-file-in-android-case-the-file-is-large-to-upload-itsound-file
- http://www.admios.com/blog/how-to-split-a-file-using-java
https://stackoverflow.com/questions/31179273/splitting-and-merging-large-files-size-in-gb-in-java


# File Reader & File Writter

FileReader and FileWriter are **character based**, they are intended for reading and writing **text**.


# FileInputStream & FileOutputStream

**FileInputStream reads the contents of a file as a stream of bytes.**

**FileOutputStream writes the contents of a file as a stream of bytes.**

     When you write to a FileOutputStream, the data may get cached internally in memory and 
     written to disk at a later time. If you want to make sure that all data is written to disk 
     without having to close the FileOutputStream, you can call its flush() method.

# Buffered-Reader vs Buffered-InputStream

 The difference is the same as between reader and inputstream: one is **character-based**, another is **byte-based.**
 For example: reader normally supports encoding, 
 
 - **BufferedInputStream reads the data in the buffer as bytes by using InputStream.**
 - **BufferedReader reads the text but not as bytes and BufferedReader is efficient reading of characters,arrays and lines.** 