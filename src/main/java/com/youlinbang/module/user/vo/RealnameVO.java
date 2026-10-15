package com.youlinbang.module.user.vo;

import com.youlinbang.module.user.entity.UserRealname;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实名信息视图（对外只有掩码，绝不返回哈希与原文）
 */
@Data
public class RealnameVO {

    private Long id;
    private Long userId;
    private String realName;
    private String idCardMasked;
    private String photoUrl;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createTime;

    public static RealnameVO from(UserRealname r) {
        RealnameVO vo = new RealnameVO();
        vo.setId(r.getId());
        vo.setUserId(r.getUserId());
        vo.setRealName(r.getRealName());
        vo.setIdCardMasked(r.getIdCardMasked());
        vo.setPhotoUrl(r.getPhotoUrl());
        vo.setStatus(r.getStatus());
        vo.setRejectReason(r.getRejectReason());
        vo.setCreateTime(r.getCreateTime());
        return vo;
    }
}
