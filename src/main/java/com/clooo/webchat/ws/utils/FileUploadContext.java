package com.clooo.webchat.ws.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileUploadContext {
    private final String fileName;
    private final FileOutputStream fos;

    public FileUploadContext(String fileName) throws IOException {
        this.fileName = generateUniqueFileName(fileName);
        this.fos = new FileOutputStream(this.fileName);
    }

    public FileOutputStream getFos() {
        return fos;
    }

    public void close() throws IOException {
        if (fos != null) {
            fos.close();
        }
    }

    private String generateUniqueFileName(String originalFileName) {
        String fileName = originalFileName;
        File file = new File(fileName);
        int count = 1;

        while (file.exists()) {
            int dotIndex = originalFileName.lastIndexOf('.');
            if (dotIndex == -1) {
                fileName = originalFileName + "(" + count + ")";
            } else {
                String name = originalFileName.substring(0, dotIndex);
                String extension = originalFileName.substring(dotIndex);
                fileName = name + "(" + count + ")" + extension;
            }
            file = new File(fileName);
            count++;
        }

        return fileName;
    }
}
