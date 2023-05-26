package com.dh.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dh.reggie.common.R;
import com.dh.reggie.entiry.AddressBook;

import java.util.List;

public interface IAddressBookService extends IService<AddressBook> {
    R<List<AddressBook>> listAdd();

    R<String> addAdd(AddressBook addressBook);

    R<String> editDefault(AddressBook addressBook);

    R<AddressBook> selectOne(Long id);

    R<String> deleteAdd(Long id);

    R<String> editAdd(AddressBook addressBook);

    R<AddressBook> getDefaultAdd();
}
