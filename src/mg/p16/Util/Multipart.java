package mg.p16.Util;

import java.io.InputStream;

public class Multipart {
    String fileName;
    long fileSize;
    InputStream fileContent;
    
    public Multipart() {
    }

    public Multipart(String fileName, long fileSize, InputStream fileContent) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.fileContent = fileContent;
    }



    public String getFileName() {
        return fileName;
    }



    public void setFileName(String fileName) {
        this.fileName = fileName;
    }



    public long getFileSize() {
        return fileSize;
    }



    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }



    public InputStream getFileContent() {
        return fileContent;
    }



    public void setFileContent(InputStream fileContent) {
        this.fileContent = fileContent;
    }
}
