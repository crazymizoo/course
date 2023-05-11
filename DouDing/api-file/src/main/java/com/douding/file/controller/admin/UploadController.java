package com.douding.file.controller.admin;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSS;
import com.douding.server.domain.Teacher;
import com.douding.server.domain.Test;
import com.douding.server.dto.FileDto;
import com.douding.server.dto.PageDto;
import com.douding.server.dto.ResponseDto;
import com.douding.server.enums.FileUseEnum;
import com.douding.server.exception.BusinessException;
import com.douding.server.exception.BusinessExceptionCode;
import com.douding.server.service.FileService;
import com.douding.server.service.TestService;
import com.douding.server.util.Base64ToMultipartFile;
import com.douding.server.util.CopyUtil;
import com.douding.server.util.UuidUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.*;
import java.util.List;

/*
    返回json 应用@RestController
    返回页面  用用@Controller
 */
@RequestMapping("/admin/file")
@RestController
public class UploadController {

    private static final Logger LOG = LoggerFactory.getLogger(UploadController.class);
    public static final String BUSINESS_NAME = "文件上传";
    @Resource
    private TestService testService;

    @Value("${file.path}")
    private String FILE_PATH;

    @Value("${file.domain}")
    private String FILE_DOMAIN;

    @Resource
    private FileService fileService;

    @RequestMapping("/upload")
    public ResponseDto upload(@RequestBody FileDto fileDto) throws Exception {
        LOG.info("文件分片上传请求开始,请求参数: {}", JSON.toJSONString(fileDto));
        //将Base64编码的文件片段转换成MultipartFile对象，以便后续进行文件合并操作
        MultipartFile multipartFile = Base64ToMultipartFile.base64ToMultipart(fileDto.getShard());
        //用本地文件夹地址拼接传入的参数use(判断是哪个类型)，得到一个路径
        String localDirPath = FILE_PATH + FileUseEnum.getByCode(fileDto.getUse());
        File dirFile = new File(localDirPath);
        //文件夹不存在
        if (!dirFile.exists() && !dirFile.mkdirs()) {
            throw new Exception("文件夹创建失败，创建路径:" + localDirPath);
        }
        //本地文件全路径
        String fileFullPath = localDirPath + File.separator + fileDto.getKey() + "." + fileDto.getSuffix();
        //获取文件分片全路径
        String fileShardFullPath = fileFullPath + "." + fileDto.getShardIndex();
        /**
         * 将文件保存到 fileShardFullPath 指定的完整路径中
         * fileShardFullPath 是指分片文件的完整路径，通过调用 new File(fileShardFullPath)
         * 创建了一个 File 对象，表示要写入文件的目标文件。调用 transferTo() 方法之后，就会将 multipartFile
         * 中的文件内容写入到该目标文件中。
         */
        multipartFile.transferTo(new File(fileShardFullPath));
        //更新文件表信息，没有上传过这个文件就新插入一条，有就直接更新索引值
        String relaPath = FileUseEnum.getByCode(fileDto.getUse()) + "/" + fileDto.getKey() + "." + fileDto.getSuffix();
        fileDto.setPath(relaPath);
        fileService.save(fileDto);
        //判断当前分片索引是否等于分片总数，如果等于分片总数则执行文件合并
        if (fileDto.getShardIndex().equals(fileDto.getShardTotal())) {
            fileDto.setPath(fileFullPath);
            //文件合并
            merge(fileDto);
        }
        ResponseDto responseDto = new ResponseDto();
        FileDto result = new FileDto();
        //设置文件映射地址给前端
        result.setPath(FILE_DOMAIN + "/" + relaPath);
        responseDto.setContent(result);
        LOG.info("文件分片上传结束，请求结果:{}", JSON.toJSONString(responseDto));
        return responseDto;
    }



    //合并分片
    public void merge(FileDto fileDto) throws Exception {
        LOG.info("合并分片开始");
        String path = fileDto.getPath();
        //创建一个输出流OutputStream，输出流指向需要合并的文件path
        try (OutputStream outputStream = new FileOutputStream(path, true)) {
            //通过循环，逐个读取每个分片的内容，创建一个输入流FileInputStream指向需要读取
            // 的分片文件，循环中使用一个字节数组byte[]来存储分片的内容
            for (Integer i = 1; i <= fileDto.getShardTotal(); i++) {
                try (FileInputStream inputStream = new FileInputStream(path + "." + i);) {
                    byte[] bytes = new byte[10 * 1024 * 1024];
                    int len;
                    //将分片文件中的内容读取到字节数组中，并使用输出流将字节数组中的数据写入到需要合并的文件中
                    while ((len = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, len);
                    }
                }

            }
        } catch (Exception e) {
            LOG.error("文件合并失败，失败原因:{}", e.getMessage(), e);
        }


        //文件上传完成后，需要将这些文件片段删除，以释放磁盘空间
        for (Integer i = 1; i <= fileDto.getShardTotal(); i++) {
            File file = new File(path + "." + i);
            file.delete();
        }

    }

    @GetMapping("/check/{key}")
    public ResponseDto check(@PathVariable String key) throws Exception {
        LOG.info("检查上传分片开始：{}", key);

        if (StringUtils.isEmpty(key)) {
            throw new BusinessException(BusinessExceptionCode.PICTURE_NOT_FOUND_EXCEPTION);
        }

        FileDto fileDto = fileService.findByKey(key);
        ResponseDto<FileDto> responseDto = new ResponseDto();

        //如果不为空，则返回映射地址以及文件上传进度
        if (fileDto != null) {
            //将文件映射地址告知前端
            fileDto.setPath(FILE_DOMAIN + "/" + fileDto.getPath());
            responseDto.setContent(fileDto);
        }

        return responseDto;
    }


}//end class
