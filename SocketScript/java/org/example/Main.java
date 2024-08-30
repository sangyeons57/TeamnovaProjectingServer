package org.example;

public class Main {
    public static void main(String[] args) {
        MysqlManager.Instance();
        SocketConnection.Instance().Start();
    }
}