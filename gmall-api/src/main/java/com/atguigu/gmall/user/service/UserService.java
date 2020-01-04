package com.atguigu.gmall.user.service;

import com.atguigu.gmall.user.bean.UmsMember;
import com.atguigu.gmall.user.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUser();

    UmsMember login(UmsMember umsMember);



    void putTokenToCache(String token,String id);

    List<UmsMemberReceiveAddress> getMemberAddressesById(String memberId);



    UmsMember addOauthUser(UmsMember umsMember);

    UmsMemberReceiveAddress getMemberAddressesByAddressId(String receiveAddressId);
}
