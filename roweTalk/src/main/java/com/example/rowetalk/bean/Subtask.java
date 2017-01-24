package com.example.rowetalk.bean;

import java.io.Serializable;
import java.util.Date;

public class Subtask implements Serializable {
    private Integer id;

    private String eId;

    private Integer taskId;

    private Byte daySeq;

    private Short phoneSeq;

    private String deviceKey;

    private Byte status;

    private Byte keepDays;

    private String message;

    private Integer costTime;

    private Integer scriptTime;

    private Integer seq;

    private Byte simPay;

    private Byte failCount;

    private String ipAddr;

    private String moreAddr;

    private String dataPath;

    private Integer dataSize;

    private Date allocTime;

    private Date reportTime;

    private String vpnCity;

    private Integer p1;

    private Integer p2;

    private String p3;

    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String geteId() {
        return eId;
    }

    public void seteId(String eId) {
        this.eId = eId == null ? null : eId.trim();
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public Byte getDaySeq() {
        return daySeq;
    }

    public void setDaySeq(Byte daySeq) {
        this.daySeq = daySeq;
    }

    public Short getPhoneSeq() {
        return phoneSeq;
    }

    public void setPhoneSeq(Short phoneSeq) {
        this.phoneSeq = phoneSeq;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey == null ? null : deviceKey.trim();
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Byte getKeepDays() {
        return keepDays;
    }

    public void setKeepDays(Byte keepDays) {
        this.keepDays = keepDays;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? null : message.trim();
    }

    public Integer getCostTime() {
        return costTime;
    }

    public void setCostTime(Integer costTime) {
        this.costTime = costTime;
    }

    public Integer getScriptTime() {
        return scriptTime;
    }

    public void setScriptTime(Integer scriptTime) {
        this.scriptTime = scriptTime;
    }

    public Integer getSeq() {
        return seq;
    }

    public void setSeq(Integer seq) {
        this.seq = seq;
    }

    public Byte getSimPay() {
        return simPay;
    }

    public void setSimPay(Byte simPay) {
        this.simPay = simPay;
    }

    public Byte getFailCount() {
        return failCount;
    }

    public void setFailCount(Byte failCount) {
        this.failCount = failCount;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr == null ? null : ipAddr.trim();
    }

    public String getMoreAddr() {
        return moreAddr;
    }

    public void setMoreAddr(String moreAddr) {
        this.moreAddr = moreAddr == null ? null : moreAddr.trim();
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath == null ? null : dataPath.trim();
    }

    public Integer getDataSize() {
        return dataSize;
    }

    public void setDataSize(Integer dataSize) {
        this.dataSize = dataSize;
    }

    public Date getAllocTime() {
        return allocTime;
    }

    public void setAllocTime(Date allocTime) {
        this.allocTime = allocTime;
    }

    public Date getReportTime() {
        return reportTime;
    }

    public void setReportTime(Date reportTime) {
        this.reportTime = reportTime;
    }

    public String getVpnCity() {
        return vpnCity;
    }

    public void setVpnCity(String vpnCity) {
        this.vpnCity = vpnCity == null ? null : vpnCity.trim();
    }

    public Integer getP1() {
        return p1;
    }

    public void setP1(Integer p1) {
        this.p1 = p1;
    }

    public Integer getP2() {
        return p2;
    }

    public void setP2(Integer p2) {
        this.p2 = p2;
    }

    public String getP3() {
        return p3;
    }

    public void setP3(String p3) {
        this.p3 = p3 == null ? null : p3.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}