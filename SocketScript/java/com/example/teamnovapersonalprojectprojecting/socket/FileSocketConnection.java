package com.example.teamnovapersonalprojectprojecting.socket;


import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.example.teamnovapersonalprojectprojecting.local.database.main.DB_FileList;
import com.example.teamnovapersonalprojectprojecting.local.database.main.LocalDBMain;
import com.example.teamnovapersonalprojectprojecting.util.DataManager;
import com.example.teamnovapersonalprojectprojecting.util.JsonUtil;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileSocketConnection {
    public static final int PORT = 5001;
    public static final int FILE_BUFFER_SIZE = 4096;

    private static FileSocketConnection instance = null;
    public static FileSocketConnection Instance(){
        if(instance == null){
            instance = new FileSocketConnection();
        }
        return instance;
    }
    public static void Reset(){
        instance = null;
        Instance();
    }

    public static void LOG(String title, int logText){
        Log.d(FileSocketConnection.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String title, String logText){
        Log.d(FileSocketConnection.class.getSimpleName(), title +": " + logText);
    }
    public static void LOG(String logText){
        Log.d(FileSocketConnection.class.getSimpleName(), logText);
    }
    public static void LOG(int logText){
        Log.d(FileSocketConnection.class.getSimpleName(), ""+logText);
    }
    public static void LOGe(String logText){
        Log.e(FileSocketConnection.class.getSimpleName(), logText);
    }
    public Thread networkThread;

    public BlockingQueue<Uri> taskQueue ;
    public List<SocketEventListener.eType> waitingListenList;

    private Socket clientSocket;
    private DataInputStream in;
    private DataOutputStream out;

    private FileSocketConnection(){
        taskQueue = new LinkedBlockingQueue<>();
        waitingListenList = new ArrayList<>();

        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket(SocketConnection.SERVER_ADDRESS, PORT);
                    in = new DataInputStream(clientSocket.getInputStream());
                    out = new DataOutputStream(clientSocket.getOutputStream());

                    out.writeInt(DataManager.Instance().userId);

                    setInputStream();
                    setOutputStream();
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    LOGe(e.getMessage());
                    /**
                     * 오프라인 기능 대처하는 만들려면 이쪽에서 구현하면됨
                     */
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    e.printStackTrace();
                    LOGe(e.getMessage());
                }
            }
        });
        networkThread.start();
    }
    private void close(){
        try {
            if(clientSocket != null) {
                clientSocket.close();
            }
            instance = null;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setInputStream(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        int id = in.readInt();
                        int fileNameLength = in.readInt();

                        byte[] fileNameBytes = new byte[fileNameLength];
                        in.readFully(fileNameBytes);
                        String fileName = new String(fileNameBytes, StandardCharsets.UTF_8);

                        long fileSize = in.readLong();
                        String internalPath = storeFileInternally(fileName, fileSize);

                        if(internalPath != null){
                            LocalDBMain.GetTable(DB_FileList.class).addFileList(id, fileName, (int) fileSize, internalPath);
                            SocketEventListener.callEvent(SocketEventListener.eType.FILE_INPUT_STREAM, new JsonUtil()
                                    .add(JsonUtil.Key.ID, id)
                                    .add(JsonUtil.Key.NAME, fileName)
                                    .add(JsonUtil.Key.DATA, internalPath));
                            LocalDBMain.LOG("File stored and recoded", internalPath);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void setOutputStream(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (true) {
                        Uri uri = taskQueue.take();
                        Context currentContext = DataManager.Instance().currentContext;
                        try (InputStream fileInputStream = currentContext.getContentResolver().openInputStream(uri)) {

                            byte[] fileNameBytes = getFileName(currentContext, uri).getBytes(StandardCharsets.UTF_8);
                            out.writeInt(fileNameBytes.length);
                            out.write(fileNameBytes);

                            long fileSize = getFileSizeFromUri(currentContext, uri);
                            out.writeLong(fileSize);

                            byte[] buffer = new byte[FILE_BUFFER_SIZE];
                            int bytesRead;
                            long totalBytesRead = 0;
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                                totalBytesRead += bytesRead;
                            }
                            out.flush();
                            LOG("FINISH Sent to Server");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void startReconnect(){
        instance.close();
        LOG("start try reconnect fileSocket");
        FileSocketConnection.Instance();
    }

    public static void sendFile(Uri uri){
        Instance().taskQueue.add(uri);
    }

    public String getFileName(Context context, Uri uri){
        String result = null;
        if(uri.getScheme().equals("content")){
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if(cursor != null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }

            if(result == null){
                result = uri.getPath();
                int cut = result.lastIndexOf('/');
                if(cut != -1){
                    result = result.substring(cut + 1);
                }
            }

        }
        Log.d("ms",result);

        return result;
    }

    private long getFileSizeFromUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        int sizeIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE);
        cursor.moveToFirst();
        long size = cursor.getLong(sizeIndex);
        cursor.close();
        return size;
    }

    public Uri saveFile(Context context, String fileName, String mimeType) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = null;
        final ContentResolver resolver = context.getContentResolver();
        try {
            uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        } catch (Exception e){
            Log.e("saveFile", "Error createing file: " + e.getMessage());
        }
        return uri;
    }

    private String getMimeType(String fileName) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        if (extension != null) {
            return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return "application/octet-stream"; // 기본 MIME 타입
    }
    private String storeFileInternally(String fileName, long fileSize){
        File internalFile = new File(DataManager.Instance().currentContext.getFilesDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(internalFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos);){
            byte[] buffer = new byte[4096];
            int bytesRead;
            long totalBytesRead = 0;
            while (totalBytesRead < fileSize){
                bytesRead = in.read(buffer, 0, (int) Math.min(buffer.length, fileSize - totalBytesRead));
                if (bytesRead == -1) break;
                bos.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                if(totalBytesRead % (fileSize / 10) < buffer.length){
                    Log.d("fileLoad", "진행률: " + (totalBytesRead * 100 / fileSize) + "%");
                }
            }
            bos.flush();
            return internalFile.getAbsolutePath();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

