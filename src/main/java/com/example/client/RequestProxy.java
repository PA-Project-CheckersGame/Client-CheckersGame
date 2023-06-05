package com.example.client;

public class RequestProxy {
    private static String loginReguest = "empty";
    private static String loginResponse = "empty";

    private static boolean requestSent = false;
    private static boolean responseReceived = false;

    private static boolean exit = false;

    public RequestProxy() {}

    public static String getLoginReguest() {
        return loginReguest;
    }

    public static void setLoginReguest(String loginReguest) {
        RequestProxy.loginReguest = loginReguest;
    }

    public static String getLoginResponse() {
        return loginResponse;
    }

    public static void setLoginResponse(String loginResponse) {
        RequestProxy.loginResponse = loginResponse;
    }

    public static boolean isExit() {
        return exit;
    }

    public static void setExit(boolean exit) {
        RequestProxy.exit = exit;
    }
    public static synchronized boolean isRequestSent() {
        return requestSent;
    }

    public static synchronized void setRequestSent(boolean requestSent) {
        RequestProxy.requestSent = requestSent;
    }

    public static synchronized boolean isResponseReceived() {
        return responseReceived;
    }

    public static synchronized void setResponseReceived(boolean responseReceived) {
        RequestProxy.responseReceived = responseReceived;
    }
}
