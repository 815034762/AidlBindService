// IBookManager.aidl
package org.wangchenlong.wcl_aidl_demo;

// Declare any non-default types here with import statements

import org.wangchenlong.wcl_aidl_demo.Book;
import org.wangchenlong.wcl_aidl_demo.IOnNewBookArrivedListener;

interface IBookManager {
   // 返回书籍列表
    List<Book> getBookList();
   // 添加书籍
    void addBook(in Book book);
    // 注册接口
    void registerListener(IOnNewBookArrivedListener listener);
    // 注册接口
    void unregisterListener(IOnNewBookArrivedListener listener);

    byte[] foo(in byte[] data);
}
