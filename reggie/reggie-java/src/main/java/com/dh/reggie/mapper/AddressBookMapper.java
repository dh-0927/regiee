package com.dh.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dh.reggie.entiry.AddressBook;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AddressBookMapper extends BaseMapper<AddressBook> {
}
