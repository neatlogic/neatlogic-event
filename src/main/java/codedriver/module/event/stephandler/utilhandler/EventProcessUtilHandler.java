package codedriver.module.event.stephandler.utilhandler;

import codedriver.framework.common.constvalue.GroupSearch;
import codedriver.framework.dto.UserVo;
import codedriver.framework.process.constvalue.ProcessTaskOperationType;
import codedriver.framework.process.constvalue.ProcessTaskStepUserStatus;
import codedriver.framework.process.constvalue.ProcessUserType;
import codedriver.framework.process.dto.*;
import codedriver.framework.process.dto.processconfig.ActionConfigActionVo;
import codedriver.framework.process.dto.processconfig.ActionConfigVo;
import codedriver.framework.process.dto.processconfig.NotifyPolicyConfigVo;
import codedriver.framework.process.stephandler.core.ProcessStepInternalHandlerBase;
import codedriver.framework.process.util.ProcessConfigUtil;
import codedriver.framework.event.constvalue.EventProcessStepHandlerType;
import codedriver.module.event.dao.mapper.EventMapper;
import codedriver.module.event.dao.mapper.EventSolutionMapper;
import codedriver.module.event.dao.mapper.EventTypeMapper;
import codedriver.framework.event.dto.EventSolutionVo;
import codedriver.framework.event.dto.EventTypeVo;
import codedriver.framework.event.dto.EventVo;
import codedriver.framework.event.exception.core.EventNotFoundException;
import codedriver.module.event.notify.handler.EventNotifyPolicyHandler;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventProcessUtilHandler extends ProcessStepInternalHandlerBase {

    @Autowired
    private EventMapper eventMapper;
    @Autowired
    private EventSolutionMapper eventSolutionMapper;
    @Autowired
    private EventTypeMapper eventTypeMapper;

//    @Autowired
//    private ProcessTaskStepSubtaskMapper processTaskStepSubtaskMapper;

    @Override
    public String getHandler() {
        return EventProcessStepHandlerType.EVENT.getHandler();
    }

    @Override
    public Object getHandlerStepInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
        return getHandlerStepInitInfo(currentProcessTaskStepVo);
    }

    @Override
    public Object getHandlerStepInitInfo(ProcessTaskStepVo currentProcessTaskStepVo) {
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
    public void makeupProcessStep(ProcessStepVo processStepVo, JSONObject stepConfigObj) {
        /** 组装通知策略id **/
        JSONObject notifyPolicyConfig = stepConfigObj.getJSONObject("notifyPolicyConfig");
        NotifyPolicyConfigVo notifyPolicyConfigVo = JSONObject.toJavaObject(notifyPolicyConfig, NotifyPolicyConfigVo.class);
        if (notifyPolicyConfigVo != null) {
            Long policyId = notifyPolicyConfigVo.getPolicyId();
            if (policyId != null) {
                processStepVo.setNotifyPolicyId(policyId);
            }
        }

        JSONObject actionConfig = stepConfigObj.getJSONObject("actionConfig");
        ActionConfigVo actionConfigVo = JSONObject.toJavaObject(actionConfig, ActionConfigVo.class);
        if (actionConfigVo != null) {
            List<ActionConfigActionVo> actionList = actionConfigVo.getActionList();
            if (CollectionUtils.isNotEmpty(actionList)) {
                List<String> integrationUuidList = new ArrayList<>();
                for (ActionConfigActionVo actionVo : actionList) {
                    String integrationUuid = actionVo.getIntegrationUuid();
                    if (StringUtils.isNotBlank(integrationUuid)) {
                        integrationUuidList.add(integrationUuid);
                    }
                }
                processStepVo.setIntegrationUuidList(integrationUuidList);
            }
        }

        /** 组装分配策略 **/
        JSONObject workerPolicyConfig = stepConfigObj.getJSONObject("workerPolicyConfig");
        if (MapUtils.isNotEmpty(workerPolicyConfig)) {
            JSONArray policyList = workerPolicyConfig.getJSONArray("policyList");
            if (CollectionUtils.isNotEmpty(policyList)) {
                List<ProcessStepWorkerPolicyVo> workerPolicyList = new ArrayList<>();
                for (int k = 0; k < policyList.size(); k++) {
                    JSONObject policyObj = policyList.getJSONObject(k);
                    if (!"1".equals(policyObj.getString("isChecked"))) {
                        continue;
                    }
                    ProcessStepWorkerPolicyVo processStepWorkerPolicyVo = new ProcessStepWorkerPolicyVo();
                    processStepWorkerPolicyVo.setProcessUuid(processStepVo.getProcessUuid());
                    processStepWorkerPolicyVo.setProcessStepUuid(processStepVo.getUuid());
                    processStepWorkerPolicyVo.setPolicy(policyObj.getString("type"));
                    processStepWorkerPolicyVo.setSort(k + 1);
                    processStepWorkerPolicyVo.setConfig(policyObj.getString("config"));
                    workerPolicyList.add(processStepWorkerPolicyVo);
                }
                processStepVo.setWorkerPolicyList(workerPolicyList);
            }
        }

        JSONArray tagList = stepConfigObj.getJSONArray("tagList");
        if (CollectionUtils.isNotEmpty(tagList)) {
            processStepVo.setTagList(tagList.toJavaList(String.class));
        }

        //保存子任务
        JSONObject taskConfig = stepConfigObj.getJSONObject("taskConfig");
        if(MapUtils.isNotEmpty(taskConfig)){
            ProcessStepTaskConfigVo taskConfigVo = JSONObject.toJavaObject(taskConfig,ProcessStepTaskConfigVo.class);
            processStepVo.setTaskConfigVo(taskConfigVo);
        }
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
        ProcessTaskOperationType[] stepActions = {
                ProcessTaskOperationType.STEP_VIEW,
                ProcessTaskOperationType.STEP_TRANSFER,
                ProcessTaskOperationType.STEP_PAUSE,
                ProcessTaskOperationType.STEP_RETREAT
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

        /** 按钮映射列表 **/
        ProcessTaskOperationType[] stepButtons = {
                ProcessTaskOperationType.STEP_COMPLETE,
                ProcessTaskOperationType.STEP_BACK,
                ProcessTaskOperationType.STEP_COMMENT,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskOperationType.STEP_START,
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_RECOVER
        };
        /** 子任务按钮映射列表 **/
//        ProcessTaskOperationType[] subtaskButtons = {
//                ProcessTaskOperationType.SUBTASK_ABORT,
//                ProcessTaskOperationType.SUBTASK_COMMENT,
//                ProcessTaskOperationType.SUBTASK_COMPLETE,
//                ProcessTaskOperationType.SUBTASK_CREATE,
//                ProcessTaskOperationType.SUBTASK_REDO,
//                ProcessTaskOperationType.SUBTASK_EDIT
//        };

        JSONArray customButtonList = configObj.getJSONArray("customButtonList");
        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
//        JSONArray subtaskCustomButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, subtaskButtons, "子任务");
//        customButtonArray.addAll(subtaskCustomButtonArray);
        resultObj.put("customButtonList", customButtonArray);

        /** 状态映射列表 **/
        JSONArray customStatusList = configObj.getJSONArray("customStatusList");
        JSONArray customStatusArray = ProcessConfigUtil.regulateCustomStatusList(customStatusList);
        resultObj.put("customStatusList", customStatusArray);

        /** 可替换文本列表 **/
        resultObj.put("replaceableTextList", ProcessConfigUtil.regulateReplaceableTextList(configObj.getJSONArray("replaceableTextList")));

        /** 通知 **/
        JSONObject notifyPolicyConfig = configObj.getJSONObject("notifyPolicyConfig");
        NotifyPolicyConfigVo notifyPolicyConfigVo = JSONObject.toJavaObject(notifyPolicyConfig, NotifyPolicyConfigVo.class);
        if (notifyPolicyConfigVo == null) {
            notifyPolicyConfigVo = new NotifyPolicyConfigVo();
        }
        notifyPolicyConfigVo.setHandler(EventNotifyPolicyHandler.class.getName());
        resultObj.put("notifyPolicyConfig", notifyPolicyConfigVo);

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
        ProcessTaskOperationType[] stepActions = {
                ProcessTaskOperationType.STEP_VIEW,
                ProcessTaskOperationType.STEP_TRANSFER,
                ProcessTaskOperationType.STEP_PAUSE,
                ProcessTaskOperationType.STEP_RETREAT
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
        NotifyPolicyConfigVo notifyPolicyConfigVo = JSONObject.toJavaObject(notifyPolicyConfig, NotifyPolicyConfigVo.class);
        if (notifyPolicyConfigVo == null) {
            notifyPolicyConfigVo = new NotifyPolicyConfigVo();
        }
        notifyPolicyConfigVo.setHandler(EventNotifyPolicyHandler.class.getName());
        resultObj.put("notifyPolicyConfig", notifyPolicyConfigVo);

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
        ProcessTaskOperationType[] stepButtons = {
                ProcessTaskOperationType.STEP_COMPLETE,
                ProcessTaskOperationType.STEP_BACK,
                ProcessTaskOperationType.STEP_COMMENT,
                ProcessTaskOperationType.PROCESSTASK_TRANSFER,
                ProcessTaskOperationType.STEP_START,
                ProcessTaskOperationType.PROCESSTASK_ABORT,
                ProcessTaskOperationType.PROCESSTASK_RECOVER,
                ProcessTaskOperationType.STEP_REAPPROVAL
        };

        /** 子任务按钮映射列表 **/
//        ProcessTaskOperationType[] subtaskButtons = {
//                ProcessTaskOperationType.SUBTASK_ABORT,
//                ProcessTaskOperationType.SUBTASK_COMMENT,
//                ProcessTaskOperationType.SUBTASK_COMPLETE,
//                ProcessTaskOperationType.SUBTASK_CREATE,
//                ProcessTaskOperationType.SUBTASK_REDO,
//                ProcessTaskOperationType.SUBTASK_EDIT
//        };
        JSONArray customButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, stepButtons);
//        JSONArray subtaskCustomButtonArray = ProcessConfigUtil.regulateCustomButtonList(customButtonList, subtaskButtons, "子任务");
//        customButtonArray.addAll(subtaskCustomButtonArray);
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
        return resultObj;
    }

}
