package codedriver.module.event.dao.mapper;

import codedriver.framework.dto.AuthorityVo;
import codedriver.module.event.dto.EventSolutionVo;
import codedriver.module.event.dto.EventTypeVo;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface EventTypeMapper {

	List<EventTypeVo> getEventTypeByParentId(Long parentId);

	int updateEventTypeLeftRightCode(@Param("id") Long id, @Param("lft") Integer lft, @Param("rht") Integer rht);

	Integer getMaxRhtCode();

	int searchEventTypeCount(EventTypeVo eventTypeVo);

	List<EventTypeVo> searchEventType(EventTypeVo eventTypeVo);

	List<EventTypeVo> getAncestorsAndSelfByLftRht(@Param("lft") Integer lft, @Param("rht") Integer rht);

	EventTypeVo getTopEventTypeByLftRht(@Param("lft") Integer lft, @Param("rht") Integer rht);

	List<EventTypeVo> getChildrenByLftRhtLayer(@Param("lft") Integer lft, @Param("rht") Integer rht,@Param("layer") Integer layer);

//	int calculateLayer(@Param("lft") Integer lft, @Param("rht") Integer rht);

	List<EventTypeVo> getEventTypeChildCountListByIdList(List<Long> eventTypeIdList);

	EventTypeVo getEventTypeSolutionCountByLftRht(@Param("lft") Integer lft, @Param("rht") Integer rht);

	int checkEventTypeIsExists(Long id);

	int updateEventTypeNameById(EventTypeVo eventTypeVo);

//	int getEventTypeCountOnLock();

//	int checkLeftRightCodeIsWrong();

	int checkEventTypeIsExistsByLeftRightCode(@Param("id") Long id, @Param("lft") Integer lft, @Param("rht") Integer rht);

	EventTypeVo getEventTypeById(Long id);

	EventTypeVo getEventTypeByParentIdAndStartNum(@Param("parentId") Long parentId, @Param("startNum") int startNum);

	List<EventTypeVo> getEventTypeListByParentId(@Param("parentId") Long parentId);

	List<EventTypeVo> getChildrenByLeftRightCode(@Param("lft") Integer lft, @Param("rht") Integer rht);

	List<Long> getChildrenIdListByLeftRightCode(@Param("lft") Integer lft, @Param("rht") Integer rht);

	List<AuthorityVo> getAuthorityByEventTypeId(@Param("eventTypId") Long eventTypId);

//	List<AuthorityVo> checkAuthorityIsExists(@Param("lft") Integer lft, @Param("rht") Integer rht,@Param("authorityVo") AuthorityVo authority);

//	List<EventSolutionVo> getSolutionList(EventTypeVo eventTypeVo);

	List<EventSolutionVo> getSolutionListByLftRht(@Param("vo") EventTypeVo vo,@Param("isActive") Integer isActive);

//	int getSolutionCountByEventTypeId(@Param("eventTypId") Long eventTypId);

	int getSolutionCountByLtfRht(@Param("lft") Integer lft, @Param("rht") Integer rht,@Param("isActive") Integer isActive);

	int checkEventTypeNameIsRepeatByLRAndLayer(EventTypeVo eventTypeVo);

	int checkEventTypeNameIsRepeatByParentId(EventTypeVo eventTypeVo);

	int updateEventTypeParentIdById(EventTypeVo eventTypeVo);

	int updateEventTypeLayer(EventTypeVo eventType);

	int batchUpdateEventTypeLeftRightCodeByLeftRightCode(@Param("lft") Integer lft, @Param("rht") Integer rht, @Param("step") int step);

	int batchUpdateEventTypeLeftCode(@Param("minCode")Integer minCode, @Param("step") int step);

	int batchUpdateEventTypeRightCode(@Param("minCode")Integer minCode, @Param("step") int step);

	int insertEventType(EventTypeVo eventTypeVo);

	int insertEventTypeAuthority(@Param("authorityVo") AuthorityVo authority, @Param("eventTypeId") Long eventTypeId);

	int deleteEventTypeByLeftRightCode(@Param("lft") Integer lft, @Param("rht") Integer rht);

	int deleteAuthorityByEventTypeId(@Param("eventTypeId") Long eventTypeId);

	List<Long> getCurrentUserAuthorizedEventTypeIdList(@Param("userUuid") String userUuid, @Param("teamUuidList") List<String> teamUuidList, @Param("roleUuidList") List<String> roleUuidList);

	List<EventTypeVo> getEventTypeListByIdList(List<Long> authorizedEventTypeIdList);

	int deleteEventTypeByIdList(List<Long> idList);

	int deleteEventTypeAuthorityByEventTypeIdList(List<Long> idList);

	int deleteEventTypeSolutionByEventTypeIdList(List<Long> idList);
}
