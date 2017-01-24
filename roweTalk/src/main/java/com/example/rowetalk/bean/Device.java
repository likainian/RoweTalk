package com.example.rowetalk.bean;

import java.io.Serializable;
import java.util.Date;

public class Device implements Serializable {
    private Integer id;

    private Integer platformId;

    private String dkey;

    private String wmac;

    private String bmac;

    private String imei;

    private String serial;

    private String aid;

    private Byte status;

    private String area;

    private String essid;

    private String bssid;

    private String ipv4;

    private String ipv6;

    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getPlatformId() {
        return platformId;
    }

    public void setPlatformId(Integer platformId) {
        this.platformId = platformId;
    }

    public String getDkey() {
        return dkey;
    }

    public void setDkey(String dkey) {
        this.dkey = dkey == null ? null : dkey.trim();
    }

    public String getWmac() {
        return wmac;
    }

    public void setWmac(String wmac) {
        this.wmac = wmac == null ? null : wmac.trim();
    }

    public String getBmac() {
        return bmac;
    }

    public void setBmac(String bmac) {
        this.bmac = bmac == null ? null : bmac.trim();
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei == null ? null : imei.trim();
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial == null ? null : serial.trim();
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid == null ? null : aid.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area == null ? null : area.trim();
    }

    public String getEssid() {
        return essid;
    }

    public void setEssid(String essid) {
        this.essid = essid == null ? null : essid.trim();
    }

    public String getBssid() {
        return bssid;
    }

    public void setBssid(String bssid) {
        this.bssid = bssid == null ? null : bssid.trim();
    }

    public String getIpv4() {
        return ipv4;
    }

    public void setIpv4(String ipv4) {
        this.ipv4 = ipv4 == null ? null : ipv4.trim();
    }

    public String getIpv6() {
        return ipv6;
    }

    public void setIpv6(String ipv6) {
        this.ipv6 = ipv6 == null ? null : ipv6.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}