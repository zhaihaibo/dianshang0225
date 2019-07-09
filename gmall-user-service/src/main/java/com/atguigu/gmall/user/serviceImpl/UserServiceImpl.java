package com.atguigu.gmall.user.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.user.bean.UmsMember;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UmsMemberMapper umsMemberMapper;

    @Override
    public List<UmsMember> getAllUser() {

        List<UmsMember> allUser = umsMemberMapper.selectAll(); //umsMemberMapper.getAllUser();

        return allUser;
    }
}
