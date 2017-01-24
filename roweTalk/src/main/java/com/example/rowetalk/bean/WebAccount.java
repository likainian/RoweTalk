package com.example.rowetalk.bean;

import java.io.Serializable;
import java.util.Date;

public class WebAccount implements Serializable {
    private Integer id;

    private String eId;

    private Integer taskId;

    private Integer subtaskId;

    private String accountName;

    private String simImsi;

    private String simNumber;

    private String nosimKey;

    private String username;

    private String password;

    private Byte type;

    private Byte status;

    private String vpnCity;

    private Date signupTime;

    private Date signinTime;

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

    public Integer getSubtaskId() {
        return subtaskId;
    }

    public void setSubtaskId(Integer subtaskId) {
        this.subtaskId = subtaskId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName == null ? null : accountName.trim();
    }

    public String getSimImsi() {
        return simImsi;
    }

    public void setSimImsi(String simImsi) {
        this.simImsi = simImsi == null ? null : simImsi.trim();
    }

    public String getSimNumber() {
        return simNumber;
    }

    public void setSimNumber(String simNumber) {
        this.simNumber = simNumber == null ? null : simNumber.trim();
    }

    public String getNosimKey() {
        return nosimKey;
    }

    public void setNosimKey(String nosimKey) {
        this.nosimKey = nosimKey == null ? null : nosimKey.trim();
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

    public String getVpnCity() {
        return vpnCity;
    }

    public void setVpnCity(String vpnCity) {
        this.vpnCity = vpnCity == null ? null : vpnCity.trim();
    }

    public Date getSignupTime() {
        return signupTime;
    }

    public void setSignupTime(Date signupTime) {
        this.signupTime = signupTime;
    }

    public Date getSigninTime() {
        return signinTime;
    }

    public void setSigninTime(Date signinTime) {
        this.signinTime = signinTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}