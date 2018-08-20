// IOnNewBookArrivedListener.aidl
package org.wangchenlong.wcl_aidl_demo;

// Declare any non-default types here with import statements
import org.wangchenlong.wcl_aidl_demo.Book;

interface IOnNewBookArrivedListener {
    void onNewBookArrived(in Book newBook);
}
