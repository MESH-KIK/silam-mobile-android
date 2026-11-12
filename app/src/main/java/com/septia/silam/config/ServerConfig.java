package com.septia.silam.config;

public class ServerConfig {
    // Emulator: 10.0.2.2 | HP asli: ganti ke IP laptop, mis. http://192.168.1.10:8000/api/
    public static final String BASE_URL = "http://10.0.2.2:8000/api/";

    public static final String API_LOGIN    = BASE_URL + "login";
    public static final String API_TRACKING = BASE_URL + "tracking?q=";
    public static final String API_ME       = BASE_URL + "me";
    public static final String API_LOGOUT   = BASE_URL + "logout";
    public static final String API_PESANAN  = BASE_URL + "pesanan";
    public static final String API_LAYANAN  = BASE_URL + "layanan";
    public static final String API_PELANGGAN = BASE_URL + "pelanggan";

    public static String pesananDetail(int id) { return API_PESANAN + "/" + id; }
    public static String pesananStatus(int id) { return API_PESANAN + "/" + id + "/status"; }
    public static String pesananBayar(int id)  { return API_PESANAN + "/" + id + "/bayar"; }
}