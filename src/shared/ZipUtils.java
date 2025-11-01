package shared;

import java.io.*;
import java.util.zip.*;

public class ZipUtils {

    /**
     * Recursively zips a file or folder into a ZipOutputStream.
     */
    public static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) return;

        if (fileToZip.isDirectory()) {
            if (!fileName.endsWith("/")) {
                fileName += "/";
            }
            zipOut.putNextEntry(new ZipEntry(fileName));
            zipOut.closeEntry();

            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File child : children) {
                    zipFile(child, fileName + child.getName(), zipOut);
                }
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zipOut.write(buffer, 0, length);
            }
        }
    }

    /**
     * Extracts the contents of a ZipInputStream into a destination folder.
     */
    public static void unZipFile(InputStream zipIn, File destinationDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(zipIn)) {
            ZipEntry entry;
            byte[] buffer = new byte[1024];

            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destinationDir, entry.getName());

                if (entry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    /**
     * Compresses an entire folder (or file) into a byte array.
     * Useful for sending zipped data over a socket or storing in memory.
     */
    public static byte[] compressFolderToBytes(File folder) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(baos)) {

            zipFile(folder, folder.getName(), zipOut);
            zipOut.finish();
            return baos.toByteArray();
        }
    }

    /**
     * Extracts a zip represented by a byte array into a destination folder.
     * 
     * @param zipBytes Byte array containing zip data
     * @param destinationDir Directory to extract into
     */
    public static void extractFromBytes(byte[] zipBytes, File destinationDir) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(zipBytes)) {
            unZipFile(bais, destinationDir);
        }
    }
}
