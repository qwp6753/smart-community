package com.smartcommunity.server.modules.property.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartcommunity.server.common.entity.BaseEntity;

@TableName("person")
public class Person extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long personId;

    private Long communityId;

    private String userName;

    private String mobile;

    private String sex;

    private String houseNo;

    private String personType;

    private String faceUrl;

    private Integer state;

    private String remark;

    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public Long getCommunityId() { return communityId; }
    public void setCommunityId(Long communityId) { this.communityId = communityId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }
    public String getHouseNo() { return houseNo; }
    public void setHouseNo(String houseNo) { this.houseNo = houseNo; }
    public String getPersonType() { return personType; }
    public void setPersonType(String personType) { this.personType = personType; }
    public String getFaceUrl() { return faceUrl; }
    public void setFaceUrl(String faceUrl) { this.faceUrl = faceUrl; }
    public Integer getState() { return state; }
    public void setState(Integer state) { this.state = state; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
