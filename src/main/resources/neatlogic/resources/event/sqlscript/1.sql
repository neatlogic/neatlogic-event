-- ----------------------------
-- Table structure for event
-- ----------------------------
CREATE TABLE `event` (
  `id` bigint NOT NULL COMMENT '主键',
  `event_type_id` bigint NOT NULL COMMENT '事件类型ID',
  `event_solution_id` bigint DEFAULT NULL COMMENT '解决方案ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='事件表';

-- ----------------------------
-- Table structure for event_solution
-- ----------------------------
CREATE TABLE `event_solution` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '解决方案名称',
  `fcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人ID',
  `lcu` char(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人ID',
  `is_active` tinyint DEFAULT NULL COMMENT '是否启用',
  `fcd` timestamp(3) NULL DEFAULT NULL COMMENT '创建时间',
  `lcd` timestamp(3) NULL DEFAULT NULL COMMENT '更新时间',
  `content` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT '内容',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='事件解决方案表';

-- ----------------------------
-- Table structure for event_type
-- ----------------------------
CREATE TABLE `event_type` (
  `id` bigint NOT NULL COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '事件类型名称',
  `parent_id` bigint NOT NULL COMMENT '父类型ID',
  `lft` int DEFAULT NULL COMMENT '左编码',
  `rht` int DEFAULT NULL COMMENT '右编码',
  `layer` int DEFAULT NULL COMMENT '节点所在层级',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_lft_rht` (`lft`,`rht`) USING BTREE,
  KEY `idx_rht_lft` (`rht`,`lft`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='事件类型表';

-- ----------------------------
-- Table structure for event_type_authority
-- ----------------------------
CREATE TABLE `event_type_authority` (
  `event_type_id` bigint NOT NULL COMMENT '事件类型ID',
  `type` enum('common','user','team','role') CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权对象类型',
  `uuid` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '授权对象ID',
  PRIMARY KEY (`event_type_id`,`type`,`uuid`) USING BTREE,
  KEY `idx_event_type_id` (`event_type_id`) USING BTREE,
  KEY `idx_uuid` (`uuid`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='事件类型授权表';

-- ----------------------------
-- Table structure for event_type_solution
-- ----------------------------
CREATE TABLE `event_type_solution` (
  `event_type_id` bigint NOT NULL COMMENT '事件类型ID',
  `solution_id` bigint NOT NULL COMMENT '解决方案ID',
  PRIMARY KEY (`event_type_id`,`solution_id`) USING BTREE,
  KEY `idx_solution_id` (`solution_id`) USING BTREE,
  KEY `idx_event_type_id` (`event_type_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='事件类型-解决方案关联表';