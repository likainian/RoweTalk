package com.example.rowetalk.bean;

import java.io.Serializable;
import java.util.Date;

public class VpnAccount implements Serializable {
    private Integer id;

    private String server;

    private String username;

    private String password;

    private Byte ppe;

    private String dns;

    private String vendor;

    private String description;

    private Byte type;

    private Byte status;

    private Short phoneSeq;

    private String city;

    private Date allocTime;

    private Date expireTime;

    private Integer dayFailLimit;

    private Integer todayFailedCount;

    private Date todayFailedReport;

    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server == null ? null : server.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username == null ? null : username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password == null ? null : password.trim();
    }

    public Byte getPpe() {
        return ppe;
    }

    public void setPpe(Byte ppe) {
        this.ppe = ppe;
    }

    public String getDns() {
        return dns;
    }

    public void setDns(String dns) {
        this.dns = dns == null ? null : dns.trim();
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor == null ? null : vendor.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Short getPhoneSeq() {
        return phoneSeq;
    }

    public void setPhoneSeq(Short phoneSeq) {
        this.phoneSeq = phoneSeq;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city == null ? null : city.trim();
    }

    public Date getAllocTime() {
        return allocTime;
    }

    public void setAllocTime(Date allocTime) {
        this.allocTime = allocTime;
    }

    public Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Date expireTime) {
        this.expireTime = expireTime;
    }

    public Integer getDayFailLimit() {
        return dayFailLimit;
    }

    public void setDayFailLimit(Integer dayFailLimit) {
        this.dayFailLimit = dayFailLimit;
    }

    public Integer getTodayFailedCount() {
        return todayFailedCount;
    }

    public void setTodayFailedCount(Integer todayFailedCount) {
        this.todayFailedCount = todayFailedCount;
    }

    public Date getTodayFailedReport() {
        return todayFailedReport;
    }

    public void setTodayFailedReport(Date todayFailedReport) {
        this.todayFailedReport = todayFailedReport;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}