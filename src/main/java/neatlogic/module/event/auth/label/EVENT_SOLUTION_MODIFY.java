package neatlogic.module.event.auth.label;

import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.process.auth.PROCESS_BASE;

import java.util.Collections;
import java.util.List;

/** 权限名称与说明使用国际化键，权限标识及校验规则保持不变。 */
public class EVENT_SOLUTION_MODIFY extends AuthBase {

	@Override
	public String getAuthDisplayName() {
		return "auth.event_solution_modify.name";
	}

	@Override
	public String getAuthIntroduction() {
		return "auth.event_solution_modify.description";
	}

	@Override
	public String getAuthGroup() {
		return "process";
	}

	@Override
	public Integer getSort() {
		return 3;
	}

	@Override
	public List<Class<? extends AuthBase>> getIncludeAuths(){
		return Collections.singletonList(PROCESS_BASE.class);
	}
}
