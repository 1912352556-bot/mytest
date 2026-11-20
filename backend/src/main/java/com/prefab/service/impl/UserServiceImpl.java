package com.prefab.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.prefab.common.PageResult;
import com.prefab.common.ResultCode;
import com.prefab.dto.LoginDTO;
import com.prefab.dto.UserDTO;
import com.prefab.dto.UserQueryDTO;
import com.prefab.entity.User;
import com.prefab.exception.BusinessException;
import com.prefab.mapper.UserMapper;
import com.prefab.service.UserService;
import com.prefab.utils.JwtUtil;
import com.prefab.vo.LoginVO;
import com.prefab.vo.UserVO;
import lombok.RequiredArgsConstructor;
import cn.hutool.crypto.digest.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户Service实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;


    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 查询用户
        User user = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginDTO.getUsername()));
        
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        // 验证密码：库中为 BCrypt 哈希（$2a/$2b 开头）时走密文校验，历史明文数据兼容直接比较
        boolean passwordMatch = false;
        String dbPassword = user.getPassword();
        if (dbPassword != null && dbPassword.startsWith("$2")) {
            try {
                passwordMatch = BCrypt.checkpw(loginDTO.getPassword(), dbPassword);
            } catch (Exception e) {
                // 哈希格式异常时按不通过处理，避免绕过校验
                passwordMatch = false;
            }
        } else {
            passwordMatch = dbPassword != null && dbPassword.equals(loginDTO.getPassword());
        }
        if (!passwordMatch) {
            throw new BusinessException(ResultCode.USER_PASSWORD_ERROR);
        }
        
        // 检查状态
        if (user.getStatus() == 0) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        
        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealname(user.getRealname());
        loginVO.setRole(user.getRole());
        loginVO.setAvatar(user.getAvatar());
        loginVO.setToken(token);
        
        return loginVO;
    }

    @Override
    public void register(UserDTO userDTO) {
        // 检查用户名是否存在
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, userDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        
        // 创建用户
        User user = new User();
        BeanUtil.copyProperties(userDTO, user);
        user.setPassword(BCrypt.hashpw(userDTO.getPassword()));
        user.setStatus(1);
        
        this.save(user);
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public PageResult<UserVO> pageUsers(UserQueryDTO queryDTO) {
        Page<User> page = new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize());
        
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), User::getUsername, queryDTO.getUsername())
               .like(StringUtils.hasText(queryDTO.getRealname()), User::getRealname, queryDTO.getRealname())
               .eq(queryDTO.getRole() != null, User::getRole, queryDTO.getRole())
               .like(StringUtils.hasText(queryDTO.getPhone()), User::getPhone, queryDTO.getPhone())
               .orderByDesc(User::getCreateTime);
        
        Page<User> result = this.page(page, wrapper);
        
        List<UserVO> voList = result.getRecords().stream()
                .map(user -> {
                    UserVO vo = new UserVO();
                    BeanUtil.copyProperties(user, vo);
                    return vo;
                })
                .collect(Collectors.toList());
        
        return new PageResult<>(voList, result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public void addUser(UserDTO userDTO) {
        // 检查用户名是否存在
        long count = this.count(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, userDTO.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }
        
        User user = new User();
        BeanUtil.copyProperties(userDTO, user);
        user.setPassword(BCrypt.hashpw(userDTO.getPassword()));
        
        this.save(user);
    }

    @Override
    public void updateUser(UserDTO userDTO) {
        User user = this.getById(userDTO.getId());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        BeanUtil.copyProperties(userDTO, user, "id", "password", "username");
        this.updateById(user);
    }

    @Override
    public void deleteUser(Long userId) {
        this.removeById(userId);
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        user.setPassword(BCrypt.hashpw(newPassword));
        this.updateById(user);
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        
        user.setStatus(status);
        this.updateById(user);
    }
}
