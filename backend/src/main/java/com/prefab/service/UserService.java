package com.prefab.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.prefab.common.PageResult;
import com.prefab.dto.LoginDTO;
import com.prefab.dto.UserDTO;
import com.prefab.dto.UserQueryDTO;
import com.prefab.entity.User;
import com.prefab.vo.LoginVO;
import com.prefab.vo.UserVO;

/**
 * 用户Service接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户登录
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     */
    void register(UserDTO userDTO);

    /**
     * 获取用户信息
     */
    UserVO getUserInfo(Long userId);

    /**
     * 分页查询用户
     */
    PageResult<UserVO> pageUsers(UserQueryDTO queryDTO);

    /**
     * 新增用户
     */
    void addUser(UserDTO userDTO);

    /**
     * 更新用户
     */
    void updateUser(UserDTO userDTO);

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 重置密码
     */
    void resetPassword(Long userId, String newPassword);

    /**
     * 修改用户状态
     */
    void updateStatus(Long userId, Integer status);
}
