package com.youlinbang.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.youlinbang.common.exception.BusinessException;
import com.youlinbang.common.result.PageResult;
import com.youlinbang.common.util.HashUtil;
import com.youlinbang.module.user.entity.User;
import com.youlinbang.module.user.entity.UserRealname;
import com.youlinbang.module.user.mapper.UserMapper;
import com.youlinbang.module.user.mapper.UserRealnameMapper;
import com.youlinbang.module.user.service.RealnameService;
import com.youlinbang.module.user.vo.RealnameVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RealnameServiceImpl implements RealnameService {

    private final UserRealnameMapper realnameMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public RealnameVO submit(Long userId, String realName, String idCard, String photoUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw BusinessException.business("用户不存在");
        }
        if (user.getRealnameStatus() != null && user.getRealnameStatus() == 2) {
            throw BusinessException.business("您已通过实名认证，无需重复提交");
        }
        String normalized = idCard.toUpperCase();
        String hash = HashUtil.sha256Hex(normalized);

        // 一个身份证号全局只能绑定一个账号（哈希唯一）
        Long hashExists = realnameMapper.selectCount(new LambdaQueryWrapper<UserRealname>()
                .eq(UserRealname::getIdCardHash, hash)
                .ne(UserRealname::getUserId, userId));
        if (hashExists > 0) {
            throw BusinessException.business("该身份证号已被其他账号实名");
        }
        // 已有待审核记录则拒绝重复提交
        Long pending = realnameMapper.selectCount(new LambdaQueryWrapper<UserRealname>()
                .eq(UserRealname::getUserId, userId)
                .eq(UserRealname::getStatus, 1));
        if (pending > 0) {
            throw BusinessException.business("已有待审核的实名申请，请耐心等待");
        }

        UserRealname r = realnameMapper.selectOne(new LambdaQueryWrapper<UserRealname>()
                .eq(UserRealname::getUserId, userId));
        boolean isNew = (r == null);
        if (isNew) {
            r = new UserRealname();
            r.setUserId(userId);
        }
        r.setRealName(realName);
        r.setIdCardMasked(HashUtil.maskIdCard(normalized));
        r.setIdCardHash(hash);
        r.setPhotoUrl(photoUrl);
        r.setStatus(1);
        r.setRejectReason("");
        r.setAuditUserId(null);
        r.setAuditTime(null);
        if (isNew) {
            realnameMapper.insert(r);
        } else {
            realnameMapper.updateById(r);
        }
        // 主表同步为"待审核"
        user.setRealnameStatus(1);
        userMapper.updateById(user);
        log.info("实名提交: userId={}, masked={}", userId, r.getIdCardMasked());
        return RealnameVO.from(r);
    }

    @Override
    public RealnameVO mine(Long userId) {
        UserRealname r = realnameMapper.selectOne(new LambdaQueryWrapper<UserRealname>()
                .eq(UserRealname::getUserId, userId));
        return r == null ? null : RealnameVO.from(r);
    }

    @Override
    public PageResult<RealnameVO> pageForAdmin(long page, long size, Integer status) {
        Page<UserRealname> result = realnameMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<UserRealname>()
                        .eq(status != null, UserRealname::getStatus, status)
                        .orderByAsc(UserRealname::getStatus)
                        .orderByDesc(UserRealname::getCreateTime));
        return new PageResult<>(result.getRecords().stream().map(RealnameVO::from).toList(),
                result.getTotal(), page, size);
    }

    @Override
    @Transactional
    public RealnameVO audit(Long auditUserId, Long realnameId, boolean pass, String reason) {
        // 管理员身份校验
        User admin = userMapper.selectById(auditUserId);
        if (admin == null || admin.getRole() == null || admin.getRole() != 1) {
            throw BusinessException.forbidden("无审核权限");
        }
        UserRealname r = realnameMapper.selectById(realnameId);
        if (r == null) {
            throw BusinessException.business("申请不存在");
        }
        if (r.getStatus() != 1) {
            throw BusinessException.business("该申请已处理过");
        }
        r.setStatus(pass ? 2 : 3);
        r.setRejectReason(pass ? "" : (reason == null || reason.isBlank() ? "资料不符合要求" : reason));
        r.setAuditUserId(auditUserId);
        r.setAuditTime(LocalDateTime.now());
        realnameMapper.updateById(r);

        // 同步主表实名状态
        User user = userMapper.selectById(r.getUserId());
        if (user != null) {
            user.setRealnameStatus(pass ? 2 : 0);
            userMapper.updateById(user);
        }
        log.info("实名审核: id={}, userId={}, pass={}", realnameId, r.getUserId(), pass);
        return RealnameVO.from(r);
    }
}
