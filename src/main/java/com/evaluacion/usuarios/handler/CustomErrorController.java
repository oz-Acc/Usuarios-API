package com.evaluacion.usuarios.handler;

import com.evaluacion.usuarios.dto.ApiError;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping(value = "/error", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiError> handleError(HttpServletRequest request) {
        var attrs = errorAttributes.getErrorAttributes(new ServletWebRequest(request), ErrorAttributeOptions.defaults());
        Object statusObj = attrs.get("status");
        int status = (statusObj instanceof Integer) ? (Integer) statusObj : 500;
        String msg = (String) attrs.getOrDefault("error", "Error");
        String message = msg != null ? msg : "Ocurrió un error";
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_JSON).body(new ApiError(message));
    }
}
