ALTER TABLE `user` ADD UNIQUE INDEX idx_user_username (username);
ALTER TABLE `user` ADD INDEX idx_user_mobile (mobile);
ALTER TABLE `user` ADD INDEX idx_user_status (status);

ALTER TABLE role ADD UNIQUE INDEX idx_role_code (role_code);
ALTER TABLE role ADD INDEX idx_role_status (status);

ALTER TABLE menu ADD INDEX idx_menu_parent_id (parent_id);
ALTER TABLE menu ADD INDEX idx_menu_type (type);
ALTER TABLE menu ADD INDEX idx_menu_permission (permission);
ALTER TABLE menu ADD INDEX idx_menu_status (status);

ALTER TABLE community ADD INDEX idx_community_name (name);
ALTER TABLE community ADD INDEX idx_community_status (status);

ALTER TABLE camera ADD INDEX idx_camera_community_id (community_id);
ALTER TABLE camera ADD INDEX idx_camera_status (status);

ALTER TABLE person ADD INDEX idx_person_community_id (community_id);
ALTER TABLE person ADD INDEX idx_person_user_name (user_name);
ALTER TABLE person ADD INDEX idx_person_mobile (mobile);
ALTER TABLE person ADD INDEX idx_person_house_no (house_no);
ALTER TABLE person ADD INDEX idx_person_state (state);

ALTER TABLE in_out_record ADD INDEX idx_record_person_id (person_id);
ALTER TABLE in_out_record ADD INDEX idx_record_person_name (person_name);
ALTER TABLE in_out_record ADD INDEX idx_record_community_id (community_id);
ALTER TABLE in_out_record ADD INDEX idx_record_type (type);
ALTER TABLE in_out_record ADD INDEX idx_record_create_time (create_time);

ALTER TABLE visitor ADD INDEX idx_visitor_person_id (person_id);
ALTER TABLE visitor ADD INDEX idx_visitor_community_id (community_id);
ALTER TABLE visitor ADD INDEX idx_visitor_status (status);
ALTER TABLE visitor ADD INDEX idx_visitor_name (name);
ALTER TABLE visitor ADD INDEX idx_visitor_mobile (mobile);

ALTER TABLE operation_log ADD INDEX idx_log_user_id (user_id);
ALTER TABLE operation_log ADD INDEX idx_log_create_time (create_time);
ALTER TABLE operation_log ADD INDEX idx_log_module (module);

ALTER TABLE camera ADD CONSTRAINT fk_camera_community FOREIGN KEY (community_id) REFERENCES community(community_id);
ALTER TABLE person ADD CONSTRAINT fk_person_community FOREIGN KEY (community_id) REFERENCES community(community_id);
ALTER TABLE in_out_record ADD CONSTRAINT fk_record_person FOREIGN KEY (person_id) REFERENCES person(person_id);
ALTER TABLE in_out_record ADD CONSTRAINT fk_record_community FOREIGN KEY (community_id) REFERENCES community(community_id);
ALTER TABLE visitor ADD CONSTRAINT fk_visitor_person FOREIGN KEY (person_id) REFERENCES person(person_id);
ALTER TABLE visitor ADD CONSTRAINT fk_visitor_community FOREIGN KEY (community_id) REFERENCES community(community_id);
ALTER TABLE user_role ADD CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES `user`(user_id);
ALTER TABLE user_role ADD CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(role_id);
ALTER TABLE role_menu ADD CONSTRAINT fk_role_menu_role FOREIGN KEY (role_id) REFERENCES role(role_id);
ALTER TABLE role_menu ADD CONSTRAINT fk_role_menu_menu FOREIGN KEY (menu_id) REFERENCES menu(menu_id);
ALTER TABLE operation_log ADD CONSTRAINT fk_log_user FOREIGN KEY (user_id) REFERENCES `user`(user_id);
