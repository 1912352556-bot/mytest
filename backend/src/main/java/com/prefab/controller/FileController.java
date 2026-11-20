package com.prefab.controller;

import com.prefab.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传控制器
 */
@Slf4j
@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
public class FileController {

    // 图片上传路径
    // 上级目录的pic文件夹
    private static final String UPLOAD_PATH = "..\\..\\..\\..\\..\\..\\..\\..\\pic";
    
    // 访问路径前缀
    @Value("${server.port:8080}")
    private String serverPort;

    @Operation(summary = "上传图片")
    @PostMapping("/upload")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        try {
            // 确保上传目录存在
            File uploadDir = new File(UPLOAD_PATH);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 获取原始文件名和扩展名
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 生成唯一文件名
            String filename = UUID.randomUUID().toString() + extension;
            
            // 保存文件
            Path filePath = Paths.get(UPLOAD_PATH, filename);
            Files.write(filePath, file.getBytes());

            // 返回访问URL
            String fileUrl = "/api/file/image/" + filename;
            log.info("文件上传成功: {} -> {}", originalFilename, fileUrl);
            
            return Result.success(fileUrl);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.fail("文件上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "访问图片")
    @GetMapping("/image/{filename}")
    public byte[] getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_PATH, filename);
            if (!Files.exists(filePath)) {
                log.warn("图片文件不存在: {}", filename);
                return new byte[0];
            }
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("读取图片失败: {}", filename, e);
            return new byte[0];
        }
    }

    @Operation(summary = "删除图片")
    @DeleteMapping("/image/{filename}")
    public Result<Void> deleteImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_PATH, filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("图片删除成功: {}", filename);
                return Result.success();
            } else {
                return Result.fail("文件不存在");
            }
        } catch (IOException e) {
            log.error("删除图片失败: {}", filename, e);
            return Result.fail("删除图片失败: " + e.getMessage());
        }
    }
}
