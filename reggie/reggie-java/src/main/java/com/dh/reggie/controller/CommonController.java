package com.dh.reggie.controller;

import com.dh.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/common")
public class CommonController {

    @Value("${reggie.basePath}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile image){
        // 获取原始文件名称
        String originalFilename = image.getOriginalFilename();
        // 生成新文件名
        String newFileName = createNewFileName(originalFilename);
        // 保存文件
        try {
            image.transferTo(new File(basePath + newFileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 返回结果
        return R.success(newFileName);
    }

    @GetMapping("/download")
    public void download(@RequestParam("name") String name, HttpServletResponse response) {
        // 获取输入流
        try {
            FileInputStream input = new FileInputStream(new File(basePath + name));
            ServletOutputStream output = response.getOutputStream();

            response.setContentType("/image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];

            while ((len = input.read(bytes)) != -1) {
                output.write(bytes);
                output.flush();
            }

            input.close();
            output.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private String createNewFileName(String originalFilename) {
        // 获取后缀
        String suffix = StringUtils.substringAfterLast(originalFilename, ".");
        // 生成目录
        String name = UUID.randomUUID().toString();
        // 生成二级目录(最多8个)
//        int hash = name.hashCode();
//        int d1 = hash & 7;
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return name + "." + suffix;
    }
}
