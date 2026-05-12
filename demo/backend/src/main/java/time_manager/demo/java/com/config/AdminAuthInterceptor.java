package time_manager.demo.java.com.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(mapper.writeValueAsString(
                    new time_manager.demo.java.com.dto.ApiResponse(401, "缺少认证令牌", null)));
            return false;
        }

        String token = authHeader.substring(7);
        Map<String, Object> claims = JwtUtil.parseToken(token);
        if (claims == null) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(mapper.writeValueAsString(
                    new time_manager.demo.java.com.dto.ApiResponse(401, "令牌无效或已过期", null)));
            return false;
        }

        String role = (String) claims.get("role");
        if (!"admin".equals(role)) {
            response.setStatus(403);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(mapper.writeValueAsString(
                    new time_manager.demo.java.com.dto.ApiResponse(403, "无管理员权限", null)));
            return false;
        }

        request.setAttribute("userId", claims.get("userId"));
        request.setAttribute("role", role);
        return true;
    }
}
