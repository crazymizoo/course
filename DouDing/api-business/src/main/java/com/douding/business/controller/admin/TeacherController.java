package com.douding.business.controller.admin;




import com.douding.server.dto.TeacherDto;
import com.douding.server.dto.PageDto;
import com.douding.server.dto.ResponseDto;
import com.douding.server.dto.UserDto;
import com.douding.server.service.TeacherService;
import com.douding.server.util.ValidatorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@RestController
@RequestMapping("/admin/teacher")
public class TeacherController {

    private static final Logger LOG = LoggerFactory.getLogger(TeacherController.class);
    //给了日志用的
    public  static final String BUSINESS_NAME ="讲师";

    @Resource
    private TeacherService teacherService;

    @RequestMapping("/list")
    public ResponseDto list(PageDto pageDto){
        ResponseDto<PageDto> responseDto = new ResponseDto<>();
        teacherService.list(pageDto);
        responseDto.setContent(pageDto);
        return responseDto;
    }

    @PostMapping("/save")
    public ResponseDto save(@RequestBody TeacherDto teacherDto){
        //校验非空且长度符合
        ValidatorUtil.require(teacherDto.getName(), "姓名");
        ValidatorUtil.length(teacherDto.getName(), "姓名", 1, 50);
        ValidatorUtil.require(teacherDto.getNickname(), "昵称");
        ValidatorUtil.length(teacherDto.getNickname(), "昵称",1,50);
        ValidatorUtil.require(teacherDto.getName(), "头像");
        ValidatorUtil.length(teacherDto.getName(), "头像", 1, 100);
        ValidatorUtil.require(teacherDto.getPosition(), "职位");
        ValidatorUtil.length(teacherDto.getPosition(), "职位",1,50);
        ValidatorUtil.require(teacherDto.getMotto(), "座右铭");
        ValidatorUtil.length(teacherDto.getMotto(), "座右铭", 0, 50);
        ValidatorUtil.require(teacherDto.getIntro(), "简介");
        ValidatorUtil.length(teacherDto.getIntro(), "简介",0,500);
        ResponseDto<TeacherDto> responseDto = new ResponseDto<>();
        //校验成功进行保存
        teacherService.save(teacherDto);
        responseDto.setContent(teacherDto);
        return responseDto;

    }
    
    @DeleteMapping("/delete/{id}")
    public ResponseDto delete(@PathVariable String id){
        ResponseDto<TeacherDto> responseDto = new ResponseDto<>();
        teacherService.delete(id);
        return responseDto;
    }

    @RequestMapping("/all")
    public ResponseDto all(){


        return null;
    }

}//end class