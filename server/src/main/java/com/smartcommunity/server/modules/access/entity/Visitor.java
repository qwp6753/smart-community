package com.smartcommunity.server.modules.access.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartcommunity.server.common.entity.BaseEntity;

@TableName("visitor")
public class Visitor extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long visitorId;

    private Long personId;

    private String name;

    private String mobile;

    private String idCard;

    private Long communityId;

    private String visitTime;

    private String leaveTime;

    private String reason;

    private Integer status;

    public Long getVisitorId() { return visitorId; }
    public void setVisitorId(Long visitorId) { this.visitorId = visitorId; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }
    public Long getCommunityId() { return communityId; }
    public void setCommunityId(Long communityId) { this.communityId = communityId; }
    public String getVisitTime() { return visitTime; }
    public void setVisitTime(String visitTime) { this.visitTime = visitTime; }
    public String getLeaveTime() { return leaveTime; }
    public void setLeaveTime(String leaveTime) { this.leaveTime = leaveTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
