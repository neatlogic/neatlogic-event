package neatlogic.module.event.notify.handler;

import neatlogic.framework.auth.core.AuthBase;
import neatlogic.framework.dto.ConditionParamVo;
import neatlogic.framework.event.constvalue.EventProcessStepHandlerType;
import neatlogic.framework.notify.dto.NotifyTriggerVo;
import neatlogic.framework.process.auth.PROCESS_MODIFY;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyParam;
import neatlogic.framework.process.notify.constvalue.ProcessTaskStepTaskNotifyTriggerType;
import neatlogic.framework.process.notify.core.ProcessTaskNotifyHandlerBase;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @Title: EventNotifyPolicyHandler
 * @Package neatlogic.module.event.notify.handler
 * @Description: 事件节点通知策略处理器
 * @Author: linbq
 * @Date: 2021/3/8 11:11

 **/
@Component
public class EventNotifyPolicyHandler extends ProcessTaskNotifyHandlerBase {
    @Override
    public String getName() {
        return EventProcessStepHandlerType.EVENT.getName();
    }

    /**
     * 绑定权限，每种handler对应不同的权限
     */
    @Override
    public Class<? extends AuthBase> getAuthClass() {
        return PROCESS_MODIFY.class;
    }

//    @Override
//    public INotifyPolicyHandlerGroup getGroup() {
//        return ProcessNotifyPolicyHandlerGroup.TASKSTEP;
//    }

    @Override
    protected List<NotifyTriggerVo> myCustomNotifyTriggerList() {
        List<NotifyTriggerVo> returnList = new ArrayList<>();
        //任务
        for (ProcessTaskStepTaskNotifyTriggerType notifyTriggerType : ProcessTaskStepTaskNotifyTriggerType.values()) {
            returnList.add(new NotifyTriggerVo(notifyTriggerType));
        }
        return returnList;
    }

    @Override
    protected List<ConditionParamVo> myCustomSystemParamList() {
        List<ConditionParamVo> notifyPolicyParamList = new ArrayList<>();
        for(ProcessTaskStepTaskNotifyParam param : ProcessTaskStepTaskNotifyParam.values()) {
            notifyPolicyParamList.add(createConditionParam(param));
        }
        return notifyPolicyParamList;
    }
}
