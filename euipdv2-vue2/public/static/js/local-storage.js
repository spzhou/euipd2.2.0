/*
 * Author: 周朔鹏
 * Date: 2023-04-30
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 */

LOCAL_KEY_REMEMBER_USER = "LOCAL_KEY_REMEMBER_USER"; // 记住我
LOCAL_KEY_LOGIN_GROUP = "LOCAL_KEY_LOGIN_GROUP"; // 登录分组

LocalStorage = {
    get: function (key) {
        let v = localStorage.getItem(key);
        if (v && typeof(v) !== "undefined" && v !== "undefined") {
            return JSON.parse(v);
        }
    },
    set: function (key, data) {
        localStorage.setItem(key, JSON.stringify(data));
    },
    remove: function (key) {
        localStorage.removeItem(key);
    },
    clearAll: function () {
        localStorage.clear();
    }
};
