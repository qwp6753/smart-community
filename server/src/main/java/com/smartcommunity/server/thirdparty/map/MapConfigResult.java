package com.smartcommunity.server.thirdparty.map;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapConfigResult {

    private String ak;
    private String securityJsCode;
    private String centerLng;
    private String centerLat;
    private Object markers;

    public MapConfigResult() {
    }

    public String getAk() { return ak; }
    public void setAk(String ak) { this.ak = ak; }
    public String getSecurityJsCode() { return securityJsCode; }
    public void setSecurityJsCode(String securityJsCode) { this.securityJsCode = securityJsCode; }
    public String getCenterLng() { return centerLng; }
    public void setCenterLng(String centerLng) { this.centerLng = centerLng; }
    public String getCenterLat() { return centerLat; }
    public void setCenterLat(String centerLat) { this.centerLat = centerLat; }
    public Object getMarkers() { return markers; }
    public void setMarkers(Object markers) { this.markers = markers; }
}
