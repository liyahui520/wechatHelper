package com.nuoxin.virtual.rep.api.mybatis;

import com.nuoxin.virtual.rep.api.entity.v3_0.params.MeetingRecordParams;
import com.nuoxin.virtual.rep.api.entity.v3_0.params.MeetingSubjectParams;
import com.nuoxin.virtual.rep.api.entity.v3_0.request.MeetingRecordRequest;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 会议记录查询Mapper
 * @author wujiang
 * @date 20190429
 */
public interface MeetingRecordMapper {

    /**
     * 会议记录查询列表
     * @param meetingRecordRequest
     * @return list
     */
    List<MeetingRecordParams> getMeetingRecordList(@Param("meetingRecordRequest") MeetingRecordRequest meetingRecordRequest);

    /**
     * 会议记录查询列表总数
     * @param meetingRecordRequest
     * @return int
     */
    Integer getMeetingRecordListCount(@Param("meetingRecordRequest") MeetingRecordRequest meetingRecordRequest);

    /**
     * 获取产品下的所有招募医生
     * @param productId
     * @return int
     */
    Integer getRecruitHcpAllCountByProduct(@Param("productId") Integer productId);

    /**
     * 查询每个会议的主题列表
     * @param productId
     * @param meetingName
     * @return
     */
    List<MeetingSubjectParams> getMeetingSubjectListByProductIdAndMeetingName(@Param("productId") Integer productId,@Param("meetingName") String meetingName);

    /**
     * 根据会议ID查询会议详情
     * @param meetingId
     * @return
     */
    MeetingRecordParams getMeetingInfoByMeetingId(@Param("meetingId") String meetingId);

}
