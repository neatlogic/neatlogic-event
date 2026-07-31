package neatlogic.module.event.stephandler.utilhandler;

import neatlogic.framework.event.constvalue.EventProcessStepHandlerType;
import neatlogic.framework.event.dto.EventSolutionVo;
import neatlogic.framework.event.dto.EventTypeVo;
import neatlogic.framework.event.dto.EventVo;
import neatlogic.framework.event.exception.core.EventNotFoundException;
import neatlogic.framework.notify.core.INotifyPolicyHandler;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import neatlogic.module.event.dao.mapper.EventMapper;
import neatlogic.module.event.dao.mapper.EventSolutionMapper;
import neatlogic.module.event.dao.mapper.EventTypeMapper;
import neatlogic.module.event.notify.handler.EventNotifyPolicyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EventProcessUtilHandler extends ProcessStepInternalHandlerBase {

    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private EventSolutionMapper eventSolutionMapper;
    @Autowired
    private EventTypeMapper eventTypeMapper;

    @Override
    public String getHandler() {
        return EventProcessStepHandlerType.EVENT.getHandler();
    }

    @Override
    public Object getStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        return getNonStartStepInfo(currentProcessTaskStepVo);
    }

    @Override
    public Object getNonStartStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        Long eventId = eventMapper.getEventIdByProcessTaskStepId(currentProcessTaskStepVo.getId());
        if (eventId != null) {
            EventVo eventVo = eventMapper.getEventById(eventId);
            if (eventVo == null) {
                throw new EventNotFoundException(eventId);
            }
            EventTypeVo eventTypeVo = eventTypeMapper.getEventTypeById(eventVo.getEventTypeId());
            if (eventTypeVo != null) {
                List<EventTypeVo> eventTypeList = eventTypeMapper.getAncestorsAndSelfByLftRht(eventTypeVo.getLft(), eventTypeVo.getRht());
                List<String> eventTypeNameList = eventTypeList.stream().map(EventTypeVo::getName).collect(Collectors.toList());
                eventVo.setEventTypeNamePath(String.join("/", eventTypeNameList));
            }
            if (eventVo.getEventSolutionId() != null) {
                EventSolutionVo eventSolutionVo = eventSolutionMapper.getSolutionById(eventVo.getEventSolutionId());
                if (eventSolutionVo != null) {
                    eventVo.setEventSolutionName(eventSolutionVo.getName());
                }
            }
            return eventVo;
        }
        return null;
    }

    @Override
    public void updateProcessTaskStepUserAndWorker(Long processTaskId, Long processTaskStepId) {
        defaultUpdateProcessTaskStepUserAndWorker(processTaskId, processTaskStepId);
    }

    public IOperationType[] getStepActions() {
        return new IOperationType[]{
                ProcessTaskStepOperationType.STEP_VIEW,
                ProcessTaskStepOperationType.STEP_TRANSFER,
                ProcessTaskStepOperationType.STEP_PAUSE,
                ProcessTaskStepOperationType.STEP_RETREAT
        };
    }

    @Override
    public IOperationType[] getStepButtons() {
        return new IOperationType[]{
                ProcessTaskStepOperationType.STEP_COMPLETE,
                ProcessTaskStepOperationType.STEP_BACK,
                ProcessTaskStepOperationType.STEP_COMMENT,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskStepOperationType.STEP_ACCEPT,
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_RECOVER,
                ProcessTaskStepOperationType.STEP_REAPPROVAL
        };
    }

    @Override
    public Class<? extends INotifyPolicyHandler> getNotifyPolicyHandlerClass() {
        return EventNotifyPolicyHandler.class;
    }

    @Override
    public String[] getRegulateKeyList() {
        return new String[]{"authorityList", "notifyPolicyConfig", "actionConfig", "customButtonList", "customStatusList", "replaceableTextList", "workerPolicyConfig", "taskConfig", "enableReapproval", "formSceneUuid", "formSceneName", "autoStart", "isNeedUploadFile", "isNeedContent", "isRequired", "commentTemplateId", "tagList", "isAllowProcessOnMobile"};
    }

}
