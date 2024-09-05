package org.example;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


/**
 * 서버 부분에서 소켓 으로 오는 데이터와 가는 데이터의 중간부분을 처리하는 역할
 */
public class FileSocketConnection {
    public static final String filePath = "/var/www/html/file/";
    public static class ChannelBuffer{
        public static final int LENGTH_FILE_NAME_SIZE = 4;
        public static final int LENGTH_FIELD_SIZE = 8;
        public static final int MESSAGE_BUFFER_SIZE = 4096;
        public static final int NOT_SETUP = -1;
        public ByteBuffer fileNameSizeBuffer = ByteBuffer.allocate(LENGTH_FILE_NAME_SIZE);
        public ByteBuffer fileNameBuffer = null;
        public ByteBuffer lengthBuffer = ByteBuffer.allocate(LENGTH_FIELD_SIZE);
        public ByteBuffer messageBuffer = ByteBuffer.allocate(MESSAGE_BUFFER_SIZE);
        public int fileNameLength = NOT_SETUP;
        public long expectedLength = NOT_SETUP;
        public String fileName = Util.NOT_SETUP_S;
        public boolean isReadName = false;
    }

    public static class UserData {
        public static final int NOT_SETUP = -1;
        public int userId = NOT_SETUP;
        public ByteBuffer userIdBuffer = ByteBuffer.allocate(4);
    }
    public static final int PORT = 5001;

    private static FileSocketConnection instance = null;
    public static FileSocketConnection Instance(){
        if(instance == null){
            instance = new FileSocketConnection();
        }
        return instance;
    }

    public Map<SocketChannel, ChannelBuffer> channelBufferMap;
    public Map<SocketChannel, UserData> userMap;

    Selector selector;
    public static SocketChannel getUserSocket(int userId){
        for (Map.Entry<SocketChannel, UserData> userdata : Instance().userMap.entrySet()) {
            if(userdata.getValue().userId == userId){
                return userdata.getKey();
            }
        }
        return null;
    }
    private FileSocketConnection(){
        channelBufferMap = new HashMap<>();
        userMap = new HashMap<>();
    }
    public void Start(){
        try {
            selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("version: 0.1");
            System.out.println("Image Chat server started on port " + PORT);

            while (true) {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();

                    try {
                        if (key.isAcceptable()) {
                            handleAccept(serverSocketChannel, selector);
                        } else if (key.isReadable()) {
                            handleRead(key);
                        }
                    } catch (IOException e) {
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (IOException|CancelledKeyException ex){
                            ex.printStackTrace();
                        }
                        System.out.println("Client disconnected unexpectedly");
                        e.printStackTrace();
                        System.out.println(e.getMessage());
                    }

                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAccept(ServerSocketChannel serverSocketChannel, Selector selector) throws IOException {
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        System.out.println("New client connected-File: " + clientChannel.getRemoteAddress());
    }

    private void handleRead(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        if (userMap.containsKey(clientChannel)){
            imageProcess(clientChannel);
        } else {
            userAddProcess(clientChannel);
        }
    }

    public void userAddProcess(SocketChannel clientChannel) {
        try {
            UserData userData = new UserData();
            clientChannel.read(userData.userIdBuffer);
            if(!userData.userIdBuffer.hasRemaining()){
                userData.userIdBuffer.flip();
                userData.userId = userData.userIdBuffer.getInt();

                userMap.put(clientChannel, userData);
                System.out.println("Add User Process: " + userData.userId );
            }
        } catch (Exception e) {
            e.printStackTrace();
            cleanChannel(clientChannel);
        }
    }

    private void cleanChannel(SocketChannel clientChannel){
        try {
            SocketConnection.trySendMessage(userMap.get(clientChannel).userId, new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.RECONNECT_FILE_SOCKET.toString()));

            for (SelectionKey key : selector.keys()) {
                if (clientChannel.equals(key.channel())) {
                    key.cancel();
                    clientChannel.close();
                    System.out.println("Client connection closed: " + clientChannel.getRemoteAddress());
                    break;
                }
            }
            channelBufferMap.remove(clientChannel);
            userMap.remove(clientChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void imageProcess(SocketChannel clientChannel) throws IOException {
        ChannelBuffer buffer = channelBufferMap.computeIfAbsent(clientChannel, k -> new ChannelBuffer());
        try {
            if (!buffer.isReadName && buffer.fileNameBuffer == null) {
                readFileNameLength(clientChannel, buffer);
            }
            if(buffer.fileNameBuffer != null){
                readFileName(clientChannel, buffer);
            }
            if (buffer.isReadName && buffer.expectedLength == ChannelBuffer.NOT_SETUP) {
                readMessageLength(clientChannel, buffer);
            }
            if(buffer.isReadName && buffer.expectedLength != ChannelBuffer.NOT_SETUP){
                readMessageContent(clientChannel, buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
            SocketConnection.trySendMessage(userMap.get(clientChannel).userId, new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ERROR.toString())
                    .put(SocketEventListener.eKey.MESSAGE.toString(), e.getMessage())
                    .put(SocketEventListener.eKey.PRINT.toString(), e.getStackTrace())
            );
            clientChannel.close();
            System.out.println("Client disconnected");
        } catch (JSONException e) {
            e.printStackTrace();
            SocketConnection.trySendMessage(userMap.get(clientChannel).userId, new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ERROR.toString())
                    .put(SocketEventListener.eKey.MESSAGE.toString(), e.getMessage())
                    .put(SocketEventListener.eKey.PRINT.toString(), e.getStackTrace())
            );
            //여기부분에서 JsoneEcetpion을 처리할떄 문제가 발생하는것 같음
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            //버퍼 문제 발생 버퍼 제거해서 초기화시키기
            channelBufferMap.remove(clientChannel);
            SocketConnection.trySendMessage(userMap.get(clientChannel).userId, new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.ERROR.toString())
                    .put(SocketEventListener.eKey.MESSAGE.toString(), e.getMessage())
                    .put(SocketEventListener.eKey.PRINT.toString(), e.getStackTrace())
            );
            cleanChannel(clientChannel);
        }

    }

    public void readFileNameLength(SocketChannel clientChannel, ChannelBuffer buffer) throws IOException {
        buffer.fileNameSizeBuffer.clear();
        clientChannel.read(buffer.fileNameSizeBuffer);
        if(!buffer.fileNameSizeBuffer.hasRemaining()) {
            buffer.fileNameSizeBuffer.flip();
            buffer.fileNameLength = buffer.fileNameSizeBuffer.getInt();
            System.out.println("readFileNameLength:" + buffer.fileNameLength);
            buffer.fileNameBuffer = ByteBuffer.allocate(buffer.fileNameLength);
        }
    }
    public void readFileName(SocketChannel clientChannel, ChannelBuffer buffer) throws IOException {
        clientChannel.read(buffer.fileNameBuffer);
        if(!buffer.fileNameBuffer.hasRemaining()){
            buffer.fileNameBuffer.flip();
            byte[] fileNameBytes = new byte[buffer.fileNameLength];
            buffer.fileNameBuffer.get(fileNameBytes);
            buffer.fileName = new String(fileNameBytes, StandardCharsets.UTF_8);
            System.out.println("readFileName:" + buffer.fileName);

            buffer.fileNameBuffer = null;
            buffer.fileNameLength = -1;
            buffer.isReadName = true;
        }
    }


    public void readMessageLength(SocketChannel clientChannel, ChannelBuffer buffer) throws IOException {
        buffer.lengthBuffer.clear();
        clientChannel.read(buffer.lengthBuffer);
        if(!buffer.lengthBuffer.hasRemaining()) {
            buffer.lengthBuffer.flip();
            buffer.expectedLength = buffer.lengthBuffer.getLong();
            System.out.println("messageLength:" + buffer.expectedLength);
        }
    }

    public void readMessageContent(SocketChannel clientChannel, ChannelBuffer buffer) throws IOException {

        String uuid = UUID.randomUUID().toString();
        String fileExtension = buffer.fileName.substring(buffer.fileName.lastIndexOf('.'));
        String realFilePath = filePath + uuid + fileExtension;
        int id = MysqlManager.Instance().addFileSave(uuid, fileExtension, buffer.expectedLength, realFilePath);

        System.out.println("start creating file: " + realFilePath);

        File file = new File(realFilePath);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }

        SocketConnection.trySendMessage(userMap.get(clientChannel).userId, new JSONObject()
                .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.FILE.toString())
                .put(SocketEventListener.eKey.ID.toString(), id)
                .put(SocketEventListener.eKey.IS_VALID.toString(), false));

        try (FileOutputStream fos = new FileOutputStream(file);
             FileChannel fileChannel = fos.getChannel();){
            int bytesRead;
            long totalBytesRead = 0;
            while (totalBytesRead < buffer.expectedLength) {
                buffer.messageBuffer.clear();
                bytesRead = clientChannel.read(buffer.messageBuffer);
                if(bytesRead == -1) {
                    System.out.println("connection closed prematurely");
                    break;
                }
                buffer.messageBuffer.flip();
                while (buffer.messageBuffer.hasRemaining()){
                    fileChannel.write(buffer.messageBuffer);
                }
                totalBytesRead += bytesRead;
            }
        }

        SocketConnection.trySendMessage(userMap.get(clientChannel).userId, new JSONObject()
                .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.FILE.toString())
                .put(SocketEventListener.eKey.ID.toString(), id)
                .put(SocketEventListener.eKey.IS_VALID.toString(), true));

        System.out.println("read file finish");

        buffer.expectedLength = ChannelBuffer.NOT_SETUP;
        buffer.isReadName = false;
    }
    public static boolean trySendMessage(int userId, int fileId) {
        SocketChannel clientChannel = getUserSocket(userId);
        if(clientChannel == null) {
            SocketConnection.trySendMessage(userId, new JSONObject()
                    .put(SocketEventListener.eKey.TYPE.toString(), SocketEventListener.eType.RECONNECT_FILE_SOCKET.toString()));
            return false;
        }
        Map<String, Object> fileData = MysqlManager.Instance().getFileDataById(fileId);
        if(fileData == null){
            return false;
        }
        int id = Integer.parseInt(fileData.get("id").toString());
        String fileName = fileData.get("file_name").toString();
        String fileType = fileData.get("file_type").toString();
        String filePath = fileData.get("file_path") .toString();
        File file = new File(filePath);
        try {
            sendFileId(clientChannel, id);
            sendFileName(clientChannel, fileName + fileType);
            sendFileSize(clientChannel, file);
            sendFile(clientChannel, file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Instance().cleanChannel(clientChannel);
        }
        return false;
    }

    private static void sendFileId(SocketChannel clientChannel, int id) throws IOException {
        ByteBuffer fileSizeBuffer = ByteBuffer.allocate(4).putInt(id);
        fileSizeBuffer.flip();
        while (fileSizeBuffer.hasRemaining()){
            clientChannel.write(fileSizeBuffer);
        }
    }
    private static void sendFileName(SocketChannel clientChannel, String fileName) throws IOException {
        byte[] fileNameBytes = fileName.getBytes(StandardCharsets.UTF_8);
        ByteBuffer fileNameLengthBuffer = ByteBuffer.allocate(4).putInt(fileNameBytes.length);
        fileNameLengthBuffer.flip();
        while (fileNameLengthBuffer.hasRemaining()){
            clientChannel.write(fileNameLengthBuffer);
        }

        ByteBuffer fileNameBuffer = ByteBuffer.wrap(fileNameBytes);
        while (fileNameBuffer.hasRemaining()){
            clientChannel.write(fileNameBuffer);
        }
    }

    private static void sendFileSize(SocketChannel clientChannel, File file ) throws IOException {
        ByteBuffer fileSizeBuffer = ByteBuffer.allocate(8).putLong(file.length());
        fileSizeBuffer.flip();
        while (fileSizeBuffer.hasRemaining()){
            clientChannel.write(fileSizeBuffer);
        }
    }

    private static void sendFile(SocketChannel clientChannel, File file ) throws IOException {
        try (FileInputStream fis = new FileInputStream(file);
             FileChannel fileChannel = fis.getChannel();) {
            ByteBuffer buffer = ByteBuffer.allocate(4096);
            long bytesWritten = 0;
            while (bytesWritten < file.length()) {
                buffer.clear();
                int bytesRead = fileChannel.read(buffer);
                if (bytesRead == -1)
                    break;
                buffer.flip();
                while (buffer.hasRemaining()) {
                    bytesWritten += clientChannel.write(buffer);
                    if(bytesWritten % (file.length() / 10) == 0){
                        System.out.println("진행률: " + (bytesWritten * 100 / file.length()) + "%");
                    }
                }
            }
            System.out.println("File sent successfully: " + file.getName());
        }
    }
}
