package org.wangchenlong.wcl_aidl_demo;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 书籍管理服务
 * <p/>
 * Created by wangchenlong on 16/5/12.
 */
public class BookManagerService extends Service {

    private int num = 0;

    private static final String TAG = "DEBUG-WCL: " + BookManagerService.class.getSimpleName();

    /**
     * 支持并发读写
     */
    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();
    private RemoteCallbackList<IOnNewBookArrivedListener> mListenerList = new RemoteCallbackList<>();
    /**
     * 在java.util.concurrent.atomic包下，有AtomicBoolean , AtomicInteger, AtomicLong, AtomicReference等类，
     * 它们的基本特性就是在多线程环境下，执行这些类实例包含的方法时
     */
    private AtomicBoolean mIsServiceDestroyed = new AtomicBoolean(false);

    /**
     * start binder
     */
    private Binder mBinder = new IBookManager.Stub() {

        @Override
        public List<Book> getBookList(){
            // 延迟加载
            SystemClock.sleep(5000);
            return mBookList;
        }

        @Override
        public void addBook(Book book){
            mBookList.add(book);
        }

        @Override
        public void registerListener(IOnNewBookArrivedListener listener){
            mListenerList.register(listener);
            int num = mListenerList.beginBroadcast();
            mListenerList.finishBroadcast();
            Log.e(TAG, "添加完成, 注册接口数: " + num);
        }

        @Override
        public void unregisterListener(IOnNewBookArrivedListener listener){
            mListenerList.unregister(listener);
            int num = mListenerList.beginBroadcast();
            mListenerList.finishBroadcast();
            Log.e(TAG, "删除完成, 注册接口数: " + num);
        }

        @Override
        public byte[] foo(byte[] data){

            byte[] temp = new byte[data.length+1];
            boolean b = false;
            temp[0] = (byte) (b ? 0x01 : 0x00);
            for (int i =0;i < data.length;i++){
                temp[i+1] = data[i];
            }
            return temp;
        }
    };
    /**
     * end binder
     */


    private void onNewBookArrived(Book book) throws RemoteException {
        mBookList.add(book);
        int num = mListenerList.beginBroadcast();
        Log.e(TAG, num + "  :新书到达的数量    发送通知的数量: " + mBookList.size());
        for (int i = 0; i < num; ++i) {
            IOnNewBookArrivedListener listener = mListenerList.getBroadcastItem(i);
            Log.e(TAG, "发送通知: " + listener.toString());
            listener.onNewBookArrived(book);
        }
        mListenerList.finishBroadcast();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBookList.add(new Book(1, "Android"));
        mBookList.add(new Book(2, "iOS"));
        //也可以执行这条语句   new Thread(new ServiceWorker()).start();
        mHandler.sendEmptyMessage(3);
    }

    @Override
    public void onDestroy() {
        mIsServiceDestroyed.set(true);
        super.onDestroy();
    }

    /**
     * 定时器，每5秒执行一次
     */
//    private class ServiceWorker implements Runnable {
//        @Override
//        public void run() {
//            while (!mIsServiceDestroyed.get()) {
//                try {
//                    Thread.sleep(5000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                num++;
//                if (num == 5) {
//                    mIsServiceDestroyed.set(true);
//                }
//                Message msg = new Message();
//                // 向Handler发送消息,更新UI
//                mHandler.sendMessage(msg);
//            }
//        }
//    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int bookId = 1 + mBookList.size();
            Book newBook = new Book(bookId, "新书#" + bookId);
            try {
                onNewBookArrived(newBook);
                num++;
                if (num == 10) {
                    mIsServiceDestroyed.set(true);
                }else {
                    sendEmptyMessageDelayed(2, 5000);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * 提供和客户端交流的Binder对象
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

}