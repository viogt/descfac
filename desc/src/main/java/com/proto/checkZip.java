package com.proto;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.*;

public class checkZip {

    public static String[][] report;
    public static String Rprt;
    public static boolean hasProtection;
    public static int perSheet = 5;

    private static void printReport() {
        if (!hasProtection) {
            Rprt = "<font color='#9e9'>This workbook is not protected.</font>";
            return;
        }
        Rprt = "<br><font color='#9e9'>UNLOCKED FILE:</font><table>";
        for (int i = 0; i < report.length; i++) {
            Rprt += String.format("<tr><td>%d</td><td>%s</td><td>%s</td><td>%s</td></tr>",
                    i + 1, report[i][0], report[i][1], report[i][2]);
        }
        Rprt += "</table><br>▾<a style='color:#9e9' href='/download'>DOWNLOAD UNLOCKED</a>";
    }

    public static void replaceFileInArchive(InputStream inputZipPath) throws IOException {

        String sheetRegex = "^xl/worksheets/sheet\\d+\\.xml$";
        hasProtection = false;
        Map<String, byte[]> entries = new HashMap<>();

        //try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(inputZipPath))) {
        try (ZipInputStream zis = new ZipInputStream(inputZipPath)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {

                String entryName = entry.getName();
                /*if (entry.isDirectory()) {
                    entries.put(entryName, null);
                    continue;
                }*/

                byte[] content = zis.readAllBytes();
                //String fullString = new String(content, StandardCharsets.UTF_8);

                if (entryName.equals("xl/workbook.xml")) {
                    content = checkXML.checkBook(content);
                    entries.put(entryName, content);
                } else if (entryName.matches(sheetRegex)) {
                    content = checkXML.checkSheet(entryName, content);
                    entries.put(entryName, content);
                } else {
                    entries.put(entryName, content);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        //Path tempZipPath = Files.createTempFile("modified", ".xlsx");
     
        //try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(Data.tempZipPath))) {
        if(hasProtection)
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos)) {
            for (Map.Entry<String, byte[]> entry : entries.entrySet()) {
                String entryName = entry.getKey();
                byte[] content = entry.getValue();

                ZipEntry newEntry = new ZipEntry(entryName);
                zos.putNextEntry(newEntry);
                zos.write(content);
                zos.closeEntry();
            }
            zos.close();
            App.zipBytes = baos.toByteArray();
        } catch (Exception e) { e.printStackTrace(); }

        // --- REPLACE ORIGINAL ARCHIVE
        //Files.move(tempZipPath, Paths.get("unlocked.xlsx").toAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
        //System.out.println("Saved: " + Paths.get("unlocked.xlsx").toAbsolutePath().toString());
        SSE.broadcast("+ Process finished successfully.");
        printReport();
    }
}