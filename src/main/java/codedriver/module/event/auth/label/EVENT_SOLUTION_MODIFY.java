package codedriver.module.event.auth.label;

import codedriver.framework.auth.core.AuthBase;

public class EVENT_SOLUTION_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "解决方案管理权限";
	}

	@Override
	public String getAuthIntroduction() {
		return "对解决方案进行添加、修改和删除";
	}

	@Override
	public String getAuthGroup() {
		return "process";
	}

	@Override
	public Integer sort() {
		return 3;
	}
}
