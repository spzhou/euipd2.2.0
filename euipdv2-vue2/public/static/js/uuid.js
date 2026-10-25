/*
 * Author: 周朔鹏
 * Date: 2024-11-24
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 */

Uuid = {
    shortUuid: function (len = 8) {
        let strVals = 'QAZXSWEDCVFRTGBNHYUJMKIOLPqazxswedcvfrtgbnhyujmkiolp1234567890';
        let maxLen = strVals.length;
        let randomStr = '';
        for (var i = 0; i < len; i++) {
            randomStr += strVals.charAt(Math.floor(Math.random() * maxLen));
        }
        return randomStr;
    },

    uuid: function (len = 32) {
        let strVals = 'QAZXSWEDCVFRTGBNHYUJMKIOLPqazxswedcvfrtgbnhyujmkiolp1234567890';
        let maxLen = strVals.length;
        let randomStr = '';
        for (var i = 0; i < len; i++) {
            randomStr += strVals.charAt(Math.floor(Math.random() * maxLen));
        }
        return randomStr;
    },

    timeStamp: function(){
        return new Date().getTime();
    },

    orderNo: function () {
        const now = new Date()
        const year = now.getFullYear();
        let month = now.getMonth() + 1;
        let day = now.getDate();
        let hour = now.getHours();
        let minutes = now.getMinutes();
        let seconds = now.getSeconds();
        String(month).length < 2 ? (month = Number("0" + month)) : month;
        String(day).length < 2 ? (day = Number("0" + day)) : day;
        String(hour).length < 2 ? (hour = Number("0" + hour)) : hour;
        String(minutes).length < 2 ? (minutes = Number("0" + minutes)) : minutes;
        String(seconds).length < 2 ? (seconds = Number("0" + seconds)) : seconds;
        const timeString = `${year}${month}${day}${hour}${minutes}${seconds}`;
        return timeString + '-' + this.timeStamp().toString().substring(8);
    },
}
