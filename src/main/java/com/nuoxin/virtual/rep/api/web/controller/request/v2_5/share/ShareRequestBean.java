package com.nuoxin.virtual.rep.api.web.controller.request.v2_5.share;

import com.nuoxin.virtual.rep.api.common.bean.PageRequestBean;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 分享记录请求参数
 * @author tiancun
 * @date 2018-09-20
 */
@ApiModel(value = "分享记录请求参数")
@Data
public class ShareRequestBean extends PageRequestBean implements Serializable {
    private static final long serialVersionUID = 3384357325002007060L;

    @ApiModelProperty(value = "医生ID")
    private Long doctorId;

    @ApiModelProperty(value = "前端不用传", hidden = true)
    private String leaderPath;



}
