package neatlogic.module.event.stephandler.utilhandler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import neatlogic.framework.crossover.CrossoverServiceFactory;
import neatlogic.framework.event.constvalue.EventProcessStepHandlerType;
import neatlogic.framework.event.dto.EventSolutionVo;
import neatlogic.framework.event.dto.EventTypeVo;
import neatlogic.framework.event.dto.EventVo;
import neatlogic.framework.event.exception.core.EventNotFoundException;
import neatlogic.framework.notify.crossover.INotifyServiceCrossoverService;
import neatlogic.framework.notify.dto.InvokeNotifyPolicyConfigVo;
import neatlogic.framework.process.operationauth.core.IOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskOperationType;
import neatlogic.framework.process.constvalue.ProcessTaskStepOperationType;
import neatlogic.framework.process.dto.ProcessTaskStepVo;
import neatlogic.framework.process.dto.processconfig.ActionConfigVo;
import neatlogic.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import neatlogic.framework.process.util.ProcessConfigUtil;
import neatlogic.module.event.dao.mapper.EventMapper;
import neatlogic.module.event.dao.mapper.EventSolutionMapper;
import neatlogic.module.event.dao.mapper.EventTypeMapper;
import neatlogic.module.event.notify.handler.EventNotifyPolicyHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
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

    @SuppressWarnings("serial")
    @Override
    public JSONObject makeupConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();

        /** 授权 **/
        IOperationType[] stepActions = {
                ProcessTaskStepOperationType.STEP_VIEW,
                ProcessTaskStepOperationType.STEP_TRANSFER,
                ProcessTaskStepOperationType.STEP_PAUSE,
                ProcessTaskStepOperationType.STEP_RETREAT
        };
        JSONArray authorityList = configObj.getJSONArray("authorityList");
        JSONArray authorityArray = ProcessConfigUtil.regulateAuthorityList(authorityList, stepActions);
        resultObj.put("authorityList", authorityArray);

        /** 按钮映射列表 **/
        IOperationType[] stepButtons = {
                ProcessTaskStepOperationType.STEP_COMPLETE,
                ProcessTaskStepOperationType.STEP_BACK,
                ProcessTaskStepOperationType.STEP_COMMENT,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskStepOperationType.STEP_ACCEPT,
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_RECOVER
        };

        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
        resultObj.put("customButtonList", customButtonArray);

        /** 状态映射列表 **/
        JSONArray customStatusList = configObj.getJSONArray("customStatusList");
        JSONArray customStatusArray = ProcessConfigUtil.regulateCustomStatusList(customStatusList);
        resultObj.put("customStatusList", customStatusArray);

        /** 可替换文本列表 **/
        resultObj.put("replaceableTextList", ProcessConfigUtil.regulateReplaceableTextList(configObj.getJSONArray("replaceableTextList")));

        /* 任务 */
        JSONObject taskConfig = configObj.getJSONObject("taskConfig");
        resultObj.put("taskConfig",taskConfig);
        return resultObj;
    }

    @Override
    public JSONObject regulateProcessStepConfig(JSONObject configObj) {
        if (configObj == null) {
            configObj = new JSONObject();
        }
        JSONObject resultObj = new JSONObject();

        /** 授权 **/
        IOperationType[] stepActions = {
                ProcessTaskStepOperationType.STEP_VIEW,
                ProcessTaskStepOperationType.STEP_TRANSFER,
                ProcessTaskStepOperationType.STEP_PAUSE,
                ProcessTaskStepOperationType.STEP_RETREAT
        };
        JSONArray authorityList = null;
        Integer enableAuthority = configObj.getInteger("enableAuthority");
        if (Objects.equals(enableAuthority, 1)) {
            authorityList = configObj.getJSONArray("authorityList");
        } else {
            enableAuthority = 0;
        }
        resultObj.put("enableAuthority", enableAuthority);
        JSONArray authorityArray = ProcessConfigUtil.regulateAuthorityList(authorityList, stepActions);
        resultObj.put("authorityList", authorityArray);

        /** 通知 **/
        JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
        INotifyServiceCrossoverService notifyServiceCrossoverService = CrossoverServiceFactory.getApi(INotifyServiceCrossoverService.class);
        InvokeNotifyPolicyConfigVo invokeNotifyPolicyConfigVo = notifyServiceCrossoverService.regulateNotifyPolicyConfig(notifyPolicyConfig, EventNotifyPolicyHandler.class);
        resultObj.put("notifyPolicyConfig", invokeNotifyPolicyConfigVo);

        /** 动作 **/
        JSONObject actionConfig = configObj.getJSONObject("actionConfig");
        ActionConfigVo actionConfigVo = JSONObject.toJavaObject(actionConfig, ActionConfigVo.class);
        if (actionConfigVo == null) {
            actionConfigVo = new ActionConfigVo();
        }
        actionConfigVo.setHandler(EventNotifyPolicyHandler.class.getName());
        resultObj.put("actionConfig", actionConfigVo);

        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
        /** 按钮映射列表 **/
        IOperationType[] stepButtons = {
                ProcessTaskStepOperationType.STEP_COMPLETE,
                ProcessTaskStepOperationType.STEP_BACK,
                ProcessTaskStepOperationType.STEP_COMMENT,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskStepOperationType.STEP_ACCEPT,
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_RECOVER,
                ProcessTaskStepOperationType.STEP_REAPPROVAL
        };

        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
        resultObj.put("customButtonList", customButtonArray);
        /** 状态映射列表 **/
        JSONArray customStatusList = configObj.getJSONArray("customStatusList");
        JSONArray customStatusArray = ProcessConfigUtil.regulateCustomStatusList(customStatusList);
        resultObj.put("customStatusList", customStatusArray);

        /** 可替换文本列表 **/
        resultObj.put("replaceableTextList", ProcessConfigUtil.regulateReplaceableTextList(configObj.getJSONArray("replaceableTextList")));

        /** 分配处理人 **/
        JSONObject workerPolicyConfig = configObj.getJSONObject("workerPolicyConfig");
        JSONObject workerPolicyObj = ProcessConfigUtil.regulateWorkerPolicyConfig(workerPolicyConfig);
        resultObj.put("workerPolicyConfig", workerPolicyObj);

        /* 任务 */
        JSONObject taskConfig = configObj.getJSONObject("taskConfig");
        resultObj.put("taskConfig",taskConfig);

        JSONObject simpleSettings = ProcessConfigUtil.regulateSimpleSettings(configObj);
        resultObj.putAll(simpleSettings);

        Integer enableReapproval = configObj.getInteger("enableReapproval");
        enableReapproval = enableReapproval == null ? 0 : enableReapproval;
        resultObj.put("enableReapproval", enableReapproval);
        /** 表单场景 **/
        String formSceneUuid = configObj.getString("formSceneUuid");
        String formSceneName = configObj.getString("formSceneName");
        resultObj.put("formSceneUuid", formSceneUuid == null ? "" : formSceneUuid);
        resultObj.put("formSceneName", formSceneName == null ? "" : formSceneName);
        return resultObj;
    }

}
