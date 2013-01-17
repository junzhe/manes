package org.whispercomm.manes.exp.cellmeasurer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Helper class for logging traces into a file.
 *
 * @author Yue Liu
 */
public class FileLogger {

    public static final String DIR = "/sdcard/";
    public String name;
    private final BufferedWriter writer;

    public FileLogger(String name) throws IOException  {
        this.name = name;
        File file = new File(DIR + name);
        // always append to historical record
        this.writer = new BufferedWriter(new FileWriter(file.getAbsoluteFile(),
                true));
    }
    
    public void append(String content) throws IOException{
        writer.append(content);
        writer.newLine();
        writer.flush();
    }
    
    public void close() throws IOException{
        writer.flush();
        writer.close();
    }
    
}
