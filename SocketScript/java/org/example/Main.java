package org.example;

public class Main {
    public static void main(String[] args) {
        MysqlManager.Instance();
        new Thread(()->{
            SocketConnection.Instance().Start();
        }).start();

        new Thread(()->{
            FileSocketConnection.Instance().Start();
        }).start();
    }
}