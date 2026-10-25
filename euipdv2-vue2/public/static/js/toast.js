/*
 * Author: 周朔鹏
 * Date: 2023-05-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 */


Toast = {
  _fire: function (swal, options) {
    swal.fire(Object.assign({
      returnFocus: false,
      didOpen: function () {
        var container = swal.getContainer && swal.getContainer();
        if (container) {
          // Element Dialog 默认 z-index≈2000，需高于对话框才能看见
          container.style.zIndex = '10000';
        }
      }
    }, options));
  },

  success: function (message, swal) {
    Toast._fire(swal, {
      position: 'top-end',
      icon: 'success',
      title: message,
      showConfirmButton: false,
      timer: 800
    });
  },

  error: function (message, swal) {
    Toast._fire(swal, {
      position: 'top-end',
      icon: 'error',
      title: message,
      showConfirmButton: false,
      timer: 1500
    });
  },

  warning: function (message, swal) {
    Toast._fire(swal, {
      position: 'top-end',
      icon: 'warning',
      title: message,
      showConfirmButton: false,
      timer: 1500
    });
  }
};
