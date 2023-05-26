package com.dh.reggie.controller;

import com.dh.reggie.common.R;
import com.dh.reggie.entiry.AddressBook;
import com.dh.reggie.service.IAddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private IAddressBookService addressBookService;

    @GetMapping("/list")
    public R<List<AddressBook>> listAdd() {
        return addressBookService.listAdd();
    }

    @PostMapping
    public R<String> addAdd(@RequestBody AddressBook addressBook) {
        return addressBookService.addAdd(addressBook);
    }

    @PutMapping("/default")
    public R<String> editDefault(@RequestBody AddressBook addressBook) {
        return addressBookService.editDefault(addressBook);
    }

    @PutMapping
    public R<String> editAdd(@RequestBody AddressBook addressBook) {
        return addressBookService.editAdd(addressBook);
    }

    @GetMapping("/{id}")
    public R<AddressBook> selectOne(@PathVariable("id") Long id) {
        return addressBookService.selectOne(id);

    }

    @DeleteMapping
    public R<String> deleteAdd(@RequestParam("id") Long id) {
        return addressBookService.deleteAdd(id);
    }

    @GetMapping("/default")
    public R<AddressBook> getDefaultAdd() {
        return addressBookService.getDefaultAdd();
    }

}
