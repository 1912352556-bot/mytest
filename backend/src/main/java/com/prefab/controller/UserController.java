package com.prefab.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.prefab.common.PageResult;
import com.prefab.common.Result;
import com.prefab.dto.LoginDTO;
import com.prefab.entity.User;
import com.prefab.mapper.UserMapper;
import com.prefab.service.UserService;
import com.prefab.vo.LoginVO;
import com.prefab.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户控制器
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@RequestBody User user) {
        user.setStatus(1);
        user.setRole(2); // 默认配送员
        userMapper.insert(user);
        return Result.success();
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            userId = 1L; // 默认管理员
        }
        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody Object data, HttpServletRequest request) {
        // 简化实现
        return Result.success();
    }

    @Operation(summary = "获取用户列表")
    @GetMapping("/list")
    public Result<PageResult<User>> listUsers(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realname,
            @RequestParam(required = false) Integer role) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), User::getUsername, username)
               .like(StringUtils.hasText(realname), User::getRealname, realname)
               .eq(role != null, User::getRole, role)
               .orderByDesc(User::getCreateTime);
        Page<User> result = userMapper.selectPage(page, wrapper);
        return Result.success(new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> addUser(@RequestBody User user) {
        user.setStatus(1);
        userMapper.insert(user);
        return Result.success();
    }

    @Operation(summary = "更新用户")
    @PutMapping
    public Result<Void> updateUser(@RequestBody User user) {
        userMapper.updateById(user);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userMapper.deleteById(id);
        return Result.success();
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/password/reset")
    public Result<Void> resetPassword(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPassword("123456"); // 重置为默认密码
            userMapper.updateById(user);
        }
        return Result.success();
    }

    @Operation(summary = "修改用户状态")
    @PutMapping("/{id}/status/{status}")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
        return Result.success();
    }
}
