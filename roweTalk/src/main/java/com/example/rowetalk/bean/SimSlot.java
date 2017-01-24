package com.example.rowetalk.bean;

import java.io.Serializable;
import java.util.Date;

public class SimSlot implements Serializable {
    private Integer id;

    private String simImsi;

    private String simNumber;

    private Byte simSeq;

    private Byte simStatus;

    private Byte slotStatus;

    private Short slotSeq;

    private Short poolSeq;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public Byte getSimSeq() {
        return simSeq;
    }

    public void setSimSeq(Byte simSeq) {
        this.simSeq = simSeq;
    }

    public Byte getSimStatus() {
        return simStatus;
    }

    public void setSimStatus(Byte simStatus) {
        this.simStatus = simStatus;
    }

    public Byte getSlotStatus() {
        return slotStatus;
    }

    public void setSlotStatus(Byte slotStatus) {
        this.slotStatus = slotStatus;
    }

    public Short getSlotSeq() {
        return slotSeq;
    }

    public void setSlotSeq(Short slotSeq) {
        this.slotSeq = slotSeq;
    }

    public Short getPoolSeq() {
        return poolSeq;
    }

    public void setPoolSeq(Short poolSeq) {
        this.poolSeq = poolSeq;
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