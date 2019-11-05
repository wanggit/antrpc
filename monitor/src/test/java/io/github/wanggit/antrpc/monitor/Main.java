package io.github.wanggit.antrpc.monitor;

public class Main {

    public static void main(String[] args) {
        String uri = "http://192.168.14.132:9200";
        String ip = uri.substring(uri.lastIndexOf("/") + 1, uri.lastIndexOf(":"));
        System.out.println(uri.substring(uri.lastIndexOf(":") + 1));
        System.out.println(ip);
    }
}
