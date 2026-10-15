package com.youlinbang.module.user.service;

import com.youlinbang.common.result.PageResult;
import com.youlinbang.module.user.vo.RealnameVO;

public interface RealnameService {

    /** 用户提交实名认证 */
    RealnameVO submit(Long userId, String realName, String idCard, String photoUrl);

    /** 查看自己的实名状态 */
    RealnameVO mine(Long userId);

    /** 管理员分页查看（按状态筛选） */
    PageResult<RealnameVO> pageForAdmin(long page, long size, Integer status);

    /** 管理员审核：pass=true 通过，否则驳回并记录原因 */
    RealnameVO audit(Long auditUserId, Long realnameId, boolean pass, String reason);
}
