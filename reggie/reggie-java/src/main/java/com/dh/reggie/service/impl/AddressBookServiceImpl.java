package com.dh.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dh.reggie.common.BaseContext;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.AddressBook;
import com.dh.reggie.mapper.AddressBookMapper;
import com.dh.reggie.service.IAddressBookService;
import org.apache.tomcat.jni.Address;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook> implements IAddressBookService {
    @Override
    public R<List<AddressBook>> listAdd() {
        Long userId = BaseContext.get();


        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getUserId, userId).orderByDesc(AddressBook::getIsDefault);

        List<AddressBook> list = list(lqw);
        return R.success(list);
    }

    @Override
    public R<String> addAdd(AddressBook addressBook) {
        Long userId = BaseContext.get();

        addressBook.setUserId(userId);
        addressBook.setIsDefault(0);
        if (save(addressBook)) {
            return R.success("添加成功");
        }
        return R.error("添加失败！");
    }

    @Override
    @Transactional
    public R<String> editDefault(AddressBook address) {
        Long userId = BaseContext.get();

        LambdaUpdateWrapper<AddressBook> luw = new LambdaUpdateWrapper<>();
        luw.eq(AddressBook::getUserId, userId);
        luw.set(AddressBook::getIsDefault, 0);
        update(luw);

        address.setIsDefault(1);
        updateById(address);

        return R.success("默认地址修改成功");
    }

    @Override
    public R<AddressBook> selectOne(Long id) {

        if (id != null) {
            return R.success(getById(id));
        }
        return R.error("地址不存在");
    }

    @Override
    public R<String> deleteAdd(Long id) {
        if (id != null) {
            if (removeById(id)) {
                return R.success("删除地址成功");
            }
            return R.error("删除失败！");
        }
        return R.error("删除失败！");
    }

    @Override
    public R<String> editAdd(AddressBook addressBook) {

        if (updateById(addressBook)) {

            return R.success("地址修改成功");
        }
        return R.success("地址修改失败");

    }

    @Override
    public R<AddressBook> getDefaultAdd() {
        Long userId = BaseContext.get();

        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getUserId, userId);

        lqw.eq(AddressBook::getIsDefault, 1);

        AddressBook add = getOne(lqw);
        if (add != null) {
            return R.success(add);
        }
        return R.error("获取地址错误！");
    }


}
