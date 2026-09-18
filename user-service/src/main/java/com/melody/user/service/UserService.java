package com.melody.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.melody.common.exception.BizException;
import com.melody.user.dto.LoginDTO;
import com.melody.user.dto.RegisterDTO;
import com.melody.user.dto.UserVO;
import com.melody.user.entity.User;
import com.melody.user.mapper.UserMapper;
import com.melody.user.utils.JwtUtil;
import com.melody.user.utils.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    public UserVO register(RegisterDTO dto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        if (this.count(wrapper) > 0) {
            throw new BizException("用户名已存在");
        }

        // 检查手机号是否已存在
        if (dto.getPhone() != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getPhone, dto.getPhone());
            if (this.count(wrapper) > 0) {
                throw new BizException("手机号已被注册");
            }
        }

        // 检查邮箱是否已存在
        if (dto.getEmail() != null) {
            wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, dto.getEmail());
            if (this.count(wrapper) > 0) {
                throw new BizException("邮箱已被注册");
            }
        }

        // 创建新用户
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole("USER");
        user.setStatus(1);

        this.save(user);

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(LoginDTO dto) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());
        User user = this.getOne(wrapper);

        if (user == null) {
            throw new BizException("用户名或密码错误");
        }

        // 验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }

        // 检查用户状态
        if (user.getStatus() == 0) {
            throw new BizException("账号已被禁用");
        }

        // 生成 Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 返回用户信息
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", vo);
        return result;
    }

    /**
     * 根据ID获取用户信息
     */
    public UserVO getUserById(Long id) {
        User user = this.getById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}