package com.atguigu.gmall.user.serviceImpl;


import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.manage.redisutils.RedisUtil;
import com.atguigu.gmall.user.bean.UmsMember;
import com.atguigu.gmall.user.bean.UmsMemberReceiveAddress;
import com.atguigu.gmall.user.mapper.UmsMemberMapper;
import com.atguigu.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Autowired
    UmsMemberMapper umsMemberMapper;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public List<UmsMember> getAllUser() {

        List<UmsMember> allUser = umsMemberMapper.selectAll(); //umsMemberMapper.getAllUser();

        return allUser;
    }

    @Override
    public UmsMember login(UmsMember umsMember) {
        UmsMember umsMember1 = new UmsMember();
        umsMember1.setUsername(umsMember.getUsername());
        umsMember1.setPassword(umsMember.getPassword());
        UmsMember umsMember2 = umsMemberMapper.selectOne(umsMember1);
        return umsMember2;
    }

    @Override
    public void putTokenToCache(String token,String id) {
        Jedis jedis = redisUtil.getJedis();
        try {
            String key = "token:"+id+":info";
            jedis.setex(key,60*60,token);
        } finally {
            jedis.close();
        }


    }

    //查询用户的收货列表
    @Override
    public List<UmsMemberReceiveAddress> getMemberAddressesById(String memberId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();

        umsMemberReceiveAddress.setMemberId(memberId);
        List<UmsMemberReceiveAddress> select = umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);

        return select;
    }

    //添加微博用户到数据库
    @Override
    public UmsMember addOauthUser(UmsMember umsMember) {
        UmsMember umsMemberReturn = new UmsMember();
        UmsMember umsMember1 = new UmsMember();
        umsMember1.setSourceUid(umsMember.getSourceUid());
        List<UmsMember> select = umsMemberMapper.select(umsMember1);
        if (select==null||select.size()==0) {
            umsMemberMapper.insertSelective(umsMember);
            umsMemberReturn=umsMember;
        }else {
            umsMemberReturn=select.get(0);
        }
        return  umsMemberReturn;
    }

    @Override
    public UmsMemberReceiveAddress getMemberAddressesByAddressId(String receiveAddressId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(receiveAddressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);

        return umsMemberReceiveAddress1;
    }

}
