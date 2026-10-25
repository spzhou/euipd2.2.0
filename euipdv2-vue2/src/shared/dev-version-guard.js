/*
 * Author: 周朔鹏
 * Date: 2026-10-13
 * Email: 391902958@qq.com
 * Website: https://www.euipd.com
 *
 * 立项类请求：主开发版本低于所属产品已有最大版本时，后端返回 DEV_VERSION_LOWER；
 * 经确认作为维护版本后带 maintenance=true 重发。取消时返回 { data: { success: false, cancelled: true } }。
 */
import Swal from 'sweetalert2';

export const DEV_VERSION_LOWER = 'DEV_VERSION_LOWER';

export function postWithMaintenance(vm, url, body) {
  return vm.$ajax.post(url, body).then((response) => {
    const resp = response.data || {};
    if (resp.success || resp.code !== DEV_VERSION_LOWER) {
      return response;
    }
    return Swal.fire({
      title: '维护版本确认',
      text: resp.message,
      icon: 'question',
      showCancelButton: true,
      confirmButtonColor: '#409EFF',
      cancelButtonColor: '#909399',
      confirmButtonText: '作为维护版本保存',
      cancelButtonText: '取消',
    }).then((result) => {
      if (!result.value) {
        return { data: { success: false, cancelled: true, message: '' } };
      }
      return vm.$ajax.post(url, Object.assign({}, body, { maintenance: true }));
    });
  });
}

export default { postWithMaintenance, DEV_VERSION_LOWER };
