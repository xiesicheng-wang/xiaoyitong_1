package com.youlinbang.module.user.service;

import com.youlinbang.module.user.dto.LoginDTO;
import com.youlinbang.module.user.dto.RegisterDTO;
import com.youlinbang.module.user.vo.LoginVO;
import com.youlinbang.module.user.vo.UserVO;

public interface UserService {

    UserVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserVO getById(Long id);
}
