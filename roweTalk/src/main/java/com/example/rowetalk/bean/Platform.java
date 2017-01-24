package com.example.rowetalk.bean;

import java.io.Serializable;
import java.util.Date;

public class Platform implements Serializable {
    private Integer id;

    private String bdId;

    private String bdDisplay;

    private String bdProduct;

    private String bdDevice;

    private String bdBoard;

    private String bdManufacture;

    private String bdBrand;

    private String bdModel;

    private String bdBootloader;

    private String bdHardware;

    private String bdVerInc;

    private String bdVerRel;

    private String bdVerSdkInt;

    private String bdVerCode;

    private String bdType;

    private String bdTags;

    private String bdFingerprint;

    private String bdTime;

    private String bdUser;

    private String bdHost;

    private String bdRadioVer;

    private String mem;

    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBdId() {
        return bdId;
    }

    public void setBdId(String bdId) {
        this.bdId = bdId == null ? null : bdId.trim();
    }

    public String getBdDisplay() {
        return bdDisplay;
    }

    public void setBdDisplay(String bdDisplay) {
        this.bdDisplay = bdDisplay == null ? null : bdDisplay.trim();
    }

    public String getBdProduct() {
        return bdProduct;
    }

    public void setBdProduct(String bdProduct) {
        this.bdProduct = bdProduct == null ? null : bdProduct.trim();
    }

    public String getBdDevice() {
        return bdDevice;
    }

    public void setBdDevice(String bdDevice) {
        this.bdDevice = bdDevice == null ? null : bdDevice.trim();
    }

    public String getBdBoard() {
        return bdBoard;
    }

    public void setBdBoard(String bdBoard) {
        this.bdBoard = bdBoard == null ? null : bdBoard.trim();
    }

    public String getBdManufacture() {
        return bdManufacture;
    }

    public void setBdManufacture(String bdManufacture) {
        this.bdManufacture = bdManufacture == null ? null : bdManufacture.trim();
    }

    public String getBdBrand() {
        return bdBrand;
    }

    public void setBdBrand(String bdBrand) {
        this.bdBrand = bdBrand == null ? null : bdBrand.trim();
    }

    public String getBdModel() {
        return bdModel;
    }

    public void setBdModel(String bdModel) {
        this.bdModel = bdModel == null ? null : bdModel.trim();
    }

    public String getBdBootloader() {
        return bdBootloader;
    }

    public void setBdBootloader(String bdBootloader) {
        this.bdBootloader = bdBootloader == null ? null : bdBootloader.trim();
    }

    public String getBdHardware() {
        return bdHardware;
    }

    public void setBdHardware(String bdHardware) {
        this.bdHardware = bdHardware == null ? null : bdHardware.trim();
    }

    public String getBdVerInc() {
        return bdVerInc;
    }

    public void setBdVerInc(String bdVerInc) {
        this.bdVerInc = bdVerInc == null ? null : bdVerInc.trim();
    }

    public String getBdVerRel() {
        return bdVerRel;
    }

    public void setBdVerRel(String bdVerRel) {
        this.bdVerRel = bdVerRel == null ? null : bdVerRel.trim();
    }

    public String getBdVerSdkInt() {
        return bdVerSdkInt;
    }

    public void setBdVerSdkInt(String bdVerSdkInt) {
        this.bdVerSdkInt = bdVerSdkInt == null ? null : bdVerSdkInt.trim();
    }

    public String getBdVerCode() {
        return bdVerCode;
    }

    public void setBdVerCode(String bdVerCode) {
        this.bdVerCode = bdVerCode == null ? null : bdVerCode.trim();
    }

    public String getBdType() {
        return bdType;
    }

    public void setBdType(String bdType) {
        this.bdType = bdType == null ? null : bdType.trim();
    }

    public String getBdTags() {
        return bdTags;
    }

    public void setBdTags(String bdTags) {
        this.bdTags = bdTags == null ? null : bdTags.trim();
    }

    public String getBdFingerprint() {
        return bdFingerprint;
    }

    public void setBdFingerprint(String bdFingerprint) {
        this.bdFingerprint = bdFingerprint == null ? null : bdFingerprint.trim();
    }

    public String getBdTime() {
        return bdTime;
    }

    public void setBdTime(String bdTime) {
        this.bdTime = bdTime == null ? null : bdTime.trim();
    }

    public String getBdUser() {
        return bdUser;
    }

    public void setBdUser(String bdUser) {
        this.bdUser = bdUser == null ? null : bdUser.trim();
    }

    public String getBdHost() {
        return bdHost;
    }

    public void setBdHost(String bdHost) {
        this.bdHost = bdHost == null ? null : bdHost.trim();
    }

    public String getBdRadioVer() {
        return bdRadioVer;
    }

    public void setBdRadioVer(String bdRadioVer) {
        this.bdRadioVer = bdRadioVer == null ? null : bdRadioVer.trim();
    }

    public String getMem() {
        return mem;
    }

    public void setMem(String mem) {
        this.mem = mem == null ? null : mem.trim();
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}