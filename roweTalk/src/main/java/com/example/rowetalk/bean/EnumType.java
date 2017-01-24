package com.example.rowetalk.bean;

import java.io.Serializable;

public class EnumType implements Serializable {
	public static final int CATEGORY_TASK_STATUS = 1;
	public static final int CATEGORY_SUBTASK_STATUS = 2;
	public static final int CATEGORY_TASK_SCRIPT_TYPE = 3;
	public static final int CATEGORY_SIM_STATUS = 5;
	//public static final int CATEGORY_TASK_DAY_TYPE = 7;
	public static final int CATEGORY_SERVER_API = 8;
	public static final int CATEGORY_SIM_SLOT_STATUS = 9;
	public static final int CATEGORY_TASK_SIM_TYPE = 10;
	public static final int CATEGORY_APP_PUBLISH_TYPE = 11;
	public static final int CATEGORY_APP_UPDATE_TYPE = 12;
	public static final int CATEGORY_TASK_PUBLISH_TYPE = 13;
	public static final int CATEGORY_TASK_SHOT_TYPE = 14;
	public static final int CATEGORY_TASK_ENV_TYPE = 15;
	public static final int CATEGORY_TASK_APK_TYPE = 16;
	public static final int CATEGORY_TASK_IP_TYPE = 17;
	public static final int CATEGORY_VPN_ACCOUNT_TYPE = 18;
	public static final int CATEGORY_VPN_OPERATOR_TYPE = 19;
	public static final int CATEGORY_VPN_STATUS_TYPE = 21;
	public static final int CATEGORY_SERVER_STATUS_TYPE = 22;
	public static final int CATEGORY_PHONE_STATUS_TYPE = 23;
	public static final int CATEGORY_TASK_APK_DATA_TYPE = 24;
	
	public static final int TASK_STATUS_INIT = 0;
	public static final int TASK_STATUS_RUNNING = 1;
	public static final int TASK_STATUS_FINISHED = 2;
	public static final int TASK_STATUS_PAUSED = 3;
	
	public static final int SUBTASK_STATUS_ALLOCATED = 0;
	public static final int SUBTASK_STATUS_FINISHED = 1;
	public static final int SUBTASK_STATUS_FAILED = 2;
	public static final int SUBTASK_STATUS_CANCELED = 3;
	public static final int SUBTASK_STATUS_SIMCARD_NA = 4;
	
	public static final int TASK_APP_SIGNUP_SUCCESS = 1;
	public static final int TASK_APP_SIGNIN_SUCCESS = 2;
	
	public static final int SIM_STATUS_IN_SERVICE = 0;
	public static final int SIM_STATUS_OUT_OF_SERVICE = 1;
	public static final int SIM_STATUS_EMERGENCY_ONLY = 2;
	public static final int SIM_STATUS_NOT_AVAILABLE = 3;

	
	public static final int TASK_SCRIPT_TYPE_PURE = 0;
	public static final int TASK_SCRIPT_TYPE_ENCRYPT = 1;
	
	public static final int TASK_ENV_TYPE_NO_EMULATE = 0;
	public static final int TASK_ENV_TYPE_EMULATE = 1;
	
	public static final int TASK_SHOT_TYPE_ONCE = 0;
	public static final int TASK_SHOT_TYPE_DAYS = 1;
	public static final int TASK_SHOT_TYPE_ROUTINE = 2;
	
	
	public static final int TASK_APK_TYPE_DELETE = 0;
	public static final int TASK_APK_TYPE_KEEP = 1;
	
	public static final int TASK_APK_DATA_TYPE_NEVER_SAVE = 0;
	public static final int TASK_APK_DATA_TYPE_SAVE_TO_SERVER = 1;
	public static final int TASK_APK_DATA_TYPE_SAVE_TO_LOCAL = 2;
	
	public static final int TASK_SIM_TYPE_NO_SIM = 0; 
	public static final int TASK_SIM_TYPE_USE_IMSI = 1; 
	public static final int TASK_SIM_TYPE_SMS = 2;

	public static final int APP_PUBLISH_TYPE_INTERNAL = 0; 
	public static final int APP_PUBLISH_TYPE_PUBLIC = 1;
	
	public static final int APP_UPDATE_TYPE_DEFAULT = 0; 
	public static final int APP_UPDATE_TYPE_FORCE = 1;
	
	public static final int TASK_PUBLISH_TYPE_INTERNAL = 0; 
	public static final int TASK_PUBLISH_TYPE_PUBLIC = 1;
	
	public static final int TASK_IP_TYPE_NO_NEED = 0;
	public static final int TASK_IP_TYPE_DECRETE = 1;
	public static final int TASK_IP_TYPE_UNIQUE = 2;
	
	public static final int VPN_ACCOUNT_STATUS_UNUSED = 0;
	public static final int VPN_ACCOUNT_STATUS_WORKING = 1;
	public static final int VPN_ACCOUNT_STATUS_TESTING = 2;
	
	public static final int VPN_STATUS_AVAILABE = 0;
	public static final int VPN_STATUS_FAILED_CONNECTION = 1;
	public static final int VPN_STATUS_FAILED_HOME_UNREACHED = 2;
	public static final int VPN_STATUS_FAILED_FORERVER = 99;;
	
	public static final int VPN_RENEW_NONE = 0;
	public static final int VPN_RENEW_FOR_WORK = 1;
	public static final int VPN_RENEW_FOR_TEST = 2;
	
	public static final int SERVER_STATUS_STOPPED = 0;
	public static final int SERVER_STATUS_RUNNING = 1;
	public static final int SERVER_STATUS_PAUSED = 2;
	
	public static final int PHONE_STATUS_STOPPED = 0;
	public static final int PHONE_STATUS_RUNNING = 1;
	public static final int PHONE_STATUS_PAUSED = 2;
	
    private Integer id;

    private Integer category;

    private String desc;

    private String name;

    private Integer value;

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc == null ? null : desc.trim();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }
}