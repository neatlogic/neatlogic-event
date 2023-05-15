BEGIN;
-- ----------------------------
-- Records of event_type
-- ----------------------------
INSERT INTO `event_type` VALUES (851383272087552, '桌面类', 0, 2, 5, 1);
INSERT INTO `event_type` VALUES (851383322419200, '故障', 0, 6, 11, 1);
INSERT INTO `event_type` VALUES (851383490191360, '网络类事件', 0, 12, 13, 1);
INSERT INTO `event_type` VALUES (851383590854656, '应用系统', 0, 14, 15, 1);
INSERT INTO `event_type` VALUES (851383733460992, '测试', 851383272087552, 3, 4, 2);
INSERT INTO `event_type` VALUES (851383876067328, '应用系统', 851383322419200, 7, 8, 2);
INSERT INTO `event_type` VALUES (851383926398976, '数据库', 851383322419200, 9, 10, 2);
-- ----------------------------
-- Records of event_type_authority
-- ----------------------------
INSERT INTO `event_type_authority` VALUES (851383272087552, 'user', 'admin');
INSERT INTO `event_type_authority` VALUES (851383322419200, 'user', 'admin');
INSERT INTO `event_type_authority` VALUES (851383490191360, 'user', 'admin');
INSERT INTO `event_type_authority` VALUES (851383590854656, 'user', 'admin');
INSERT INTO `event_type_authority` VALUES (851383733460992, 'user', 'admin');
INSERT INTO `event_type_authority` VALUES (851383876067328, 'user', 'admin');
INSERT INTO `event_type_authority` VALUES (851383926398976, 'user', 'admin');

COMMIT;