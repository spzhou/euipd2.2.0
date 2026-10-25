/*
 * Author: 周朔鹏
 * Date: 2024-05-23
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 */

Validator = {
  require: function (value, text, swal, alert=false) {
    if (Tool.isEmpty(value)) {
      if(!alert){
        Toast.warning(text + "不能为空", swal);
      }else{
        window.alert("\"" +text + "\"" + "不能为空");
      }
      return false;
    } else {
      return true
    }
  },

  length: function (value, text, min, max, swal, alert=false) {
    if (Tool.isEmpty(value)) {
      if(!alert){
        Toast.warning(text + "不能为空", swal);
      }else{
        window.alert("\"" +text + "\"" + "不能为空");
      }
      return false;
    }else if (!Tool.isLength(value, min, max)) {
      if(!alert){
        Toast.warning(text + "长度" + min + "~" + max + "位", swal);
      }else{
        window.alert("\"" +text + "\"" + "长度" + min + "~" + max + "位");
      }
      return false;
    } else {
      return true
    }
  },

  /**
   * 富文本字数校验：按纯文本计数，空白与格式不计。
   */
  richLength: function (value, text, min, max, swal, alert=false) {
    let count = Tool.richTextLength(value);
    let msg = null;
    if (count === 0 && min > 0) {
      msg = text + "不能为空";
    } else if (count < min || count > max) {
      msg = text + "字数" + min + "~" + max + "字（当前 " + count + " 字，不计空白与格式）";
    }
    if (msg === null) {
      return true;
    }
    if(!alert){
      Toast.warning(msg, swal);
    }else{
      window.alert(msg);
    }
    return false;
  },

  isNumber: function (value, text, swal) {
    if (! Number.isNaN(Number(value))) {
      return true;
    }else{
      Toast.warning(text + "不是有效数字", swal);
      return false;
    }
  },

  isStrictEqual: function (value1, value2, text, swal) {
    if (value1 === value2) {
      return true;
    }else{
      Toast.warning(text, swal);
      return false;
    }

  },

  greatThanZero: function (value, text, swal, alert=false) {
    if (Tool.isEmpty(value)) {
      if(!alert){
        Toast.warning(text + "不能为空", swal);
      }else{
        window.alert("\"" +text + "\"" + "不能为空");
      }
      return false;
    }else if(isNaN(value)){
      if(!alert){
        Toast.warning(text + "不是有效数字", swal);
      }else{
        window.alert("\"" +text + "\"" + "不是有效数字");
      }
      return false;
    }else if(value <= 0){
      if(!alert){
        Toast.warning(text + "输入错误", swal);
      }else{
        window.alert("\"" +text + "\"" + "输入错误");
      }
      return false;
    }
    return true
  },

  arrayIncludes: function (array, testStr, text, swal, alert=false) {
    if (Tool.isEmpty(array)) {
      if(!alert){
        Toast.warning(text + "参数错误", swal);
      }else{
        window.alert("\"" +text + "\"" + "参数错误");
      }
      return false;
    }else if(Tool.isEmpty(testStr)) {
      if(!alert){
        Toast.warning(text + "不能为空", swal);
      }else{
        window.alert("\"" +text + "\"" + "不能为空");
      }
      return false;
    }
    if(array.includes(testStr)){
      return true;
    }else{
      if(!alert){
        Toast.warning(text + "输入错误", swal);
      }else{
        window.alert("\"" +text + "\"" + "输入错误");
      }
      return false;
    }
  },

  isMobile: function (str, text, swal){
    const reg = /^[1][3,4,5,6,7,8,9][0-9]{9}$/
    if(reg.test(str)){
      return reg.test(str)
    }else{
      Toast.warning(text + "不是有效电话号码", swal);
      return false;
    }
  },

  /*
  * 判断是否为有效年份
  * 日期格式为: YYYY-MM-DD
  * */
  isDate: function (str, text, swal, alert=false){
    const regex = /^[1-9]\d{3}-(0[1-9]|1[0-2])-(0[1-9]|[12]\d|3[01])$/
    if(regex.test(str)){
      return true;
    }else{
      if(!alert){
        Toast.warning(text + "不是有效日期格式", swal);
      }else{
        window.alert("\"" +text + "\"" + "不是有效日期格式");
      }

      return false;
    }
  },

  isPhone: function (str, text, swal){
    const reg = /^((\(\d{3,4}\)|\d{3,4}-|\s)?\d{7,14})|([1][3,4,5,6,7,8,9][0-9]{9})$/
    if(reg.test(str)){
      return reg.test(str)
    }else{
      Toast.warning(text + "不是有效电话号码", swal);
      return false;
    }
  }



};
