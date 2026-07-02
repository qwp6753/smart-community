package com.smartcommunity.server.modules.auth.vo;

import java.util.List;

public class MenuItemVO {

    private String name;

    private String path;

    private String component;

    private String icon;

    private String permission;

    private List<MenuItemVO> children;

    public MenuItemVO() {
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getPermission() { return permission; }
    public void setPermission(String permission) { this.permission = permission; }
    public List<MenuItemVO> getChildren() { return children; }
    public void setChildren(List<MenuItemVO> children) { this.children = children; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private String path;
        private String component;
        private String icon;
        private String permission;
        private List<MenuItemVO> children;

        public Builder name(String name) { this.name = name; return this; }
        public Builder path(String path) { this.path = path; return this; }
        public Builder component(String component) { this.component = component; return this; }
        public Builder icon(String icon) { this.icon = icon; return this; }
        public Builder permission(String permission) { this.permission = permission; return this; }
        public Builder children(List<MenuItemVO> children) { this.children = children; return this; }

        public MenuItemVO build() {
            MenuItemVO vo = new MenuItemVO();
            vo.setName(name);
            vo.setPath(path);
            vo.setComponent(component);
            vo.setIcon(icon);
            vo.setPermission(permission);
            vo.setChildren(children);
            return vo;
        }
    }
}
