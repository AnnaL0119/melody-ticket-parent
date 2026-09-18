package com.melody.user.controller;

import com.melody.common.Result;
import com.melody.user.dto.LoginDTO;
import com.melody.user.dto.RegisterDTO;
import com.melody.user.dto.UserVO;
import com.melody.user.service.UserService;
import com.melody.user.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 用户注册
     * POST /users/register
     */
    @PostMapping("/register")
    public Result<UserVO> register(@RequestBody RegisterDTO dto) {
        UserVO user = userService.register(dto);
        return Result.success("注册成功", user);
    }

    /**
     * 用户登录
     * POST /users/login
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO dto) {
        Map<String, Object> result = userService.login(dto);
        return Result.success("登录成功", result);
    }

    /**
     * 获取当前用户信息（需要Token）
     * GET /users/me
     */
    @GetMapping("/me")
    public Result<UserVO> getCurrentUser(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null || !jwtUtil.validateToken(token)) {
            return Result.error(401, "未登录或Token无效");
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        UserVO user = userService.getUserById(userId);
        return Result.success(user);
    }

    /**
     * 根据ID获取用户信息
     * GET /users/{id}
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return Result.success(user);
    }

    /**
     * 健康检查
     * GET /users/health
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("user-service is running!");
    }

    /**
     * 从请求头中提取Token
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}