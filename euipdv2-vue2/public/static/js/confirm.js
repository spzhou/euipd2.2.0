/*
 * Author: 周朔鹏
 * Date: 2024-01-12
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 */

Confirm = {
  show: function (swal, message, callback) {
    swal.fire({
      title: '确认？',
      text: message,
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#409EFF',
      cancelButtonColor: '#909399',
      confirmButtonText: '确认!',
      cancelButtonText: '取消!',
      returnFocus: false,
      didOpen: function () {
        var container = swal.getContainer && swal.getContainer();
        if (container) {
          container.style.zIndex = '10000';
        }
      }
    }).then((result) => {
      if (result.value) {
        if (callback) {
          callback()
        }
      }
    })



  }
}
