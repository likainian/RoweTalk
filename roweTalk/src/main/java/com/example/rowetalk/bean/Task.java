package com.example.rowetalk.bean;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
    private Integer id;

    private String eId;

    private String name;

    private String description;

    private Integer status;

    private String statusMessage;

    private Byte taskType;

    private Byte simType;

    private Byte envType;

    private Byte ipType;

    private Byte taskDays;

    private Integer privilege;

    private Byte scriptType;

    private String scriptPath;

    private Short scriptVersion;

    private Integer scriptSize;

    private Byte apkType;

    private Byte apkDataType;

    private Integer apkId;

    private String apkDirs;

    private Short extrasVersion;

    private String extrasPath;

    private Integer extrasSize;

    private Integer amount;

    private Integer success;

    private Integer dayLimit;

    private Integer keepSuccess;

    private Float day2Keep;

    private Float day3Keep;

    private Float day4Keep;

    private Float day5Keep;

    private Float day7Keep;

    private Float day15Keep;

    private Float day30Keep;

    private Integer p1;

    private Integer p2;

    private String p3;

    private Float payRatio;

    private Byte publishType;

    private Date publishTime;

    private Date startTime;

    private Date endTime;

    private Date normalFinishTime;

    private Date keepFinishTime;

    private Date createTime;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description == null ? null : description.trim();
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage == null ? null : statusMessage.trim();
    }

    public Byte getTaskType() {
        return taskType;
    }

    public void setTaskType(Byte taskType) {
        this.taskType = taskType;
    }

    public Byte getSimType() {
        return simType;
    }

    public void setSimType(Byte simType) {
        this.simType = simType;
    }

    public Byte getEnvType() {
        return envType;
    }

    public void setEnvType(Byte envType) {
        this.envType = envType;
    }

    public Byte getIpType() {
        return ipType;
    }

    public void setIpType(Byte ipType) {
        this.ipType = ipType;
    }

    public Byte getTaskDays() {
        return taskDays;
    }

    public void setTaskDays(Byte taskDays) {
        this.taskDays = taskDays;
    }

    public Integer getPrivilege() {
        return privilege;
    }

    public void setPrivilege(Integer privilege) {
        this.privilege = privilege;
    }

    public Byte getScriptType() {
        return scriptType;
    }

    public void setScriptType(Byte scriptType) {
        this.scriptType = scriptType;
    }

    public String getScriptPath() {
        return scriptPath;
    }

    public void setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath == null ? null : scriptPath.trim();
    }

    public Short getScriptVersion() {
        return scriptVersion;
    }

    public void setScriptVersion(Short scriptVersion) {
        this.scriptVersion = scriptVersion;
    }

    public Integer getScriptSize() {
        return scriptSize;
    }

    public void setScriptSize(Integer scriptSize) {
        this.scriptSize = scriptSize;
    }

    public Byte getApkType() {
        return apkType;
    }

    public void setApkType(Byte apkType) {
        this.apkType = apkType;
    }

    public Byte getApkDataType() {
        return apkDataType;
    }

    public void setApkDataType(Byte apkDataType) {
        this.apkDataType = apkDataType;
    }

    public Integer getApkId() {
        return apkId;
    }

    public void setApkId(Integer apkId) {
        this.apkId = apkId;
    }

    public String getApkDirs() {
        return apkDirs;
    }

    public void setApkDirs(String apkDirs) {
        this.apkDirs = apkDirs == null ? null : apkDirs.trim();
    }

    public Short getExtrasVersion() {
        return extrasVersion;
    }

    public void setExtrasVersion(Short extrasVersion) {
        this.extrasVersion = extrasVersion;
    }

    public String getExtrasPath() {
        return extrasPath;
    }

    public void setExtrasPath(String extrasPath) {
        this.extrasPath = extrasPath == null ? null : extrasPath.trim();
    }

    public Integer getExtrasSize() {
        return extrasSize;
    }

    public void setExtrasSize(Integer extrasSize) {
        this.extrasSize = extrasSize;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public Integer getDayLimit() {
        return dayLimit;
    }

    public void setDayLimit(Integer dayLimit) {
        this.dayLimit = dayLimit;
    }

    public Integer getKeepSuccess() {
        return keepSuccess;
    }

    public void setKeepSuccess(Integer keepSuccess) {
        this.keepSuccess = keepSuccess;
    }

    public Float getDay2Keep() {
        return day2Keep;
    }

    public void setDay2Keep(Float day2Keep) {
        this.day2Keep = day2Keep;
    }

    public Float getDay3Keep() {
        return day3Keep;
    }

    public void setDay3Keep(Float day3Keep) {
        this.day3Keep = day3Keep;
    }

    public Float getDay4Keep() {
        return day4Keep;
    }

    public void setDay4Keep(Float day4Keep) {
        this.day4Keep = day4Keep;
    }

    public Float getDay5Keep() {
        return day5Keep;
    }

    public void setDay5Keep(Float day5Keep) {
        this.day5Keep = day5Keep;
    }

    public Float getDay7Keep() {
        return day7Keep;
    }

    public void setDay7Keep(Float day7Keep) {
        this.day7Keep = day7Keep;
    }

    public Float getDay15Keep() {
        return day15Keep;
    }

    public void setDay15Keep(Float day15Keep) {
        this.day15Keep = day15Keep;
    }

    public Float getDay30Keep() {
        return day30Keep;
    }

    public void setDay30Keep(Float day30Keep) {
        this.day30Keep = day30Keep;
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

    public Float getPayRatio() {
        return payRatio;
    }

    public void setPayRatio(Float payRatio) {
        this.payRatio = payRatio;
    }

    public Byte getPublishType() {
        return publishType;
    }

    public void setPublishType(Byte publishType) {
        this.publishType = publishType;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Date getNormalFinishTime() {
        return normalFinishTime;
    }

    public void setNormalFinishTime(Date normalFinishTime) {
        this.normalFinishTime = normalFinishTime;
    }

    public Date getKeepFinishTime() {
        return keepFinishTime;
    }

    public void setKeepFinishTime(Date keepFinishTime) {
        this.keepFinishTime = keepFinishTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}