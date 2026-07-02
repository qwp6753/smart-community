package com.smartcommunity.server.modules.access.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartcommunity.server.common.entity.BaseEntity;

@TableName("in_out_record")
public class InOutRecord extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long recordId;

    private Long personId;

    private String personName;

    private Long communityId;

    private String type;

    private String time;

    private String location;

    private String photoUrl;

    private Integer verified;

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }
    public Long getPersonId() { return personId; }
    public void setPersonId(Long personId) { this.personId = personId; }
    public String getPersonName() { return personName; }
    public void setPersonName(String personName) { this.personName = personName; }
    public Long getCommunityId() { return communityId; }
    public void setCommunityId(Long communityId) { this.communityId = communityId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public Integer getVerified() { return verified; }
    public void setVerified(Integer verified) { this.verified = verified; }
}
