package com.smartcommunity.server.modules.property.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.smartcommunity.server.common.entity.BaseEntity;

@TableName("community")
public class Community extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long communityId;

    private String name;

    private String address;

    private Double mapLng;

    private Double mapLat;

    private Integer totalBuilding;

    private Integer totalHouse;

    private Integer status;

    public Long getCommunityId() { return communityId; }
    public void setCommunityId(Long communityId) { this.communityId = communityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Double getMapLng() { return mapLng; }
    public void setMapLng(Double mapLng) { this.mapLng = mapLng; }
    public Double getMapLat() { return mapLat; }
    public void setMapLat(Double mapLat) { this.mapLat = mapLat; }
    public Integer getTotalBuilding() { return totalBuilding; }
    public void setTotalBuilding(Integer totalBuilding) { this.totalBuilding = totalBuilding; }
    public Integer getTotalHouse() { return totalHouse; }
    public void setTotalHouse(Integer totalHouse) { this.totalHouse = totalHouse; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
